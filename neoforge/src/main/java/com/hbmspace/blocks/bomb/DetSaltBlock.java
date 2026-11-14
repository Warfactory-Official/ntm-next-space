/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file under the Apache License except in
 * compliance with that License. You may obtain a copy at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Apache License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License for the specific language governing permissions
 * and limitations under the License.
 */

package com.hbmspace.blocks.bomb;

import com.hbm.blocks.bomb.ExplosiveChargeBlock;
import com.hbm.blocks.bomb.TntRule;
import com.hbm.data.ExplosionData;
import com.hbm.entity.effect.NukeTorexEntity;
import com.hbm.entity.logic.NukeExplosionMK5Entity;
import com.hbm.interfaces.TriggerResult;
import com.hbmspace.blocks.MaterialBlocksSpace;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public class DetSaltBlock extends ExplosiveChargeBlock {

    public DetSaltBlock(Properties props) {
        super(props);
    }

    @Override
    public TriggerResult trigger(Level level, BlockPos pos, @Nullable Entity detonator) {
        if (level.isClientSide()) return TriggerResult.DETONATED;
        if (!TntRule.explodes(level)) return TriggerResult.TNT_DISABLED;
        level.removeBlock(pos, false);

        double x = pos.getX() + 0.5, y = pos.getY() + 0.5, z = pos.getZ() + 0.5;
        level.addFreshEntity(
                NukeExplosionMK5Entity.statFac(level, ExplosionData.MISSILE_RADIUS.get(), x, y, z)
                        .falloutLayer(MaterialBlocksSpace.SALTED_FALLOUT.get()));
        NukeTorexEntity.statFac(level, x, y, z, ExplosionData.MISSILE_RADIUS.get());

        return TriggerResult.DETONATED;
    }
}
