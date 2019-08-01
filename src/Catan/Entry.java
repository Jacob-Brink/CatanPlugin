package Catan;

import org.bukkit.ChatColor;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public abstract class Entry {
	private String entryString;
	private Team team;
	private Score score;
	
	public void removeEntry() {
		team.unregister();
	}
	
	public String getString() {
		return entryString;
	}
	
	public void setPosition(int pos) {
		score.setScore(pos);
	}
	
	protected void updateEntry(String newDisplayText) {		
		System.out.println(newDisplayText);
		team.setPrefix("" + ChatColor.RESET + newDisplayText);
	}
	
	Entry(Team t, Score s, int pos) {
		score = s;
		setPosition(pos);
		team = t;
	}
}
