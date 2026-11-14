/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2024 George Paton
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2024 George Paton. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License version 3 only,
 * as published by the Free Software Foundation.
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this code. If not, see <https://www.gnu.org/licenses/>.
 *
 * Under GPLv3 §7(c) and §7(e), these additional terms apply to LGPL-covered
 * material authored by movblock:
 *
 * - the license grants no rights to the name "NTM NEXT: Space";
 * - a modified version must not be named NTM NEXT: Space or presented as
 *   the official release, and must be marked as different from it.
 *
 * Accurately identifying a work as derived from NTM NEXT: Space does not
 * violate these additional terms, provided it is clearly distinguished from
 * the official release and does not imply endorsement. Ports are encouraged
 * to acknowledge NTM NEXT: Space as their upstream project and provide a
 * link to it.
 */

package com.hbmspace.blockentity.machine;

import com.hbm.api.energymk2.EnergyHandler;
import com.hbm.blockentity.MachineBlockEntity;
import com.hbm.blocks.multiblock.MultiblockCoreBlock;
import com.hbm.inventory.material.Material;
import com.hbm.inventory.material.MaterialRegistry;
import com.hbm.inventory.material.MaterialShape;
import com.hbm.inventory.material.MaterialStack;
import com.hbm.inventory.material.Materials;
import com.hbm.packet.SyncList;
import com.hbm.packet.SyncUnitSchema;
import com.hbm.util.CrucibleUtil;
import com.hbm.util.TickPhase;
import com.hbmspace.blocks.IndustryBlockEntitiesSpace;
import com.hbmspace.dim.CelestialBody;
import com.hbmspace.inventory.material.MatsSpace;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class MagmaBlockEntity extends MachineBlockEntity implements EnergyHandler, SyncUnitSchema {

    public static final long MAX_POWER = 1_000_000L;
    public static final long CONSUMPTION = 10_000L;
    public static final int MAX_LIQUID = MaterialShape.BLOCK.amount(16);
    public static final int OUTPUT_INTERVAL = 10;
    public static final double SPOUT = 3.875D;
    public static final double SPOUT_Y = 1.25D;
    public static final int POUR_REACH = 6;

    public long power;
    public boolean operating;
    public boolean validPosition;

    {
        hbm$syncSet$MagmaBlockEntity$validPosition(true);
    }

    public final List<MaterialStack> liquids = new SyncList<>();
    private int pourColor;

    {
        hbm$syncSet$MagmaBlockEntity$pourColor(-1);
    }

    private float pourLen;

    public final List<PourStream> streams = new ArrayList<>();
    public float drillSpeed;
    public float drillRotation;
    public float prevDrillRotation;
    public float lavaHeight;
    public float prevLavaHeight;

    private boolean onMoho;
    private boolean lavaKnown;

    public MagmaBlockEntity(BlockPos pos, BlockState state) {
        super(IndustryBlockEntitiesSpace.MAGMA.get(), pos, state, 0);
    }

    @Override
    public long getPower() {
        return power;
    }

    @Override
    public void setPower(long p) {
        hbm$syncSet$MagmaBlockEntity$power(p);
    }

    @Override
    public long getMaxPower() {
        return MAX_POWER;
    }

    @Override
    public void onGraphLoad(ServerLevel server) {
        onMoho = "moho".equals(CelestialBody.getBody(server).name);
    }

    public void refreshLava() {
        lavaKnown = false;
    }

    @Override
    public void tickServer() {
        hbm$syncSet$MagmaBlockEntity$pourColor(-1);

        hbm$syncSet$MagmaBlockEntity$operating(canOperate());
        if (operating) {
            hbm$syncSet$MagmaBlockEntity$power((long) (power - (CONSUMPTION)));
            if (TickPhase.every(this, OUTPUT_INTERVAL)) {
                for (MaterialStack output : outputs()) {
                    int total = 0;
                    for (MaterialStack held : liquids) total += held.amount;
                    if (total + output.amount > MAX_LIQUID) break;
                    add(output);
                }
            }
        }

        if (!liquids.isEmpty()) {
            Direction dir = MultiblockCoreBlock.coreFacing(getBlockState());
            CrucibleUtil.Pour poured =
                    CrucibleUtil.pourFirst(
                            level,
                            worldPosition.getX() + 0.5D + dir.getStepX() * SPOUT,
                            worldPosition.getY() + SPOUT_Y,
                            worldPosition.getZ() + 0.5D + dir.getStepZ() * SPOUT,
                            POUR_REACH,
                            liquids,
                            MaterialShape.INGOT.amount(1));
            if (poured != null) {
                hbm$syncSet$MagmaBlockEntity$pourColor(poured.material().moltenColor());
                hbm$syncSet$MagmaBlockEntity$pourLen(
                        Math.max(
                                1F,
                                worldPosition.getY()
                                        + 1
                                        - (float) (Math.ceil(poured.impact().y) - 0.875)));
                markSyncEvent();
            }
        }

        liquids.removeIf(stack -> stack.amount <= 0);
        networkPackNT(250);
    }

    @Override
    public void tickClient() {
        prevLavaHeight = lavaHeight;
        prevDrillRotation = drillRotation;

        if (operating) {
            drillSpeed = Math.min(drillSpeed + 0.15F, 15F);
            lavaHeight += (float) ((level.getRandom().nextFloat() - 0.5) * 0.01);
            lavaHeight = (float) Mth.lerp(0.02D, lavaHeight, 0.9D);
        } else {
            drillSpeed = Math.max(drillSpeed - 0.3F, 0F);
            lavaHeight = (float) Mth.lerp(0.02D, lavaHeight, 0D);
        }

        drillRotation += drillSpeed;
        if (drillRotation > 360F) {
            drillRotation -= 360F;
            prevDrillRotation -= 360F;
        }

        long now = level.getGameTime();
        streams.removeIf(stream -> now - stream.birth() >= 20);
    }

    private boolean canOperate() {
        if (!onMoho) return false;
        if (!lavaKnown) {
            hbm$syncSet$MagmaBlockEntity$validPosition(lavaBelow());
            lavaKnown = true;
        }
        if (!validPosition) return false;
        return power >= CONSUMPTION;
    }

    private boolean lavaBelow() {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (!level.getBlockState(pos.setWithOffset(worldPosition, x, -4, z))
                        .is(Blocks.LAVA)) return false;
            }
        }
        return true;
    }

    private static MaterialStack[] outputs() {
        return new MaterialStack[] {
            new MaterialStack(Materials.SLAG, MaterialShape.INGOT.amount(1)),
            new MaterialStack(MatsSpace.MAT_RICH_MAGMA, MaterialShape.QUANTUM.amount(4))
        };
    }

    private void add(MaterialStack output) {
        for (MaterialStack held : liquids) {
            if (held.material == output.material) {
                held.hbm$syncSet$MaterialStack$amount((int) (held.amount + (output.amount)));
                return;
            }
        }
        liquids.add(output.copy());
    }

    @Override
    public void afterSyncUnits(long units) {
        if (level == null || !level.isClientSide()) return;
        if ((units & 1L << 3) != 0 && pourColor != -1) {
            streams.add(new PourStream(pourColor, pourLen, level.getGameTime()));
        }
    }

    @Override
    public void afterInitialSyncUnits() {}

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        hbm$syncSet$MagmaBlockEntity$power(input.getLongOr("power", 0L));
        liquids.clear();
        int[] data = input.getIntArray("liquids").orElse(new int[0]);
        for (int i = 0; i < data.length / 2; i++) {
            Material material = MaterialRegistry.byId(data[i * 2]);
            if (material == null) continue;
            liquids.add(new MaterialStack(material, data[i * 2 + 1]));
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putLong("power", power);
        int[] data = new int[liquids.size() * 2];
        for (int i = 0; i < liquids.size(); i++) {
            data[i * 2] = liquids.get(i).material.id();
            data[i * 2 + 1] = liquids.get(i).amount;
        }
        output.putIntArray("liquids", data);
    }

    @Override
    public long syncUnitMask() {
        return 0xfL;
    }

    @Override
    public void writeSyncUnit(int unit, ByteBuf output) {
        switch (unit) {
            case 0 -> output.writeLong(power);
            case 1 -> {
                output.writeBoolean(operating);
                output.writeBoolean(validPosition);
            }
            case 2 -> {
                output.writeShort(liquids.size());
                for (MaterialStack stack : liquids) {
                    output.writeInt(stack.material.id());
                    output.writeInt(stack.amount);
                }
            }
            case 3 -> {
                output.writeInt(pourColor);
                output.writeFloat(pourLen);
            }
            default -> throw new IllegalArgumentException();
        }
    }

    @Override
    public void readSyncUnit(int unit, ByteBuf input) {
        switch (unit) {
            case 0 -> hbm$syncSet$MagmaBlockEntity$power(input.readLong());
            case 1 -> {
                hbm$syncSet$MagmaBlockEntity$operating(input.readBoolean());
                hbm$syncSet$MagmaBlockEntity$validPosition(input.readBoolean());
            }
            case 2 -> {
                int count = input.readShort();
                if (count < 0 || count > input.readableBytes() / 8)
                    throw new DecoderException("Invalid melt count");
                liquids.clear();
                for (int i = 0; i < count; i++) {
                    liquids.add(
                            new MaterialStack(
                                    MaterialRegistry.byId(input.readInt()), input.readInt()));
                }
            }
            case 3 -> {
                hbm$syncSet$MagmaBlockEntity$pourColor(input.readInt());
                hbm$syncSet$MagmaBlockEntity$pourLen(input.readFloat());
            }
            default -> throw new IllegalArgumentException();
        }
    }

    public record PourStream(int color, float len, long birth) {}

    public final void hbm$syncSet$MagmaBlockEntity$power(long next) {
        if (this.power != next) {
            this.power = next;
            if ((1L << 0) == 0L) syncChanged(3);
            else syncUnitsChanged(3, 1L << 0);
        }
    }

    public final void hbm$syncSet$MagmaBlockEntity$operating(boolean next) {
        if (this.operating != next) {
            this.operating = next;
            if ((1L << 1) == 0L) syncChanged(3);
            else syncUnitsChanged(3, 1L << 1);
        }
    }

    public final void hbm$syncSet$MagmaBlockEntity$validPosition(boolean next) {
        if (this.validPosition != next) {
            this.validPosition = next;
            if ((1L << 1) == 0L) syncChanged(3);
            else syncUnitsChanged(3, 1L << 1);
        }
    }

    public final void hbm$syncSet$MagmaBlockEntity$pourColor(int next) {
        if (this.pourColor != next) {
            this.pourColor = next;
            if ((1L << 3) == 0L) syncChanged(3);
            else syncUnitsChanged(3, 1L << 3);
        }
    }

    public final void hbm$syncSet$MagmaBlockEntity$pourLen(float next) {
        if (Float.floatToRawIntBits(this.pourLen) != Float.floatToRawIntBits(next)) {
            this.pourLen = next;
            if ((1L << 3) == 0L) syncChanged(3);
            else syncUnitsChanged(3, 1L << 3);
        }
    }

    static {
        com.hbm.packet.SyncBindings.field(MagmaBlockEntity.class, "liquids", 3, 1L << 2);
    }
}
