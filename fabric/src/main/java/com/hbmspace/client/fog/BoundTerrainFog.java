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

package com.hbmspace.client.fog;

import com.mojang.renderpearl.api.buffers.GpuBuffer;

public interface BoundTerrainFog extends TerrainFogPayload {
    TerrainFogWriter hbmspace$writer();

    void hbmspace$writer(TerrainFogWriter writer);

    GpuBuffer hbmspace$buffer();

    long hbmspace$offset();

    long hbmspace$length();

    void hbmspace$binding(GpuBuffer buffer, long offset, long length);

    void hbmspace$cullDistance(float distance);

    boolean hbmspace$cullOverridden();
}
