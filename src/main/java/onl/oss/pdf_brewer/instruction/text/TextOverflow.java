package onl.oss.pdf_brewer.instruction.text;

import onl.oss.pdf_brewer.Overflow;

import java.util.List;

public class TextOverflow extends TextBufferingInstruction {

    private Overflow textOverflow;

    public TextOverflow(int indent, List<Object> params) {
        super(indent, params);

        for (int i = 0; i < params.size(); i++) {
            Object obj = params.get(i);
            if (obj instanceof String) {
                String s = ((String) obj).toLowerCase();
                if (s.equals("wrap")) {
                    textOverflow = Overflow.Wrap;
                } else if (s.equals("truncate")) {
                    textOverflow = Overflow.Truncate;
                } else if (s.equals("ellipsis")) {
                    textOverflow = Overflow.Ellipsis;
                }
            }
        }
    }

    public Overflow getTextOverflow() {
        return textOverflow;
    }
}
