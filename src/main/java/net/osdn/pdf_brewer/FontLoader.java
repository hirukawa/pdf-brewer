package net.osdn.pdf_brewer;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.fontbox.ttf.NameRecord;
import org.apache.fontbox.ttf.NamingTable;
import org.apache.fontbox.ttf.TTFParser;
import org.apache.fontbox.ttf.TrueTypeCollection;
import org.apache.fontbox.ttf.TrueTypeCollection.TrueTypeFontProcessor;
import org.apache.fontbox.ttf.TrueTypeFont;

public class FontLoader {

	public static final List<String> FILENAMES_YUGOTHIC;
	public static final List<String> FILENAMES_YUMINCHO;
	public static final List<String> FILENAMES_IPA_GOTHIC;
	public static final List<String> FILENAMES_IPA_MINCHO;
	public static final List<String> FILENAMES_NOTO_GOTHIC;
	public static final List<String> FILENAMES_NOTO_MINCHO;

	static {
		//
		// for Windows
		//
		FILENAMES_YUGOTHIC = Arrays.asList(
				"yugothib.ttf",
				"yugothic-bold.ttf",
				"yugothic.ttf",
				"yugothil.ttf",
				"yugothb.ttc",
				"yugothl.ttc",
				"yugothm.ttc",
				"yugothr.ttc"
		);
		FILENAMES_YUMINCHO = Arrays.asList(
				"yumin.ttf",
				"yumindb.ttf",
				"yuminl.ttf"
		);

		//
		// for NOTO
		//
		FILENAMES_NOTO_GOTHIC = Arrays.asList(
				"GenShinGothic-Regular.ttf",
				"GenShinGothic-Bold.ttf"
		);
		FILENAMES_NOTO_MINCHO = Arrays.asList(
				"GenYoMinJP-R.ttf",
				"GenYoMinJP-B.ttf"
		);

		//
		// for IPA
		//
		FILENAMES_IPA_GOTHIC = Arrays.asList(
				"ipaexg.ttf"
		);
		FILENAMES_IPA_MINCHO = Arrays.asList(
				"ipaexm.ttf"
		);
	}

	public static File getDefaultFontDir() {
		String windir = System.getenv("windir");
		if(windir != null) {
			return new File(windir, "Fonts");
		}
		return null;
	}

	private Map<String, TrueTypeFont> fonts = new HashMap<String, TrueTypeFont>();
	private Map<TrueTypeFont, File> ttcFiles = new HashMap<TrueTypeFont, File>();

	public FontLoader(File fontDir) {
		this(fontDir, null, null);
	}

	public FontLoader(File fontDir, Collection<String> fileNames, Collection<String> fontNames) {
		load(fontDir, fileNames, fontNames);
		
		TrueTypeFont serif;
		TrueTypeFont serifBold;
		TrueTypeFont sansSerif;
		TrueTypeFont sansSerifBold;

		// for Windows
		serif = getFont("YuMincho-Regular");
		serifBold = getFont("YuMincho-Demibold");
		sansSerif = getFont("YuGothic-Regular");
		sansSerifBold = getFont("YuGothic-Bold");

		// for Noto (fallback)
		if(serif == null) {
			serif = getFont("r-源様明朝");
		}
		if(serifBold == null) {
			serifBold = getFont("b-源様明朝");
		}
		if(sansSerif == null) {
			sansSerif = getFont("regular-源真ゴシック");
		}
		if(sansSerifBold == null) {
			sansSerifBold = getFont("bold-源真ゴシック");
		}

		// for IPA (fallback)
		if(serif == null) {
			serif = getFont("ipaexmincho");
		}
		if(serifBold == null) {
			serifBold = getFont("ipaexmincho");
		}
		if(sansSerif == null) {
			sansSerif = getFont("ipaexgothic");
		}
		if(sansSerifBold == null) {
			sansSerifBold = getFont("ipaexgothic");
		}

		if(serif != null && serifBold != null && sansSerif != null && sansSerifBold != null) {
			try {
				register("serif", serif);
				register("serif-Bold", serifBold);
				register("sansSerif", sansSerif);
				register("sansSerif-Bold", sansSerifBold);
				register("sans-Serif", sansSerif);
				register("sans-Serif-Bold", sansSerifBold);
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public List<TrueTypeFont> listFonts() {
		List<TrueTypeFont> list = new ArrayList<TrueTypeFont>(fonts.values());
		Collections.sort(list, new Comparator<TrueTypeFont>() {
			@Override
			public int compare(TrueTypeFont o1, TrueTypeFont o2) {
				try {
					return o1.getName().compareTo(o2.getName());
				} catch (IOException e) {
					return 0;
				}
			}
		});
		return list;
	}
	
	public TrueTypeFont getFont(String name) {
		if(name == null || name.isBlank()) {
			return null;
		}

		TrueTypeFont ttf = fonts.get(normalize(name));
		if(ttf != null) {
			return ttf;
		}
			
		ttf = fonts.get(normalize(name + "-Regular"));
		if(ttf != null) {
			return ttf;
		}
		
		ttf = fonts.get(normalize(name + "-標準"));
		if(ttf != null) {
			return ttf;
		}
		
		return null;
	}

	public File getTtcFile(TrueTypeFont ttf) throws IOException {
		return ttcFiles.get(ttf);
	}

	public void load(File dir, Collection<String> fileNames, Collection<String> fontNames) {
		if(dir == null) {
			return;
		}

		Set<String> fileNameSet = null;
		if(fileNames != null) {
			fileNameSet = new HashSet<String>();
			for(String fileName : fileNames) {
				fileNameSet.add(fileName.toLowerCase());
			}
		}

		Set<String> fontNameSet = null;
		if(fontNames != null) {
			fontNameSet = new HashSet<String>();
			for(String fontName : fontNames) {
				fontNameSet.add(normalize(fontName));
			}
		}

		TTFParser parser = new TTFParser();
		File[] ttfFiles = dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".ttf");
			}
		});
		for(File file : ttfFiles) {
			if(fileNameSet != null && !fileNameSet.contains(file.getName().toLowerCase())) {
				// fileNames が指定されている場合は一致するファイルのみを処理対象にします。（一致しない場合は continue で次へ）
				continue;
			}
			TrueTypeFont ttf = null;
			try {
				ttf = parser.parse(file);

				if(fontNameSet == null) {
					register(ttf);
					ttf = null; // 登録したttfがfinallyでcloseされないようにします。
				} else {
					List<String> normalizedNames = getNormalizedNames(ttf);
					if(!Collections.disjoint(fontNameSet, normalizedNames)) {
						for(String s : normalizedNames) {
							register(s, ttf);
							ttf = null; // 登録したttfがfinallyでcloseされないようにします。
						}
					}
				}
			} catch(IOException e) {
				e.printStackTrace();
			} finally {
				if(ttf != null) {
					try { ttf.close(); } catch(IOException ignore) {}
				}
			}
		}
		
		File[] ttcFiles = dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".ttc");
			}
		});
		for(File file : ttcFiles) {
			if(fileNameSet != null && !fileNameSet.contains(file.getName().toLowerCase())) {
				// fileNames が指定されている場合は一致するファイルのみを処理対象にします。（一致しない場合は continue で次へ）
				continue;
			}

			TrueTypeCollection ttc = null;
			try {
				final Set<String> _fontNameSet = fontNameSet;
				ttc = new TrueTypeCollection(file);
				ttc.processAllFonts(new TrueTypeFontProcessor() {
					@Override
					public void process(TrueTypeFont ttf) throws IOException {
						try {
							if(_fontNameSet == null) {
								register(ttf);
								FontLoader.this.ttcFiles.put(ttf, file);
								ttf = null; // 登録したttfがfinallyでcloseされないようにします。
							} else {
								List<String> normalizedNames = getNormalizedNames(ttf);
								if(!Collections.disjoint(_fontNameSet, normalizedNames)) {
									for(String s : normalizedNames) {
										register(s, ttf);
									}
									FontLoader.this.ttcFiles.put(ttf, file);
									ttf = null; // 登録したttfがfinallyでcloseされないようにします。
								}
							}
						} finally {
							if(ttf != null) {
								try { ttf.close(); } catch(IOException ignore) {}
							}
						}
					}
				});
			} catch(IOException e) {
				e.printStackTrace();
			} finally {
				if(ttc != null) {
					try { ttc.close(); } catch(Exception e) {}
				}
			}
		}
	}
	
	public void register(TrueTypeFont ttf) throws IOException {
		register(null, ttf);
	}
	
	public void register(String name, TrueTypeFont ttf) throws IOException {
		if(name != null) {
			fonts.put(normalize(name), ttf);
			return;
		}
		for(String nomalizedName : getNormalizedNames(ttf)) {
			fonts.put(nomalizedName, ttf);
		}
	}

	public List<String> getNormalizedNames(TrueTypeFont ttf) throws IOException {
		List<String> names = new ArrayList<String>();
		String fontName = ttf.getName();
		if(fontName != null && !fontName.isBlank()) {
			names.add(normalize(fontName));
		}

		NamingTable table = ttf.getNaming();
		String fontFamily = table.getFontFamily();
		String fontSubFamily = table.getFontSubFamily();
		if(fontFamily != null && !fontFamily.isBlank() && fontSubFamily != null && !fontSubFamily.isBlank()) {
			names.add(normalize(fontFamily + "-" + fontSubFamily));
		}

		String postScriptName = table.getPostScriptName();
		if(postScriptName != null && !postScriptName.isBlank()) {
			names.add(normalize(postScriptName));
		}

		Set<Integer> languages = new HashSet<Integer>();
		Map<Integer, String> fontFamilies1 = new HashMap<Integer, String>();
		Map<Integer, String> fontSubFamilies2 = new HashMap<Integer, String>();
		Map<Integer, String> fontFamilies16 = new HashMap<Integer, String>();
		Map<Integer, String> fontSubFamilies17 = new HashMap<Integer, String>();
		List<NameRecord> records = table.getNameRecords();
		for(NameRecord record : records) {
			if(record.getNameId() == 1) {
				languages.add(record.getLanguageId());
				fontFamilies1.put(record.getLanguageId(), record.getString());
			} else if(record.getNameId() == 2) {
				languages.add(record.getLanguageId());
				fontSubFamilies2.put(record.getLanguageId(), record.getString());
			} else if(record.getNameId() == 16) {
				languages.add(record.getLanguageId());
				fontFamilies16.put(record.getLanguageId(), record.getString());
			} else if(record.getNameId() == 17) {
				languages.add(record.getLanguageId());
				fontSubFamilies17.put(record.getLanguageId(), record.getString());
			}
		}
		for(Integer languageId : languages) {
			fontFamily = fontFamilies1.get(languageId);
			if(fontFamily != null && !fontFamily.isBlank()) {
				fontSubFamily = fontSubFamilies2.get(languageId);
				if(fontSubFamily != null && !fontSubFamily.isBlank()) {
					names.add(normalize(fontFamily + "-" + fontSubFamily));
				} else {
					names.add(normalize(fontFamily));
				}
			}
			fontFamily = fontFamilies16.get(languageId);
			if(fontFamily != null && !fontFamily.isBlank()) {
				fontSubFamily = fontSubFamilies17.get(languageId);
				if(fontSubFamily != null && !fontSubFamily.isBlank()) {
					names.add(normalize(fontFamily + "-" + fontSubFamily));
				} else {
					names.add(normalize(fontFamily));
				}
			}
		}
		return names;
	}


	private String normalize(String name) {
		String s = name.replace('\t', '-').replace('\u3000', '-').replace(' ', '-').toLowerCase();
		while(s.contains("--")) {
			s = s.replace("--", "-");
		}
		s = Normalizer.normalize(s, Normalizer.Form.NFKC);
		String[] tokens = s.split("-");
		Arrays.sort(tokens);
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < tokens.length; i++) {
			sb.append(tokens[i]);
			if(i + 1 < tokens.length) {
				sb.append('-');
			}
		}
		return sb.toString();
	}

	public void close() {
		ttcFiles.clear();

		for(TrueTypeFont ttf : fonts.values()) {
			try { ttf.close(); } catch(IOException ignore) {}
		}
		fonts.clear();
	}
}
