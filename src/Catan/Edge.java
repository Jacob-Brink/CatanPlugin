package Catan;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

public class Edge {
	private Node firstNode, secondNode;
	private boolean isRoad = false;
	private CatanPlayer owner;
	private Material roadMaterial;
	int X, Z, Xdelta, Zdelta;
	
	Edge(Node fNode, Node sNode) {
		firstNode = fNode;
		secondNode = sNode;
		
		firstNode.addEdge(this);
		secondNode.addEdge(this);
		
		if (firstNode.getX() < secondNode.getX()) {
			X = firstNode.getX();
		} else {
			X = secondNode.getX();
		}
		
		if (firstNode.getZ() < secondNode.getZ()) {
			Z = firstNode.getZ();
		} else {
			Z = secondNode.getZ();
		}
		
		Xdelta = Math.abs(firstNode.getX()-secondNode.getX());
		Zdelta = Math.abs(firstNode.getZ()-secondNode.getZ());
	}
	
	/* makeEdgeRoad
	 * Precondition: called by makeFirstSecondRoad or makeRoad
	 * Postcondition: isRoad is true, owner set to player
	 */
	private void makeEdgeRoad(CatanPlayer cPlayer) {
		isRoad = true;
		owner = cPlayer;
		roadMaterial = CatanPlugin.ColorToMaterial.get(owner.getColor());
		replaceBlocks(roadMaterial, Material.QUARTZ_BLOCK);
	}
	
	/* containsLocation
	 * @params: location
	 * Precondition: none
	 * Returns: if location's 2d coords are inside road boundaries
	 */
	public boolean containsLocation(Location location) {
		return ((location.getBlockX()>=X && location.getBlockX()<=X+Xdelta) && (location.getBlockZ()>=Z && location.getBlockZ()<=Z+Zdelta));
	}
	

	private boolean baseRoadRequirementsMet(CatanPlayer cPlayer) { 
		//check if road already placed
		if (isRoad) {
			cPlayer.sendError("Road already placed here");
			return false;
		};
		return true;
	}
	
	/* makeRoad
	 * @params: player
	 * Precondition: edge must be connected to other road and must not be already occupied
	 * Postcondition: makeEdgeRoad called if conditions met
	 */
	public boolean makeRoad(CatanPlayer player) {
		if (!baseRoadRequirementsMet(player)) {
			return false;
		}
		
		//check if player owns nearby road
		for (int i = 0; i < getNodes().size(); i++) {
			for (int j = 0; j < getNodes().get(i).getEdges().size(); j++) {
				if (getNodes().get(i).getEdges().get(j).isOwner(player)) {
					makeEdgeRoad(player);
					return true;
				}
			}
		}
		
		//check if road is connected to house
		boolean connectedToHouse = false;
		for (int i = 0; i < getNodes().size(); i++) {
			if (getNodes().get(i).hasStructure() && getNodes().get(i).getStructure().getOwner().equals(player)) {
				connectedToHouse = true;
				break;
			}
		}
		
		//if no house, ignore placement
		if (!connectedToHouse) {
			player.sendError("Road must be placed next to a road or a settlement of yours.");
			return false;
		}
		
		makeEdgeRoad(player);
		
		return true;
	}

	/* getOtherNode
	 * Precondition: node given is either firstNode or secondNode
	 * Returns: other node
	 */
	public Node getOtherNode(Node node) {
		if (node == firstNode) {
			return secondNode;
		} else {
			return firstNode;
		}
	}
	
	public boolean isOwner(CatanPlayer cPlayer) {
		return cPlayer == owner;
	}

	public boolean isRoad() {
		return isRoad;
	}

	public ArrayList<Node> getNodes() {
		ArrayList<Node> nodeList = new ArrayList<>();
		nodeList.add(firstNode);
		nodeList.add(secondNode);
		return nodeList;
	}
	
	private void replaceBlocks(Material materialNew, Material materialOld) {
		for (int x = X+1; x <= (X + Xdelta-1); x++) {
			for (int z = Z; z <= (Z + Zdelta); z++) {
				World world = Bukkit.getServer().getWorld("world");
				Location l = new Location(world, x, CatanPlugin.boardHeight, z);
				
				if (l.getBlock().getType() == materialOld) {
					l.getBlock().setType(materialNew);
				}
			}
		}
		
	}

	public void reset() {
		owner = null;
		isRoad = false;
		replaceBlocks(Material.QUARTZ_BLOCK, roadMaterial);
	}
	
}
