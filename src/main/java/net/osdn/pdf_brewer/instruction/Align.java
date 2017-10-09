package net.osdn.pdf_brewer.instruction;

import java.io.IOException;
import java.util.List;

import net.osdn.pdf_brewer.Context;
import net.osdn.pdf_brewer.Horizontal;
import net.osdn.pdf_brewer.PdfBrewer;
import net.osdn.pdf_brewer.Vertical;

public class Align extends Instruction {
	
	private Horizontal hAlign;
	private Vertical vAlign;
	
	public Align(int indent, List<Object> params) {
		super(indent, params);
		
		for(int i = 0; i < params.size(); i++) {
			Object obj = params.get(i);
			if(obj instanceof String) {
				String s = ((String)obj).toLowerCase();
				if(s.equals("left")) {
					hAlign = Horizontal.Left;
				} else if(s.equals("right")) {
					hAlign = Horizontal.Right;
				} else if(s.equals("top")) {
					vAlign = Vertical.Top;
				} else if(s.equals("bottom")) {
					vAlign = Vertical.Bottom;
				} else if(s.equals("center")) {
					if(hAlign == null) {
						hAlign = Horizontal.Center;
					}
					if(vAlign == null) {
						vAlign = Vertical.Center;
					}
				} else if(s.matches("h.*?center")) {
					hAlign = Horizontal.Center;
				} else if(s.matches("v.*?center")) {
					vAlign = Vertical.Center;
				}
			}
		}
	}

	@Override
	public void process(PdfBrewer brewer, Context context) throws IOException {
		if(hAlign != null) {
			context.setHorizontalAlignment(hAlign);
		}
		if(vAlign != null) {
			context.setVerticalAlignment(vAlign);
		}
	}
}
