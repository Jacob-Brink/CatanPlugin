package Catan;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Team;

import net.md_5.bungee.api.ChatColor;

public class ScoreEntry extends Entry {
	private String scoreTitle;
	private int scoreCount, difference;
	private String spaces = "";
	
	private String getScoreEntry() {
		return "  " + scoreTitle + ": " + ChatColor.RESET + spaces + scoreCount;
	}
	
	public void updateScore(int newScore) {
		String changeNotification = "";
		difference = newScore - scoreCount;
		
		if (difference == 0) {
			return;
		}
		
		if (difference < 0) {
			changeNotification += "" + ChatColor.DARK_RED;
		} else if (difference > 0) {
			changeNotification += "" + ChatColor.GREEN + "+";
		}
		
		changeNotification += difference;
		updateEntry(getScoreEntry()+changeNotification);
		
		Bukkit.getScheduler().runTaskLater(CatanPlugin.returnInstance(), new Runnable() {
			public void run() {
					scoreCount = newScore;
					updateEntry(getScoreEntry());
			}
		}, 1*20);
		
	}
	
	ScoreEntry(String title, int initialScore, Team team, Score score, int pos) {
		super(team, score, pos);
		scoreCount = initialScore;
		scoreTitle = title;
		updateEntry(getScoreEntry());
	}
	
}
