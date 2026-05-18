package com.hfstudio.guidenh.guide.internal;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import net.minecraft.util.ResourceLocation;

import org.jetbrains.annotations.Nullable;

import com.github.bsideup.jabel.Desugar;
import com.google.common.base.Stopwatch;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.hfstudio.guidenh.guide.GuidePageChange;
import com.hfstudio.guidenh.guide.compiler.PageCompiler;
import com.hfstudio.guidenh.guide.compiler.ParsedGuidePage;
import com.hfstudio.guidenh.guide.internal.util.LangUtil;

import cpw.mods.fml.common.FMLLog;
import io.methvin.watcher.DirectoryChangeEvent;
import io.methvin.watcher.DirectoryChangeListener;
import io.methvin.watcher.DirectoryWatcher;

public class GuideSourceWatcher implements AutoCloseable {

    private final String defaultLanguage;

    /**
     * The {@link ResourceLocation} namespace to use for files in the watched folder.
     */
    private final String namespace;
    private final String contentRootFolder;
    private final Path sourceFolder;
    private final GuideDevelopmentSourceLayout sourceLayout;

    // Recursive directory watcher for the guidebook sources.
    @Nullable
    private final DirectoryWatcher sourceWatcher;

    // Queued changes that come in from a separate thread
    @Desugar
    private static class PageLangKey {

        @Nullable
        private final String sourceLang;
        private final ResourceLocation pageId;

        private PageLangKey(@Nullable String sourceLang, ResourceLocation pageId) {
            this.sourceLang = sourceLang;
            this.pageId = pageId;
        }

        @Nullable
        private String sourceLang() {
            return sourceLang;
        }

        private ResourceLocation pageId() {
            return pageId;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof PageLangKey other)) {
                return false;
            }
            return Objects.equals(sourceLang, other.sourceLang) && Objects.equals(pageId, other.pageId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(sourceLang, pageId);
        }
    }

    @Desugar
    private static class PageSource {

        private final String language;
        private final Path path;

        private PageSource(String language, Path path) {
            this.language = language;
            this.path = path;
        }

        private String language() {
            return language;
        }

        private Path path() {
            return path;
        }
    }

    @Desugar
    private static class PageReloadRequest {

        @Nullable
        private final String namespace;

        private PageReloadRequest(@Nullable String namespace) {
            this.namespace = namespace;
        }

        @Nullable
        private String namespace() {
            return namespace;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof PageReloadRequest other)) {
                return false;
            }
            return Objects.equals(namespace, other.namespace);
        }

        @Override
        public int hashCode() {
            return Objects.hash(namespace);
        }
    }

    private final Map<PageLangKey, ParsedGuidePage> changedPages = new HashMap<>();
    private final Set<PageLangKey> deletedPages = new HashSet<>();
    private final Set<PageReloadRequest> reloadRequests = new HashSet<>();

    private final ExecutorService watchExecutor;

    public GuideSourceWatcher(String namespace, String contentRootFolder, String defaultLanguage, Path sourceFolder) {
        this.namespace = namespace;
        this.contentRootFolder = contentRootFolder;
        this.defaultLanguage = LangUtil.normalizeLanguage(defaultLanguage);
        this.sourceFolder = sourceFolder;
        this.sourceLayout = detectSourceLayout(sourceFolder);
        if (!Files.isDirectory(sourceFolder)) {
            throw new RuntimeException("Cannot find the specified folder with guidebook sources: " + sourceFolder);
        }
        FMLLog.getLogger()
            .info("[GuideNH] [GuideSourceWatcher] Watching guidebook sources in {}", sourceFolder);

        watchExecutor = Executors.newSingleThreadExecutor(
            new ThreadFactoryBuilder().setDaemon(true)
                .setNameFormat("GuideMELiveReloadWatcher%d")
                .build());

        // Watch the folder recursively in a separate thread, queue up any changes and apply them
        // in the client tick.
        DirectoryWatcher watcher;
        try {
            watcher = DirectoryWatcher.builder()
                .path(sourceFolder)
                .fileHashing(false)
                .listener(new Listener())
                .build();
        } catch (IOException e) {
            FMLLog.getLogger()
                .error(
                    "[GuideNH] [GuideSourceWatcher] Failed to watch for changes in the guidebook sources at {}",
                    sourceFolder,
                    e);
            watcher = null;
        }
        sourceWatcher = watcher;

        // Actually process changes in the client tick to prevent race conditions and other crashes
        if (sourceWatcher != null) {
            sourceWatcher.watchAsync(watchExecutor);
        }
    }

    public List<ParsedGuidePage> loadAll() {
        return loadAll(null);
    }

    private List<ParsedGuidePage> loadAll(@Nullable String namespaceFilter) {
        var stopwatch = Stopwatch.createStarted();
        var currentLanguage = LangUtil.getCurrentLanguage();
        var pageSources = new HashMap<PageLangKey, Path>();
        var pageIds = new LinkedHashSet<ResourceLocation>();

        try {
            Files.walkFileTree(sourceFolder, new FileVisitor<>() {

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    var pageKey = getPageLangKey(file);
                    if (pageKey != null && (namespaceFilter == null || namespaceFilter.equals(
                        pageKey.pageId()
                            .getResourceDomain()))) {
                        pageSources.put(pageKey, file);
                        pageIds.add(pageKey.pageId());
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    FMLLog.getLogger()
                        .error("[GuideNH] [GuideSourceWatcher] Failed to list page {}", file, exc);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
                    if (exc != null) {
                        FMLLog.getLogger()
                            .error("[GuideNH] [GuideSourceWatcher] Failed to list all pages in {}", dir, exc);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            FMLLog.getLogger()
                .error("[GuideNH] [GuideSourceWatcher] Failed to list all pages in {}", sourceFolder, e);
        }

        FMLLog.getLogger()
            .info(
                "[GuideNH] [GuideSourceWatcher] Loading {} guidebook pages from {} localized variants",
                pageIds.size(),
                pageSources.size());
        var loadedPages = new ArrayList<ParsedGuidePage>(pageIds.size());
        for (var pageId : pageIds) {
            var pageSource = resolvePageSource(pageSources, pageId, currentLanguage);
            if (pageSource == null) {
                continue;
            }

            try (var in = Files.newInputStream(pageSource.path())) {
                loadedPages.add(PageCompiler.parse(getSourcePackId(pageId), pageSource.language(), pageId, in));
            } catch (Exception e) {
                FMLLog.getLogger()
                    .error("[GuideNH] [GuideSourceWatcher] Failed to reload guidebook page {}", pageSource.path(), e);
            }
        }

        FMLLog.getLogger()
            .info(
                "[GuideNH] [GuideSourceWatcher] Loaded {} pages from {} in {}",
                loadedPages.size(),
                sourceFolder,
                stopwatch);
        return loadedPages;
    }

    public void clearChanges() {
        synchronized (this) {
            changedPages.clear();
            deletedPages.clear();
            reloadRequests.clear();
        }
    }

    public List<GuidePageChange> takeChanges() {
        Set<PageReloadRequest> requests = takeReloadRequests();
        if (!requests.isEmpty()) {
            for (PageReloadRequest request : requests) {
                queueReloadedPages(loadAll(request.namespace()));
            }
        }

        synchronized (this) {
            return takeQueuedChanges();
        }
    }

    private synchronized Set<PageReloadRequest> takeReloadRequests() {
        if (reloadRequests.isEmpty()) {
            return Collections.emptySet();
        }
        Set<PageReloadRequest> requests = new HashSet<>(reloadRequests);
        reloadRequests.clear();
        return requests;
    }

    private synchronized void queueReloadedPages(List<ParsedGuidePage> pages) {
        for (ParsedGuidePage page : pages) {
            clearQueuedPageState(page.getId());
            PageLangKey pageKey = new PageLangKey(page.getLanguage(), page.getId());
            changedPages.put(pageKey, page);
            deletedPages.remove(pageKey);
        }
    }

    private List<GuidePageChange> takeQueuedChanges() {
        if (deletedPages.isEmpty() && changedPages.isEmpty()) {
            return Collections.emptyList();
        }

        var changes = new ArrayList<GuidePageChange>();

        for (var deletedPage : deletedPages) {
            ParsedGuidePage newPage = null;
            changes.add(new GuidePageChange(deletedPage.sourceLang(), deletedPage.pageId(), null, newPage));
        }
        deletedPages.clear();

        for (var entry : changedPages.entrySet()) {
            var pageKey = entry.getKey();
            var changedPage = entry.getValue();
            changes.add(new GuidePageChange(pageKey.sourceLang(), pageKey.pageId(), null, changedPage));
        }
        changedPages.clear();

        return changes;
    }

    public synchronized void close() {
        changedPages.clear();
        deletedPages.clear();
        reloadRequests.clear();
        watchExecutor.shutdownNow();
        try {
            watchExecutor.awaitTermination(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread()
                .interrupt();
        }

        if (sourceWatcher != null) {
            try {
                sourceWatcher.close();
            } catch (IOException e) {
                FMLLog.getLogger()
                    .error("[GuideNH] [GuideSourceWatcher] Failed to close fileystem watcher for {}", sourceFolder);
            }
        }
    }

    private class Listener implements DirectoryChangeListener {

        @Override
        public void onEvent(DirectoryChangeEvent event) {
            if (event.isDirectory()) {
                return;
            }
            switch (event.eventType()) {
                case CREATE, MODIFY -> pageChanged(event.path());
                case DELETE -> pageDeleted(event.path());
            }
        }

        @Override
        public boolean isWatching() {
            return sourceWatcher != null && !sourceWatcher.isClosed();
        }

        @Override
        public void onException(Exception e) {
            FMLLog.getLogger()
                .error("[GuideNH] [GuideSourceWatcher] Failed watching for changes", e);
        }
    }

    // Only call while holding the lock!
    private synchronized void pageChanged(Path path) {
        var pageKey = getPageLangKey(path);
        if (pageKey == null) {
            resourceChanged(path);
            return;
        }
        queueResolvedPageState(pageKey.pageId());
    }

    // Only call while holding the lock!
    private synchronized void pageDeleted(Path path) {
        var pageKey = getPageLangKey(path);
        if (pageKey == null) {
            resourceChanged(path);
            return;
        }
        queueResolvedPageState(pageKey.pageId());
    }

    public static boolean isGuideResourcePathForSourcePath(Path sourceFolder, String namespace,
        String contentRootFolder, Path path) {
        GuideDevelopmentSourceLayout sourceLayout = detectSourceLayout(sourceFolder, contentRootFolder);
        return getGuideRelativePath(sourceFolder, sourceLayout, namespace, contentRootFolder, path) != null;
    }

    public static boolean isPagePathForSourcePath(Path sourceFolder, String namespace, String contentRootFolder,
        Path path) {
        return resolvePageIdForSourcePath(sourceFolder, namespace, contentRootFolder, path) != null;
    }

    private synchronized void resourceChanged(Path path) {
        if (!isGuideResourcePath(path)) {
            return;
        }
        reloadRequests.add(new PageReloadRequest(getGuideResourceNamespace(path)));
    }

    @Nullable
    public ResourceLocation getPageId(Path path) {
        String relativePath = getGuideRelativePath(sourceFolder, sourceLayout, namespace, contentRootFolder, path);
        return parseGuideRelativePageId(relativePath);
    }

    private boolean isGuideResourcePath(Path path) {
        String relativePath = getGuideRelativePath(sourceFolder, sourceLayout, namespace, contentRootFolder, path);
        return relativePath != null;
    }

    @Nullable
    private String getGuideResourceNamespace(Path path) {
        String relativePath = getGuideRelativePath(sourceFolder, sourceLayout, namespace, contentRootFolder, path);
        if (relativePath == null) {
            return null;
        }
        int namespaceEnd = relativePath.indexOf('/');
        return namespaceEnd > 0 ? relativePath.substring(0, namespaceEnd) : null;
    }

    @Nullable
    public static ResourceLocation resolvePageIdForSourcePath(Path sourceFolder, String namespace,
        String contentRootFolder, Path path) {
        GuideDevelopmentSourceLayout sourceLayout = detectSourceLayout(sourceFolder, contentRootFolder);
        String relativePath = getGuideRelativePath(sourceFolder, sourceLayout, namespace, contentRootFolder, path);
        ResourceLocation pageId = parseGuideRelativePageId(relativePath);
        return pageId != null ? LangUtil.stripLangFromPageId(pageId) : null;
    }

    @Nullable
    private static ResourceLocation parseGuideRelativePageId(@Nullable String relativePath) {
        if (relativePath == null || !relativePath.endsWith(".md")) {
            return null;
        }
        int namespaceEnd = relativePath.indexOf('/');
        if (namespaceEnd <= 0 || namespaceEnd + 1 >= relativePath.length()) {
            return null;
        }
        String sourceNamespace = relativePath.substring(0, namespaceEnd);
        String pagePath = relativePath.substring(namespaceEnd + 1);
        if (!sourceNamespace.matches("[a-z0-9_.-]+") || !pagePath.matches("[a-z0-9_./-]+")) {
            return null;
        }
        return new ResourceLocation(sourceNamespace, pagePath);
    }

    @Nullable
    private PageLangKey getPageLangKey(Path path) {
        var pageId = getPageId(path);
        if (pageId == null) {
            return null;
        }

        var lang = LangUtil.getLangFromPageId(pageId);
        if (lang != null) {
            pageId = LangUtil.stripLangFromPageId(pageId);
        }

        return new PageLangKey(lang, pageId);
    }

    @Nullable
    private PageSource resolvePageSource(Map<PageLangKey, Path> pageSources, ResourceLocation pageId,
        String currentLanguage) {
        var currentPath = pageSources.get(new PageLangKey(currentLanguage, pageId));
        if (currentPath != null) {
            return new PageSource(currentLanguage, currentPath);
        }

        if (!Objects.equals(currentLanguage, defaultLanguage)) {
            var defaultPath = pageSources.get(new PageLangKey(defaultLanguage, pageId));
            if (defaultPath != null) {
                return new PageSource(defaultLanguage, defaultPath);
            }
        }

        var neutralPath = pageSources.get(new PageLangKey(null, pageId));
        if (neutralPath != null) {
            return new PageSource(currentLanguage, neutralPath);
        }

        return null;
    }

    private void queueResolvedPageState(ResourceLocation pageId) {
        clearQueuedPageState(pageId);
        PageSource activeSource = resolveActivePageSource(pageId, LangUtil.getCurrentLanguage());
        if (activeSource == null) {
            deletedPages.add(new PageLangKey(null, pageId));
            return;
        }

        try (var in = Files.newInputStream(activeSource.path())) {
            var page = PageCompiler.parse(getSourcePackId(pageId), activeSource.language(), pageId, in);
            changedPages.put(new PageLangKey(activeSource.language(), pageId), page);
        } catch (Exception e) {
            FMLLog.getLogger()
                .error(
                    "[GuideNH] [GuideSourceWatcher] Failed to resolve effective guidebook page {}",
                    activeSource.path(),
                    e);
        }
    }

    private void clearQueuedPageState(ResourceLocation pageId) {
        changedPages.keySet()
            .removeIf(
                pageKey -> pageKey.pageId()
                    .equals(pageId));
        deletedPages.removeIf(
            pageKey -> pageKey.pageId()
                .equals(pageId));
    }

    @Nullable
    private PageSource resolveActivePageSource(ResourceLocation pageId, String currentLanguage) {
        var currentPath = getLocalizedSourcePath(pageId, currentLanguage);
        if (Files.isRegularFile(currentPath)) {
            return new PageSource(currentLanguage, currentPath);
        }

        if (!Objects.equals(currentLanguage, defaultLanguage)) {
            var defaultPath = getLocalizedSourcePath(pageId, defaultLanguage);
            if (Files.isRegularFile(defaultPath)) {
                return new PageSource(defaultLanguage, defaultPath);
            }
        }

        var neutralPath = getNeutralSourcePath(pageId);
        if (Files.isRegularFile(neutralPath)) {
            return new PageSource(currentLanguage, neutralPath);
        }

        return null;
    }

    private Path getLocalizedSourcePath(ResourceLocation pageId, String language) {
        return getSourcePath(pageId, "_" + LangUtil.normalizeLanguage(language) + "/" + pageId.getResourcePath());
    }

    private Path getNeutralSourcePath(ResourceLocation pageId) {
        return getSourcePath(pageId, pageId.getResourcePath());
    }

    private String getSourcePackId(ResourceLocation pageId) {
        return "development:" + pageId.getResourceDomain();
    }

    private Path getSourcePath(ResourceLocation pageId, String contentRelativePath) {
        return switch (sourceLayout) {
            case CONTENT_ROOT -> sourceFolder.resolve(contentRelativePath);
            case RESOURCE_PACK_ROOT -> sourceFolder.resolve("assets")
                .resolve(pageId.getResourceDomain())
                .resolve(contentRootFolder)
                .resolve(contentRelativePath);
            case ASSETS_ROOT -> sourceFolder.resolve(pageId.getResourceDomain())
                .resolve(contentRootFolder)
                .resolve(contentRelativePath);
        };
    }

    @Nullable
    private static String getGuideRelativePath(Path sourceFolder, GuideDevelopmentSourceLayout sourceLayout,
        String namespace, String contentRootFolder, Path path) {
        String relativePath = sourceFolder.relativize(path)
            .toString()
            .replace('\\', '/');
        return switch (sourceLayout) {
            case CONTENT_ROOT -> namespace + "/" + relativePath;
            case RESOURCE_PACK_ROOT -> stripResourcePackPrefix(relativePath, contentRootFolder);
            case ASSETS_ROOT -> stripAssetsPrefix(relativePath, contentRootFolder);
        };
    }

    @Nullable
    private static String stripResourcePackPrefix(String relativePath, String contentRootFolder) {
        String prefix = "assets/";
        if (!relativePath.startsWith(prefix)) {
            return null;
        }
        return stripAssetsPrefix(relativePath.substring(prefix.length()), contentRootFolder);
    }

    @Nullable
    private static String stripAssetsPrefix(String relativePath, String contentRootFolder) {
        int namespaceEnd = relativePath.indexOf('/');
        if (namespaceEnd <= 0) {
            return null;
        }
        String afterNamespace = relativePath.substring(namespaceEnd + 1);
        String guidePrefix = contentRootFolder + "/";
        if (!afterNamespace.startsWith(guidePrefix)) {
            return null;
        }
        return relativePath.substring(0, namespaceEnd + 1) + afterNamespace.substring(guidePrefix.length());
    }

    private GuideDevelopmentSourceLayout detectSourceLayout(Path folder) {
        return detectSourceLayout(folder, contentRootFolder);
    }

    private static GuideDevelopmentSourceLayout detectSourceLayout(Path folder, String contentRootFolder) {
        return GuideDevelopmentSourceLayout.detect(folder, contentRootFolder);
    }
}
