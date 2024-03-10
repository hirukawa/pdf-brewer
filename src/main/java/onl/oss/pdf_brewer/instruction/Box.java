package onl.oss.pdf_brewer.instruction;

import java.util.List;

import onl.oss.pdf_brewer.Context;
import onl.oss.pdf_brewer.PdfBrewer;

public class Box extends Instruction {

    private double p1;
    private double p2;
    private double p3;
    private double p4;

    public Box(int indent, List<Object> params) {
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

    public void process(PdfBrewer brewer, Context context) {
        Context parent = context.getParent();
        double left;
        double top;
        double right;
        double bottom;

        if (p1 >= 0.0) {
            left = parent.getLeft() + p1;
        } else {
            left = parent.getRight() + p1;
        }
        if (p2 >= 0.0) {
            top = parent.getTop() + p2;
        } else {
            top = parent.getBottom() + p2;
        }
        if (p3 > 0.0) {
            right = left + p3;
        } else if (p1 < 0.0) {
            right = left;
            left = left + p3;
        } else {
            right = parent.getRight() + p3;
        }
        if (p4 > 0.0) {
            bottom = top + p4;
        } else if (p2 < 0.0) {
            bottom = top;
            top = top + p4;
        } else {
            bottom = parent.getBottom() + p4;
        }
        context.setBox(left, top, right, bottom);

        //System.out.println("Box: left=" + left + ", top=" + top + ", right=" + right + ", bottom=" + bottom + ", width=" + (right - left) + ", height=" + (bottom - top));
    }
}
