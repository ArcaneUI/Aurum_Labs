package dev.aurum.backpacks.client;

import dev.aurum.backpacks.AurumBackpacks;
import dev.aurum.backpacks.network.OpenBackpackPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class AurumBackpacksClient implements ClientModInitializer {
    private static KeyBinding openKey;

    @Override
    public void onInitializeClient() {
        net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback.EVENT.register((entityType, entityRenderer, registrationHelper, context) -> {
            if (entityType == net.minecraft.entity.EntityType.PLAYER) {
                registrationHelper.register(new BackpackFeatureRenderer<>(entityRenderer));
            }
        });
        openKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.aurum_backpacks.open", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_V, "key.categories.aurum"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openKey.wasPressed()) {
                if (ClientPlayNetworking.canSend(OpenBackpackPayload.ID)) {
                    ClientPlayNetworking.send(new OpenBackpackPayload(true));
                }
            }
        });

        ColorProviderRegistry.ITEM.register((stack, tintIndex) -> {
            if (tintIndex > 0) return -1;
            int defaultColor = switch (((dev.aurum.backpacks.BackpackItem)stack.getItem()).getTier()) {
                case "leather" -> 0xA06540;
                case "iron" -> 0xE0E0E0;
                case "gold" -> 0xFFD700;
                case "diamond" -> 0x00FFFF;
                case "netherite" -> 0x404040;
                default -> 0xFFFFFF;
            };
            net.minecraft.component.type.DyedColorComponent dyed = stack.get(net.minecraft.component.DataComponentTypes.DYED_COLOR);
            return dyed != null ? dyed.rgb() : defaultColor;
        }, AurumBackpacks.LEATHER_BACKPACK, AurumBackpacks.IRON_BACKPACK, AurumBackpacks.GOLD_BACKPACK, AurumBackpacks.DIAMOND_BACKPACK, AurumBackpacks.NETHERITE_BACKPACK);
    }
}
// append
