package dev.aurum.economy.currency;

import dev.aurum.economy.item.CoinItem;
import dev.aurum.economy.item.WalletItem;
import dev.aurum.economy.registry.ModItems;
import dev.aurum.economy.registry.ModParticles;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

/**
 * Единая «денежная система» мода. Базовая единица — 1 AU (аурум).
 * Номиналы кратны 9, как слитки и блоки в ванильном Minecraft.
 */
public final class Currency {
	public static final long COPPER = 1L;
	public static final long SILVER = 9L;
	public static final long GOLD = 81L;
	public static final long NOTE = 729L;

	/** Максимум, который помещается в кошелёк. */
	public static final long MAX_BALANCE = 1_000_000L;

	private Currency() {
	}

	/** Стоимость обычных монет в стопке (кошельки не считаются). */
	public static long looseValue(ItemStack stack) {
		if (stack.getItem() instanceof CoinItem coin) {
			return coin.getValue() * stack.getCount();
		}
		return 0L;
	}

	/** Полный баланс игрока: монеты, купюры и содержимое всех кошельков в инвентаре. */
	public static long total(PlayerEntity player) {
		long sum = 0L;
		PlayerInventory inv = player.getInventory();
		for (int i = 0; i < inv.size(); i++) {
			ItemStack s = inv.getStack(i);
			if (s.isEmpty()) continue;
			sum += looseValue(s);
			if (s.getItem() instanceof WalletItem) {
				sum += WalletItem.getBalance(s);
			}
		}
		return sum;
	}

	/** Раскладывает сумму в минимальное количество купюр и монет. */
	public static List<ItemStack> toStacks(long value) {
		List<ItemStack> out = new ArrayList<>();
		long[] values = {NOTE, GOLD, SILVER, COPPER};
		Item[] items = {ModItems.BANKNOTE, ModItems.GOLD_COIN, ModItems.SILVER_COIN, ModItems.COPPER_COIN};
		for (int i = 0; i < values.length; i++) {
			long n = value / values[i];
			value %= values[i];
			while (n > 0) {
				int c = (int) Math.min(n, 64L);
				out.add(new ItemStack(items[i], c));
				n -= c;
			}
		}
		return out;
	}

	/** Выдаёт игроку сумму наличными (лишнее падает под ноги). */
	public static void give(PlayerEntity player, long value) {
		for (ItemStack stack : toStacks(value)) {
			player.getInventory().offerOrDrop(stack);
		}
	}

	/** Выдаёт предметы стопками, не превышающими максимальный размер стопки. */
	public static void giveItems(PlayerEntity player, Item item, long total) {
		while (total > 0) {
			int n = (int) Math.min(total, item.getMaxCount());
			player.getInventory().offerOrDrop(new ItemStack(item, n));
			total -= n;
		}
	}

	/** Красивое число: 1 234 567 */
	public static String format(long value) {
		String raw = Long.toString(Math.abs(value));
		StringBuilder sb = new StringBuilder();
		int count = 0;
		for (int i = raw.length() - 1; i >= 0; i--) {
			sb.append(raw.charAt(i));
			if (++count % 3 == 0 && i != 0) sb.append(' ');
		}
		if (value < 0) sb.append('-');
		return sb.reverse().toString();
	}

	// ------------------------------------------------------------------
	//  Эффекты: фонтан монет и звон
	// ------------------------------------------------------------------

	/** Монеты вылетают фонтаном и красиво вращаются (см. CoinParticle на клиенте). */
	public static void burst(ServerWorld world, double x, double y, double z, int count) {
		Random r = world.random;
		for (int i = 0; i < count; i++) {
			double angle = r.nextDouble() * Math.PI * 2;
			double speed = 0.03 + r.nextDouble() * 0.11;
			world.spawnParticles(ModParticles.COIN, x, y, z, 0,
				Math.cos(angle) * speed, 0.28 + r.nextDouble() * 0.22, Math.sin(angle) * speed, 1.0);
		}
	}

	/** Звон монет: хрустальный перезвон + металлический шорох. */
	public static void clink(World world, double x, double y, double z, float pitch) {
		world.playSound(null, x, y, z, SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.PLAYERS, 0.8f, 1.5f + (pitch - 1f) * 0.5f);
		world.playSound(null, x, y, z, SoundEvents.ITEM_ARMOR_EQUIP_CHAIN.value(), SoundCategory.PLAYERS, 0.9f, pitch);
	}
}
