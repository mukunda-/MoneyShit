package com.mukunda.moneyshit;

import java.util.HashMap; 
import java.util.Map.Entry;
import java.util.Properties; 

//---------------------------------------------------------------------------------------------
public class Account { 
//---------------------------------------------------------------------------------------------
	double balance;
	
	HashMap<String,String> fields = new HashMap<String,String>();
	  
	//---------------------------------------------------------------------------------------------
	public Properties toProperties() {
		Properties props = new Properties();
		props.setProperty( "balance", Double.toString(balance) );
		
		for( Entry<String,String> entry : fields.entrySet() ) {
			props.setProperty( entry.getKey(), entry.getValue() );
		}
		
		return props;
	}

	//---------------------------------------------------------------------------------------------
	public enum ReadPropertiesResult {
		OKAY,NOBALANCE;
	}

	//---------------------------------------------------------------------------------------------
	public boolean readProperties( Properties props ) {
		boolean hasBalance = true;
		String strBalance = props.getProperty( "balance" );
		
		if( strBalance == null ) {
			hasBalance = false;
			balance = 0.0;
		} else {
			try {
				balance = Double.parseDouble( strBalance );
			} catch (NumberFormatException e ) {
				balance = 0.0;
				hasBalance = false;
			}
		}
		
		props.remove( "balance" );
		
		for( Entry<Object, Object> entry : props.entrySet() ) {
			fields.put( (String)entry.getKey(), (String)entry.getValue() );
		}
		
		return hasBalance;
	}
	
	//---------------------------------------------------------------------------------------------
}
