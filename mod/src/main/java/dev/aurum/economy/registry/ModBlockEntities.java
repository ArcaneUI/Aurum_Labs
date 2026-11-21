package dev.aurum.economy.registry;

import dev.aurum.economy.AurumMod;
import dev.aurum.economy.block.CoinPedestalBlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public final class ModBlockEntities {
	public static final BlockEntityType<CoinPedestalBlockEntity> COIN_PEDESTAL = Registry.register(
		Registries.BLOCK_ENTITY_TYPE,
		AurumMod.id("coin_pedestal"),
		BlockEntityType.Builder.create(CoinPedestalBlockEntity::new, ModBlocks.COIN_PEDESTAL).build()
	);

	private ModBlockEntities() {
	}

	public static void init() {
	}
}
