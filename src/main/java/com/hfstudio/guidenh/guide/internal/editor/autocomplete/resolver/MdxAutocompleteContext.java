package com.hfstudio.guidenh.guide.internal.editor.autocomplete.resolver;

import com.hfstudio.guidenh.guide.internal.editor.autocomplete.AutocompleteContext;

public final class MdxAutocompleteContext implements AutocompleteContext {
    private final String tagName;
    private final String attributeName;
    private final int replaceStart;
    private final int replaceEnd;
    private final String partialText;

    public MdxAutocompleteContext(String tagName, String attributeName, int replaceStart, int replaceEnd,
                                  String partialText) {
        this.tagName = tagName;
        this.attributeName = attributeName;
        this.replaceStart = replaceStart;
        this.replaceEnd = replaceEnd;
        this.partialText = partialText;
    }

    public String getTagName() { return tagName; }
    public String getAttributeName() { return attributeName; }

    @Override public int replaceStart() { return replaceStart; }
    @Override public int replaceEnd() { return replaceEnd; }
    @Override public String getPartialText() { return partialText; }
}
