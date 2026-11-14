/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2025-2026 George Paton
 * SPDX-FileCopyrightText: 2025 JamesH_2
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2025-2026 George Paton. All rights reserved.
 * Copyright (C) 2025 JamesH_2. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License version 3 only,
 * as published by the Free Software Foundation.
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this code. If not, see <https://www.gnu.org/licenses/>.
 *
 * Under GPLv3 §7(c) and §7(e), these additional terms apply to LGPL-covered
 * material authored by movblock:
 *
 * - the license grants no rights to the name "NTM NEXT: Space";
 * - a modified version must not be named NTM NEXT: Space or presented as
 *   the official release, and must be marked as different from it.
 *
 * Accurately identifying a work as derived from NTM NEXT: Space does not
 * violate these additional terms, provided it is clearly distinguished from
 * the official release and does not imply endorsement. Ports are encouraged
 * to acknowledge NTM NEXT: Space as their upstream project and provide a
 * link to it.
 */

package com.hbmspace.blocks.generic;

import com.hbm.blockentity.PlushieBlockEntity;
import com.hbm.blocks.generic.PlushieBlock.Plushie;
import com.hbm.sound.ModSounds;
import com.hbmspace.blocks.TrinketsSpace;
import com.hbmspace.items.ConsumableItemsSpace;
import com.hbmspace.sound.ModSoundsSpace;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public enum PlushieTypeSpace implements Plushie {
    TETO(
            "Kasane Teto",
            "please help I've been trapped in her basement for da-",
            ModSoundsSpace.BLOCK_TETO),
    MIKU("Hatsune Miku", "In your wifi, and your heart.", ModSoundsSpace.BLOCK_MIKU),
    NERU("Akita Neru", "Careful, she might electrocute you.", ModSoundsSpace.BLOCK_AKITA),
    FATO("FAT TETO", "pls don't bully me", ModSoundsSpace.BLOCK_TETO);

    private final String label;
    private final String inscription;
    private final Supplier<SoundEvent> sound;

    PlushieTypeSpace(String label, String inscription, Supplier<SoundEvent> sound) {
        this.label = label;
        this.inscription = inscription;
        this.sound = sound;
    }

    @Override
    public String label() {
        return label;
    }

    @Override
    public @Nullable String inscription() {
        return inscription;
    }

    @Override
    public ItemStack stack() {
        return TrinketsSpace.PLUSHIES.stack(this);
    }

    @Override
    public InteractionResult useItemOn(ItemStack held, PlushieBlockEntity plushie, Player player) {
        if (this != TETO || !held.is(ConsumableItemsSpace.BUTTER.get()))
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        Level level = plushie.getLevel();
        if (level.isClientSide()) {
            plushie.squish();
            return InteractionResult.SUCCESS;
        }
        plushie.hbm$syncSet$PlushieBlockEntity$type(FATO);
        plushie.setChanged();
        plushie.networkPackNTTracking();
        held.consume(1, player);
        level.playSound(
                null,
                plushie.getBlockPos(),
                ModSounds.PLAYER_GROAN.get(),
                SoundSource.PLAYERS,
                1.0F,
                2.0F);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void squeak(Level level, BlockPos pos) {
        level.playSound(
                null, pos, sound.get(), SoundSource.BLOCKS, 0.25F, this == FATO ? 0.5F : 1.0F);
    }
}
