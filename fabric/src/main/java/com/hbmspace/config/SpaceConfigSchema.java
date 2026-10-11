// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

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
