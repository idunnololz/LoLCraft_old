package com.ggstudios.lolcraft;

import org.json.JSONObject;

import android.graphics.drawable.Drawable;

public class RuneInfo {
	String key;
	int id;
	
	String name;
	String shortName;
	String desc;
	String shortDesc;
	String iconAssetName;
	String runeType;
	Drawable icon;
	
	JSONObject stats;
	
	JSONObject rawJson;

	Object tag;
}
