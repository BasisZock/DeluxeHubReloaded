package dev.strafbefehl.deluxehubreloaded.module.modules.hologram;

import dev.strafbefehl.deluxehubreloaded.utility.PlaceholderUtil;
import dev.strafbefehl.deluxehubreloaded.utility.TextUtil;
import dev.strafbefehl.deluxehubreloaded.utility.reflection.ArmorStandName;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Hologram {

	private final List<ArmorStand> stands;
	private Location location;
	private final String name;

	public Hologram(String name, Location location) {
		this.name = name;
		this.location = location;
		stands = new ArrayList<>();
	}

	public void setLines(List<String> lines) {
		remove();
		for (String s : lines) addLine(s);
	}
/* not used
	public Hologram addLines(List<String> lines) {
		for (String s : lines) addLine(s);
		return this;
	}
*/
	public void addLine(String text) {
		ArmorStand stand = (ArmorStand) location.getWorld().spawnEntity(location.clone().subtract(0, getHeight(), 0), EntityType.ARMOR_STAND);
		stand.setVisible(false);
		stand.setGravity(false);
		stand.setCustomNameVisible(true);
		stand.setCustomName(TextUtil.color(PlaceholderUtil.setPlaceholders(text, null)));
		stand.setCanPickupItems(false);
		stands.add(stand);
	}

	public void setLine(int line, String text) {
		ArmorStand stand = stands.get(line - 1);
		stand.setCustomName(TextUtil.color(PlaceholderUtil.setPlaceholders(text, null)));
	}

	public Hologram removeLine(int line) {
		ArmorStand stand = stands.get(line - 1);
		stand.remove();

		stands.remove(line - 1);

		if (!refreshLines(line - 1)) return null;
		return this;
	}

	public boolean refreshLines(int line) {
		List<ArmorStand> standsTemp = new ArrayList<>();

		int count = 0;
		for (ArmorStand entry : stands) {
			if (count >= line) standsTemp.add(entry);
			count++;
		}

		for (ArmorStand stand : standsTemp) stand.teleport(stand.getLocation().add(0, 0.25, 0));

		return count >= 1;
	}

	public boolean hasLine(int line) {
		return line - 1 >= stands.size() || line <= 0;
	}

	public void remove() {
        for (ArmorStand stand : stands) {
            stand.remove();
        }
		stands.clear();
	}

	public Location getLocation() {
		return location;
	}

	public Hologram setLocation(Location location) {
		this.location = location;
		setLines(stands.stream().map(ArmorStandName::getName).collect(Collectors.toList()));
		return this;
	}

	public List<ArmorStand> getStands() {
		return stands;
	}

	public String getName() {
		return name;
	}

	private double getHeight() {
		return stands.size() * 0.25;
	}


}
