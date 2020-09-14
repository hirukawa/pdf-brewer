package net.osdn.pdf_brewer;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Calendar;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.fontbox.ttf.TrueTypeCollection;
import org.apache.fontbox.ttf.TrueTypeFont;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDTrueTypeFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

import net.osdn.pdf_brewer.instruction.Instruction;
import net.osdn.pdf_brewer.instruction.text.TextBufferingInstruction;

public class PdfBrewer {

	public static void main(String[] args) throws Exception {

		new PdfBrewer();
		System.in.read();
	}

	
	private static PDFont FONT_NULL;
	
	static {
		try {
			FONT_NULL = new PDTrueTypeFont(new COSDictionary());
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	public static String getDefaultProducer() {
		Package pkg = PDDocument.class.getPackage();
		String title = pkg.getSpecificationTitle();
		if(title == null || title.trim().length() == 0) {
			title = "Apache PDFBox";
		}
		String version = pkg.getSpecificationVersion();
		if(version == null) {
			version = "";
		}
		return title + " " + version;
	}
	
	public static String getDefaultCreator() {
		Package pkg = PdfBrewer.class.getPackage();
		String title = pkg.getSpecificationTitle();
		if(title == null || title.trim().length() == 0) {
			title = "PDF Brewer";
		}
		String version = pkg.getSpecificationVersion();
		if(version == null) {
			version = "";
		}
		return title + " " + version;
	}
	
	private PDRectangle mediaBox;
	private PDDocument document;
	private PDPage page;
	private PDPageContentStream stream;
	private FontLoader fontLoader;
	private boolean isFontLoaderAutoGenerated;
	private Map<String, PDFont> fonts = new HashMap<String, PDFont>();
	private Set<TrueTypeCollection> ttcLoaded = new HashSet<TrueTypeCollection>();

	public PdfBrewer() {
		this(FontLoader.getDefaultFontDir().toPath(), null, null);
	}

	public PdfBrewer(Path fontDir, Collection<String> fileNames, Collection<String> fontNames) {
		this(new FontLoader(fontDir.toFile(), fileNames, fontNames));
		isFontLoaderAutoGenerated = true;
	}

	public PdfBrewer(FontLoader fontLoader) {
		if(fontLoader == null) {
			throw new IllegalArgumentException("fontLoader is null");
		}
		this.fontLoader = fontLoader;
		this.document = new PDDocument();
		
		PDDocumentInformation info = document.getDocumentInformation();
		info.setProducer(getDefaultProducer());
		info.setCreator(getDefaultCreator());
	}


	
	public FontLoader getFontLoader() {
		return fontLoader;
	}
	
	public String getProducer() {
		PDDocumentInformation info = document.getDocumentInformation();
		return info.getProducer();
	}

	public void setProducer(String producer) {
		if(producer == null) {
			producer = "";
		}
		PDDocumentInformation info = document.getDocumentInformation();
		info.setProducer(producer);
	}
	
	public String getCreator() {
		PDDocumentInformation info = document.getDocumentInformation();
		return info.getCreator();
	}

	public void setCreator(String creator) {
		if(creator == null) {
			creator = "";
		}
		PDDocumentInformation info = document.getDocumentInformation();
		info.setCreator(creator);
	}

	public String getTitle() {
		PDDocumentInformation info = document.getDocumentInformation();
		return info.getTitle();
	}

	public void setTitle(String title) {
		PDDocumentInformation info = document.getDocumentInformation();
		info.setTitle(title);
	}

	public String getAuthor() {
		PDDocumentInformation info = document.getDocumentInformation();
		return info.getAuthor();
	}
	
	public void setAuthor(String author) {
		PDDocumentInformation info = document.getDocumentInformation();
		info.setAuthor(author);
	}
	
	public PDDocument getDocument() {
		return document;
	}
	
	public PDPage getPage() {
		return page;
	}
	
	public PDPageContentStream getContentStream() {
		return stream;
	}

	public void newPage() throws IOException {
		if(stream != null) {
			stream.close();
		}
		page = new PDPage(mediaBox);
		document.addPage(page);
		stream = new PDPageContentStream(document, page);
	}
	
	public PDFont loadFont(String fontName) throws IOException {
		PDFont font = null;
		TrueTypeFont ttf = getFontLoader().getFont(fontName);
		if(ttf != null) {
			font = fonts.get(ttf.getName());
			if(font == null) {
				File ttcFile = getFontLoader().getTtcFile(ttf);
				if(ttcFile != null) {
					TrueTypeCollection ttc = new TrueTypeCollection(ttcFile);
					font = PDType0Font.load(document, ttc.getFontByName(ttf.getName()), true);
					ttcLoaded.add(ttc);
				} else {
					font = PDType0Font.load(document, ttf, true);
				}
				fonts.put(ttf.getName(), font);
			}
		} else {
			font = FONT_NULL;
		}
		if(font == FONT_NULL) {
			return null;
		}
		return font;
	}

	public void process(BrewerData pb) throws IOException {
		Deque<Context> stack = new ArrayDeque<Context>();
		
		mediaBox = pb.getMediaBox();
		if(mediaBox == null) {
			mediaBox = PDRectangle.A4;
		}
		
		Context context = new Context(new Context(getFontLoader(), mediaBox), 0);
		TextBuffer textBuffer = new TextBuffer();
		
		if(pb.getTitle() != null) {
			setTitle(pb.getTitle());
		}
		if(pb.getAuthor() != null) {
			setAuthor(pb.getAuthor());
		}
		
		newPage();
		
		List<Instruction> instructions = pb.getInstructions();
		for(Instruction instruction : instructions) {
			while(instruction.getIndent() < context.getIndent()) {
				if(!textBuffer.isEmpty()) {
					textBuffer.process(this, context);
					textBuffer.clear();
				}
				context = stack.pop();
			}
			if(instruction.getIndent() > context.getIndent()) {
				if(!textBuffer.isEmpty()) {
					textBuffer.process(this, context);
					textBuffer.clear();
				}
				stack.push(context);
				context = new Context(context, instruction.getIndent());
			}
			if(instruction instanceof TextBufferingInstruction) {
				textBuffer.add((TextBufferingInstruction)instruction);
			} else {
				if(!textBuffer.isEmpty()) {
					textBuffer.process(this, context);
					textBuffer.clear();
				}
				instruction.process(this, context);
			}
		}
		if(!textBuffer.isEmpty()) {
			textBuffer.process(this, context);
			textBuffer.clear();
		}
	}
	
	public void save(String pathname) throws IOException {
		try(OutputStream f = new FileOutputStream(pathname);
			OutputStream out = new BufferedOutputStream(f)) {
			save(out);
		}
	}
	
	public void save(File file) throws IOException {
		try(OutputStream f = new FileOutputStream(file);
			OutputStream out = new BufferedOutputStream(f)) {
			save(out);
		}
	}

	public void save(Path path) throws IOException {
		try(OutputStream f = Files.newOutputStream(path);
			OutputStream out = new BufferedOutputStream(f)) {
			save(out);
		}
	}
	
	public void save(OutputStream output) throws IOException {
		if(stream != null) {
			stream.close();
			stream = null;
		}
		
		PDDocumentInformation info = document.getDocumentInformation();
		Calendar date = Calendar.getInstance();
		info.setCreationDate(date);
		info.setModificationDate(date);
		
		/*
		PDAcroForm form = new PDAcroForm(document);
		PDSignatureField sigField = new PDSignatureField(form);
		form.setFields(Arrays.asList(sigField));
		document.getDocumentCatalog().setAcroForm(form);
		*/

		document.save(output);
	}

	public void close() throws IOException {
		if(document != null) {
			document.close();
			document = null;
		}

		for(TrueTypeCollection ttc : ttcLoaded) {
			ttc.close();
		}
		ttcLoaded.clear();

		if(isFontLoaderAutoGenerated) {
			fontLoader.close();
		}
	}
}
