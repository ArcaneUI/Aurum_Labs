package dev.aurum.economy.mixin;

import dev.aurum.economy.economy.VillagerEconomy;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Единственная «ванильная» точка вмешательства: общий предок жителей и странствующего торговца.
 */
@Mixin(MerchantEntity.class)
public abstract class MerchantEntityMixin {

	/** Любой запрос списка сделок -> заменяем изумруды монетами (работает и для старых миров). */
	@Inject(method = "getOffers", at = @At("RETURN"))
	private void aurum$convertEmeralds(CallbackInfoReturnable<TradeOfferList> cir) {
		VillagerEconomy.convertOffers((MerchantEntity) (Object) this, cir.getReturnValue());
	}

	/** После успешной сделки — фонтан монет и звон. */
	@Inject(method = "trade", at = @At("TAIL"))
	private void aurum$tradeEffects(TradeOffer offer, CallbackInfo ci) {
		VillagerEconomy.onTrade((MerchantEntity) (Object) this);
	}
}
