package onl.oss.pdf_brewer.instruction;

import java.io.IOException;
import java.util.List;

import org.apache.pdfbox.pdmodel.common.PDRectangle;

import onl.oss.pdf_brewer.Context;
import onl.oss.pdf_brewer.PdfBrewer;

public class Media extends Instruction {

    private PDRectangle mediaBox;

    public Media(int indent, List<Object> params) {
        super(indent, params);

        for (int i = 0; i < params.size(); i++) {
            Object obj = params.get(i);
            if (obj instanceof String) {
                String s = (String) obj;
                mediaBox = parse(s);
            }
        }

        // 定形用紙サイズが指定されず、数値パラメーターが2つ指定されている場合は、
        // それを横と縦のサイズ（単位はミリメートル）とします。
        if (mediaBox == null && params.size() == 2
                && (params.get(0) instanceof Number) && (params.get(1) instanceof Number)) {
            double width = ((Number) params.get(0)).doubleValue();
            double height = ((Number) params.get(1)).doubleValue();
            mediaBox = new PDRectangle(mm2pt(width), mm2pt(height));
        }
    }

    public PDRectangle getRectangle() {
        return mediaBox;
    }

    @Override
    public void process(PdfBrewer brewer, Context context) throws IOException {
    }

    public static PDRectangle parse(String s) {
        if (s == null) {
            return null;
        }
        s = s.toUpperCase().trim();
        if (s.equals("LETTER")) {
            return PDRectangle.LETTER;
        } else if (s.equals("LEGAL")) {
            return PDRectangle.LEGAL;
        } else if (s.equals("A0")) {
            return PDRectangle.A0;
        } else if (s.equals("A1")) {
            return PDRectangle.A1;
        } else if (s.equals("A2")) {
            return PDRectangle.A2;
        } else if (s.equals("A3")) {
            return PDRectangle.A3;
        } else if (s.equals("A4")) {
            return PDRectangle.A4;
        } else if (s.equals("A5")) {
            return PDRectangle.A5;
        } else if (s.equals("A6")) {
            return PDRectangle.A6;
        } else {
            return null;
        }
    }
}
