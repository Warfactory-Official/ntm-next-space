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

package com.hbmspace.blockentity.machine;

import com.hbm.blockentity.PersistentDrop;
import com.hbm.blockentity.machine.thermal.LeviathanTurbineBlockEntity;
import com.hbm.interfaces.Extinguishable;
import com.hbm.interfaces.RepairHandler;
import com.hbm.inventory.fluid.NTMFluids;
import com.hbm.inventory.material.MaterialTagRegistry;
import com.hbm.inventory.recipes.ingredient.CountIngredient;
import com.hbm.items.ModItems;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class ChungusSpaceBlockEntity extends LeviathanTurbineBlockEntity
        implements RepairHandler, Extinguishable, PersistentDrop {
    private static final List<CountIngredient> REPAIR_MATERIALS =
            List.of(
                    CountIngredient.of(MaterialTagRegistry.STEEL.plateWelded(), 6),
                    CountIngredient.of(MaterialTagRegistry.STEEL.pipe(), 8),
                    CountIngredient.of(MaterialTagRegistry.ANY_RESISTANTALLOY.ingot(), 8),
                    CountIngredient.of(MaterialTagRegistry.GOLD.wireDense(), 24),
                    CountIngredient.of(ModItems.FLYWHEEL_BERYLLIUM.get()));

    public @Nullable Explosion lastExplosion;

    public ChungusSpaceBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    @Override
    public boolean isDamaged() {
        return ThermalMachineDamage.damaged(this);
    }

    public void damage() {
        hbm$syncSet$LeviathanTurbineBlockEntity$turnTimer(0);
        ThermalMachineDamage.set(this, true);
    }

    @Override
    public void tickServer() {
        if (isDamaged()) {
            hbm$syncSet$TurbineBaseBlockEntity$powerBuffer(0);
            info[0] = info[1] = info[2] = 0;
            hbm$syncSet$TurbineBaseBlockEntity$operational(false);
            hbm$syncSet$LeviathanTurbineBlockEntity$turnTimer(0);
            networkPackNT(150);
            return;
        }
        super.tickServer();
    }

    @Override
    public void onLeverPull() {
        Fluid old = tanks[0].getTankType();
        Fluid input, output;
        if (old == NTMFluids.STEAM) {
            input = NTMFluids.HOTSTEAM;
            output = NTMFluids.STEAM;
        } else if (old == NTMFluids.HOTSTEAM) {
            input = NTMFluids.SUPERHOTSTEAM;
            output = NTMFluids.HOTSTEAM;
        } else if (old == NTMFluids.SUPERHOTSTEAM) {
            input = NTMFluids.ULTRAHOTSTEAM;
            output = NTMFluids.SUPERHOTSTEAM;
        } else {
            input = NTMFluids.STEAM;
            output = NTMFluids.SPENTSTEAM;
        }
        tanks[0].setTankType(input);
        tanks[0].setFill(0);
        tanks[1].setTankType(output);
        tanks[1].setFill(0);
        setChanged();
    }

    @Override
    public List<CountIngredient> getRepairMaterials() {
        return REPAIR_MATERIALS;
    }

    @Override
    public void repair(Player player) {
        ThermalMachineDamage.set(this, false);
    }

    @Override
    public void tryExtinguish(Level level, BlockPos pos, Agent agent) {}

    @Override
    public void writePersistent(DataComponentMap.Builder components) {
        ThermalMachineDamage.write(this, components);
    }

    @Override
    public void readPersistent(DataComponentGetter components) {
        ThermalMachineDamage.read(this, components);
    }

    @Override
    public String[] persistentKeys() {
        return ThermalMachineDamage.PERSISTENT_KEYS;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        ThermalMachineDamage.set(this, input.getBooleanOr("damaged", isDamaged()));
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putBoolean("damaged", isDamaged());
    }

    public void stripPersistent(ValueOutput output) {
        for (String key : persistentKeys()) output.discard(key);
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        writePersistent(components);
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        readPersistent(components);
    }

    @Override
    public void removeComponentsFromTag(ValueOutput output) {
        super.removeComponentsFromTag(output);
        stripPersistent(output);
    }
}
