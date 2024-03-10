package onl.oss.pdf_brewer.instruction;

import java.io.IOException;
import java.util.List;

import onl.oss.pdf_brewer.Context;
import onl.oss.pdf_brewer.PdfBrewer;
import org.apache.pdfbox.pdmodel.PDPageContentStream;

public class Line extends Instruction {

    private double p1;
    private double p2;
    private double p3;
    private double p4;

    public Line(int indent, List<Object> params) {
        super(indent, params);

        if (params.size() != 4) {
            throw new IllegalArgumentException();
        }
        if (!(params.get(0) instanceof Number)) {
            throw new IllegalArgumentException();
        }
        if (!(params.get(1) instanceof Number)) {
            throw new IllegalArgumentException();
        }
        if (!(params.get(2) instanceof Number)) {
            throw new IllegalArgumentException();
        }
        if (!(params.get(3) instanceof Number)) {
            throw new IllegalArgumentException();
        }

        p1 = ((Number) params.get(0)).doubleValue();
        p2 = ((Number) params.get(1)).doubleValue();
        p3 = ((Number) params.get(2)).doubleValue();
        p4 = ((Number) params.get(3)).doubleValue();
    }

    @Override
    public void process(PdfBrewer brewer, Context context) throws IOException {
        float pageHeight = brewer.getPage().getMediaBox().getHeight();

        double x1;
        double y1;
        double x2;
        double y2;

        if (p1 >= 0.0) {
            x1 = context.getLeft() + p1;
        } else {
            x1 = context.getRight() + p1;
        }
        if (p2 >= 0.0) {
            y1 = context.getTop() + p2;
        } else {
            y1 = context.getBottom() + p2;
        }
        if (p3 >= 0.0) {
            x2 = context.getLeft() + p3;
        } else {
            x2 = context.getRight() + p3;
        }
        if (p4 >= 0.0) {
            y2 = context.getTop() + p4;
        } else {
            y2 = context.getBottom() + p4;
        }

        float ptX1 = mm2pt(x1);
        float ptY1 = pageHeight - mm2pt(y1);
        float ptX2 = mm2pt(x2);
        float ptY2 = pageHeight - mm2pt(y2);

        PDPageContentStream stream = brewer.getContentStream();
        float lw = context.getLineWidth();
        stream.setLineWidth(lw);

        int capStyle = context.getCapStyle();
        if (capStyle != LineStyle.CAP_BUTT && capStyle != LineStyle.CAP_ROUND && capStyle != LineStyle.CAP_PROJECTING_SQUARE) {
            capStyle = LineStyle.CAP_DEFAULT;
        }
        int lineStyle = context.getLineStyle();
        if (lineStyle == LineStyle.LINE_DOTTED) {
            if (capStyle == LineStyle.CAP_DEFAULT) {
                capStyle = LineStyle.CAP_ROUND;
            }
            stream.setLineCapStyle(capStyle);
            stream.setLineDashPattern(new float[]{0f, lw * 3f}, 0f);
        } else if (lineStyle == LineStyle.LINE_DASHED) {
            if (capStyle == LineStyle.CAP_DEFAULT) {
                capStyle = LineStyle.CAP_BUTT;
            }
            stream.setLineCapStyle(capStyle);
            stream.setLineDashPattern(new float[]{lw * 3f, lw * 3f}, 0f);
        } else {
            if (capStyle == LineStyle.CAP_DEFAULT) {
                capStyle = LineStyle.CAP_PROJECTING_SQUARE;
            }
            stream.setLineCapStyle(capStyle);
            stream.setLineDashPattern(new float[]{lw, 0f}, 0f);
        }
        stream.moveTo(ptX1, ptY1);
        stream.lineTo(ptX2, ptY2);
        stream.stroke();
    }
}
