package dev.aurum.machines;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
public class ModItemGroups {
    public static final ItemGroup MACHINES = Registry.register(Registries.ITEM_GROUP, AurumMachines.id("machines"),
        FabricItemGroup.builder().icon(() -> new ItemStack(AurumMachines.MOTOR)).displayName(Text.translatable("itemGroup.aurum_machines")).entries((ctx, entries) -> {
            entries.add(AurumMachines.CONVEYOR); entries.add(AurumMachines.CRUSHER); entries.add(AurumMachines.PRESS); entries.add(AurumMachines.SORTER); entries.add(AurumMachines.PIPE); entries.add(AurumMachines.GENERATOR);
            entries.add(AurumMachines.MOTOR); entries.add(AurumMachines.CIRCUIT); entries.add(AurumMachines.IRON_GEAR); entries.add(AurumMachines.GOLD_GEAR); entries.add(AurumMachines.COPPER_WIRE);
            entries.add(AurumMachines.IRON_DUST); entries.add(AurumMachines.GOLD_DUST); entries.add(AurumMachines.COPPER_DUST);
            entries.add(AurumMachines.IRON_PLATE); entries.add(AurumMachines.GOLD_PLATE); entries.add(AurumMachines.COPPER_PLATE);
        }).build());
    public static void init() {}
}
