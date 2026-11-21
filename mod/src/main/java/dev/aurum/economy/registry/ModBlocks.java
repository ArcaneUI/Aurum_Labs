package dev.aurum.economy.registry;

import dev.aurum.economy.AurumMod;
import dev.aurum.economy.block.CoinPedestalBlock;
import dev.aurum.economy.block.MintBlock;
import dev.aurum.economy.block.TellerDeskBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;

public final class ModBlocks {
	public static final Block MINT = registerBlock("mint", new MintBlock(
		AbstractBlock.Settings.create()
			.mapColor(MapColor.IRON_GRAY)
			.strength(3.5f, 6.0f)
			.requiresTool()
			.sounds(BlockSoundGroup.METAL)));

	public static final Block TELLER_DESK = registerBlock("teller_desk", new TellerDeskBlock(
		AbstractBlock.Settings.create()
			.mapColor(MapColor.OAK_TAN)
			.instrument(NoteBlockInstrument.BASS)
			.strength(2.5f)
			.sounds(BlockSoundGroup.WOOD)
			.burnable()));

	public static final Block COIN_PEDESTAL = registerBlock("coin_pedestal", new CoinPedestalBlock(
		AbstractBlock.Settings.create()
			.mapColor(MapColor.STONE_GRAY)
			.strength(2.0f, 6.0f)
			.requiresTool()
			.nonOpaque()
			.sounds(BlockSoundGroup.STONE)));

	private ModBlocks() {
	}

	private static Block registerBlock(String name, Block block) {
		Registry.register(Registries.BLOCK, AurumMod.id(name), block);
		Registry.register(Registries.ITEM, AurumMod.id(name), new BlockItem(block, new Item.Settings()));
		return block;
	}

	public static void init() {
	}
}
