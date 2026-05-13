package com.hfstudio.guidenh.guide.internal.editor.autocomplete.provider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import com.hfstudio.guidenh.guide.internal.editor.autocomplete.AutocompleteContext;

public class ItemIdProvider implements AutocompleteProvider {

    private static final Set<AutocompleteKey> KEYS =
        Collections.singleton(new AutocompleteKey("*", "id"));

    @Override
    public Set<AutocompleteKey> getSupportedKeys() {
        return KEYS;
    }

    @Override
    public List<AutocompleteCandidate> provide(AutocompleteContext ctx, int limit) {
        String partial = ctx.getPartialText();
        String lower = partial != null ? partial.toLowerCase() : "";

        List<AutocompleteCandidate> results = new ArrayList<>();
        for (Object obj : Item.itemRegistry.getKeys()) {
            if (results.size() >= limit) break;
            if (obj instanceof String key) {
                if (lower.isEmpty() || key.toLowerCase().contains(lower)) {
                    Item item = (Item) Item.itemRegistry.getObject(key);
                    if (item != null) {
                        results.add(new ItemCandidate(key, new ItemStack(item)));
                    }
                }
            }
        }
        return results;
    }
}
