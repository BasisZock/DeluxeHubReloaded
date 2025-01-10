package fun.lewisdev.deluxehub.command;

import co.aikar.commands.PaperCommandManager;
import fun.lewisdev.deluxehub.DeluxeHubPlugin;
import fun.lewisdev.deluxehub.module.Module;
import fun.lewisdev.deluxehub.module.ModuleType;

import java.util.List;
import java.util.stream.Collectors;

public class CustomCommandHandler extends Module {

	private List<CustomCommand> commands;

	public CustomCommandHandler(DeluxeHubPlugin plugin) {
		super(plugin, ModuleType.CUSTOM_COMMANDS);
	}

	@Override
	public void onEnable() {
		final PaperCommandManager commandManager = new PaperCommandManager(this.getPlugin());
		commands = getPlugin().getCommandManager().getCustomCommands();

		commandManager.getCommandReplacements().addReplacement("%custom-commands",
				this.commands.stream().map(CustomCommand::getAliases)
						.flatMap(List::stream).collect(Collectors.joining("|")));
		commandManager.registerCommand(new CustomCommandLoader(this));
	}

	@Override
	public void onDisable() {
		//does nothing
	}

	public List<CustomCommand> getCommands() {
		return commands;
	}
}