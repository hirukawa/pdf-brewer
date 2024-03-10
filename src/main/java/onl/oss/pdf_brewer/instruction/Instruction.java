package onl.oss.pdf_brewer.instruction;

import java.io.IOException;
import java.util.List;

import onl.oss.pdf_brewer.Context;
import onl.oss.pdf_brewer.PdfBrewer;
import org.apache.pdfbox.pdmodel.font.PDFont;

public abstract class Instruction {

    private int indent;
    private List<Object> params;

    public Instruction(int indent, List<Object> params) {
        this.indent = indent;
        this.params = params;
    }

    public int getIndent() {
        return indent;
    }

    public abstract void process(PdfBrewer brewer, Context context) throws IOException;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < indent; i++) {
            sb.append("- ");
        }
        sb.append('\\');
        sb.append(getClass().getSimpleName());
        if (params != null) {
            for (Object object : params) {
                sb.append(' ');
                sb.append(object);
            }
        }
        return sb.toString();
    }

    public static float getWidth(PDFont font, float fontSize, String text) throws IOException {
        return font.getStringWidth(text) * fontSize / 1000f;
    }

    public static float getHeight(PDFont font, float fontSize) {
        return font.getFontDescriptor().getCapHeight() * fontSize / 1000f;
    }

    public static double pt2mm(double pt) {
        return pt * 10.0 * 2.54 / 72.0;
    }

    public static float mm2pt(double mm) {
        return (float) (mm * 72 / 2.54 / 10.0);
    }
}
