package onl.oss.pdf_brewer;

import onl.oss.pdf_brewer.instruction.LineStyle;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

import onl.oss.pdf_brewer.instruction.Instruction;

public class Context {
    private Context parent;
    private int indent;

    private double left;
    private double top;
    private double right;
    private double bottom;

    private FontLoader fontLoader;
    private String fontFamily;
    private String fontSubFamily;
    private float fontSize;
    private float lineHeight; //行の高さ倍率
    private Horizontal textAlign;
    private Overflow textOverflow;

    private float lineWidth; //RectやLineで線を描画するときの線の太さ
    private int lineStyle;
    private int capStyle; //線の終端処理

    private Horizontal hAlign;
    private Vertical vAlign;

    public Context(FontLoader loader, PDRectangle mediaBox) {
        this.parent = null;
        this.indent = -1;

        left = 0;
        right = Instruction.pt2mm(mediaBox.getWidth());
        top = 0;
        bottom = Instruction.pt2mm(mediaBox.getHeight());

        fontLoader = loader;
        fontFamily = "serif";
        fontSubFamily = null;
        fontSize = 14.0f;
        lineHeight = 1.8f;
        textAlign = Horizontal.Left;
        textOverflow = Overflow.Wrap;

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

        fontLoader = parent.getFontLoader();
        fontFamily = parent.getFontFamily();
        fontSubFamily = parent.getFontSubFamily();
        fontSize = parent.getFontSize();
        lineHeight = parent.getLineHeight();
        textAlign = parent.getTextAlignment();
        textOverflow = parent.getTextOverflow();

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

    public FontLoader getFontLoader() {
        return fontLoader;
    }

    public void setFont(String fontFamily, String fontSubFamily, float fontSize) {
        this.fontFamily = fontFamily;
        this.fontSubFamily = fontSubFamily;

        if (fontSize != Float.NaN && fontSize > 0.0) {
            this.fontSize = fontSize;
        }
    }

    public String getFontFamily() {
        return fontFamily;
    }

    public String getFontSubFamily() {
        return fontSubFamily;
    }

    public float getFontSize() {
        return fontSize;
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

    public Overflow getTextOverflow() {
        return textOverflow;
    }

    public void setTextOverflow(Overflow textOverflow) {
        this.textOverflow = textOverflow;
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
