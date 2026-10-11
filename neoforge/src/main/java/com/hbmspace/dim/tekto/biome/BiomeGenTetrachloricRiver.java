// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.tekto.biome;

import com.hbmspace.blocks.ModBlocksSpace;
import com.hbmspace.blocks.TerrainBlocksSpace;
import com.hbmspace.dim.legacy.LegacyWorld;
import java.util.Random;
import net.minecraft.world.level.block.state.BlockState;

public class BiomeGenTetrachloricRiver extends BiomeGenBaseTekto {

    BiomeGenTetrachloricRiver() {
        super("tekto_tetrachloride_river", 90);
        this.setBiomeName("Tetrachloride River");

        this.setHeight(new Height(-0.7F, 0.0F));
    }

    @Override
    public void genTerrainBlocks(
            LegacyWorld world, Random rand, BlockState[] blocks, int x, int z, double noise) {
        genTektoTerrain(
                world,
                rand,
                blocks,
                x,
                z,
                noise,
                TerrainBlocksSpace.DUNA_ROCK.get(),
                TerrainBlocksSpace.DUNA_ROCK.get(),
                ModBlocksSpace.DUNA_SANDS.get());
    }
}
