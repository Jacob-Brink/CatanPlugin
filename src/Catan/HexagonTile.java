package Catan;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Sign;

public class HexagonTile {
	private ResourceType resourceType;
	private Map<UUID, Integer> IDToCollection = new HashMap<>();
	private Map<UUID, Integer> IDToReceived = new HashMap<>();
	private int cX, cZ, indexR, indexC;
	private double hexRadius = 26;
	
	public boolean hasClaim(CatanPlayer cPlayer) {
		return IDToCollection.containsKey(cPlayer.returnPlayer().getUniqueId());
	}
	
	/* HexagonTile Constructor
	 * @params: ResourceType centerX and centerZ coordinates
	 * Postcondition: sets ResourceType
	 */
	HexagonTile(ResourceType rType, int centerX, int centerZ, int row, int column) {
		resourceType = rType;
		cX = centerX;
		cZ = centerZ;
		indexR = row;
		indexC = column;
	}
	
	/* increaseCollectingRate
	 * @params: playerID
	 * Precondition: Player bought settlement or upgraded settlement on property, IDToReceived and IDToCollection are initiated together
	 * Postcondition: Player increase rate by 1, or if new to hex, gets 1 property
	 */
	public void increaseCollectingRate(UUID playerID) {
		if (IDToCollection.containsKey(playerID)) {
			IDToCollection.put(playerID, IDToCollection.get(playerID) + 1);
		} else {
			IDToCollection.put(playerID, 1);
			IDToReceived.put(playerID, 0);
		}
	}
	
	public void debug() {
		World world = CatanPlugin.catanWorld;
		Location l = new Location(world, cX, 184, cZ);
		l.getBlock().setType(Material.GOLD_BLOCK);
		
	}
	
	public void debugChange() {
		World world = CatanPlugin.catanWorld;
		Location l = new Location(world, cX, 184, cZ);
		l.getBlock().setType(Material.COAL_BLOCK);
	}
	
	/* getResources
	 * @params:playerID
	 * Precondition: player right clicks chest in hex tile 
	 * Postcondition: player's uncollected store becomes 0
	 * Returns: player's collected resource count
	 */
	public int getResources(UUID playerID) {
		if (IDToCollection.containsKey(playerID)) {
			int resources = IDToReceived.get(playerID);
			IDToReceived.put(playerID, 0);
			return resources;
		} else {
			return 0;
		}
	}
	
	/* getResourceType
	 */
	public ResourceType getResourceType() {
		return resourceType;
	}
	
	/* produceResource
	 * precondition: called by game manager only
	 * postcondition: everyone receives one more item to be received 
	 */
	public void produceResource() {
		for(HashMap.Entry<UUID, Integer> entry : IDToReceived.entrySet()) {
			IDToReceived.put(entry.getKey(), IDToCollection.get(entry.getKey()));
			CatanPlugin.spawnFirework(cX, cZ, 10, CatanPlugin.IDToPlayer.get(entry.getKey()).getColor());
		}
	}
	
	public int getX() {
		return cX;
	}
	
	public int getZ() {
		return cZ;
	}
	
	/* equalsHex
	 * Returns: whether hex is equal to other hex
	 */
	public boolean equalsHex(HexagonTile hex) {
		return ((hex.getX() == cX) && (hex.getZ() == cZ));
	}
	
	/* collidesWith
	 * @params: x and z coordinate
	 * Precondition: none
	 * Returns: true if position is within 25-26 blocks of hexagon center
	 */
	public boolean collidesWith(int lX, int lZ) {
		int dX = lX - cX;
		int dZ = lZ - cZ;
		double distance = Math.sqrt(dX*dX+dZ*dZ);
		return distance < hexRadius;
	}
}
