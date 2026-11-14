/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2024 George Paton
 * SPDX-FileCopyrightText: 2023 JamesH_2
 * SPDX-FileCopyrightText: 2023 Pu-238
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2024 George Paton. All rights reserved.
 * Copyright (C) 2023 JamesH_2. All rights reserved.
 * Copyright (C) 2023 Pu-238. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License version 3 only,
 * as published by the Free Software Foundation.
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this code. If not, see <https://www.gnu.org/licenses/>.
 *
 * Under GPLv3 §7(c) and §7(e), these additional terms apply to LGPL-covered
 * material authored by movblock:
 *
 * - the license grants no rights to the name "NTM NEXT: Space";
 * - a modified version must not be named NTM NEXT: Space or presented as
 *   the official release, and must be marked as different from it.
 *
 * Accurately identifying a work as derived from NTM NEXT: Space does not
 * violate these additional terms, provided it is clearly distinguished from
 * the official release and does not imply endorsement. Ports are encouraged
 * to acknowledge NTM NEXT: Space as their upstream project and provide a
 * link to it.
 */

package com.hbmspace.hazard;

import com.hbm.data.RadiationData;
import com.hbm.extprop.LivingHazards;
import com.hbm.hazard.HazardEntry;
import com.hbm.hazard.HazardRegistry;
import com.hbm.hazard.HazardSystem;
import com.hbm.hazard.transformer.HazardTransformer;
import com.hbm.items.ModDataComponents;
import com.hbm.util.ContaminationUtil.ContaminationType;
import com.hbm.util.ContaminationUtil;
import com.hbm.util.SectionAccess;
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
                new HazardTransformer() {
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
        LivingHazards.setFieldThisSecond(
                entity, LivingHazards.fieldThisSecond(entity) + amount * 10D);
        if (entity instanceof Player player) {
            if (player.isSpectator()
                    || type.protects(player)
                    || type.spares(player)
                    || player.tickCount < 200) {
                return false;
            }
        }
        float dose = (float) (amount * type.radiationMod(entity));
        LivingHazards props = LivingHazards.getOrCreate(entity);
        props.neutron = Math.max(0F, (float) props.neutron + dose);
        return true;
    }

    public static void tick(LivingEntity entity) {
        if (entity.level().isClientSide() || !enabled() || ContaminationUtil.isRadImmune(entity))
            return;
        LivingHazards props = LivingHazards.getIfPresent(entity);
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
            float activation = (float) LivingHazards.neutron(entity);
            if (activation > 0F) {
                ContaminationUtil.contaminate(
                        entity,
                        ContaminationUtil.HazardType.RADIATION,
                        ContaminationType.RAD_BYPASS,
                        activation / 20F);
                LivingHazards.setNeutron(entity, activation * 0.899916F);
            }
        }
        if (entity instanceof Player player) decayInventory(player, 0.899916F);
    }

    public static void radiate(ServerLevel level, BlockPos source, float rad) {
        if (!enabled()) return;
        double x = source.getX() + 0.5D, y = source.getY() + 0.5D, z = source.getZ() + 0.5D;
        BlockPos.MutableBlockPos cell = new BlockPos.MutableBlockPos();
        var reader = new SectionAccess.LoadedReader(level);
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
                BlockState state = reader.read(cell);
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
