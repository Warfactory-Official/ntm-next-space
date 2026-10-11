// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.handler.atmosphere;

import com.hbmspace.dim.trait.CBT_Atmosphere;
import org.jspecify.annotations.Nullable;

public interface IPlantableBreathing {

    boolean canBreathe(@Nullable CBT_Atmosphere atmosphere);
}
