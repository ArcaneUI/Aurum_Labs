package dev.aurum.economy.item;

import dev.aurum.economy.currency.Currency;
import dev.aurum.economy.registry.ModParticles;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

/**
 * Монета или купюра с фиксированным номиналом.
 * ПКМ по монете — подбросить её («Орёл или решка?»), купюры подбрасывать нельзя.
 */
public class CoinItem extends Item {
	private final long value;
	private final int color;
	private final boolean flippable;

	public CoinItem(Settings settings, long value, int color, boolean flippable) {
		super(settings);
		this.value = value;
		this.color = color;
		this.flippable = flippable;
	}

	public long getValue() {
		return value;
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
		ItemStack stack = player.getStackInHand(hand);
		if (!flippable) return TypedActionResult.pass(stack);

		if (!world.isClient) {
			boolean heads = world.random.nextBoolean();
			player.sendMessage(Text.translatable(heads ? "message.aurum.flip.heads" : "message.aurum.flip.tails")
				.styled(s -> s.withColor(color).withBold(true)), true);

			net.minecraft.util.math.Vec3d look = player.getRotationVec(1.0f);
			net.minecraft.util.math.Vec3d spawn = player.getEyePos().add(look.multiply(0.78));

			ServerWorld sw = (ServerWorld) world;
			// Используем vy = 0.44 как флаг для увеличенной частицы
			net.minecraft.particle.SimpleParticleType coinParticle = switch ((int)value) {
                case 1 -> ModParticles.COIN_COPPER;
                case 9 -> ModParticles.COIN_SILVER;
                default -> ModParticles.COIN;
            };
            sw.spawnParticles(coinParticle, spawn.x, spawn.y, spawn.z, 0, 0.0, 0.44, 0.0, 1.0);
			sw.spawnParticles(net.minecraft.particle.ParticleTypes.FIREWORK, spawn.x, spawn.y, spawn.z, 4, 0.1, 0.1, 0.1, 0.05);

			world.playSound(null, spawn.x, spawn.y, spawn.z, SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.PLAYERS, 0.8f, heads ? 1.9f : 1.35f);
			world.playSound(null, spawn.x, spawn.y, spawn.z, SoundEvents.ITEM_ARMOR_EQUIP_CHAIN.value(), SoundCategory.PLAYERS, 0.8f, 1.2f);
			
			player.getItemCooldownManager().set(this, 18);
		}
		return TypedActionResult.success(stack, world.isClient());
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		tooltip.add(Text.translatable("tooltip.aurum.value", Currency.format(value)).styled(s -> s.withColor(color)));
		if (stack.getCount() > 1) {
			tooltip.add(Text.translatable("tooltip.aurum.stack_value", Currency.format(value * stack.getCount()))
				.formatted(Formatting.DARK_GRAY));
		}
		if (flippable) {
			tooltip.add(Text.translatable("tooltip.aurum.flip").formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
		}
	}
}
