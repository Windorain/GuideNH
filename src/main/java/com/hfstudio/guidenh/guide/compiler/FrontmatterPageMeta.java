package com.hfstudio.guidenh.guide.compiler;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.github.bsideup.jabel.Desugar;

@Desugar
public record FrontmatterPageMeta(List<String> authors, @Nullable String date, @Nullable String updated) {

    public boolean isEmpty() {
        return authors.isEmpty() && date == null && updated == null;
    }
}
