package com.hfstudio.guidenh.compat.preview;

import com.hfstudio.guidenh.compat.ae2.Ae2ServerPreviewRegistration;

/**
 * Registers all compat-layer server-preview supplement strategies into
 * {@link com.hfstudio.guidenh.guide.scene.snapshot.ServerPreviewSupplementRegistry}.
 */
public class GuideCompatStructurePreviewBootstrap {

    public GuideCompatStructurePreviewBootstrap() {}

    public static void registerServerPreviewSupplements() {
        Ae2ServerPreviewRegistration.register();
    }
}
