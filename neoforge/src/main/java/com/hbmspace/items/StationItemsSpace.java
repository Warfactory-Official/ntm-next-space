// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.items;

import com.hbm.items.CreativeContents.Tab;
import com.hbm.items.CreativeContents;
import com.hbm.lib.Library;
import com.hbm.packet.PayloadHandlerThread;
import com.hbm.platform.NeoForgeNetwork;
import com.hbm.registration.NeoForgeRegistrar;
import com.hbm.registration.Reg;
import com.hbm.registration.RegistryHandle;
import com.hbmspace.HbmSpace;
import com.hbmspace.items.tool.ItemTransporterLinker;
import com.hbmspace.items.tool.TransporterLinkerPayload;
import java.util.List;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class StationItemsSpace {
    public static final RegistryHandle<DataComponentType<List<GlobalPos>>> TRANSPORTER_LINKS =
            NeoForgeRegistrar.registerDataComponent(
                    HbmSpace.MOD_ID + ":transporter_links",
                    DataComponentType.<List<GlobalPos>>builder()
                            .persistent(GlobalPos.CODEC.listOf())
                            .networkSynchronized(GlobalPos.STREAM_CODEC.apply(ByteBufCodecs.list()))
                            .build());

    public static final Reg.Handle<ItemTransporterLinker> TRANSPORTER_LINKER =
            HbmSpace.REG.item(
                    "transporter_linker",
                    ItemTransporterLinker::new,
                    () -> new Item.Properties().stacksTo(1));

    static {
        CreativeContents.insertAfter(
                Tab.CONTROL,
                Library.id("inf_water_mk2"),
                () -> new ItemStack(TRANSPORTER_LINKER.get()));
        NeoForgeNetwork.clientbound.add(
                new NeoForgeNetwork.Registration<>(
                        TransporterLinkerPayload.TYPE,
                        TransporterLinkerPayload.STREAM_CODEC,
                        TransporterLinkerPayload::handle,
                        PayloadHandlerThread.MAIN));
    }

    private StationItemsSpace() {}
}
