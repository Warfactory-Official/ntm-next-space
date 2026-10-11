// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blockentity.machine;

import com.hbm.blockentity.PersistentDrop;
import com.hbm.blockentity.machine.BlockEntityCondenserPowered;
import com.hbm.interfaces.IRepairable;
import com.hbm.inventory.OreDictManager;
import com.hbm.inventory.recipes.ingredient.CountIngredient;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class BlockEntityCondenserPoweredSpace extends BlockEntityCondenserPowered
        implements IRepairable, PersistentDrop {
    private static final List<CountIngredient> REPAIR_MATERIALS =
            List.of(
                    CountIngredient.of(OreDictManager.STEEL.plateWelded(), 4),
                    CountIngredient.of(OreDictManager.STEEL.pipe(), 12),
                    CountIngredient.of(OreDictManager.ANY_RESISTANTALLOY.plateWelded(), 2));

    public @Nullable Explosion lastExplosion;

    public BlockEntityCondenserPoweredSpace(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    @Override
    public boolean isDamaged() {
        return ThermalMachineDamage.damaged(this);
    }

    public void damage() {
        waterTimer = 0;
        ThermalMachineDamage.set(this, true);
    }

    @Override
    public void tickServer() {
        if (isDamaged()) {
            waterTimer = 0;
            networkPackNT(150);
            return;
        }
        super.tickServer();
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
