package Catan;

import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Team;

public class TimerEntry extends Entry {
	private String title;
	
	private String getTimeString(int seconds) {
		return title + " " + seconds + "s";
	}
	
	public void updateTime(int newTime) {
		updateEntry(getTimeString(newTime));
	}
	
	TimerEntry(Team team, Score score, int pos, String titleString) {
		super(team, score, pos);
		title = titleString;
		updateEntry("");
	}

}
