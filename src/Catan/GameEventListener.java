package Catan;

import java.util.UUID;

import org.bukkit.Location;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;


public class GameEventListener implements Listener {
	
	@EventHandler
	public void clickBlock(PlayerInteractEvent e) {
		UUID pID = (UUID) e.getPlayer().getUniqueId();
		//ignore events if player is not in game
		if (!CatanPlugin.IDToPlayer.containsKey(pID)) {
			return;
		}
		
		
		CatanPlayer cPlayer = CatanPlugin.IDToPlayer.get(pID);
		
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock() != null) {
			Block clickedBlock = e.getClickedBlock();
			if (clickedBlock.getType() == Material.CHEST) {
				cPlayer.openChest(clickedBlock.getLocation());
				e.setCancelled(true);
			} 
		}
	}
	
	/* onPlace
	 * Precondition: assumes if player id is in IDToGame map, it is in IDToplayer
	 */
	@EventHandler
    public void onPlace(BlockPlaceEvent e) {
		UUID pID = (UUID) e.getPlayer().getUniqueId();
		//ignore events if player is not in game
		if (!CatanPlugin.IDToPlayer.containsKey(pID)) {
			return;
		}
		
		
		Location location = e.getBlock().getLocation();
		CatanPlayer cPlayer = CatanPlugin.IDToPlayer.get(pID);
		Material placedBlock = e.getBlock().getType();
		if (!CatanPlugin.gm.loading()) {
			//Placing Settlements
			if (placedBlock == Material.RED_BANNER) {
				CatanPlugin.gm.placeSettlement(location, cPlayer);
			}
			
			//Placing Roads
			if (placedBlock == Material.BLUE_BANNER) {
				CatanPlugin.gm.placeRoad(location, cPlayer);
			}
			
			//Placing Cities
			if (placedBlock == Material.WHITE_BANNER) {
				CatanPlugin.gm.upgradeSettlement(location, cPlayer);
			}
		}
		
		//cancel block placement if player
		e.setCancelled(true);
    }
	
	/* onPlayerBreakBlock
	 * Postcondition: if player in game, cancel block break
	 */
	@EventHandler
	public void onPlayerBreakBlock(BlockBreakEvent e) {
		UUID pID = (UUID) e.getPlayer().getUniqueId();
		//ignore events if player is not in game
		if (!CatanPlugin.IDToPlayer.containsKey(pID)) {
			return;
		}
		e.setCancelled(true);
	}
	
	/* onEntityDeath
	 * @params: EntityDeathEvent
	 * PostCondition: if player is in game, respawn in house
	 */
	@EventHandler
	public void onEntityDeath(PlayerDeathEvent e) {
		if (e.getEntity() instanceof Player) {
			Player player = (Player)e.getEntity();
			//ignore players not in game
			if (!CatanPlugin.IDToPlayer.containsKey(player.getUniqueId())) {
				return;
			}
			//getPlayer
			CatanPlayer deadPlayer = CatanPlugin.IDToPlayer.get(player.getUniqueId());
			//TODO: check if this prevents dropping items on death
			e.getDrops().clear();
			
			//transfer resources to killer
			Player killerPlayer = e.getEntity().getKiller();
			if (killerPlayer instanceof Player) {
				CatanPlugin.IDToPlayer.get(killerPlayer.getUniqueId()).stealResources(deadPlayer);
				CatanPlugin.IDToPlayer.get(killerPlayer.getUniqueId()).sendSuccess("You negotiated with " + player.getName());
			}
		}
	}
	
	@EventHandler
	public void onRespawn(PlayerRespawnEvent e) {
		Player player = e.getPlayer();
		if (!CatanPlugin.IDToPlayer.containsKey(player.getUniqueId())) {
			return;
		}
		
		Bukkit.getScheduler().runTaskLater(CatanPlugin.returnInstance(), new Runnable() {
			public void run() {
				CatanPlugin.IDToPlayer.get(player.getUniqueId()).ReSpawn();
			}
		}, 1);
		
	}
	
	
	/* onPlayerDropItem
	 * prevents players from dropping resources
	 */
	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent e) {
		Player p = e.getPlayer();
		UUID pID = p.getUniqueId();
		if (CatanPlugin.IDToPlayer.containsKey(pID)) {
			e.setCancelled(true);
		}
	}
	
	/* onPlayerInteract
	 * disables trampling wheat
	 */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e){
        if (!CatanPlugin.IDToPlayer.containsKey(e.getPlayer().getUniqueId())) {
        	return;
        }
 
        if (e.getAction().equals(Action.PHYSICAL)) {
 
            if (e.getClickedBlock().getType().equals( Material.FARMLAND)) {
            	e.setCancelled(true);
            }
            
        }
        
    }
	
    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent e) {
    	if (!(e.getEntity() instanceof Player)) {
    		return;
    	}
    	
    	Player player = (Player) e.getEntity();
    	
    	if (CatanPlugin.waitingPlayers.contains(player.getUniqueId())) {
    		e.setCancelled(true);
    	}
    	
    	if (!CatanPlugin.IDToPlayer.containsKey(player.getUniqueId())) {
    		return;
    	}
    	
    	if (!CatanPlugin.gm.pvpTurnedOn()) {
    		e.setCancelled(true);
    	}
    }
}
