package dev.strafbefehl.deluxehubreloaded.module.modules.world;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import dev.strafbefehl.deluxehubreloaded.DeluxeHubPlugin;
import dev.strafbefehl.deluxehubreloaded.Permissions;
import dev.strafbefehl.deluxehubreloaded.config.ConfigType;
import dev.strafbefehl.deluxehubreloaded.config.Messages;
import dev.strafbefehl.deluxehubreloaded.module.Module;
import dev.strafbefehl.deluxehubreloaded.module.ModuleType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

public class AntiWorldDownloader extends Module implements PluginMessageListener {

	private final boolean legacy;

	public AntiWorldDownloader(DeluxeHubPlugin plugin) {
		super(plugin, ModuleType.ANTI_WDL);
		this.legacy = false;
	}

	@Override
	public void onEnable() {
		if (legacy) {
			getPlugin().getServer().getMessenger().registerIncomingPluginChannel(getPlugin(), "WDL|INIT", this);
			getPlugin().getServer().getMessenger().registerOutgoingPluginChannel(getPlugin(), "WDL|CONTROL");
		} else {
			getPlugin().getServer().getMessenger().registerIncomingPluginChannel(getPlugin(), "wdl:init", this);
			getPlugin().getServer().getMessenger().registerOutgoingPluginChannel(getPlugin(), "wdl:control");
		}
	}

	@Override
	public void onDisable() {
		if (legacy) {
			getPlugin().getServer().getMessenger().unregisterIncomingPluginChannel(getPlugin(), "WDL|INIT");
			getPlugin().getServer().getMessenger().unregisterOutgoingPluginChannel(getPlugin(), "WDL|CONTROL");
		} else {
			getPlugin().getServer().getMessenger().unregisterIncomingPluginChannel(getPlugin(), "wdl:init");
			getPlugin().getServer().getMessenger().unregisterOutgoingPluginChannel(getPlugin(), "wdl:control");
		}
	}

	public void onPluginMessageReceived(String channel, Player player, byte[] data) {
		if (player.hasPermission(Permissions.ANTI_WDL_BYPASS.getPermission())) return;

		if (legacy && channel.equals("WDL|INIT") || !legacy && channel.equals("wdl:init")) {

			ByteArrayDataOutput out = ByteStreams.newDataOutput();
			out.writeInt(0);
			out.writeBoolean(false);
			if (legacy) player.sendPluginMessage(getPlugin(), "WDL|CONTROL", out.toByteArray());
			else player.sendPluginMessage(getPlugin(), "wdl:control", out.toByteArray());

			if (!getPlugin().getConfigManager().getFile(ConfigType.SETTINGS).getConfig().getBoolean("anti_wdl.admin_notify"))
				return;

			for (Player p : Bukkit.getOnlinePlayers()) {
				if (p.hasPermission(Permissions.ANTI_WDL_NOTIFY.getPermission())) {
					Messages.WORLD_DOWNLOAD_NOTIFY.send(p, "%player%", player.getName());
				}
			}
		}
	}
}
