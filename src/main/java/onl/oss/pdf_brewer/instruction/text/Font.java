package onl.oss.pdf_brewer.instruction.text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Font extends TextBufferingInstruction {

    private String fontFamily;
    private String fontSubFamily;
    private float fontSize = Float.NaN;

    public Font(int indent, List<Object> params) {
        super(indent, params);

        List<String> strings = new ArrayList<>();

        for (int i = 0; i < params.size(); i++) {
            Object obj = params.get(i);
            if (obj instanceof Number) {
                fontSize = ((Number) obj).floatValue();
            } else if (obj instanceof String) {
                String s = (String) obj;
                s = s.trim();
                if (s.length() >= 2) {
                    if (s.charAt(0) == '"' && s.charAt(s.length() - 1) == '"') {
                        s = s.substring(1, s.length() - 2);
                    } else if (s.charAt(0) == '\'' && s.charAt(s.length() - 1) == '\'') {
                        s = s.substring(1, s.length() - 2);
                    }
                }
                strings.add(s);
            }
        }

        if (strings.size() > 0) {
            fontFamily = strings.get(0);
        }

        if (strings.size() > 1) {
            fontSubFamily = strings.get(1);
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
}
