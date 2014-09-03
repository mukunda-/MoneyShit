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

import java.util.HashMap; 
import java.util.Map.Entry;
import java.util.Properties; 

/******************************************************************************
 * Holds information, primarily a balance, associated with a UUID.
 * 
 * @author mukunda
 *
 ******************************************************************************/
public class Account { 

	public double balance;
	public final HashMap<String,String> fields = new HashMap<String,String>();
	  
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
	
}
