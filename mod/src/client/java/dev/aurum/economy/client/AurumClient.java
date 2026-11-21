package dev.aurum.economy.client;

import dev.aurum.economy.client.hud.BalanceHud;
import dev.aurum.economy.client.particle.CoinParticle;
import dev.aurum.economy.client.render.CoinPedestalRenderer;
import dev.aurum.economy.registry.ModBlockEntities;
import dev.aurum.economy.registry.ModParticles;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;

public class AurumClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ParticleFactoryRegistry.getInstance().register(ModParticles.COIN, CoinParticle.Factory::new);
		ParticleFactoryRegistry.getInstance().register(ModParticles.COIN_COPPER, CoinParticle.Factory::new);
		ParticleFactoryRegistry.getInstance().register(ModParticles.COIN_SILVER, CoinParticle.Factory::new);
		BlockEntityRendererFactories.register(ModBlockEntities.COIN_PEDESTAL, CoinPedestalRenderer::new);
		BalanceHud.init();
	}
}
