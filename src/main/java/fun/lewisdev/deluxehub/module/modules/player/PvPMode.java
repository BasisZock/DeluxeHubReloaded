package fun.lewisdev.deluxehub.module.modules.player;

import fun.lewisdev.deluxehub.DeluxeHubPlugin;
import fun.lewisdev.deluxehub.config.ConfigType;
import fun.lewisdev.deluxehub.config.Messages;
import fun.lewisdev.deluxehub.module.Module;
import fun.lewisdev.deluxehub.module.ModuleType;
import fun.lewisdev.deluxehub.module.modules.hotbar.HotbarManager;
import fun.lewisdev.deluxehub.utility.ItemStackBuilder;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.*;

public class PvPMode extends Module {
    private short _slot;
    private short _time_to_toggle;
    private final EnumMap<PvPSwitcherState, ItemStack> _switcher = new EnumMap<>(PvPSwitcherState.class);
    private final EnumMap<PvPItemType, ItemStack> _items = new EnumMap<>(PvPItemType.class);
    private final List<UUID> _players = new ArrayList<>();
    private final Map<Integer, UUID> _tasks = new HashMap<>();

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
                    _items.put(type, ItemStackBuilder.getItemStack(itemsSection).addPartialData(section).build());
                }
            }
        }
        getPlugin().getServer().getPluginManager().registerEvents(this, getPlugin());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onJoin(PlayerJoinEvent ev) {
        Player player = ev.getPlayer();
        Inventory inv = player.getInventory();
        boolean found = false;
        for (int i = 0; i < 9; i++) {
            int slot = (_slot + i) % 9;
            if (inv.getItem(slot) != null) continue;
            _slot = (short) slot;
            found = true;
            break;
        }
        if (!found) {
            Messages.PVP_MODE_NO_EMPTY_SLOT_FOUND.send(player);
            getPlugin().getLogger().warning("No empty slot found for PvPMode switcher for player " + player.getName());
            return;
        }
        inv.setItem(_slot, _switcher.get(PvPSwitcherState.PVP_OFF));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChangeItem(PlayerInteractEvent ev) {
        Player player = ev.getPlayer();
        // Remove task if player switches item
        if(_tasks.containsValue(player.getUniqueId()) && player.getInventory().getHeldItemSlot() != _slot){
            BukkitScheduler scheduler = getPlugin().getServer().getScheduler();
            int taskId = _tasks.entrySet().stream().filter(entry -> entry.getValue().equals(player.getUniqueId())).findFirst().get().getKey();
            scheduler.cancelTask(taskId);
            _tasks.remove(taskId);
            return;
        }
        if (!_players.contains(player.getUniqueId())) return;
        if (ev.getAction() != Action.RIGHT_CLICK_AIR && ev.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        ItemStack item = player.getInventory().getItemInMainHand();
        if(item == _switcher.get(PvPSwitcherState.PVP_OFF)){
            ev.setCancelled(true);
            player.getInventory().setItem(_slot, _switcher.get(PvPSwitcherState.PVP_ON));
            int taskId = getPlugin().getServer().getScheduler().runTaskLater(getPlugin(), () -> {
                if (player.isOnline()) {
                    _players.add(player.getUniqueId());
                    player.getInventory().clear();
                    player.getInventory().setItem(_slot, _switcher.get(PvPSwitcherState.PVP_OFF));
                    for (PvPItemType itemType : PvPItemType.values()) {
                        if(itemType == PvPItemType.SWORD){
                            short slot = (short) itemType.getSlot();
                            while(player.getInventory().getItem(slot) != null){
                                slot = (short) ((slot + 1) % 9);
                            }
                            player.getInventory().setItem(slot, _items.get(itemType));
                            continue;
                        }
                        player.getInventory().setItem(itemType.getSlot(), _items.get(itemType));
                    }
                }
            }, _time_to_toggle * 20).getTaskId();
            _tasks.put(taskId, player.getUniqueId());
        }else if(item == _switcher.get(PvPSwitcherState.PVP_ON)){
            ev.setCancelled(true);
            int taskId = getPlugin().getServer().getScheduler().runTaskLater(getPlugin(), () -> {
                if (player.isOnline()) {
                    _players.remove(player.getUniqueId());
                    player.getInventory().clear();
                    player.getInventory().setItem(_slot, _switcher.get(PvPSwitcherState.PVP_OFF));
                    ((HotbarManager) getPlugin().getModuleManager().getModule(ModuleType.HOTBAR_ITEMS)).giveItems(player);
                }
            }, _time_to_toggle * 20).getTaskId();
            _tasks.put(taskId, player.getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onLeave(PlayerQuitEvent ev) {
        _players.remove(ev.getPlayer().getUniqueId());
    }

    @Override
    public void onDisable() {
    }
}
