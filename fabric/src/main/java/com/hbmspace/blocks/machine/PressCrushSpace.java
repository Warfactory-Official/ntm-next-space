/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file under the Apache License except in
 * compliance with that License. You may obtain a copy at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Apache License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License for the specific language governing permissions
 * and limitations under the License.
 */

package com.hbmspace.blocks.machine;

import com.hbm.blockentity.PlushieBlockEntity;
import com.hbm.blocks.generic.PlushieBlock.PlushieType;
import com.hbm.explosion.vanillant.ComposableExplosion;
import com.hbm.explosion.vanillant.standard.SmallExplosionEffect;
import com.hbm.explosion.vanillant.standard.WeaponExplosionEntityProcessor;
import com.hbm.sound.WorldSounds;
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
        if (level.getBlockEntity(pos.above()) instanceof PlushieBlockEntity plushie) {
            sound = ModSoundsSpace.BLOCK_SQUEAKY_PAIN.get();
            if (plushie.type == PlushieType.HUNDUN) {
                level.destroyBlock(pos, false);
                new ComposableExplosion(
                                level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 5F)
                        .entityProcessor(new WeaponExplosionEntityProcessor(1D, 50F))
                        .effects(new SmallExplosionEffect(10, 2.5F, 1F))
                        .explode();
            }
        }
        WorldSounds.machine(level, except, pos, sound, source, volume, pitch);
    }

    private PressCrushSpace() {}
}
