package com.hfstudio.guidenh.guide.scene.level;

import net.minecraft.block.Block;
import net.minecraft.world.chunk.Chunk;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuidebookFakeChunk extends Chunk {

    public GuidebookLevel level;

    public GuidebookFakeChunk(GuidebookFakeWorld world, int chunkX, int chunkZ) {
        super(world, chunkX, chunkZ);
        this.level = world.getGuidebookLevel();
    }

    @Override
    public Block getBlock(int x, int y, int z) {
        return level.getBlock(xPosition * 16 + x, y, zPosition * 16 + z);
    }

    @Override
    public int getBlockMetadata(int x, int y, int z) {
        return level.getBlockMetadata(xPosition * 16 + x, y, zPosition * 16 + z);
    }
}
