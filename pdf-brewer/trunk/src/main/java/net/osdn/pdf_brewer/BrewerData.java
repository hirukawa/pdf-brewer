package net.osdn.pdf_brewer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Scanner;

import net.osdn.pdf_brewer.instruction.text.TextOverflow;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

import net.osdn.pdf_brewer.instruction.Align;
import net.osdn.pdf_brewer.instruction.Box;
import net.osdn.pdf_brewer.instruction.Image;
import net.osdn.pdf_brewer.instruction.Instruction;
import net.osdn.pdf_brewer.instruction.Line;
import net.osdn.pdf_brewer.instruction.LineStyle;
import net.osdn.pdf_brewer.instruction.Media;
import net.osdn.pdf_brewer.instruction.NewPage;
import net.osdn.pdf_brewer.instruction.Rect;
import net.osdn.pdf_brewer.instruction.text.Font;
import net.osdn.pdf_brewer.instruction.text.LineHeight;
import net.osdn.pdf_brewer.instruction.text.Text;
import net.osdn.pdf_brewer.instruction.text.TextAlign;

public class BrewerData {

	private FontLoader fontLoader;
	private String title;
	private String author;
	private PDRectangle mediaBox;
	private List<Instruction> instructions = new ArrayList<Instruction>();

	public BrewerData(Path path, FontLoader fontLoader) throws IOException {
		if(!Files.exists(path) || Files.isDirectory(path)) {
			throw new IllegalArgumentException();
		}
		String s = path.getFileName().toString().toLowerCase();
		if(!s.endsWith(".pb")) {
			throw new IllegalArgumentException();
		}
		
		List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
		
		this.fontLoader = fontLoader;
		initialize(lines);
	}

	public BrewerData(String pbData, FontLoader fontLoader) throws IOException {
		List<String> lines = new ArrayList<String>();
		BufferedReader r = new BufferedReader(new StringReader(pbData));
		String line;
		while((line = r.readLine()) != null) {
			lines.add(line);
		}
		r.close();
		
		this.fontLoader = fontLoader;
		initialize(lines);
	}
	
	public BrewerData(List<String> lines, FontLoader fontLoader) throws IOException {
		this.fontLoader = fontLoader;
		initialize(lines);
	}
	
	private void initialize(List<String> lines) throws IOException {
		for(String line : lines) {
			Entry<Integer, List<Object>> result = parse(line);
			int indent = result.getKey();
			List<Object> tokens = result.getValue();
			if(tokens.size() >= 1 && tokens.get(0) instanceof String) {
				String first = (String)tokens.remove(0);
				if(first.charAt(0) == '\\') {
					first = first.substring(1).toLowerCase();
					try {
						if(first.equals("media")) { //mediaはinstructionsに追加しません。
							if(mediaBox == null) {
								Media media = new Media(indent, tokens);
								mediaBox = media.getRectangle();
							}
						} else if(first.equals("align")) {
							instructions.add(new Align(indent, tokens));
						} else if(first.equals("box")) {
							instructions.add(new Box(indent, tokens));
						} else if(first.equals("image")) {
							instructions.add(new Image(indent, tokens));
						} else if(first.equals("line")) {
							instructions.add(new Line(indent, tokens));
						} else if(first.equals("line-style")) {
							instructions.add(new LineStyle(indent, tokens));
						} else if(first.equals("new-page")) {
							instructions.add(new NewPage(indent, tokens));
						} else if(first.equals("rect")) {
							instructions.add(new Rect(indent, tokens));
						} else if(first.equals("font")) {
							instructions.add(new Font(fontLoader, indent, tokens));
						} else if(first.equals("line-height")) {
							instructions.add(new LineHeight(indent, tokens));
						} else if(first.equals("text")) {
							String text = getRawText(first, line);
							if(text.length() > 0) {
								instructions.add(new Text(indent, text));
							}
						} else if(first.equals("text-align")) {
							instructions.add(new TextAlign(indent, tokens));
						} else if(first.equals("text-overflow")) {
							instructions.add(new TextOverflow(indent, tokens));
						}
					} catch(IllegalArgumentException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	public String getTitle() {
		return this.title;
	}
	
	public String getAuthor() {
		return this.author;
	}
	
	public PDRectangle getMediaBox() {
		return this.mediaBox;
	}
	
	public List<Instruction> getInstructions() {
		return instructions;
	}
	
	private static Entry<Integer, List<Object>> parse(String line) {
		List<Object> tokens = new LinkedList<Object>();
		
		StringBuilder indent = new StringBuilder();
		int i = 0;
		for(i = 0; i < line.length(); i++) {
			char c = line.charAt(i);
			if(c == ' ' || c == '\t') {
				indent.append(c);
			} else if(c == '\u3000') { //全角スペースは半角スペース2つに置き換えます。
				indent.append(' ');
				indent.append(' ');
			} else {
				break;
			}
		}
		line = line.substring(i);

		for(String from : new String[] { "   \t", "  \t", " \t" }) {
			i = 0;
			while((i = indent.indexOf(from, i)) != -1) {
				indent.replace(i,  i + from.length(),  "\t");
				i += 1;
			}
		}
		for(String from : new String[] { "   ", "  ", " " }) {
			i = 0;
			while((i = indent.indexOf(from, i)) != -1) {
				indent.replace(i,  i + from.length(),  "\t");
				i += 1;
			}
		}
		
		Scanner scanner = null;
		try {
			scanner = new Scanner(line);
			while(scanner.hasNext()) {
				String s = scanner.next();
				double d;
				try {
					d = Double.parseDouble(s);
					if(d == 0.0 && s.charAt(0) == '-') {
						d = -Double.MIN_VALUE;
					}
					tokens.add(d);
				} catch(NumberFormatException e) {
					tokens.add(s);
				}
			}
		} finally {
			if(scanner != null) {
				scanner.close();
			}
		}
		
		return new AbstractMap.SimpleEntry<Integer, List<Object>>(indent.length(), tokens);
	}
	
	private static String getRawText(String first, String line) {
		StringBuilder indent = new StringBuilder();
		int i = 0;
		for(i = 0; i < line.length(); i++) {
			char c = line.charAt(i);
			if(c == ' ' || c == '\t') {
				indent.append(c);
			} else if(c == '\u3000') { //全角スペースは半角スペース2つに置き換えます。
				indent.append(' ');
				indent.append(' ');
			} else {
				break;
			}
		}
		String text = line.substring(i + first.length() + 2);
		text = text.replace("\\n", "\n");
		text = text.replace('\t', ' ');
		return text;
	}
	
	
	public static File getApplicationDirectory(Class<?> cls) {
		try {
			ProtectionDomain pd = cls.getProtectionDomain();
			CodeSource cs = pd.getCodeSource();
			URL location = cs.getLocation();
			URI uri = location.toURI();
			String path = uri.getPath();
			File file = new File(path);
			return file.getParentFile();
		} catch (Exception e) {
			try {
				return new File(".").getCanonicalFile();
			} catch (IOException e1) {
				return new File(".").getAbsoluteFile();
			}
		}
	}
	
	/*
	public List<String> processYaml(Path path) throws IOException, TemplateException {
		List<String> lines = new ArrayList<String>();
		
		File appDir = getApplicationDirectory(BrewerData.class);
		Configuration freeMarker = new Configuration(Configuration.VERSION_2_3_26);
		freeMarker.setDefaultEncoding("UTF-8");
		freeMarker.setDirectoryForTemplateLoading(new File(appDir, "templates"));
		
		A
		
		Yaml yml = new Yaml(file);
		Object obj;
		
		Template template = null;
		obj = yml.get("template");
		if(obj instanceof String) {
			String filename = (String)obj;
			template = freeMarker.getTemplate(filename);
		}
		if(template == null) {
			return null;
		}
		obj = yml.get("title");
		if(obj instanceof String) {
			this.title = (String)obj;
		}
		obj = yml.get("author");
		if(obj instanceof String) {
			this.author = (String)obj;
		}
		
		StringWriter writer = null;
		BufferedReader reader = null;
		try {
			writer = new StringWriter();
			template.process(yml.getMap(), writer);
			reader = new BufferedReader(new StringReader(writer.toString()));
			String line;
			while((line = reader.readLine()) != null) {
				lines.add(line);
			}
		} finally {
			if(reader != null) {
				reader.close();
			}
			if(writer != null) {
				writer.close();
			}
		}
		
		return lines;
	}
	*/
}
