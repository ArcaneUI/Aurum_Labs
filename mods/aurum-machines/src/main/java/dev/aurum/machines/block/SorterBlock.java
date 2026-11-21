package dev.aurum.machines.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class SorterBlock extends HorizontalFacingBlock {
    public static final com.mojang.serialization.MapCodec<SorterBlock> CODEC = createCodec(SorterBlock::new);
    @Override protected com.mojang.serialization.MapCodec<? extends net.minecraft.block.HorizontalFacingBlock> getCodec() { return CODEC; }
    public SorterBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState().with(FACING, Direction.NORTH));
    }

    @Override protected void appendProperties(StateManager.Builder<Block, BlockState> builder) { builder.add(FACING); }
    @Override public BlockState getPlacementState(ItemPlacementContext ctx) { return getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing()); }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (entity instanceof ItemEntity ie) {
            ItemStack stack = ie.getStack();
            String name = net.minecraft.registry.Registries.ITEM.getId(stack.getItem()).getPath();
            boolean isResource = name.contains("ore") || name.contains("ingot") || name.contains("dust") || name.contains("plate");
            
            Direction dir = state.get(FACING);
            Direction outDir = isResource ? dir.rotateYClockwise() : dir.rotateYCounterclockwise();
            double speed = 0.15;
            ie.addVelocity(outDir.getOffsetX() * speed, 0, outDir.getOffsetZ() * speed);
        }
    }
}
