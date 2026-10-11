// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.inventory.container;

import com.hbm.packet.PayloadHandlerThread;
import com.hbm.platform.FabricNetwork;
import com.hbm.registration.FabricRegistrar;
import com.hbm.registration.RegistryHandle;
import com.hbmspace.HbmSpace;
import com.hbmspace.blockentity.bomb.BlockEntityLaunchPadRocket;
import com.hbmspace.blockentity.machine.BlockEntityMachineDriveProcessor;
import com.hbmspace.blockentity.machine.BlockEntityMachineRocketAssembly;
import com.hbmspace.blockentity.machine.BlockEntityMachineStardar;
import com.hbmspace.blockentity.machine.StardarHeightmapPayload;
import net.minecraft.world.inventory.MenuType;

public final class RocketryMenusSpace {

    public static final RegistryHandle<MenuType<MenuMachineRocketAssembly>>
            MACHINE_ROCKET_ASSEMBLY =
                    FabricRegistrar.registerBlockEntityMenu(
                            HbmSpace.MOD_ID + ":machine_rocket_assembly",
                            BlockEntityMachineRocketAssembly.class);

    public static final RegistryHandle<MenuType<MenuLaunchPadRocket>> LAUNCH_PAD_ROCKET =
            FabricRegistrar.registerBlockEntityMenu(
                    HbmSpace.MOD_ID + ":launch_pad_rocket", BlockEntityLaunchPadRocket.class);

    public static final RegistryHandle<MenuType<MenuMachineDriveProcessor>>
            MACHINE_DRIVE_PROCESSOR =
                    FabricRegistrar.registerBlockEntityMenu(
                            HbmSpace.MOD_ID + ":machine_drive_processor",
                            BlockEntityMachineDriveProcessor.class);

    public static final RegistryHandle<MenuType<MenuMachineStardar>> MACHINE_STARDAR =
            FabricRegistrar.registerBlockEntityMenu(
                    HbmSpace.MOD_ID + ":machine_stardar", BlockEntityMachineStardar.class);

    static {
        FabricNetwork.clientbound.add(
                new FabricNetwork.Registration<>(
                        StardarHeightmapPayload.TYPE,
                        StardarHeightmapPayload.STREAM_CODEC,
                        StardarHeightmapPayload::handle,
                        PayloadHandlerThread.MAIN));
    }

    private RocketryMenusSpace() {}
}
