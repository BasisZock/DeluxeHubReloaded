package dev.strafbefehl.deluxehubreloaded.command.commands;

import cl.bgmp.minecraft.util.commands.CommandContext;
import cl.bgmp.minecraft.util.commands.annotations.Command;
import cl.bgmp.minecraft.util.commands.exceptions.CommandException;
import dev.strafbefehl.deluxehubreloaded.DeluxeHubPlugin;
import dev.strafbefehl.deluxehubreloaded.Permissions;
import dev.strafbefehl.deluxehubreloaded.config.Messages;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FlyCommand {

	public static Map<UUID, Boolean> allowPlayerFly = new HashMap<>();

	public FlyCommand(DeluxeHubPlugin plugin) {
	}

	@Command(
			aliases = {"fly"},
			desc = "Toggle flight mode",
			usage = "[player]",
			max = 1
	)
	public void flight(final CommandContext args, final CommandSender sender) throws CommandException {

		if (args.argsLength() == 0) {
			if (!(sender instanceof Player)) throw new CommandException("Console cannot clear inventory");

			if (!(sender.hasPermission(Permissions.COMMAND_FLIGHT.getPermission()))) {
				Messages.NO_PERMISSION.send(sender);
				return;
			}

			Player player = (Player) sender;
			if (allowPlayerFly.putIfAbsent(player.getUniqueId(), false) == null) ;
			if (allowPlayerFly.get(player.getUniqueId())) {
				Messages.FLIGHT_DISABLE.send(player);
				toggleFlight(player, false);
				allowPlayerFly.put(player.getUniqueId(), false);
				player.setAllowFlight(true);
			} else {
				Messages.FLIGHT_ENABLE.send(player);
				toggleFlight(player, true);
				allowPlayerFly.put(player.getUniqueId(), true);
			}
		} else if (args.argsLength() == 1) {
			if (!(sender.hasPermission(Permissions.COMMAND_FLIGHT_OTHERS.getPermission()))) {
				Messages.NO_PERMISSION.send(sender);
				return;
			}

			Player player = Bukkit.getPlayer(args.getString(0));
			if (player == null) {
				Messages.INVALID_PLAYER.send(sender, "%player%", args.getString(0));
				return;
			}
			if (player.getAllowFlight()) {
				Messages.FLIGHT_DISABLE.send(player);
				Messages.FLIGHT_DISABLE_OTHER.send(sender, "%player%", player.getName());
				toggleFlight(player, false);
			} else {
				Messages.FLIGHT_ENABLE.send(player);
				Messages.FLIGHT_ENABLE_OTHER.send(sender, "%player%", player.getName());
				toggleFlight(player, true);
			}
		}
	}

	private void toggleFlight(Player player, boolean value) {
		player.setAllowFlight(value);
		player.setFlying(value);
	}
}