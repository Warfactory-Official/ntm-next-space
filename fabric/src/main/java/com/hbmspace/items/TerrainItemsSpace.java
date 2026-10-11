// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.items;

import com.hbm.blocks.fluid.ClassicFluid;
import com.hbm.items.CreativeContents.Tab;
import com.hbm.items.CreativeContents;
import com.hbm.lib.Library;
import com.hbm.registration.Reg;
import com.hbmspace.HbmSpace;
import com.hbmspace.blocks.BlockCrop;
import com.hbmspace.blocks.FloraBlocksSpace;
import com.hbmspace.blocks.TerrainBlocksSpace;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

public final class TerrainItemsSpace {
    public static final Reg.Handle<Item> SCUTTERTAIL =
            HbmSpace.REG.item("scuttertail", Item::new, Item.Properties::new);
    public static final Reg.Handle<Item> SALTLEAF =
            HbmSpace.REG.item("saltleaf", Item::new, () -> food(2, 0.5F));
    public static final Reg.Handle<Item> LEAF_RUBBER =
            HbmSpace.REG.item("leaf_rubber", Item::new, Item.Properties::new);
    public static final Reg.Handle<Item> LEAF_PET =
            HbmSpace.REG.item("leaf_pet", Item::new, Item.Properties::new);

    public static final Reg.Handle<BlockItem> STRAWBERRY =
            seedFood(
                    "strawberry",
                    () -> FloraBlocksSpace.CROP_STRAWBERRY.get(),
                    3,
                    0.4F,
                    "item/strawberry");
    public static final Reg.Handle<BlockItem> MINT_LEAVES =
            seedFood(
                    "mint_leaves",
                    () -> FloraBlocksSpace.CROP_MINT.get(),
                    3,
                    0.4F,
                    "item/mint_leaves");
    public static final Reg.Handle<BlockItem> TEASEEDS =
            seedFood("teaseeds", () -> FloraBlocksSpace.CROP_TEA.get(), 0, 0.0F, "item/teaseeds");
    public static final Reg.Handle<Item> TEA_LEAF =
            HbmSpace.REG.item("tea_leaf", Item::new, Item.Properties::new);
    public static final Reg.Handle<BlockItem> BEAN_RAW =
            seedFood(
                    "bean_raw",
                    () -> FloraBlocksSpace.CROP_COFFEE.get(),
                    1,
                    0.2F,
                    "item/coffeebeanraw");
    public static final Reg.Handle<BlockItem> PARAFFIN_SEEDS =
            seedFood(
                    "paraffin_seeds",
                    () -> FloraBlocksSpace.CROP_PARAFFIN.get(),
                    1,
                    0.2F,
                    "item/paraffin_seeds");

    public static final Reg.Handle<BucketItem> BUCKET_MERCURY =
            bucket("bucket_mercury", () -> TerrainBlocksSpace.MERCURY_FLUID);
    public static final Reg.Handle<BucketItem> BUCKET_BROMINE =
            bucket("bucket_bromine", () -> TerrainBlocksSpace.BROMINE_FLUID);
    public static final Reg.Handle<BucketItem> BUCKET_CCL =
            bucket("bucket_ccl", () -> TerrainBlocksSpace.CCL_FLUID);

    static {
        for (Reg.Handle<? extends Item> item :
                List.of(SCUTTERTAIL, SALTLEAF, LEAF_RUBBER, LEAF_PET)) {
            CreativeContents.insertAfter(
                    Tab.PARTS,
                    Library.id("plant_item_mustardwillow"),
                    () -> new ItemStack(item.get()));
        }
        for (Reg.Handle<? extends Item> item :
                List.of(STRAWBERRY, MINT_LEAVES, TEASEEDS, TEA_LEAF, BEAN_RAW, PARAFFIN_SEEDS)) {
            CreativeContents.insertAfter(
                    Tab.CONSUMABLES, Library.id("bdcl"), () -> new ItemStack(item.get()));
        }
        for (Reg.Handle<? extends Item> item :
                List.of(BUCKET_MERCURY, BUCKET_BROMINE, BUCKET_CCL)) {
            CreativeContents.insertBefore(
                    Tab.BLOCKS, Library.id("door_metal"), () -> new ItemStack(item.get()));
        }
    }

    private TerrainItemsSpace() {}

    private static Item.Properties food(int nutrition, float saturation) {
        return new Item.Properties()
                .food(
                        new FoodProperties.Builder()
                                .nutrition(nutrition)
                                .saturationModifier(saturation)
                                .build());
    }

    private static Reg.Handle<BlockItem> seedFood(
            String name, Supplier<BlockCrop> crop, int nutrition, float saturation, String icon) {
        return HbmSpace.REG.<BlockItem>item(
                name,
                props -> new SeedFoodBlockItem(crop.get(), props),
                () -> food(nutrition, saturation).useItemDescriptionPrefix());
    }

    private static final class SeedFoodBlockItem extends BlockItem {
        private SeedFoodBlockItem(Block crop, Item.Properties props) {
            super(crop, props);
        }

        @Override
        public void registerBlocks(Map<Block, Item> map, Item item) {}
    }

    private static Reg.Handle<BucketItem> bucket(String name, Supplier<ClassicFluid.Pair> fluid) {
        return HbmSpace.REG.item(
                name,
                props -> new BucketItem(fluid.get().source().get(), props),
                () -> new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1));
    }
}
