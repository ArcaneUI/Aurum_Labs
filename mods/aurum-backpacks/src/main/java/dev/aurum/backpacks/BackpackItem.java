package dev.aurum.backpacks;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;

public class BackpackItem extends Item {
    private final int slots;
    private final String tier;

    public BackpackItem(Settings settings, int slots, String tier) {
        super(settings.component(DataComponentTypes.CONTAINER, ContainerComponent.DEFAULT)
                      .component(AurumBackpacks.AUTO_PICKUP, false));
        this.slots = slots;
        this.tier = tier;
    }

    public int getSlots() { return slots; }
    public String getTier() { return tier; }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (!world.isClient) {
            if (player.isSneaking()) {
                boolean current = stack.getOrDefault(AurumBackpacks.AUTO_PICKUP, false);
                stack.set(AurumBackpacks.AUTO_PICKUP, !current);
                player.sendMessage(Text.literal("Автоподбор: " + (!current ? "ВКЛ" : "ВЫКЛ")).formatted(!current ? Formatting.GREEN : Formatting.RED), true);
                return TypedActionResult.success(stack, false);
            }
            openBackpack(player, stack);
        }
        return TypedActionResult.success(stack, world.isClient());
    }

    public void openBackpack(PlayerEntity player, ItemStack stack) {
        int rows = Math.max(1, Math.min(6, slots / 9));
        ContainerComponent component = stack.getOrDefault(DataComponentTypes.CONTAINER, ContainerComponent.DEFAULT);
        SimpleInventory inv = new SimpleInventory(slots);
        component.copyTo(inv.getHeldStacks());

        inv.addListener(inventory -> stack.set(DataComponentTypes.CONTAINER, ContainerComponent.fromStacks(inv.getHeldStacks())));

        player.openHandledScreen(new SimpleNamedScreenHandlerFactory(
            (syncId, playerInv, p) -> switch (rows) {
                case 1 -> new GenericContainerScreenHandler(net.minecraft.screen.ScreenHandlerType.GENERIC_9X1, syncId, playerInv, inv, rows);
                case 2 -> new GenericContainerScreenHandler(net.minecraft.screen.ScreenHandlerType.GENERIC_9X2, syncId, playerInv, inv, rows);
                case 4 -> new GenericContainerScreenHandler(net.minecraft.screen.ScreenHandlerType.GENERIC_9X4, syncId, playerInv, inv, rows);
                case 5 -> new GenericContainerScreenHandler(net.minecraft.screen.ScreenHandlerType.GENERIC_9X5, syncId, playerInv, inv, rows);
                case 6 -> new GenericContainerScreenHandler(net.minecraft.screen.ScreenHandlerType.GENERIC_9X6, syncId, playerInv, inv, rows);
                default -> new GenericContainerScreenHandler(net.minecraft.screen.ScreenHandlerType.GENERIC_9X3, syncId, playerInv, inv, rows);
            },
            Text.translatable("container.aurum_backpacks." + tier)
        ));
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.literal(slots + " слотов").formatted(Formatting.GRAY));
        boolean ap = stack.getOrDefault(AurumBackpacks.AUTO_PICKUP, false);
        tooltip.add(Text.literal("Автоподбор: " + (ap ? "ВКЛ" : "ВЫКЛ")).formatted(ap ? Formatting.GREEN : Formatting.RED));
        tooltip.add(Text.literal("ПКМ — открыть рюкзак").formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
        tooltip.add(Text.literal("Shift + ПКМ — автоподбор").formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
    }
}
