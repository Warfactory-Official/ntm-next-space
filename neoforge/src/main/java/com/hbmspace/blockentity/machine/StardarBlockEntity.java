/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2026 George Paton
 * SPDX-FileCopyrightText: 2026 detree05
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2026 George Paton. All rights reserved.
 * Copyright (C) 2026 detree05. All rights reserved.
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

import com.hbm.blockentity.MachineBlockEntity;
import com.hbm.packet.SyncUnitSchema;
import com.hbm.util.ChunkUtil;
import com.hbmspace.blocks.RocketryBlocksSpace;
import com.hbmspace.dim.CelestialBody;
import com.hbmspace.dim.SolarSystem;
import com.hbmspace.dim.SolarSystemWorldSavedData;
import com.hbmspace.dim.SpaceDimensions;
import com.hbmspace.dim.orbit.OrbitalStation;
import com.hbmspace.inventory.container.StardarMenu;
import com.hbmspace.items.VOTVdriveItem.Destination;
import com.hbmspace.items.VOTVdriveItem;
import com.hbmspace.items.weapon.MissilePartsSpace;
import io.netty.buffer.ByteBuf;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ChunkResult;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class StardarBlockEntity extends MachineBlockEntity implements MenuProvider, SyncUnitSchema {

    public static final int MAP_SIZE = 256;
    private static final float MAX_SPEED_YAW = 0.5F;

    private static long pointAtTime;
    private static float pointYaw;
    private static float pointPitch;

    public float targetYaw;
    public float targetPitch;
    public boolean radarMode;

    public float dishYaw;
    public float dishPitch;
    public float prevDishYaw;
    public float prevDishPitch;

    public int @Nullable [] heightmap;
    public boolean updateHeightmap;
    private @Nullable Destination mapped;
    private @Nullable PendingMap pending;

    public StardarBlockEntity(BlockPos pos, BlockState state) {
        super(RocketryBlocksSpace.STARDAR_BE.get(), pos, state, 1);
    }

    @Override
    public void tickServer() {
        if (level.getGameTime() >= pointAtTime) repoint();
        if (targetYaw != pointYaw) hbm$syncSet$StardarBlockEntity$targetYaw(pointYaw);
        if (targetPitch != pointPitch) hbm$syncSet$StardarBlockEntity$targetPitch(pointPitch);

        ItemStack drive = getItem(0);
        if (drive.getItem() instanceof VOTVdriveItem) {
            Destination approx = VOTVdriveItem.getApproximateDestination(drive, level.getRandom());
            if (!approx.equals(mapped)) {
                cancelMap();
                heightmap = null;
                mapped = approx;
            }
            if (heightmap == null && pending == null) pending = startMap(approx);
            if (pending != null && pending.poll()) {
                heightmap = pending.heights;
                pending = null;
            }
        } else {
            cancelMap();
            mapped = null;
            heightmap = null;
        }

        networkPackNT(250);
    }

    private void repoint() {
        pointAtTime = level.getGameTime() + level.getRandom().nextInt(300) + 300;
        pointYaw = Mth.wrapDegrees(level.getRandom().nextFloat() * 360);
        pointPitch = level.getRandom().nextFloat() * 80;
    }

    private @Nullable PendingMap startMap(Destination approx) {
        CelestialBody body = approx.body().getBody();
        ServerLevel target = body == null ? null : body.getLevel(level.getServer());
        if (target == null) return null;
        long[] chunks = new long[256];
        for (int cz = 0; cz < 16; cz++) {
            for (int cx = 0; cx < 16; cx++) {
                chunks[cz * 16 + cx] =
                        ChunkPos.pack((approx.x() >> 4) + cx - 8, (approx.z() >> 4) + cz - 8);
                ChunkUtil.addBlastTicket(target, chunks[cz * 16 + cx]);
            }
        }
        return new PendingMap(target, chunks);
    }

    private void cancelMap() {
        if (pending == null) return;
        pending.release();
        pending = null;
    }

    @Override
    public void setRemoved() {
        if (level instanceof ServerLevel server) {
            cancelMap();
            showDrive(server, false);
        }
        super.setRemoved();
    }

    @Override
    public void onGraphLoad(ServerLevel server) {
        showDrive(server, !getItem(0).isEmpty());
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (level instanceof ServerLevel server) showDrive(server, !getItem(0).isEmpty());
    }

    private void showDrive(ServerLevel server, boolean drive) {
        for (DishControlBlockEntity console :
                DishControlBlockEntity.LINKED.at(server, worldPosition.asLong())) {
            console.showDisk(drive);
        }
    }

    private static final class PendingMap {
        final ServerLevel target;
        final long[] chunks;
        final int[] heights = new int[MAP_SIZE * MAP_SIZE];
        final boolean[] read = new boolean[256];
        int remaining = 256;

        PendingMap(ServerLevel target, long[] chunks) {
            this.target = target;
            this.chunks = chunks;
        }

        boolean poll() {
            for (int i = 0; i < 256; i++) {
                if (read[i]) continue;
                CompletableFuture<ChunkResult<LevelChunk>> future =
                        ChunkUtil.blastChunkFuture(target, chunks[i]);
                if (future == null || !future.isDone()) continue;
                LevelChunk chunk = future.join().orElse(null);
                if (chunk == null) continue;
                int originX = (i & 15) * 16;
                int originZ = (i >> 4) * 16;
                for (int z = 0; z < 16; z++) {
                    for (int x = 0; x < 16; x++) {
                        heights[(originZ + z) * MAP_SIZE + originX + x] =
                                chunk.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z) + 1;
                    }
                }
                read[i] = true;
                remaining--;
                ChunkUtil.releaseChunkAsync(target, chunks[i]);
            }
            return remaining == 0;
        }

        void release() {
            for (int i = 0; i < 256; i++) {
                if (!read[i]) ChunkUtil.releaseChunkAsync(target, chunks[i]);
            }
        }
    }

    public void receiveHeightmap(int @Nullable [] heights) {
        heightmap = heights;
        updateHeightmap = true;
    }

    @Override
    public void tickClient() {
        float yawOffset = Mth.wrapDegrees(targetYaw - dishYaw);
        float moveYaw = Mth.clamp(yawOffset, -MAX_SPEED_YAW, MAX_SPEED_YAW);

        float pitchOffset = targetPitch - dishPitch;
        float pitchSpeed =
                yawOffset == 0
                        ? Math.abs(pitchOffset)
                        : moveYaw / yawOffset * Math.abs(pitchOffset);
        float movePitch = Mth.clamp(pitchOffset, -pitchSpeed, pitchSpeed);

        prevDishYaw = dishYaw;
        prevDishPitch = dishPitch;
        dishYaw += moveYaw;
        dishPitch += movePitch;
    }

    private void processDrive(ResourceKey<Level> target, boolean here) {
        boolean orbit = target == SpaceDimensions.ORBIT;
        CelestialBody body = CelestialBody.getBodyOrNull(target);
        if (body == null && !orbit) return;
        if (!getItem(0).is(MissilePartsSpace.HARD_DRIVE.get())) return;
        SolarSystem.Body type = body != null ? body.getEnum() : SolarSystem.Body.ORBIT;
        if (type == null) return;

        ItemStack drive = MissilePartsSpace.HARD_DRIVE_FULL.stack(type);
        boolean inOrbit = CelestialBody.inOrbit(level);
        if (here && target == level.dimension() && !inOrbit) {
            VOTVdriveItem.setApproximateDestination(
                    drive, worldPosition.getX(), worldPosition.getZ());
            VOTVdriveItem.setProcessed(drive, true);
        } else if (orbit) {
            ChunkPos pos =
                    inOrbit
                            ? new ChunkPos(
                                    Mth.floor(
                                            (float) worldPosition.getX()
                                                    / OrbitalStation.STATION_SIZE),
                                    Mth.floor(
                                            (float) worldPosition.getZ()
                                                    / OrbitalStation.STATION_SIZE))
                            : SolarSystemWorldSavedData.get(level).findFreeSpace();
            VOTVdriveItem.setCoordinates(drive, pos.x(), pos.z());
            VOTVdriveItem.setProcessed(drive, true);
        }
        setItem(0, drive);

        repoint();
    }

    public void setTarget(Identifier target, boolean here) {
        processDrive(ResourceKey.create(Registries.DIMENSION, target), here);
    }

    public void updateDriveCoords(int x, int z) {
        ItemStack drive = getItem(0);
        if (!(drive.getItem() instanceof VOTVdriveItem)) return;
        if (heightmap == null || landingIssue(heightmap, x, z) != null) return;

        Destination approx = VOTVdriveItem.getApproximateDestination(drive, level.getRandom());
        VOTVdriveItem.setCoordinates(
                drive, (approx.x() >> 4 << 4) + x - 8 * 16, (approx.z() >> 4 << 4) + z - 8 * 16);
        setChanged();
    }

    public static @Nullable String landingIssue(int[] heightmap, int x, int z) {
        if (x < 3 || x > 252 || z < 3 || z > 252) return "gui.stardar.landing.outside_bounds";

        for (int ox = x - 2; ox <= x + 2; ox++) {
            for (int oz = z - 2; oz <= z + 2; oz++) {
                if (heightmap[MAP_SIZE * oz + ox] != heightmap[MAP_SIZE * z + x]) {
                    return "gui.stardar.landing.area_not_flat";
                }
            }
        }

        return null;
    }

    @Override
    protected double interactionRangeSq() {
        return Double.POSITIVE_INFINITY;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        hbm$syncSet$StardarBlockEntity$radarMode(input.getBooleanOr("radarmode", false));
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putBoolean("radarmode", radarMode);
    }

    @Override
    public Component getDisplayName() {
        return getName();
    }

    @Override
    public AbstractContainerMenu createMenu(
            int containerId, Inventory playerInventory, Player player) {
        return new StardarMenu(containerId, playerInventory, this);
    }

    @Override
    public long syncUnitMask() {
        return 0x1L;
    }

    @Override
    public void writeSyncUnit(int unit, ByteBuf output) {
        if (unit != 0) throw new IllegalArgumentException();
        output.writeFloat(targetYaw);
        output.writeFloat(targetPitch);
        output.writeBoolean(radarMode);
    }

    @Override
    public void readSyncUnit(int unit, ByteBuf input) {
        if (unit != 0) throw new IllegalArgumentException();
        hbm$syncSet$StardarBlockEntity$targetYaw(input.readFloat());
        hbm$syncSet$StardarBlockEntity$targetPitch(input.readFloat());
        hbm$syncSet$StardarBlockEntity$radarMode(input.readBoolean());
    }

    public final void hbm$syncSet$StardarBlockEntity$targetYaw(float next) {
        if (Float.floatToRawIntBits(this.targetYaw) != Float.floatToRawIntBits(next)) {
            this.targetYaw = next;
            if ((1L << 0) == 0L) syncChanged(3);
            else syncUnitsChanged(3, 1L << 0);
        }
    }

    public final void hbm$syncSet$StardarBlockEntity$targetPitch(float next) {
        if (Float.floatToRawIntBits(this.targetPitch) != Float.floatToRawIntBits(next)) {
            this.targetPitch = next;
            if ((1L << 0) == 0L) syncChanged(3);
            else syncUnitsChanged(3, 1L << 0);
        }
    }

    public final void hbm$syncSet$StardarBlockEntity$radarMode(boolean next) {
        if (this.radarMode != next) {
            this.radarMode = next;
            if ((1L << 0) == 0L) syncChanged(3);
            else syncUnitsChanged(3, 1L << 0);
        }
    }
}
