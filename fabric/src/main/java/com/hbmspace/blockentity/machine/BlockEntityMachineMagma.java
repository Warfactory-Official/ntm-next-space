// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blockentity.machine;

import com.hbm.api.energymk2.IEnergyHandlerMK2;
import com.hbm.blockentity.BlockEntityMachineBase;
import com.hbm.blocks.multiblock.BlockMultiblockCore;
import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.NTMMaterial;
import com.hbm.packet.SyncField;
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
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class BlockEntityMachineMagma extends BlockEntityMachineBase
        implements IEnergyHandlerMK2, SyncUnitSchema {

    public static final long MAX_POWER = 1_000_000L;
    public static final long CONSUMPTION = 10_000L;
    public static final int MAX_LIQUID = MaterialShapes.BLOCK.q(16);
    public static final int OUTPUT_INTERVAL = 10;
    public static final double SPOUT = 3.875D;
    public static final double SPOUT_Y = 1.25D;
    public static final int POUR_REACH = 6;

    @SyncField(units = 1L << 0)
    public long power;

    @SyncField(units = 1L << 1)
    public boolean operating;

    @SyncField(units = 1L << 1)
    public boolean validPosition = true;

    @SyncField(units = 1L << 2)
    public final List<MaterialStack> liquids = new SyncList<>();

    @SyncField(units = 1L << 3)
    private int pourColor = -1;

    @SyncField(units = 1L << 3)
    private float pourLen;

    public final List<PourStream> streams = new ArrayList<>();
    public float drillSpeed;
    public float drillRotation;
    public float prevDrillRotation;
    public float lavaHeight;
    public float prevLavaHeight;

    public BlockEntityMachineMagma(BlockPos pos, BlockState state) {
        super(IndustryBlockEntitiesSpace.MAGMA.get(), pos, state, 0);
    }

    @Override
    public long getPower() {
        return power;
    }

    @Override
    public void setPower(long p) {
        power = p;
    }

    @Override
    public long getMaxPower() {
        return MAX_POWER;
    }

    @Override
    public void tickServer() {
        pourColor = -1;

        operating = canOperate();
        if (operating) {
            power -= CONSUMPTION;
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
            Direction dir = BlockMultiblockCore.coreFacing(getBlockState());
            CrucibleUtil.ImpactPos impact = new CrucibleUtil.ImpactPos();
            MaterialStack poured =
                    CrucibleUtil.pourFullStack(
                            level,
                            worldPosition.getX() + 0.5D + dir.getStepX() * SPOUT,
                            worldPosition.getY() + SPOUT_Y,
                            worldPosition.getZ() + 0.5D + dir.getStepZ() * SPOUT,
                            POUR_REACH,
                            true,
                            liquids,
                            MaterialShapes.INGOT.q(1),
                            impact);
            if (poured != null) {
                pourColor = poured.material.moltenColor;
                pourLen =
                        Math.max(
                                1F,
                                worldPosition.getY() + 1 - (float) (Math.ceil(impact.y) - 0.875));
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
        if (!"moho".equals(CelestialBody.getBody(level).name)) return false;
        validPosition = lavaBelow();
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
            new MaterialStack(Mats.MAT_SLAG, MaterialShapes.INGOT.q(1)),
            new MaterialStack(MatsSpace.MAT_RICH_MAGMA, MaterialShapes.QUANTUM.q(4))
        };
    }

    private void add(MaterialStack output) {
        for (MaterialStack held : liquids) {
            if (held.material == output.material) {
                held.amount += output.amount;
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
        power = input.getLongOr("power", 0L);
        liquids.clear();
        int[] data = input.getIntArray("liquids").orElse(new int[0]);
        for (int i = 0; i < data.length / 2; i++) {
            NTMMaterial material = Mats.matById.get(data[i * 2]);
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
            data[i * 2] = liquids.get(i).material.id;
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
                    output.writeInt(stack.material.id);
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
            case 0 -> power = input.readLong();
            case 1 -> {
                operating = input.readBoolean();
                validPosition = input.readBoolean();
            }
            case 2 -> {
                int count = input.readShort();
                if (count < 0 || count > input.readableBytes() / 8)
                    throw new DecoderException("Invalid melt count");
                liquids.clear();
                for (int i = 0; i < count; i++) {
                    liquids.add(
                            new MaterialStack(Mats.matById.get(input.readInt()), input.readInt()));
                }
            }
            case 3 -> {
                pourColor = input.readInt();
                pourLen = input.readFloat();
            }
            default -> throw new IllegalArgumentException();
        }
    }

    public record PourStream(int color, float len, long birth) {}
}
