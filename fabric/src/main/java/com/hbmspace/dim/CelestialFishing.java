// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockPlushie.PlushieType;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.advancements.predicates.LocationPredicate;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.NestedLootTable;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemDamageFunction;
import net.minecraft.world.level.storage.loot.functions.SetPotionFunction;
import net.minecraft.world.level.storage.loot.predicates.AnyOfCondition;
import net.minecraft.world.level.storage.loot.predicates.InvertedLootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LocationCheck;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

public final class CelestialFishing {

    private record Replacement(List<ResourceKey<Level>> bodies, Supplier<LootPool.Builder> pool) {}

    private static final Replacement IKE =
            new Replacement(List.of(SpaceDimensions.IKE), CelestialFishing::plushie);

    private CelestialFishing() {}

    public static LootTable wrap(ResourceKey<LootTable> key, LootTable original) {
        List<Replacement> replacements;
        if (key.equals(BuiltInLootTables.FISHING_FISH)) {
            replacements =
                    List.of(
                            IKE,
                            new Replacement(
                                    List.of(
                                            SpaceDimensions.MUN,
                                            SpaceDimensions.DUNA,
                                            SpaceDimensions.EVE,
                                            SpaceDimensions.DRES,
                                            SpaceDimensions.MOHO,
                                            SpaceDimensions.MINMUS,
                                            SpaceDimensions.LAYTHE,
                                            SpaceDimensions.TEKTO,
                                            SpaceDimensions.THATMO),
                                    CelestialFishing::dead));
        } else if (key.equals(BuiltInLootTables.FISHING_JUNK)
                || key.equals(BuiltInLootTables.FISHING_TREASURE)) {
            replacements = List.of(IKE);
        } else {
            return original;
        }
        List<ResourceKey<Level>> replaced = new ArrayList<>();
        LootTable.Builder table =
                LootTable.lootTable()
                        .setParamSet(original.getParamSet())
                        .setRandomSequence(key.identifier());
        for (Replacement replacement : replacements) {
            replaced.addAll(replacement.bodies());
            table.withPool(replacement.pool().get().when(on(replacement.bodies())));
        }
        return table.withPool(
                        LootPool.lootPool()
                                .add(NestedLootTable.inlineLootTable(original))
                                .when(InvertedLootItemCondition.invert(on(replaced))))
                .build();
    }

    private static AnyOfCondition.Builder on(List<ResourceKey<Level>> bodies) {
        AnyOfCondition.Builder any = AnyOfCondition.anyOf();
        for (ResourceKey<Level> body : bodies) {
            any.or(LocationCheck.checkLocation(LocationPredicate.Builder.inDimension(body)));
        }
        return any;
    }

    private static LootPool.Builder dead() {
        return LootPool.lootPool()
                .add(
                        LootItem.lootTableItem(Items.POTION)
                                .setWeight(10)
                                .apply(SetPotionFunction.setPotion(Potions.WATER)))
                .add(LootItem.lootTableItem(Items.STRING).setWeight(5))
                .add(
                        LootItem.lootTableItem(Items.FISHING_ROD)
                                .setWeight(2)
                                .apply(
                                        SetItemDamageFunction.setDamage(
                                                UniformGenerator.between(0.0F, 0.9F))))
                .add(LootItem.lootTableItem(Items.BOWL).setWeight(10))
                .add(LootItem.lootTableItem(Items.STICK).setWeight(5))
                .add(
                        LootItem.lootTableItem(Items.INK_SAC)
                                .setWeight(1)
                                .apply(SetItemCountFunction.setCount(ConstantValue.exactly(10.0F))))
                .add(LootItem.lootTableItem(Blocks.TRIPWIRE_HOOK).setWeight(10));
    }

    private static LootPool.Builder plushie() {
        return LootPool.lootPool()
                .add(
                        LootItem.lootTableItem(ModBlocks.PLUSHIES.get(PlushieType.YOMI))
                                .setWeight(100));
    }
}
