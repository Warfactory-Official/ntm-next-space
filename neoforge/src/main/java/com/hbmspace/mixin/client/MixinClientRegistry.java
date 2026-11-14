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

package com.hbmspace.mixin.client;

import com.hbm.blockentity.machine.thermal.LeviathanTurbineBlockEntity;
import com.hbm.blockentity.machine.thermal.PoweredCondenserBlockEntity;
import com.hbm.blockentity.machine.thermal.PoweredCondenserBlockEntity;
import com.hbm.blocks.ModBlockEntities;
import com.hbm.client.ClientRegistry;
import com.hbm.entity.ModEntities;
import com.hbm.entity.item.TntNtmEntity;
import com.hbmspace.client.render.ChungusSpaceRenderer;
import com.hbmspace.client.render.CondenserPoweredSpaceRenderer;
import com.hbmspace.client.render.TntSpinRenderer;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientRegistry.class)
public abstract class MixinClientRegistry {
    @WrapOperation(
            method = "registerEntityRenderers",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lcom/hbm/client/ClientRegistry$EntityRendererRegistrar;accept(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/client/renderer/entity/EntityRendererProvider;)V"))
    private static void hbmspace$spinningTnt(
            ClientRegistry.EntityRendererRegistrar reg,
            EntityType<?> type,
            EntityRendererProvider<?> provider,
            Operation<Void> original) {
        EntityRendererProvider<TntNtmEntity> spin = context -> new TntSpinRenderer(context);
        original.call(reg, type, type == ModEntities.TNT_NTM.get() ? spin : provider);
    }

    @WrapOperation(
            method = "registerBlockEntityRenderers",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lcom/hbm/client/ClientRegistry$BlockEntityRendererRegistrar;accept(Lnet/minecraft/world/level/block/entity/BlockEntityType;Lnet/minecraft/client/renderer/blockentity/BlockEntityRendererProvider;)V"))
    private static void hbmspace$thermalDamage(
            ClientRegistry.BlockEntityRendererRegistrar reg,
            BlockEntityType<?> type,
            BlockEntityRendererProvider<?, ?> provider,
            Operation<Void> original) {
        BlockEntityRendererProvider<LeviathanTurbineBlockEntity, ChungusSpaceRenderer.State>
                chungus = context -> new ChungusSpaceRenderer();
        BlockEntityRendererProvider<
                        PoweredCondenserBlockEntity, CondenserPoweredSpaceRenderer.State>
                condenser = context -> new CondenserPoweredSpaceRenderer();
        original.call(
                reg,
                type,
                type == ModBlockEntities.CHUNGUS.get()
                        ? chungus
                        : type == ModBlockEntities.CONDENSER_POWERED.get() ? condenser : provider);
    }
}
