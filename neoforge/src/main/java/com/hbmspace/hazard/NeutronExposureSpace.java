// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.hazard;

import com.hbm.data.RadiationData;
import com.hbm.extprop.HbmLivingProps;
import com.hbm.handler.ArmorUtil;
import com.hbm.hazard.HazardEntry;
import com.hbm.hazard.HazardRegistry;
import com.hbm.hazard.HazardSystem;
import com.hbm.hazard.transformer.IHazardTransformer;
import com.hbm.items.ModDataComponents;
import com.hbm.util.ChunkUtil;
import com.hbm.util.ContaminationUtil.ContaminationType;
import com.hbm.util.ContaminationUtil;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class NeutronExposureSpace {
    public static final HazardTypeNeutronSpace NEUTRON = new HazardTypeNeutronSpace();

    private NeutronExposureSpace() {}

    public static void register() {
        HazardRegistry.registerType(
                Identifier.fromNamespaceAndPath("hbmspace", "neutron"), NEUTRON);
        HazardSystem.addTransformer(
                new IHazardTransformer() {
                    @Override
                    public boolean readsLiveStorage() {
                        return true;
                    }

                    @Override
                    public boolean appliesTo(ItemStack stack) {
                        return activation(stack) > 0F
                                && !stack.has(ModDataComponents.HAZ_RADIATION.get());
                    }

                    @Override
                    public void transform(ItemStack stack, List<HazardEntry> entries) {
                        entries.add(new HazardEntry(HazardRegistry.RADIATION, activation(stack)));
                    }
                });
    }

    public static boolean enabled() {
        return RadiationData.NEUTRON_ACTIVATION.get();
    }

    public static float activation(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.NEUTRON_ACTIVATION.get(), 0F);
    }

    public static void apply(ItemStack stack, float rad) {
        if (stack.isEmpty()) return;
        stack.set(
                ModDataComponents.NEUTRON_ACTIVATION.get(),
                activation(stack) + (rad / stack.getCount()) / 10F);
    }

    public static void decay(ItemStack stack, float factor) {
        if (stack.isEmpty() || !stack.has(ModDataComponents.NEUTRON_ACTIVATION.get())) return;
        float next = activation(stack) * factor;
        if (next < 1E-5F) stack.remove(ModDataComponents.NEUTRON_ACTIVATION.get());
        else stack.set(ModDataComponents.NEUTRON_ACTIVATION.get(), next);
    }

    public static void applyInventory(Player player, float rad) {
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot <= Inventory.SLOT_OFFHAND; slot++)
            apply(inventory.getItem(slot), rad);
    }

    public static void decayInventory(Player player, float factor) {
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot <= Inventory.SLOT_OFFHAND; slot++)
            decay(inventory.getItem(slot), factor);
    }

    public static boolean contaminate(LivingEntity entity, ContaminationType type, double amount) {
        if (entity == null || entity.level().isClientSide() || !enabled()) return false;
        HbmLivingProps.setRadEnv(entity, HbmLivingProps.getRadEnv(entity) + amount * 10D);
        if (entity instanceof Player player) {
            if (player.isSpectator()) return false;
            boolean protectedFrom =
                    switch (type) {
                        case FARADAY -> ArmorUtil.checkForFaraday(player);
                        case HAZMAT -> ArmorUtil.checkForHazmat(player);
                        case HAZMAT2 -> ArmorUtil.checkForHaz2(player);
                        case DIGAMMA ->
                                ArmorUtil.checkForDigamma(player)
                                        || ArmorUtil.checkForDigamma2(player);
                        case DIGAMMA2 -> ArmorUtil.checkForDigamma2(player);
                        default -> false;
                    };
            if (protectedFrom
                    || player.isCreative()
                            && type != ContaminationType.NONE
                            && type != ContaminationType.DIGAMMA2
                    || player.tickCount < 200) return false;
        }
        float dose =
                (float)
                        (amount
                                * (type == ContaminationType.RAD_BYPASS
                                        ? 1D
                                        : ContaminationUtil.calculateRadiationMod(entity)));
        HbmLivingProps props = HbmLivingProps.getData(entity);
        props.neutron = Math.max(0F, (float) props.neutron + dose);
        return true;
    }

    public static void tick(LivingEntity entity) {
        if (entity.level().isClientSide() || !enabled() || ContaminationUtil.isRadImmune(entity))
            return;
        HbmLivingProps props = HbmLivingProps.peek(entity);
        if (props == null) return;
        float activation = (float) props.neutron;
        if (activation > 0F) {
            ContaminationUtil.contaminate(
                    entity,
                    ContaminationUtil.HazardType.RADIATION,
                    ContaminationType.RAD_BYPASS,
                    activation / 20F);
            props.neutron = activation * 0.998816F;
        }
        if (activation < 1E-5F) props.neutron = 0D;
    }

    public static void decon(LivingEntity entity) {
        if (enabled()) {
            float activation = (float) HbmLivingProps.getNeutron(entity);
            if (activation > 0F) {
                ContaminationUtil.contaminate(
                        entity,
                        ContaminationUtil.HazardType.RADIATION,
                        ContaminationType.RAD_BYPASS,
                        activation / 20F);
                HbmLivingProps.setNeutron(entity, activation * 0.899916F);
            }
        }
        if (entity instanceof Player player) decayInventory(player, 0.899916F);
    }

    public static void radiate(ServerLevel level, BlockPos source, float rad) {
        if (!enabled()) return;
        double x = source.getX() + 0.5D, y = source.getY() + 0.5D, z = source.getZ() + 0.5D;
        BlockPos.MutableBlockPos cell = new BlockPos.MutableBlockPos();
        for (LivingEntity entity :
                level.getEntitiesOfClass(
                        LivingEntity.class, new AABB(x, y, z, x, y, z).inflate(25D))) {
            Vec3 delta = new Vec3(entity.getX() - x, entity.getEyeY() - y, entity.getZ() - z);
            double length = delta.length();
            Vec3 direction = delta.normalize();
            float resistance = 0F;
            boolean loaded = true;
            for (int step = 1; step < length; step++) {
                cell.set(x + direction.x * step, y + direction.y * step, z + direction.z * step);
                BlockState state = ChunkUtil.blockStateIfLoaded(level, cell);
                if (state == null) {
                    loaded = false;
                    break;
                }
                resistance += state.getBlock().getExplosionResistance();
            }
            if (!loaded) continue;
            if (resistance < 1F) resistance = 1F;
            float dose = rad;
            dose /= resistance;
            if (length != 0D) dose /= (float) (length * length);
            irradiate(entity, dose);
        }
    }

    public static void irradiate(LivingEntity entity, float dose) {
        if (!enabled()) return;
        contaminate(entity, ContaminationType.CREATIVE, dose);
        if (entity instanceof Player player) applyInventory(player, dose);
    }
}
