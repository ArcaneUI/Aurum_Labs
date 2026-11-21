package dev.aurum.seasons.item;

import dev.aurum.seasons.AurumSeasons;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class SeasonCalendarItem extends Item {
    public SeasonCalendarItem(Settings settings) { super(settings); }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        if (!world.isClient) {
            ServerWorld sw = (ServerWorld) world;
            long day = sw.getTimeOfDay() / AurumSeasons.TICKS_PER_DAY;
            AurumSeasons.Season s = AurumSeasons.getSeasonForDay(day);
            int dayOfSeason = (int) (day % AurumSeasons.DAYS_PER_SEASON) + 1;
            int daysLeft = AurumSeasons.DAYS_PER_SEASON - dayOfSeason;
            String sign = s.tempModifier > 0 ? "+" : "";
            
            player.sendMessage(Text.literal(s.displayName + " · День " + dayOfSeason + "/" + AurumSeasons.DAYS_PER_SEASON + 
                " · Осталось дней: " + daysLeft + " · Темп: " + sign + s.tempModifier).formatted(s.color), true);
        }
        return TypedActionResult.success(player.getStackInHand(hand), world.isClient());
    }
}
