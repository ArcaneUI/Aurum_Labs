package dev.aurum.economy.event;

import dev.aurum.economy.currency.Currency;
import dev.aurum.economy.registry.ModItems;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.random.Random;

/**
 * Источник денег №1: монстры, убитые игроком, роняют монеты.
 * Рейдеры богаче обычных мобов, а боссы щедро платят купюрами.
 */
public final class CoinDrops {
	private CoinDrops() {
	}

	public static void init() {
		ServerLivingEntityEvents.AFTER_DEATH.register(CoinDrops::onDeath);
	}

	private static void drop(LivingEntity entity, Item item, int count) {
		if (count > 0) {
			entity.dropStack(new ItemStack(item, count));
		}
	}

	private static void onDeath(LivingEntity entity, DamageSource source) {
		if (!(entity.getWorld() instanceof ServerWorld world)) return;
		if (!(source.getAttacker() instanceof PlayerEntity)) return;

		Random r = world.random;
		EntityType<?> type = entity.getType();
		int coins = 0;

		if (type == EntityType.ENDER_DRAGON) {
			drop(entity, ModItems.GOLD_COIN, 5 + r.nextInt(4));
			drop(entity, ModItems.BANKNOTE, 2);
			coins = 24;
		} else if (type == EntityType.WITHER) {
			drop(entity, ModItems.GOLD_COIN, 4 + r.nextInt(3));
			drop(entity, ModItems.BANKNOTE, 1 + r.nextInt(2));
			coins = 20;
		} else if (type == EntityType.WARDEN) {
			drop(entity, ModItems.GOLD_COIN, 6 + r.nextInt(4));
			drop(entity, ModItems.BANKNOTE, 2 + r.nextInt(2));
			coins = 24;
		} else if (type == EntityType.ELDER_GUARDIAN) {
			drop(entity, ModItems.GOLD_COIN, 3 + r.nextInt(3));
			coins = 14;
		} else if (type.isIn(EntityTypeTags.RAIDERS)) {
			if (r.nextFloat() < 0.55f) {
				int n = 1 + r.nextInt(2);
				drop(entity, ModItems.SILVER_COIN, n);
				coins += n;
			}
			if (r.nextFloat() < 0.06f) {
				drop(entity, ModItems.GOLD_COIN, 1);
				coins += 3;
			}
		} else if (entity instanceof Monster) {
			if (entity instanceof SlimeEntity slime && slime.getSize() > 1) return;
			if (r.nextFloat() < 0.40f) {
				int n = 1 + r.nextInt(3);
				drop(entity, ModItems.COPPER_COIN, n);
				coins += n;
			}
			if (r.nextFloat() < 0.07f) {
				drop(entity, ModItems.SILVER_COIN, 1);
				coins += 2;
			}
			if (r.nextFloat() < 0.008f) {
				drop(entity, ModItems.GOLD_COIN, 1);
				coins += 3;
			}
		} else {
			return;
		}

		if (coins > 0) {
			Currency.burst(world, entity.getX(), entity.getBodyY(0.6), entity.getZ(), Math.min(coins + 1, 24));
			Currency.clink(world, entity.getX(), entity.getY(), entity.getZ(), 1.2f);
		}
	}
}
