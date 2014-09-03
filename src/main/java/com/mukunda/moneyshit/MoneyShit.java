/*
 * MoneyShit Economy System
 *
 * Copyright (c) 2014 Mukunda Johnson
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.mukunda.moneyshit;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.UUID;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener; 
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.java.JavaPlugin;
 
/**
 * MoneyShit Bukkit Plugin
 * 
 * @author mukunda
 *
 */
//-------------------------------------------------------------------------------------------------
public class MoneyShit extends JavaPlugin implements Listener {
	
	private static final String COIN_META = ChatColor.COLOR_CHAR + "\u0118" + ChatColor.COLOR_CHAR + "\u0118";
	
	//-------------------------------------------------------------------------------------------------
	private double startingBalance;
	private HashMap<UUID,Account> accounts;
	private HashSet<UUID> dirtyMoney;
	private boolean saveScheduled = false;
	
	
	//-------------------------------------------------------------------------------------------------
	public void onEnable() {
		accounts = new HashMap<UUID,Account>();
		dirtyMoney = new HashSet<UUID>();
		saveConfig();
		startingBalance = getConfig().getDouble("starting_balance");
		
		try {
			Files.createDirectories( getDataFolder().toPath().resolve( "users" ) );
		} catch( IOException e ) {
			getLogger().warning( "Could not create users directory. Disabling." );
			getServer().getPluginManager().disablePlugin( this );
		}
		
		Plugin vault = getServer().getPluginManager().getPlugin("Vault");
		ServicesManager manager = getServer().getServicesManager();
		try {
			Economy econ = new Economy_MoneyShit(this);
			manager.register( Economy.class, econ, vault, ServicePriority.High );
			getLogger().info( "Registered with VAULT." );
			
		} catch (Exception e) {
			getLogger().severe( "Error registering with VAULT!" );
		}
		

		getServer().getPluginManager().registerEvents( this, this );
	}
	
	public void onDisable() {
		saveToDisk();
	}
	
	/************************************************************************************
	 * Get an account associated with a UUID, and create one if it doesn't exist.
	 * 
	 * An account holds the balance plus other stored fields for an entity. It does not
	 * have to be a player UUID.
	 * 
	 * @param player  UUID of account to lookup
	 * @return        Account associated with UUID. null if a serious error occurs.
	 ************************************************************************************/
	public Account getAccount( UUID uuid ) {
		Account account = accounts.get( uuid );
		if( account == null ) {
			account = new Account();
			// try to load
			Path path = getDataFolder().toPath().resolve( "users" ).resolve( uuid.toString() + ".properties" );
			
			if( Files.exists( path ) ) {
				// load balance
				try( FileInputStream fis = new FileInputStream( path.toFile() ) ) {
					Properties props = new Properties();
					props.load(fis);
					if( !account.readProperties( props ) ) {
						getLogger().severe( "Balance missing from player file. Resetting! " );
						account.balance = startingBalance;
					} else {
						getLogger().info( "Loaded balance: " + uuid.toString() + " : " + account.balance );
					} 
					
				} catch (IOException e) { 
					getLogger().severe( "Could not read player balance file. Resetting! "  + e.getMessage() );
					account.balance = startingBalance;
					e.printStackTrace(); 
				}
				
			} else {
				// new player
				account.balance = startingBalance;
			}
			
			accounts.put( uuid, account );
		}
		
		return account;
	}
	
	/**************************************************************************
	 * Save account to disk.
	 * 
	 * @param uuid UUID of account to save.
	 **************************************************************************/
	private void saveAccountFile( UUID uuid ) {
		Account account = accounts.get( uuid ); 
		if( account == null ) return; // this should not happen and should assert something..
		
		Path path = getDataFolder().toPath().resolve( "users" ).resolve( uuid.toString() + ".properties" );
		Properties props = account.toProperties();  
		 
		try ( FileOutputStream out = new FileOutputStream( path.toFile() ) ){
			props.store( out, "Account data" );
		} catch( IOException e ) {
			getLogger().severe( "Could not save player data file! " + e.getMessage() );
			e.printStackTrace();
		}
		
	}
	
	/**************************************************************************
	 * Save any dirty (changed) accounts to disk, and reset the dirty list. 
	 **************************************************************************/
	private void saveToDisk() {
		for( UUID id : dirtyMoney ) {
			saveAccountFile(id);
		}
		dirtyMoney.clear();
	}
	
	/**************************************************************************
	 * Mark an account as "dirty" (changed), and schedule a save if one is
	 * not scheduled already.
	 * 
	 * @param uuid UUID of account to flag.
	 **************************************************************************/
	private void setDirty( UUID uuid ) {
		dirtyMoney.add( uuid );
		if( !saveScheduled ) {
			saveScheduled = true;
			
			// save after 10 seconds, (minimum interval between saves)
			Bukkit.getScheduler().scheduleSyncDelayedTask( this, new Runnable() {
				public void run() {

					saveScheduled = false;
					saveToDisk();
					
				}
				
			}, 20*10 );
			
		}
	}
	 
	/**************************************************************************
	 * Set the balance of an account.
	 * 
	 * @param uuid   UUID of account to modify.
	 * @param amount New balance to set in the account.
	 **************************************************************************/
	public void setBalance( UUID uuid, double amount ) {
		Account account = getAccount( uuid );
		account.balance = amount;
		setDirty( uuid ); 
		
		giveCurrencyItem( Bukkit.getPlayer(uuid), amount );
	} 
	
	/**************************************************************************
	 * Add to an account's balance
	 * 
	 * @param uuid    UUID of account to modify.
	 * @param amount  Amount to add to the account's balance.
	 * @return        The new balance of the account.
	 **************************************************************************/
	public double deposit( UUID uuid, double amount ) {
		Account account = getAccount( uuid );
		double balance = account.balance;
		if( amount == 0.0 ) return balance;
		balance += amount;
		
		account.balance = balance;
		setDirty( uuid ); 
		giveCurrencyItem( Bukkit.getPlayer( uuid ), amount );
		
		return balance;
	}
	
	/**************************************************************************
	 * Subtract from an account's balance.
	 * @param uuid    UUID of account to modify.
	 * @param amount  Amount to subtract from the account's balance.
	 * @return        The new balance of the account.
	 * @throws InsufficientFunds If the account's balance is less than amount.
	 **************************************************************************/
	public double withdraw( UUID uuid, double amount ) throws InsufficientFunds {
		Account account = getAccount( uuid );
		double balance = account.balance;
		if( amount > balance ) throw new InsufficientFunds();
		if( amount == 0.0 ) return balance;
		balance -= amount;
		
		account.balance = balance;
		setDirty( uuid ); 
		giveCurrencyItem( Bukkit.getPlayer(uuid), amount );
		
		return balance;
	}
	
	/**************************************************************************
	 * Read a data field from an account
	 * 
	 * Data fields are general purpose storage associated with a UUID.
	 * 
	 * @param uuid      UUID of account to modify.
	 * @param fieldName Name of field to read.
	 * @return          Value of field, or null if it doesn't exist yet.
	 **************************************************************************/
	public String readField( UUID uuid, String fieldName ) {
		Account account = getAccount( uuid );
		String data = account.fields.get( fieldName );
		return data;
	}
	
	/**************************************************************************
	 * Set a data field for an account
	 * 
	 * Data fields are general purpose storage associated with a UUID.
	 * 
	 * @param uuid       UUID of account to modify
	 * @param fieldName  Name of field to write
	 * @param fieldValue Value of field to set, null to remove the field.
	 * @return           The previous value of the field, or null if it was
	 *                   not set.
	 **************************************************************************/
	public String setField( UUID uuid, String fieldName, String fieldValue ) {
		Account account = getAccount( uuid );
		String oldValue;
		if( fieldValue == null ) {
			oldValue = account.fields.remove( fieldName );
		} else {
			oldValue = account.fields.put( fieldName, fieldValue );
		}
		
		setDirty( uuid ); 
		return oldValue;
	}
	
	/**************************************************************************
	 * Create an ItemStack that shows an account's balance. This item is 
	 * a Gold Nugget with the balance shown the title, and a short description.
	 * 
	 * @param balance Balance to display in the item
	 * @return        "Currency Item"
	 **************************************************************************/
	private ItemStack createCurrencyItem( double balance ) {
		ItemStack item = new ItemStack( Material.GOLD_NUGGET );
		ItemMeta meta = item.getItemMeta();
		
		meta.setDisplayName( 
				""+ChatColor.GOLD + ChatColor.BOLD + formatCurrency(balance,true) );
		ArrayList<String> lore = new ArrayList<String>();
		lore.add( COIN_META ); // special marker that identifies a Currency item 
		lore.add( ChatColor.GRAY + "Currency" ); // short description
		
		meta.setLore( lore );
		item.setItemMeta( meta );
		return item;
	}
	
	/**************************************************************************
	 * Search an inventory for a currency item.
	 * 
	 * @param inv Inventory to search.
	 * @return    Slot index that contains a currency item, 
	 *            or -1 if none found.
	 **************************************************************************/
	private int findCurrencyItem( Inventory inv ) {
		for( int i = 0; i < inv.getSize(); i++ ) {
			if( isCurrencyItem( inv.getItem(i) ) ) {
				return i;
			}
		}
		return -1;
	}
	
	private boolean itemExists( ItemStack item ) {
		if( item == null ) return false;
		if( item.getType() == Material.AIR ) {
			return false;
		}
		return true;
	}
	
	/**************************************************************************
	 * Give a player a currency item. This replaces an existing one if found.
	 * 
	 * @param player Player to give to, this can be null and this function will
	 *               then do nothing.
	 * @param balance Balance to reflect in the new currency item.
	 **************************************************************************/
	private void giveCurrencyItem( Player player, double balance ) {
		if( player == null ) return;
		Inventory inv = player.getInventory();
		if( inv == null ) return;
		int slot = findCurrencyItem( inv );
		if( slot < 0 ) {
			// prefer a few hotbar slots
			for( int i = 8; i >= 5; i-- ) {
				if( !itemExists( inv.getItem( i ) ) ) {
					inv.setItem( i, createCurrencyItem( balance ) );
					return;
				}
			}
			inv.addItem( createCurrencyItem( balance ) );
		} else {
			inv.setItem( slot, createCurrencyItem( balance ) );
		}
	}
	
	/**************************************************************************
	 * Checks if the item given is a "currency item".
	 * 
	 * @param item ItemStack to check.
	 * @return     true if the item is a MoneyShit currency item.
	 **************************************************************************/
	private boolean isCurrencyItem( ItemStack item ) {
		if( item == null ) return false;
		if( item.getType() != Material.GOLD_NUGGET ) return false;
		if( !item.getItemMeta().hasLore() ) return false;
		return item.getItemMeta().getLore().get(0).equals(COIN_META);
	}
	
	//-------------------------------------------------------------------------------------------------
	@EventHandler( priority = EventPriority.LOW, ignoreCancelled = true)
	public void onInventoryClick( InventoryClickEvent event ) {

		// prevent a user from moving the coin item from their inventory
		// to somewhere else
		
		boolean isCursor = isCurrencyItem(event.getCursor());
		boolean isCurrent = isCurrencyItem(event.getCurrentItem()); 
		
		if( !isCursor && !isCurrent ) return;
		
		switch( event.getAction() ) {
			case CLONE_STACK:
			case DROP_ALL_SLOT:
			case DROP_ONE_SLOT:
			case MOVE_TO_OTHER_INVENTORY: // this can have valid purposes, need to check inventory type though.
				// block
				if( isCurrent ) {
					event.setCancelled(true);
				}
				break;
			case DROP_ALL_CURSOR:
			case DROP_ONE_CURSOR:
				// block
				if( isCursor ) {
					event.setCancelled(true);
				}
				break;
			
			case PLACE_ALL:
			case PLACE_ONE:
			case PLACE_SOME:
			case SWAP_WITH_CURSOR:
				
				// block if placing in other inventory.
				if( isCursor && event.getRawSlot() < event.getInventory().getSize() ) {
					event.setCancelled(true);
				}
				break;
				
			case PICKUP_ALL:
			case PICKUP_HALF:
			case PICKUP_ONE:
			case PICKUP_SOME:
			case COLLECT_TO_CURSOR:
			case HOTBAR_MOVE_AND_READD:
			case HOTBAR_SWAP:
			case NOTHING:
				// allow
				break;
			default:
				event.setCancelled(true);
		}
		
	}
	//-------------------------------------------------------------------------------------------------
	@EventHandler( priority = EventPriority.LOW, ignoreCancelled = true)
	public void onInventoryDrag( InventoryDragEvent event ) {
		
		// prevent a user from moving the coin item from their inventory
		// to somewhere else
		if( isCurrencyItem( event.getOldCursor() ) ) {
			int size = event.getInventory().getSize();
			for( int slot : event.getRawSlots() ) {
				if( slot < size ) {
					// they put the coin somewhere it doesn't belong.
					event.setCancelled(true);
					return;
				}
			}
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@EventHandler( priority = EventPriority.LOW, ignoreCancelled = true)
	public void onDrop( PlayerDropItemEvent event ) {
		 
		// stop them from dropping their cool currency item.
		if( isCurrencyItem(event.getItemDrop().getItemStack()) ) {
			event.setCancelled(true);
		}
		
		// they can still drop it if they have no inventory space :(, but it gets destroyed
		// they will get it back the next time they login or do a transaction
	}
	
	//-------------------------------------------------------------------------------------------------
	@EventHandler
	public void onPlayerJoin( PlayerJoinEvent event ) {
		
		// give player's their currency item.
		giveCurrencyItem( event.getPlayer(), getAccount( event.getPlayer().getUniqueId() ).balance );
	}
	
	/**************************************************************************
	 * Format a currency string.
	 *   
	 * @param amount Amount to display.
	 * @param integer Discard fractional part.
	 * @return Formatted currency string, looks like "1,234,456 G" or
	 * "1,234.5 G" (when integer==false)
	 **************************************************************************/
	public String formatCurrency( Double amount, boolean integer ) {
		DecimalFormat df;
		if( integer ) {
			df = new DecimalFormat( "#,#" );
			df.setGroupingSize(3);
			return String.format( df.format(Math.floor(amount)) + " G" );
		} else {
			df = new DecimalFormat( "#,#.#" );
			df.setGroupingSize(3);
			return String.format( df.format(amount) + " G" );
		}
	}
}
