// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2024-2025 George Paton
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.handler.atmosphere;

import com.hbm.blocks.generic.BlockDoorGeneric;
import com.hbm.blocks.machine.MachineDiesel;
import com.hbm.blocks.machine.MachineFan;
import com.hbm.blocks.machine.MachineFurnaceBrick;
import com.hbm.blocks.multiblock.BlockMultiblockCell;
import com.hbm.blocks.multiblock.BlockMultiblockCore;
import com.hbm.blocks.multiblock.BlockMultiblockGeometryCell;
import com.hbm.blocks.multiblock.MultiblockSurface;
import com.hbmspace.dim.trait.CBT_Atmosphere;
import com.hbmspace.entity.effect.EntityDepress;
import it.unimi.dsi.fastutil.longs.Long2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import org.jspecify.annotations.Nullable;

public class AtmosphereBlob {

    private static final Direction[] DIRECTIONS = Direction.values();

    protected final LongOpenHashSet graph = new LongOpenHashSet();

    protected final IAtmosphereProvider handler;

    public boolean runDepress;
    public Direction depressDir = Direction.UP;

    private final Long2IntLinkedOpenHashMap plants = new Long2IntLinkedOpenHashMap();

    public AtmosphereBlob(IAtmosphereProvider handler) {
        this.handler = handler;
    }

    private ServerLevel level() {
        return (ServerLevel) handler.getLevel();
    }

    public boolean isPositionAllowed(ServerLevel level, BlockPos pos) {
        return !isBlockSealed(level, pos);
    }

    public static boolean isBlockSealed(ServerLevel level, BlockPos pos) {
        if (level.isOutsideBuildHeight(pos)) return false;

        if (level.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4) == null)
            return true;

        return seals(level, pos, level.getBlockState(pos));
    }

    public static boolean seals(ServerLevel level, BlockPos pos, BlockState state) {
        Boolean own = sealsByState(level, pos, state);
        if (own != null) return own;

        Block block = state.getBlock();
        if (block instanceof IBlockSealable sealable) return sealable.isSealed(level, pos);
        if (block instanceof BlockDoorGeneric) return !state.getValue(BlockDoorGeneric.OPEN);

        BlockPos core = MultiblockSurface.indexedCore(level, pos);
        if (core == null) return false;
        Block owner = level.getBlockState(core).getBlock();
        if (owner instanceof IBlockSealable sealable) return sealable.isSealed(level, pos);
        return owner instanceof BlockDoorGeneric
                && block instanceof BlockMultiblockGeometryCell cell
                && !cell.isOpen(state);
    }

    static @Nullable Boolean sealsByState(ServerLevel level, BlockPos pos, BlockState state) {
        if (state.isAir()) return false;

        Block block = state.getBlock();
        if (block instanceof IBlockSealable
                || block instanceof BlockDoorGeneric
                || block instanceof BlockMultiblockCell) {
            return null;
        }
        if (block instanceof BlockMultiblockCore) return false;
        if (block instanceof MachineDiesel
                || block instanceof MachineFurnaceBrick
                || block instanceof MachineFan
                || block instanceof AbstractFurnaceBlock) {
            return false;
        }

        if (!state.getFluidState().isEmpty()) return false;
        if (state.is(BlockTags.LEAVES)) return false;

        return state.isCollisionShapeFullBlock(level, pos);
    }

    public int getBlobMaxRadius() {
        return handler.getMaxBlobRadius();
    }

    public boolean hasFluid(Fluid fluid) {
        return hasFluid(fluid, 0.001);
    }

    public boolean hasFluid(Fluid fluid, double abovePressure) {
        if (handler.getFluidType() != fluid) return false;
        return handler.getFluidPressure() >= abovePressure;
    }

    public boolean hasPressure(double abovePressure) {
        return handler.getFluidPressure() >= abovePressure;
    }

    public void consume(int amount) {
        handler.consume(amount);
    }

    public void produce(int amount) {
        handler.produce(amount);
    }

    public void addBlock(BlockPos pos) {
        long key = pos.asLong();
        if (graph.contains(key)) return;
        if (!graph.isEmpty() && !touches(key)) return;
        flood(pos);
    }

    private boolean touches(long key) {
        for (Direction dir : DIRECTIONS) {
            if (graph.contains(BlockPos.offset(key, dir))) return true;
        }
        return false;
    }

    private void addSingleBlock(ServerLevel level, long key) {
        if (!graph.add(key)) return;
        BlockPos pos = BlockPos.of(key);
        BlockState state = loadedState(level, pos);
        if (state != null && state.getBlock() instanceof BonemealableBlock) addPlant(pos, state);
    }

    public BlockPos getRootPosition() {
        return handler.getRootPosition();
    }

    public boolean contains(BlockPos pos) {
        return graph.contains(pos.asLong());
    }

    public boolean contains(long pos) {
        return graph.contains(pos);
    }

    public void removeBlock(BlockPos pos) {
        long key = pos.asLong();
        graph.remove(key);

        long root = getRootPosition().asLong();
        for (Direction dir : DIRECTIONS) {
            long next = BlockPos.offset(key, dir);
            if (graph.contains(next) && !doesPathExist(next, root)) {
                runEffectOnWorldBlocks(level(), removeAllNodesConnectedTo(next));
            }
        }
    }

    private boolean doesPathExist(long from, long to) {
        LongOpenHashSet visited = new LongOpenHashSet();
        LongArrayList stack = new LongArrayList();
        stack.push(from);

        while (!stack.isEmpty()) {
            long element = stack.popLong();
            visited.add(element);
            for (Direction dir : DIRECTIONS) {
                long next = BlockPos.offset(element, dir);
                if (!graph.contains(next)) continue;
                if (next == to) return true;
                if (!visited.contains(next)) stack.push(next);
            }
        }

        return false;
    }

    private LongCollection removeAllNodesConnectedTo(long start) {
        LongOpenHashSet component = new LongOpenHashSet();
        LongArrayList stack = new LongArrayList();
        stack.push(start);
        component.add(start);

        while (!stack.isEmpty()) {
            long element = stack.popLong();
            for (Direction dir : DIRECTIONS) {
                long next = BlockPos.offset(element, dir);
                if (graph.contains(next) && component.add(next)) stack.push(next);
            }
        }

        graph.removeAll(component);
        return component;
    }

    public void clearBlob() {
        LongArrayList locations = new LongArrayList(graph);
        graph.clear();
        runEffectOnWorldBlocks(level(), locations);
    }

    public LongSet getLocations() {
        return graph;
    }

    public int getBlobSize() {
        return graph.size();
    }

    private void flood(BlockPos start) {
        ServerLevel level = level();
        BlockPos root = getRootPosition();
        final int maxSize = getBlobMaxRadius();
        final LongOpenHashSet addableBlocks = new LongOpenHashSet();
        LongArrayList stack = new LongArrayList();
        BlockPos.MutableBlockPos search = new BlockPos.MutableBlockPos();
        long[] candidates = new long[DIRECTIONS.length];
        double[] distances = new double[DIRECTIONS.length];
        stack.push(start.asLong());

        while (!stack.isEmpty()) {
            long stackElement = stack.popLong();
            addableBlocks.add(stackElement);

            int count = 0;
            for (Direction dir : DIRECTIONS) {
                long next = BlockPos.offset(stackElement, dir);

                if (graph.contains(next) || addableBlocks.contains(next)) continue;

                search.set(next);
                if (!isPositionAllowed(level, search)) continue;

                double distance = search.distSqr(root);
                if (distance > maxSize * maxSize) {
                    if (runDepress) decompress(start, depressDir);

                    clearBlob();
                    return;
                }

                int slot = count++;
                for (; slot > 0 && distances[slot - 1] > distance; slot--) {
                    distances[slot] = distances[slot - 1];
                    candidates[slot] = candidates[slot - 1];
                }
                distances[slot] = distance;
                candidates[slot] = next;
            }

            for (int i = 0; i < count; i++) {
                stack.push(candidates[i]);
                addableBlocks.add(candidates[i]);
            }
        }

        LongIterator addable = addableBlocks.iterator();
        while (addable.hasNext()) {
            addSingleBlock(level, addable.nextLong());
        }

        handler.onBlobCreated(this);
    }

    protected void runEffectOnWorldBlocks(ServerLevel level, LongCollection blocks) {
        CBT_Atmosphere newAtmosphere =
                ChunkAtmosphereManager.proxy.getAtmosphere(level, getRootPosition(), this);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        LongIterator iterator = blocks.iterator();
        while (iterator.hasNext()) {
            pos.set(iterator.nextLong());
            BlockState state = loadedState(level, pos);
            if (state != null)
                ChunkAtmosphereManager.proxy.runEffectsOnBlock(newAtmosphere, level, state, pos);
        }
    }

    public void decompress(BlockPos pos, Direction dir) {
        ServerLevel level = level();
        double x = pos.getX() + 0.5, y = pos.getY() + 0.5, z = pos.getZ() + 0.5;
        EntityDepress depress = new EntityDepress(level, dir.getOpposite(), 20);
        depress.setPos(x, y, z);
        level.addFreshEntity(depress);

        level.playSound(null, x, y, z, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 1.0F, 1.6F);
        level.playSound(
                null, x, y, z, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 0.25F);
    }

    public void checkGrowth() {
        ServerLevel level = level();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        ObjectIterator<Long2IntMap.Entry> iterator = plants.long2IntEntrySet().fastIterator();
        while (iterator.hasNext()) {
            Long2IntMap.Entry entry = iterator.next();
            pos.set(entry.getLongKey());
            BlockState state = loadedState(level, pos);
            if (state == null) continue;

            if (!(state.getBlock() instanceof BonemealableBlock)) {
                iterator.remove();
                continue;
            }

            int oldStage = entry.getIntValue();
            int newStage = growthStage(state);

            if (newStage != oldStage) {
                entry.setValue(newStage);
                produce(
                        Math.max(newStage - oldStage, 0)
                                * ChunkAtmosphereHandler.CROP_GROWTH_CONVERSION);
            }
        }
    }

    public void addPlant(BlockPos pos, BlockState state) {
        plants.put(pos.asLong(), growthStage(state));
    }

    private static int growthStage(BlockState state) {
        for (Property<?> property : state.getProperties()) {
            if (property instanceof IntegerProperty stage
                    && (stage.getName().equals("age") || stage.getName().equals("stage"))) {
                return state.getValue(stage);
            }
        }
        return 0;
    }

    static @Nullable BlockState loadedState(ServerLevel level, BlockPos pos) {
        var chunk = level.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4);
        return chunk == null ? null : chunk.getBlockState(pos);
    }
}
