package com.hfstudio.guidenh.integration.ae2;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

import org.jetbrains.annotations.Nullable;

import appeng.items.parts.ItemMultiPart;
import appeng.items.parts.PartType;
import cpw.mods.fml.common.Optional;

public class Ae2PonderSupport {

    private static final String CABLE_BUS_TILE_ID = "BlockCableBus";
    private static final String CABLE_BUS_BLOCK_ID = "appliedenergistics2:tile.BlockCableBus";

    private Ae2PonderSupport() {}

    @Optional.Method(modid = "appliedenergistics2")
    @Nullable
    public static Block resolvePonderBlock(@Nullable String name, @Nullable NBTTagCompound tileTag, int itemDamage) {
        if (isCableBusTileTag(tileTag) || isCablePartItem(name, itemDamage)) {
            return getCableBusBlock();
        }
        return null;
    }

    @Optional.Method(modid = "appliedenergistics2")
    @Nullable
    public static NBTTagCompound createCableBusTileTag(@Nullable String itemName, int itemDamage) {
        if (!isCablePartItem(itemName, itemDamage)) {
            return null;
        }
        Item item = (Item) Item.itemRegistry.getObject(itemName);
        if (!(item instanceof ItemMultiPart)) {
            return null;
        }

        int cableSide = ForgeDirection.UNKNOWN.ordinal();
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("id", CABLE_BUS_TILE_ID);
        tag.setInteger("hasRedstone", 2);
        tag.setTag("def:" + cableSide, new ItemStack(item, 1, itemDamage).writeToNBT(new NBTTagCompound()));
        tag.setTag("extra:" + cableSide, new NBTTagCompound());
        return tag;
    }

    @Optional.Method(modid = "appliedenergistics2")
    @Nullable
    public static NBTTagCompound normalizePonderTileTag(@Nullable NBTTagCompound tileTag) {
        if (!isCableBusTileTag(tileTag)) {
            return tileTag;
        }

        NBTTagCompound normalized = new NBTTagCompound();
        ArrayList<String> keys = new ArrayList<>(tileTag.func_150296_c());
        for (String key : keys) {
            String normalizedKey = normalizeCableBusPartKey(key);
            if (!normalizedKey.equals(key) && tileTag.hasKey(normalizedKey)) {
                continue;
            }
            NBTBase value = tileTag.getTag(key);
            if (value != null) {
                normalized.setTag(normalizedKey, value.copy());
            }
        }
        return normalized;
    }

    @Optional.Method(modid = "appliedenergistics2")
    public static int resolvePonderBlockMeta(@Nullable Block block, int requestedMeta) {
        return isCableBusBlock(block) ? 0 : requestedMeta;
    }

    @Optional.Method(modid = "appliedenergistics2")
    public static boolean isCableBusBlock(@Nullable Block block) {
        Block cableBusBlock = getCableBusBlock();
        return block != null && block != Blocks.air && block == cableBusBlock;
    }

    @Optional.Method(modid = "appliedenergistics2")
    private static boolean isCablePartItem(@Nullable String itemName, int itemDamage) {
        if (itemName == null || itemName.isEmpty()) {
            return false;
        }
        Item item = (Item) Item.itemRegistry.getObject(itemName);
        if (!(item instanceof ItemMultiPart multiPart)) {
            return false;
        }
        PartType partType = multiPart.getTypeByStack(new ItemStack(item, 1, itemDamage));
        return partType != null && partType.isCable();
    }

    @Optional.Method(modid = "appliedenergistics2")
    private static boolean isCableBusTileTag(@Nullable NBTTagCompound tileTag) {
        return tileTag != null && CABLE_BUS_TILE_ID.equals(tileTag.getString("id"));
    }

    private static String normalizeCableBusPartKey(String key) {
        if (key == null) {
            return "";
        }
        String normalized = normalizeCableBusPartKey(key, "def:");
        if (!normalized.equals(key)) {
            return normalized;
        }
        normalized = normalizeCableBusPartKey(key, "extra:");
        if (!normalized.equals(key)) {
            return normalized;
        }
        return normalizeCableBusPartKey(key, "facade:");
    }

    private static String normalizeCableBusPartKey(String key, String prefix) {
        if (!key.startsWith(prefix)) {
            return key;
        }
        String suffix = key.substring(prefix.length())
            .trim();
        if (suffix.isEmpty()) {
            return key;
        }
        for (int index = 0; index < suffix.length(); index++) {
            if (!Character.isDigit(suffix.charAt(index))) {
                return key;
            }
        }
        return prefix + suffix;
    }

    @Optional.Method(modid = "appliedenergistics2")
    @Nullable
    private static Block getCableBusBlock() {
        Block block = (Block) Block.blockRegistry.getObject(CABLE_BUS_BLOCK_ID);
        return block != null && block != Blocks.air ? block : null;
    }
}
