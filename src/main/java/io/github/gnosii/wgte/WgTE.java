package io.github.gnosii.wgte;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.TownyAPI;
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
	
	private WorldGuardPlatform wg;
	
	private String NAME = "WgTE";
	private String VERSION = getClass().getPackage().getImplementationVersion();
	
	@Override
	public String getIdentifier() {
		return NAME.toLowerCase();
	}

	@Override
	public String getAuthor() {
		return "GNosii";
	}

	@Override
	public String getVersion() {
		return VERSION;
	}
	
	@Override
	public List<String> getPlaceholders() {
		return Arrays.asList("can_pvp");
	}
	
	@Override
    public boolean canRegister() {
		// check if they're enabled!
		boolean townyCheck = Bukkit.getPluginManager().getPlugin("Towny").isEnabled();
		boolean wgCheck = Bukkit.getPluginManager().getPlugin("WorldGuard").isEnabled();
		
		// can we register?
		boolean can = townyCheck != false && wgCheck != false;
		
		// register wg platform
		// this was originally on an overriden #register method, but it prevented normal registering logic to go through.
		if (can)
			wg = WorldGuard.getInstance().getPlatform();
		
		// townyCheck is not false and wgCheck is not false
		// there it has to be an better way of doing this	
        return can;
    }

	@Override
	public String onRequest(OfflinePlayer player, String params) {
		if (!player.isOnline()) return null; // cannot really do this if player is offline.
		return onRequest(player.getPlayer(), params);
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
        	
        	// is this area wilderness?
        	boolean townyGoverns = doesTownyGovern(loc);
        	
        	if (townyGoverns)
        		return Boolean.toString(doesTownyPrevent(player));
        	else
        		return Boolean.toString(doesWgPrevent(loc, player));
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
    
    /**
     * Does Towny govern protection for this location?
     * A.K.A "is it NOT wilderness?"
     * @param loc Location
     * @return inverted {@link TownyAPI#isWilderness(Location)} check (if it is wilderness, towny shouldn't care about it, right?)
     */
    private boolean doesTownyGovern(Location loc) {
    	return !TownyAPI.getInstance().isWilderness(loc);
    }
}
