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

package com.hbmspace.blocks.machine;

import com.hbm.blocks.machine.ChungusBlock;
import com.hbm.blocks.machine.PoweredCondenserBlock;
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
                    || !(info.state().getBlock() instanceof ChungusBlock
                            || info.state().getBlock() instanceof PoweredCondenserBlock)) continue;
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
