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

package com.hbmspace.inventory.container;

import com.hbm.inventory.container.BlockEntityMenu;
import com.hbmspace.blockentity.machine.TransporterBaseBlockEntity;
import com.hbmspace.items.tool.TransporterInfo;
import io.netty.buffer.ByteBuf;
import java.util.List;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class TransporterLinkerMenu extends BlockEntityMenu<TransporterBaseBlockEntity> {

    public record Opening(
            InteractionHand hand, TransporterInfo from, List<TransporterInfo> transporters) {
        public static final StreamCodec<ByteBuf, Opening> STREAM_CODEC =
                StreamCodec.composite(
                        InteractionHand.STREAM_CODEC,
                        Opening::hand,
                        TransporterInfo.STREAM_CODEC,
                        Opening::from,
                        TransporterInfo.STREAM_CODEC.apply(ByteBufCodecs.list()),
                        Opening::transporters,
                        Opening::new);
    }

    public final Opening opening;
    private final ItemStack linker;

    public TransporterLinkerMenu(
            int containerId,
            Inventory inventory,
            TransporterBaseBlockEntity transporter,
            Opening opening) {
        super(StationMenusSpace.TRANSPORTER_LINKER.get(), containerId, transporter, null);
        this.opening = opening;
        this.linker = inventory.player.getItemInHand(opening.hand());
    }

    @Override
    public boolean stillValid(Player player) {
        return player.getItemInHand(opening.hand()) == linker && blockEntity().stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}
