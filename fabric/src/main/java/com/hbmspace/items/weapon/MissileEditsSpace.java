// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.items.weapon;

import com.hbm.blockentity.bomb.BlockEntityCompactLauncher.AddonFuel;
import com.hbm.blockentity.bomb.BlockEntityCompactLauncher;
import com.hbm.handler.MissileStruct;
import com.hbm.items.ModItems;
import com.hbm.items.weapon.ItemCustomMissile;
import com.hbm.items.weapon.ItemCustomMissilePart.FuelType;
import com.hbm.items.weapon.ItemCustomMissilePart.PartSize;
import com.hbm.items.weapon.ItemCustomMissilePart.PartType;
import com.hbm.items.weapon.ItemCustomMissilePart;
import com.hbm.registration.FabricRegistrar;
import com.hbm.registration.Reg;
import com.hbm.registration.RegistryHandle;
import com.hbmspace.HbmSpace;
import com.hbmspace.handler.RocketPart;
import com.hbmspace.inventory.fluid.FluidsSpace;
import java.util.Locale;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import org.jspecify.annotations.Nullable;

import static com.hbm.items.weapon.ItemCustomMissilePart.stat;

public final class MissileEditsSpace {
    public static final float KEROSENE_20_HEIGHT = 2F;
    public static final float KEROSENE_20_GUI_HEIGHT = 2F;

    private MissileEditsSpace() {}

    public static void install() {
        ItemCustomMissilePart.DESCRIBE.install(HbmSpace.MOD_ID, MissileEditsSpace::describe);
        ItemCustomMissile.DESCRIBE.install(HbmSpace.MOD_ID, MissileEditsSpace::describeMissile);
        ItemCustomMissilePart.VERTEX_NORMAL_MESHES.install(
                HbmSpace.MOD_ID, MissilePartsSpace.VERTEX_NORMAL_MESHES);
        Reg.afterRegistration(
                ModItems.MP_THRUSTER_20_KEROSENE,
                part ->
                        part.setAssembly(
                                KEROSENE_20_HEIGHT, KEROSENE_20_GUI_HEIGHT, part.mesh, part.skin));
        BlockEntityCompactLauncher.addFuel(
                new AddonFuel(MissileKindsSpace.HYDRAZINE, FluidsSpace::hydrazine, null));

        for (RegistryHandle<? extends Item> handle : ModItems.MISSILE_ALL) {
            FabricRegistrar.modifyDefaultComponents(
                    handle,
                    components -> {
                        if (stacks(handle.get())) components.set(DataComponents.MAX_STACK_SIZE, 64);
                        if (handle.get() instanceof ItemCustomMissilePart part && restyled(part)) {
                            components.set(DataComponents.ITEM_MODEL, restyle(handle.id()));
                        }
                    });
        }
    }

    public static boolean restyled(ItemCustomMissilePart part) {
        return part.type == PartType.FUSELAGE || part == ModItems.MP_THRUSTER_20_KEROSENE.get();
    }

    public static Identifier restyle(Identifier ntm) {
        return Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, ntm.getPath());
    }

    private static boolean stacks(Item item) {
        if (!(item instanceof ItemCustomMissilePart part)) return false;
        return part.type == PartType.THRUSTER
                || part.type == PartType.FUSELAGE
                || item == ModItems.MP_WARHEAD_15_NUCLEAR_SHARK.get()
                || item == ModItems.MP_WARHEAD_15_NUCLEAR_MIMI.get();
    }

    static void describe(ItemCustomMissilePart part, Consumer<Component> adder) {
        if (part == MissilePartsSpace.RP_POD_20.get()) return;

        if (part.title() != null) {
            adder.accept(
                    Component.literal("\"" + part.title() + "\"")
                            .withStyle(ChatFormatting.DARK_PURPLE));
        }

        RocketPart rocket = RocketPart.of(part);
        String mass = (rocket == null ? 0 : rocket.mass) + "kg";

        switch (part.type) {
            case CHIP ->
                    adder.accept(
                            stat("inaccuracy", Component.literal(part.inaccuracy() * 100 + "%")));
            case WARHEAD -> {
                adder.accept(stat("size", part.bottom.getDisplay()));
                adder.accept(stat("type", part.warheadType().getDisplay()));
                if (part.warheadType() != MissileKindsSpace.APOLLO
                        && part.warheadType() != MissileKindsSpace.SATELLITE) {
                    adder.accept(
                            stat(
                                    "strength",
                                    Component.literal(String.valueOf(part.warheadStrength()))));
                }
                adder.accept(space("mass", mass));
            }
            case FUSELAGE -> {
                adder.accept(stat("topSize", part.top.getDisplay()));
                adder.accept(stat("bottomSize", part.bottom.getDisplay()));
                adder.accept(stat("fuelType", fuelName(part.fuelType())));
                adder.accept(stat("fuelAmount", Component.literal((int) part.fuelAmount() + "mB")));
                adder.accept(space("mass", mass));
            }
            case FINS -> {
                adder.accept(stat("size", part.top.getDisplay()));
                adder.accept(stat("inaccuracy", Component.literal(part.inaccuracy() * 100 + "%")));
            }
            case THRUSTER -> {
                adder.accept(stat("size", part.top.getDisplay()));
                adder.accept(stat("fuelType", fuelName(part.fuelType())));
                adder.accept(
                        stat("fuelConsumption", Component.literal(part.consumption() + "l/tick")));
                adder.accept(
                        stat(
                                "maxPayload",
                                Component.literal((Float) part.attributes[2] * 1000 + "kg")));
                adder.accept(space("thrust", (rocket == null ? 0 : rocket.thrust) + "N"));
                adder.accept(space("isp", (rocket == null ? 0 : rocket.isp) + "s"));
                adder.accept(space("mass", mass));
            }
        }

        if (part.rarity != null) adder.accept(stat("rarity", part.rarity.getDisplay()));
        if (part.author() != null) {
            adder.accept(
                    Component.translatable("item.hbm.missile.part.by")
                            .append(" " + part.author())
                            .withStyle(ChatFormatting.WHITE));
        }
        if (part.witty() != null) {
            adder.accept(
                    Component.literal("\"" + part.witty() + "\"")
                            .withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC));
        }
    }

    static void describeMissile(
            MissileStruct parts, @Nullable ItemCustomMissilePart chip, Consumer<Component> adder) {
        ItemCustomMissilePart warhead = parts.warhead();
        ItemCustomMissilePart fuselage = parts.fuselage();
        if (warhead != null) {
            adder.accept(ItemCustomMissile.stat("warhead", warhead.warheadType().getDisplay()));
            adder.accept(
                    ItemCustomMissile.stat(
                            "strength",
                            Component.literal(String.valueOf(warhead.warheadStrength()))));
        }
        if (fuselage != null) {
            adder.accept(ItemCustomMissile.stat("fuelType", fuelName(fuselage.fuelType())));
            adder.accept(
                    ItemCustomMissile.stat(
                            "fuelAmount", Component.literal((int) fuselage.fuelAmount() + "l")));
        }

        adder.accept(
                ItemCustomMissile.stat(
                        "chipInaccuracy",
                        Component.literal((chip != null ? chip.inaccuracy() * 100 : 100F) + "%")));
        adder.accept(
                ItemCustomMissile.stat(
                        "finInaccuracy",
                        Component.literal(
                                parts.fins() != null
                                        ? parts.fins().inaccuracy() * 100 + "%"
                                        : "100%")));
        if (fuselage != null) {
            adder.accept(
                    ItemCustomMissile.stat(
                            "size",
                            fuselage.top
                                    .getDisplay()
                                    .copy()
                                    .append("/")
                                    .append(fuselage.bottom.getDisplay())));
        }

        float health = 0;
        if (warhead != null) health += warhead.health;
        if (fuselage != null) health += fuselage.health;
        if (parts.thruster() != null) health += parts.thruster().health;
        if (parts.fins() != null) health += parts.fins().health;
        adder.accept(ItemCustomMissile.stat("health", Component.literal(health + "HP")));
    }

    public static void describeSatellite(int mass, Consumer<Component> adder) {
        adder.accept(stat("size", PartSize.SIZE_20.getDisplay()));
        adder.accept(stat("type", MissileKindsSpace.SATELLITE.getDisplay()));
        adder.accept(space("mass", mass + "kg"));
    }

    private static Component fuelName(FuelType fuel) {
        ChatFormatting color;
        if (fuel == FuelType.KEROSENE) color = ChatFormatting.LIGHT_PURPLE;
        else if (fuel == FuelType.SOLID) color = ChatFormatting.GOLD;
        else if (fuel == FuelType.HYDROGEN) color = ChatFormatting.DARK_AQUA;
        else if (fuel == FuelType.XENON) color = ChatFormatting.DARK_PURPLE;
        else if (fuel == FuelType.BALEFIRE) color = ChatFormatting.GREEN;
        else return fuel.getDisplay();
        return Component.translatable(
                        "item.hbmspace.custom_missile_part.fuel."
                                + fuel.name().toLowerCase(Locale.ROOT))
                .withStyle(color);
    }

    private static Component space(String key, String value) {
        return Component.translatable("item.hbmspace.missile.part." + key)
                .withStyle(ChatFormatting.BOLD)
                .append(Component.literal(": ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(value));
    }
}
