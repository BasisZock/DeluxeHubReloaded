package fun.lewisdev.deluxehub.module.modules.player;

import fun.lewisdev.deluxehub.DeluxeHubPlugin;
import fun.lewisdev.deluxehub.config.ConfigType;
import fun.lewisdev.deluxehub.config.Messages;
import fun.lewisdev.deluxehub.module.Module;
import fun.lewisdev.deluxehub.module.ModuleType;
import fun.lewisdev.deluxehub.module.modules.hotbar.HotbarManager;
import fun.lewisdev.deluxehub.utility.ItemStackBuilder;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.*;

public class PvPMode extends Module {
    private short _slot;
    private short _time_to_toggle;
    private final EnumMap<PvPSwitcherState, ItemStack> _switcher = new EnumMap<>(PvPSwitcherState.class);
    private final EnumMap<PvPItemType, ItemStack> _items = new EnumMap<>(PvPItemType.class);
    private final List<UUID> _players = new ArrayList<>();
    private final Map<UUID, Integer> _tasks = new HashMap<>();

    public enum PvPSwitcherState {
        PVP_ON,
        PVP_OFF
    }

    public enum PvPItemType {
        SWORD(0),
        HELMET(103),
        CHESTPLATE(102),
        LEGGINGS(101),
        BOOTS(100);

        private final int _slot;

        PvPItemType(int slot) {
            _slot = slot;
        }

        public int getSlot() {
            return _slot;
        }
    }

    public PvPMode(DeluxeHubPlugin plugin) {
        super(plugin, ModuleType.PVP_MODE);
    }

    @Override
    public void onEnable() {
        ConfigurationSection config = getPlugin().getConfigManager().getFile(ConfigType.SETTINGS).getConfig().getConfigurationSection("pvp_mode");
		_slot = (short) config.getInt("slot");
        _time_to_toggle = (short) config.getInt("time_to_toggle");
        ConfigurationSection switcherSection = config.getConfigurationSection("switcher");
        if(switcherSection == null){
            Messages.PVP_MODE_NO_SWITCHER_SECTION.send(getPlugin().getServer().getConsoleSender());
            return;
        }
        for (PvPSwitcherState state : PvPSwitcherState.values()) {
            ConfigurationSection section = switcherSection.getConfigurationSection(state.name().toLowerCase());
            if (section != null) {
                _switcher.put(state, ItemStackBuilder.getItemStack(switcherSection).addPartialData(section).build());
            }
        }
        ConfigurationSection itemsSection = config.getConfigurationSection("items");
        if (itemsSection != null) {
            for (PvPItemType type : PvPItemType.values()) {
                ConfigurationSection section = itemsSection.getConfigurationSection(type.name().toLowerCase());
                if (section != null) {
                    _items.put(type, ItemStackBuilder.getItemStack(section).build());
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onJoin(PlayerJoinEvent ev) {
        Player player = ev.getPlayer();
        Inventory inv = player.getInventory();
        boolean found = false;
        for (int i = 0; i < 9; i++) {
            int slot = (_slot + i) % 9;
			ItemStack item = inv.getItem(slot);
			if (item == null || (item.hasItemMeta() && item.getItemMeta().getDisplayName().equals(_switcher.get(PvPSwitcherState.PVP_OFF).getItemMeta().getDisplayName()))) {
				_slot = (short) slot;
				found = true;
				break;
			}
        }
        if (!found) {
            Messages.PVP_MODE_NO_EMPTY_SLOT_FOUND.send(player);
            getPlugin().getLogger().warning("No empty slot found for PvPMode switcher for player " + player.getName());
            return;
        }
        inv.setItem(_slot, _switcher.get(PvPSwitcherState.PVP_OFF));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChangeItem(PlayerItemHeldEvent ev) {
        Player player = ev.getPlayer();
		UUID pUUID = player.getUniqueId();
        if(_tasks.containsKey(pUUID)){
            BukkitScheduler scheduler = getPlugin().getServer().getScheduler();
			int taskId = _tasks.get(player.getUniqueId());
            scheduler.cancelTask(taskId);
            _tasks.remove(pUUID);
			player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new ComponentBuilder().append(" ").create());
            return;
        }
		PlayerInventory inv = player.getInventory();
        ItemStack item = inv.getItem(ev.getNewSlot());
		if(item == null) return;
		if(!item.hasItemMeta()) return;
		if(!item.getItemMeta().hasDisplayName()) return;
		if(item.getItemMeta().getDisplayName().equals(Objects.requireNonNull(_switcher.get(PvPSwitcherState.PVP_OFF).getItemMeta()).getDisplayName())){
			int taskId = getPlugin().getServer().getScheduler().runTaskTimer(getPlugin(), new Runnable() {
				int timeLeft = _time_to_toggle;
				@Override
				public void run() {
					if(timeLeft > 0){
						player.spigot().
								sendMessage(ChatMessageType.ACTION_BAR, new ComponentBuilder().appendLegacy(Messages.PVP_MODE_SWITCH_ON_TIME.toString().replaceAll("&", "§").replace("%time%", ""+timeLeft)).create());
						timeLeft--;
					}else {
						if (player.isOnline()) {
							_players.add(pUUID);
							inv.clear();
							inv.setItem(_slot, _switcher.get(PvPSwitcherState.PVP_ON));
							for (PvPItemType itemType : PvPItemType.values()) {
								ItemStack is = _items.get(itemType);
								switch (itemType) {
									case SWORD:
										short slot = (short) itemType.getSlot();
										while (inv.getItem(slot) != null) {
											slot = (short) ((slot + 1) % 9);
										}
										inv.setItem(slot, is);
										inv.setHeldItemSlot(slot);
										continue;
									case HELMET:
										inv.setHelmet(is);
										break;
									case CHESTPLATE:
										inv.setChestplate(is);
										break;
									case LEGGINGS:
										inv.setLeggings(is);
										break;
									case BOOTS:
										inv.setBoots(is);
										break;
									default:
										inv.setItem(itemType.getSlot(), _items.get(itemType));
								}
							}
							inv.clear(_slot);
							inv.setItem(8, _switcher.get(PvPSwitcherState.PVP_ON));
							getPlugin().getServer().getScheduler().cancelTask(_tasks.get(pUUID));
							_tasks.remove(pUUID);
							player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new ComponentBuilder().appendLegacy(Messages.PVP_MODE_LETS_FIGHT.toString().replaceAll("&", "§")).create());
						}
					}
				}
			}, 0L, 20L).getTaskId();
            _tasks.put(pUUID, taskId);
        }else if(item.getItemMeta().getDisplayName().equals(Objects.requireNonNull(_switcher.get(PvPSwitcherState.PVP_ON).getItemMeta()).getDisplayName())){
            int taskId = getPlugin().getServer().getScheduler().runTaskTimer(getPlugin(), new Runnable() {
				int timeLeft = _time_to_toggle;
				@Override
				public void run() {
					if (timeLeft > 0) {
						player.spigot().
								sendMessage(ChatMessageType.ACTION_BAR, new ComponentBuilder().appendLegacy(Messages.PVP_MODE_SWITCH_OFF_TIME.toString().replaceAll("&", "§").replace("%time%", ""+timeLeft)).create());
						timeLeft--;
					} else {
						if (player.isOnline()) {
							HotbarManager hotbarManager = (HotbarManager) getPlugin().getModuleManager().getModule(ModuleType.HOTBAR_ITEMS);
							_players.remove(pUUID);
							inv.clear();
							hotbarManager.giveItems(player);
							inv.setItem(_slot, _switcher.get(PvPSwitcherState.PVP_OFF));
							hotbarManager.changeToJoinSlot(player);
							getPlugin().getServer().getScheduler().cancelTask(_tasks.get(pUUID));
							_tasks.remove(pUUID);
							player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new ComponentBuilder().append(" ").create());
						}
					}
				}
			}, 0L, 20L).getTaskId();
            _tasks.put(pUUID, taskId);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onLeave(PlayerQuitEvent ev) {
		UUID pUUID = ev.getPlayer().getUniqueId();
        _players.remove(pUUID);
		_tasks.remove(pUUID);
    }

    @Override
    public void onDisable() {
    }
}
