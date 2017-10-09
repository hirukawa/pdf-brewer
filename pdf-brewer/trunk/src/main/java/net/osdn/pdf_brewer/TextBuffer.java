package net.osdn.pdf_brewer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;

import net.osdn.pdf_brewer.instruction.Instruction;
import net.osdn.pdf_brewer.instruction.text.Font;
import net.osdn.pdf_brewer.instruction.text.LineHeight;
import net.osdn.pdf_brewer.instruction.text.Text;
import net.osdn.pdf_brewer.instruction.text.TextAlign;
import net.osdn.pdf_brewer.instruction.text.TextBufferingInstruction;

public class TextBuffer {
	
	private List<Op> ops = new ArrayList<Op>();
	
	public void add(TextBufferingInstruction instruction) throws IOException {
		if(instruction instanceof Text) {
			Text textInstruction = (Text)instruction;
			List<Op> list = split(textInstruction.getText());

			if(list.get(0) instanceof TextOp) {
				Op last = null;
				if(ops.size() > 0) {
					last = ops.get(ops.size() - 1);
				}
				if(last instanceof TextOp) {
					TextOp op = (TextOp)list.remove(0);
					((TextOp)last).text = ((TextOp)last).text + op.text;
				}
			}
			for(Op op : list) {
				ops.add(op);
			}
		} else if(instruction instanceof Font) {
			Font fontInstruction = (Font)instruction;
			String fontName = fontInstruction.getFontName();
			float fontSize = fontInstruction.getFontSize();
			ops.add(new FontOp(fontName, fontSize));
		} else if(instruction instanceof LineHeight) {
			LineHeight lineHeightInstruction = (LineHeight)instruction;
			if(lineHeightInstruction.getLineHeight() != Float.NaN && lineHeightInstruction.getLineHeight() >= 0.0) {
				ops.add(new LineHeightOp(lineHeightInstruction.getLineHeight()));
			}
		} else if(instruction instanceof TextAlign) {
			TextAlign textAlignInstruction = (TextAlign)instruction;
			if(textAlignInstruction.getTextAlignment() != null) {
				ops.add(new TextAlignOp(textAlignInstruction.getTextAlignment()));
			}
		} else {
			
		}
	}
	
	public boolean isEmpty() {
		return ops.isEmpty();
	}
	
	public void clear() {
		ops.clear();
	}
	
	public void process(PdfBrewer brewer, Context context) throws IOException {
		PDFont font = brewer.loadFont(context.getFontName());
		Horizontal textAlign = context.getTextAlignment();
		if(textAlign == null) {
			textAlign = Horizontal.Left;
		}
		float fontSize = context.getFontSize();
		float lineHeight = context.getLineHeight();
		boolean isHeightChanged = true;
		
		float maxWidth = Instruction.mm2pt(context.getRight() - context.getLeft());
		float rest = maxWidth;
		
		int lineNumber = 0;
		float[] lineWidth = new float[1024];
		float[] fontHeight = new float[1024];
		float[] leading = new float[1024];
		
		List<Op> ops2 = new ArrayList<Op>();
		for(int i = 0; i < ops.size(); i++) {
			Op op = ops.get(i);
			if(op instanceof TextOp) {
				TextOp textOp = (TextOp)op;
				String text = textOp.text;
				while(text != null) {
					DivideResult result = divide(rest, font, fontSize, text);
					if(result.text1 == null) {
						// 1文字も出力できない。改行が必要。
						float w = lineWidth[lineNumber];
						float h = lineNumber == 0 ? fontHeight[0] : leading[lineNumber];
						ops2.add(new NewLineOp(w, h));
						lineNumber++;
						isHeightChanged = true;
						rest = maxWidth;
					} else {
						// テキストの一部または全部を出力可能。
						if(isHeightChanged) {
							float fh = getFontHeight(font, fontSize);
							if(fh > fontHeight[lineNumber]) {
								fontHeight[lineNumber] = fh;
							}
							float l = fh * lineHeight;
							if(l > leading[lineNumber]) {
								leading[lineNumber] = l;
							}
						}
						ops2.add(new TextOp(result.text1));
						lineWidth[lineNumber] += result.width;
						rest -= result.width;
						if(result.text2 == null) {
							// 改行の必要なくすべて出力可能。
							text = null;
						} else {
							// テキストの一部を出力可能。残りを出力するために改行が必要。
							float w = lineWidth[lineNumber];
							float h = lineNumber == 0 ? fontHeight[0] : leading[lineNumber];
							ops2.add(new NewLineOp(w, h));
							lineNumber++;
							isHeightChanged = true;
							rest = maxWidth;
							text = result.text2;
						}
					}
				}
			} else if(op instanceof NewLineOp) {
				float w = lineWidth[lineNumber];
				float h = lineNumber == 0 ? fontHeight[0] : leading[lineNumber];
				ops2.add(new NewLineOp(w, h));
				lineNumber++;
				isHeightChanged = true;
				rest = maxWidth;
			} else if(op instanceof FontOp) {
				FontOp fontOp = (FontOp)op;
				if(fontOp.fontName != null) {
					fontOp.font = brewer.loadFont(fontOp.fontName);
					if(fontOp.font != null) {
						font = fontOp.font;
					}
				}
				if(fontOp.fontSize > 0.0) {
					fontSize = fontOp.fontSize;
				}
				ops2.add(fontOp);
				isHeightChanged = true;
			} else if(op instanceof TextAlignOp) {
				TextAlignOp textAlignOp = (TextAlignOp)op;
				if(textAlignOp.textAlign != null) {
					if(textAlign != textAlignOp.textAlign) {
						textAlign = textAlignOp.textAlign;
						if(lineWidth[lineNumber] > 0f) {
							float w = lineWidth[lineNumber];
							float h = lineNumber == 0 ? fontHeight[0] : leading[lineNumber];
							ops2.add(new NewLineOp(w, h));
							lineNumber++;
							isHeightChanged = true;
							rest = maxWidth;
						}
					}
					ops2.add(textAlignOp);
				}
			} else if(op instanceof LineHeightOp) {
				LineHeightOp lineHeightOp = (LineHeightOp)op;
				if(lineHeightOp.lineHeight != Float.NaN && lineHeightOp.lineHeight >= 0.0) {
					lineHeight = lineHeightOp.lineHeight;
					float fh = getFontHeight(font, fontSize);
					leading[lineNumber] = fh * lineHeight;
					context.setLineHeight(lineHeight);
				}
			}
		}
		if(lineWidth[lineNumber] > 0f) {
			float w = lineWidth[lineNumber];
			float h = lineNumber == 0 ? fontHeight[0] : leading[lineNumber];
			ops2.add(new NewLineOp(w, h));
		}
		
		textAlign = context.getTextAlignment();
		if(textAlign == null) {
			textAlign = Horizontal.Left;
		}
		float width = 0f;
		float height = 0f;
		NewLineOp prev = new NewLineOp(0f, 0f);
		ops2.add(0, prev);
		for(Op op: ops2) {
			if(op instanceof NewLineOp) {
				NewLineOp n = (NewLineOp)op;
				if(prev != n) {
					if(n.width > width) {
						width = n.width;
					}
					height += n.height;
					prev.width = n.width;
					prev.height = n.height;
					n.width = 0f;
					n.height = 0f;
				}
				n.textAlign = textAlign;
				prev = n;
			} else if(op instanceof TextAlignOp) {
				textAlign = ((TextAlignOp)op).textAlign;
				prev.textAlign = textAlign;
			}
		}
		
		float pageHeight = brewer.getPage().getMediaBox().getHeight();
		float ptX;
		Horizontal hAlign = context.getHorizontalAlignment();
		if(hAlign == Horizontal.Right) {
			ptX = Instruction.mm2pt(context.getRight()) - width;
		} else if(hAlign == Horizontal.Center) {
			ptX = Instruction.mm2pt(context.getLeft()) + (Instruction.mm2pt(context.getRight() - context.getLeft()) - width) / 2f;
		} else {
			ptX = Instruction.mm2pt(context.getLeft());			
		}
		float ptY;
		Vertical vAlign = context.getVerticalAlignment();
		if(vAlign == Vertical.Bottom) {
			ptY = pageHeight - Instruction.mm2pt(context.getBottom()) + height;
		} else if(vAlign == Vertical.Center) {
			ptY = pageHeight - Instruction.mm2pt(context.getTop()) - (Instruction.mm2pt(context.getBottom() - context.getTop()) - height) / 2f;
		} else {
			ptY = pageHeight - Instruction.mm2pt(context.getTop());
		}
		float prevNewLineX = 0f;
		
		PDPageContentStream stream = brewer.getContentStream();
		stream.beginText();
		stream.newLineAtOffset(ptX, ptY);
		font = brewer.loadFont(context.getFontName());
		fontSize = context.getFontSize();
		if(font != null && fontSize != Float.NaN && fontSize > 0.0) {
			stream.setFont(font, fontSize);
		}
		for(Op op : ops2) {
			if(op instanceof NewLineOp) {
				NewLineOp newLineOp = (NewLineOp)op;
				stream.newLineAtOffset(-prevNewLineX, -newLineOp.height);
				float x;
				if(newLineOp.textAlign == Horizontal.Right) {
					x = maxWidth - newLineOp.width;
				} else if(newLineOp.textAlign == Horizontal.Center) {
					x = (maxWidth - newLineOp.width) / 2f;
				} else {
					x = 0f;
				}
				stream.newLineAtOffset(x, 0f);
				prevNewLineX = x;
			} else if(op instanceof TextOp) {
				TextOp textOp = (TextOp)op;
				stream.showText(textOp.text);
			} else if(op instanceof FontOp) {
				FontOp fontOp = (FontOp)op;
				if(fontOp.font != null) {
					font = fontOp.font;
				}
				if(fontOp.fontSize != Float.NaN && fontOp.fontSize > 0.0) {
					fontSize = fontOp.fontSize;
				}
				if(font != null && fontSize != Float.NaN && fontSize > 0.0) {
					stream.setFont(font, fontOp.fontSize);
					context.setFont(fontOp.fontName, fontOp.fontSize);
				}
			} else if(op instanceof TextAlignOp) {
				TextAlignOp textAlignOp = (TextAlignOp)op;
				context.setTextAlignment(textAlignOp.textAlign);
			}
		}
		stream.endText();
	}
	
	public static List<Op> split(String text) {
		List<Op> ops = new ArrayList<Op>();
		int from = 0;
		int to = 0;
		while((to = text.indexOf('\n', from)) >= 0) {
			if(to > from) {
				ops.add(new TextOp(text.substring(from, to)));
			}
			ops.add(new NewLineOp(0f, 0f));
			from = to + 1;
		}
		if(from < text.length()) {
			ops.add(new TextOp(text.substring(from)));
		}
		return ops;
	}
	
	public static DivideResult divide(float maxWidth, PDFont font, float fontSize, String text) throws IOException {
		float width = getStringWidth(font, fontSize, text);
		if(width <= maxWidth) {
			return new DivideResult(width, text, null);
		}
		int s = 0;
		int p = text.length() / 2;
		int e = text.length();
		for(;;) {
			width = getStringWidth(font, fontSize, text.substring(0, p));
			if(width == maxWidth) {
				break;
			} else if(width < maxWidth) {
				s = p;
			} else {
				e = p;
			}
			if(p == (e + s) / 2) {
				break;
			}
			p = (e + s) / 2;
		}
		String text1 = text.substring(0, p);
		String text2 = text.substring(p);
		if(text1 != null && text1.length() == 0) {
			text1 = null;
		}
		if(text2 != null && text2.length() == 0) {
			text2 = null;
		}
		return new DivideResult(width, text1, text2);
	}
	
	public static float getStringWidth(PDFont font, float fontSize, String text) throws IOException {
		return font.getStringWidth(text) * fontSize / 1000f;
	}
	
	public static float getFontHeight(PDFont font, float fontSize) {
		return font.getFontDescriptor().getCapHeight() * fontSize / 1000f;
	}
	
	private static class DivideResult {
		public float width;
		public String text1;
		public String text2;
		
		public DivideResult(float width, String text1, String text2) {
			this.width = width;
			this.text1 = text1;
			this.text2 = text2;
		}
	}
	
	private static class Op {
	}
	
	private static class NewLineOp extends Op {
		public float width;
		public float height;
		public Horizontal textAlign;
		
		public NewLineOp(float width, float height) {
			this.width = width;
			this.height = height;
		}
	}
	
	private static class TextOp extends Op {
		public String text;
		
		public TextOp(String text) {
			this.text = text;
		}
	}
	
	private static class FontOp extends Op {
		public String fontName;
		public float fontSize;
		public PDFont font;
		
		public FontOp(String fontName, float fontSize) {
			this.fontName = fontName;
			this.fontSize = fontSize;
		}
	}
	
	private static class TextAlignOp extends Op {
		public Horizontal textAlign;
		
		public TextAlignOp(Horizontal textAlign) {
			this.textAlign = textAlign;
		}
	}
	
	private static class LineHeightOp extends Op {
		public float lineHeight;
		
		public LineHeightOp(float lineHeight) {
			this.lineHeight = lineHeight;
		}
	}
}
