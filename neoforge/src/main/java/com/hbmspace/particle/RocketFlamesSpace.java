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

package com.hbmspace.particle;

import com.hbm.particle.RocketFlameColor;

public final class RocketFlamesSpace {
    public static final RocketFlameColor BALEFIRE =
            new RocketFlameColor(0F, 1F, 0F, false, false, false, 240);
    public static final RocketFlameColor FUSION =
            new RocketFlameColor(1F, 1F, 1F, false, false, true, 240);
    public static final RocketFlameColor MUD =
            new RocketFlameColor(1F, 1F, 0F, false, false, false, 20);
    public static final RocketFlameColor SCHRABIDIUM =
            new RocketFlameColor(1F, 1F, 1F, true, false, false, 240);
    public static final RocketFlameColor URANIUM_PLUTONIUM =
            new RocketFlameColor(0F, 1F, 0F, false, false, false, 80);

    private RocketFlamesSpace() {}
}
