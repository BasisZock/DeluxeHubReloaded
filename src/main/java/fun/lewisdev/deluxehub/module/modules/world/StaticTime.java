package fun.lewisdev.deluxehub.module.modules.world;

import fun.lewisdev.deluxehub.DeluxeHubPlugin;
import fun.lewisdev.deluxehub.config.ConfigType;
import fun.lewisdev.deluxehub.module.Module;
import fun.lewisdev.deluxehub.module.ModuleType;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

public class StaticTime extends Module {
	private int _time;

	public StaticTime(DeluxeHubPlugin plugin) {
		super(plugin, ModuleType.STATIC_TIME);
	}

	@Override
	public void onEnable() {
		FileConfiguration config = getConfig(ConfigType.SETTINGS);
		_time = config.getInt("static_time.time");
		runScheduler();
	}

	private void runScheduler(){
		final DeluxeHubPlugin plugin = DeluxeHubPlugin.getPlugin(DeluxeHubPlugin.class);
		new BukkitRunnable(){
			@Override
			public void run() {
				Bukkit.getWorlds().forEach(world->{
					if(plugin.getModuleManager().getDisabledWorlds().contains(world.getName())) return;
					if(world.getTime() != _time) world.setTime(_time);
				});
			}
		}.runTaskTimer(plugin, 2L, 2L);
	}

	@Override
	public void onDisable() {}
}
