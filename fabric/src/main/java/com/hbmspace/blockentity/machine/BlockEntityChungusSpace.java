// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blockentity.machine;

import com.hbm.blockentity.PersistentDrop;
import com.hbm.blockentity.machine.BlockEntityChungus;
import com.hbm.interfaces.IRepairable;
import com.hbm.inventory.OreDictManager;
import com.hbm.inventory.fluid.NTMFluids;
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

public class BlockEntityChungusSpace extends BlockEntityChungus
        implements IRepairable, PersistentDrop {
    private static final List<CountIngredient> REPAIR_MATERIALS =
            List.of(
                    CountIngredient.of(OreDictManager.STEEL.plateWelded(), 6),
                    CountIngredient.of(OreDictManager.STEEL.pipe(), 8),
                    CountIngredient.of(OreDictManager.ANY_RESISTANTALLOY.ingot(), 8),
                    CountIngredient.of(OreDictManager.GOLD.wireDense(), 24),
                    CountIngredient.of(ModItems.FLYWHEEL_BERYLLIUM.get()));

    public @Nullable Explosion lastExplosion;

    public BlockEntityChungusSpace(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    @Override
    public boolean isDamaged() {
        return ThermalMachineDamage.damaged(this);
    }

    public void damage() {
        turnTimer = 0;
        ThermalMachineDamage.set(this, true);
    }

    @Override
    public void tickServer() {
        if (isDamaged()) {
            powerBuffer = 0;
            info[0] = info[1] = info[2] = 0;
            operational = false;
            turnTimer = 0;
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
    public void tryExtinguish(Level level, BlockPos pos, EnumExtinguishType type) {}

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
}
