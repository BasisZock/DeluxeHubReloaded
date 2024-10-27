package fun.lewisdev.deluxehub.base;

import fun.lewisdev.deluxehub.DeluxeHubPlugin;
import fun.lewisdev.deluxehub.config.ConfigType;
import fun.lewisdev.deluxehub.config.Messages;
import fun.lewisdev.deluxehub.module.ModuleType;
import fun.lewisdev.deluxehub.module.modules.hotbar.HotbarManager;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.GameMode;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class BuildMode implements Listener {
	private static BuildMode _instance;
	private final DeluxeHubPlugin _plugin;
	private final ArrayList<UUID> _players = new ArrayList<>();
	private final List<String> _worlds;
	private boolean _actionbar_enabled = true;
	private boolean _invertedWorlds = false;


	public BuildMode() {
		_instance = this;
		this._plugin = DeluxeHubPlugin.getPlugin(DeluxeHubPlugin.class);
		this.runScheduler();
		FileConfiguration config = _plugin.getConfigManager().getFile(ConfigType.SETTINGS).getConfig();
		this._actionbar_enabled = config.getBoolean("build_mode.use_actionbar_message");
		this._invertedWorlds = config.getBoolean("disabled-worlds.inverted");
		this._worlds = config.getStringList("disabled-worlds.worlds");

		this._plugin.getServer().getPluginManager().registerEvents(this, _plugin);
	}

	public static BuildMode getInstance() {
		if (_instance == null) {
			_instance = new BuildMode();
		}
		return _instance;
	}

	public void runScheduler() {
		new BukkitRunnable() {
			@Override
			public void run() {
				if (_plugin.getServer().getOnlinePlayers().isEmpty()) return;
				for (Player p : _plugin.getServer().getOnlinePlayers()) {
					if (_players.contains(p.getUniqueId())) {
						if (!isInPermittedWorld(Objects.requireNonNull(p.getLocation().getWorld()).getName())) {
							p.getInventory().clear();
							removePlayer(p.getUniqueId());
							continue;
						}
						if (p.getGameMode() != GameMode.CREATIVE) p.setGameMode(GameMode.CREATIVE);
						if (_actionbar_enabled)
							p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new ComponentBuilder().appendLegacy(Messages.BUILD_MODE_ENABLED_ACTIONBAR.toString().replaceAll("&", "§")).create());
					}
				}
			}
		}.runTaskTimer(_plugin, 5L, 5L);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void PlayerQuit(PlayerQuitEvent event) {
		final UUID uuid = event.getPlayer().getUniqueId();
		if (isPresent(uuid)) removePlayer(uuid);
	}

	public void addPlayer(Player player) {
		_players.add(player.getUniqueId());
		player.getInventory().clear();
		player.setGameMode(GameMode.CREATIVE);
		player.getInventory().setHeldItemSlot(0);
	}

	public void removePlayer(UUID uuid) {
		Player player = _plugin.getServer().getPlayer(uuid);
		if (player == null) return;
		player.getInventory().clear();
		_players.remove(uuid);
		if (player.isOnline()) {
			player.setGameMode(GameMode.SURVIVAL);
			((HotbarManager) _plugin.getModuleManager().getModule(ModuleType.HOTBAR_ITEMS)).giveItems(player);
		}
	}

	public boolean isPresent(UUID uuid) {
		return _players.contains(uuid);
	}

	public boolean isInPermittedWorld(String world) {
		return _invertedWorlds == _worlds.contains(world);
	}
}
