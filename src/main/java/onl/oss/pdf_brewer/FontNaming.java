package onl.oss.pdf_brewer;

public class FontNaming {

    private final String family;
    private final String subFamily;

    public FontNaming(String family) {
        this.family = family != null ? family : "";
        this.subFamily = "";
    }

    public FontNaming(String family, String subFamily) {
        this.family = family != null ? family : "";
        this.subFamily = subFamily != null ? subFamily : "";
    }

    public String getFamily() {
        return family;
    }

    public String getSubFamily() {
        return subFamily;
    }
}
