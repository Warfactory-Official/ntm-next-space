// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.mixin;

import com.hbm.blockentity.BlockEntityPlushie;
import com.hbm.blockentity.machine.BlockEntityMachinePress;
import com.hbmspace.blocks.machine.PressCrushSpace;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntityMachinePress.class)
public abstract class MixinBlockEntityMachinePress {

    @Shadow private double syncPress;

    @Inject(method = "tickClient", at = @At("TAIL"))
    private void hbmspace$plushieMisery(CallbackInfo ci) {
        BlockEntity press = (BlockEntity) (Object) this;
        if (press.getLevel().getBlockEntity(press.getBlockPos().above())
                instanceof BlockEntityPlushie plushie) {
            plushie.miseryFactor = Mth.clamp(((float) syncPress - 100F) / 100F, 0F, 1F);
        }
    }

    @Redirect(
            method = "tickServer",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/world/level/Level;"
                                            + "playSound(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/BlockPos;"
                                            + "Lnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V"))
    private void hbmspace$pressCrush(
            Level level,
            @Nullable Entity except,
            BlockPos pos,
            SoundEvent sound,
            SoundSource source,
            float volume,
            float pitch) {
        PressCrushSpace.play(level, except, pos, sound, source, volume, pitch);
    }
}
