// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blocks.machine;

import com.hbm.blockentity.BlockEntityPlushie;
import com.hbm.blocks.generic.BlockPlushie.PlushieType;
import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.explosion.vanillant.standard.EntityProcessorCrossSmooth;
import com.hbm.explosion.vanillant.standard.ExplosionEffectWeapon;
import com.hbm.explosion.vanillant.standard.PlayerProcessorStandard;
import com.hbmspace.sound.ModSoundsSpace;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public final class PressCrushSpace {

    public static void play(
            Level level,
            @Nullable Entity except,
            BlockPos pos,
            SoundEvent ordinary,
            SoundSource source,
            float volume,
            float pitch) {
        SoundEvent sound = ordinary;
        if (level.getBlockEntity(pos.above()) instanceof BlockEntityPlushie plushie) {
            sound = ModSoundsSpace.BLOCK_SQUEAKY_PAIN.get();
            if (plushie.type == PlushieType.HUNDUN) {
                level.destroyBlock(pos, false);
                new ExplosionVNT(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 5F)
                        .setEntityProcessor(new EntityProcessorCrossSmooth(1D, 50F))
                        .setPlayerProcessor(new PlayerProcessorStandard())
                        .setSFX(new ExplosionEffectWeapon(10, 2.5F, 1F))
                        .explode();
            }
        }
        level.playSound(except, pos, sound, source, volume, pitch);
    }

    private PressCrushSpace() {}
}
