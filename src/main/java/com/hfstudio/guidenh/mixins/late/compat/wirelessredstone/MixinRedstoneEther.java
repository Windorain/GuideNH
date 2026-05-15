package com.hfstudio.guidenh.mixins.late.compat.wirelessredstone;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import codechicken.wirelessredstone.core.RedstoneEther;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

@Mixin(value = RedstoneEther.class, remap = false)
public abstract class MixinRedstoneEther {

    @Shadow
    protected Object2IntOpenHashMap<EntityLivingBase> jammedentities;

    @Inject(method = "isPlayerJammed", at = @At("HEAD"), cancellable = true, require = 0)
    private void guidenh$treatUnloadedEtherAsUnjammed(EntityPlayer player, CallbackInfoReturnable<Boolean> cir) {
        if (player == null || jammedentities == null) {
            cir.setReturnValue(false);
        }
    }
}
