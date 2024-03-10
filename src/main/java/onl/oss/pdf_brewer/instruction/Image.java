package onl.oss.pdf_brewer.instruction;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import onl.oss.pdf_brewer.Context;
import onl.oss.pdf_brewer.Horizontal;
import onl.oss.pdf_brewer.PdfBrewer;
import onl.oss.pdf_brewer.Vertical;

public class Image extends Instruction {

    private File file;
    private float scale = 1f;

    public Image(int indent, List<Object> params) {
        super(indent, params);

        for (int i = 0; i < params.size(); i++) {
            Object obj = params.get(i);
            if (obj instanceof Number) {
                scale = ((Number) obj).floatValue();
            } else if (obj instanceof String) {
                String s = (String) obj;
                File file = new File(s);
                if (file.exists()) {
                    this.file = file;
                }
            }
        }
    }

    @Override
    public void process(PdfBrewer brewer, Context context) throws IOException {
        if (file == null) {
            return;
        }

        float pageHeight = brewer.getPage().getMediaBox().getHeight();
        PDDocument document = brewer.getDocument();
        PDImageXObject image = PDImageXObject.createFromFileByContent(file, document);
        float imageWidth = image.getWidth() * scale;
        float imageHeight = image.getHeight() * scale;
        Horizontal hAlign = context.getHorizontalAlignment();
        Vertical vAlign = context.getVerticalAlignment();

        float ptLeft = mm2pt(context.getLeft());
        float ptTop = mm2pt(context.getTop());
        float ptRight = mm2pt(context.getRight());
        float ptBottom = mm2pt(context.getBottom());

        float ptX;
        float ptY;

        if (hAlign == Horizontal.Right) {
            ptX = ptRight - imageWidth;
        } else if (hAlign == Horizontal.Center) {
            ptX = ptLeft + (ptRight - ptLeft - imageWidth) / 2f;
        } else {
            ptX = ptLeft;
        }
        if (vAlign == Vertical.Bottom) {
            ptY = ptBottom - imageHeight;
        } else if (vAlign == Vertical.Center) {
            ptY = ptTop + (ptBottom - ptTop - imageHeight) / 2f;
        } else {
            ptY = ptTop;
        }

        PDPageContentStream stream = brewer.getContentStream();
        stream.drawImage(image, ptX, pageHeight - ptY - imageHeight, imageWidth, imageHeight);
    }
}
