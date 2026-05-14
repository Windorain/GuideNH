package com.hfstudio.guidenh.guide.internal.structure;

import net.minecraft.client.Minecraft;

import com.hfstudio.guidenh.config.ModConfig;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public final class GuideNhStructureExportAccess {

    private GuideNhStructureExportAccess() {}

    @SideOnly(Side.CLIENT)
    public static boolean canUseSceneExport() {
        Minecraft minecraft = Minecraft.getMinecraft();
        boolean singlePlayer = minecraft != null && minecraft.isSingleplayer();
        return canUseSceneExport(singlePlayer, GuideNhStructureRuntime.isServerStructureCommandsAvailable());
    }

    public static boolean canUseSceneExport(boolean singlePlayer, boolean serverGuideNhInstalled) {
        if (singlePlayer) {
            return ModConfig.ui.sceneExportEnabled;
        }
        return serverGuideNhInstalled && ModConfig.ui.sceneExportEnabled;
    }
}
