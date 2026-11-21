package dev.aurum.seasons.client;

import dev.aurum.seasons.AurumSeasons;
import dev.aurum.seasons.network.SeasonSyncPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.block.Blocks;

public class AurumSeasonsClient implements ClientModInitializer {
    private static AurumSeasons.Season clientSeason = AurumSeasons.Season.SPRING;
    private static int clientDayOfSeason = 1;

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(SeasonSyncPayload.ID, (payload, context) -> {
            AurumSeasons.Season newSeason = AurumSeasons.Season.values()[payload.seasonIndex()];
            if (clientSeason != newSeason) {
                clientSeason = newSeason;
                context.client().execute(() -> {
                    if (context.client().worldRenderer != null) {
                        context.client().worldRenderer.reload();
                    }
                });
            }
            clientDayOfSeason = payload.dayOfSeason();
        });

        ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) -> {
            if (world == null || pos == null) return 0x77ab2f;
            return switch (clientSeason) {
                case SPRING -> 0x88cc44;
                case SUMMER -> 0x55aa22;
                case AUTUMN -> 0xdcae3f;
                case WINTER -> 0xaaddff;
            };
        }, Blocks.OAK_LEAVES, Blocks.BIRCH_LEAVES, Blocks.DARK_OAK_LEAVES, Blocks.JUNGLE_LEAVES, Blocks.ACACIA_LEAVES,
           Blocks.GRASS_BLOCK, Blocks.SHORT_GRASS, Blocks.TALL_GRASS, Blocks.FERN, Blocks.LARGE_FERN);

        HudRenderCallback.EVENT.register(this::renderSeasonHud);
    }

    private void renderSeasonHud(DrawContext ctx, RenderTickCounter tickCounter) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.options.hudHidden) return;

        String text = clientSeason.displayName + " · День " + clientDayOfSeason + "/" + AurumSeasons.DAYS_PER_SEASON;
        int color = switch (clientSeason) {
            case SPRING -> 0xFF6BFF8E;
            case SUMMER -> 0xFFFFD23F;
            case AUTUMN -> 0xFFFF9A3C;
            case WINTER -> 0xFF6BE6FF;
        };

        // Removed visual season text from HUD per user request
    }
}
