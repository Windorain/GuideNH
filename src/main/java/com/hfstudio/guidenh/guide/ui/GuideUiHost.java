package com.hfstudio.guidenh.guide.ui;

import java.net.URI;

import net.minecraft.util.ResourceLocation;

import com.hfstudio.guidenh.guide.PageAnchor;

public interface GuideUiHost {

    void navigateTo(PageAnchor anchor);

    default void navigateTo(ResourceLocation guideId, PageAnchor anchor) {
        navigateTo(anchor);
    }

    void close();

    default void openExternalUrl(URI uri) {}

    default boolean copyCodeBlock(String text) {
        return false;
    }

    default boolean isCodeBlockWheelInteractionBlocked() {
        return false;
    }
}
