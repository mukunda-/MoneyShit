package com.mukunda.moneyshit;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
 


import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.economy.EconomyResponse.ResponseType;

public class Economy_MoneyShit implements Economy {
	
	MoneyShit context;
	
	public Economy_MoneyShit( MoneyShit context ) {
		this.context = context;
	}

	public EconomyResponse bankBalance(String arg0) {
		return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "Bank accounts are not implemented.");
		
	}

	public EconomyResponse bankDeposit(String arg0, double arg1) {
		return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "Bank accounts are not implemented.");
		
	}

	public EconomyResponse bankHas(String arg0, double arg1) {
		return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "Bank accounts are not implemented.");
		
	}

	public EconomyResponse bankWithdraw(String arg0, double arg1) {
		return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "Bank accounts are not implemented.");
		
	}

	public EconomyResponse createBank(String arg0, String arg1) {
		return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "Bank accounts are not implemented.");
		
	}

	public EconomyResponse createBank(String arg0, OfflinePlayer arg1) {
		return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "Bank accounts are not implemented.");
		
	}

	@Deprecated
	public boolean createPlayerAccount(String arg0) {
		return true;
		
	}

	public boolean createPlayerAccount(OfflinePlayer arg0) {
		return true;
		
	}

	@Deprecated
	public boolean createPlayerAccount(String arg0, String arg1) {
		return true;
	}

	public boolean createPlayerAccount(OfflinePlayer arg0, String arg1) {
		return true;
	}

	public String currencyNamePlural() {
		return "";
	}

	public String currencyNameSingular() {
		return "";
	}

	public EconomyResponse deleteBank(String arg0) {
		return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "Bank accounts are not implemented.");
	}


	@Deprecated
	public EconomyResponse depositPlayer( String playerName, double amount ) {
		
		OfflinePlayer player = Bukkit.getOfflinePlayer( playerName );
		return depositPlayer( player, amount );
	}

	public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
		double balance = context.deposit( player.getUniqueId(), amount );
		return new EconomyResponse( amount, balance, ResponseType.SUCCESS, null );
	}

	@Deprecated
	public EconomyResponse depositPlayer(String playerName, String worldName, double amount ) {
		
		OfflinePlayer player = Bukkit.getOfflinePlayer( playerName );
		return depositPlayer( player, amount );
		
	}

	public EconomyResponse depositPlayer(OfflinePlayer player, String worldName, double amount) {
		
		return depositPlayer( player, amount );
	}

	public String format(double arg0) {
		
		return context.formatCurrency( arg0,false );
		
		
	}

	public int fractionalDigits() {
	
		return 2;
	}

	@Deprecated
	public double getBalance( String playerName ) {
		return getBalance( Bukkit.getOfflinePlayer(playerName) );
	}

	public double getBalance( OfflinePlayer player ) {
		return context.getAccount( player.getUniqueId() ).balance;
	}

	@Deprecated
	public double getBalance( String playerName, String worldName ) {
		return getBalance( Bukkit.getOfflinePlayer(playerName) );
	}

	public double getBalance( OfflinePlayer player, String worldName ) {
		return getBalance( player );
	}

	public List<String> getBanks() {
		return new ArrayList<String>();
	}

	public String getName() {
		return "MoneyShit Economy";
	}

	@Deprecated
	public boolean has( String playerName, double amount ) {
		return has( Bukkit.getOfflinePlayer(playerName), amount );
	}

	public boolean has(OfflinePlayer player, double amount ) {
		
		return context.getAccount( player.getUniqueId() ).balance >= amount;
	}

	@Deprecated
	public boolean has(String playerName, String worldName, double amount ) {
		return has( Bukkit.getOfflinePlayer(playerName), amount );
	}

	public boolean has(OfflinePlayer player, String worldName, double amount) {
		return has( player, amount );
	}

	@Deprecated
	public boolean hasAccount( String playerName ) { 
		return true;
	}

	public boolean hasAccount( OfflinePlayer player ) {
		return true;
	}

	@Deprecated
	public boolean hasAccount( String playerName, String worldName ) {
		return true;
	}

	public boolean hasAccount(OfflinePlayer player, String worldName) {
		return true;
	}

	public boolean hasBankSupport() {
		return false;
	}

	@Deprecated
	public EconomyResponse isBankMember(String arg0, String arg1) {
		return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "Bank accounts are not implemented.");
	}

	public EconomyResponse isBankMember(String arg0, OfflinePlayer arg1) {
		return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "Bank accounts are not implemented.");
	}

	@Deprecated
	public EconomyResponse isBankOwner(String arg0, String arg1) {
		return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "Bank accounts are not implemented.");
	}

	public EconomyResponse isBankOwner(String arg0, OfflinePlayer arg1) {
		return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "Bank accounts are not implemented.");
	}

	public boolean isEnabled() {
		return context.isEnabled();
	}

	@Deprecated
	public EconomyResponse withdrawPlayer( String playerName, double amount ) {
		return withdrawPlayer( Bukkit.getOfflinePlayer(playerName), amount );
	}

	public EconomyResponse withdrawPlayer( OfflinePlayer player, double amount ) {

		if( amount < 0 ) {
			return new EconomyResponse(0, 0, ResponseType.FAILURE, "Cannot withdraw negative amount.");
		}
		
		try {
			double balance = context.withdraw( player.getUniqueId(), amount );
			return new EconomyResponse( amount, balance, ResponseType.SUCCESS, null );
			
		} catch( InsufficientFunds e ) {
			return new EconomyResponse( 
					0, context.getAccount(player.getUniqueId()).balance, 
					ResponseType.FAILURE, "Insufficient funds." );
		}
	}

	@Deprecated
	public EconomyResponse withdrawPlayer( String playerName, String worldName, double amount ) {
		return withdrawPlayer( Bukkit.getOfflinePlayer(playerName), amount );
	}

	public EconomyResponse withdrawPlayer( OfflinePlayer player, String worldName, double amount ) {
		return withdrawPlayer( player, amount );
	}


}
