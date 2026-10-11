// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.mixin.client;

import com.hbm.blockentity.machine.BlockEntityChungus;
import com.hbm.blockentity.machine.BlockEntityCondenserPowered;
import com.hbm.blocks.ModBlockEntities;
import com.hbm.client.ClientRegistry;
import com.hbm.entity.ModEntities;
import com.hbm.entity.item.EntityTntNtm;
import com.hbmspace.client.render.RenderChungusSpace;
import com.hbmspace.client.render.RenderCondenserPoweredSpace;
import com.hbmspace.client.render.RenderTntSpin;
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
        EntityRendererProvider<EntityTntNtm> spin = context -> new RenderTntSpin(context);
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
        BlockEntityRendererProvider<BlockEntityChungus, RenderChungusSpace.State> chungus =
                context -> new RenderChungusSpace();
        BlockEntityRendererProvider<BlockEntityCondenserPowered, RenderCondenserPoweredSpace.State>
                condenser = context -> new RenderCondenserPoweredSpace();
        original.call(
                reg,
                type,
                type == ModBlockEntities.CHUNGUS.get()
                        ? chungus
                        : type == ModBlockEntities.CONDENSER_POWERED.get() ? condenser : provider);
    }
}
