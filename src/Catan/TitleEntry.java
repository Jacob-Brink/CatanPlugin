package Catan;

import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Team;

public class TitleEntry extends Entry {
	TitleEntry(String titleString, int pos, Team team, Score s) {
		super(team, s, pos);
		updateEntry(titleString);
	}
}
