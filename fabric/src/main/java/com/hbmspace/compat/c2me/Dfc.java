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

package com.hbmspace.compat.c2me;

import com.hbmspace.world.gen.density.BedrockFloor;
import com.hbmspace.world.gen.density.CelestialBiomes;
import com.hbmspace.world.gen.density.CelestialRelief;
import com.hbmspace.world.gen.density.LimitNoise;
import com.hbmspace.world.gen.density.NoiseCaves;
import com.ishland.c2me.opts.dfc.common.ast.McToAst;
import com.ishland.c2me.opts.dfc.common.ast.misc.DelegateNode;
import com.ishland.c2me.opts.dfc.common.gen.dot.DotEmitter;
import com.ishland.c2me.opts.dfc.common.gen.dot.DotGenRegistry;
import com.ishland.c2me.opts.dfc.common.gen.jvm.BytecodeGenRegistry;
import com.ishland.c2me.opts.dfc.common.gen.jvm.emitters.misc.DelegateNodeBytecodeEmitter;
import net.minecraft.world.level.levelgen.DensityFunction;

final class Dfc {

    private Dfc() {}

    static void register() {
        McToAst.REGISTRY.registerExactMatch(
                NoiseCaves.class, caves -> McToAst.toAst(caves.caves()));
        McToAst.REGISTRY.registerExactMatch(BedrockFloor.class, Node::new);
        McToAst.REGISTRY.registerExactMatch(LimitNoise.class, Node::new);
        McToAst.REGISTRY.registerExactMatch(CelestialRelief.class, Node::new);
        McToAst.REGISTRY.registerExactMatch(CelestialBiomes.class, Node::new);
        BytecodeGenRegistry.REGISTRY.registerExactMatch(
                Node.class, DelegateNodeBytecodeEmitter.instance());
        DotGenRegistry.REGISTRY.registerExactMatch(
                Node.class,
                (DotEmitter<Node>)
                        (node, context, builder) ->
                                builder.trapeziumShape()
                                        .label("hbmspace " + node.getDelegate())
                                        .build());
    }

    static final class Node extends DelegateNode {

        Node(DensityFunction function) {
            super(function);
        }
    }
}
