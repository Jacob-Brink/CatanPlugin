package Catan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;


public class CatanPlugin extends JavaPlugin {
	public static HashMap<UUID, CatanPlayer> IDToPlayer = new HashMap<>();
	public static ArrayList<UUID> QuitterID = new ArrayList<>();
	public static HashMap<Color, Material> ColorToMaterial = new HashMap<>();
	public static HashMap<Color, Material> ColorToGlass = new HashMap<>();
	public static HashMap<Color, Material> ColorToCityGlass = new HashMap<>();
	public static int boardHeight = 184;
	public static boolean gameInProgress = false;
	public static Queue<UUID> waitingPlayers = new LinkedList<>();
	public static GameManager gm = null;
	public static HashMap<Structures, HashMap<ResourceType, Integer>> structureToPrice = new HashMap<>();
	public static String CatanPrefix = "" + ChatColor.AQUA + ChatColor.BOLD + "CATAN: ";
	public static Location gameLobby, gameSpawn, hubSpawn, waitingRoom;
	public static World catanWorld, spawnWorld;
	private static CatanPlugin instance;
	private static int minPlayers = 2; //for debugging purposes only
	
	private static void setupLocations() {
		catanWorld = Bukkit.createWorld(new WorldCreator("catan"));
		spawnWorld = Bukkit.createWorld(new WorldCreator("world"));
		gameLobby = new Location(catanWorld, -84, 239, -647);
		gameSpawn = new Location(catanWorld, -83, 192, -643);
		hubSpawn = new Location(spawnWorld, 8, 20, 8);
		waitingRoom = new Location(catanWorld, -159, 160, -344);
	}
	
	public static class gameHook {
		public void endGameEvent() {
			handleGameChangeEvent();
		}
	}
	
	private static void setPrices() {
		HashMap<ResourceType, Integer> roadPrices = new HashMap<>();
		roadPrices.put(ResourceType.wood, 1);
		roadPrices.put(ResourceType.brick, 1);
		structureToPrice.put(Structures.ROAD, roadPrices);
		
		HashMap<ResourceType, Integer> cityPrices = new HashMap<>();
		cityPrices.put(ResourceType.stone, 3);
		cityPrices.put(ResourceType.wheat, 2);
		structureToPrice.put(Structures.CITY, cityPrices);
		
		HashMap<ResourceType, Integer> settlementPrices = new HashMap<>();
		settlementPrices.put(ResourceType.wood, 1);
		settlementPrices.put(ResourceType.brick, 1);
		settlementPrices.put(ResourceType.wheat, 1);
		settlementPrices.put(ResourceType.wool, 1);
		structureToPrice.put(Structures.SETTLEMENT, settlementPrices);
	}
	
	private static void setupNewGame() {
		ArrayList<Player> players = new ArrayList<>();
		for(int i = 0; i < minPlayers; i++) {
			Player p = Bukkit.getPlayer(waitingPlayers.remove());
			players.add(i, p);
		}
		gm = new GameManager(players, new gameHook());
	}
	
	public static boolean handleGameChangeEvent() {
		if (gm != null) {
			if (gm.inProgress()) {
				return false;
			} else if (waitingPlayers.size() >= minPlayers) {
				setupNewGame();
			}
		} else {
			if (waitingPlayers.size() >= minPlayers) {
				setupNewGame();
			}
		}
		return true;
	}
	
	public static void addPlayerToQueue(Player player) {
		if (waitingPlayers.contains(player.getUniqueId())) {
			player.sendMessage("You are already in the queue");
			return;
		}
		waitingPlayers.add(player.getUniqueId());
		player.sendMessage("Marker");
		if (waitingRoom == null) {
			player.sendMessage("waitingRoom is null");
		}
		player.teleport(waitingRoom);
		
		
		Bukkit.getScheduler().runTaskLater(CatanPlugin.returnInstance(), new Runnable() {
			public void run() {
				player.setGameMode(GameMode.ADVENTURE);
				player.getInventory().clear();
			}
		}, 1);
		
		
		player.sendMessage(CatanPrefix + ChatColor.WHITE+"Queue Position: " + ChatColor.WHITE + waitingPlayers.size() + "/" + ChatColor.YELLOW + minPlayers);
		player.sendMessage(CatanPrefix + ChatColor.WHITE + "Type /catan leave to leave the queue");
		
		if(!handleGameChangeEvent()) {
			player.sendMessage(CatanPrefix + ChatColor.WHITE + "Game in progress...");
		}
	}
	
	private void setupColorMaterial() {
		ColorToMaterial.put(Color.RED, Material.RED_WOOL);
		ColorToMaterial.put(Color.BLUE, Material.BLUE_WOOL);
		ColorToMaterial.put(Color.YELLOW, Material.YELLOW_WOOL);
		ColorToMaterial.put(Color.LIME, Material.LIME_WOOL);
	}
	
	private void setupColorGlass() {
		ColorToGlass.put(Color.RED, Material.RED_STAINED_GLASS);
		ColorToGlass.put(Color.BLUE, Material.BLUE_STAINED_GLASS);
		ColorToGlass.put(Color.YELLOW, Material.YELLOW_STAINED_GLASS);
		ColorToGlass.put(Color.LIME, Material.GREEN_STAINED_GLASS);
	}
	
	private void setupColorCity() {
		ColorToCityGlass.put(Color.RED, Material.PINK_STAINED_GLASS);
		ColorToCityGlass.put(Color.BLUE, Material.LIGHT_BLUE_STAINED_GLASS);
		ColorToCityGlass.put(Color.YELLOW, Material.WHITE_STAINED_GLASS);
		ColorToCityGlass.put(Color.LIME, Material.LIME_STAINED_GLASS);
	}
	
	public static void spawnFirework(int x, int z, int amount, Color color) {
		World world = Bukkit.getServer().getWorld("world");
		
		for (int i = 0; i < amount; i++) {
			Location location = new Location(world, x+i*2, 200, z+i*2);
			Firework fw = (Firework) location.getWorld().spawnEntity(location, EntityType.FIREWORK);
			FireworkMeta fwm = fw.getFireworkMeta();
			
			fwm.setPower(1);
			fwm.addEffect(FireworkEffect.builder().withColor(color).trail(true).flicker(true).build());
			fw.setFireworkMeta(fwm);
		}
	}
	
	public static CatanPlugin returnInstance() {
		return instance;
	}
	
	@Override
	public void onEnable() {
		instance = this;
		PluginManager manager = getServer().getPluginManager();
		setupLocations();
	    manager.registerEvents(new GameEventListener(), this);
	    manager.registerEvents(new PlayerLeaveListener(), this);
	    getCommand("catan").setExecutor(new CommandListener());
	    setupColorMaterial();
	    setupColorGlass();
	    setupColorCity();
	    setPrices();
	}
	
	@Override
	public void onDisable() {
		
	}

	
}
