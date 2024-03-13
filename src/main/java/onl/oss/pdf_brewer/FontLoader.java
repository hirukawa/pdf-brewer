package onl.oss.pdf_brewer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.fontbox.ttf.NamingTable;
import org.apache.fontbox.ttf.TTFParser;
import org.apache.fontbox.ttf.TrueTypeCollection;
import org.apache.fontbox.ttf.TrueTypeFont;

public class FontLoader {

    public static final String FONT_FAMILY_SERIF = "serif";
    public static final String FONT_FAMILY_SANS_SERIF = "sans-serif";

    private static final FontNaming[] DEFAULT_SERIF = new FontNaming[] {
            new FontNaming("Yu Mincho", FontSubFamily.REGULAR),

            new FontNaming("Noto Serif JP", FontSubFamily.REGULAR),
            new FontNaming("Noto Serif JP Light"),
            new FontNaming("Noto Serif JP ExtraLight"),
            new FontNaming("Noto Serif JP Medium"),
            new FontNaming("Noto Serif JP SemiBold"),
            new FontNaming("Noto Serif JP", FontSubFamily.BOLD),
            new FontNaming("Noto Serif JP ExtraBold"),
            new FontNaming("Noto Serif JP Black"),

            new FontNaming("Noto Serif", FontSubFamily.REGULAR)
    };

    private static final FontNaming[] DEFAULT_SERIF_BOLD = new FontNaming[] {
            new FontNaming("Yu Mincho", FontSubFamily.BOLD),
            new FontNaming("Yu Mincho Demibold"),

            new FontNaming("Noto Serif JP", FontSubFamily.BOLD),
            new FontNaming("Noto Serif JP SemiBold"),
            new FontNaming("Noto Serif JP ExtraBold"),
            new FontNaming("Noto Serif JP Black"),
            new FontNaming("Noto Serif JP Medium"),
            new FontNaming("Noto Serif JP", FontSubFamily.REGULAR),
            new FontNaming("Noto Serif JP Light"),
            new FontNaming("Noto Serif JP ExtraLight"),

            new FontNaming("Noto Serif", FontSubFamily.BOLD)
    };

    private static final FontNaming[] DEFAULT_SANS_SERIF = new FontNaming[] {
            new FontNaming("Yu Gothic", FontSubFamily.REGULAR),

            new FontNaming("Noto Sans JP", FontSubFamily.REGULAR),
            new FontNaming("Noto Sans JP Light"),
            new FontNaming("Noto Sans JP ExtraLight"),
            new FontNaming("Noto Sans JP Thin"),
            new FontNaming("Noto Sans JP Medium"),
            new FontNaming("Noto Sans JP SemiBold"),
            new FontNaming("Noto Sans JP", FontSubFamily.BOLD),
            new FontNaming("Noto Sans JP ExtraBold"),
            new FontNaming("Noto Sans JP Black"),

            new FontNaming("Noto Sans", FontSubFamily.REGULAR)
    };

    private static final FontNaming[] DEFAULT_SANS_SERIF_BOLD = new FontNaming[] {
            new FontNaming("Yu Gothic", FontSubFamily.BOLD),

            new FontNaming("Noto Sans JP", FontSubFamily.BOLD),
            new FontNaming("Noto Sans JP SemiBold"),
            new FontNaming("Noto Sans JP ExtraBold"),
            new FontNaming("Noto Sans JP Black"),
            new FontNaming("Noto Sans JP Medium"),
            new FontNaming("Noto Sans JP", FontSubFamily.REGULAR),
            new FontNaming("Noto Sans JP Light"),
            new FontNaming("Noto Sans JP ExtraLight"),
            new FontNaming("Noto Sans JP Thin"),

            new FontNaming("Noto Sans", FontSubFamily.BOLD)
    };


    private List<Map.Entry<NamingTable, TrueTypeFont>> fontList = new ArrayList<>();
    private Map<String, TrueTypeFont> fontMap = new HashMap<>();

    private List<FontNaming> serif;
    private List<FontNaming> serifBold;
    private List<FontNaming> sansSerif;
    private List<FontNaming> sansSerifBold;

    public FontLoader() {
        this.serif = new ArrayList<>(Arrays.asList(DEFAULT_SERIF));
        this.serifBold = new ArrayList<>(Arrays.asList(DEFAULT_SERIF_BOLD));
        this.sansSerif = new ArrayList<>(Arrays.asList(DEFAULT_SANS_SERIF));
        this.sansSerifBold = new ArrayList<>(Arrays.asList(DEFAULT_SANS_SERIF_BOLD));
    }


    public List<TrueTypeFont> load(Path dir, Collection<String> filenames) {
        List<TrueTypeFont> loaded = new ArrayList<>();
        TTFParser ttfParser = new TTFParser();

        for (String filename : filenames) {
            try {
                Path file = dir.resolve(filename);
                if (!Files.exists(file)) {
                    continue;
                }

                if (filename.toLowerCase().endsWith(".ttc")) {
                    TrueTypeCollection ttc;
                    try (InputStream is = Files.newInputStream(file, StandardOpenOption.READ)) {
                        ttc = new TrueTypeCollection(is);
                    }
                    ttc.processAllFonts(ttf -> {
                        loaded.add(ttf);
                        fontList.add(Map.entry(ttf.getNaming(), ttf));
                    });
                } else if (filename.toLowerCase().endsWith(".ttf")) {
                    TrueTypeFont ttf;
                    try (InputStream is = Files.newInputStream(file, StandardOpenOption.READ)) {
                        ttf = ttfParser.parse(is);
                    }
                    loaded.add(ttf);
                    fontList.add(Map.entry(ttf.getNaming(), ttf));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        fontMap.clear();
        return loaded;
    }

    public List<FontNaming> getSerif() {
        return serif;
    }

    public void setSerif(FontNaming... candidates) {
        serif = new ArrayList<>(Arrays.asList(candidates));
    }

    public List<FontNaming> getSerifBold() {
        return serifBold;
    }

    public void setSerifBold(FontNaming... candidates) {
        serifBold = new ArrayList<>(Arrays.asList(candidates));
    }

    public List<FontNaming> getSansSerif() {
        return sansSerif;
    }

    public void setSansSerif(FontNaming... candidates) {
        sansSerif = new ArrayList<>(Arrays.asList(candidates));
    }

    public List<FontNaming> getSansSerifBold() {
        return sansSerifBold;
    }

    public void setSansSerifBold(FontNaming... candidates) {
        sansSerifBold = new ArrayList<>(Arrays.asList(candidates));
    }

    public List<TrueTypeFont> getFonts() {
        List<TrueTypeFont> list = new ArrayList<>();

        for (Map.Entry<NamingTable, TrueTypeFont> entry : fontList) {
            list.add(entry.getValue());
        }

        return list;
    }

    public TrueTypeFont getFont(String family, String subFamily) {
        if (family == null || family.isBlank()) {
            return null;
        }

        if (family == null) {
            family = "";
        }

        if (subFamily == null || subFamily.isBlank()) {
            subFamily = FontSubFamily.REGULAR;
        }

        String key = (family + "/" + subFamily).toLowerCase();
        TrueTypeFont font = fontMap.get(key);
        if (font != null) {
            return font;
        }

        List<FontNaming> fontNamings = List.of(new FontNaming(family, subFamily));

        if (family.equalsIgnoreCase(FONT_FAMILY_SERIF)) {
            if (serif != null && subFamily.equalsIgnoreCase(FontSubFamily.REGULAR)) {
                fontNamings = serif;
            } else if (serifBold != null && subFamily.equalsIgnoreCase(FontSubFamily.BOLD)) {
                fontNamings = serifBold;
            }
        } else if (family.equalsIgnoreCase(FONT_FAMILY_SANS_SERIF)) {
            if (sansSerif != null && subFamily.equalsIgnoreCase(FontSubFamily.REGULAR)) {
                fontNamings = sansSerif;
            } else if (sansSerifBold != null && subFamily.equalsIgnoreCase(FontSubFamily.BOLD)) {
                fontNamings = sansSerifBold;
            }
        }

        // ファミリー名が完全一致するフォントの候補リストからサブファミリー名が完全一致するフォントを探します。
        if (font == null) {
            for (FontNaming fontNaming : fontNamings) {
                List<Map.Entry<NamingTable, TrueTypeFont>> candidates = getExactMatchFonts(fontNaming.getFamily());
                // サブファミリーが指定されていない場合は候補リストの先頭のフォントを返します。
                if (fontNaming.getSubFamily().isBlank() && candidates.size() >= 1) {
                    font = candidates.get(0).getValue();
                    break;
                }
                font = getExactMatchFont(candidates, fontNaming.getSubFamily());
                if (font != null) {
                    break;
                }
            }
        }

        // ファミリー名が完全一致するフォント候補のリストからサブファミリー名が部分一致するフォントを探します。
        if (font == null) {
            for (FontNaming fontNaming : fontNamings) {
                List<Map.Entry<NamingTable, TrueTypeFont>> candidates = getExactMatchFonts(fontNaming.getFamily());
                // サブファミリーが指定されていない場合は候補リストの先頭のフォントを返します。
                if (fontNaming.getSubFamily().isBlank() && candidates.size() >= 1) {
                    font = candidates.get(0).getValue();
                    break;
                }
                font = getPartialMatchFont(candidates, fontNaming.getSubFamily());
                if (font != null) {
                    break;
                }
            }
        }

        // ファミリー名が部分一致するフォント候補のリストからサブファミリー名が完全一致するフォントを探します。
        if (font == null) {
            for (FontNaming fontNaming : fontNamings) {
                List<Map.Entry<NamingTable, TrueTypeFont>> candidates = getPartialMatchFonts(fontNaming.getFamily());
                // サブファミリーが指定されていない場合は候補リストの先頭のフォントを返します。
                if (fontNaming.getSubFamily().isBlank() && candidates.size() >= 1) {
                    font = candidates.get(0).getValue();
                    break;
                }
                font = getExactMatchFont(candidates, fontNaming.getSubFamily());
                if (font != null) {
                    break;
                }
            }
        }

        // ファミリー名が部分一致するフォント候補のリストからサブファミリー名が部分一致するフォントを探します。
        if (font == null) {
            for (FontNaming fontNaming : fontNamings) {
                List<Map.Entry<NamingTable, TrueTypeFont>> candidates = getPartialMatchFonts(fontNaming.getFamily());
                // サブファミリーが指定されていない場合は候補リストの先頭のフォントを返します。
                if (fontNaming.getSubFamily().isBlank() && candidates.size() >= 1) {
                    font = candidates.get(0).getValue();
                    break;
                }
                font = getPartialMatchFont(candidates, fontNaming.getSubFamily());
                if (font != null) {
                    break;
                }
            }
        }

        // ファミリー名が完全一致するフォント候補のリストから先頭のフォントを探します。（サブファミリー名は問いません）
        if (font == null) {
            for (FontNaming fontNaming : fontNamings) {
                List<Map.Entry<NamingTable, TrueTypeFont>> candidates = getExactMatchFonts(fontNaming.getFamily());
                if (candidates.size() >= 1) {
                    font = candidates.get(0).getValue();
                    break;
                }
            }
        }

        // ファミリー名が部分一致するフォント候補のリストから先頭のフォントを探します。（サブファミリー名は問いません）
        if (font == null) {
            for (FontNaming fontNaming : fontNamings) {
                List<Map.Entry<NamingTable, TrueTypeFont>> candidates = getPartialMatchFonts(fontNaming.getFamily());
                if (candidates.size() >= 1) {
                    font = candidates.get(0).getValue();
                    break;
                }
            }
        }

        if (font != null) {
            fontMap.put(key, font);
        }

        return font;
    }

    /** ファミリー名が完全一致するフォントの候補リストを返します。
     *
     * @param fontFamily 検索するファミリー名
     * @return ファミリー名が完全一致するフォントの候補リスト
     */
    private List<Map.Entry<NamingTable, TrueTypeFont>> getExactMatchFonts(String fontFamily) {
        List<Map.Entry<NamingTable, TrueTypeFont>> candidates = new ArrayList<>();

        for (Map.Entry<NamingTable, TrueTypeFont> entry : fontList) {
            if (fontFamily.equalsIgnoreCase(entry.getKey().getFontFamily())) {
                candidates.add(entry);
            }
        }

        return candidates;
    }

    /** ファミリー名が部分一致するフォントの候補リストを返します。
     *
     * @param fontFamily 検索するファミリー名
     * @return ファミリー名が部分一致するフォントの候補リスト
     */
    private List<Map.Entry<NamingTable, TrueTypeFont>> getPartialMatchFonts(String fontFamily) {
        List<Map.Entry<NamingTable, TrueTypeFont>> candidates = new ArrayList<>();

        List<String> tokens = new ArrayList<>();
        for (String token : fontFamily.split("[\\s-]")) {
            if (!token.isBlank()) {
                tokens.add(token.toLowerCase().trim());
            }
        }

        for (Map.Entry<NamingTable, TrueTypeFont> entry : fontList) {
            String familyLowerCase = entry.getKey().getFontFamily().toLowerCase();
            boolean matched = true;
            for (String token : tokens) {
                if (!familyLowerCase.contains(token)) {
                    matched = false;
                    break;
                }
            }

            if (matched) {
                candidates.add(entry);
            }
        }

        return candidates;
    }

    /** フォントの候補リストからサブファミリー名が完全一致するフォントを返します。
     *
     * @param candidates フォントの候補リスト
     * @param fontSubFamily 検索するサブファミリー名
     * @return 候補リスト内で見つかった最初のフォント
     */
    private TrueTypeFont getExactMatchFont(List<Map.Entry<NamingTable, TrueTypeFont>> candidates, String fontSubFamily) {
        for (Map.Entry<NamingTable, TrueTypeFont> entry : candidates) {
            if (fontSubFamily.equalsIgnoreCase(entry.getKey().getFontSubFamily())) {
                return entry.getValue();
            }
        }

        return null;
    }

    /** フォントの候補リストからサブファミリー名が部分一致するフォントを返します。
     *
     * @param candidates フォントの候補リスト
     * @param fontSubFamily 検索するサブファミリー名
     * @return 候補リスト内で最初に見つかったフォント
     */
    private TrueTypeFont getPartialMatchFont(List<Map.Entry<NamingTable, TrueTypeFont>> candidates, String fontSubFamily) {
        List<String> tokens = new ArrayList<>();
        for (String token : fontSubFamily.split("[\\s-]")) {
            if (!token.isBlank()) {
                tokens.add(token.toLowerCase().trim());
            }
        }

        for (Map.Entry<NamingTable, TrueTypeFont> entry : candidates) {
            String subFamilyLowerCase = entry.getKey().getFontSubFamily().toLowerCase();
            boolean matched = true;
            for (String token : tokens) {
                if (!subFamilyLowerCase.contains(token)) {
                    matched = false;
                    break;
                }
            }

            if (matched) {
                return entry.getValue();
            }
        }

        return null;
    }

    public void close() {
        for (Map.Entry<NamingTable, TrueTypeFont> entry : fontList) {
            TrueTypeFont ttf = entry.getValue();
            try {
                ttf.close();
            } catch (IOException e) {
                // ignore
            }
        }
        fontList.clear();
        fontMap.clear();
    }
}
