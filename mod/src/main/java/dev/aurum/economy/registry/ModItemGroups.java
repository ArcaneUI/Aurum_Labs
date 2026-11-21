package dev.aurum.economy.registry;

import dev.aurum.economy.AurumMod;
import dev.aurum.economy.item.WalletItem;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;

public final class ModItemGroups {
	public static final ItemGroup AURUM = Registry.register(
		Registries.ITEM_GROUP,
		AurumMod.id("main"),
		FabricItemGroup.builder()
			.icon(() -> new ItemStack(ModItems.GOLD_COIN))
			.displayName(Text.translatable("itemGroup.aurum.main"))
			.entries((context, entries) -> {
				entries.add(ModItems.COPPER_COIN);
				entries.add(ModItems.SILVER_COIN);
				entries.add(ModItems.GOLD_COIN);
				entries.add(ModItems.BANKNOTE);
				entries.add(ModItems.WALLET);

				ItemStack rich = new ItemStack(ModItems.WALLET);
				WalletItem.setBalance(rich, 10_000L);
				entries.add(rich);

				entries.add(ModBlocks.MINT);
				entries.add(ModBlocks.TELLER_DESK);
				entries.add(ModBlocks.COIN_PEDESTAL);
			})
			.build()
	);

	private ModItemGroups() {
	}

	public static void init() {
	}
}
