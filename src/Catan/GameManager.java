package Catan;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;

import Catan.CatanPlugin.gameHook;
import net.md_5.bungee.api.ChatColor;

public class GameManager {
	private ArrayList<CatanPlayer> players = new ArrayList<>();
	private ArrayList<Integer> calledHexs = new ArrayList<>();
	private int resourceProducingDelay = 45;
	private int firstPartDuration = 10;
	public static int winningScore = 6;
	private boolean gameOver = false;
	private boolean firstPart = true;
	private boolean preGame = true;
	private GameBoard gameBoard;
	private ScoreManager scoreManager;
	private gameHook gHook;

	public boolean loading() {
		return preGame;
	}
	
	GameManager(ArrayList<Player> basePlayers, gameHook gameHook) {
		gHook = gameHook;
		//teleport to game lobby, then change inventory
		int i = 0;
		for(Map.Entry<Color, Material> colorMaterial : CatanPlugin.ColorToMaterial.entrySet()) {
			
			CatanPlayer cPlayer = new CatanPlayer(basePlayers.get(i), colorMaterial.getKey());
			basePlayers.get(i).setGameMode(GameMode.ADVENTURE);
			players.add(i, cPlayer);
			CatanPlugin.IDToPlayer.put(basePlayers.get(i).getUniqueId(), cPlayer);
			
			i++;
			if(i == (basePlayers.size())) {
				break;
			}
		}
		
		
		//game starts
		new BukkitRunnable() {
			int seconds = 0;
			public void run() {
				if (seconds == 15) {
					startGame(basePlayers);
					cancel();
					return;
				}
				for (CatanPlayer cPlayer : players) {
					cPlayer.sendSuccess(" starting in " + (15-seconds));
				}
				this.seconds++;
			}
		}.runTaskTimer(CatanPlugin.returnInstance(), 0l, 20L);
	}
	
	public void startGame(ArrayList<Player> basePlayers) {
		scoreManager = new ScoreManager(players);
		gameBoard = new GameBoard();
		
		for (int i = 0; i < players.size(); i++) {
			CatanPlayer cPlayer = players.get(i);
			basePlayers.get(i).setGameMode(GameMode.SURVIVAL);
			cPlayer.startGame();
			cPlayer.returnPlayer().teleport(CatanPlugin.gameSpawn);
			
			cPlayer.sendSuccess("Get Ready! Get Set! Go!");
			cPlayer.setGameBoard(gameBoard);
			preGame = false;
		}
		produceResources();
		//after 60 seconds, pvp is turned on
		Bukkit.getScheduler().runTaskLater(CatanPlugin.returnInstance(), new Runnable() {
			public void run() {
				firstPart = false;
			}
		}, firstPartDuration*20);
	}
	
	public void removePlayer(CatanPlayer player) {
		CatanPlugin.IDToPlayer.remove(player.getUUID());
		players.remove(player);
		player.removeFromGame();
		if (!preGame) {
			gameBoard.removePlayer(player);
			//TODO: handle removing players items from world
			if (players.size() == 1) {
				players.get(0).sendSuccess("You won! Because unlike everyone else, you didn't quit!");
				gameOver = true;
				endGame();
			} else if (players.size() == 0) {
				gameOver = true;
				endGame();
			}
		} else {
			if (players.size() == 1) {
				players.get(0).sendSuccess("You won! Because unlike everyone else, you didn't quit!");
				gameOver = true;
			} else if (players.size() == 0) {
				gameOver = true;
			}
		}
	}
	
	public boolean pvpTurnedOn() {
		return !firstPart;
	}
	
	/* produceResources
	 * Precondition: called every so often
	 * Postcondition: all owned hexes get called
	 */
	private void produceResources() {
		for (int i = 0; i < 19; i++) {
			gameBoard.returnHexByNum(i).produceResource();
		}
		
		new BukkitRunnable() {
			int seconds = 0;
			public void run() {
				if (!gameOver) {
					this.seconds++;
					
					if (seconds == resourceProducingDelay) {
						this.seconds = 0;
						cancel();
						produceResources();
						
					}
					scoreManager.setTime(resourceProducingDelay-seconds);
				}
			}
		}.runTaskTimer(CatanPlugin.returnInstance(), 0l, 20L);
	}

	public GameBoard getBoard() {
		return gameBoard;
	}

	public boolean inProgress() {
		return !gameOver;
	}

	public void placeRoad(Location location, CatanPlayer p) {
		p.placeRoad(location);
	}
	
	private void resetPlayers() {
		for (int i = 0; i < players.size(); i++) {
			players.get(i).removeFromGame();
			CatanPlugin.IDToPlayer.remove(players.get(i).getUUID());
		}
	}
	
	private void endGame() {
		gameBoard.reset();
		gHook.endGameEvent();
	}
	
	private void checkScore(CatanPlayer p) {
		if (p.getScore() >= winningScore) {
			//TODO: figure out reward
			//p.giveReward();
			p.returnPlayer().sendMessage(CatanPlugin.CatanPrefix + ChatColor.RESET + ChatColor.GREEN+"Congratulations! You win!");
			resetPlayers();
			Bukkit.getScheduler().runTaskLater(CatanPlugin.returnInstance(), new Runnable() {
				public void run() {
					gameOver = true;
					endGame();
				}
			}, 15*20);
			
			
			
		}
	}
	
	public void placeSettlement(Location location, CatanPlayer p) {
		if (p.placeSettlement(location)) {
			
			checkScore(p);
		} 
	}

	public void upgradeSettlement(Location location, CatanPlayer p) {
		if (p.upgradeSettlement(location)) {
			checkScore(p);
		} 
	}

	public void openChest(Location location, CatanPlayer p) {
		p.openChest(location);
	}

}
