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

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
 


import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.economy.EconomyResponse.ResponseType;

/*****************************
 * MoneyShit Vault extension
 * 
 * @author mukunda
 *
 *****************************/
public class Economy_MoneyShit implements Economy {
	
	MoneyShit context;
	
	public Economy_MoneyShit( MoneyShit context ) {
		this.context = context;
	}

	@Override
	public EconomyResponse bankBalance(String arg0) {
		return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "Bank accounts are not implemented.");
		
	}

	@Override
	public EconomyResponse bankDeposit(String arg0, double arg1) {
		return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "Bank accounts are not implemented.");
		
	}

	@Override
	public EconomyResponse bankHas(String arg0, double arg1) {
		return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "Bank accounts are not implemented.");
		
	}

	@Override
	public EconomyResponse bankWithdraw(String arg0, double arg1) {
		return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "Bank accounts are not implemented.");
		
	}

	@Override
	public EconomyResponse createBank(String arg0, String arg1) {
		return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "Bank accounts are not implemented.");
		
	}

	@Override
	public EconomyResponse createBank(String arg0, OfflinePlayer arg1) {
		return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "Bank accounts are not implemented.");
		
	}

	@Override
	@Deprecated
	public boolean createPlayerAccount(String arg0) {
		return true;
		
	}

	@Override
	public boolean createPlayerAccount(OfflinePlayer arg0) {
		return true;
		
	}

	@Override
	@Deprecated
	public boolean createPlayerAccount(String arg0, String arg1) {
		return true;
	}

	@Override
	public boolean createPlayerAccount(OfflinePlayer arg0, String arg1) {
		return true;
	}

	@Override
	public String currencyNamePlural() {
		return "G";
	}

	@Override
	public String currencyNameSingular() {
		return "G";
	}

	@Override
	public EconomyResponse deleteBank(String arg0) {
		return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "Bank accounts are not implemented.");
	}

	@Override
	@Deprecated
	public EconomyResponse depositPlayer( String playerName, double amount ) {
		
		OfflinePlayer player = Bukkit.getOfflinePlayer( playerName );
		return depositPlayer( player, amount );
	}

	@Override
	public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
		double balance = context.deposit( player.getUniqueId(), amount );
		return new EconomyResponse( amount, balance, ResponseType.SUCCESS, null );
	}

	@Override
	@Deprecated
	public EconomyResponse depositPlayer(String playerName, String worldName, double amount ) {
		
		OfflinePlayer player = Bukkit.getOfflinePlayer( playerName );
		return depositPlayer( player, amount );
		
	}
	
	@Override
	public EconomyResponse depositPlayer(OfflinePlayer player, String worldName, double amount) {
		
		return depositPlayer( player, amount );
	}

	@Override
	public String format(double arg0) {
		
		return context.formatCurrency( arg0,false );
		
		
	}

	@Override
	public int fractionalDigits() {
	
		return 0;
	}

	@Override
	@Deprecated
	public double getBalance( String playerName ) {
		return getBalance( Bukkit.getOfflinePlayer(playerName) );
	}

	@Override
	public double getBalance( OfflinePlayer player ) {
		return context.getAccount( player.getUniqueId() ).balance;
	}

	@Override
	@Deprecated
	public double getBalance( String playerName, String worldName ) {
		return getBalance( Bukkit.getOfflinePlayer(playerName) );
	}

	@Override
	public double getBalance( OfflinePlayer player, String worldName ) {
		return getBalance( player );
	}

	@Override
	public List<String> getBanks() {
		return new ArrayList<String>();
	}

	@Override
	public String getName() {
		return "MoneyShit Economy";
	}

	@Override
	@Deprecated
	public boolean has( String playerName, double amount ) {
		return has( Bukkit.getOfflinePlayer(playerName), amount );
	}

	@Override
	public boolean has(OfflinePlayer player, double amount ) {
		
		return context.getAccount( player.getUniqueId() ).balance >= amount;
	}

	@Override
	@Deprecated
	public boolean has(String playerName, String worldName, double amount ) {
		return has( Bukkit.getOfflinePlayer(playerName), amount );
	}

	@Override
	public boolean has(OfflinePlayer player, String worldName, double amount) {
		return has( player, amount );
	}

	@Override
	@Deprecated
	public boolean hasAccount( String playerName ) { 
		return true;
	}

	@Override
	public boolean hasAccount( OfflinePlayer player ) {
		return true;
	}

	@Override
	@Deprecated
	public boolean hasAccount( String playerName, String worldName ) {
		return true;
	}

	@Override
	public boolean hasAccount(OfflinePlayer player, String worldName) {
		return true;
	}

	@Override
	public boolean hasBankSupport() {
		return false;
	}

	@Override
	@Deprecated
	public EconomyResponse isBankMember(String arg0, String arg1) {
		return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "Bank accounts are not implemented.");
	}

	@Override
	public EconomyResponse isBankMember(String arg0, OfflinePlayer arg1) {
		return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "Bank accounts are not implemented.");
	}

	@Override
	@Deprecated
	public EconomyResponse isBankOwner(String arg0, String arg1) {
		return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "Bank accounts are not implemented.");
	}

	@Override
	public EconomyResponse isBankOwner(String arg0, OfflinePlayer arg1) {
		return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "Bank accounts are not implemented.");
	}

	@Override
	public boolean isEnabled() {
		return context.isEnabled();
	}

	@Override
	@Deprecated
	public EconomyResponse withdrawPlayer( String playerName, double amount ) {
		return withdrawPlayer( Bukkit.getOfflinePlayer(playerName), amount );
	}

	@Override
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

	@Override
	@Deprecated
	public EconomyResponse withdrawPlayer( String playerName, String worldName, double amount ) {
		return withdrawPlayer( Bukkit.getOfflinePlayer(playerName), amount );
	}

	@Override
	public EconomyResponse withdrawPlayer( OfflinePlayer player, String worldName, double amount ) {
		return withdrawPlayer( player, amount );
	}


}
