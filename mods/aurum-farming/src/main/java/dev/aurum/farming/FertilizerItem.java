package dev.aurum.farming;

import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class FertilizerItem extends Item {
    public FertilizerItem(Settings settings) { super(settings); }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        BlockState state = world.getBlockState(pos);

        if (state.getBlock() instanceof CropBlock crop && !crop.isMature(state)) {
            if (!world.isClient) {
                for (int i = 0; i < 3 + world.random.nextInt(3); i++) {
                    crop.applyGrowth(world, pos, state);
                    state = world.getBlockState(pos);
                    if (crop.isMature(state)) break;
                }
                ((ServerWorld) world).spawnParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX()+0.5, pos.getY()+0.5, pos.getZ()+0.5, 10, 0.3, 0.2, 0.3, 0.05);
                world.playSound(null, pos, SoundEvents.ITEM_BONE_MEAL_USE, SoundCategory.BLOCKS, 1f, 1f);
            }
            if (!context.getPlayer().isCreative()) context.getStack().decrement(1);
            return ActionResult.success(world.isClient);
        }
        return ActionResult.PASS;
    }
}
