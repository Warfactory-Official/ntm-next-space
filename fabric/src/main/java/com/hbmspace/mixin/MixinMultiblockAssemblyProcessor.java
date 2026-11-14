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

import com.hbm.world.gen.nbt.MultiblockAssemblyProcessor;
import com.hbmspace.blocks.machine.ThermalTemplateDamage;
import java.util.List;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(MultiblockAssemblyProcessor.class)
public abstract class MixinMultiblockAssemblyProcessor {
    @ModifyVariable(method = "finalizeProcessing", at = @At("HEAD"), argsOnly = true, index = 5)
    private List<StructureTemplate.StructureBlockInfo> hbmspace$damagedThermalCores(
            List<StructureTemplate.StructureBlockInfo> processed) {
        return ThermalTemplateDamage.mark(processed);
    }
}
