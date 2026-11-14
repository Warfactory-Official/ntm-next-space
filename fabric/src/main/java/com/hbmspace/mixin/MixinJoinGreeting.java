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

import com.hbm.NuclearTech;
import com.hbm.client.UpdateChecker;
import com.hbm.handler.JoinGreeting;
import com.hbmspace.HbmSpace;
import java.net.URI;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(JoinGreeting.class)
public abstract class MixinJoinGreeting {
    @Unique
    private static final URI CONTINUUM =
            URI.create("https://www.curseforge.com/minecraft/mc-mods/ntm-continuum");

    @Unique
    private static final URI CONTINUUM_SPACE =
            URI.create("https://www.curseforge.com/minecraft/mc-mods/ntm-continuum-space");

    @Inject(method = "message", at = @At("HEAD"), cancellable = true)
    private static void hbmspace$message(CallbackInfoReturnable<Component> cir) {
        cir.setReturnValue(
                Component.translatable(
                        "chat.hbmspace.loaded",
                        FabricLoader.getInstance()
                                .getModContainer(HbmSpace.MOD_ID)
                                .orElseThrow()
                                .getMetadata()
                                .getVersion()
                                .getFriendlyString(),
                        SharedConstants.getCurrentVersion().name(),
                        FabricLoader.getInstance()
                                .getModContainer(NuclearTech.MOD_ID)
                                .orElseThrow()
                                .getMetadata()
                                .getVersion()
                                .getFriendlyString(),
                        UpdateChecker.link(Component.literal("NTM-Continuum"), CONTINUUM),
                        UpdateChecker.link(
                                Component.literal("NTM-Continuum Space"), CONTINUUM_SPACE)));
    }
}
