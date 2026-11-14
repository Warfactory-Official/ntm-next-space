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

package com.hbmspace.mixin;

import com.hbm.blockentity.travel.TeleporterBlockEntity;
import com.hbm.blocks.machine.TeleporterBlock;
import com.hbm.capability.MachineCaps;
import com.hbm.interfaces.LookOverlay;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TeleporterBlock.class)
public abstract class MixinMachineTeleporter {

    @Inject(method = "caps", at = @At("RETURN"), cancellable = true)
    private void hbmspace$receiveNmass(CallbackInfoReturnable<MachineCaps> cir) {
        cir.setReturnValue(cir.getReturnValue().fluidIn());
    }

    @Inject(method = "buildLookOverlay", at = @At("HEAD"), cancellable = true)
    private void hbmspace$crossingFuelLine(
            Level level, BlockPos pos, LookOverlay info, CallbackInfo ci) {
        if (!(level.getBlockEntity(pos) instanceof TeleporterBlockEntity tele)
                || tele.target == null
                || level.dimension().equals(tele.target.dimension())) return;

        info.title(((TeleporterBlock) (Object) this).getName());
        info.row(
                Component.literal(
                                String.format(Locale.ROOT, "%,d", tele.power)
                                        + " / "
                                        + String.format(
                                                Locale.ROOT,
                                                "%,d",
                                                TeleporterBlockEntity.MAX_POWER))
                        .withStyle(
                                tele.power >= TeleporterBlockEntity.CONSUMPTION
                                        ? ChatFormatting.GREEN
                                        : ChatFormatting.RED));
        var tank = ((com.hbmspace.blockentity.machine.SpaceTeleporterTank) (tele)).hbmspace$tank();
        info.row(
                Component.translatable(
                        "overlay.hbm.tank",
                        Component.translatable("block.hbmspace.nmass"),
                        tank.getFill(),
                        tank.getMaxFill()));
        info.row(
                Component.translatable(
                        "overlay.hbm.destination",
                        tele.target.pos().getX(),
                        tele.target.pos().getY(),
                        tele.target.pos().getZ(),
                        tele.target.dimension().identifier().toString()));
        ci.cancel();
    }
}
