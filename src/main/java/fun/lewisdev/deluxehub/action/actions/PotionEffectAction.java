package fun.lewisdev.deluxehub.action.actions;

import fun.lewisdev.deluxehub.DeluxeHubPlugin;
import fun.lewisdev.deluxehub.action.Action;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PotionEffectAction implements Action {

    @Override
    public String getIdentifier() {
        return "EFFECT";
    }

    @Override
    public void execute(DeluxeHubPlugin plugin, Player player, String data) {
        String[] args = data.split(";");
        PotionEffectType type = PotionEffectType.getByName(args[0].toUpperCase());
        if (type != null) {
            player.addPotionEffect(new PotionEffect(type, 9999999, Integer.parseInt(args[1]) - 1));
        }
    }
}
