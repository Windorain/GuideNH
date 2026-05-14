package com.hfstudio.guidenh.guide.internal.editor.autocomplete.provider;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.ResourcePackRepository;
import net.minecraft.util.ResourceLocation;

import com.hfstudio.guidenh.guide.Guide;
import com.hfstudio.guidenh.guide.compiler.ParsedGuidePage;
import com.hfstudio.guidenh.guide.internal.datadriven.DataDrivenGuideLoader;
import com.hfstudio.guidenh.guide.internal.editor.autocomplete.AutocompleteContext;

/**
 * Suggests file paths from the guide assets directory for src attributes.
 * Call {@link #refreshFromGuide(Guide)} once to initialize the scanned directories.
 */
public class ImagePathProvider implements AutocompleteProvider {

    private static final Set<AutocompleteKey> KEYS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
        AutocompleteKey.forValue("FloatingImage", "src"),
        AutocompleteKey.forValue("ImportStructure", "src"),
        AutocompleteKey.forValue("ImportPonder", "src"),
        AutocompleteKey.forValue("Mermaid", "src"),
        AutocompleteKey.forValue("CsvTable", "src")
    )));

    private static final String[] EXTENSIONS = {
        ".png", ".jpg", ".jpeg", ".gif", ".snbt", ".nbt", ".csv", ".json", ".mmd", ".md"
    };

    @Nullable
    private static volatile List<File> baseDirs;
    private static volatile boolean scanned;

    /**
     * Scans the guide's resource pack directories for asset folders matching
     * page paths.  This method performs the scan only once; subsequent calls
     * are no-ops once the scan has completed.
     */
    public static void refreshFromGuide(@Nullable Guide guide) {
        if (scanned || guide == null) return;
        scanned = true;

        List<File> dirs = new ArrayList<>();

        // Collect all active resource packs (same pattern as DataDrivenGuideLoader)
        List<IResourcePack> packs = new ArrayList<>();
        var fmlAccessor = (com.hfstudio.guidenh.mixins.early.fml.AccessorFMLClientHandler)
            cpw.mods.fml.client.FMLClientHandler.instance();
        List<IResourcePack> basePacks = fmlAccessor.guidenh$getResourcePackList();
        if (basePacks != null) packs.addAll(basePacks);
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.getResourcePackRepository() != null) {
            for (ResourcePackRepository.Entry entry : mc.getResourcePackRepository().getRepositoryEntries()) {
                IResourcePack rp = entry.getResourcePack();
                if (rp != null) packs.add(rp);
            }
        }

        for (ParsedGuidePage page : guide.getPages()) {
            ResourceLocation id = page.getId();
            String ns = id.getResourceDomain();
            String path = id.getResourcePath();

            // Extract directory portion: "path/to/page.md" -> "path/to"
            int slashIdx = path.lastIndexOf('/');
            String dirPath = slashIdx > 0 ? path.substring(0, slashIdx) : "";

            for (IResourcePack rp : packs) {
                File packFile = DataDrivenGuideLoader.getResourcePackFile(rp);
                if (packFile == null) continue;
                File assetsDir = new File(packFile, "assets/" + ns + "/" + dirPath);
                if (assetsDir.isDirectory() && !dirs.contains(assetsDir)) {
                    dirs.add(assetsDir);
                }
            }
        }

        baseDirs = dirs.isEmpty() ? null : Collections.unmodifiableList(dirs);
    }

    @Override
    public Set<AutocompleteKey> getSupportedKeys() {
        return KEYS;
    }

    @Override
    public List<AutocompleteCandidate> provide(AutocompleteContext ctx, int limit) {
        if (baseDirs == null) return Collections.emptyList();
        String partial = ctx.getPartialText().toLowerCase();
        List<AutocompleteCandidate> results = new ArrayList<>();

        for (File dir : baseDirs) {
            if (results.size() >= limit) break;
            scanDir(dir, "", partial, results, limit);
        }
        return results;
    }

    private void scanDir(File dir, String prefix, String partial,
                         List<AutocompleteCandidate> results, int limit) {
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File f : files) {
            if (results.size() >= limit) return;
            String name = f.getName();
            if (name.startsWith(".")) continue;
            String relPath = prefix.isEmpty() ? name : prefix + "/" + name;
            if (f.isDirectory()) {
                scanDir(f, relPath, partial, results, limit);
            } else if (matchesExtension(name)) {
                if (partial.isEmpty() || relPath.toLowerCase().contains(partial)) {
                    results.add(new TextCandidate(relPath));
                }
            }
        }
    }

    private static boolean matchesExtension(String name) {
        String lower = name.toLowerCase();
        for (String ext : EXTENSIONS) {
            if (lower.endsWith(ext)) return true;
        }
        return false;
    }
}
