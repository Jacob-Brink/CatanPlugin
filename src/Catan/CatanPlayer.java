package Catan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

public class CatanPlayer {
	private int score = 0;
	private int roads = 0;
	private int structures = 0;
	private HashMap<ResourceType, Integer> resourceToAmount = new HashMap<>();
	private HashMap<ResourceType, Material> resourceToMaterial = new HashMap<>();
	private Color color;
	private Player player;
	private GameBoard gb;
	private Location spawnLocation = CatanPlugin.gameSpawn;
	private ScoreManager.ScoreboardHook scoreHook;
	
	public ItemStack setCustomName(ItemStack item, String name) {
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        item.setItemMeta(meta);
        return item;
    }
	
	public Color getColor() {
		return color;
	}
	
	public boolean isDoneFirstPart() {
		return structures >= 2;// && roads >= 2;
	}
	
	public void giveItem(Material itemMaterial, String name, int slot) {
    	ItemStack itemStack = new ItemStack(itemMaterial, 1);
    	player.getInventory().setItem(slot, setCustomName(itemStack, name));
    }
	
	/* resetInventory
	 * Precondition: none
	 * Postcondition: resourceToAmount is set to 0, equips player armor and banners
	 */
	private void resetInventory() {
		//Reset ResourceCount
		for (Map.Entry<ResourceType, Material> entry : resourceToMaterial.entrySet()) {
			resourceToAmount.put(entry.getKey(), 0);
			scoreHook.setResourceScore(entry.getKey(), 0);
		}
		
		//clear inventory
		player.getInventory().clear();
		
		//Equip Armour
		player.getInventory().setChestplate(getArmor(Material.LEATHER_CHESTPLATE));
		player.getInventory().setBoots(getArmor(Material.LEATHER_BOOTS));
		

		giveItem(Material.IRON_SWORD, "Negotiator", 0);
		
		//Equip Settlement, Road, and City Banners
		giveItem(Material.RED_BANNER, "Settlement", 6);
		giveItem(Material.WHITE_BANNER, "City", 7);
		giveItem(Material.BLUE_BANNER, "Road", 8);
	}
	
	/* getArmor
	 * Precondition: material must be leather armor, otherwise it will die :(
	 * Returns: itemstack of armor in the color of the player
	 */
	private ItemStack getArmor(Material armorType) {
		ItemStack armor = new ItemStack(armorType, 1);
		LeatherArmorMeta armorMeta = (LeatherArmorMeta)armor.getItemMeta();
		armorMeta.setColor(color);
		armor.setItemMeta(armorMeta);
        return armor;
    }
 
	public void setGameBoard(GameBoard g) {
		gb = g;
	}
	
	CatanPlayer(Player p, Color c) {
		resourceToMaterial.put(ResourceType.stone, Material.STONE);
		resourceToMaterial.put(ResourceType.wood, Material.OAK_LOG);
		resourceToMaterial.put(ResourceType.wool, Material.WHITE_WOOL);
		resourceToMaterial.put(ResourceType.wheat, Material.WHEAT);
		resourceToMaterial.put(ResourceType.brick, Material.BRICK);
		
		player = p;
		player.setGameMode(GameMode.SURVIVAL);
		color = c;
	}
	
	public void startGame() {
		resetInventory();
	}
	
	public void changeResourceCount(ResourceType rType, int change) {
		resourceToAmount.put(rType, returnResourceCount(rType)+change);
		changeDisplay(rType);
	}
	
	public void registerScoreboardListener(ScoreManager.ScoreboardHook scoreboardHook) {
		scoreHook = scoreboardHook;
	}
	
	/* changeInventory
	 * precondition: called after any change in resource amount
	 */
	public void changeDisplay(ResourceType rType) {
		scoreHook.setResourceScore(rType, returnResourceCount(rType));
	}
	
	/* gatherResource
	 * @param: ResourceType, amount
	 * Precondition: called by GameManager when player right clicks chest, resourceToAmount already initialized
	 * Postcondition: resource is added
	 */
	public void gatherResource(ResourceType rType, int amount) {
		changeResourceCount(rType, amount);
	}
	
	/* return resource count
	 * @param: resourceType
	 * Precondition: resourceToAmount HashMap is initialized
	 * Returns: amount of inquired resources
	 */
	public int returnResourceCount(ResourceType resourceType) {
		if (resourceToAmount.containsKey(resourceType)) {
			return resourceToAmount.get(resourceType);
		} else {
			return 0;
		}
		
	}
	
	/* spendResources
	 * @params: resource type, amount
	 * Precondition: amount is positive
	 * Postcondition: player loses amount of resources
	 */
	public boolean spendResources(ResourceType rType, int amount) {
		if (!resourceToAmount.containsKey(rType)) {
			return false;
		}
		
		int resourceCount = resourceToAmount.get(rType);
		
		if (amount <= 0) {
			return false;
		}
		
		if (amount <= resourceCount) {
			changeResourceCount(rType, -amount);
			return true;
		} else {
			return false;
		}
	}
	
	/* ReSpawn
	 * Precondition: called on death event of player
	 * Postcondition: player gets armor and sword
	 */
	public void ReSpawn() {
		//TODO: teleport player back to random settlement of theirs
		sendSuccess("Teleporting...");
		
		if (spawnLocation != null) {
			player.teleport(spawnLocation);
		} else {
			Location l = new Location(CatanPlugin.catanWorld, -82, 192, -642);
			player.teleport(l);
		}
		resetInventory();
		
	}
	
	public Player returnPlayer() {
		return player;
	}
	
	public UUID getUUID() {
		return player.getUniqueId();
	}
	
	//TODO add chat colors
	public void sendError(String message) {
		player.sendMessage(CatanPlugin.CatanPrefix  + ChatColor.DARK_RED + message);
	}

	public void sendSuccess(String message) {
		// TODO Auto-generated method stub
		player.sendMessage(CatanPlugin.CatanPrefix + ChatColor.RESET + ChatColor.GOLD + message);
	}

	public void stealResources(CatanPlayer deadPlayer) {
		for (Map.Entry<ResourceType, Integer> entry : resourceToAmount.entrySet()) {
			sendSuccess("Amount: " + resourceToAmount.get(entry.getKey()));
			gatherResource(entry.getKey(), deadPlayer.returnResourceCount(entry.getKey()));
		}
	}

	public int getScore() {
		return score;
	}
	
	public void removeFromGame() {		
		//Teleport to hub
		sendSuccess("Teleporting to game lobby...");
		player.setGameMode(GameMode.ADVENTURE);
		player.teleport(CatanPlugin.gameLobby);
		scoreHook.remove();
		
	}
	
	private boolean canBuy(Structures structure) {
		HashMap<ResourceType, Integer> resourceToCost = CatanPlugin.structureToPrice.get(structure);
		//check if player has resources
		for(Map.Entry<ResourceType, Integer> entry : resourceToCost.entrySet()) {
			ResourceType rType = (ResourceType) entry.getKey();
			int cost = (int) entry.getValue();
			
			if (returnResourceCount(rType) < cost) {
				return false;
			}
		}
		return true;
	}
	
	private boolean buy(Structures structure) {
		if (canBuy(structure)) {
			HashMap<ResourceType, Integer> resourceToCost = CatanPlugin.structureToPrice.get(structure);
			Iterator it = resourceToCost.entrySet().iterator();
			//check if player has resources
			while(it.hasNext()) {
				Map.Entry pair = (Map.Entry) it.next();
				ResourceType rType = (ResourceType) pair.getKey();
				int cost = (int) pair.getValue();
				spendResources(rType, cost);
			}
			return true;
		}
		return false;
	}
	
	public boolean placeSettlement(Location location) {
		if (structures < 2) {
			if (gb.placeSettlement(location, this)) {
				structures++;
				incrementScore();
				if (spawnLocation == null) {
					spawnLocation = new Location(Bukkit.getWorld("world"), location.getX(), location.getY()+2, location.getZ());
				}
				sendSuccess("Settlement created!");
				return true;
			}
		} else {
			if (canBuy(Structures.SETTLEMENT)) {
				if (gb.placeSettlement(location, this)) {
					structures++;
					incrementScore();
					sendSuccess("Settlement purchased and created!");
					buy(Structures.SETTLEMENT);
					return true;
				}
			} else {
				showMissingResources(Structures.SETTLEMENT);
			}
		}
		return false;
	}

	private void showMissingResources(Structures structure) {
		HashMap<ResourceType, Integer> resourceToCost = CatanPlugin.structureToPrice.get(structure);
		//list all items player is deficient in
		for(Map.Entry<ResourceType, Integer> entryDetails : resourceToCost.entrySet()) {
			int resourceCount = returnResourceCount((ResourceType) entryDetails.getKey());
			if(resourceCount < (int) entryDetails.getValue()) {
				sendError("Missing " + (entryDetails.getValue()-resourceCount) + " " + entryDetails.getKey() );
			}
			
		}
	}

	public void placeRoad(Location location) {
		if (roads < 2) {
			if (gb.placeRoad(location, this)) {
				roads++;
				sendSuccess("Road created!");
				return;
			}
		} else {
			if (canBuy(Structures.ROAD)) {
				if (gb.placeRoad(location, this)) {
					roads++;
					buy(Structures.ROAD);
					sendSuccess("Road purchased!");
					return;
				}
			} else {
				showMissingResources(Structures.ROAD);
			}
		}
		return;
	}

	public void incrementScore() {
		score++;
		scoreHook.updateScore(score);
	}
	
	public boolean upgradeSettlement(Location location) {
		if (canBuy(Structures.CITY)) {
			if (gb.upgradeSettlement(location, this)) {
				incrementScore();
				buy(Structures.CITY);
				sendSuccess("Settlement upgraded!");
				return true;
			}
		} else {
			showMissingResources(Structures.CITY);
		}
		
		return false;
	}

	public void openChest(Location location) {
		HexagonTile hex = gb.returnHexByLocation(location);
		if (hex != null) {
			
			if (hex.hasClaim(this)) {
				
				int amount = hex.getResources(player.getUniqueId());
				gatherResource(hex.getResourceType(), amount);
				
			} else {
				sendError("You need a settlement or city placed next to this hexagon before you can collect resources...");
			}
			
		} else {
			sendError("You found a chest that shouldn't be where it is. Please report this in the VertX chat.");
		}
		return;
	}
	
	
}
