// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blocks.machine;

import com.hbm.blocks.machine.MachineChungus;
import com.hbm.blocks.machine.MachineCondenserPowered;
import com.hbmspace.blockentity.machine.ThermalMachineDamage;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public final class ThermalTemplateDamage {
    private ThermalTemplateDamage() {}

    public static List<StructureTemplate.StructureBlockInfo> mark(
            List<StructureTemplate.StructureBlockInfo> source) {
        List<StructureTemplate.StructureBlockInfo> damaged = null;
        for (int i = 0; i < source.size(); i++) {
            var info = source.get(i);
            if (info.nbt() == null
                    || !(info.state().getBlock() instanceof MachineChungus
                            || info.state().getBlock() instanceof MachineCondenserPowered))
                continue;
            if (damaged == null) damaged = new ArrayList<>(source);
            CompoundTag tag = info.nbt().copy();
            tag.putBoolean("damaged", true);
            damaged.set(
                    i,
                    new StructureTemplate.StructureBlockInfo(
                            info.pos(),
                            info.state().setValue(ThermalMachineDamage.DAMAGED, true),
                            tag));
        }
        return damaged == null ? source : damaged;
    }
}
