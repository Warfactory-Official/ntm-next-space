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

package com.hbmspace.explosion;

import com.hbm.explosion.assembly.ChargeIngredients.Channel;
import com.hbm.explosion.assembly.ChargeIngredients.Contribution;
import com.hbm.explosion.assembly.ChargeIngredients.Editor;
import com.hbm.explosion.assembly.ChargeIngredients.Mode;
import com.hbm.explosion.assembly.ChargeIngredients;
import com.hbm.items.ModItems;
import com.hbmspace.HbmSpace;

public final class AntimatterEditsSpace {

    private AntimatterEditsSpace() {}

    public static void install() {
        ChargeIngredients.EDITS.install(HbmSpace.MOD_ID, AntimatterEditsSpace::entries);
    }

    private static void entries(Editor table) {
        table.remove(ModItems.EGG_BALEFIRE_SHARD.get());
        table.remove(ModItems.EGG_BALEFIRE.get());
        table.put(ModItems.PARTICLE_AMAT.get(), new Contribution(Channel.ANTIMATTER, 5F, Mode.ADD));
        table.put(
                ModItems.PELLET_ANTIMATTER.get(),
                new Contribution(Channel.ANTIMATTER, 45F, Mode.ADD));
        table.antimatterNuclearThreshold(0F);
    }
}
