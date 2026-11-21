package dev.aurum.economy.block;

import com.mojang.serialization.MapCodec;
import dev.aurum.economy.currency.Currency;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

/**
 * Витрина монет: ПКМ предметом — выставить его (он парит и вращается),
 * ПКМ пустой рукой — забрать. Идеальна для демонстрации богатства.
 */
public class CoinPedestalBlock extends BlockWithEntity {
	public static final MapCodec<CoinPedestalBlock> CODEC = createCodec(CoinPedestalBlock::new);

	private static final VoxelShape SHAPE = VoxelShapes.union(
		Block.createCuboidShape(3, 0, 3, 13, 2, 13),
		Block.createCuboidShape(5, 2, 5, 11, 12, 11),
		Block.createCuboidShape(3, 12, 3, 13, 14, 13)
	);

	public CoinPedestalBlock(Settings settings) {
		super(settings);
	}

	@Override
	protected MapCodec<? extends BlockWithEntity> getCodec() {
		return CODEC;
	}

	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new CoinPedestalBlockEntity(pos, state);
	}

	@Override
	protected BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}

	@Override
	protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return SHAPE;
	}

	@Override
	protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos,
											 PlayerEntity player, Hand hand, BlockHitResult hit) {
		if (!(world.getBlockEntity(pos) instanceof CoinPedestalBlockEntity pedestal) || stack.isEmpty()) {
			return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		}
		if (world.isClient) {
			return ItemActionResult.SUCCESS;
		}

		ItemStack previous = pedestal.getStack();
		pedestal.setStack(stack.copyWithCount(1));
		if (!player.isCreative()) {
			stack.decrement(1);
		}
		if (!previous.isEmpty()) {
			player.getInventory().offerOrDrop(previous);
		}

		ServerWorld sw = (ServerWorld) world;
		Currency.burst(sw, pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5, 4);
		Currency.clink(world, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, 1.6f);
		return ItemActionResult.SUCCESS;
	}

	@Override
	protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		if (!(world.getBlockEntity(pos) instanceof CoinPedestalBlockEntity pedestal) || pedestal.getStack().isEmpty()) {
			return ActionResult.PASS;
		}
		if (!world.isClient) {
			player.getInventory().offerOrDrop(pedestal.getStack());
			pedestal.setStack(ItemStack.EMPTY);
			Currency.clink(world, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, 1.1f);
		}
		return ActionResult.SUCCESS;
	}

	@Override
	protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
		if (!state.isOf(newState.getBlock()) && world.getBlockEntity(pos) instanceof CoinPedestalBlockEntity pedestal) {
			ItemScatterer.spawn(world, pos.getX() + 0.5, pos.getY() + 0.9, pos.getZ() + 0.5, pedestal.getStack());
		}
		super.onStateReplaced(state, world, pos, newState, moved);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
		if (world.getBlockEntity(pos) instanceof CoinPedestalBlockEntity pedestal && !pedestal.getStack().isEmpty()) {
			if (random.nextInt(3) == 0) {
				world.addParticle(ParticleTypes.END_ROD,
					pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.7,
					pos.getY() + 1.0 + random.nextDouble() * 0.6,
					pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.7,
					0.0, 0.015, 0.0);
			}
		}
	}
}
