// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.inventory.recipes;

import com.hbm.inventory.recipes.ingredient.CountIngredient;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.inventory.recipes.loader.GenericRecipes;
import com.mojang.serialization.MapCodec;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class VacuumCircuitRecipe extends GenericRecipe {

    private static final CountIngredient[] EMPTY_GROUP = new CountIngredient[0];
    private static final MapCodec<List<CountIngredient>> PCB_MAP_CODEC =
            CountIngredient.CODEC.listOf().optionalFieldOf("pcb", List.of());
    private static final StreamCodec<RegistryFriendlyByteBuf, List<CountIngredient>>
            PCB_STREAM_CODEC = CountIngredient.STREAM_CODEC.apply(ByteBufCodecs.list());
    private static final BiConsumer<VacuumCircuitRecipe, List<CountIngredient>> APPLY_PCB =
            (recipe, pcb) -> recipe.pcb = pcb.toArray(CountIngredient[]::new);
    private static final Function<VacuumCircuitRecipe, List<CountIngredient>> EXTRACT_PCB =
            recipe -> Arrays.asList(recipe.pcb);
    public static final MapCodec<VacuumCircuitRecipe> MAP_CODEC =
            GenericRecipe.mapCodec(VacuumCircuitRecipe::new, PCB_MAP_CODEC, APPLY_PCB, EXTRACT_PCB);
    public static final StreamCodec<RegistryFriendlyByteBuf, VacuumCircuitRecipe> STREAM_CODEC =
            GenericRecipe.streamCodec(
                    VacuumCircuitRecipe::new, PCB_STREAM_CODEC, APPLY_PCB, EXTRACT_PCB);
    public static final RecipeSerializer<VacuumCircuitRecipe> SERIALIZER =
            new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    public CountIngredient[] pcb = EMPTY_GROUP;

    public VacuumCircuitRecipe(String name) {
        super(name);
    }

    public VacuumCircuitRecipe setPcb(CountIngredient... pcb) {
        this.pcb = pcb;
        return this;
    }

    public CountIngredient[] wafer() {
        return inputItem == null ? EMPTY_GROUP : inputItem;
    }

    public ItemStack output() {
        return outputItems()[0].getRandom(GenericRecipes.RNG).orElse(ItemStack.EMPTY);
    }

    public Item previewItem() {
        return outputItems()[0].unwrap().getFirst().value().getItem();
    }

    @Override
    protected void input(List<Component> list) {
        super.input(list);
        for (CountIngredient ingredient : pcb) {
            ItemStack display = ingredient.extractForCyclingDisplay(20);
            if (display.isEmpty()) continue;
            list.add(
                    Component.literal("  " + display.getCount() + "x ")
                            .withStyle(ChatFormatting.GRAY)
                            .append(display.getHoverName()));
        }
    }

    @Override
    public GenericRecipes<?, ?> table() {
        return VacuumCircuitRecipes.INSTANCE;
    }
}
