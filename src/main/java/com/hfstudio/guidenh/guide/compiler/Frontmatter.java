package com.hfstudio.guidenh.guide.compiler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import net.minecraft.util.ResourceLocation;

import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import com.github.bsideup.jabel.Desugar;

@Desugar
public record Frontmatter(@Nullable FrontmatterNavigation navigationEntry, Map<String, Object> additionalProperties) {

    // SnakeYAML's Yaml is not thread-safe; use a per-thread cached instance to avoid
    // re-allocating LoaderOptions/SafeConstructor for every page parsed.
    private static final ThreadLocal<Yaml> YAML = ThreadLocal
        .withInitial(() -> new Yaml(new SafeConstructor(new LoaderOptions())));

    public static Frontmatter parse(ResourceLocation pageId, String yamlText) {
        var yaml = YAML.get();

        FrontmatterNavigation navigation = null;
        Map<String, Object> data = yaml.load(yamlText);
        var navigationObj = data.remove("navigation");
        if (navigationObj != null) {
            if (!(navigationObj instanceof Map<?, ?>navigationMap)) {
                throw new IllegalArgumentException("The navigation key in the frontmatter has to be a map");
            }

            var title = getString(navigationMap, "title");
            if (title == null) {
                throw new IllegalArgumentException("title is missing in navigation frontmatter");
            }
            var parentIdStr = getString(navigationMap, "parent");
            var position = 0;
            if (navigationMap.containsKey("position")) {
                position = getInt(navigationMap, "position");
            }
            var iconIdStr = getString(navigationMap, "icon");
            var iconTextureStr = getString(navigationMap, "icon_texture");
            Map<?, ?> iconComponents = getCompound(navigationMap, "icon_components");

            ResourceLocation parentId = null;
            if (parentIdStr != null) {
                parentId = IdUtils.resolveId(parentIdStr, pageId.getResourceDomain());
            }

            ResourceLocation iconId = null;
            if (iconIdStr != null) {
                iconId = IdUtils.resolveId(iconIdStr, pageId.getResourceDomain());
            }

            ResourceLocation iconTextureId = null;
            if (iconTextureStr != null) {
                iconTextureId = IdUtils.resolveLink(iconTextureStr, pageId);
            }

            navigation = new FrontmatterNavigation(title, parentId, position, iconId, iconComponents, iconTextureId);
        }

        return new Frontmatter(navigation, Collections.unmodifiableMap(new HashMap<>(data)));
    }

    @Nullable
    public static String getString(Map<?, ?> map, String key) {
        var value = map.get(key);
        if (value != null && !(value instanceof String)) {
            throw new IllegalArgumentException("Key " + key + " has to be a String!");
        }
        return (String) value;
    }

    public static int getInt(Map<?, ?> map, String key) {
        var value = map.get(key);
        if (value == null) {
            throw new IllegalArgumentException("Key " + key + " is missing in navigation frontmatter");
        }
        if (!(value instanceof Number number)) {
            throw new IllegalArgumentException("Key " + key + " has to be a number!");
        }
        return number.intValue();
    }

    @Nullable
    public static Map<?, ?> getCompound(Map<?, ?> map, String key) {
        var value = map.get(key);
        if (value == null) {
            return null;
        }
        if (!(value instanceof Map<?, ?>mapValue)) {
            throw new IllegalArgumentException("Key " + key + " has to be a map!");
        }
        return mapValue;
    }

    /**
     * Parses {@code author}/{@code authors}, {@code date}, and {@code updated} from
     * {@link #additionalProperties()} into a {@link FrontmatterPageMeta} value object.
     */
    public FrontmatterPageMeta parseMeta() {
        List<String> authors = new ArrayList<>();

        // Single-author shorthand: author: "Name"
        Object authorObj = additionalProperties.get("author");
        if (authorObj instanceof String) {
            String s = (String) authorObj;
            if (!s.trim()
                .isEmpty()) authors.add(s.trim());
        }

        // Multi-author list: authors: ["Name"] or authors: [{name: "Name"}]
        Object authorsObj = additionalProperties.get("authors");
        if (authorsObj instanceof List<?>) {
            for (Object item : (List<?>) authorsObj) {
                if (item instanceof String) {
                    String s = ((String) item).trim();
                    if (!s.isEmpty()) authors.add(s);
                } else if (item instanceof Map<?, ?>) {
                    Object name = ((Map<?, ?>) item).get("name");
                    if (name instanceof String) {
                        String s = ((String) name).trim();
                        if (!s.isEmpty()) authors.add(s);
                    }
                }
            }
        }

        String date = toDateString(additionalProperties.get("date"));
        String updated = toDateString(additionalProperties.get("updated"));

        return new FrontmatterPageMeta(Collections.unmodifiableList(authors), date, updated);
    }

    @Nullable
    private static String toDateString(@Nullable Object value) {
        if (value == null) return null;
        if (value instanceof String) {
            String s = ((String) value).trim();
            return s.isEmpty() ? null : s;
        }
        if (value instanceof java.util.Date) {
            return new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format((java.util.Date) value);
        }
        return value.toString();
    }
}
