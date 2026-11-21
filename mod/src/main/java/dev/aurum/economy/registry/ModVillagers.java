package dev.aurum.economy.registry;

import com.google.common.collect.ImmutableSet;
import dev.aurum.economy.AurumMod;
import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;
import net.fabricmc.fabric.api.object.builder.v1.world.poi.PointOfInterestHelper;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.SoundEvents;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.TradedItem;
import net.minecraft.village.VillagerProfession;
import net.minecraft.world.poi.PointOfInterestType;

/**
 * Профессия «Банкир» — рабочее место: Банковская стойка.
 * Банкир меняет изумруды на монеты, продаёт кошельки и редкие товары за золото и купюры.
 */
public final class ModVillagers {
	public static final RegistryKey<PointOfInterestType> TELLER_POI_KEY =
		RegistryKey.of(RegistryKeys.POINT_OF_INTEREST_TYPE, AurumMod.id("teller_desk"));

	public static final PointOfInterestType TELLER_POI =
		PointOfInterestHelper.register(AurumMod.id("teller_desk"), 1, 1, ModBlocks.TELLER_DESK);

	public static final VillagerProfession BANKER = Registry.register(
		Registries.VILLAGER_PROFESSION,
		AurumMod.id("banker"),
		new VillagerProfession(
			"banker",
			entry -> entry.matchesKey(TELLER_POI_KEY),
			entry -> entry.matchesKey(TELLER_POI_KEY),
			ImmutableSet.of(),
			ImmutableSet.of(),
			SoundEvents.ENTITY_VILLAGER_WORK_CARTOGRAPHER
		)
	);

	private ModVillagers() {
	}

	private static TradeOffers.Factory trade(ItemConvertible give, int giveCount,
											 ItemConvertible get, int getCount,
											 int maxUses, int xp, float multiplier) {
		return (entity, random) -> new TradeOffer(
			new TradedItem(give, giveCount),
			new ItemStack(get, getCount),
			maxUses, xp, multiplier
		);
	}

	public static void init() {
		// Ванильное правило: на каждом уровне житель берёт 2 случайные сделки из пула.
		// Поэтому на уровнях 1 и 2 — ровно по 2 сделки (они гарантированы), дальше — ротация.

		// Уровень 1 — Новичок: обмен изумрудов и кошелёк
		TradeOfferHelper.registerVillagerOffers(BANKER, 1, factories -> {
			factories.add(trade(Items.EMERALD, 1, ModItems.SILVER_COIN, 1, 24, 1, 0.02f));
			factories.add(trade(ModItems.SILVER_COIN, 4, ModItems.WALLET, 1, 6, 5, 0.05f));
		});

		// Уровень 2 — Ученик: обратный обмен и часы
		TradeOfferHelper.registerVillagerOffers(BANKER, 2, factories -> {
			factories.add(trade(ModItems.SILVER_COIN, 2, Items.EMERALD, 1, 24, 4, 0.02f));
			factories.add(trade(ModItems.GOLD_COIN, 1, Items.CLOCK, 1, 8, 8, 0.05f));
		});

		// Уровень 3 — Подмастерье
		TradeOfferHelper.registerVillagerOffers(BANKER, 3, factories -> {
			factories.add(trade(ModItems.GOLD_COIN, 2, Items.NAME_TAG, 1, 6, 12, 0.05f));
			factories.add(trade(ModItems.GOLD_COIN, 3, Items.SPYGLASS, 1, 6, 12, 0.05f));
			factories.add(trade(ModItems.SILVER_COIN, 8, Items.LEAD, 2, 10, 12, 0.05f));
		});

		// Уровень 4 — Эксперт
		TradeOfferHelper.registerVillagerOffers(BANKER, 4, factories -> {
			factories.add(trade(ModItems.GOLD_COIN, 2, Items.EXPERIENCE_BOTTLE, 4, 8, 18, 0.05f));
			factories.add(trade(ModItems.GOLD_COIN, 4, Items.SADDLE, 1, 4, 18, 0.05f));
			factories.add(trade(ModItems.GOLD_COIN, 6, Items.DIAMOND, 1, 4, 20, 0.05f));
		});

		// Уровень 5 — Мастер: сделки на банкноты
		TradeOfferHelper.registerVillagerOffers(BANKER, 5, factories -> {
			factories.add(trade(ModItems.BANKNOTE, 2, Items.TOTEM_OF_UNDYING, 1, 2, 30, 0.05f));
			factories.add(trade(ModItems.BANKNOTE, 3, Items.ENCHANTED_GOLDEN_APPLE, 1, 2, 30, 0.05f));
			factories.add(trade(ModItems.BANKNOTE, 4, Items.NETHERITE_INGOT, 1, 2, 30, 0.05f));
		});
	}
}
