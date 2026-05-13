package com.hfstudio.guidenh.guide.internal.editor.autocomplete.provider;

import java.util.Objects;

public final class AutocompleteKey {
    private final String tagName;
    private final String attributeName;

    public AutocompleteKey(String tagName, String attributeName) {
        this.tagName = Objects.requireNonNull(tagName);
        this.attributeName = Objects.requireNonNull(attributeName);
    }

    public String getTagName() { return tagName; }
    public String getAttributeName() { return attributeName; }

    public boolean matches(String tag, String attr) {
        if (!tagName.equals("*") && !tagName.equals(tag)) return false;
        if (!attributeName.equals("*") && !attributeName.equals(attr)) return false;
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AutocompleteKey)) return false;
        AutocompleteKey that = (AutocompleteKey) o;
        return tagName.equals(that.tagName) && attributeName.equals(that.attributeName);
    }

    @Override
    public int hashCode() { return Objects.hash(tagName, attributeName); }
}
