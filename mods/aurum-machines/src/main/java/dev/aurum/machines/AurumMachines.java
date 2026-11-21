package dev.aurum.machines;

import net.fabricmc.api.ModInitializer;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AurumMachines implements ModInitializer {
    public static final String MOD_ID = "aurum_machines";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Identifier id(String path) { return Identifier.of(MOD_ID, path); }

    // Машины — базовые блоки
    public static final Block CRUSHER = regBlock("crusher",
        new dev.aurum.machines.block.CrusherBlock(AbstractBlock.Settings.create().mapColor(MapColor.IRON_GRAY).instrument(NoteBlockInstrument.IRON_XYLOPHONE).requiresTool().strength(4.0f, 6.0f).sounds(BlockSoundGroup.METAL)));
    public static final Block PRESS = regBlock("press",
        new Block(AbstractBlock.Settings.create().mapColor(MapColor.IRON_GRAY).requiresTool().strength(4.0f, 6.0f).sounds(BlockSoundGroup.METAL)));
    public static final Block CONVEYOR = regBlock("conveyor",
        new Block(AbstractBlock.Settings.create().mapColor(MapColor.STONE_GRAY).strength(2.0f).sounds(BlockSoundGroup.METAL)));
    public static final Block SORTER = regBlock("sorter",
        new Block(AbstractBlock.Settings.create().mapColor(MapColor.IRON_GRAY).requiresTool().strength(3.5f).sounds(BlockSoundGroup.METAL)));
    public static final Block PIPE = regBlock("pipe",
        new Block(AbstractBlock.Settings.create().mapColor(MapColor.IRON_GRAY).strength(2.0f).sounds(BlockSoundGroup.COPPER)));
    public static final Block GENERATOR = regBlock("generator",
        new Block(AbstractBlock.Settings.create().mapColor(MapColor.DARK_RED).requiresTool().strength(4.0f, 6.0f).sounds(BlockSoundGroup.METAL).luminance(s -> 7)));

    // Компоненты (предметы)
    public static final Item IRON_GEAR = reg("iron_gear", new Item(new Item.Settings()));
    public static final Item GOLD_GEAR = reg("gold_gear", new Item(new Item.Settings()));
    public static final Item MOTOR = reg("motor", new Item(new Item.Settings()));
    public static final Item CIRCUIT = reg("circuit", new Item(new Item.Settings()));
    public static final Item COPPER_WIRE = reg("copper_wire", new Item(new Item.Settings()));
    public static final Item IRON_DUST = reg("iron_dust", new Item(new Item.Settings()));
    public static final Item GOLD_DUST = reg("gold_dust", new Item(new Item.Settings()));
    public static final Item COPPER_DUST = reg("copper_dust", new Item(new Item.Settings()));
    public static final Item IRON_PLATE = reg("iron_plate", new Item(new Item.Settings()));
    public static final Item GOLD_PLATE = reg("gold_plate", new Item(new Item.Settings()));
    public static final Item COPPER_PLATE = reg("copper_plate", new Item(new Item.Settings()));

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
        
        net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents.modifyEntriesEvent(net.minecraft.item.ItemGroups.FUNCTIONAL).register(entries -> {
            entries.add(CRUSHER); entries.add(PRESS); entries.add(CONVEYOR); entries.add(SORTER); entries.add(PIPE); entries.add(GENERATOR);
        });
        net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents.modifyEntriesEvent(net.minecraft.item.ItemGroups.INGREDIENTS).register(entries -> {
            entries.add(IRON_GEAR); entries.add(GOLD_GEAR); entries.add(MOTOR); entries.add(CIRCUIT); entries.add(COPPER_WIRE); entries.add(IRON_DUST); entries.add(GOLD_DUST); entries.add(COPPER_DUST); entries.add(IRON_PLATE); entries.add(GOLD_PLATE); entries.add(COPPER_PLATE);
        });
        LOGGER.info("Aurum: Machines загружен — дробилка, пресс, конвейер, сортировщик, трубы и генератор!");
    }
}
// append
