package com.hfstudio.guidenh.guide.internal.editor.autocomplete.provider;

import java.util.ArrayList;
import java.util.List;

import com.hfstudio.guidenh.guide.internal.editor.autocomplete.AutocompleteContext;

public final class AutocompleteProviders {

    private static final List<AutocompleteProvider> providers = new ArrayList<>();

    private AutocompleteProviders() {}

    public static void register(AutocompleteProvider provider) {
        providers.add(provider);
    }

    public static List<AutocompleteCandidate> query(AutocompleteContext ctx, int limit) {
        List<AutocompleteCandidate> results = new ArrayList<>();
        for (AutocompleteProvider provider : providers) {
            for (AutocompleteKey key : provider.getSupportedKeys()) {
                if (matchesContext(key, ctx)) {
                    results.addAll(provider.provide(ctx, Math.max(0, limit - results.size())));
                    break;
                }
            }
            if (results.size() >= limit) break;
        }
        return results;
    }

    private static boolean matchesContext(AutocompleteKey key, AutocompleteContext ctx) {
        if (ctx instanceof com.hfstudio.guidenh.guide.internal.editor.autocomplete.resolver.MdxAutocompleteContext) {
            com.hfstudio.guidenh.guide.internal.editor.autocomplete.resolver.MdxAutocompleteContext mdx =
                (com.hfstudio.guidenh.guide.internal.editor.autocomplete.resolver.MdxAutocompleteContext) ctx;
            return key.matches(AutocompleteKey.MatchType.ATTR_VALUE, mdx.getTagName(), mdx.getAttributeName());
        }
        return false;
    }

    public static void clear() {
        providers.clear();
    }
}
