package net.osdn.pdf_brewer.instruction.text;

import java.io.IOException;
import java.util.List;

import org.apache.fontbox.ttf.TrueTypeFont;

import net.osdn.pdf_brewer.FontLoader;

public class Font extends TextBufferingInstruction {
	
	private String fontName;
	private float fontSize = Float.NaN;
	
	public Font(FontLoader fontLoader, int indent, List<Object> params) throws IOException {
		super(indent, params);
		
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < params.size(); i++) {
			Object obj = params.get(i);
			if(obj instanceof Number) {
				fontSize = ((Number)obj).floatValue();
			} else if(obj instanceof String) {
				sb.append(' ');
				sb.append(obj);
			}
		}
		if(sb.length() >= 2) {
			String name = sb.substring(1);
			TrueTypeFont ttf = fontLoader.get(name);
			if(ttf != null) {
				fontName = ttf.getName();
			}
		}
	}

	public String getFontName() {
		return fontName;
	}
	
	public float getFontSize() {
		return fontSize;
	}
}
