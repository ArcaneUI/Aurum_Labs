package dev.aurum.machines.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class ConveyorBlock extends HorizontalFacingBlock {
    public static final com.mojang.serialization.MapCodec<ConveyorBlock> CODEC = createCodec(ConveyorBlock::new);
    @Override protected com.mojang.serialization.MapCodec<? extends net.minecraft.block.HorizontalFacingBlock> getCodec() { return CODEC; }
    public ConveyorBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState().with(FACING, Direction.NORTH));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) { builder.add(FACING); }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) { return getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing()); }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        Direction dir = state.get(FACING);
        double speed = 0.1;
        entity.addVelocity(dir.getOffsetX() * speed, 0, dir.getOffsetZ() * speed);
    }
}
