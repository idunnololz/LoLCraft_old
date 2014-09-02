package com.ggstudios.lolcraft;

import java.util.List;

import org.json.JSONObject;

import android.graphics.drawable.Drawable;

public class ItemInfo {
	Drawable icon;
	
	int baseGold;
	int totalGold;
	boolean purchasable;
	
	String key;
	int id;
	String name;
	
	JSONObject stats;
	
	JSONObject rawJson;
	String requiredChamp;
	
	List<Integer> into;
	List<Integer> from;
}
