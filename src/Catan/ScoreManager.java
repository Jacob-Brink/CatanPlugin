package Catan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Color;

import net.md_5.bungee.api.ChatColor;

public class ScoreManager {
	private static HashMap<ResourceType, String> resourceToTitle = new HashMap<>();
	private static HashMap<Color, ChatColor> colorToChatColor = new HashMap<>();
	private HashMap<CatanPlayer, ScoreDisplay> playerToScoreDisplay = new HashMap<>();
	
	public class ScoreboardHook {
		private CatanPlayer cPlayer;
		private ScoreDisplay scoreDisplay;

		ScoreboardHook(CatanPlayer catanPlayer) {
			cPlayer = catanPlayer;
			scoreDisplay = playerToScoreDisplay.get(catanPlayer);
		}
		
		public void updateScore(int score) {
			updateAllPlayerScore(cPlayer, score);
		}
		
		public void setResourceScore(ResourceType rType, int amount) {
			scoreDisplay.setScore(rType.toString(), amount);
		}
		
		public void remove() {
			scoreDisplay.clearScoreBoard();
			playerToScoreDisplay.remove(cPlayer);
			removePlayer(cPlayer);
		}
		
	}
	
	private String playerToKey(CatanPlayer catanPlayer) {
		return catanPlayer.returnPlayer().getName();
	}
	
	private String resourceToKey(ResourceType rType) {
		return rType.toString();
	}
	
	private void removePlayer(CatanPlayer catanPlayer) {
		for(Map.Entry<CatanPlayer, ScoreDisplay> entry : playerToScoreDisplay.entrySet()) {
			CatanPlayer cPlayer = entry.getKey();
			ScoreDisplay scoreDisplay = playerToScoreDisplay.get(cPlayer);
			scoreDisplay.removeScore(playerToKey(cPlayer));
		}
	}
	
	private void updateAllPlayerScore(CatanPlayer catanPlayer, int score) {
		for(Map.Entry<CatanPlayer, ScoreDisplay> entry : playerToScoreDisplay.entrySet()) {
			CatanPlayer cPlayer = entry.getKey();
			ScoreDisplay scoreDisplay = playerToScoreDisplay.get(cPlayer);
			scoreDisplay.setScore(playerToKey(catanPlayer), score);
		}
	}
	
	private String title, playerScoreTitleString, resourceScoreTitleString;
	
	private void setup() {
		title = CatanPlugin.CatanPrefix + ChatColor.RESET + ChatColor.BOLD + ChatColor.UNDERLINE + ChatColor.DARK_RED + " Explore 'n' Conquer";
		
		playerScoreTitleString = "" + ChatColor.GREEN + ChatColor.BOLD + "SCORE" + ChatColor.RESET + " (First to " + GameManager.winningScore + " wins!)";
		
		resourceScoreTitleString = "" + ChatColor.GREEN + ChatColor.BOLD + "RESOURCE";
		
		String wheat = "" + ChatColor.WHITE + "Wheat" + ChatColor.RESET;
		String stone = "" + ChatColor.WHITE + "Stone" + ChatColor.RESET;
		String wood = "" + ChatColor.WHITE + "Wood" + ChatColor.RESET;
		String brick = "" + ChatColor.WHITE + "Brick" + ChatColor.RESET;
		String wool = "" + ChatColor.WHITE + "Wool" + ChatColor.RESET;
		
		resourceToTitle.put(ResourceType.wheat, wheat);
		resourceToTitle.put(ResourceType.stone, stone);
		resourceToTitle.put(ResourceType.wool, wool);
		resourceToTitle.put(ResourceType.brick, brick);
		resourceToTitle.put(ResourceType.wood, wood);
		
		colorToChatColor.put(Color.RED, ChatColor.RED);
		colorToChatColor.put(Color.LIME, ChatColor.GREEN);
		colorToChatColor.put(Color.YELLOW, ChatColor.YELLOW);
		colorToChatColor.put(Color.BLUE, ChatColor.BLUE);
	}

	public static ChatColor getChatColor(Color color) {
		return colorToChatColor.get(color);
	}
	
	private String getPlayerTitle(CatanPlayer cPlayer) {
		return "" + getChatColor(cPlayer.getColor()) + cPlayer.returnPlayer().getName();
	}
	
	/* ScoreManager
	 * Postcondition: given players are given scoreboards
	 */
	ScoreManager(ArrayList<CatanPlayer> cPlayers) {
		setup();
		for (CatanPlayer catanPlayer : cPlayers) {
			ScoreDisplay scoreDisplay = new ScoreDisplay(catanPlayer.returnPlayer(), title);
			scoreDisplay.addTitleLine("  " + ChatColor.DARK_GRAY + "BETA");

			scoreDisplay.addTimerLine("Next Drop: ");
			
			scoreDisplay.addTitleLine(playerScoreTitleString);
			scoreDisplay.addTitleLine(" ");
			
			for (CatanPlayer cPlayer : cPlayers) {
				String key = playerToKey(cPlayer);
				if (cPlayer != catanPlayer) {
					scoreDisplay.addScoreLine(key, getPlayerTitle(cPlayer), 0);
				} else {
					scoreDisplay.addScoreLine(key, ""+getChatColor(cPlayer.getColor()) + ChatColor.BOLD + "YOU", 0);
				}
			}
			
			scoreDisplay.addTitleLine(" ");
			scoreDisplay.addTitleLine(resourceScoreTitleString);
			for (Map.Entry<ResourceType, String> resourceTitleEntry : resourceToTitle.entrySet()) {
				String key = resourceToKey(resourceTitleEntry.getKey());
				scoreDisplay.addScoreLine(key, resourceTitleEntry.getValue(), 0);
			}
			
			playerToScoreDisplay.put(catanPlayer, scoreDisplay);
			catanPlayer.registerScoreboardListener(new ScoreboardHook(catanPlayer));
			scoreDisplay.setScoreboard();
		}
	}

	public void setTime(int i) {
		for (Map.Entry<CatanPlayer, ScoreDisplay> entry : playerToScoreDisplay.entrySet()) {
			entry.getValue().setTime(i);
		}
	}
	
}

