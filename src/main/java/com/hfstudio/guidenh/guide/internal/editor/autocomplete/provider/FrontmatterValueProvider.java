package com.hfstudio.guidenh.guide.internal.editor.autocomplete.provider;

import java.util.*;

import com.hfstudio.guidenh.guide.internal.editor.autocomplete.AutocompleteContext;
import com.hfstudio.guidenh.guide.internal.editor.autocomplete.resolver.FrontmatterContext;

/** Dispatches frontmatter value completion based on the current key. */
public class FrontmatterValueProvider implements AutocompleteProvider {

    private static final Set<AutocompleteKey> KEYS =
        Collections.singleton(AutocompleteKey.forValue("*", "fm_value"));

    private static final Map<String, String[]> HINTS = new LinkedHashMap<>();
    static {
        HINTS.put("navigation", new String[]{"  title:", "  parent:", "  position:", "  icon:", "  icon_texture:"});
        HINTS.put("title", new String[]{"\"Page Title\""});
        HINTS.put("parent", new String[]{});
        HINTS.put("position", new String[]{"0"});
        HINTS.put("icon", new String[]{});
        HINTS.put("icon_texture", new String[]{});
        HINTS.put("item_ids", new String[]{});
        HINTS.put("ore_ids", new String[]{});
        HINTS.put("quest_ids", new String[]{});
        HINTS.put("authors", new String[]{"\"Author Name\""});
        HINTS.put("author", new String[]{"\"Author Name\""});
        HINTS.put("date", new String[]{"\"YYYY-MM-DD\""});
        HINTS.put("updated", new String[]{"\"YYYY-MM-DD\""});
        HINTS.put("zoom", new String[]{"1.0"});
    }

    @Override
    public Set<AutocompleteKey> getSupportedKeys() { return KEYS; }

    @Override
    public List<AutocompleteCandidate> provide(AutocompleteContext ctx, int limit) {
        if (!(ctx instanceof FrontmatterContext)) return Collections.emptyList();
        FrontmatterContext fmc = (FrontmatterContext) ctx;
        String[] suggestions = HINTS.getOrDefault(fmc.getKey(), new String[0]);
        String partial = fmc.getPartialText().toLowerCase();
        List<AutocompleteCandidate> results = new ArrayList<>();
        for (String s : suggestions) {
            if (results.size() >= limit) break;
            if (partial.isEmpty() || s.toLowerCase().contains(partial)) {
                results.add(new TextCandidate(s));
            }
        }
        return results;
    }
}
