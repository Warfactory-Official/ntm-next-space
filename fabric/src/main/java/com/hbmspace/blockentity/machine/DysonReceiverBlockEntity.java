/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2024 George Paton
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2024 George Paton. All rights reserved.
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

package com.hbmspace.blockentity.machine;

import com.hbm.blockentity.AudioLoop;
import com.hbm.blockentity.MachineBlockEntity;
import com.hbm.blockentity.SoundEmitter;
import com.hbm.blockentity.machine.storage.CrateTungstenBlockEntity;
import com.hbm.blocks.multiblock.MultiblockCoreBlock;
import com.hbm.blocks.multiblock.MultiblockSurface;
import com.hbm.explosion.vanillant.ComposableExplosion;
import com.hbm.explosion.vanillant.standard.AttenuatedRayBlockSelector;
import com.hbm.explosion.vanillant.standard.BlockMutatorFire;
import com.hbm.explosion.vanillant.standard.CellDestruction;
import com.hbm.explosion.vanillant.standard.ExplosionEffectStandard;
import com.hbm.explosion.vanillant.standard.SinglePointExplosionEntityProcessor;
import com.hbm.items.SatelliteFrequencyProvider;
import com.hbm.packet.SyncUnitSchema;
import com.hbm.saveddata.SatelliteSavedData;
import com.hbm.saveddata.satellites.Satellite;
import com.hbm.sound.AudioSystem;
import com.hbm.sound.SoundHandle;
import com.hbm.util.PositionWatchers;
import com.hbmspace.blockentity.DysonEnergyHandler;
import com.hbmspace.blocks.SkyBlockEntitiesSpace;
import com.hbmspace.dim.trait.CBT_Dyson;
import com.hbmspace.explosion.vanillant.standard.CustomDamageHandlerDyson;
import com.hbmspace.particle.FlareParticleOptions;
import com.hbmspace.saveddata.satellites.SatelliteDysonRelay;
import com.hbmspace.sound.SkySoundsSpace;
import io.netty.buffer.ByteBuf;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;

public class DysonReceiverBlockEntity extends MachineBlockEntity
        implements SoundEmitter, SyncUnitSchema, AudioLoop {

    private static final int MAX_LENGTH = 24;
    private static final int TOWER_TOP = 18;
    private static final int SHADE_REACH = 3;
    private static final int SHADE_FLOOR = 10;

    private static final PositionWatchers<DysonReceiverBlockEntity> COLUMNS =
            new PositionWatchers<>();

    public boolean isReceiving;
    public int swarmId;
    public int swarmCount;
    public int swarmConsumers;
    public int beamLength;

    private boolean shaded;
    private boolean shadeKnown;

    public DysonReceiverBlockEntity(BlockPos pos, BlockState state) {
        super(SkyBlockEntitiesSpace.DYSON_RECEIVER.get(), pos, state, 1);
    }

    public static long getEnergyOutput(int swarmCount) {
        double adjustedDensity = (double) swarmCount / 1024.0D;
        long maxOutput = Long.MAX_VALUE / 10;
        double b = 32.0D;
        double c = 1.3D;
        double gompertz = Math.exp(-b * Math.exp(-c * adjustedDensity));
        return (long) (maxOutput * gompertz) / 20;
    }

    @Override
    public void onGraphLoad(ServerLevel server) {
        hbm$syncSet$DysonReceiverBlockEntity$swarmId(
                SatelliteFrequencyProvider.frequency(getItem(0)));
        watchColumns(server, true);
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (level instanceof ServerLevel)
            hbm$syncSet$DysonReceiverBlockEntity$swarmId(
                    SatelliteFrequencyProvider.frequency(getItem(0)));
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level instanceof ServerLevel server) watchColumns(server, false);

        com$hbm$blockentity$AudioLoop$setRemoved();
    }

    private void watchColumns(ServerLevel server, boolean watch) {
        for (int dx = -SHADE_REACH; dx <= SHADE_REACH; dx++) {
            for (int dz = -SHADE_REACH; dz <= SHADE_REACH; dz++) {
                long column = column(worldPosition.getX() + dx, worldPosition.getZ() + dz);
                if (watch) COLUMNS.add(server, column, this);
                else COLUMNS.remove(server, column, this);
            }
        }
    }

    public static void blockChanged(
            ServerLevel level, BlockPos pos, BlockState oldState, BlockState newState) {
        List<DysonReceiverBlockEntity> receivers =
                COLUMNS.at(level, column(pos.getX(), pos.getZ()));
        if (receivers.isEmpty() || shades(oldState) == shades(newState)) return;
        for (DysonReceiverBlockEntity receiver : receivers) {
            if (pos.getY() >= receiver.getBlockPos().getY() + SHADE_FLOOR)
                receiver.shadeKnown = false;
        }
    }

    private static long column(int x, int z) {
        return BlockPos.asLong(x, 0, z);
    }

    @Override
    public void tickServer() {
        ServerLevel level = (ServerLevel) this.level;
        Direction dir = MultiblockCoreBlock.coreFacing(getBlockState()).getOpposite();

        Satellite sat =
                SatelliteSavedData.at(level, worldPosition.getX(), worldPosition.getZ())
                        .getSatFromFreq(swarmId);
        int sun =
                level.getBrightness(LightLayer.SKY, worldPosition.above(TOWER_TOP))
                        - level.getSkyDarken()
                        - 11;

        hbm$syncSet$DysonReceiverBlockEntity$swarmCount(CBT_Dyson.count(level, swarmId));
        hbm$syncSet$DysonReceiverBlockEntity$swarmConsumers(CBT_Dyson.consumers(level, swarmId));

        hbm$syncSet$DysonReceiverBlockEntity$isReceiving(
                (sat instanceof SatelliteDysonRelay || sun > 0)
                        && swarmId > 0
                        && !shaded(level)
                        && swarmCount > 0
                        && swarmConsumers > 0);

        if (isReceiving) {
            long energyOutput = getEnergyOutput(swarmCount) / swarmConsumers;
            fire(level, dir, energyOutput);
        }

        networkPackNT(250);
    }

    @Override
    public void tickClient() {
        audioLoop(isReceiving, 0.75F, 0.85F);
        if (!isReceiving) return;

        RandomSource random = level.getRandom();
        if (random.nextInt(10) != 0) return;
        double x = worldPosition.getX() - 5 + random.nextDouble() * 10;
        double z = worldPosition.getZ() - 5 + random.nextDouble() * 10;
        double yd = 0.1 + random.nextFloat() * 0.1;
        float scale = 4F + random.nextFloat() * 2;
        level.addParticle(
                new FlareParticleOptions(scale),
                true,
                false,
                x,
                worldPosition.getY() + 11,
                z,
                0,
                yd,
                0);
    }

    @Override
    public SoundHandle createAudioLoop() {
        Direction dir = MultiblockCoreBlock.coreFacing(getBlockState()).getOpposite();
        return AudioSystem.getLoopedSound(
                SkySoundsSpace.DYSON_BEAM.get(),
                SoundSource.BLOCKS,
                worldPosition.getX() + dir.getStepX() * 8,
                worldPosition.getY(),
                worldPosition.getZ() + dir.getStepZ() * 8,
                0.75F,
                20F,
                1.0F,
                20);
    }

    private void fire(ServerLevel level, Direction dir, long energyOutput) {
        hbm$syncSet$DysonReceiverBlockEntity$beamLength(MAX_LENGTH);
        for (int i = 9; i < MAX_LENGTH; i++) {
            BlockPos pos = worldPosition.relative(dir, i).above();
            BlockState state = level.getBlockState(pos);

            boolean detonate = true;
            BlockEntity be = null;
            if (i > 10) {
                BlockPos core = MultiblockSurface.coreOfAny(level, pos, state);
                be = level.getBlockEntity(core != null ? core : pos);

                if (be instanceof DysonEnergyHandler converter) {
                    detonate = !converter.provideEnergy(pos, energyOutput);
                } else if (be instanceof CrateTungstenBlockEntity crate) {
                    crate.receiveLaserEnergy(level, pos, energyOutput / 5000, Direction.NORTH);
                    detonate = false;
                }
            }

            if (state.isSolidRender() || be != null) {
                if (detonate) {
                    level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());

                    ComposableExplosion vnt =
                            new ComposableExplosion(level, pos.getX(), pos.getY(), pos.getZ(), 3);
                    vnt.blockSelector(new AttenuatedRayBlockSelector());
                    vnt.blockProcessor(CellDestruction.decay().transform(new BlockMutatorFire()));
                    vnt.entityProcessor(new SinglePointExplosionEntityProcessor().selfDamage());
                    vnt.effects(new ExplosionEffectStandard());
                    vnt.explode();
                }

                hbm$syncSet$DysonReceiverBlockEntity$beamLength(i);
                break;
            }
        }

        int x = worldPosition.getX();
        int y = worldPosition.getY();
        int z = worldPosition.getZ();
        AABB beam =
                new AABB(
                        Math.min(x, x + dir.getStepX() * beamLength) + 0.2,
                        y + 0.2,
                        Math.min(z, z + dir.getStepZ() * beamLength) + 0.2,
                        Math.max(x, x + dir.getStepX() * beamLength) + 0.8,
                        y + 1.8,
                        Math.max(z, z + dir.getStepZ() * beamLength) + 0.8);

        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, beam)) {
            ComposableExplosion vnt =
                    new ComposableExplosion(
                            level,
                            entity.getX() - dir.getStepX(),
                            entity.getY() + 1.5,
                            entity.getZ() - dir.getStepZ(),
                            3);
            vnt.blockSelector(new AttenuatedRayBlockSelector());
            vnt.blockProcessor(CellDestruction.decay().transform(new BlockMutatorFire()));
            vnt.entityProcessor(
                    new SinglePointExplosionEntityProcessor()
                            .hook(new CustomDamageHandlerDyson(energyOutput)));
            vnt.effects(new ExplosionEffectStandard());
            vnt.explode();
        }
    }

    private boolean shaded(ServerLevel level) {
        if (!shadeKnown) {
            shaded = occluded(level);
            shadeKnown = true;
        }
        return shaded;
    }

    private boolean occluded(ServerLevel level) {
        int floor = worldPosition.getY() + SHADE_FLOOR;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dx = -SHADE_REACH; dx <= SHADE_REACH; dx++) {
            for (int dz = -SHADE_REACH; dz <= SHADE_REACH; dz++) {
                int x = worldPosition.getX() + dx;
                int z = worldPosition.getZ() + dz;
                for (int y = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z) - 1;
                        y >= floor;
                        y--) {
                    if (shades(level.getBlockState(pos.set(x, y, z)))) return true;
                }
            }
        }
        return false;
    }

    private static boolean shades(BlockState state) {
        return state.getLightDampening() != 0
                && !(state.getBlock() instanceof MultiblockCoreBlock)
                && !MultiblockCoreBlock.isCell(state);
    }

    @Override
    public long syncUnitMask() {
        return 1L;
    }

    @Override
    public void writeSyncUnit(int unit, ByteBuf output) {
        if (unit != 0) throw new IllegalArgumentException();
        output.writeBoolean(isReceiving);
        output.writeInt(swarmId);
        output.writeInt(swarmCount);
        output.writeInt(swarmConsumers);
        output.writeInt(beamLength);
    }

    @Override
    public void readSyncUnit(int unit, ByteBuf input) {
        if (unit != 0) throw new IllegalArgumentException();
        hbm$syncSet$DysonReceiverBlockEntity$isReceiving(input.readBoolean());
        hbm$syncSet$DysonReceiverBlockEntity$swarmId(input.readInt());
        hbm$syncSet$DysonReceiverBlockEntity$swarmCount(input.readInt());
        hbm$syncSet$DysonReceiverBlockEntity$swarmConsumers(input.readInt());
        hbm$syncSet$DysonReceiverBlockEntity$beamLength(input.readInt());
    }

    @Override
    public boolean tickSound() {
        return isReceiving;
    }

    public final void hbm$syncSet$DysonReceiverBlockEntity$isReceiving(boolean next) {
        if (this.isReceiving != next) {
            this.isReceiving = next;
            if ((1L) == 0L) syncChanged(3);
            else syncUnitsChanged(3, 1L);
        }
    }

    public final void hbm$syncSet$DysonReceiverBlockEntity$swarmId(int next) {
        if (this.swarmId != next) {
            this.swarmId = next;
            if ((1L) == 0L) syncChanged(3);
            else syncUnitsChanged(3, 1L);
        }
    }

    public final void hbm$syncSet$DysonReceiverBlockEntity$swarmCount(int next) {
        if (this.swarmCount != next) {
            this.swarmCount = next;
            if ((1L) == 0L) syncChanged(3);
            else syncUnitsChanged(3, 1L);
        }
    }

    public final void hbm$syncSet$DysonReceiverBlockEntity$swarmConsumers(int next) {
        if (this.swarmConsumers != next) {
            this.swarmConsumers = next;
            if ((1L) == 0L) syncChanged(3);
            else syncUnitsChanged(3, 1L);
        }
    }

    public final void hbm$syncSet$DysonReceiverBlockEntity$beamLength(int next) {
        if (this.beamLength != next) {
            this.beamLength = next;
            if ((1L) == 0L) syncChanged(3);
            else syncUnitsChanged(3, 1L);
        }
    }

    private boolean hbm$soundActive;

    public boolean soundStarted(boolean active) {
        boolean started = active && !hbm$soundActive;
        hbm$soundActive = active;
        return started;
    }

    private SoundHandle hbm$audio;

    public void audioLoop(boolean running, float volume) {
        hbm$audio = AudioLoop.loop(this, hbm$audio, running, volume, 0F, false);
    }

    public void audioLoop(boolean running, float volume, float pitch) {
        hbm$audio = AudioLoop.loop(this, hbm$audio, running, volume, pitch, true);
    }

    private void hbm$audioRemove() {
        audioLoop(false, 0F);
    }

    private void com$hbm$blockentity$AudioLoop$setRemoved() {
        hbm$audioRemove();
    }
}
