package Catan;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

public class Node {
	private ArrayList<HexagonTile> adjacentTiles = new ArrayList<>();
	private ArrayList<Edge> connectedEdges = new ArrayList<>();
	private int coordX, coordZ;
	private Structure structure = null;
	
	public void addEdge(Edge edge) {
		connectedEdges.add(edge);
	}
	
	public ArrayList<Edge> getEdges() {
		return connectedEdges;
	}
	
	public ArrayList<Node> getNeighborNodes() {
		ArrayList<Node> nodeList = new ArrayList<>();
		for (int i = 0; i < connectedEdges.size(); i++) {
			Edge edge = connectedEdges.get(i);
			nodeList.add(edge.getOtherNode(this));
		}
		return nodeList;
	}
	
	Node(int cX, int cZ, ArrayList<HexagonTile> aTiles) {
		adjacentTiles = aTiles;
		coordX = cX;
		coordZ = cZ;
	}
	
	public int getX() {
		return coordX;
	}
	
	public int getZ() {
		return coordZ;
	}
	
	public ArrayList<HexagonTile> returnAdjacentTiles() {
		return adjacentTiles;
	}
	
	public void increaseHexCollectingRate(UUID pID) {
		for (int i = 0; i < adjacentTiles.size(); ++i) {
			adjacentTiles.get(i).increaseCollectingRate(pID);
		}
	}
	
	private boolean ownsNearbyRoad(CatanPlayer player) {
		for (int i = 0; i < connectedEdges.size(); i++) {
			if (connectedEdges.get(i).isOwner(player)) {
				return true;
			}
		}
		return false;
	}
	
	private boolean hasNeighboringStructures(CatanPlayer player) {
		for (int i = 0; i < connectedEdges.size(); i++) {
			if (connectedEdges.get(i).getOtherNode(this).hasStructure()) {
				return true;
			}
		}
		return false;
	}
	
	public boolean placeSettlement(CatanPlayer player, boolean firstPlacement) {
		//always check if structure already exists
		if (hasStructure()) {
			player.sendError("Cannot place settlement on another settlement");
			return false;
		}
		
		//if not first placement, check if connectedEdges has a road owned by player
		if (!firstPlacement && !ownsNearbyRoad(player)) {
			player.sendError("Settlement must be placed by a road of yours");
			return false;
		}
		
		//check if structures within 1 road distance exist
		if (hasNeighboringStructures(player)) {
			player.sendError("You cannot place a settlement closer than two roads away from another structure");
			return false;
		}
		Location l = new Location(CatanPlugin.catanWorld, coordX, CatanPlugin.boardHeight, coordZ);
		structure = new Structure(player, l);
		increaseHexCollectingRate(player.getUUID());
		return true;
	}
	
	public boolean upgradeSettlement(CatanPlayer player) {
		if (structure != null) {
			if (structure.upgrade(player)) {
				increaseHexCollectingRate(player.getUUID());
				return true;
			}
		}
		player.sendError("You can't upgrade nothing!");
		return false;
	}
	
	public boolean hasStructure() {
		return structure != null;
	}

	public Structure getStructure() {
		return structure;
	}
	
	public void debug() {
		World world = CatanPlugin.catanWorld;
		Location l = new Location(world, coordX, 184, coordZ);
		l.getBlock().setType(Material.DIAMOND_BLOCK);
		
	}

	public void reset() {
		// TODO Auto-generated method stub
		if (structure != null) {
			structure.cleanup();
			structure = null;
		}
	}

	public boolean isOwner(CatanPlayer player) {
		if (structure != null) {
			return player == structure.getOwner();
		} else {
			return false;
		}
		
	}
	
}
