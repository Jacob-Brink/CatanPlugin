package Catan;


import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandListener implements CommandExecutor {
	
	@Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    	Player player = (Player)sender;
    	
    	if (!player.hasPermission("catan.play")) {
    		player.sendMessage(CatanPlugin.CatanPrefix + "game is still in beta testing. If you wish to play anyways contact an admin.");
    		return false;
    	}
    	
    	if (command.getName().equalsIgnoreCase("catan")) {
    		String com = args[0];
    		if(com.equalsIgnoreCase("play")) {
    			if (CatanPlugin.IDToPlayer.containsKey(player.getUniqueId())) {
    				player.sendMessage("Can't add to queue in game");
    				return false;
    			}
    			CatanPlugin.addPlayerToQueue(player);
    			return true;
    		}
    		
    		if (CatanPlugin.IDToPlayer.containsKey(player.getUniqueId())) {
    			if (com.equalsIgnoreCase("quit")) {
        			CatanPlugin.gm.removePlayer(CatanPlugin.IDToPlayer.get(player.getUniqueId()));
        		}
			}
    		
    		if (CatanPlugin.waitingPlayers.contains(player.getUniqueId())) {
    			if (com.equalsIgnoreCase("leave")) {
    				CatanPlugin.waitingPlayers.remove(player.getUniqueId());
    				player.sendMessage(CatanPlugin.CatanPrefix + "Teleporting back to hub...");
    				player.teleport(CatanPlugin.hubSpawn);
    			}
    		}
    		
    		
    	}
    	return true;
    	
	}
}

