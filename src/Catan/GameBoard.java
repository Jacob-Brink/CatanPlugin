package Catan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.block.BlockPlaceEvent;

public class GameBoard {

	private ArrayList<ArrayList<HexagonTile>> hexagons = new ArrayList<>();
	private ArrayList<ResourceType[]> resourceTypeMap = new ArrayList<>();
	private ArrayList<ArrayList<Node>> nodeMap = new ArrayList<>();
	private ArrayList<ArrayList<Edge>> edgeMap = new ArrayList<>();
	private HashMap<Location, Node> locationToNode = new HashMap<>();
	private int startX, startZ, rowJump, columnJump, offsetZ;
	
	public void reset() {
		//Turn banners into air
		for(HashMap.Entry<Location, Node> entry : locationToNode.entrySet()) {
			entry.getValue().reset();
		}
		//Turn roads back into blank edges
		for (int i = 0; i < edgeMap.size(); i++) {
			for (int j = 0; j < edgeMap.get(i).size(); j++) {
				edgeMap.get(i).get(j).reset();
			}
		}
	}
	
	/* returnHexes
	 * @params: Node row, Node column
	 * Precondition: hexagons exists
	 * Returns: all hexes sharing that point
	 */
	private ArrayList<HexagonTile> returnHexes(int row, int column) {

		ArrayList<HexagonTile> hexs = new ArrayList<>();
		//first half
		if (row < 3) {
			//if even
			if (column % 2 == 0) {
				//end exceptions
				if (column == 0) {
					hexs.add(hexagons.get(row).get(column));
					return hexs;
				} else if (column == 2*hexagons.get(row).size()) {
					hexs.add(hexagons.get(row).get(column/2-1));
					return hexs;
				}
				//middle nodes
				hexs.add(hexagons.get(row).get(column/2));
				hexs.add(hexagons.get(row).get(column/2-1));
				
				//add hex above if not first row
				if (row != 0) {
					hexs.add(hexagons.get(row-1).get(column/2-1));
				}
				
			} else {
				hexs.add(hexagons.get(row).get(column/2));
				//add hexs above if not first row
				if (row != 0) {
					if (column == 1) {
						//if first odd, then get first hex above
						hexs.add(hexagons.get(row-1).get(0));
					} else if (column == 2*hexagons.get(row).size()-1) {
						//if last odd, then get last hex above
						hexs.add(hexagons.get(row-1).get(column/2-1));
					} else {
						//if normal hex
						hexs.add(hexagons.get(row-1).get(column/2));
						hexs.add(hexagons.get(row-1).get(column/2-1));
					}
				}
			}
		//second half
		} else {
			//if even
			if (column % 2 == 0) {
				//end exceptions
				if (column == 0) {
					hexs.add(hexagons.get(row-1).get(0));
					return hexs;
				} else if (column == 2*hexagons.get(row-1).size()) {
					hexs.add(hexagons.get(row-1).get(column/2-1));
					return hexs;
				}
				
				//middle nodes
				hexs.add(hexagons.get(row-1).get(column/2));
				hexs.add(hexagons.get(row-1).get(column/2-1));
				
				//add hex above
				if (row != 5) {
					hexs.add(hexagons.get(row).get(column/2-1));
				}
				
			} else {
				hexs.add(hexagons.get(row-1).get(column/2));
				//check hexes above
				if (row != 5) {
					if (column == 1) {
						//if first odd, then get first hex above
						hexs.add(hexagons.get(row).get(0));
						return hexs;
					} else if (column == 2*hexagons.get(row-1).size()-1) {
						//if last odd, then get last hex above
						hexs.add(hexagons.get(row).get(column/2-1));
						return hexs;
					}
					hexs.add(hexagons.get(row).get(column/2-1));
					hexs.add(hexagons.get(row).get(column/2));
				} 
			}
		}
		return hexs;
	}
	
	/* setEdges
	 * Precondition: nodeMap setup and hexagons setup
	 * Postcondition: edgeMap contains all edges with all nodes
	 */
	private void setEdges() {
		//first half (rows increase by 2 nodes)
		for (int i = 0; i < 3; ++i) {
			ArrayList<Edge> rowEdges = new ArrayList<>();
			//horizontal ^^^^^^^^
			for (int j = 0; j < 6+i*2; ++j) {
				Node leftNode = nodeMap.get(i).get(j);
				Node rightNode = nodeMap.get(i).get(j+1);
				Edge horizontalEdge = new Edge(leftNode, rightNode);
				rowEdges.add(horizontalEdge);
				//vertical lines ||||
				if (j % 2 == 0 ) {
					Node downNode;
					if (i != 2) {
						downNode = nodeMap.get(i+1).get(j+1);
					} else {
						//when i is 2, the node down from the leftNode is at the same spot (look at visuals for help)
						downNode = nodeMap.get(i+1).get(j);
					}
					Edge verticalEdge = new Edge(downNode, leftNode);
					 
					rowEdges.add(verticalEdge);
				//last node case
				} else if (j == 5+i*2) {
					Node downNode;
					if (i != 2) {
						downNode = nodeMap.get(i+1).get(j+2);
					} else {
						//when i is 2, the node down from the leftNode is at the same spot (look at visuals for help)
						downNode = nodeMap.get(i+1).get(j+1);
					}

					Edge verticalEdge = new Edge(downNode, rightNode);
					rowEdges.add(verticalEdge);
				}

				
			}
			edgeMap.add(i, rowEdges);
		}
		//second half (rows decrease by 2 nodes)
		for (int i = 3; i < 6; ++i) {
			ArrayList<Edge> rowEdges = new ArrayList<>();
			for (int j = 0; j < 10-(i-3)*2; ++j) {
				Node leftNode = nodeMap.get(i).get(j);
				Node rightNode = nodeMap.get(i).get(j+1);
				Edge horizontalEdge = new Edge(leftNode, rightNode);
				rowEdges.add(horizontalEdge);
				//vertical lines ||||
				if ((j % 2 != 0) && (i != 5)) {
					Node downNode = nodeMap.get(i+1).get(j-1);
					Edge verticalEdge = new Edge(downNode, leftNode);
					rowEdges.add(verticalEdge);
				}
			}
			edgeMap.add(i, rowEdges);
		}
	}
	
	/* setupHexagons 
	 * sets up hexagons according to settlers of catan beginners guide
	 */
	private void setupHexagons() {
		//1st layer
		ResourceType[] firstRow = {ResourceType.wood, ResourceType.wool, ResourceType.wheat};
		resourceTypeMap.add(firstRow);
		
		//2nd layer
		ResourceType[] secondRow = {ResourceType.brick, ResourceType.stone, ResourceType.brick, ResourceType.wool};
		resourceTypeMap.add(secondRow);
		
		//3rd (middle) layer
		ResourceType[] thirdRow = {null, ResourceType.wood, ResourceType.wheat, ResourceType.wood, ResourceType.wheat};
		resourceTypeMap.add(thirdRow);
		
		//4th row
		ResourceType[] fourthRow = {ResourceType.brick, ResourceType.wool, ResourceType.wool, ResourceType.stone};
		resourceTypeMap.add(fourthRow);
		
		//5th row
		ResourceType[] fifthRow = {ResourceType.stone, ResourceType.wheat, ResourceType.wood};
		resourceTypeMap.add(fifthRow);
		
		//setup z position of first hex's center on each row
		ArrayList<Integer> rowStartZ = new ArrayList<>();
		rowStartZ.add(startZ);
		rowStartZ.add(rowStartZ.get(0) + offsetZ);
		rowStartZ.add(rowStartZ.get(1)+offsetZ);
		rowStartZ.add(rowStartZ.get(1));
		rowStartZ.add(rowStartZ.get(0));
		
		//setup hexagon tiles
		for (int row = 0; row <= 4; row++) {
			int rowZStart = rowStartZ.get(row);
			ArrayList<HexagonTile> rTileArray = new ArrayList<>();
			for (int col = 0; col < resourceTypeMap.get(row).length; col++) {
				ResourceType resource = resourceTypeMap.get(row)[col];
				//subtract because going to the right, z decreases
				int cZ = rowZStart - col * columnJump;
				//add because going down the map, x increases
				int cX = startX + row * rowJump;
				rTileArray.add(col, new HexagonTile(resource, cX, cZ, row, col));
			}
			hexagons.add(row, rTileArray);
		}
		
		int startNodeX = -203;
		int startNodeZ = -558;
		int localXJump = -18;
		int localZJump = -30;
		int rowXJump = 51;
		
		//setup node map with real world coords ( sorry for the mess XD)
		//first half
		int rowX = 0;
		int rowZ = 0;
		for (int i = 0; i < 3; ++i) {
			ArrayList<Node> rowNodeList = new ArrayList<>();
			for (int j = 0; j < 7+i*2; ++j) {
				int posX, posZ;
				rowX = startNodeX + i * rowXJump;
				rowZ = startNodeZ - i * localZJump;
				//handle up and down of hexagon tiling
				if (j % 2 == 0) {
					posX = rowX;
				} else {
					posX = rowX+localXJump;
				}
				posZ = rowZ + j * localZJump;
				//get all connected Hexes
				Node newNode = new Node(posX, posZ, returnHexes(i, j));
				rowNodeList.add(j, newNode);
				locationToNode.put(returnLocation(posX, posZ), newNode);
			}
			nodeMap.add(i, rowNodeList);
		}
		//shift start nodes down
		startNodeX = rowX+33;
		startNodeZ = rowZ;
				
		//second half
		for (int i = 0; i < 3; ++i) {
			ArrayList<Node> rowNodeList = new ArrayList<>();
			//start at 11 per each row then go down 2 per row
			for (int j = 0; j < 11-i*2; ++j) {
				int posX, posZ;
				rowX = startNodeX + i * rowXJump;
				rowZ = startNodeZ + i * localZJump;
				//handle up and down of hexagon tiling
				if (j % 2 == 0) {
					posX = rowX;
				} else {
					posX = rowX-localXJump;
				}
				posZ = rowZ+j*localZJump;
				Node newNode = new Node(posX, posZ, returnHexes(i+3, j));
				rowNodeList.add(j, newNode);
				locationToNode.put(returnLocation(posX, posZ), newNode);
				
			}
			nodeMap.add(i+3, rowNodeList);
		}
		
	}
	
	private Location returnLocation(int x, int z) {
		return new Location(Bukkit.getServer().getWorld("world"), x, 185, z);
	}

	GameBoard() {
		//center of first hex
		startX = -187;
		startZ = -588;
		
		//distance between hex centers east-west
		rowJump = 51;
		
		//distance from hex center to flat side
		offsetZ = 30;
		
		//distance between hex centers north-south
		columnJump = 60;
		setupHexagons();
		setEdges();
	}
	
	/* returnHexByLocation
	 * Precondition: none
	 * Postcondition: none
	 * returns: hex if collides with location, else null
	 */
	public HexagonTile returnHexByLocation(Location l) {
		int lX = l.getBlockX();
		int lZ = l.getBlockZ();
		for (int row = 0; row <= 4; row++) {
			for (int col = 0; col < hexagons.get(row).size(); col++) {
				if (hexagons.get(row).get(col).collidesWith(lX, lZ)) {
					return hexagons.get(row).get(col);
				}
			}
		}
		return null;
	}
	
	/* returnHexByNum
	 * Precondition: num is between 1 and 19
	 * Returns:HexTile
	 */
	public HexagonTile returnHexByNum(int num) {
		if (num < 3) {
			return hexagons.get(0).get(num);
		} else if (num < 7) {
			return hexagons.get(1).get(num-3);
		} else if (num < 12) {
			return hexagons.get(2).get(num-7);
		} else if (num < 16) {
			return hexagons.get(3).get(num-12);
		} else {
			return hexagons.get(4).get(num-16);
		}
	}
	
	/* getRandomTileNum
	 * 
	 */
	public int getRandomTileNum() {
		Random r = new Random();
		return r.nextInt(19);
	}
	
	public Node getNodeByLocation(Location l) {
		return locationToNode.get(l);
	}

	public boolean placeRoad(Location location, CatanPlayer cPlayer) {
		World world = Bukkit.getServer().getWorld("world");
		Location l = new Location(world, location.getBlockX(), location.getBlockY()-1, location.getBlockZ());
	
		if (l.getBlock().getType() == Material.QUARTZ_BLOCK && !locationToNode.containsKey(location)) {
			Edge edge = getEdgeByLocation(location);
			if (edge != null) {
				if (edge.makeRoad(cPlayer)) {
					return true;
				}
			} 
			
		} else {
			cPlayer.sendError("You must place a road on quartz blocks");
		}
		
		return false;
	}

	private Edge getEdgeByLocation(Location location) {
		for (int i = 0; i < edgeMap.size(); i++) {
			for (int j = 0; j < edgeMap.get(i).size(); j++) {
				if (edgeMap.get(i).get(j).containsLocation(location)) {
					return edgeMap.get(i).get(j);
				}
			}
		}
		return null;
	}

	public boolean upgradeSettlement(Location location, CatanPlayer cPlayer) {
		Node node = getNodeByLocation(location);
		if (node != null) {
			if (node.upgradeSettlement(cPlayer)) {
				return true;
			}
			return false;
		}
		cPlayer.sendError("Place the city banner on a settlement");
		return false;
	}

	public boolean placeSettlement(Location location, CatanPlayer cPlayer) {
		Node node = getNodeByLocation(location);
		if (node != null) {
			if (!node.placeSettlement(cPlayer, !cPlayer.isDoneFirstPart())) {
				return false;
			} 
			return true;
		} else {
			cPlayer.sendError("Place a structure banner on an intersection block.");
			return false;
		}
	}
	
	public void removePlayer(CatanPlayer player) {
		
		//remove structures from nodes
		for (int i = 0; i < nodeMap.size(); i++) {
			for (int j = 0; j < nodeMap.get(i).size(); j++) {
				if (nodeMap.get(i).get(j).isOwner(player)) {
					nodeMap.get(i).get(j).reset();
				}
			}
		}
		
		//remove roads from edges
		for (int i = 0; i < edgeMap.size(); i++) {
			for (int j = 0; j < edgeMap.get(i).size(); j++) {
				if (edgeMap.get(i).get(j).isOwner(player)) {
					edgeMap.get(i).get(j).reset();
				}
			}
		}
		
	}


}
