package com.hfstudio.guidenh.guide.siteexport.site;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import com.hfstudio.guidenh.guide.compiler.PageCompiler;
import com.hfstudio.guidenh.guide.compiler.ParsedGuidePage;
import com.hfstudio.guidenh.guide.internal.MutableGuide;
import com.hfstudio.guidenh.guide.internal.datadriven.DataDrivenGuideLoader;
import com.hfstudio.guidenh.guide.internal.resource.GuideResourceAccess;

import cpw.mods.fml.common.FMLLog;

public class GuideSitePageCollector {

    @FunctionalInterface
    public interface PageLoader {

        Optional<ParsedGuidePage> load(String language, ResourceLocation pageId);
    }

    private final PageLoader pageLoader;

    public GuideSitePageCollector(PageLoader pageLoader) {
        this.pageLoader = pageLoader;
    }

    public GuideSitePageCollector(MutableGuide guide, IResourceManager resourceManager) {
        this((language, pagePath) -> tryLoadPage(guide, resourceManager, language, pagePath));
    }

    public List<GuideSitePageVariant> collect(MutableGuide guide) {
        List<String> languages;
        try {
            languages = discoverLanguages();
        } catch (Throwable t) {
            FMLLog.getLogger()
                .debug(
                    "[GuideNH] [GuideSitePageCollector] Falling back to the guide default language for {}",
                    guide.getId(),
                    t);
            languages = new ArrayList<>();
        }
        if (languages.isEmpty()) {
            languages.add(guide.getDefaultLanguage());
        } else if (!languages.contains(guide.getDefaultLanguage())) {
            languages.add(0, guide.getDefaultLanguage());
        }

        LinkedHashSet<ResourceLocation> pageIdSet;
        try {
            pageIdSet = new LinkedHashSet<>();
            var pathsByNs = DataDrivenGuideLoader.discoverPagePaths(guide.getContentRootFolder());
            for (var entry : pathsByNs.entrySet()) {
                for (String path : entry.getValue()) {
                    pageIdSet.add(new ResourceLocation(entry.getKey(), path));
                }
            }
        } catch (Throwable t) {
            FMLLog.getLogger()
                .debug(
                    "[GuideNH] [GuideSitePageCollector] Falling back to already loaded page ids for {}",
                    guide.getId(),
                    t);
            pageIdSet = new LinkedHashSet<>();
        }
        for (ParsedGuidePage page : guide.getPages()) {
            pageIdSet.add(page.getId());
        }
        List<ResourceLocation> pageIds = new ArrayList<>(pageIdSet);
        return collect(guide.getId(), guide.getDefaultLanguage(), languages, pageIds);
    }

    public List<GuideSitePageVariant> collect(ResourceLocation guideId, String defaultLanguage, List<String> languages,
        List<ResourceLocation> pageIds) {
        List<GuideSitePageVariant> variants = new ArrayList<>();

        for (String language : languages) {
            for (ResourceLocation pageId : pageIds) {
                Optional<ParsedGuidePage> localized = pageLoader.load(language, pageId);
                if (localized.isPresent()) {
                    ParsedGuidePage page = localized.get();
                    variants.add(new GuideSitePageVariant(guideId, page.getId(), language, language, false, page));
                    continue;
                }

                Optional<ParsedGuidePage> fallback = pageLoader.load(defaultLanguage, pageId);
                fallback.ifPresent(
                    page -> variants
                        .add(new GuideSitePageVariant(guideId, page.getId(), language, defaultLanguage, true, page)));
            }
        }

        return variants;
    }

    private static List<String> discoverLanguages() {
        Map<ResourceLocation, LinkedHashSet<String>> discovered = new LinkedHashMap<>();
        for (var resourcePack : DataDrivenGuideLoader.getActiveResourcePacks()) {
            DataDrivenGuideLoader.scanResourcePack(resourcePack, discovered);
        }
        var merged = new LinkedHashSet<String>();
        for (var langs : discovered.values()) {
            merged.addAll(langs);
        }
        return new ArrayList<>(merged);
    }

    private static Optional<ParsedGuidePage> tryLoadPage(MutableGuide guide, IResourceManager resourceManager,
        String language, ResourceLocation pageId) {
        String namespace = pageId.getResourceDomain();
        String pagePath = pageId.getResourcePath();
        ResourceLocation localizedSource = new ResourceLocation(
            namespace,
            guide.getContentRootFolder() + "/_" + language + "/" + pagePath);

        try (var stream = GuideResourceAccess.openStream(resourceManager, localizedSource)) {
            if (stream == null) {
                return Optional.empty();
            }
            return Optional.of(PageCompiler.parse("resources:" + namespace, language, pageId, stream));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
