package dev.aurum.economy.registry;

import dev.aurum.economy.AurumMod;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public final class ModParticles {
	/** Вращающаяся золотая монета (8 кадров анимации, см. assets/aurum/particles/coin.json). */
	public static final SimpleParticleType COIN = Registry.register(
		Registries.PARTICLE_TYPE, AurumMod.id("coin"), FabricParticleTypes.simple(true));

	public static final SimpleParticleType COIN_COPPER = Registry.register(
		Registries.PARTICLE_TYPE, AurumMod.id("coin_copper"), FabricParticleTypes.simple(true));

	public static final SimpleParticleType COIN_SILVER = Registry.register(
		Registries.PARTICLE_TYPE, AurumMod.id("coin_silver"), FabricParticleTypes.simple(true));

	private ModParticles() {
	}

	public static void init() {
	}
}
