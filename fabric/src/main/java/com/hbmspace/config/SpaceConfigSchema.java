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

package com.hbmspace.config;

import com.hbm.config.ConfigEntry.Domain;
import com.hbm.config.ConfigEntry;
import com.hbm.config.ConfigSchemaExtension;
import com.hbm.config.ConfigStore;
import java.util.List;

public final class SpaceConfigSchema implements ConfigSchemaExtension {
    static final ConfigEntry<Boolean> SHOW_ORE_LOCATIONS =
            ConfigEntry.bool(
                    Domain.CLIENT,
                    "space",
                    "show_ore_locations",
                    true,
                    "Should ores indicate which planets they can be found on.");
    static final ConfigEntry<Boolean> HARDCORE_DARKNESS =
            ConfigEntry.bool(
                    Domain.CLIENT,
                    "space",
                    "hardcore_darkness",
                    false,
                    "If enabled, sets night-time minimum fog to zero, to complement hardcore darkness mods");
    static final ConfigEntry<Double> SHIP_HUM_VOLUME =
            ConfigEntry.real(
                    Domain.CLIENT, "audio", "ship_hum_volume", 0.1D, 0D, Float.MAX_VALUE, "");

    @Override
    public List<ConfigEntry<?>> entries() {
        return List.of(SHOW_ORE_LOCATIONS, HARDCORE_DARKNESS, SHIP_HUM_VOLUME);
    }

    @Override
    public void loadClient(ConfigStore client) {
        SpaceConfig.loadFrom(client);
    }
}
