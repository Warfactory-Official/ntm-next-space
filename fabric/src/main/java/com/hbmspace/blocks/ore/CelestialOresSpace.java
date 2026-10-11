// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blocks.ore;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockOreNetherFire;
import com.hbm.blocks.generic.BlockOutgas;
import com.hbm.items.CreativeContents.Tab;
import com.hbm.items.CreativeContents;
import com.hbm.items.armor.ItemModLens;
import com.hbm.items.tool.ItemSurveyScanner.Deposit;
import com.hbm.items.tool.ItemSurveyScanner;
import com.hbm.lib.Library;
import com.hbm.registration.FabricRegistrar;
import com.hbm.registration.Reg;
import com.hbm.registration.RegistryHandle;
import com.hbmspace.HbmSpace;
import com.hbmspace.blocks.ore.CelestialOre.Spec;
import com.hbmspace.dim.SolarSystem.Body;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import org.jspecify.annotations.Nullable;

public final class CelestialOresSpace {

    public static final RegistryHandle<SoundEvent> STEP_MORKITE =
            FabricRegistrar.registerSound(HbmSpace.MOD_ID + ":step.morkite");

    private static final String TEMPLATE = "hbmspace:block/celestial_ore";
    private static final String HOST_STONE = "minecraft:block/stone";
    private static final Map<Body, String> STONES =
            new EnumMap<>(
                    Map.of(
                            Body.LAYTHE, "minecraft:block/stone",
                            Body.MUN, "hbmspace:block/moon_rock",
                            Body.MINMUS, "hbmspace:block/minmus_stone",
                            Body.DUNA, "hbmspace:block/duna_rock",
                            Body.MOHO, "hbmspace:block/moho_stone",
                            Body.DRES, "hbmspace:block/dresbase",
                            Body.EVE, "hbmspace:block/eve_stone_2",
                            Body.IKE, "hbmspace:block/ike_stone",
                            Body.TEKTO, "hbm:block/basalt"));
    private static final Identifier FIRST_NTM_ORE = Library.id("ore_uranium");

    static {
        for (CelestialOre ore : CelestialOre.values()) {
            if (ore.spec.host == CelestialOre.Host.SPACE) {
                ore.spaceHost =
                        declare(
                                HbmSpace.REG.blockItem(
                                        ore.name,
                                        factory(ore, Body.ORBIT),
                                        () -> properties(ore.spec)),
                                ore,
                                HOST_STONE);
            }
            for (Body body : ore.bodies()) {
                Reg.Handle<Block> handle =
                        ore == CelestialOre.RARE || ore == CelestialOre.AUSTRALIUM
                                ? HbmSpace.REG.blockItem(
                                        ore.id(body),
                                        factory(ore, body),
                                        () -> properties(ore.spec),
                                        (block, props) ->
                                                new BlockItem(block, props.rarity(Rarity.UNCOMMON)))
                                : HbmSpace.REG.blockItem(
                                        ore.id(body),
                                        factory(ore, body),
                                        () -> properties(ore.spec));
                ore.bodies.put(
                        body,
                        declare(
                                handle,
                                ore,
                                body == Body.ORBIT || body == Body.KERBIN
                                        ? HOST_STONE
                                        : STONES.get(body)));
            }
        }
        listInBlockTab();
        prospecting();
        ReservesSpace.install();
    }

    private CelestialOresSpace() {}

    private static void prospecting() {
        for (Body body : CelestialOre.OIL.bodies()) {
            Supplier<Block> twin = () -> CelestialOre.OIL.block(body);
            ItemSurveyScanner.alias(twin, Deposit.OIL);
            ItemModLens.alias(twin, ModBlocks.ORE_OIL::get);
        }
        for (Body body : CelestialOre.BEDROCK_OIL.bodies()) {
            Supplier<Block> twin = () -> CelestialOre.BEDROCK_OIL.block(body);
            ItemSurveyScanner.alias(twin, Deposit.BEDROCK_OIL);
            ItemModLens.alias(twin, ModBlocks.ORE_BEDROCK_OIL::get);
        }
        List<Supplier<? extends Block>> gas = withHost(CelestialOre.GAS);
        List<Supplier<? extends Block>> brine = withHost(CelestialOre.BRINE);
        ItemSurveyScanner.addLine(
                "chat.hbmspace.surveyScanner.foundGas", ChatFormatting.WHITE, gas);
        ItemSurveyScanner.addLine(
                "chat.hbmspace.surveyScanner.foundBrine", ChatFormatting.WHITE, brine);
        Component gasMarker = Component.translatable("marker.hbmspace.neutrino_lens.natural_gas");
        Component brineMarker = Component.translatable("marker.hbmspace.neutrino_lens.brine");
        for (Supplier<? extends Block> block : gas)
            ItemModLens.addTarget(block, 300, gasMarker, 0xa0a0a0);
        for (Supplier<? extends Block> block : brine)
            ItemModLens.addTarget(block, 300, brineMarker, 0xa0a0a0);
    }

    private static List<Supplier<? extends Block>> withHost(CelestialOre ore) {
        List<Supplier<? extends Block>> blocks = new ArrayList<>();
        blocks.add(ore::host);
        for (Body body : ore.bodies()) blocks.add(() -> ore.block(body));
        return blocks;
    }

    private static BlockBehaviour.Properties properties(Spec spec) {
        BlockBehaviour.Properties properties =
                BlockBehaviour.Properties.of()
                        .mapColor(MapColor.STONE)
                        .strength(spec.hardness, spec.resistance)
                        .sound(spec.morkite ? MorkiteSoundType.INSTANCE : SoundType.STONE)
                        .requiresCorrectToolForDrops();
        if (spec.light > 0) properties.lightLevel(state -> spec.light);
        if (spec.gas != CelestialOre.Gas.NONE) properties.randomTicks();
        if (spec.noSpawn) properties.isValidSpawn((state, level, pos, type) -> false);
        return properties;
    }

    private static Function<BlockBehaviour.Properties, Block> factory(CelestialOre ore, Body body) {
        Spec spec = ore.spec;
        if (spec.gas == CelestialOre.Gas.RADON)
            return p -> new BlockOutgas(p, () -> ModBlocks.GAS_RADON.get(), true);
        if (spec.gas == CelestialOre.Gas.ASBESTOS) {
            return p -> new BlockOutgas(p, () -> ModBlocks.GAS_ASBESTOS.get(), true);
        }
        if (spec.fire) return BlockOreNetherFire::new;
        if (spec.reserve != null) {
            BlockOreFluid.ReserveType reserve = spec.reserve;
            @Nullable CelestialOre emptyFamily =
                    spec.drained == null ? null : CelestialOre.valueOf(spec.drained);
            @Nullable Supplier<Block> drained =
                    emptyFamily == null ? null : () -> emptyFamily.block(body);
            return p -> new BlockOreFluid(p, drained, reserve, body, emptyFamily);
        }
        if (spec.drainedDeposit) return BlockOreFluid.Drained::new;
        return Block::new;
    }

    private static Reg.Handle<Block> declare(
            Reg.Handle<Block> handle, CelestialOre ore, String stone) {
        Spec spec = ore.spec;
        String overlay = "hbmspace:block/" + spec.overlay;
        if (spec.dict != null)
            ;
        if (spec.archetype != null)
            ;
        if (spec.needs != null)
            ;
        return handle;
    }

    private static void listInBlockTab() {
        @Nullable Identifier anchor = null;
        for (CelestialOre ore : CelestialOre.values()) {
            if (ore.spec.host == CelestialOre.Host.NTM) anchor = ore.hostId();
            if (ore.spec.host == CelestialOre.Host.SPACE) {
                if (ore.spec.after != null) anchor = Library.id(ore.spec.after);
                list(anchor, () -> new ItemStack(ore.host()));
                if (anchor != null) anchor = ore.hostId();
            }
            for (Body body : ore.bodies()) {
                Reg.Handle<? extends Block> handle = ore.bodies.get(body);
                list(anchor, () -> new ItemStack(handle.get()));
            }
        }
    }

    private static void list(@Nullable Identifier anchor, Supplier<ItemStack> entry) {
        if (anchor == null) CreativeContents.insertBefore(Tab.BLOCKS, FIRST_NTM_ORE, entry);
        else CreativeContents.insertAfter(Tab.BLOCKS, anchor, entry);
    }
}
