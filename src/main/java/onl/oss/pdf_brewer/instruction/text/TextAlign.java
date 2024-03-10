package onl.oss.pdf_brewer.instruction.text;

import java.util.List;

import onl.oss.pdf_brewer.Horizontal;

public class TextAlign extends TextBufferingInstruction {

    private Horizontal textAlign;

    public TextAlign(int indent, List<Object> params) {
        super(indent, params);

        for (int i = 0; i < params.size(); i++) {
            Object obj = params.get(i);
            if (obj instanceof String) {
                String s = ((String) obj).toLowerCase();
                if (s.equals("left")) {
                    textAlign = Horizontal.Left;
                } else if (s.equals("right")) {
                    textAlign = Horizontal.Right;
                } else if (s.matches(".*?center")) {
                    textAlign = Horizontal.Center;
                }
            }
        }
    }

    public Horizontal getTextAlignment() {
        return textAlign;
    }
}
