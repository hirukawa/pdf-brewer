package net.osdn.pdf_brewer;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
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

	private Map<String, TrueTypeFont> fonts = new HashMap<String, TrueTypeFont>();
	private Map<TrueTypeFont, File> files = new HashMap<TrueTypeFont, File>();
	
	public FontLoader(File fontDir) {
		load(fontDir);
		
		TrueTypeFont serif;
		TrueTypeFont serifBold;
		TrueTypeFont sansSerif;
		TrueTypeFont sansSerifBold;
		
		serif = get("YuMincho-Regular");
		serifBold = get("YuMincho-Demibold");
		sansSerif = get("YuGothic-Regular");
		sansSerifBold = get("YuGothic-Bold");

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
	
	public TrueTypeFont get(String name) {
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
	
	public File getFile(TrueTypeFont ttf) throws IOException {
		return files.get(ttf);
	}
	
	public void load(File dir) {
		TTFParser parser = new TTFParser();
		File[] ttfFiles = dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".ttf");
			}
		});
		for(File file : ttfFiles) {
			try {
				TrueTypeFont ttf = parser.parse(file);
				register(ttf);
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
		
		File[] ttcFiles = dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".ttc");
			}
		});
		for(File file : ttcFiles) {
			TrueTypeCollection ttc = null;
			try {
				ttc = new TrueTypeCollection(file);
				ttc.processAllFonts(new TrueTypeFontProcessor() {
					@Override
					public void process(TrueTypeFont ttf) throws IOException {
						register(ttf);
						files.put(ttf, file);
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
		
		List<String> names = new ArrayList<String>();
		String fontName = ttf.getName();
		names.add(normalize(fontName));
		
		NamingTable table = ttf.getNaming();
		String fontFamily = table.getFontFamily();
		String fontSubFamily = table.getFontSubFamily();
		names.add(normalize(fontFamily + "-" + fontSubFamily));
		
		String postScriptName = table.getPostScriptName();
		names.add(normalize(postScriptName));
		
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
			if(fontFamily != null) {
				fontSubFamily = fontSubFamilies2.get(languageId);
				if(fontSubFamily != null) {
					names.add(normalize(fontFamily + "-" + fontSubFamily));
				} else {
					names.add(normalize(fontFamily));
				}
			}
			fontFamily = fontFamilies16.get(languageId);
			if(fontFamily != null) {
				fontSubFamily = fontSubFamilies17.get(languageId);
				if(fontSubFamily != null) {
					names.add(normalize(fontFamily + "-" + fontSubFamily));
				} else {
					names.add(normalize(fontFamily));
				}
			}
		}
		
		for(String s : names) {
			fonts.put(s, ttf);
		}
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
}
