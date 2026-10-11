// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.config;

import com.hbm.config.ConfigStore;

public final class SpaceConfig {

    public static volatile boolean showOreLocations = true;

    public static volatile boolean enableHardcoreDarkness = false;

    public static volatile float shipHumVolume = 0.1F;

    private SpaceConfig() {}

    static void loadFrom(ConfigStore client) {
        showOreLocations = client.get(SpaceConfigSchema.SHOW_ORE_LOCATIONS);
        enableHardcoreDarkness = client.get(SpaceConfigSchema.HARDCORE_DARKNESS);
        shipHumVolume = client.get(SpaceConfigSchema.SHIP_HUM_VOLUME).floatValue();
    }
}
