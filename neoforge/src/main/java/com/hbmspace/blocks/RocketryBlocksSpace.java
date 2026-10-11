// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blocks;

import com.hbm.blockentity.BlockEntityMachineBase;
import com.hbm.items.CreativeContents.Tab;
import com.hbm.items.CreativeContents;
import com.hbm.items.DescBlockItem;
import com.hbm.lib.Library;
import com.hbm.registration.Reg;
import com.hbmspace.HbmSpace;
import com.hbmspace.blockentity.bomb.BlockEntityCombatDropPod;
import com.hbmspace.blockentity.bomb.BlockEntityLaunchPadRocket;
import com.hbmspace.blockentity.machine.BlockEntityMachineDriveProcessor;
import com.hbmspace.blockentity.machine.BlockEntityMachineRocketAssembly;
import com.hbmspace.blockentity.machine.BlockEntityMachineStardar;
import com.hbmspace.blocks.bomb.CombatDropPod;
import com.hbmspace.blocks.bomb.LaunchPadRocket;
import com.hbmspace.blocks.machine.MachineDriveProcessor;
import com.hbmspace.blocks.machine.MachineRocketAssembly;
import com.hbmspace.blocks.machine.MachineStardar;
import java.util.Set;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public final class RocketryBlocksSpace {
    public static final Reg.Handle<MachineRocketAssembly> MACHINE_ROCKET_ASSEMBLY =
            HbmSpace.REG.blockItem(
                    "machine_rocket_assembly",
                    MachineRocketAssembly::new,
                    () ->
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.METAL)
                                    .strength(5.0F, 6.0F)
                                    .requiresCorrectToolForDrops()
                                    .noOcclusion(),
                    (block, props) -> new DescBlockItem(block, props, 3));

    public static final Reg.Handle<LaunchPadRocket> LAUNCH_PAD_ROCKET =
            HbmSpace.REG.blockItem(
                    "launch_pad_rocket",
                    LaunchPadRocket::new,
                    () ->
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.METAL)
                                    .strength(5.0F, 6.0F)
                                    .requiresCorrectToolForDrops()
                                    .noOcclusion(),
                    (block, props) -> new DescBlockItem(block, props, 1));

    public static final Reg.Handle<MachineStardar> MACHINE_STARDAR =
            HbmSpace.REG.blockItem(
                    "machine_stardar",
                    MachineStardar::new,
                    () ->
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.METAL)
                                    .strength(5.0F, 6.0F)
                                    .requiresCorrectToolForDrops()
                                    .noOcclusion(),
                    (block, props) -> new DescBlockItem(block, props, 3));

    public static final Reg.Handle<MachineDriveProcessor> MACHINE_DRIVE_PROCESSOR =
            HbmSpace.REG.blockItem(
                    "machine_drive_processor",
                    MachineDriveProcessor::new,
                    () ->
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.METAL)
                                    .strength(5.0F, 6.0F)
                                    .requiresCorrectToolForDrops()
                                    .noOcclusion());

    public static final Reg.Handle<CombatDropPod> COMBAT_DROP =
            HbmSpace.REG.blockItem(
                    "combat_drop",
                    CombatDropPod::new,
                    () ->
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.METAL)
                                    .strength(5.0F, 6.0F)
                                    .requiresCorrectToolForDrops()
                                    .noOcclusion());

    public static final Reg.BeHandle<BlockEntityMachineRocketAssembly> ROCKET_ASSEMBLY =
            HbmSpace.REG
                    .be(
                            "machine_rocket_assembly",
                            () ->
                                    new BlockEntityType<>(
                                            BlockEntityMachineRocketAssembly::new,
                                            Set.of(MACHINE_ROCKET_ASSEMBLY.get())))
                    .ticksServer(BlockEntityMachineBase::tickServer);

    public static final Reg.BeHandle<BlockEntityLaunchPadRocket> LAUNCH_PAD_ROCKET_BE =
            HbmSpace.REG
                    .be(
                            "launch_pad_rocket",
                            () ->
                                    new BlockEntityType<>(
                                            BlockEntityLaunchPadRocket::new,
                                            Set.of(LAUNCH_PAD_ROCKET.get())))
                    .ticksServer(BlockEntityMachineBase::tickServer);

    public static final Reg.BeHandle<BlockEntityMachineStardar> STARDAR_BE =
            HbmSpace.REG
                    .be(
                            "machine_stardar",
                            () ->
                                    new BlockEntityType<>(
                                            BlockEntityMachineStardar::new,
                                            Set.of(MACHINE_STARDAR.get())))
                    .ticks(BlockEntityMachineBase::tickClient, BlockEntityMachineBase::tickServer);

    public static final Reg.BeHandle<BlockEntityCombatDropPod> COMBAT_DROP_BE =
            HbmSpace.REG
                    .be(
                            "combat_drop",
                            () ->
                                    new BlockEntityType<>(
                                            BlockEntityCombatDropPod::new,
                                            Set.of(COMBAT_DROP.get())))
                    .ticks(BlockEntityCombatDropPod::tick);

    public static final Reg.BeHandle<BlockEntityMachineDriveProcessor> DRIVE_PROCESSOR_BE =
            HbmSpace.REG
                    .be(
                            "machine_drive_processor",
                            () ->
                                    new BlockEntityType<>(
                                            BlockEntityMachineDriveProcessor::new,
                                            Set.of(MACHINE_DRIVE_PROCESSOR.get())))
                    .ticksServer(BlockEntityMachineBase::tickServer);

    static {
        CreativeContents.insertAfter(
                Tab.MISSILES,
                Library.id("machine_missile_assembly"),
                () -> new ItemStack(MACHINE_ROCKET_ASSEMBLY.get()));
        CreativeContents.insertAfter(
                Tab.MISSILES,
                Library.id("launch_pad"),
                () -> new ItemStack(LAUNCH_PAD_ROCKET.get()));
        CreativeContents.insertAfter(
                Tab.MISSILES, Library.id("soyuz_capsule"), () -> new ItemStack(COMBAT_DROP.get()));
        CreativeContents.insertAfter(
                Tab.MACHINES,
                Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, "machine_solar"),
                () -> new ItemStack(MACHINE_STARDAR.get()));
        CreativeContents.insertAfter(
                Tab.MACHINES,
                Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, "machine_stardar"),
                () -> new ItemStack(MACHINE_DRIVE_PROCESSOR.get()));
    }

    private RocketryBlocksSpace() {}
}
