package onl.oss.pdf_brewer.instruction.text;

import java.util.List;

public class LineHeight extends TextBufferingInstruction {

    private float lineHeight = Float.NaN;

    public LineHeight(int indent, List<Object> params) {
        super(indent, params);

        for (int i = 0; i < params.size(); i++) {
            Object obj = params.get(i);
            if (obj instanceof Number) {
                lineHeight = ((Number) obj).floatValue();
            }
        }
    }

    public float getLineHeight() {
        return lineHeight;
    }
}
