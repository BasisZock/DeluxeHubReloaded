package dev.strafbefehl.deluxehubreloaded.action.actions;

import dev.strafbefehl.deluxehubreloaded.DeluxeHubPlugin;
import dev.strafbefehl.deluxehubreloaded.action.Action;
import org.bukkit.entity.Player;

public class PauseAction implements Action {
    @Override
    public String getIdentifier() {
        return "PAUSE";
    }

    @Override
    public void execute(DeluxeHubPlugin plugin, Player player, String data) {

    }
}
