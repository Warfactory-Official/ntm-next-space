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

package com.hbmspace.blockentity.machine;

import com.hbm.blockentity.BlockEntityTicks;
import com.hbm.blockentity.machine.LPW2BlockEntity;
import com.hbm.blocks.ModBlockEntities;
import com.hbm.registration.Reg;

public final class LPW2EngineSpace {
    private LPW2EngineSpace() {}

    public static void install() {
        var handle = (Reg.BeHandle<LPW2BlockEntity>) ModBlockEntities.LPW2;
        handle.ticksClientAt(BlockEntityTicks.CLIENT)
                .ticksServerAt(BlockEntityTicks.SERVER_WITH_SOUND);
    }
}
