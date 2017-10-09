package net.osdn.pdf_brewer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.fontbox.ttf.TrueTypeFont;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;

import net.osdn.pdf_brewer.instruction.Instruction;
import net.osdn.pdf_brewer.instruction.LineStyle;

public class Context {
	private static Map<String, PDFont> fonts = new HashMap<String, PDFont>();

	private Context parent;
	private int indent;
	
	private double left;
	private double top;
	private double right;
	private double bottom;
	
	private String fontName;
	private float fontSize;
	private float lineHeight; //行の高さ倍率
	private Horizontal textAlign;
	
	private float lineWidth; //RectやLineで線を描画するときの線の太さ
	private int lineStyle;
	private int capStyle; //線の終端処理
	
	private Horizontal hAlign;
	private Vertical vAlign;
	
	public Context(PDRectangle mediaBox) {
		this.parent = null;
		this.indent = -1;
		
		left = 0;
		right = Instruction.pt2mm(mediaBox.getWidth());
		top = 0;
		bottom = Instruction.pt2mm(mediaBox.getHeight());
		
		fontName = "serif";
		fontSize = 14.0f;
		lineHeight = 1.8f;
		textAlign = Horizontal.Left;
		
		lineWidth = LineStyle.WIDTH_MEDIUM;
		lineStyle = LineStyle.LINE_SOLID;
		capStyle = LineStyle.CAP_DEFAULT;
		
		hAlign = Horizontal.Left;
		vAlign = Vertical.Top;
	}
	
	public Context(Context parent, int index) {
		this.parent = parent;
		this.indent = index;
		
		left = parent.getLeft();
		right = parent.getRight();
		top = parent.getTop();
		bottom = parent.getBottom();
		
		fontName = parent.getFontName();
		fontSize = parent.getFontSize();
		lineHeight = parent.getLineHeight();
		textAlign = parent.getTextAlignment();
		
		lineWidth = parent.getLineWidth();
		lineStyle = parent.getLineStyle();
		capStyle = parent.getCapStyle();
		
		hAlign = parent.getHorizontalAlignment();
		vAlign = parent.getVerticalAlignment();
	}
	
	public Context getParent() {
		return parent;
	}
	
	public int getIndent() {
		return indent;
	}
	
	public double getLeft() {
		return left;
	}
	
	public double getTop() {
		return top;
	}
	
	public double getRight() {
		return right;
	}
	
	public double getBottom() {
		return bottom;
	}
	
	public void setBox(double left, double top, double right, double bottom) {
		this.left = left;
		this.top = top;
		this.right = right;
		this.bottom = bottom;
	}
	
	public Horizontal getHorizontalAlignment() {
		return hAlign;
	}
	
	public void setHorizontalAlignment(Horizontal hAlign) {
		this.hAlign = hAlign;
	}
	
	public Vertical getVerticalAlignment() {
		return vAlign;
	}
	
	public void setVerticalAlignment(Vertical vAlign) {
		this.vAlign = vAlign;
	}
	
	public void setFont(String fontName, float fontSize) throws IOException {
		if(fontName != null) {
			TrueTypeFont ttf = FontLoader.get(fontName);
			if(ttf != null) {
				this.fontName = ttf.getName();
			}
		}
		if(fontSize != Float.NaN && fontSize > 0.0) {
			this.fontSize = fontSize;
		}
	}
	
	public String getFontName() {
		return fontName;
	}
	
	public void setFontName(String fontName) throws IOException {
		TrueTypeFont ttf = FontLoader.get(fontName);
		if(ttf != null) {
			this.fontName = ttf.getName();
		}
	}
	
	public float getFontSize() {
		return fontSize;
	}
	
	public void setFontSize(float fontSize) {
		if(fontSize != Float.NaN && fontSize > 0.0) {
			this.fontSize = fontSize;
		}
	}
	
	public float getLineHeight() {
		return lineHeight;
	}
	
	public void setLineHeight(float lineHeight) {
		this.lineHeight = lineHeight;
	}
	
	public Horizontal getTextAlignment() {
		return textAlign;
	}
	
	public void setTextAlignment(Horizontal textAlign) {
		this.textAlign = textAlign;
	}
	
	public float getLineWidth() {
		return lineWidth;
	}
	
	public void setLineWidth(float lineWidth) {
		this.lineWidth = lineWidth;
	}
	
	public int getLineStyle() {
		return lineStyle;
	}
	
	public void setLineStyle(int lineStyle) {
		this.lineStyle = lineStyle;
	}
	
	public int getCapStyle() {
		return capStyle;
	}
	
	public void setCapStyle(int capStyle) {
		this.capStyle = capStyle;
	}
}
