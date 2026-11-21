package dev.aurum.backpacks;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
public class ModItemGroups {
    public static final ItemGroup BACKPACKS = Registry.register(Registries.ITEM_GROUP, AurumBackpacks.id("backpacks"),
        FabricItemGroup.builder().icon(() -> new ItemStack(AurumBackpacks.IRON_BACKPACK)).displayName(Text.translatable("itemGroup.aurum_backpacks")).entries((ctx, entries) -> {
            entries.add(AurumBackpacks.LEATHER_BACKPACK); entries.add(AurumBackpacks.IRON_BACKPACK); entries.add(AurumBackpacks.GOLD_BACKPACK); entries.add(AurumBackpacks.DIAMOND_BACKPACK); entries.add(AurumBackpacks.NETHERITE_BACKPACK);
        }).build());
    public static void init() {}
}
