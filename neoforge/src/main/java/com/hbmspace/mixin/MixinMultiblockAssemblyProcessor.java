// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

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
