package onl.oss.pdf_brewer.instruction;

import java.io.IOException;
import java.util.List;

import onl.oss.pdf_brewer.Context;
import onl.oss.pdf_brewer.PdfBrewer;

public class NewPage extends Instruction {

    public NewPage(int indent, List<Object> params) {
        super(indent, params);
    }

    @Override
    public void process(PdfBrewer brewer, Context context) throws IOException {
        brewer.newPage();
    }
}
