package com.hfstudio.guidenh.guide.internal.editor.autocomplete.resolver;

import com.hfstudio.guidenh.guide.internal.editor.autocomplete.AutocompleteContext;

public final class FrontmatterContext implements AutocompleteContext {
    private final String key;
    private final int replaceStart;
    private final int replaceEnd;
    private final String partialText;

    public FrontmatterContext(String key, int replaceStart, int replaceEnd, String partialText) {
        this.key = key;
        this.replaceStart = replaceStart;
        this.replaceEnd = replaceEnd;
        this.partialText = partialText;
    }

    public String getKey() { return key; }

    @Override public int replaceStart() { return replaceStart; }
    @Override public int replaceEnd() { return replaceEnd; }
    @Override public String getPartialText() { return partialText; }
}
