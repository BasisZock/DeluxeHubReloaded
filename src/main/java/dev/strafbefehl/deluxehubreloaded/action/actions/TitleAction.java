package dev.strafbefehl.deluxehubreloaded.action.actions;

import dev.strafbefehl.deluxehubreloaded.DeluxeHubPlugin;
import dev.strafbefehl.deluxehubreloaded.action.Action;
import dev.strafbefehl.deluxehubreloaded.utility.TextUtil;
import org.bukkit.entity.Player;

public class TitleAction implements Action {

	@Override
	public String getIdentifier() {
		return "TITLE";
	}

	@Override
	public void execute(DeluxeHubPlugin plugin, Player player, String data) {
		String[] args = data.split(";");

		String mainTitle = TextUtil.color(args[0]);
		String subTitle = TextUtil.color(args[1]);

		int fadeIn;
		int stay;
		int fadeOut;
		try {
			fadeIn = Integer.parseInt(args[2]);
			stay = Integer.parseInt(args[3]);
			fadeOut = Integer.parseInt(args[4]);
		} catch (NumberFormatException ex) {
			fadeIn = 1;
			stay = 3;
			fadeOut = 1;
		}

//		if (XMaterial.supports(10)) {
			player.sendTitle(mainTitle, subTitle, fadeIn * 20, stay * 20, fadeOut * 20);
//		} else {
//			Titles.sendTitle(player, fadeIn * 20, stay * 20, fadeOut * 20, mainTitle, subTitle);
//		}
	}
}
