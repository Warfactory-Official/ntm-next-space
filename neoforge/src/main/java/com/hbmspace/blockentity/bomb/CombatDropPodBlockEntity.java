/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2026 George Paton
 * SPDX-FileCopyrightText: 2026 JamesH_2
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2026 George Paton. All rights reserved.
 * Copyright (C) 2026 JamesH_2. All rights reserved.
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

package com.hbmspace.blockentity.bomb;

import com.hbm.blockentity.BlockEntityTicks;
import com.hbm.sound.WorldSounds;
import com.hbmspace.blocks.RocketryBlocksSpace;
import com.hbmspace.sound.RocketrySoundsSpace;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntitySpawnRequest;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class CombatDropPodBlockEntity extends BlockEntity implements BlockEntityTicks {
    public static final int DESPAWN_TICKS = 1000;

    private @Nullable CompoundTag entityType;
    private int amount;
    public int color;
    private int delay = 40;

    public double hatchopen;
    public double hatchopen2;
    public double prevHatchopen;
    public double prevHatchopen2;

    private float @Nullable [] tilt;

    public CombatDropPodBlockEntity(BlockPos pos, BlockState state) {
        super(RocketryBlocksSpace.COMBAT_DROP_BE.get(), pos, state);
    }

    public void setPayload(CompoundTag entityType, int amount, int color) {
        this.entityType = entityType;
        this.amount = amount;
        this.color = color;
        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }

    @Override
    public void tickClient() {
        prevHatchopen = hatchopen;
        prevHatchopen2 = hatchopen2;

        if (delay > 0) {
            delay--;
            return;
        }

        hatchopen += (90 - hatchopen) * 0.2;
        hatchopen2 += (-90 - hatchopen2) * 0.2;
    }

    @Override
    public void tickServer() {
        var delayed = delay > 0;
        tickClient();
        if (delayed) {
            if (delay == 0)
                level.scheduleTick(worldPosition, getBlockState().getBlock(), DESPAWN_TICKS);
            return;
        }

        if (entityType != null && amount > 0) {
            for (int i = 0; i < amount; i++) {
                Entity entity =
                        EntityType.loadEntityRecursive(
                                entityType,
                                level,
                                new EntitySpawnRequest(EntitySpawnReason.TRIGGERED, false),
                                loaded -> {
                                    loaded.setUUID(Mth.createInsecureUUID(level.getRandom()));
                                    loaded.setPos(
                                            worldPosition.getX() + 0.5,
                                            worldPosition.getY() + 1,
                                            worldPosition.getZ() + 0.5);
                                    return loaded;
                                });
                if (entity != null) level.addFreshEntity(entity);
            }

            amount = 0;
            setChanged();
            WorldSounds.machine(
                    level,
                    null,
                    worldPosition,
                    RocketrySoundsSpace.HATCH_OPEN.get(),
                    SoundSource.BLOCKS,
                    10.0F,
                    0.9F);
        }
    }

    public float[] tilt() {
        if (tilt == null) {
            int x = worldPosition.getX(), y = worldPosition.getY(), z = worldPosition.getZ();
            Random rand = new Random((y + z * 27644437) * 27644437 + x);
            tilt =
                    new float[] {
                        (float) (rand.nextDouble() * 360),
                        (float) (rand.nextDouble() * 8),
                        (float) (rand.nextDouble() * 6)
                    };
        }
        return tilt;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        entityType = input.read("EntityType", CompoundTag.CODEC).orElse(null);
        amount = input.getIntOr("amount", 0);
        color = input.getIntOr("color", 0);
        delay = input.getIntOr("delay", 0);
        hatchopen = input.getDoubleOr("hatchopen", 0);
        hatchopen2 = input.getDoubleOr("hatchopen2", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (entityType != null) output.store("EntityType", CompoundTag.CODEC, entityType);
        output.putInt("amount", amount);
        output.putInt("color", color);
        output.putInt("delay", delay);
        output.putDouble("hatchopen", hatchopen);
        output.putDouble("hatchopen2", hatchopen2);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("color", color);
        tag.putInt("delay", delay);
        tag.putDouble("hatchopen", hatchopen);
        tag.putDouble("hatchopen2", hatchopen2);
        return tag;
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
