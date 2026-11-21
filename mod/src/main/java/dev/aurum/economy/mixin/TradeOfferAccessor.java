package dev.aurum.economy.mixin;

import net.minecraft.item.ItemStack;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradedItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Optional;

/** Даёт право менять final-поля сделки, не пересоздавая её (сохраняются uses, demand, скидки героя). */
@Mixin(TradeOffer.class)
public interface TradeOfferAccessor {
	@Mutable
	@Accessor("firstBuyItem")
	void aurum$setFirstBuyItem(TradedItem item);

	@Mutable
	@Accessor("secondBuyItem")
	void aurum$setSecondBuyItem(Optional<TradedItem> item);

	@Mutable
	@Accessor("sellItem")
	void aurum$setSellItem(ItemStack stack);
}
