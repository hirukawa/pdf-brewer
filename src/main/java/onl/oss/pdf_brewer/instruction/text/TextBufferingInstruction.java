package onl.oss.pdf_brewer.instruction.text;

import java.io.IOException;
import java.util.List;

import onl.oss.pdf_brewer.Context;
import onl.oss.pdf_brewer.PdfBrewer;
import onl.oss.pdf_brewer.instruction.Instruction;

public abstract class TextBufferingInstruction extends Instruction {

    public TextBufferingInstruction(int indent, List<Object> params) {
        super(indent, params);
    }

    @Override
    public void process(PdfBrewer brewer, Context context) throws IOException {
        throw new UnsupportedOperationException();
    }
}
