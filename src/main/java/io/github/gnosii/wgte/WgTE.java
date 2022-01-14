/*
* WgTE Expansion for PlaceholderAPI
* Copyright (C) 2022 GNosii
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

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
 * 
 * @author GNosii
 */
public class WgTE extends PlaceholderExpansion {

	/**
	 * WorldGuard platform.
	 */
	private WorldGuardPlatform wg;

	/**
	 * Name for this expansion.
	 */
	private String NAME = "WgTE";

	/**
	 * Current version of the expansion.
	 * Tried to get it dynamically but it returned null, so...
	 */
	private String VERSION = "1.0.0";

	/**
	 * Get the identifier for this expansion. (lowercased).
	 */
	@Override
	public String getIdentifier() {
		return NAME.toLowerCase();
	}

	/**
	 * Get the author name.
	 */
	@Override
	public String getAuthor() {
		return "GNosii";
	}

	/**
	 * Get the version of the expansion.
	 */
	@Override
	public String getVersion() {
		return VERSION;
	}
	
	/**
	 * Get an list of the placeholders we register. Shown on /papi info.
	 */
	@Override
	public List<String> getPlaceholders() {
		return Arrays.asList("can_pvp");
	}

	/**
	 * Checks if this expansion can be registered. Checks for Towny and WorldGuard
	 * being enabled through {@link org.bukkit.plugin.PluginManager}.
	 */
	@Override
	public boolean canRegister() {
		// check if they're enabled!
		boolean townyCheck = Bukkit.getPluginManager().getPlugin("Towny").isEnabled();
		boolean wgCheck = Bukkit.getPluginManager().getPlugin("WorldGuard").isEnabled();

		// townyCheck is not false and wgCheck is not false
		// there it has to be an better way of doing this
		return townyCheck != false && wgCheck != false;
	}

	/**
	 * Register the expansion.
	 */
	@Override
	public boolean register() {
		if (canRegister()) {
			// register using the overriden register method.
			super.register();

			// get the worldguard platform
			wg = WorldGuard.getInstance().getPlatform();

			return true;
		}
		return false;

	}

	/**
	 * Overriden onRequest method, called by PlaceholderAPI. Internally calls my
	 * {@link #onRequest(Player, String)} method.
	 */
	@Override
	public String onRequest(OfflinePlayer player, String params) {
		if (!player.isOnline())
			return null; // cannot really do this if player is offline.
		
		return onRequest(player.getPlayer(), params);
	}

	/**
	 * Wrapped onRequest method that is called by the overriden method. Instead of
	 * accepting an {@link OfflinePlayer}, it accepts an {@link Player} entity.
	 * 
	 * @param player Bukkit player entity
	 * @param params Placeholder params
	 * @return result.
	 */
	private String onRequest(Player player, String params) {
		
		// note: this switch is intentional as i plan to add more placeholders.
		switch (params.toLowerCase()) {
		
		// %wgte_can_pvp% placeholder
		case "can_pvp": {
			return Boolean.toString(canPvP(player.getLocation(), player));
		}
		
		// %wgte_can_pvp_formatted% placeholder
		case "can_pvp_formatted": {
			return canPvP(player.getLocation(), player) ? "&aPvP" : "&cNo PvP";
		}
		
		}
		return null; // unknown placeholder
	}

	/**
	 * Does WorldGuard prevent damage?
	 * 
	 * @param loc    Location
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
	 * 
	 * @param player Player
	 * @return boolean depending on the CombatUtil test.
	 */
	private boolean doesTownyPrevent(Player player) {
		return CombatUtil.preventDamageCall(Towny.getPlugin(), player, player, DamageCause.ENTITY_ATTACK);
	}

	/**
	 * Does Towny govern protection for this location? A.K.A "is it NOT wilderness?"
	 * 
	 * @param loc Location
	 * @return inverted {@link TownyAPI#isWilderness(Location)} check (if it is
	 *         wilderness, towny shouldn't care about it, right?)
	 */
	private boolean doesTownyGovern(Location loc) {
		return !TownyAPI.getInstance().isWilderness(loc);
	}
	
	/**
	 * Can players PvP here?
	 * @param loc Location
	 * @param player Player
	 * @return boolean depending on the result
	 */
	private boolean canPvP(Location loc, Player player) {
		boolean townyGoverns = doesTownyGovern(loc);

		if (townyGoverns)
			return doesTownyPrevent(player);
		else
			return doesWgPrevent(loc, player);
	}
}
