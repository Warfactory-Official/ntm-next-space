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

package com.hbmspace.dim;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.FlatLevelSource;

public class WorldProviderEarth extends WorldProviderCelestial {

    public WorldProviderEarth() {
        super(Level.OVERWORLD);
    }

    @Override
    public boolean hasLife() {
        return true;
    }

    @Override
    public double getDayLength() {
        return 24_000D;
    }

    @Override
    public int getHorizon(Level level) {
        return level instanceof ServerLevel server
                        && server.getChunkSource().getGenerator() instanceof FlatLevelSource
                ? level.getMinY()
                : 63;
    }
}
