package Catan;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
public class PlayerLeaveListener implements Listener {
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		if (CatanPlugin.QuitterID.contains(event.getPlayer().getUniqueId())) {
			event.getPlayer().teleport(CatanPlugin.hubSpawn);
		}
	}
	
	@EventHandler 
	public void onPlayerQuit(PlayerQuitEvent event) {
		if (CatanPlugin.waitingPlayers.contains(event.getPlayer().getUniqueId())) {
			CatanPlugin.waitingPlayers.remove(event.getPlayer().getUniqueId());
		}
		if (CatanPlugin.IDToPlayer.containsKey(event.getPlayer().getUniqueId())) {
			CatanPlugin.gm.removePlayer(CatanPlugin.IDToPlayer.get(event.getPlayer().getUniqueId()));
			CatanPlugin.QuitterID.add(event.getPlayer().getUniqueId());
		}
	}
}