package dev.aurum.oceans.item;

import dev.aurum.oceans.AurumOceans;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

public class FishingNetItem extends Item {
    public FishingNetItem(Settings settings) { super(settings); }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        HitResult hit = raycast(world, player, RaycastContext.FluidHandling.SOURCE_ONLY);
        if (hit.getType() == HitResult.Type.BLOCK && world.getFluidState(((net.minecraft.util.hit.BlockHitResult)hit).getBlockPos()).isOf(net.minecraft.fluid.Fluids.WATER)) {
            if (!world.isClient) {
                Item[] loot = {Items.COD, Items.SALMON, AurumOceans.CRAB_MEAT, AurumOceans.SHELL, AurumOceans.STARFISH, AurumOceans.PEARL, AurumOceans.BLACK_PEARL};
                int[] weights = {40, 30, 15, 10, 5, 3, 1};
                int total = 104;
                int r = world.random.nextInt(total);
                Item out = Items.COD;
                for (int i = 0; i < weights.length; i++) {
                    if (r < weights[i]) { out = loot[i]; break; }
                    r -= weights[i];
                }
                
                player.getInventory().offerOrDrop(new ItemStack(out, 1 + world.random.nextInt(2)));
                stack.damage(1, player, PlayerEntity.getSlotForHand(hand));
                
                net.minecraft.util.math.Vec3d pos = hit.getPos();
                ((ServerWorld)world).spawnParticles(ParticleTypes.SPLASH, pos.x, pos.y, pos.z, 20, 0.5, 0.1, 0.5, 0.1);
                world.playSound(null, pos.x, pos.y, pos.z, SoundEvents.ENTITY_FISHING_BOBBER_SPLASH, SoundCategory.PLAYERS, 1f, 1f);
                player.getItemCooldownManager().set(this, 20);
            }
            return TypedActionResult.success(stack, world.isClient());
        }
        return TypedActionResult.pass(stack);
    }
}
