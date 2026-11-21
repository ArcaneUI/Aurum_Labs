package dev.aurum.farming.block;

import dev.aurum.farming.AurumFarming;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class KitchenTableBlock extends Block {
    public KitchenTableBlock(Settings settings) { super(settings); }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        ItemStack stack = player.getStackInHand(Hand.MAIN_HAND);
        if (stack.getCount() >= 2) {
            Item out = null;
            if (stack.isOf(AurumFarming.TOMATO)) out = AurumFarming.TOMATO_SOUP;
            else if (stack.isOf(AurumFarming.CORN)) out = AurumFarming.ROASTED_CORN;
            else if (stack.isOf(AurumFarming.STRAWBERRY)) out = AurumFarming.STRAWBERRY_TART;
            else if (stack.isOf(AurumFarming.ONION)) out = AurumFarming.ONION_RINGS;
            else if (stack.isOf(AurumFarming.RICE)) out = AurumFarming.FARMERS_PAELLA;

            if (out != null) {
                if (!world.isClient) {
                    if (!player.isCreative()) stack.decrement(2);
                    player.getInventory().offerOrDrop(new ItemStack(out));
                    world.playSound(null, pos, SoundEvents.BLOCK_CAMPFIRE_CRACKLE, SoundCategory.BLOCKS, 1f, 1f);
                    ((ServerWorld)world).spawnParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, pos.getX()+0.5, pos.getY()+1.1, pos.getZ()+0.5, 5, 0.2, 0.1, 0.2, 0.05);
                }
                return ActionResult.success(world.isClient);
            }
        }
        return ActionResult.PASS;
    }
}
