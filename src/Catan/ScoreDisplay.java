package Catan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import net.md_5.bungee.api.ChatColor;

public class ScoreDisplay {
	private Player player;
	private Scoreboard board;
	private Objective objective;
	private ArrayList<Entry> entryList = new ArrayList<>();
	private HashMap<String, ScoreEntry> keyToScoreEntry = new HashMap<>();
	private TimerEntry timerEntry;
	private ArrayList<String> chatColors = new ArrayList<>();
	private int chatFirstInt = -1;
	private int chatSecondInt = 0;
	
	/* addEntry
	 * Precondition: entryList is initialized
	 * Postcondition: positions of entry's are set (scoreboard automatically orders position from top to bottom from max to min score (position))
	 */
	private void addEntry(Entry entry) {
		entryList.add(entry);
		for (int i = 0; i < entryList.size(); i++) {
			Entry thisEntry = entryList.get(i);
			thisEntry.setPosition(entryList.size()-i);
		}
	}
	
	/* addTitleLine
	 * Precondition: none
	 * Postcondition: will append entry on scoreboard
	 */
	public void addTitleLine(String titleName) {
		String newEntry = makeNewTeamString();
		Team team = board.registerNewTeam(newEntry);
		team.addEntry(newEntry);
		Score score = objective.getScore(newEntry);
		TitleEntry titleEntry = new TitleEntry(titleName, 0, team, score);
		addEntry(titleEntry);
	}	
	
	/* addScoreLine
	 * Precondition: none
	 * Postcondition: score entry is appended on scoreboard
	 */
	public void addScoreLine(String key, String scoreName, int scoreCount) {
		String newEntry = makeNewTeamString();
		Team team = board.registerNewTeam(newEntry);
		team.addEntry(newEntry);
		Score score = objective.getScore(newEntry);
		ScoreEntry scoreEntry = new ScoreEntry(scoreName, scoreCount, team, score, 0);
		addEntry(scoreEntry);
		keyToScoreEntry.put(key, scoreEntry);
	}
	
	private String makeNewTeamString() {
		String firstPartEntry = ""; 
		String secondPartEntry = "";
		if (chatFirstInt == -1) {
			firstPartEntry = "";
		} else if (chatFirstInt < chatColors.size()-1) {
			firstPartEntry = chatColors.get(chatFirstInt);
		} else {
			chatFirstInt = 0;
			chatSecondInt++;
		}
		
		secondPartEntry = chatColors.get(chatSecondInt);
		
		String newEntryString = firstPartEntry + "" + secondPartEntry;
		
		chatFirstInt++;
		return newEntryString;
	}

	/* setScore
	 * Precondition: given key is in keyToScoreEntry
	 * Postcondition: score is updated
	 */
	public void setScore(String key, int newScore) {
		keyToScoreEntry.get(key).updateScore(newScore);
	}
	
	/* removeScore
	 * Precondition: score removed exists and has a key
	 * Postcondition: score is removed and no longer displayed
	 */
	public void removeScore(String key) {
		keyToScoreEntry.get(key).removeEntry();
	}
	
	public void setScoreboard() {
		player.sendMessage("Setting scoreboard");
		player.setScoreboard(board);
	}
	
	ScoreDisplay(Player p, String mainTitle) {	
		for (ChatColor cColor : ChatColor.values()) {
			chatColors.add("" + cColor);
		}
		
		player = p;
		board = Bukkit.getScoreboardManager().getNewScoreboard();
		objective = board.registerNewObjective("asdf", "ff", "Catan");//.getObjective(DisplaySlot.SIDEBAR);
		objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		objective.setDisplayName(mainTitle);
		/*Team team = board.registerNewTeam("Team");
		//Adding players
		team.addEntry(ChatColor.GREEN + "" + ChatColor.RED);
		team.addEntry(ChatColor.COLOR_CHAR + "" + ChatColor.GREEN);
		
		//Adding prefixes (shows up in player list before the player's name, supports ChatColors)
		team.setPrefix("prefix" + ChatColor.COLOR_CHAR + "asdf" + ChatColor.GREEN + "f");
		 
		//Adding suffixes (shows up in player list after the player's name, supports ChatColors)
		 
		//Setting the display name
		//team.setDisplayName("display name");
		 
		//Making invisible players on the same team have a transparent body
		team.setCanSeeFriendlyInvisibles(true);
		 
		//Making it so players can't hurt others on the same team
		team.setAllowFriendlyFire(false);
		
		objective.getScore(ChatColor.GREEN + "" + ChatColor.RED).setScore(11);
		objective.getScore(ChatColor.COLOR_CHAR + "" + ChatColor.GREEN).setScore(0);*/
	}
	
	public void clearScoreBoard() {		
		player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
	}

	public void addTimerLine(String string) {
		String newEntry = makeNewTeamString();
		Team team = board.registerNewTeam(newEntry);
		team.addEntry(newEntry);
		Score score = objective.getScore(newEntry);
		timerEntry = new TimerEntry(team, score, 0, string);
		addEntry(timerEntry);
	}

	public void setTime(int i) {
		timerEntry.updateTime(i);
	}
}
