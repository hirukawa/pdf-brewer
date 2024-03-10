package onl.oss.pdf_brewer.instruction;

import java.io.IOException;
import java.util.List;

import onl.oss.pdf_brewer.Context;
import onl.oss.pdf_brewer.PdfBrewer;
import org.apache.pdfbox.pdmodel.PDPageContentStream;

public class Rect extends Instruction {

    private double p1;
    private double p2;
    private double p3;
    private double p4;

    public Rect(int indent, List<Object> params) {
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

        double x;
        double y;
        double w;
        double h;

        if (p1 >= 0.0) {
            x = context.getLeft() + p1;
        } else {
            x = context.getRight() + p1;
        }
        if (p2 >= 0.0) {
            y = context.getTop() + p2;
        } else {
            y = context.getBottom() + p2;
        }
        if (p3 > 0.0) {
            w = p3;
        } else if (p1 < 0.0) {
            w = p3;
        } else {
            w = (context.getRight() + p3) - x;
        }
        if (p4 > 0.0) {
            h = p4;
        } else if (p2 < 0.0) {
            h = p4;
        } else {
            h = (context.getBottom() + p4) - y;
        }

        float ptX = mm2pt(x);
        float ptY = pageHeight - mm2pt(y);
        float ptW = mm2pt(w);
        float ptH = mm2pt(-h);

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
            stream.setLineDashPattern(new float[]{0f, lw * 2.5f}, 0f);
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
        stream.addRect(ptX, ptY, ptW, ptH);
        stream.stroke();

        //System.out.println("Rect: x=" + x + ", y=" + y + ", width=" + w + ", height=" + h);
    }
}
