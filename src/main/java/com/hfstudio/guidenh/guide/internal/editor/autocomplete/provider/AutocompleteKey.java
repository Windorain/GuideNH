package com.hfstudio.guidenh.guide.internal.editor.autocomplete.provider;

import java.util.Objects;

public final class AutocompleteKey {

    public enum MatchType {
        /** Cursor right after '<' — match tag name candidates */
        TAG_NAME,
        /** Cursor inside tag body, not in a value — match attribute names */
        ATTR_NAME,
        /** Cursor inside an attribute value — match value candidates */
        ATTR_VALUE
    }

    private final MatchType type;
    private final String tagName;
    private final String attrName;

    private AutocompleteKey(MatchType type, String tagName, String attrName) {
        this.type = type;
        this.tagName = tagName;
        this.attrName = attrName;
    }

    public static AutocompleteKey forTag() {
        return new AutocompleteKey(MatchType.TAG_NAME, null, null);
    }

    public static AutocompleteKey forAttr(String tagName) {
        return new AutocompleteKey(MatchType.ATTR_NAME, Objects.requireNonNull(tagName), null);
    }

    public static AutocompleteKey forValue(String tagName, String attrName) {
        return new AutocompleteKey(MatchType.ATTR_VALUE,
            Objects.requireNonNull(tagName), Objects.requireNonNull(attrName));
    }

    public MatchType getType() { return type; }
    public String getTagName() { return tagName; }
    public String getAttrName() { return attrName; }

    public boolean matches(MatchType queryType, String queryTag, String queryAttr) {
        if (type != queryType) return false;
        switch (type) {
            case TAG_NAME:
                return true;
            case ATTR_NAME:
                return tagName.equals("*") || tagName.equals(queryTag);
            case ATTR_VALUE:
                return (tagName.equals("*") || tagName.equals(queryTag))
                    && (attrName.equals("*") || attrName.equals(queryAttr));
            default:
                return false;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AutocompleteKey)) return false;
        AutocompleteKey that = (AutocompleteKey) o;
        return type == that.type && Objects.equals(tagName, that.tagName)
            && Objects.equals(attrName, that.attrName);
    }

    @Override
    public int hashCode() { return Objects.hash(type, tagName, attrName); }

    @Override
    public String toString() {
        return "AutocompleteKey{" + type + ", " + tagName + ", " + attrName + "}";
    }
}
