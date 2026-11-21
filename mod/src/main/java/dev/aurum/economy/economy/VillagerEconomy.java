package dev.aurum.economy.economy;

import dev.aurum.economy.currency.Currency;
import dev.aurum.economy.mixin.TradeOfferAccessor;
import dev.aurum.economy.registry.ModItems;
import dev.aurum.economy.registry.ModVillagers;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import net.minecraft.village.TradedItem;

import java.util.Optional;

/**
 * Сердце мода: все сделки с жителями и странствующим торговцем, где фигурировал изумруд,
 * автоматически пересчитываются в монеты.
 *
 * Курс: 1 изумруд = 1 серебряная монета (9 AU).
 * Дорогие сделки (18+ изумрудов) пересчитываются в золотые монеты (1 золотая = 9 серебряных),
 * поэтому в окне торговли вы видите красивые золотые стопки, а не 40 серебряных монет.
 */
public final class VillagerEconomy {
	private VillagerEconomy() {
	}

	private record Price(Item item, int count) {
	}

	/** Сколько монет заменяет N изумрудов. */
	private static Price priceFor(int emeralds) {
		if (emeralds >= 18) {
			return new Price(ModItems.GOLD_COIN, Math.max(2, Math.round(emeralds / 9.0f)));
		}
		return new Price(ModItems.SILVER_COIN, Math.max(1, emeralds));
	}

	/** Вызывается из миксина после каждого getOffers(): операция идемпотентна и дешёвая. */
	public static void convertOffers(MerchantEntity merchant, TradeOfferList offers) {
		if (offers == null || offers.isEmpty()) return;

		// У Банкира свои сделки — там изумруд меняется на монеты намеренно.
		if (merchant instanceof VillagerEntity villager
			&& villager.getVillagerData().getProfession() == ModVillagers.BANKER) {
			return;
		}

		for (TradeOffer offer : offers) {
			convert(offer);
		}
	}

	private static boolean isEmerald(TradedItem item) {
		return item.item().value() == Items.EMERALD;
	}

	private static void convert(TradeOffer offer) {
		TradeOfferAccessor accessor = (TradeOfferAccessor) offer;

		// 1) Игрок платит изумрудами (первый слот)
		TradedItem first = offer.getFirstBuyItem();
		if (isEmerald(first)) {
			Price p = priceFor(first.count());
			accessor.aurum$setFirstBuyItem(new TradedItem(p.item(), p.count()));
		}

		// 2) Игрок платит изумрудами (второй слот)
		Optional<TradedItem> second = offer.getSecondBuyItem();
		if (second.isPresent() && isEmerald(second.get())) {
			Price p = priceFor(second.get().count());
			accessor.aurum$setSecondBuyItem(Optional.of(new TradedItem(p.item(), p.count())));
		}

		// 3) Житель платит изумрудами игроку
		ItemStack sell = offer.getSellItem();
		if (sell.isOf(Items.EMERALD)) {
			Price p = priceFor(sell.getCount());
			accessor.aurum$setSellItem(new ItemStack(p.item(), p.count()));
		}
	}

	/** Анимация успешной сделки: фонтан монет над головой торговца и звон. */
	public static void onTrade(MerchantEntity merchant) {
		if (merchant.getWorld() instanceof ServerWorld world) {
			Currency.burst(world, merchant.getX(), merchant.getEyeY() + 0.35, merchant.getZ(), 10);
			Currency.clink(world, merchant.getX(), merchant.getY(), merchant.getZ(), 1.0f);
		}
	}
}
