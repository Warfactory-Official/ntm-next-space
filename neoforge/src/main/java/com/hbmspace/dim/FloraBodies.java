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

import com.hbmspace.dim.eve.WorldProviderEve;
import com.hbmspace.dim.laythe.WorldProviderLaythe;
import com.hbmspace.dim.tekto.WorldProviderTekto;
import com.hbmspace.dim.thatmo.WorldProviderThatmo;
import com.hbmspace.world.gen.layer.CelestialLayers;

public final class FloraBodies {

    static {
        WorldProviderCelestial.register(new WorldProviderEve());
        CelestialLayers.register(SpaceDimensions.EVE.identifier(), WorldProviderEve::layers);

        WorldProviderCelestial.register(new WorldProviderLaythe());
        CelestialLayers.register(SpaceDimensions.LAYTHE.identifier(), WorldProviderLaythe::layers);

        WorldProviderCelestial.register(new WorldProviderTekto());
        CelestialLayers.register(SpaceDimensions.TEKTO.identifier(), WorldProviderTekto::layers);

        WorldProviderCelestial.register(new WorldProviderThatmo());
    }

    private FloraBodies() {}
}
