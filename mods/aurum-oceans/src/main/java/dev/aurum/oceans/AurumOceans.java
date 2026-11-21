package dev.aurum.oceans;

import net.fabricmc.api.ModInitializer;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AurumOceans implements ModInitializer {
    public static final String MOD_ID = "aurum_oceans";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Identifier id(String path) { return Identifier.of(MOD_ID, path); }

    // Предметы
    public static final Item PEARL = reg("pearl", new Item(new Item.Settings().rarity(Rarity.UNCOMMON)));
    public static final Item BLACK_PEARL = reg("black_pearl", new Item(new Item.Settings().rarity(Rarity.RARE)));
    public static final Item SHELL = reg("shell", new Item(new Item.Settings()));
    public static final Item STARFISH = reg("starfish", new Item(new Item.Settings()));
    public static final Item FISHING_NET = reg("fishing_net", new dev.aurum.oceans.item.FishingNetItem(new Item.Settings().maxDamage(32)));
    public static final Item PEARL_AMULET = reg("pearl_amulet", new dev.aurum.oceans.item.PearlAmuletItem(new Item.Settings().maxCount(1)));
    public static final Block SUNKEN_CHEST = regBlock("sunken_chest", new dev.aurum.oceans.block.SunkenChestBlock(AbstractBlock.Settings.create().mapColor(MapColor.OAK_TAN).strength(2.5f)));
    public static final Item CRAB_MEAT = reg("crab_meat", new Item(new Item.Settings().food(
        new net.minecraft.component.type.FoodComponent.Builder().nutrition(4).saturationModifier(0.5f).build())));
    public static final Item COOKED_CRAB = reg("cooked_crab", new Item(new Item.Settings().food(
        new net.minecraft.component.type.FoodComponent.Builder().nutrition(8).saturationModifier(0.8f).build())));

    // Блоки
    public static final Block CORAL_LAMP = regBlock("coral_lamp",
        new Block(AbstractBlock.Settings.create().mapColor(MapColor.CYAN).luminance(s -> 12).strength(1.0f).sounds(BlockSoundGroup.GLASS)));
    public static final Block DOCK_PLANKS = regBlock("dock_planks",
        new Block(AbstractBlock.Settings.create().mapColor(MapColor.SPRUCE_BROWN).strength(2.0f, 3.0f).sounds(BlockSoundGroup.WOOD)));
    public static final Block BARNACLE_BLOCK = regBlock("barnacle_block",
        new Block(AbstractBlock.Settings.create().mapColor(MapColor.STONE_GRAY).strength(1.5f).sounds(BlockSoundGroup.CALCITE)));

    private static Item reg(String name, Item item) {
        return Registry.register(Registries.ITEM, id(name), item);
    }

    private static Block regBlock(String name, Block block) {
        Registry.register(Registries.BLOCK, id(name), block);
        Registry.register(Registries.ITEM, id(name), new BlockItem(block, new Item.Settings()));
        return block;
    }

    @Override
    public void onInitialize() {
        ModItemGroups.init();
        net.fabricmc.fabric.api.loot.v3.LootTableEvents.MODIFY.register((key, tableBuilder, source, provider) -> {
            if (key.getValue().toString().equals("minecraft:chests/shipwreck_treasure") || key.getValue().toString().equals("minecraft:chests/underwater_ruin_big")) {
                tableBuilder.pool(net.minecraft.loot.LootPool.builder().rolls(net.minecraft.loot.provider.number.ConstantLootNumberProvider.create(1))
                    .with(net.minecraft.loot.entry.ItemEntry.builder(PEARL).weight(10))
                    .with(net.minecraft.loot.entry.ItemEntry.builder(BLACK_PEARL).weight(2))
                    .with(net.minecraft.loot.entry.ItemEntry.builder(SHELL).weight(15))
                    .with(net.minecraft.loot.entry.ItemEntry.builder(STARFISH).weight(5)));
            }
        });
        
        net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents.modifyEntriesEvent(net.minecraft.item.ItemGroups.INGREDIENTS).register(entries -> {
            entries.add(PEARL); entries.add(BLACK_PEARL); entries.add(SHELL); entries.add(STARFISH); entries.add(FISHING_NET); entries.add(PEARL_AMULET); entries.add(SUNKEN_CHEST);
        });
        net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents.modifyEntriesEvent(net.minecraft.item.ItemGroups.FOOD_AND_DRINK).register(entries -> {
            entries.add(CRAB_MEAT); entries.add(COOKED_CRAB);
        });
        net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents.modifyEntriesEvent(net.minecraft.item.ItemGroups.BUILDING_BLOCKS).register(entries -> {
            entries.add(DOCK_PLANKS); entries.add(BARNACLE_BLOCK);
        });
        net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents.modifyEntriesEvent(net.minecraft.item.ItemGroups.FUNCTIONAL).register(entries -> {
            entries.add(CORAL_LAMP);
        });
        LOGGER.info("Aurum: Oceans загружен — жемчуг, ракушки, рыбацкие сети и морские блоки!");
    }
}
// append
