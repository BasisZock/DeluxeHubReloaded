package dev.strafbefehl.deluxehubreloaded.command;

import java.util.ArrayList;
import java.util.List;

public class CustomCommand {

	private String permission;
	private final List<String> aliases;
	private final List<String> actions;

	public CustomCommand(String command, List<String> actions) {
		this.aliases = new ArrayList<>();
		this.aliases.add(command);
		this.actions = actions;
	}

	public void addAliases(List<String> aliases) {
		this.aliases.addAll(aliases);
	}

	public String getPermission() {
		return permission;
	}

	public void setPermission(String permission) {
		this.permission = permission;
	}

	public List<String> getAliases() {
		return aliases;
	}

	public List<String> getActions() {
		return actions;
	}

}
