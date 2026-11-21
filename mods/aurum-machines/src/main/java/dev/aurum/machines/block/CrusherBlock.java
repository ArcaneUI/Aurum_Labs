package dev.aurum.machines.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CrusherBlock extends Block {
    public CrusherBlock(Settings settings) { super(settings); }

    private void crush(World world, BlockPos pos, ItemStack stack, PlayerEntity player, ItemEntity entity) {
        Item in = stack.getItem();
        Item out = null;
        int count = 1;

        if (in == Items.RAW_IRON || in == Items.IRON_ORE) { out = Registries.ITEM.get(Identifier.of("aurum_machines:iron_dust")); count = 2; }
        else if (in == Items.RAW_GOLD || in == Items.GOLD_ORE) { out = Registries.ITEM.get(Identifier.of("aurum_machines:gold_dust")); count = 2; }
        else if (in == Items.RAW_COPPER || in == Items.COPPER_ORE) { out = Registries.ITEM.get(Identifier.of("aurum_machines:copper_dust")); count = 2; }
        else if (in == Items.COBBLESTONE) out = Items.GRAVEL;
        else if (in == Items.GRAVEL) out = Items.SAND;
        else if (in == Items.BONE) { out = Items.BONE_MEAL; count = 4; }

        if (out != null && out != Items.AIR) {
            stack.decrement(1);
            ItemStack res = new ItemStack(out, count);
            if (player != null) player.getInventory().offerOrDrop(res);
            else if (entity != null) world.spawnEntity(new ItemEntity(world, pos.getX()+0.5, pos.getY()+1, pos.getZ()+0.5, res));

            if (!world.isClient) {
                ((ServerWorld)world).spawnParticles(ParticleTypes.CRIT, pos.getX()+0.5, pos.getY()+1, pos.getZ()+0.5, 10, 0.2, 0.2, 0.2, 0.1);
                world.playSound(null, pos, SoundEvents.BLOCK_GRINDSTONE_USE, SoundCategory.BLOCKS, 1f, 0.8f);
            }
        }
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        ItemStack stack = player.getStackInHand(Hand.MAIN_HAND);
        if (!stack.isEmpty()) {
            crush(world, pos, stack, player, null);
            return ActionResult.success(world.isClient);
        }
        return ActionResult.PASS;
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (entity instanceof ItemEntity ie) {
            crush(world, pos, ie.getStack(), null, ie);
        }
    }
}
