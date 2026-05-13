package com.hfstudio.guidenh.guide.internal.editor.autocomplete.provider;

import net.minecraft.client.gui.FontRenderer;

public interface AutocompleteCandidate {
    String displayText();
    String replacementText();
    default int renderHeight() { return 14; }
    void render(FontRenderer fontRenderer, int x, int y, int width, boolean hovered);
}
