package com.ggstudios.lolcraft;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONObject;

import android.graphics.drawable.Drawable;

public class ItemInfo {
	Drawable icon;
	
	int baseGold;
	int totalGold;
	boolean purchasable;
	
	Set<Integer> notOnMap;
	
	String key;
	int id;
	String name;
	String lowerName;
	String colloq;
	int stacks = 1;
	
	JSONObject stats;
	JSONObject uniquePassiveStat;
	
	JSONObject rawJson;
	String requiredChamp;
	
	List<Integer> into;
	List<Integer> from;
}
