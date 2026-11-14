/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2024-2025 George Paton
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2024-2025 George Paton. All rights reserved.
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

package com.hbmspace.handler.atmosphere;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.RadiationConfig;
import com.hbm.inventory.fluid.NTMFluids;
import com.hbm.inventory.fluid.trait.CorrosiveTrait;
import com.hbm.platform.Platform;
import com.hbmspace.dim.CelestialBody;
import com.hbmspace.dim.trait.CBT_Atmosphere;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import org.jspecify.annotations.Nullable;

public class ChunkAtmosphereHandler {

    private static final int MAX_BLOB_RADIUS = 256;

    public static final int TREE_GROWTH_CONVERSION = 400;
    public static final int CROP_GROWTH_CONVERSION = 15;
    public static final int GRASS_GROWTH_CONVERSION = 100;

    private final Map<ServerLevel, LevelAtmosphere> levels = new IdentityHashMap<>();

    private static final class LevelAtmosphere {
        final Map<AtmosphereProvider, AtmosphereBlob> blobs = new LinkedHashMap<>();
        final Queue<Growth> growths = new ArrayDeque<>();
        Long2ObjectLinkedOpenHashMap<BlockState> changes = new Long2ObjectLinkedOpenHashMap<>();
    }

    private LevelAtmosphere state(ServerLevel level) {
        return levels.computeIfAbsent(level, l -> new LevelAtmosphere());
    }

    private static Fluid earthAir() {
        return NTMFluids.byName("EARTHAIR");
    }

    public @Nullable CBT_Atmosphere getAtmosphere(Entity entity) {
        return getAtmosphere(
                (ServerLevel) entity.level(),
                BlockPos.containing(entity.getX(), entity.getEyeY(), entity.getZ()),
                null);
    }

    public @Nullable CBT_Atmosphere getAtmosphere(ServerLevel level, BlockPos pos) {
        return getAtmosphere(level, pos, null);
    }

    public @Nullable CBT_Atmosphere getAtmosphere(
            ServerLevel level, BlockPos pos, @Nullable AtmosphereBlob excludeBlob) {
        CBT_Atmosphere atmosphere = getCelestialAtmosphere(level);

        LevelAtmosphere state = levels.get(level);
        if (state != null) {
            for (AtmosphereBlob blob : state.blobs.values()) {
                if (blob == excludeBlob) continue;

                if (blob.contains(pos)) {
                    double pressure = blob.handler.getFluidPressure();
                    if (pressure < 0) {
                        atmosphere.reduce(pressure);
                    } else {
                        atmosphere.add(blob.handler.getFluidType(), pressure);
                    }
                }
            }
        }

        if (atmosphere.fluids.isEmpty()) return null;

        return atmosphere;
    }

    private static CBT_Atmosphere getCelestialAtmosphere(ServerLevel level) {
        if (CelestialBody.inOrbit(level)) return new CBT_Atmosphere();
        CBT_Atmosphere atmosphere = CelestialBody.getTrait(level, CBT_Atmosphere.class);
        if (atmosphere == null) return new CBT_Atmosphere();

        return atmosphere.clone();
    }

    public List<AtmosphereBlob> getBlobs(ServerLevel level, BlockPos pos) {
        List<AtmosphereBlob> inBlobs = new ArrayList<>();
        LevelAtmosphere state = levels.get(level);
        if (state == null) return inBlobs;

        for (AtmosphereBlob blob : state.blobs.values()) {
            if (blob.contains(pos)) inBlobs.add(blob);
        }

        return inBlobs;
    }

    public boolean hasAtmosphere(ServerLevel level, BlockPos pos) {
        LevelAtmosphere state = levels.get(level);
        if (state == null) return false;

        for (AtmosphereBlob blob : state.blobs.values()) {
            if (blob.contains(pos)) return true;
        }

        return false;
    }

    public List<AtmosphereBlob> getBlobsWithinRadius(ServerLevel level, BlockPos pos) {
        return getBlobsWithinRadius(level, pos, MAX_BLOB_RADIUS);
    }

    public List<AtmosphereBlob> getBlobsWithinRadius(ServerLevel level, BlockPos pos, int radius) {
        List<AtmosphereBlob> list = new ArrayList<>();
        LevelAtmosphere state = levels.get(level);
        if (state == null) return list;

        double radiusSqr = radius * radius;

        for (AtmosphereBlob blob : state.blobs.values()) {
            if (blob.getRootPosition().distSqr(pos) <= radiusSqr) list.add(blob);
        }

        return list;
    }

    public boolean canBreathe(LivingEntity entity) {
        CBT_Atmosphere atmosphere = getAtmosphere(entity);

        if (RadiationConfig.enableDebugMode
                && entity instanceof Player
                && entity.level().getGameTime() % 20 == 0) {
            if (atmosphere != null && !atmosphere.fluids.isEmpty()) {
                for (CBT_Atmosphere.FluidEntry entry : atmosphere.fluids) {
                    ChunkAtmosphereManager.LOGGER.info(
                            "Atmosphere: {} - {}bar",
                            NTMFluids.legacyName(entry.fluid),
                            entry.pressure);
                }
            } else {
                ChunkAtmosphereManager.LOGGER.info("Atmosphere: TOTAL VACUUM");
            }
        }

        return canBreathe(atmosphere);
    }

    public boolean canBreathe(@Nullable CBT_Atmosphere atmosphere) {
        return atmosphere != null
                && (atmosphere.hasFluid(earthAir(), 0.21)
                        || atmosphere.hasFluid(NTMFluids.OXYGEN, 0.09));
    }

    public boolean hasLiquidPressure(@Nullable CBT_Atmosphere atmosphere) {
        return atmosphere != null && atmosphere.getPressure() >= 0.19D;
    }

    public boolean willCorrode(LivingEntity entity) {
        return willCorrode(getAtmosphere(entity));
    }

    public boolean willCorrode(@Nullable CBT_Atmosphere atmosphere) {
        return atmosphere != null && atmosphere.hasTrait(CorrosiveTrait.class, 0.2);
    }

    private boolean runEffectsOnBlock(
            @Nullable CBT_Atmosphere atmosphere,
            ServerLevel level,
            BlockState state,
            BlockPos pos,
            boolean fetchAtmosphere) {
        if (!needsAir(state)) return false;

        if (fetchAtmosphere) {
            atmosphere = getAtmosphere(level, pos);
        }

        if (canExist(atmosphere, state)) return false;

        BlockPos at = pos.immutable();
        Platform.dropResources(
                level, at, at, state, level.getBlockEntity(at), null, ItemStack.EMPTY);
        level.setBlock(at, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);

        return true;
    }

    private static boolean needsAir(BlockState state) {
        Block block = state.getBlock();
        return state.is(Blocks.WATER) || requiresO2(block) || isPlant(block);
    }

    private static boolean requiresO2(Block block) {
        return block instanceof TorchBlock || block instanceof BaseFireBlock;
    }

    private boolean canExist(@Nullable CBT_Atmosphere atmosphere, BlockState state) {
        Block block = state.getBlock();
        if (requiresO2(block)) {
            return !(atmosphere == null
                    || (!atmosphere.hasFluid(NTMFluids.OXYGEN, 0.09)
                            && !atmosphere.hasFluid(earthAir(), 0.21)));
        } else if (state.is(Blocks.WATER)) {
            return hasLiquidPressure(atmosphere);
        } else if (isPlant(block)) {
            if (block instanceof PlantBreathingHandler breathing)
                return breathing.canBreathe(atmosphere);
            return !(atmosphere == null
                    || (!atmosphere.hasFluid(NTMFluids.OXYGEN, 0.01)
                            && !atmosphere.hasFluid(earthAir(), 0.1)));
        }
        return true;
    }

    private static boolean isPlant(Block block) {
        return block instanceof VegetationBlock
                || block instanceof CactusBlock
                || block instanceof SugarCaneBlock
                || block instanceof PlantBreathingHandler;
    }

    public boolean runEffectsOnBlock(
            @Nullable CBT_Atmosphere atmosphere,
            ServerLevel level,
            BlockState state,
            BlockPos pos) {
        return runEffectsOnBlock(atmosphere, level, state, pos, false);
    }

    public boolean runEffectsOnBlock(ServerLevel level, BlockState state, BlockPos pos) {
        return runEffectsOnBlock(null, level, state, pos, true);
    }

    public void registerAtmosphere(AtmosphereProvider handler) {
        ServerLevel level = (ServerLevel) handler.getLevel();
        LevelAtmosphere state = state(level);
        AtmosphereBlob blob = state.blobs.get(handler);

        if (blob == null) {
            processChanges(level, state);
            blob = new AtmosphereBlob(handler);
            blob.addBlock(handler.getRootPosition());
            state.blobs.put(handler, blob);
        }
    }

    public void unregisterAtmosphere(AtmosphereProvider handler) {
        LevelAtmosphere state = levels.get((ServerLevel) handler.getLevel());
        if (state != null) state.blobs.remove(handler);
    }

    public void onSealableChange(
            ServerLevel level,
            BlockPos pos,
            SealableBlock sealable,
            List<AtmosphereBlob> nearbyBlobs) {
        if (sealable.isSealed(level, pos)) {
            sealed(pos, nearbyBlobs, Collections.newSetFromMap(new IdentityHashMap<>()));
        } else {
            onBlockRemoved(level, pos);
        }
    }

    private static void sealed(
            BlockPos pos, List<AtmosphereBlob> nearbyBlobs, Set<AtmosphereBlob> regrown) {
        for (AtmosphereBlob blob : nearbyBlobs) {
            if (blob.contains(pos)) {
                blob.removeBlock(pos);
            } else if (!blob.contains(blob.getRootPosition()) && regrown.add(blob)) {
                blob.runDepress = false;
                blob.addBlock(blob.getRootPosition());
            }
        }
    }

    private void onBlockPlaced(ServerLevel level, BlockPos pos, Set<AtmosphereBlob> regrown) {
        sealed(pos, getBlobsWithinRadius(level, pos), regrown);
    }

    private void onBlockRemoved(ServerLevel level, BlockPos pos) {
        for (AtmosphereBlob blob : getBlobsWithinRadius(level, pos)) {
            for (Direction dir : Direction.values()) {
                if (blob.contains(pos.relative(dir))) {
                    blob.runDepress = true;
                    blob.depressDir = dir;
                    blob.addBlock(pos);
                    break;
                }
            }
        }
    }

    public void blockChanged(
            ServerLevel level, BlockPos pos, BlockState oldState, BlockState newState) {
        LevelAtmosphere state = levels.get(level);
        if (state == null || state.blobs.isEmpty()) return;
        if (mayReseal(level, pos, oldState, newState)
                || !oldState.is(newState.getBlock())
                        && oldState.getBlock() instanceof SaplingBlock) {
            state.changes.putIfAbsent(pos.asLong(), oldState);
        }
    }

    public void placed(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (runEffectsOnBlock(level, state, pos)) return;
        if (state.getBlock() instanceof BonemealableBlock) trackPlant(level, pos);
    }

    private static boolean mayReseal(
            ServerLevel level, BlockPos pos, BlockState before, BlockState now) {
        Boolean was = AtmosphereBlob.sealsByState(level, pos, before);
        Boolean is = AtmosphereBlob.sealsByState(level, pos, now);
        return was == null || is == null || was.booleanValue() != is.booleanValue();
    }

    private void processChanges(ServerLevel level, LevelAtmosphere state) {
        Long2ObjectLinkedOpenHashMap<BlockState> changes = state.changes;
        if (changes.isEmpty()) return;
        state.changes = new Long2ObjectLinkedOpenHashMap<>();

        treesGrew(level, changes);

        Set<AtmosphereBlob> regrown = Collections.newSetFromMap(new IdentityHashMap<>());

        ObjectIterator<Long2ObjectMap.Entry<BlockState>> iterator =
                changes.long2ObjectEntrySet().fastIterator();
        while (iterator.hasNext()) {
            Long2ObjectMap.Entry<BlockState> change = iterator.next();
            BlockPos pos = BlockPos.of(change.getLongKey());
            BlockState before = change.getValue();
            BlockState now = AtmosphereBlob.loadedState(level, pos);
            if (now == null || now == before || !mayReseal(level, pos, before, now)) continue;

            if (AtmosphereBlob.seals(level, pos, now)) {
                onBlockPlaced(level, pos, regrown);
            } else {
                onBlockRemoved(level, pos);
            }
        }
    }

    private void treesGrew(ServerLevel level, Long2ObjectLinkedOpenHashMap<BlockState> changes) {
        LongOpenHashSet grown = new LongOpenHashSet();
        ObjectIterator<Long2ObjectMap.Entry<BlockState>> iterator =
                changes.long2ObjectEntrySet().fastIterator();
        while (iterator.hasNext()) {
            Long2ObjectMap.Entry<BlockState> change = iterator.next();
            if (!(change.getValue().getBlock() instanceof SaplingBlock)) continue;
            BlockState now = AtmosphereBlob.loadedState(level, BlockPos.of(change.getLongKey()));
            if (now == null || now.isAir() || now.getBlock() instanceof SaplingBlock) continue;
            grown.add(change.getLongKey());
        }

        LongIterator trees = grown.iterator();
        while (trees.hasNext()) {
            long sapling = trees.nextLong();
            if (grown.contains(BlockPos.offset(sapling, Direction.WEST))) continue;
            if (grown.contains(BlockPos.offset(sapling, Direction.NORTH))) continue;
            treeGrew(level, BlockPos.of(sapling));
        }
    }

    private void treeGrew(ServerLevel level, BlockPos pos) {
        for (AtmosphereBlob blob : getBlobsWithinRadius(level, pos, MAX_BLOB_RADIUS)) {
            if (blob.contains(pos)) {
                blob.produce(TREE_GROWTH_CONVERSION);
                break;
            }
        }
    }

    public void trackPlant(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        for (AtmosphereBlob blob : getBlobsWithinRadius(level, pos)) {
            if (blob.contains(pos)) blob.addPlant(pos, state);
        }
    }

    public void serverTick(MinecraftServer server) {
        for (Map.Entry<ServerLevel, LevelAtmosphere> entry : new ArrayList<>(levels.entrySet())) {
            ServerLevel level = entry.getKey();
            LevelAtmosphere state = entry.getValue();

            processChanges(level, state);
            tickTerraforming(level, state);

            if (level.getGameTime() % 20 != 0) continue;

            for (AtmosphereBlob blob : state.blobs.values()) {
                blob.checkGrowth();
            }
        }
    }

    public void serverStopping() {
        levels.clear();
    }

    private void tickTerraforming(ServerLevel level, LevelAtmosphere state) {
        Queue<Growth> growths = state.growths;

        for (int g = 0; g < 64; g++) {
            Growth growth = growths.poll();

            if (growth == null) return;

            for (int i = 0; i < 4; i++) {
                Direction dir = Direction.from3DDataValue(level.getRandom().nextInt(4) + 2);

                int x = growth.x + dir.getStepX();
                int z = growth.z + dir.getStepZ();

                for (int y = growth.y + 1; y >= growth.y - 1; y--) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState found = AtmosphereBlob.loadedState(level, pos);
                    if (found == null) continue;
                    Block block = found.getBlock();

                    boolean shouldReplace =
                            growth.from != null
                                    ? block == growth.from
                                    : block == Blocks.DIRT
                                            || block == ModBlocks.WASTE_EARTH.get()
                                            || block == ModBlocks.DIRT_DEAD.get()
                                            || block == ModBlocks.DIRT_OILY.get();

                    if (shouldReplace) {
                        level.setBlock(pos, growth.into.defaultBlockState(), Block.UPDATE_ALL);

                        if (growth.into == Blocks.GRASS_BLOCK) {
                            level.levelEvent(LevelEvent.PARTICLES_BEE_GROWTH, pos.above(), 15);
                        }

                        if (growth.time > 0)
                            addGrowth(level, growth.from, growth.into, x, y, z, growth.time - 1);

                        return;
                    }
                }
            }
        }
    }

    public void addGrowth(ServerLevel level, Block into, int x, int y, int z, int size) {
        addGrowth(level, null, into, x, y, z, size);
    }

    public void addGrowth(ServerLevel level, Block into, int x, int y, int z, int count, int size) {
        addGrowth(level, null, into, x, y, z, count, size);
    }

    public void addGrowth(
            ServerLevel level, @Nullable Block from, Block into, int x, int y, int z, int size) {
        state(level).growths.add(new Growth(from, into, x, y, z, size));
    }

    public void addGrowth(
            ServerLevel level,
            @Nullable Block from,
            Block into,
            int x,
            int y,
            int z,
            int count,
            int size) {
        Queue<Growth> growths = state(level).growths;
        for (int i = 0; i < count; i++) {
            growths.add(new Growth(from, into, x, y, z, size));
        }
    }

    private record Growth(@Nullable Block from, Block into, int x, int y, int z, int time) {}
}
