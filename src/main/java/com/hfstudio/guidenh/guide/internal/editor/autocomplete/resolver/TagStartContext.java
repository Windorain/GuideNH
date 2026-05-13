package com.hfstudio.guidenh.guide.internal.editor.autocomplete.resolver;

import com.hfstudio.guidenh.guide.internal.editor.autocomplete.AutocompleteContext;

/** Context for tag NAME completion — cursor right after '<', before tag name. */
public final class TagStartContext implements AutocompleteContext {
    private final int replaceStart;
    private final int replaceEnd;
    private final String partialText;

    public TagStartContext(int replaceStart, int replaceEnd, String partialText) {
        this.replaceStart = replaceStart;
        this.replaceEnd = replaceEnd;
        this.partialText = partialText;
    }

    @Override public int replaceStart() { return replaceStart; }
    @Override public int replaceEnd() { return replaceEnd; }
    @Override public String getPartialText() { return partialText; }
}
