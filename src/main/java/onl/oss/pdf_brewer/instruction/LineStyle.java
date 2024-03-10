package onl.oss.pdf_brewer.instruction;

import java.io.IOException;
import java.util.List;

import onl.oss.pdf_brewer.Context;
import onl.oss.pdf_brewer.PdfBrewer;

public class LineStyle extends Instruction {

    public static final float WIDTH_THIN = 1f / 3f;
    public static final float WIDTH_MEDIUM = 2f / 3f;
    public static final float WIDTH_THICK = 4f / 3f;

    public static final int LINE_SOLID = 0;
    public static final int LINE_DOTTED = 1;
    public static final int LINE_DASHED = 2;

    public static final int CAP_DEFAULT = -1;
    public static final int CAP_BUTT = 0;
    public static final int CAP_ROUND = 1;
    public static final int CAP_PROJECTING_SQUARE = 2;

    private float lineWidth = Float.NaN;
    private int capStyle = CAP_DEFAULT;
    private int lineStyle = LINE_SOLID;

    public LineStyle(int indent, List<Object> params) {
        super(indent, params);

        for (int i = 0; i < params.size(); i++) {
            Object obj = params.get(i);
            if (obj instanceof Number) {
                lineWidth = ((Number) obj).floatValue();
            } else if (obj instanceof String) {
                String s = ((String) obj).toLowerCase();
                if (s.equals("thin")) {
                    lineWidth = LineStyle.WIDTH_THIN;
                } else if (s.equals("medium")) {
                    lineWidth = LineStyle.WIDTH_MEDIUM;
                } else if (s.equals("thick")) {
                    lineWidth = LineStyle.WIDTH_THICK;
                } else if (s.contains("butt")) {
                    capStyle = CAP_BUTT;
                } else if (s.contains("round")) {
                    capStyle = CAP_ROUND;
                } else if (s.contains("project") || s.contains("square")) {
                    capStyle = CAP_PROJECTING_SQUARE;
                } else if (s.contains("solid")) {
                    lineStyle = LINE_SOLID;
                } else if (s.contains("dot")) {
                    lineStyle = LINE_DOTTED;
                } else if (s.contains("dash")) {
                    lineStyle = LINE_DASHED;
                }
            }
        }
    }

    @Override
    public void process(PdfBrewer brewer, Context context) throws IOException {
        if (lineWidth != Float.NaN && lineWidth >= 0.0) {
            context.setLineWidth(lineWidth);
        }
        if (lineStyle == LINE_SOLID || lineStyle == LINE_DOTTED || lineStyle == LINE_DASHED) {
            context.setLineStyle(lineStyle);
        }
        if (capStyle == CAP_BUTT || capStyle == CAP_ROUND || capStyle == CAP_PROJECTING_SQUARE) {
            context.setCapStyle(capStyle);
        } else {
            context.setCapStyle(CAP_DEFAULT);
        }
    }
}
