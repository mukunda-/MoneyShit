package com.mukunda.moneyshit;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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

 


//-------------------------------------------------------------------------------------------------
public class MoneyShit extends JavaPlugin implements Listener {
	
	private static final String COIN_META = ChatColor.COLOR_CHAR + "\u0118" + ChatColor.COLOR_CHAR + "\u0118";
	
	//-------------------------------------------------------------------------------------------------
	private double startingBalance;
	private HashMap<UUID,Double> money;
	private HashSet<UUID> dirtyMoney;
	private boolean saveScheduled = false;
	
	
	//-------------------------------------------------------------------------------------------------
	public void onEnable() {
		money = new HashMap<UUID,Double>();
		dirtyMoney = new HashSet<UUID>();
		saveConfig();
		startingBalance = getConfig().getDouble("starting_balance");
		
		try {
			Files.createDirectories( getDataFolder().toPath().resolve( "balances" ) );
		} catch( IOException e ) {
			getLogger().warning( "Could not create balances directory. Disabling." );
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
	
	//-------------------------------------------------------------------------------------------------
	public double getBalance( UUID player ) {
		Double balance = money.get(player);
		if( balance == null ) {
			// try to load
			Path path = getDataFolder().toPath().resolve( "balances" ).resolve( player.toString() + ".bal" );
			
			if( Files.exists( path ) ) {
				// load balance
				try {
					String content = new String( Files.readAllBytes(path) );
					balance = Double.parseDouble( content );
					getLogger().info( "Loaded balance: " + player.toString()+ " : " + balance );
				} catch (IOException e) {
					getLogger().severe( "Could not read player balance file. Resetting! "  + e.getMessage() );
					balance = startingBalance;
					e.printStackTrace();
				} catch( NumberFormatException e ) {
					getLogger().severe( "Could not read player balance file (bad format!). Resetting! " + e.getMessage() );
					balance = startingBalance;
				}
				
			} else {
				balance = startingBalance;
			}
			
			money.put( player, balance );
		}
		
		return balance;
	}
	
	//-------------------------------------------------------------------------------------------------
	private void saveBalance( UUID player ) {
		Double balance = money.get(player);
		if( balance == null ) return; // this should not happen and should assert something..
		Path path = getDataFolder().toPath().resolve( "balances" ).resolve( player.toString() + ".bal" );
		ArrayList<String> lines = new ArrayList<String>();
		lines.add( balance.toString() );
		try {
			Files.write( path, lines, StandardCharsets.US_ASCII );
		} catch( IOException e ) {
			getLogger().severe( "Could not save player balance file. " + e.getMessage() );
			e.printStackTrace();
		}
		
	}
	
	//-------------------------------------------------------------------------------------------------
	private void saveToDisk() {
		for( UUID id : dirtyMoney ) {
			saveBalance(id);
		}
		dirtyMoney.clear();
	}
	
	//-------------------------------------------------------------------------------------------------
	private void setDirty( UUID player ) {
		dirtyMoney.add(player);
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
	
	//-------------------------------------------------------------------------------------------------
	public void setBalance( UUID player, double amount ) {
		money.put( player, amount );
		setDirty( player ); 
		
		giveCurrencyItem( Bukkit.getPlayer(player), amount );
		
	}
	
	//-------------------------------------------------------------------------------------------------
	public double deposit( UUID player, double amount ) {
		double balance = getBalance( player );
		if( amount == 0.0 ) return balance;
		balance += amount;
		setBalance( player, balance );
		return balance;
	}
	
	//-------------------------------------------------------------------------------------------------
	public double withdraw( UUID player, double amount ) throws InsufficientFunds {
		double balance = getBalance( player );
		if( amount > balance ) throw new InsufficientFunds();
		if( amount == 0.0 ) return balance;
		balance -= amount;
		setBalance( player, balance );
		return balance;
	}
	
	//-------------------------------------------------------------------------------------------------
	private ItemStack createCurrencyItem( double balance ) {
		ItemStack item = new ItemStack( Material.GOLD_NUGGET );
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName( ""+ChatColor.GOLD + ChatColor.BOLD +  formatCurrency(balance,true) );//""+ChatColor.GOLD+ ChatColor.ITALIC +"Gold" );
		ArrayList<String> lore = new ArrayList<String>();
		lore.add( COIN_META ); 
		lore.add( ChatColor.GRAY + "Currency" ); 
		  
		meta.setLore( lore );
		item.setItemMeta( meta );
		return item;
		
	}
	
	private int findCurrencyItem( Inventory inv ) {
		for( int i = 0; i < inv.getSize(); i++ ) {
			if( isCurrencyItem( inv.getItem(i) ) ) {
				return i;
			}
		}
		return -1;
	}
	
	//-------------------------------------------------------------------------------------------------
	private void giveCurrencyItem( Player player, double balance ) {
		if( player == null ) return;
		Inventory inv = player.getInventory();
		if( inv == null ) return;
		int slot = findCurrencyItem( inv );
		if( slot < 0 ) {
			inv.addItem( createCurrencyItem( balance ) );
		} else {
			inv.setItem( slot, createCurrencyItem( balance ) );
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private boolean isCurrencyItem( ItemStack item ) {
		if( item == null ) return false;
		if( item.getType() != Material.GOLD_NUGGET ) return false;
		if( !item.getItemMeta().hasLore() ) return false;
		return item.getItemMeta().getLore().get(0).equals(COIN_META);
	}
	
	//-------------------------------------------------------------------------------------------------
	@EventHandler( priority = EventPriority.LOW, ignoreCancelled = true)
	public void onInventoryClick( InventoryClickEvent event ) {
		 
		Inventory inv = event.getInventory();
		Bukkit.broadcastMessage( "---4" );
		Bukkit.broadcastMessage( ChatColor.AQUA + inv.getHolder().getClass().toString() );
		Bukkit.broadcastMessage( ChatColor.WHITE + event.getAction().toString() );
		Bukkit.broadcastMessage( ChatColor.GREEN + event.getClick().toString() );
		Bukkit.broadcastMessage( 
				""+ChatColor.BLUE + event.getHotbarButton() + "," + 
					event.getRawSlot() + "," + event.getSlot() + "," + 
					event.getSlotType().toString() + "," + 
					event.getInventory().getType() + "," + 
					event.getInventory().getSize() );
		
		boolean isCursor = isCurrencyItem(event.getCursor());
		boolean isCurrent = isCurrencyItem(event.getCurrentItem()); 
		
		if( !isCursor && !isCurrent ) return;
		
		//Bukkit.broadcastMessage( ""+ChatColor.GREEN + event.getClick().toString() );
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
		/* old and simple :(
		if( event.getSlot() == 8 && event.getSlotType() == SlotType.QUICKBAR ) {
			event.getCurrentItem();
			if( isCurrencyItem( event.getCurrentItem() ) ) {
				event.setCancelled(true);
			}
				
		}*/
		
		
	}
	//-------------------------------------------------------------------------------------------------
	@EventHandler( priority = EventPriority.LOW, ignoreCancelled = true)
	public void onInventoryDrag( InventoryDragEvent event ) {
		if( isCurrencyItem( event.getOldCursor() ) ) {
			int size = event.getInventory().getSize();
			for( int slot : event.getRawSlots() ) {
				if( slot < size ) {
					// they put the coin somewhere it doesnt belong.
					event.setCancelled(true);
					return;
				}
			}
		}
	}
	//-------------------------------------------------------------------------------------------------
	@EventHandler( priority = EventPriority.LOW, ignoreCancelled = true)
	public void onDrop( PlayerDropItemEvent event ) {
		 
		if( isCurrencyItem(event.getItemDrop().getItemStack()) ) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerJoin( PlayerJoinEvent event ) {
		giveCurrencyItem( event.getPlayer(), getBalance( event.getPlayer().getUniqueId() ) );
	}
	
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
