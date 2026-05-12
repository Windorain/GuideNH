package com.hfstudio.guidenh.guide.internal;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public enum GuideDevelopmentSourceLayout {

    CONTENT_ROOT,
    RESOURCE_PACK_ROOT,
    ASSETS_ROOT;

    public static GuideDevelopmentSourceLayout detect(Path folder, String contentRootFolder) {
        if (Files.isDirectory(folder.resolve("assets"))) {
            return RESOURCE_PACK_ROOT;
        }
        if (containsGuideNamespaceFolder(folder, contentRootFolder)) {
            return ASSETS_ROOT;
        }
        return CONTENT_ROOT;
    }

    public static boolean containsGuideNamespaceFolder(Path folder, String contentRootFolder) {
        try (var children = Files.list(folder)) {
            return children.anyMatch(child -> Files.isDirectory(child.resolve(contentRootFolder)));
        } catch (IOException e) {
            return false;
        }
    }
}
