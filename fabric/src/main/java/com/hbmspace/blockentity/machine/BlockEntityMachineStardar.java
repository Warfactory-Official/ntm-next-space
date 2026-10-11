// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blockentity.machine;

import com.hbm.api.control.IControlReceiver;
import com.hbm.blockentity.BlockEntityMachineBase;
import com.hbm.packet.PacketWire;
import com.hbm.packet.SyncField;
import com.hbm.packet.SyncUnitSchema;
import com.hbm.util.ChunkUtil;
import com.hbmspace.blocks.RocketryBlocksSpace;
import com.hbmspace.dim.CelestialBody;
import com.hbmspace.dim.SolarSystem;
import com.hbmspace.dim.SolarSystemWorldSavedData;
import com.hbmspace.dim.SpaceDimensions;
import com.hbmspace.dim.orbit.OrbitalStation;
import com.hbmspace.inventory.container.MenuMachineStardar;
import com.hbmspace.items.ItemVOTVdrive.Destination;
import com.hbmspace.items.ItemVOTVdrive;
import com.hbmspace.items.weapon.MissilePartsSpace;
import io.netty.buffer.ByteBuf;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ChunkResult;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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

public class BlockEntityMachineStardar extends BlockEntityMachineBase
        implements IControlReceiver, MenuProvider, SyncUnitSchema {

    public static final int MAP_SIZE = 256;
    private static final float MAX_SPEED_YAW = 0.5F;

    private static long pointAtTime;
    private static float pointYaw;
    private static float pointPitch;

    @SyncField(units = 1L << 0)
    public float targetYaw;

    @SyncField(units = 1L << 0)
    public float targetPitch;

    @SyncField(units = 1L << 0)
    public boolean radarMode;

    public float dishYaw;
    public float dishPitch;
    public float prevDishYaw;
    public float prevDishPitch;

    public int @Nullable [] heightmap;
    public boolean updateHeightmap;
    private @Nullable Destination mapped;
    private @Nullable PendingMap pending;

    public BlockEntityMachineStardar(BlockPos pos, BlockState state) {
        super(RocketryBlocksSpace.STARDAR_BE.get(), pos, state, 1);
    }

    @Override
    public void tickServer() {
        if (level.getGameTime() >= pointAtTime) repoint();
        if (targetYaw != pointYaw) targetYaw = pointYaw;
        if (targetPitch != pointPitch) targetPitch = pointPitch;

        ItemStack drive = getItem(0);
        if (drive.getItem() instanceof ItemVOTVdrive) {
            Destination approx = ItemVOTVdrive.getApproximateDestination(drive, level.getRandom());
            if (!approx.equals(mapped)) {
                cancelMap();
                if (heightmap != null) {
                    heightmap = null;
                    sendHeightmap();
                }
                mapped = approx;
            }
            if (heightmap == null && pending == null) pending = startMap(approx);
            if (pending != null && pending.poll()) {
                heightmap = pending.heights;
                pending = null;
                sendHeightmap();
            }
        } else {
            cancelMap();
            mapped = null;
            if (heightmap != null) {
                heightmap = null;
                sendHeightmap();
            }
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
        if (level instanceof ServerLevel) cancelMap();
        super.setRemoved();
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

    private void sendHeightmap() {
        for (ServerPlayer player : ((ServerLevel) level).players()) {
            if (player.containerMenu instanceof MenuMachineStardar menu
                    && menu.blockEntity() == this) {
                sendHeightmap(player);
            }
        }
    }

    public void sendHeightmap(ServerPlayer player) {
        PacketWire.sendTo(new StardarHeightmapPayload(worldPosition, heightmap), player);
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
            ItemVOTVdrive.setApproximateDestination(
                    drive, worldPosition.getX(), worldPosition.getZ());
            ItemVOTVdrive.setProcessed(drive, true);
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
            ItemVOTVdrive.setCoordinates(drive, pos.x(), pos.z());
            ItemVOTVdrive.setProcessed(drive, true);
        }
        setItem(0, drive);

        repoint();
    }

    private void updateDriveCoords(int x, int z) {
        ItemStack drive = getItem(0);
        if (!(drive.getItem() instanceof ItemVOTVdrive)) return;
        if (heightmap == null || landingIssue(heightmap, x, z) != null) return;

        Destination approx = ItemVOTVdrive.getApproximateDestination(drive, level.getRandom());
        ItemVOTVdrive.setCoordinates(
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
    public void receiveControl(CompoundTag data) {
        Identifier target = data.getString("pid").map(Identifier::tryParse).orElse(null);
        if (target != null) {
            processDrive(
                    ResourceKey.create(Registries.DIMENSION, target),
                    data.getBooleanOr("here", false));
        }
        if (data.contains("px") && data.contains("pz"))
            updateDriveCoords(data.getIntOr("px", 0), data.getIntOr("pz", 0));
        if (data.contains("radarmode")) radarMode = data.getBooleanOr("radarmode", false);
    }

    @Override
    protected double interactionRangeSq() {
        return Double.POSITIVE_INFINITY;
    }

    @Override
    public boolean hasPermission(Player player) {
        return stillValid(player);
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        radarMode = input.getBooleanOr("radarmode", false);
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
        if (player instanceof ServerPlayer server) sendHeightmap(server);
        return new MenuMachineStardar(containerId, playerInventory, this);
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
        targetYaw = input.readFloat();
        targetPitch = input.readFloat();
        radarMode = input.readBoolean();
    }
}
