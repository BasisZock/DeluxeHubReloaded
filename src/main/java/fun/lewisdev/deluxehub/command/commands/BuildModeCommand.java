package fun.lewisdev.deluxehub.command.commands;

import cl.bgmp.minecraft.util.commands.CommandContext;
import cl.bgmp.minecraft.util.commands.annotations.Command;
import cl.bgmp.minecraft.util.commands.exceptions.CommandException;
import fun.lewisdev.deluxehub.DeluxeHubPlugin;
import fun.lewisdev.deluxehub.Permissions;
import fun.lewisdev.deluxehub.base.BuildMode;
import fun.lewisdev.deluxehub.config.Messages;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BuildModeCommand {

	public BuildModeCommand(DeluxeHubPlugin plugin){}

	@Command(
			aliases = {"buildmode"},
			desc = "Toggle build mode."
	)
	public void buildMode(final CommandContext args, final CommandSender sender) throws CommandException {
		if (!(sender instanceof Player)) throw new CommandException("Console cannot use build mode");
		if (!(sender.hasPermission(Permissions.COMMAND_BUILD_MODE.getPermission()))) {
			Messages.NO_PERMISSION.send(sender);
			return;
		}

		Player player = (Player) sender;
		BuildMode bm = BuildMode.getInstance();

		if(bm.isPresent(player.getUniqueId())){
			bm.removePlayer(player.getUniqueId());
			Messages.BUILD_MODE_DISABLED.send(player);
			return;
		}
		bm.addPlayer(player);
		Messages.BUILD_MODE_ENABLED.send(player);
	}
}
