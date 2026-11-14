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

import com.hbm.packet.PayloadHandlerContext;
import net.minecraft.core.BlockPos;

public final class StardarHeightmapActions {

    private StardarHeightmapActions() {}

    public static int[] heights(PayloadHandlerContext context, BlockPos pos) {
        if (context.player().level().getBlockEntity(pos) instanceof StardarBlockEntity stardar
                && stardar.heightmap != null) return stardar.heightmap;
        return new int[StardarBlockEntity.MAP_SIZE * StardarBlockEntity.MAP_SIZE];
    }

    public static void receive(
            PayloadHandlerContext context, BlockPos pos, boolean heightsPresent, int[] heights) {
        if (context.player().level().getBlockEntity(pos) instanceof StardarBlockEntity stardar) {
            stardar.receiveHeightmap(heightsPresent ? heights : null);
        }
    }
}
