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

package com.hbmspace.entity.mob;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.MoveControl;

public class MoonWalkControl extends MoveControl<Mob> {

    public MoonWalkControl(Mob mob) {
        super(mob);
    }

    @Override
    public void tick() {
        boolean backwards = operation == Operation.MOVE_TO || operation == Operation.JUMPING;
        super.tick();
        if (backwards) mob.setZza(-mob.zza);
    }

    @Override
    protected float rotlerp(float current, float target, float max) {
        return super.rotlerp(current, target + 180.0F, 30.0F);
    }
}
