package dev.aurum.economy.client.hud;

import dev.aurum.economy.currency.Currency;
import dev.aurum.economy.item.CoinItem;
import dev.aurum.economy.item.WalletItem;
import dev.aurum.economy.registry.ModItems;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * Анимированный баланс в правом верхнем углу.
 *  - число «накручивается» плавно, как счётчик в казино;
 *  - при изменении баланса панель «пульсирует», а рядом всплывает +N / -N;
 *  - панель появляется при изменениях или когда вы держите монету/кошелёк;
 *  - клавиша B циклически меняет режим: авто -> скрыт -> закреплён.
 */
public final class BalanceHud {
	private static final ItemStack ICON_COPPER = new ItemStack(ModItems.COPPER_COIN);
	private static final ItemStack ICON_SILVER = new ItemStack(ModItems.SILVER_COIN);
	private static final ItemStack ICON_GOLD = new ItemStack(ModItems.GOLD_COIN);
	private static final ItemStack ICON_NOTE = new ItemStack(ModItems.BANKNOTE);

	/**
	 * Режимы HUD:
	 *  AUTO   — показывать при изменении баланса или когда игрок держит деньги.
	 *  HIDDEN — не показывать вообще, даже если в руке кошелёк.
	 *  PINNED — всегда показывать.
	 *
	 * Цикл клавишей B: AUTO -> HIDDEN -> PINNED -> AUTO.
	 */
	private enum HudMode {
		AUTO,
		HIDDEN,
		PINNED
	}

	private static final class Popup {
		final long delta;
		int age;

		Popup(long delta) {
			this.delta = delta;
		}
	}

	private static final List<Popup> POPUPS = new ArrayList<>();
	private static KeyBinding toggleKey;
	private static HudMode mode = HudMode.AUTO;
	private static boolean initialized;
	private static long target;
	private static double shown;
	private static int sinceChange = 9999;
	private static int pulse;
	private static float alpha, prevAlpha;

	private BalanceHud() {
	}

	public static void init() {
		toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
			"key.aurum.toggle_hud", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_B, "key.categories.aurum"));
		ClientTickEvents.END_CLIENT_TICK.register(BalanceHud::tick);
		HudRenderCallback.EVENT.register(BalanceHud::render);
	}

	private static boolean holdsCurrency(PlayerEntity player) {
		ItemStack main = player.getMainHandStack();
		ItemStack off = player.getOffHandStack();
		return main.getItem() instanceof CoinItem || main.getItem() instanceof WalletItem
			|| off.getItem() instanceof CoinItem || off.getItem() instanceof WalletItem;
	}

	private static void tick(MinecraftClient client) {
		PlayerEntity player = client.player;
		if (player == null) {
			initialized = false;
			POPUPS.clear();
			alpha = prevAlpha = 0f;
			return;
		}

		while (toggleKey.wasPressed()) {
			mode = switch (mode) {
				case AUTO -> HudMode.HIDDEN;
				case HIDDEN -> HudMode.PINNED;
				case PINNED -> HudMode.AUTO;
			};
			sinceChange = 0;
		}

		long total = Currency.total(player);
		if (!initialized) {
			target = total;
			shown = total;
			initialized = true;
		} else if (total != target) {
			POPUPS.add(new Popup(total - target));
			target = total;
			sinceChange = 0;
			pulse = 10;
		} else {
			sinceChange++;
		}

		// «Накрутка» числа: быстро приближаемся к цели
		double diff = target - shown;
		if (Math.abs(diff) < 1.0) {
			shown = target;
		} else {
			shown += Math.signum(diff) * Math.max(1.0, Math.abs(diff) * 0.2);
		}

		boolean visible = switch (mode) {
			case HIDDEN -> false;
			case PINNED -> true;
			case AUTO -> sinceChange < 120 || holdsCurrency(player);
		};
		prevAlpha = alpha;
		alpha += ((visible ? 1f : 0f) - alpha) * 0.45f;
		if (!visible && alpha < 0.05f) alpha = 0f;

		if (pulse > 0) pulse--;
		POPUPS.removeIf(p -> ++p.age > 44);
	}

	private static ItemStack iconFor(long value) {
		if (value >= Currency.NOTE) return ICON_NOTE;
		if (value >= Currency.GOLD) return ICON_GOLD;
		if (value >= Currency.SILVER) return ICON_SILVER;
		return ICON_COPPER;
	}

	private static void render(DrawContext ctx, RenderTickCounter tickCounter) {
		MinecraftClient mc = MinecraftClient.getInstance();
		if (mc.player == null || mc.options.hudHidden || mc.getDebugHud().shouldShowDebugHud()) return;

		float partial = tickCounter.getTickDelta(false);
		float a = MathHelper.lerp(partial, prevAlpha, alpha);
		if (a < 0.01f) return;

		TextRenderer font = mc.textRenderer;
		String text = Currency.format(Math.round(shown)) + " AU";
		int textWidth = font.getWidth(text);
		int w = textWidth + 34;
		int h = 22;
		int x = ctx.getScaledWindowWidth() - w - 8;
		int y = 8 - Math.round((1f - a) * 14f);
		int alphaByte = Math.max(4, Math.min(255, Math.round(a * 255f)));

		MatrixStack matrices = ctx.getMatrices();
		matrices.push();

		// Пульсация панели при изменении баланса
		float pulseScale = 1f + (pulse - partial) / 10f * 0.10f;
		if (pulse > 0) {
			float cx = x + w, cy = y + h / 2f;
			matrices.translate(cx, cy, 0);
			matrices.scale(pulseScale, pulseScale, 1f);
			matrices.translate(-cx, -cy, 0);
		}

		// Панель: тёмный фон, золотая рамка, тонкая светлая полоса сверху
		ctx.fill(x, y, x + w, y + h, (Math.round(a * 0xC0) << 24) | 0x14101A);
		ctx.fill(x + 1, y + 1, x + w - 1, y + 2, (Math.round(a * 0x50) << 24) | 0xFFE9A0);
		ctx.drawBorder(x, y, w, h, (alphaByte << 24) | 0xC9A23C);

		ctx.drawItem(iconFor(Math.round(shown)), x + 4, y + 3);
		int color = (alphaByte << 24) | (pulse > 0 ? 0xFFF2A8 : 0xFFD23F);
		ctx.drawText(font, text, x + 26, y + 7, color, true);
		matrices.pop();

		// Всплывающие +N / -N: плывут вниз и тают
		int slot = 0;
		for (Popup p : POPUPS) {
			float life = p.age + partial;
			float t = life / 44f;
			int pa = Math.max(4, Math.round((1f - t * t) * 255f * a));
			int py = y + h + 3 + Math.round(life * 0.55f) + slot * 10;
			String sign = (p.delta > 0 ? "+" : "-") + Currency.format(Math.abs(p.delta));
			int rgb = p.delta > 0 ? 0x6BFF8E : 0xFF6B6B;
			ctx.drawText(font, sign, x + w - font.getWidth(sign) - 2, py, (pa << 24) | rgb, true);
			slot++;
			if (slot > 3) break;
		}
	}
}
