// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.handler;

import com.hbm.handler.ImpactWorldHandler;
import com.hbmspace.HbmSpace;

public final class ImpactEditsSpace {

    private ImpactEditsSpace() {}

    public static void install() {
        ImpactWorldHandler.EXTINCTION_LEVELS.install(HbmSpace.MOD_ID, level -> true);
    }
}
