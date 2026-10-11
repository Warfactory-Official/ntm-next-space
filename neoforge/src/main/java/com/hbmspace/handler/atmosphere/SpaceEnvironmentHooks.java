// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.handler.atmosphere;

import com.hbm.blockentity.machine.BlockEntityMachinePumpBase;
import com.hbm.items.armor.ItemModInsert;
import com.hbm.items.armor.ModArmorItem;
import com.hbm.items.machine.ItemFluidContainerInfinite;
import com.hbm.lib.Library;
import com.hbmspace.HbmSpace;
import com.hbmspace.blocks.FloraBlocksSpace;
import com.hbmspace.blocks.ModBlocksSpace;
import com.hbmspace.blocks.TerrainBlocksSpace;
import com.hbmspace.items.armor.ArmorUtilSpace;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

final class SpaceEnvironmentHooks {

    private SpaceEnvironmentHooks() {}

    static void install() {
        ModArmorItem.EXTRA_LINES.install(HbmSpace.MOD_ID, SpaceEnvironmentHooks::armorLines);
        ItemModInsert.EXTRA_LINES.install(HbmSpace.MOD_ID, SpaceEnvironmentHooks::insertLines);
        ItemFluidContainerInfinite.CONDITION.install(HbmSpace.MOD_ID, new WaterTableCondition());

        BlockEntityMachinePumpBase.addGround(() -> TerrainBlocksSpace.EVE_SILT.get());
        BlockEntityMachinePumpBase.addGround(() -> TerrainBlocksSpace.EVE_ROCK.get());
        BlockEntityMachinePumpBase.addGround(() -> TerrainBlocksSpace.IKE_REGOLITH.get());
        BlockEntityMachinePumpBase.addGround(() -> ModBlocksSpace.IKE_STONE.get());
        BlockEntityMachinePumpBase.addGround(() -> ModBlocksSpace.DUNA_SANDS.get());
        BlockEntityMachinePumpBase.addGround(
                () -> BuiltInRegistries.BLOCK.getValue(Library.id("moon_turf")));
        BlockEntityMachinePumpBase.addGround(() -> TerrainBlocksSpace.LAYTHE_SILT.get());
        BlockEntityMachinePumpBase.addGround(() -> ModBlocksSpace.MOHO_REGOLITH.get());
        BlockEntityMachinePumpBase.addGround(() -> ModBlocksSpace.MINMUS_SMOOTH.get());
        BlockEntityMachinePumpBase.addGround(() -> TerrainBlocksSpace.VINYL_SAND.get());
        BlockEntityMachinePumpBase.addGround(() -> TerrainBlocksSpace.RUBBER_SILT.get());
        BlockEntityMachinePumpBase.addGround(() -> FloraBlocksSpace.RUBBER_GRASS.get());
    }

    private static void armorLines(ItemStack stack, Consumer<Component> adder) {
        if (stack.getItem() instanceof ModArmorItem armor
                && armor.suit() == ModArmorItem.Suit.DNS) {
            adder.accept(Component.translatable("armor.canBreathe").withStyle(ChatFormatting.BLUE));
        } else if (ArmorUtilSpace.canSeal(stack)) {
            adder.accept(Component.translatable("armor.canSeal").withStyle(ChatFormatting.BLUE));
        }
    }

    private static void insertLines(ItemStack stack, Consumer<Component> adder) {
        if (ArmorUtilSpace.isCorrosionProtection(stack)) {
            adder.accept(
                    Component.translatable("desc.hbmspace.insert.corrosionProtection")
                            .withStyle(ChatFormatting.GOLD));
        }
    }
}
