package dev.aurum.economy.registry;

import dev.aurum.economy.AurumMod;
import dev.aurum.economy.currency.Currency;
import dev.aurum.economy.item.CoinItem;
import dev.aurum.economy.item.WalletItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Rarity;

public final class ModItems {
	public static final Item COPPER_COIN = register("copper_coin",
		new CoinItem(new Item.Settings(), Currency.COPPER, 0xE08A4F, true));

	public static final Item SILVER_COIN = register("silver_coin",
		new CoinItem(new Item.Settings(), Currency.SILVER, 0xD6DEE8, true));

	public static final Item GOLD_COIN = register("gold_coin",
		new CoinItem(new Item.Settings().rarity(Rarity.UNCOMMON), Currency.GOLD, 0xFFD23F, true));

	public static final Item BANKNOTE = register("banknote",
		new CoinItem(new Item.Settings().rarity(Rarity.RARE), Currency.NOTE, 0x7DE3A0, false));

	public static final Item WALLET = register("wallet",
		new WalletItem(new Item.Settings().maxCount(1).rarity(Rarity.UNCOMMON).component(ModComponents.BALANCE, 0L)));

	private ModItems() {
	}

	private static Item register(String name, Item item) {
		return Registry.register(Registries.ITEM, AurumMod.id(name), item);
	}

	public static void init() {
	}
}
