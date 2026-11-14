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

import com.hbm.integration.recipeviewer.SawmillPage;
import com.hbmspace.HbmSpace;
import com.hbmspace.blocks.machine.PolymerSawmillSpace;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SawmillPage.class)
public abstract class MixinSawmillPage {
    @ModifyReturnValue(method = "rows", at = @At("RETURN"))
    private List<SawmillPage.Recipe> hbmspace$polymerRows(List<SawmillPage.Recipe> original) {
        List<SawmillPage.Recipe> rows = new ArrayList<>(original.size() + 6);
        for (Item input : PolymerSawmillSpace.inputs()) {
            ItemStack stack = new ItemStack(input);
            rows.add(
                    new SawmillPage.Recipe(
                            Identifier.fromNamespaceAndPath(
                                    HbmSpace.MOD_ID,
                                    "sawmill/" + BuiltInRegistries.ITEM.getKey(input).getPath()),
                            List.of(stack),
                            PolymerSawmillSpace.output(stack),
                            0F));
        }
        for (SawmillPage.Recipe row : original) {
            List<ItemStack> input =
                    row.input().stream()
                            .filter(stack -> !PolymerSawmillSpace.polymer(stack))
                            .toList();
            rows.add(new SawmillPage.Recipe(row.id(), input, row.output(), row.sawdustChance()));
        }
        return List.copyOf(rows);
    }
}
