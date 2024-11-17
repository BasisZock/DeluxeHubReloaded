package fun.lewisdev.deluxehub.module.modules.player;

import fun.lewisdev.deluxehub.DeluxeHubPlugin;
import fun.lewisdev.deluxehub.config.ConfigType;
import fun.lewisdev.deluxehub.config.Messages;
import fun.lewisdev.deluxehub.cooldown.CooldownType;
import fun.lewisdev.deluxehub.module.Module;
import fun.lewisdev.deluxehub.module.ModuleType;
import fun.lewisdev.deluxehub.utility.ItemStackBuilder;
import fun.lewisdev.deluxehub.utility.NamespacedKeys;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.persistence.PersistentDataType;

public class TeleportationBow extends Module {
    private short _slot;
    private short _cooldown;
    private EntityType _entityType;
    private ItemStack _bow;

    public TeleportationBow(DeluxeHubPlugin plugin) {
        super(plugin, ModuleType.TELEPORTATION_BOW);
    }

    @Override
    public void onEnable() {
        ConfigurationSection config = getPlugin().getConfigManager().getFile(ConfigType.SETTINGS).getConfig().getConfigurationSection("teleportation_bow");
        _slot = (short) config.getInt("slot");
        _cooldown = (short) config.getInt("cooldown");
        try {
            _entityType = EntityType.valueOf(config.getString("entity"));
            if (!Projectile.class.isAssignableFrom(_entityType.getEntityClass())) {
                getPlugin().getLogger().warning("Invalid entity type for teleportation bow. Defaulting to ARROW.");
                throw new IllegalArgumentException("Invalid entity type for teleportation bow.");
            }
        }catch(IllegalArgumentException e) {
            _entityType = EntityType.ARROW;
            getPlugin().getLogger().warning("Invalid entity type for teleportation bow. Defaulting to ARROW.");
        }
        ConfigurationSection itemSection = config.getConfigurationSection("item");
        ItemStack bowItem = new ItemStack(Material.BOW, 1);
        if(itemSection == null){
            itemSection = config.createSection("item");
        }
        _bow = ItemStackBuilder.
                getItemStack(bowItem,
                        itemSection,
                        null)
                .addNamespacedKey(NamespacedKeys.Keys.TELEPORTATION_BOW.get(), PersistentDataType.BOOLEAN, true)
                .build();
        if(config.getBoolean("disable_inventory_movement")){
            getPlugin().getServer().getPluginManager().registerEvent(
                    InventoryClickEvent.class,
                    new Listener() {
                        @EventHandler(priority = EventPriority.HIGHEST)
                        public void onItemMove(InventoryClickEvent event) {
                            if(!(event.getWhoClicked() instanceof Player)) return;
                            if(event.getSlot() != _slot) return;
                            Inventory inv = event.getClickedInventory();
                            if(!(inv instanceof PlayerInventory)) return;
                            ItemStack item = event.getCurrentItem();
                            if(item == null) return;
                            if(!item.hasItemMeta() || (item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(NamespacedKeys.Keys.TELEPORTATION_BOW.get()))){
                                return;
                            }
                            event.setCancelled(true);
                        }
                    },
                    EventPriority.HIGHEST,
                    (listener, event) -> {},
                    getPlugin(),
                    true
            );
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onShoot(EntityShootBowEvent ev){
        if(!(ev.getEntity() instanceof Player)) return;
        Player player = (Player) ev.getEntity();
        if(ev.getBow() == null || !ev.getBow().isSimilar(_bow)) return;
        if(!tryCooldown(player.getUniqueId(), CooldownType.TELEPORTATION_BOW, _cooldown)){
            Messages.TELEPORTATION_BOW_IN_COOLDOWN.send(player, "%time%", getCooldown(player.getUniqueId(), CooldownType.TELEPORTATION_BOW));
            ev.setCancelled(true);
            return;
        }
        Entity launchedProjectile = ev.getProjectile();
        if(launchedProjectile.getType() != _entityType){
            launchedProjectile.remove();
            Projectile projectile = Projectile.class.cast(_entityType.getEntityClass());
            player.launchProjectile(projectile.getClass(), ev.getProjectile().getVelocity());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Packet103SetSlot packet = new Packet103SetSlot(0, 35, new ItemStack(Material.ARROW));
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
        giveItem(player);
    }



    @Override
    public void onDisable() {

    }

    private void giveItem(Player player){
        Inventory inv = player.getInventory();
        boolean found = false;
        if(player.getInventory().getItem(_slot) != null){
            for (int i = 0; i < 9; i++) {
                int slot = (_slot + i) % 9;
                ItemStack item = inv.getItem(slot);
                if(item == null || item.getType() == Material.AIR){
                    _slot = (short) slot;
                    found = true;
                    break;
                }
            }
        }else found = true;
        if(!found){
            getPlugin().getLogger().warning("Could not find an empty slot for the teleportation bow for player " + player.getName());
            return;
        }
        inv.setItem(_slot, _bow);
    }
}
