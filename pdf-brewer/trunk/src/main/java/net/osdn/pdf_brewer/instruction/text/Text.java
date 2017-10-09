package net.osdn.pdf_brewer.instruction.text;

import java.util.Arrays;

public class Text extends TextBufferingInstruction {

	private String text;
	
	public Text(int indent, String text) {
		super(indent, Arrays.asList(text));
		this.text = text;
	}
	
	public String getText() {
		return text;
	}
	
	public void setText(String text) {
		this.text = text;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < getIndent(); i++) {
			sb.append("- ");
		}
		sb.append('\\');
		sb.append(getClass().getSimpleName());
		sb.append(' ');
		sb.append(text);
		return sb.toString();
	}
}
