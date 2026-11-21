package dev.aurum.economy.item;

import dev.aurum.economy.currency.Currency;
import dev.aurum.economy.registry.ModComponents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

/**
 * Кошелёк хранит деньги в компоненте {@code aurum:balance}.
 * ПКМ — убрать все монеты из инвентаря в кошелёк.
 * Shift + ПКМ — достать все деньги наличными (купюры, золото, серебро, медь).
 */
public class WalletItem extends Item {
	public WalletItem(Settings settings) {
		super(settings);
	}

	public static long getBalance(ItemStack stack) {
		return stack.getOrDefault(ModComponents.BALANCE, 0L);
	}

	public static void setBalance(ItemStack stack, long value) {
		stack.set(ModComponents.BALANCE, Math.max(0L, Math.min(Currency.MAX_BALANCE, value)));
	}

	@Override
	public boolean hasGlint(ItemStack stack) {
		// Богатый кошелёк переливается как зачарованный.
		return getBalance(stack) > 0;
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
		ItemStack wallet = player.getStackInHand(hand);
		if (world.isClient) {
			return TypedActionResult.success(wallet);
		}
		if (player.isSneaking()) {
			withdraw((ServerWorld) world, player, wallet);
		} else {
			deposit((ServerWorld) world, player, wallet);
		}
		return TypedActionResult.success(wallet, false);
	}

	private void deposit(ServerWorld world, PlayerEntity player, ItemStack wallet) {
		long sum = 0L;
		PlayerInventory inv = player.getInventory();
		long room = Currency.MAX_BALANCE - getBalance(wallet);

		for (int i = 0; i < inv.size() && room > 0; i++) {
			ItemStack s = inv.getStack(i);
			if (!(s.getItem() instanceof CoinItem coin)) continue;
			long per = coin.getValue();
			int take = (int) Math.min(s.getCount(), room / per);
			if (take <= 0) continue;
			sum += per * take;
			room -= per * take;
			s.decrement(take);
		}

		if (sum <= 0) {
			player.sendMessage(Text.translatable("message.aurum.wallet.nothing_to_deposit").formatted(Formatting.GRAY), true);
			return;
		}

		long balance = getBalance(wallet) + sum;
		setBalance(wallet, balance);
		player.sendMessage(Text.translatable("message.aurum.wallet.deposit",
			Currency.format(sum), Currency.format(balance)).formatted(Formatting.GREEN), true);
		feedback(world, player, 6, 1.35f);
	}

	private void withdraw(ServerWorld world, PlayerEntity player, ItemStack wallet) {
		long balance = getBalance(wallet);
		if (balance <= 0) {
			player.sendMessage(Text.translatable("message.aurum.wallet.empty").formatted(Formatting.GRAY), true);
			return;
		}
		setBalance(wallet, 0L);
		Currency.give(player, balance);
		player.sendMessage(Text.translatable("message.aurum.wallet.withdraw", Currency.format(balance))
			.formatted(Formatting.GOLD), true);
		feedback(world, player, 14, 0.95f);
	}

	private static void feedback(ServerWorld world, PlayerEntity player, int coins, float pitch) {
		Vec3d look = player.getRotationVec(1.0f);
		Currency.burst(world, player.getX() + look.x * 0.7, player.getEyeY() - 0.3, player.getZ() + look.z * 0.7, coins);
		Currency.clink(world, player.getX(), player.getY(), player.getZ(), pitch);
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		long balance = getBalance(stack);
		tooltip.add(Text.translatable("tooltip.aurum.wallet.balance", Currency.format(balance))
			.formatted(balance > 0 ? Formatting.GOLD : Formatting.GRAY));
		tooltip.add(Text.translatable("tooltip.aurum.wallet.deposit").formatted(Formatting.DARK_GRAY));
		tooltip.add(Text.translatable("tooltip.aurum.wallet.withdraw").formatted(Formatting.DARK_GRAY));
	}
}
