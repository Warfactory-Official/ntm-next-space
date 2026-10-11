// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.legacy;

import com.hbmspace.dim.BiomeGenBaseCelestial;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

public final class LegacyDecorWorld {

    private final WorldGenLevel level;
    private final LegacyWorld terrain;
    private final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

    public LegacyDecorWorld(WorldGenLevel level, LegacyWorld terrain) {
        this.level = level;
        this.terrain = terrain;
    }

    public WorldGenLevel level() {
        return level;
    }

    public long getSeed() {
        return terrain.getSeed();
    }

    public BiomeGenBaseCelestial getBiomeGenForCoords(int x, int z) {
        return terrain.getBiomeGenForCoords(x, z);
    }

    public BlockState getBlock(int x, int y, int z) {
        if (y < level.getMinY() || y > level.getMaxY()) return Blocks.AIR.defaultBlockState();
        return level.getBlockState(pos.set(x, y, z));
    }

    public boolean is(int x, int y, int z, Block block) {
        return getBlock(x, y, z).is(block);
    }

    public boolean isAirBlock(int x, int y, int z) {
        return getBlock(x, y, z).isAir();
    }

    public boolean blockExists(int x, int y, int z) {
        return y >= level.getMinY()
                && y <= level.getMaxY()
                && (!(level instanceof WorldGenRegion region)
                        || region.isWithinWriteZone(pos.set(x, y, z)));
    }

    public void setBlock(int x, int y, int z, BlockState state, int flags) {
        if (y < level.getMinY() || y > level.getMaxY()) return;
        level.setBlock(pos.set(x, y, z), state, flags);
    }

    public int getHeightValue(int x, int z) {
        return level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
    }

    public int getTopSolidOrLiquidBlock(int x, int z) {
        return level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z);
    }

    public void flowLiquid(int x, int y, int z, BlockState liquid) {
        setBlock(x, y, z, liquid, Block.UPDATE_CLIENTS);
        level.scheduleTick(pos.set(x, y, z).immutable(), liquid.getFluidState().getType(), 0);
    }
}
