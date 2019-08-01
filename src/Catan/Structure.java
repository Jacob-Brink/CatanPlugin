package Catan;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;

public class Structure {
	private CatanPlayer owner;
	private boolean isCity = false;
	private Location glassLocation;
	
	
	Structure(CatanPlayer o, Location l) {
		owner = o;
		glassLocation = l;
		placeSettlementBlocks();
	}
	
	public CatanPlayer getOwner() {
		return owner;
	}
	
	/* upgrade
	 * Precondition: only to be called once
	 * Postcondition: isCity, if not already true becomes true and blocks are placed
	 */
	public boolean upgrade(CatanPlayer p) {
		if (isCity) {
			p.sendError("Cannot upgrade city...");
			return false;
		}
		if (p != owner) {
			p.sendError("Cannot upgrade someone else's settlement!");
			return false;
		}
		isCity = true;
		placeCityBlocks();
		return true;
	}
	
	public void cleanup() {
		glassLocation.getBlock().setType(Material.QUARTZ_BLOCK);
	}
	
	
	//TODO
	public void placeSettlementBlocks() {
		glassLocation.getBlock().setType(CatanPlugin.ColorToGlass.get(owner.getColor()));
	}
	
	//TODO
	public void placeCityBlocks() {
		glassLocation.getBlock().setType(CatanPlugin.ColorToCityGlass.get(owner.getColor()));
	}
}
