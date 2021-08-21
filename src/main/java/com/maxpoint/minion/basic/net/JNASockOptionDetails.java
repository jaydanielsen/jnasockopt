/*
 * Copyright (c) 2021 Valassis Digital. All rights reserved.
 */
package com.maxpoint.minion.basic.net;

import com.sun.jna.Platform;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class JNASockOptionDetails {
	static JNASockOptionDetails instance = null;
	
    private Map<JNASockOption, Integer> optionMap;
    private Map<JNASockOptionLevel, Integer> levelMap;
 
	static {
		if (Platform.isLinux()) {
	        instance = new JNASockOptionDetailsLinux();
	    }
	    else if (Platform.isMac()) {
	        instance = new JNASockOptionDetailsMac();
	    }
	    
	    if (instance == null) {
	    	// empty map
	    	instance = new JNASockOptionDetails();
	    }
	}
	
	protected JNASockOptionDetails () {
		optionMap = new HashMap<JNASockOption, Integer>();
		levelMap = new HashMap<JNASockOptionLevel, Integer>();
	}
	
	protected void putOption(JNASockOption key, int value) {
		optionMap.put(key, value);
	}
	
	public static JNASockOptionDetails getInstance() {
		return instance;
	}
	protected void putLevel(JNASockOptionLevel key, int value) {
		levelMap.put(key, value);
	}

	public int getOption(JNASockOption key) throws IOException {
		Integer option = optionMap.get(key);
		if (option == null) {
			throw new IOException("Bad socket option "+key.toString());
		}
		return option;
	}
	
	public int getLevel(JNASockOptionLevel key) throws IOException {
		Integer level = levelMap.get(key);
		if (level == null) {
			throw new IOException("Bad socket option level "+key.toString());
		}
		return level;
	}
}
