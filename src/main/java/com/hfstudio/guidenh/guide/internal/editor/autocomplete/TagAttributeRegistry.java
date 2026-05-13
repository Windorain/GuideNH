package com.hfstudio.guidenh.guide.internal.editor.autocomplete;

import java.util.*;

public final class TagAttributeRegistry {

    private static final Map<String, List<AttributeSpec>> registry = new LinkedHashMap<>();

    private TagAttributeRegistry() {}

    public static void register(String tagName, AttributeSpec... specs) {
        registry.computeIfAbsent(tagName, k -> new ArrayList<>())
            .addAll(Arrays.asList(specs));
    }

    public static List<AttributeSpec> get(String tagName) {
        return Collections.unmodifiableList(
            registry.getOrDefault(tagName, Collections.emptyList()));
    }

    public static Set<String> getRegisteredTags() {
        return Collections.unmodifiableSet(registry.keySet());
    }

    /** Populate the registry with all known tag→attribute mappings. */
    public static void initialize() {
        // Inline/Flow tags
        register("ItemImage",
            new AttributeSpec("id", AttrType.ITEM_ID),
            new AttributeSpec("ore", AttrType.ORE_DICT),
            new AttributeSpec("scale", AttrType.FLOAT),
            new AttributeSpec("yOffset", AttrType.INT),
            new AttributeSpec("labelYOffset", AttrType.INT),
            new AttributeSpec("noTooltip", AttrType.BOOLEAN),
            new AttributeSpec("showTooltip", AttrType.BOOLEAN),
            new AttributeSpec("showIcon", AttrType.STRING),
            new AttributeSpec("label", AttrType.STRING),
            new AttributeSpec("format", AttrType.FORMAT_PATTERN));
        register("ItemLink",
            new AttributeSpec("id", AttrType.ITEM_ID),
            new AttributeSpec("ore", AttrType.ORE_DICT),
            new AttributeSpec("noTooltip", AttrType.BOOLEAN),
            new AttributeSpec("showTooltip", AttrType.BOOLEAN),
            new AttributeSpec("showIcon", AttrType.STRING),
            new AttributeSpec("linksTo", AttrType.PAGE_PATH));
        register("BlockImage",
            new AttributeSpec("id", AttrType.BLOCK_ID),
            new AttributeSpec("ore", AttrType.ORE_DICT),
            new AttributeSpec("scale", AttrType.FLOAT),
            new AttributeSpec("wrap", AttrType.STRING),
            new AttributeSpec("align", AttrType.STRING),
            new AttributeSpec("float", AttrType.STRING));
        register("FloatingImage",
            new AttributeSpec("src", AttrType.FILE_PATH),
            new AttributeSpec("align", AttrType.STRING),
            new AttributeSpec("title", AttrType.STRING),
            new AttributeSpec("width", AttrType.INT),
            new AttributeSpec("height", AttrType.INT));
        register("Color",
            new AttributeSpec("id", AttrType.COLOR),
            new AttributeSpec("color", AttrType.COLOR));
        register("Entity",
            new AttributeSpec("id", AttrType.ENTITY_ID),
            new AttributeSpec("data", AttrType.SNBT),
            new AttributeSpec("name", AttrType.STRING),
            new AttributeSpec("uuid", AttrType.STRING),
            new AttributeSpec("showName", AttrType.BOOLEAN),
            new AttributeSpec("showCape", AttrType.BOOLEAN),
            new AttributeSpec("baby", AttrType.BOOLEAN),
            new AttributeSpec("x", AttrType.FLOAT),
            new AttributeSpec("y", AttrType.FLOAT),
            new AttributeSpec("z", AttrType.FLOAT));
        register("KeyBind",
            new AttributeSpec("id", AttrType.KEY_BIND),
            new AttributeSpec("action", AttrType.STRING));
        register("CommandLink",
            new AttributeSpec("command", AttrType.COMMAND),
            new AttributeSpec("close", AttrType.BOOLEAN),
            new AttributeSpec("title", AttrType.STRING));
        register("Recipe",
            new AttributeSpec("id", AttrType.ITEM_ID),
            new AttributeSpec("fallbackText", AttrType.STRING),
            new AttributeSpec("handlerName", AttrType.STRING),
            new AttributeSpec("handlerId", AttrType.STRING),
            new AttributeSpec("handlerOrder", AttrType.INT),
            new AttributeSpec("input", AttrType.STRING),
            new AttributeSpec("output", AttrType.STRING),
            new AttributeSpec("limit", AttrType.INT));
        register("RecipeFor",
            new AttributeSpec("id", AttrType.ITEM_ID),
            new AttributeSpec("fallbackText", AttrType.STRING),
            new AttributeSpec("handlerName", AttrType.STRING),
            new AttributeSpec("handlerId", AttrType.STRING),
            new AttributeSpec("handlerOrder", AttrType.INT),
            new AttributeSpec("input", AttrType.STRING),
            new AttributeSpec("output", AttrType.STRING),
            new AttributeSpec("limit", AttrType.INT));
        register("RecipesFor",
            new AttributeSpec("id", AttrType.ITEM_ID),
            new AttributeSpec("fallbackText", AttrType.STRING),
            new AttributeSpec("handlerName", AttrType.STRING),
            new AttributeSpec("handlerId", AttrType.STRING),
            new AttributeSpec("handlerOrder", AttrType.INT),
            new AttributeSpec("input", AttrType.STRING),
            new AttributeSpec("output", AttrType.STRING),
            new AttributeSpec("limit", AttrType.INT));
        register("SubPages",
            new AttributeSpec("id", AttrType.PAGE_PATH),
            new AttributeSpec("alphabetical", AttrType.BOOLEAN));
        register("CategoryIndex",
            new AttributeSpec("category", AttrType.STRING));
        register("Structure",
            new AttributeSpec("width", AttrType.INT),
            new AttributeSpec("height", AttrType.INT));
        register("GameScene",
            new AttributeSpec("width", AttrType.INT),
            new AttributeSpec("height", AttrType.INT),
            new AttributeSpec("zoom", AttrType.FLOAT),
            new AttributeSpec("perspective", AttrType.STRING),
            new AttributeSpec("interactive", AttrType.BOOLEAN),
            new AttributeSpec("showGrid", AttrType.BOOLEAN));
        register("Mermaid",
            new AttributeSpec("src", AttrType.FILE_PATH),
            new AttributeSpec("width", AttrType.INT),
            new AttributeSpec("height", AttrType.INT));
        register("CsvTable",
            new AttributeSpec("src", AttrType.FILE_PATH),
            new AttributeSpec("header", AttrType.BOOLEAN),
            new AttributeSpec("widths", AttrType.STRING));
        register("details",
            new AttributeSpec("open", AttrType.BOOLEAN));
        register("Row",
            new AttributeSpec("gap", AttrType.INT),
            new AttributeSpec("alignItems", AttrType.STRING),
            new AttributeSpec("fullWidth", AttrType.BOOLEAN),
            new AttributeSpec("width", AttrType.INT));
        register("Column",
            new AttributeSpec("gap", AttrType.INT),
            new AttributeSpec("alignItems", AttrType.STRING),
            new AttributeSpec("fullWidth", AttrType.BOOLEAN),
            new AttributeSpec("width", AttrType.INT));
        register("a",
            new AttributeSpec("name", AttrType.STRING),
            new AttributeSpec("href", AttrType.PAGE_PATH),
            new AttributeSpec("title", AttrType.STRING));
        register("br",
            new AttributeSpec("clear", AttrType.STRING));
        register("ImportStructure",
            new AttributeSpec("src", AttrType.FILE_PATH),
            new AttributeSpec("offsetX", AttrType.FLOAT),
            new AttributeSpec("offsetY", AttrType.FLOAT),
            new AttributeSpec("offsetZ", AttrType.FLOAT));
        register("ImportStructureLib",
            new AttributeSpec("controller", AttrType.STRING),
            new AttributeSpec("piece", AttrType.STRING),
            new AttributeSpec("channel", AttrType.STRING),
            new AttributeSpec("facing", AttrType.STRING),
            new AttributeSpec("rotation", AttrType.STRING),
            new AttributeSpec("flip", AttrType.STRING));
        register("ImportPonder",
            new AttributeSpec("src", AttrType.FILE_PATH));
        register("PlaceBlock",
            new AttributeSpec("id", AttrType.BLOCK_ID),
            new AttributeSpec("nbt", AttrType.SNBT),
            new AttributeSpec("x", AttrType.INT),
            new AttributeSpec("y", AttrType.INT),
            new AttributeSpec("z", AttrType.INT));
        register("RemoveBlocks",
            new AttributeSpec("id", AttrType.BLOCK_ID));
        register("ReplaceBlock",
            new AttributeSpec("from", AttrType.BLOCK_ID),
            new AttributeSpec("to", AttrType.BLOCK_ID),
            new AttributeSpec("from_nbt", AttrType.SNBT),
            new AttributeSpec("to_nbt", AttrType.SNBT));
        register("IsometricCamera",
            new AttributeSpec("yaw", AttrType.FLOAT),
            new AttributeSpec("pitch", AttrType.FLOAT),
            new AttributeSpec("roll", AttrType.FLOAT));
        register("Function",
            new AttributeSpec("expr", AttrType.EXPRESSION),
            new AttributeSpec("inverse", AttrType.BOOLEAN),
            new AttributeSpec("domain", AttrType.DOMAIN),
            new AttributeSpec("color", AttrType.COLOR),
            new AttributeSpec("label", AttrType.STRING));
        register("FunctionGraph",
            new AttributeSpec("title", AttrType.STRING),
            new AttributeSpec("width", AttrType.INT),
            new AttributeSpec("height", AttrType.INT),
            new AttributeSpec("background", AttrType.COLOR),
            new AttributeSpec("border", AttrType.COLOR),
            new AttributeSpec("xMin", AttrType.FLOAT),
            new AttributeSpec("xMax", AttrType.FLOAT),
            new AttributeSpec("yMin", AttrType.FLOAT),
            new AttributeSpec("yMax", AttrType.FLOAT));
        register("Tooltip",
            new AttributeSpec("label", AttrType.STRING));
        register("Latex",
            new AttributeSpec("formula", AttrType.STRING),
            new AttributeSpec("color", AttrType.COLOR),
            new AttributeSpec("scale", AttrType.FLOAT),
            new AttributeSpec("valign", AttrType.STRING));
        register("mark",
            new AttributeSpec("color", AttrType.COLOR));
        register("FileTree",
            new AttributeSpec("indent", AttrType.INT),
            new AttributeSpec("gap", AttrType.INT));
        register("ItemGrid"); // no attributes - uses child elements
        register("FootnoteList",
            new AttributeSpec("width", AttrType.INT));
    }
}
