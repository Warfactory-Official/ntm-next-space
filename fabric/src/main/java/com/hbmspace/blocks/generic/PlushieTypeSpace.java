// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blocks.generic;

import com.hbm.blockentity.BlockEntityPlushie;
import com.hbm.blocks.generic.BlockPlushie.Plushie;
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
    public InteractionResult useItemOn(ItemStack held, BlockEntityPlushie plushie, Player player) {
        if (this != TETO || !held.is(ConsumableItemsSpace.BUTTER.get()))
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        Level level = plushie.getLevel();
        if (level.isClientSide()) {
            plushie.squish();
            return InteractionResult.SUCCESS;
        }
        plushie.type = FATO;
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
