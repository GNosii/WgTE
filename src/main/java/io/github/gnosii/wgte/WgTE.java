package io.github.gnosii.wgte;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.utils.CombatUtil;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.internal.platform.WorldGuardPlatform;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

/**
 * WgTE expansion class.
 * @author GNosii
 */
public class WgTE extends PlaceholderExpansion {
	
	WorldGuardPlatform wg;
	
	@Override
	public String getIdentifier() {
		return "wgte";
	}

	@Override
	public String getAuthor() {
		return "GNosii";
	}

	@Override
	public String getVersion() {
		return "1.0.0";
	}
	
	@Override
    public boolean canRegister() {
		boolean townyCheck = Bukkit.getPluginManager().getPlugin("Towny").isEnabled();
		boolean wgCheck = Bukkit.getPluginManager().getPlugin("WorldGuard").isEnabled();
		
		// townyCheck is not false and wgCheck is not false
		// there it has to be an better way of doing this	
        return townyCheck != false && wgCheck != false;
    }

	@Override
	public String onRequest(OfflinePlayer player, String params) {
		if (!player.isOnline()) return null; // cannot really do this if player is offline.
		return onRequest(player.getPlayer(), params);
	}
	
	@Override
	public boolean register() {
		if (!canRegister()) {
			return false;
		}
		
		wg = WorldGuard.getInstance().getPlatform();
		return true;
	}

	/**
	 * Wrapped onRequest method.
	 * @param player Player entity.
	 * @param params Placeholder params
	 * @return result.
	 */
    private String onRequest(Player player, String params) {
    	// can_pvp placeholder
        if (params.equalsIgnoreCase("can_pvp")) {
        	Location loc = player.getLocation();
        	
            boolean wgPrevented = doesWgPrevent(loc, player);
        	boolean townyPrevented = doesTownyPrevent(player);
        	
        	switch(Boolean.compare(wgPrevented, townyPrevented)) {
        	// wg and towny prevented pvp
        	case 0: {
        		return "true";
        	}
        	// only towny prevented
        	case 1: {
        		return "false";
        	}
        	// only wg prevented
        	case -1: {
        		return "false";
        	}
        	}
        }
        return null; // unknown placeholder
    }
    
    /**
     * Does WorldGuard prevent damage?
     * @param loc Location
     * @param player Player
     * @return boolean depending on the test query.
     */
    private boolean doesWgPrevent(Location loc, Player player) {
        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
        
        RegionContainer container = wg.getRegionContainer();
        RegionQuery query = container.createQuery();
        
        ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(loc));
        
        return set.testState(localPlayer, Flags.PVP);
    }
    
    /**
     * Does Towny prevent damage?
     * @param player Player
     * @return boolean depending on the CombatUtil test.
     */
    private boolean doesTownyPrevent(Player player) {
    	return CombatUtil.preventDamageCall(Towny.getPlugin(), player, player, DamageCause.ENTITY_ATTACK);
    }
}
