package com.ggstudios.lolcraft;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ggstudios.lolcraft.ChampionInfo.Scaling;
import com.ggstudios.lolcraft.ChampionInfo.Skill;
import com.ggstudios.utils.DebugLog;

import android.content.Context;
import android.content.res.Resources;

public class LibraryUtils {

	// champion.json = https://na.api.pvp.net/api/lol/static-data/na/v1.2/champion?api_key=0daeb2cf-a0d0-4a94-a7b2-8b282e1a4336
	// item json = https://na.api.pvp.net/api/lol/static-data/na/v1.2/item?api_key=0daeb2cf-a0d0-4a94-a7b2-8b282e1a4336

	private static final String TAG = "LibraryUtils";

	public static interface OnChampionLoadListener {
		public void onStartLoadPortrait(List<ChampionInfo> champs);
		public void onPortraitLoad(int position, ChampionInfo info);
		public void onCompleteLoadPortrait(List<ChampionInfo> champs);
	}

	public static interface OnItemLoadListener {
		public void onStartLoadPortrait(List<ItemInfo> champs);
		public void onPortraitLoad(int position, ItemInfo info);
		public void onCompleteLoadPortrait(List<ItemInfo> champs);
	}

	public static ChampionInfo completeChampionInfo(Context con, ChampionInfo info) {
		try {
			InputStream is = con.getAssets().open("champions/" + info.key + ".json");

			BufferedReader br = new BufferedReader(new InputStreamReader(is));

			StringBuilder builder = new StringBuilder();
			String readLine = null;

			// While the BufferedReader readLine is not null 
			while ((readLine = br.readLine()) != null) {
				builder.append(readLine);
			}

			// Close the InputStream and BufferedReader
			is.close();
			br.close();

			JSONObject o = new JSONObject(builder.toString());
			JSONObject champData = o.getJSONObject("data").getJSONObject(info.key);

			JSONArray skillsJson = champData.getJSONArray("spells");

			//DebugLog.d(TAG, "Data: " + champData.toString());
			//DebugLog.d(TAG, "Data: " + skills.toString());
			
			Skill[] skills = new Skill[4];

			for (int i = 0; i < skills.length; i++) {
				skills[i] = new Skill();
			}
			
			for (int i = 0; i < skillsJson.length(); i++) {
				builder.setLength(0);
				o = skillsJson.getJSONObject(i);
				Skill s = skills[i];
				
				s.id = o.getString("id");
				s.name = o.getString("name");
				s.desc = o.getString("tooltip");
				s.iconAssetName = o.getJSONObject("image").getString("full");
				s.rawEffect = o.getJSONArray("effect");
				s.rawEffectBurn = o.getJSONArray("effectBurn");
				
				if (o.has("rangeBurn")) {
					builder.append("Range: ");
					builder.append(o.getString("rangeBurn"));
					builder.append(' ');
				}
				
				if (o.has("costBurn")) {
					builder.append("Cost: ");
					builder.append(o.getString("costBurn"));
					builder.append(' ');
				}
				
				if (o.has("cooldownBurn")) {
					builder.append("Cooldown: ");
					builder.append(o.getString("cooldownBurn"));
					builder.append(' ');
				}
				
				s.details = builder.toString();

				JSONArray vars = o.getJSONArray("vars");

				for (int j = 0; j < vars.length(); j++) {
					JSONObject var = vars.getJSONObject(j);
					Scaling sc = new Scaling();
					sc.var = var.getString("key");
					sc.coeff = var.get("coeff");
					sc.link = var.getString("link");
					
					s.varToScaling.put(sc.var, sc);
				}
			}
			
			info.setSkills(skills);

		} catch (IOException e) {
			DebugLog.e(TAG, e);
		} catch (JSONException e) {
			DebugLog.e(TAG, e);
		}

		return info;
	}

	public static List<ChampionInfo> getAllChampionInfo(Context con, OnChampionLoadListener listener) 
			throws IOException, JSONException {

		List<ChampionInfo> champs = new ArrayList<ChampionInfo>();

		InputStream is = con.getResources().openRawResource(R.raw.champion);
		BufferedReader br = new BufferedReader(new InputStreamReader(is));

		StringBuilder builder = new StringBuilder();
		String readLine = null;

		// While the BufferedReader readLine is not null 
		while ((readLine = br.readLine()) != null) {
			builder.append(readLine);
		}

		// Close the InputStream and BufferedReader
		is.close();
		br.close();

		JSONObject o = new JSONObject(builder.toString());
		JSONObject champData = o;

		Iterator<?> iter = champData.keys();
		while (iter.hasNext()) {
			String key = (String) iter.next();
			try {
				JSONObject value = champData.getJSONObject(key);

				ChampionInfo info = new ChampionInfo();

				info.id = value.getInt("id");
				info.name = value.getString("name");
				info.title = value.getString("title");

				String as = value.getString("as+");
				as = as.substring(1, as.length() - 1);

				info.hp = value.getDouble("hp");
				info.hpG = value.getDouble("hp+");
				info.hpRegen = value.getDouble("hp5");
				info.hpRegenG = value.getDouble("hp5+");
				info.mp = value.getDouble("mp");
				info.mpG = value.getDouble("mp+");
				info.mpRegen = value.getDouble("mp5");
				info.mpRegenG = value.getDouble("mp5+");
				info.ad = value.getDouble("ad");
				info.adG = value.getDouble("ad+");
				info.as = value.getDouble("as");
				info.asG = Double.parseDouble(as) * 0.01;
				info.ar = value.getDouble("ar");
				info.arG = value.getDouble("ar+");
				info.mr = value.getDouble("mr");
				info.mrG = value.getDouble("mr+");
				info.ms = value.getDouble("ms");
				info.range = value.getDouble("range");

				info.key = key;

				champs.add(info);
			} catch (JSONException e) {
				DebugLog.e(TAG, e);
			}
		}

		Collections.sort(champs, new Comparator<ChampionInfo>(){

			@Override
			public int compare(ChampionInfo lhs, ChampionInfo rhs) {
				return lhs.name.compareTo(rhs.name);
			}

		});

		if (listener != null) {
			listener.onStartLoadPortrait(champs);
		}

		int c = 0;

		for (ChampionInfo i : champs) {
			Resources r = con.getResources();

			int id = r.getIdentifier(i.key.toLowerCase(Locale.US), "drawable", con.getPackageName());
			if (id != 0) {
				i.icon = r.getDrawable(id);
			}

			if (listener != null) {
				listener.onPortraitLoad(c, i);
			}
			c++;
		}

		if (listener != null) {
			listener.onCompleteLoadPortrait(champs);
		}

		return champs;
	}

	public static List<ItemInfo> getAllItemInfo(Context con, OnItemLoadListener listener) 
			throws IOException, JSONException {

		List<ItemInfo> items = new ArrayList<ItemInfo>();

		InputStream is = con.getResources().openRawResource(R.raw.item);
		BufferedReader br = new BufferedReader(new InputStreamReader(is));

		StringBuilder builder = new StringBuilder();
		String readLine = null;

		// While the BufferedReader readLine is not null 
		while ((readLine = br.readLine()) != null) {
			builder.append(readLine);
		}

		// Close the InputStream and BufferedReader
		is.close();
		br.close();

		JSONObject o = new JSONObject(builder.toString());
		JSONObject itemData = o;

		Iterator<?> iter = itemData.keys();
		while (iter.hasNext()) {
			String key = (String) iter.next();
			try {
				JSONObject value = itemData.getJSONObject(key);
				JSONObject gold = value.getJSONObject("gold");
				JSONArray into = value.optJSONArray("into");
				JSONArray from = value.optJSONArray("from");

				ItemInfo info = new ItemInfo();
				info.id = Integer.valueOf(key);
				info.key = key;
				info.baseGold = gold.getInt("base");
				info.totalGold = gold.getInt("total");
				info.purchasable = gold.getBoolean("purchasable");

				info.stats = value.getJSONObject("stats");
				
				info.from = new ArrayList<Integer>();
				info.into = new ArrayList<Integer>();
				
				if (from != null) {
					for (int i = 0; i < from.length(); i++) {
						info.from.add(from.getInt(i));
					}
				}
				
				if (into != null) {
					for (int i = 0; i < into.length(); i++) {
						info.into.add(into.getInt(i));
					}
				}

				info.rawJson = value;
				info.requiredChamp = value.optString("requiredChampion");
				if (info.requiredChamp.length() == 0) {
					info.requiredChamp = null;
				}

				items.add(info);
			} catch (JSONException e) {
				DebugLog.e(TAG, "Error while digesting item with key " + key, e);
			}
		}

		Collections.sort(items, new Comparator<ItemInfo>(){

			@Override
			public int compare(ItemInfo lhs, ItemInfo rhs) {
				return lhs.totalGold - rhs.totalGold;
			}

		});

		if (listener != null) {
			listener.onStartLoadPortrait(items);
		}

		int c = 0;

		for (ItemInfo i : items) {
			Resources r = con.getResources();

			int id = r.getIdentifier("a" + i.key, "drawable", con.getPackageName());
			if (id != 0) {
				i.icon = r.getDrawable(id);
			}

			if (listener != null) {
				listener.onPortraitLoad(c, i);
			}
			c++;
		}

		if (listener != null) {
			listener.onCompleteLoadPortrait(items);
		}

		return items;
	}
}
