// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client.model;

import com.hbm.client.model.BlockModel;
import com.hbm.client.model.ObjUnbakedGeometry;
import com.hbm.client.model.QuadLighting;
import com.hbm.client.model.SimpleBlockModel;
import com.hbmspace.blocks.BlockCoral;
import com.mojang.blaze3d.platform.Transparency;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;

public class CoralModel implements BlockStateModel {

    public static Factory FACTORY = CoralModel::new;

    private final BlockStateModelPart[] parts = new BlockStateModelPart[Direction.values().length];
    private final Material.Baked particle;

    public CoralModel(ModelBaker baker, Identifier carrier) {
        ResolvedModel resolved = baker.getModel(carrier);
        TextureSlots slots = resolved.getTopTextureSlots();
        particle = resolved.resolveParticleMaterial(slots, baker);
        Material.Baked coral = BlockModel.slot(baker, resolved, slots, "coral");
        var info = BakedQuad.MaterialInfo.of(coral, Transparency.TRANSPARENT, -1, false, 0);
        for (Direction attachment : Direction.values()) {
            parts[attachment.ordinal()] =
                    new SimpleModelWrapper(
                            ObjUnbakedGeometry.bakeGroups(
                                    List.of(), quads(info, attachment), 0F, 0F, 0F),
                            false,
                            particle);
        }
    }

    private static CoralModel create(ModelBaker baker, Identifier carrier) {
        return FACTORY.create(baker, carrier);
    }

    @Override
    public void collectParts(RandomSource random, List<BlockStateModelPart> output) {
        output.add(parts[Direction.DOWN.ordinal()]);
    }

    public void collectParts(
            BlockAndTintGetter level,
            BlockPos pos,
            BlockState state,
            RandomSource random,
            List<BlockStateModelPart> output) {
        output.add(parts[geometryKey(level, pos, state)]);
    }

    public int geometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        return BlockCoral.attachment(level, pos).ordinal();
    }

    @Override
    public Material.Baked particleMaterial() {
        return particle;
    }

    @Override
    public int materialFlags() {
        return parts[0].materialFlags();
    }

    private static List<ObjUnbakedGeometry.BoxQuad> quads(
            BakedQuad.MaterialInfo info, Direction dir) {
        double factor = 0.45D;
        double minY = 0;
        double maxY = 1;
        double minX = 0.5D - factor;
        double maxX = 0.5D + factor;
        double minZ = 0.5D - factor;
        double maxZ = 0.5D + factor;

        double[] x = {minX, minX, maxX, maxX, maxX, maxX, minX, minX};
        double[] y = {minY, maxY, maxY, minY, minY, maxY, maxY, minY};
        double[] z = {minZ, minZ, maxZ, maxZ, minZ, minZ, maxZ, maxZ};

        switch (dir) {
            case UP -> {
                for (int i = 0; i < 8; i++) y[i] = 1 - y[i];
            }
            case NORTH -> {
                y = new double[] {minZ, minZ, maxZ, maxZ, minZ, minZ, maxZ, maxZ};
                z = new double[] {minY, maxY, maxY, minY, minY, maxY, maxY, minY};
            }
            case SOUTH -> {
                y = new double[] {minZ, minZ, maxZ, maxZ, minZ, minZ, maxZ, maxZ};
                z =
                        new double[] {
                            1 - minY, 1 - maxY, 1 - maxY, 1 - minY, 1 - minY, 1 - maxY, 1 - maxY,
                            1 - minY
                        };
            }
            case WEST -> {
                y = new double[] {minX, minX, maxX, maxX, maxX, maxX, minX, minX};
                x = new double[] {minY, maxY, maxY, minY, minY, maxY, maxY, minY};
            }
            case EAST -> {
                y = new double[] {minX, minX, maxX, maxX, maxX, maxX, minX, minX};
                x =
                        new double[] {
                            1 - minY, 1 - maxY, 1 - maxY, 1 - minY, 1 - minY, 1 - maxY, 1 - maxY,
                            1 - minY
                        };
            }
            default -> {}
        }

        Vector3f[] v = new Vector3f[8];
        for (int i = 0; i < 8; i++) v[i] = new Vector3f((float) x[i], (float) y[i], (float) z[i]);

        var sprite = info.sprite();
        long maxUmaxV = UVPair.pack(sprite.getU(1), sprite.getV(1));
        long maxUminV = UVPair.pack(sprite.getU(1), sprite.getV(0));
        long minUminV = UVPair.pack(sprite.getU(0), sprite.getV(0));
        long minUmaxV = UVPair.pack(sprite.getU(0), sprite.getV(1));

        List<ObjUnbakedGeometry.BoxQuad> out = new ArrayList<>(4);
        quad(out, info, v[0], v[1], v[2], v[3], maxUmaxV, maxUminV, minUminV, minUmaxV);
        quad(out, info, v[3], v[2], v[1], v[0], maxUmaxV, maxUminV, minUminV, minUmaxV);
        quad(out, info, v[4], v[5], v[6], v[7], maxUmaxV, maxUminV, minUminV, minUmaxV);
        quad(out, info, v[7], v[6], v[5], v[4], maxUmaxV, maxUminV, minUminV, minUmaxV);
        return out;
    }

    private static void quad(
            List<ObjUnbakedGeometry.BoxQuad> out,
            BakedQuad.MaterialInfo info,
            Vector3f a,
            Vector3f b,
            Vector3f c,
            Vector3f d,
            long uvA,
            long uvB,
            long uvC,
            long uvD) {
        Vector3f normal = new Vector3f(c).sub(a).cross(new Vector3f(d).sub(b));
        Direction direction = Direction.getApproximateNearest(normal.x, normal.y, normal.z);
        out.add(
                new ObjUnbakedGeometry.BoxQuad(
                        new BakedQuad(a, b, c, d, uvA, uvB, uvC, uvD, direction, info),
                        ObjUnbakedGeometry.WHITE,
                        null,
                        QuadLighting.OWN_BLOCK));
    }

    public interface Factory {
        CoralModel create(ModelBaker baker, Identifier carrier);
    }

    public record Family() implements SimpleBlockModel {
        @Override
        public BlockStateModel.UnbakedRoot root(Block block, BlockState state) {
            return new Root(BlockModel.base(block));
        }
    }

    public record Root(Identifier carrier) implements BlockStateModel.UnbakedRoot {
        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {
            resolver.markDependency(carrier);
        }

        @Override
        public BlockStateModel bake(BlockState state, ModelBaker baker) {
            return baker.compute(new SharedKey(this));
        }

        @Override
        public Object visualEqualityGroup(BlockState state) {
            return this;
        }
    }

    private record SharedKey(Root root) implements ModelBaker.SharedOperationKey<BlockStateModel> {
        @Override
        public BlockStateModel compute(ModelBaker baker) {
            return create(baker, root.carrier());
        }
    }
}
