package com.hfstudio.guidenh.guide.document.block;

/**
 * Vertical alignment mode for inline {@link LytLatexBlock} relative to the surrounding text line.
 * Determines how the formula is positioned vertically when placed inside a text flow.
 */
public enum LatexVerticalAlign {

    /**
     * Formula top aligns with the top of the text line.
     * The formula may extend below the normal text bottom when it is taller than the text.
     */
    TOP,

    /**
     * Formula is positioned so its math baseline aligns with the text baseline.
     *
     * <p>
     * Internally this adds back the formula's typographic depth (pixels below the math baseline,
     * e.g. denominators in fractions) to the BOTTOM offset, so the math baseline lands on the same
     * horizontal line as the text glyphs. For depth-zero formulas (pure superscripts, plain letters)
     * the result is identical to {@link #BOTTOM}.
     *
     * <p>
     * This is the default alignment because it produces the most natural-looking result for inline
     * math: letters and superscripts align flush with the surrounding text, while fractions and
     * integral limits extend symmetrically above and below the text baseline.
     */
    BASELINE,

    /**
     * Formula is vertically centered on the surrounding text line height.
     * Tall formulas grow symmetrically around the text mid-line.
     *
     * <p>
     * Note: for formulas that have non-zero typographic ascent but zero depth (e.g. {@code E=mc^2}),
     * CENTER shifts the base letters <em>below</em> the text baseline. Prefer {@link #BASELINE} for
     * most inline formulas.
     */
    CENTER,

    /**
     * Formula bottom aligns with the bottom of the text line.
     * Tall formulas extend above the normal text top.
     */
    BOTTOM;

    /**
     * Parses a string value (case-insensitive) and returns the matching enum constant,
     * or {@link #BASELINE} if the value is not recognised.
     *
     * @param value the raw attribute string, may be {@code null}
     * @return the corresponding {@link LatexVerticalAlign} constant
     */
    public static LatexVerticalAlign parse(String value) {
        if (value == null) {
            return BASELINE;
        }
        return switch (value.trim()
            .toLowerCase()) {
            case "top" -> TOP;
            case "center", "centre" -> CENTER;
            case "bottom" -> BOTTOM;
            default -> BASELINE;
        };
    }
}
