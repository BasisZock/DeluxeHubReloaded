package dev.strafbefehl.deluxehubreloaded.action;

import dev.strafbefehl.deluxehubreloaded.DeluxeHubPlugin;
import dev.strafbefehl.deluxehubreloaded.action.actions.*;
import dev.strafbefehl.deluxehubreloaded.utility.PlaceholderUtil;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class ActionManager {

	private final DeluxeHubPlugin plugin;
	private final Map<String, Action> actions;

	public ActionManager(DeluxeHubPlugin plugin) {
		this.plugin = plugin;
		actions = new HashMap<>();
		load();
	}

	private void load() {
		registerAction(
				new MessageAction(),
				new BroadcastMessageAction(),
				new CommandAction(),
				new ConsoleCommandAction(),
				new SoundAction(),
				new PotionEffectAction(),
				new GamemodeAction(),
				new BungeeAction(),
				new ProxyAction(),
				new CloseInventoryAction(),
				new ActionbarAction(),
				new TitleAction(),
				new MenuAction(),
				new PauseAction()
		);
	}

	public void registerAction(Action... actions) {
		Arrays.asList(actions).forEach(action -> this.actions.put(action.getIdentifier(), action));
	}

	public void executeActions(Player player, List<String> items) {
		if (items == null || items.isEmpty()) {
			return;
			//Return if there are no actions
		}
		// Execute the actions sequentially
		executeSequentially(player, items, 0);
	}

	private void executeSequentially(Player player, List<String> allItems, int currentIndex) {
		if (currentIndex >= allItems.size()) {
			return;
		}

		String item = allItems.get(currentIndex);
		// Extract the action identifier (everything between square brackets)
		String actionIdentifier = StringUtils.substringBetween(item, "[", "]");
		Action action = actionIdentifier == null ? null : actions.get(actionIdentifier.toUpperCase());

		if (action != null) {
			// Extract the data (everything after the action identifier)
			String actionData = item.contains(" ") ? item.split(" ", 2)[1] : "";
			actionData = PlaceholderUtil.setPlaceholders(actionData, player);

			// Check if it is a pause action
			if (action.getIdentifier().equalsIgnoreCase("PAUSE")) {
				try {
					// Try changing the delay to a long
					long delayTicks = Long.parseLong(actionData.trim());

					if (delayTicks > 0) {
						// Schedules the next execution of the next action
						Bukkit.getScheduler().runTaskLater(plugin, () -> {
							executeSequentially(player, allItems, currentIndex + 1);
						}, delayTicks);
					} else {
						// If it is zero, just execute the next action
						executeSequentially(player, allItems, currentIndex + 1);
					}
				} catch (NumberFormatException e) {
					// If the number is invalid, we just execute the next action
					executeSequentially(player, allItems, currentIndex + 1);
					plugin.getLogger().severe("One or more [PAUSE] actions has an invalid number. It will be ignored");
				}
			} else {
				// If any other action, execute it
				action.execute(plugin, player, actionData);
				executeSequentially(player, allItems, currentIndex + 1);
			}
		} else {
			// If action wasn't found, just execute the next action
			plugin.getLogger().warning("Action not found: " + item);
			executeSequentially(player, allItems, currentIndex + 1);
		}
	}
}
