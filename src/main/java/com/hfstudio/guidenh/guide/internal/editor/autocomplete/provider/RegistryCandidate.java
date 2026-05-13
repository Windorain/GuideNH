package com.hfstudio.guidenh.guide.internal.editor.autocomplete.provider;

import net.minecraft.client.gui.FontRenderer;

/** Candidate displaying a registry key with optional subtitle. */
public class RegistryCandidate implements AutocompleteCandidate {
    private final String key;
    private final String subtitle;
    private static final int TEXT_COLOR = 0xFFF0F0F0;
    private static final int SUBTITLE_COLOR = 0xFFA0A0A0;

    public RegistryCandidate(String key) {
        this(key, null);
    }

    public RegistryCandidate(String key, String subtitle) {
        this.key = key;
        this.subtitle = subtitle;
    }

    @Override public String displayText() { return key; }
    @Override public String replacementText() { return key; }
    @Override public int renderHeight() { return subtitle != null ? 28 : 14; }

    @Override
    public void render(FontRenderer fontRenderer, int x, int y, int width, boolean hovered) {
        fontRenderer.drawString(key, x, y + 2, TEXT_COLOR);
        if (subtitle != null) {
            fontRenderer.drawString(subtitle, x + 4, y + 14, SUBTITLE_COLOR);
        }
    }
}
