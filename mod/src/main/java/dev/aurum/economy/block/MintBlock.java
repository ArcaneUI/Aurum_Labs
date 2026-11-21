package dev.aurum.economy.block;

import com.mojang.serialization.MapCodec;
import dev.aurum.economy.currency.Currency;
import dev.aurum.economy.registry.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * Монетный двор: ПКМ слитком — получить монеты. Shift + ПКМ — отчеканить всю стопку сразу.
 */
public class MintBlock extends Block {
	public static final MapCodec<MintBlock> CODEC = createCodec(MintBlock::new);

	public record Rate(Item output, int count) {
	}

	public MintBlock(Settings settings) {
		super(settings);
	}

	@Override
	protected MapCodec<? extends Block> getCodec() {
		return CODEC;
	}

	/** Курс чеканки. Один слиток -> N монет. */
	@Nullable
	public static Rate rateFor(Item item) {
		if (item == Items.COPPER_INGOT) return new Rate(ModItems.COPPER_COIN, 3);
		if (item == Items.IRON_INGOT) return new Rate(ModItems.SILVER_COIN, 1);
		if (item == Items.EMERALD) return new Rate(ModItems.SILVER_COIN, 1);
		if (item == Items.GOLD_INGOT) return new Rate(ModItems.GOLD_COIN, 1);
		if (item == Items.DIAMOND) return new Rate(ModItems.GOLD_COIN, 2);
		if (item == Items.NETHERITE_INGOT) return new Rate(ModItems.BANKNOTE, 2);
		return null;
	}

	@Override
	protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos,
											 PlayerEntity player, Hand hand, BlockHitResult hit) {
		Rate rate = rateFor(stack.getItem());
		if (rate == null) {
			return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		}
		if (world.isClient) {
			return ItemActionResult.SUCCESS;
		}

		int amount = player.isSneaking() ? stack.getCount() : 1;
		if (!player.isCreative()) {
			stack.decrement(amount);
		}
		Currency.giveItems(player, rate.output(), (long) rate.count() * amount);

		ServerWorld sw = (ServerWorld) world;
		double x = pos.getX() + 0.5, y = pos.getY() + 1.05, z = pos.getZ() + 0.5;
		Currency.burst(sw, x, y, z, Math.min(18, 4 + amount * 2));
		sw.spawnParticles(ParticleTypes.CRIT, x, y, z, 10, 0.25, 0.05, 0.25, 0.25);
		world.playSound(null, pos, SoundEvents.BLOCK_ANVIL_PLACE, SoundCategory.BLOCKS, 0.45f, 1.5f);
		Currency.clink(world, x, y, z, 1.25f);
		return ItemActionResult.SUCCESS;
	}

	@Override
	protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		if (!world.isClient) {
			player.sendMessage(Text.translatable("message.aurum.mint.help").formatted(Formatting.GOLD), true);
		}
		return ActionResult.SUCCESS;
	}
}
