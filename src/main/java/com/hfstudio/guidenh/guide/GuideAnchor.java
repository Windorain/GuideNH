package com.hfstudio.guidenh.guide;

import net.minecraft.util.ResourceLocation;

import com.github.bsideup.jabel.Desugar;

@Desugar
public record GuideAnchor(ResourceLocation guideId, PageAnchor page) {}
