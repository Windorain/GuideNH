package com.hfstudio.guidenh.guide.compiler.tags;

import java.util.Collections;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import com.hfstudio.guidenh.guide.compiler.PageCompiler;
import com.hfstudio.guidenh.guide.document.block.LytBlockContainer;
import com.hfstudio.guidenh.guide.document.block.LytParagraph;
import com.hfstudio.guidenh.libs.mdast.mdx.model.MdxJsxElementFields;

public class BlockImageCompiler extends BlockTagCompiler {

    @Override
    public Set<String> getTagNames() {
        return Collections.singleton("BlockImage");
    }

    @Override
    protected void compile(PageCompiler compiler, LytBlockContainer parent, MdxJsxElementFields el) {
        String id = MdxAttrs.getString(compiler, parent, el, "id", null);
        String ore = MdxAttrs.getString(compiler, parent, el, "ore", null);

        if ((id == null || id.trim().isEmpty()) && (ore == null || ore.trim().isEmpty())) {
            parent.appendError(compiler, "Missing id attribute (or ore).", el);
            return;
        }

        int meta = MdxAttrs.getInt(compiler, parent, el, "meta", 0);
        String nbt = MdxAttrs.getString(compiler, parent, el, "nbt", null);
        float scale = MdxAttrs.getFloat(compiler, parent, el, "scale", 1f);
        String perspective = MdxAttrs.getString(compiler, parent, el, "perspective", null);
        int width = MdxAttrs.getInt(compiler, parent, el, "width", 128);
        int height = MdxAttrs.getInt(compiler, parent, el, "height", 128);

        // Create placeholder block that carries all extracted config to BlockImageScript
        BlockImagePlaceholder placeholder = new BlockImagePlaceholder(
            id, ore, meta, nbt, scale, perspective, width, height);
        placeholder.setStyleClass("BlockImage");
        placeholder.appendText("[BlockImage]");
        parent.append(placeholder);
    }

    /**
     * Placeholder block that stores all extracted block-image configuration for deferred scene
     * creation by {@code BlockImageScript}.
     */
    static class BlockImagePlaceholder extends LytParagraph {
        @Nullable final String id;
        @Nullable final String ore;
        final int meta;
        @Nullable final String nbt;
        final float scale;
        @Nullable final String perspective;
        final int width;
        final int height;

        BlockImagePlaceholder(@Nullable String id, @Nullable String ore, int meta, @Nullable String nbt,
                              float scale, @Nullable String perspective, int width, int height) {
            this.id = id;
            this.ore = ore;
            this.meta = meta;
            this.nbt = nbt;
            this.scale = scale;
            this.perspective = perspective;
            this.width = width;
            this.height = height;
        }
    }
}
