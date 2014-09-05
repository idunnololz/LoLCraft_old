package com.ggstudios.lolcraft;

import org.json.JSONObject;

import android.graphics.drawable.Drawable;

public class RuneInfo {
	String key;
	int id;
	
	String name;
	String lowerName;
	String shortName;
	String veryShortName;
	String desc;
	String shortDesc;
	String iconAssetName;
	int runeType;
	Drawable icon;
	
	String colloq;
	
	JSONObject stats;
	
	JSONObject rawJson;

	Object tag;
}
