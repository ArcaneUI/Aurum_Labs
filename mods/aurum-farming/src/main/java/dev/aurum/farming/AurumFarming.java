package dev.aurum.farming;

import net.fabricmc.api.ModInitializer;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.CropBlock;
import net.minecraft.block.MapColor;
import net.minecraft.item.AliasedBlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AurumFarming implements ModInitializer {
    public static final String MOD_ID = "aurum_farming";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Identifier id(String path) { return Identifier.of(MOD_ID, path); }

    // Новые культуры
    public static final Block TOMATO_CROP = registerCrop("tomato_crop");
    public static final Block CORN_CROP = registerCrop("corn_crop");
    public static final Block STRAWBERRY_CROP = registerCrop("strawberry_crop");
    public static final Block ONION_CROP = registerCrop("onion_crop");
    public static final Block RICE_CROP = registerCrop("rice_crop");

    // Семена
    public static final Item TOMATO_SEEDS = registerSeeds("tomato_seeds", TOMATO_CROP);
    public static final Item CORN_SEEDS = registerSeeds("corn_seeds", CORN_CROP);
    public static final Item STRAWBERRY_SEEDS = registerSeeds("strawberry_seeds", STRAWBERRY_CROP);
    public static final Item ONION_SEEDS = registerSeeds("onion_seeds", ONION_CROP);
    public static final Item RICE_SEEDS = registerSeeds("rice_seeds", RICE_CROP);

    // Плоды
    public static final Item TOMATO = registerItem("tomato", new Item(new Item.Settings().food(new net.minecraft.component.type.FoodComponent.Builder().nutrition(3).saturationModifier(0.4f).build())));
    public static final Item CORN = registerItem("corn", new Item(new Item.Settings().food(new net.minecraft.component.type.FoodComponent.Builder().nutrition(4).saturationModifier(0.5f).build())));
    public static final Item STRAWBERRY = registerItem("strawberry", new Item(new Item.Settings().food(new net.minecraft.component.type.FoodComponent.Builder().nutrition(2).saturationModifier(0.3f).build())));
    public static final Item ONION = registerItem("onion", new Item(new Item.Settings().food(new net.minecraft.component.type.FoodComponent.Builder().nutrition(2).saturationModifier(0.2f).build())));
    public static final Item RICE = registerItem("rice", new Item(new Item.Settings()));

    // Удобрение
    public static final Item FERTILIZER = registerItem("fertilizer", new FertilizerItem(new Item.Settings()));

    // Кухонный стол
    public static final Block KITCHEN_TABLE = Registry.register(Registries.BLOCK, id("kitchen_table"),
        new dev.aurum.farming.block.KitchenTableBlock(AbstractBlock.Settings.create().mapColor(MapColor.OAK_TAN).strength(2.5f).sounds(BlockSoundGroup.WOOD)));
    public static final Item TOMATO_SOUP = registerItem("tomato_soup", new Item(new Item.Settings().food(new net.minecraft.component.type.FoodComponent.Builder().nutrition(6).saturationModifier(0.6f).build())));
    public static final Item ROASTED_CORN = registerItem("roasted_corn", new Item(new Item.Settings().food(new net.minecraft.component.type.FoodComponent.Builder().nutrition(7).saturationModifier(0.7f).build())));
    public static final Item STRAWBERRY_TART = registerItem("strawberry_tart", new Item(new Item.Settings().food(new net.minecraft.component.type.FoodComponent.Builder().nutrition(5).saturationModifier(0.5f).build())));
    public static final Item ONION_RINGS = registerItem("onion_rings", new Item(new Item.Settings().food(new net.minecraft.component.type.FoodComponent.Builder().nutrition(5).saturationModifier(0.5f).build())));
    public static final Item FARMERS_PAELLA = registerItem("farmers_paella", new Item(new Item.Settings().food(new net.minecraft.component.type.FoodComponent.Builder().nutrition(10).saturationModifier(0.9f).build())));
    public static final Item DELIVERY_CONTRACT = registerItem("delivery_contract", new dev.aurum.farming.item.DeliveryContractItem(new Item.Settings()));
    public static final Item KITCHEN_TABLE_ITEM = registerItem("kitchen_table",
        new net.minecraft.item.BlockItem(KITCHEN_TABLE, new Item.Settings()));

    private static Block registerCrop(String name) {
        return Registry.register(Registries.BLOCK, id(name),
            new CropBlock(AbstractBlock.Settings.create().noCollision().ticksRandomly().breakInstantly().sounds(BlockSoundGroup.CROP)));
    }

    private static Item registerSeeds(String name, Block crop) {
        return Registry.register(Registries.ITEM, id(name),
            new AliasedBlockItem(crop, new Item.Settings()));
    }

    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, id(name), item);
    }

    @Override
    public void onInitialize() {
        ModItemGroups.init();
        
        net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents.modifyEntriesEvent(net.minecraft.item.ItemGroups.FOOD_AND_DRINK).register(entries -> {
            entries.add(TOMATO); entries.add(CORN); entries.add(STRAWBERRY); entries.add(ONION); entries.add(RICE);
        });
        net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents.modifyEntriesEvent(net.minecraft.item.ItemGroups.NATURAL).register(entries -> {
            entries.add(TOMATO_SEEDS); entries.add(CORN_SEEDS); entries.add(STRAWBERRY_SEEDS); entries.add(ONION_SEEDS); entries.add(RICE_SEEDS); entries.add(FERTILIZER);
        });
        net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents.modifyEntriesEvent(net.minecraft.item.ItemGroups.FUNCTIONAL).register(entries -> {
            entries.add(KITCHEN_TABLE_ITEM); entries.add(DELIVERY_CONTRACT); entries.add(TOMATO_SOUP); entries.add(ROASTED_CORN); entries.add(STRAWBERRY_TART); entries.add(ONION_RINGS); entries.add(FARMERS_PAELLA);
        });
        LOGGER.info("Aurum: Farming загружен — 5 культур, удобрение и кухонный стол!");
    }
}
// append
