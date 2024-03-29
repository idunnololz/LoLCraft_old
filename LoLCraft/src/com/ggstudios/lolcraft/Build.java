package com.ggstudios.lolcraft;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.SparseIntArray;

import com.ggstudios.utils.DebugLog;

/**
 * Class that holds information about a build, such as build order, stats and cost. 
 */
public class Build {
	private static final String TAG = "Build";

	private List<BuildRune> runeBuild;
	private List<BuildItem> itemBuild;
	private SparseIntArray itemDic;
	
	private ChampionInfo champ;
	private int champLevel;
	
	public static final int RUNE_TYPE_RED = 0;
	public static final int RUNE_TYPE_BLUE = 1;
	public static final int RUNE_TYPE_YELLOW = 2;
	public static final int RUNE_TYPE_BLACK = 3;

	private static final int[] RUNE_COUNT_MAX = new int[] {
		9, 9, 9, 3
	};
	
	private static final int[] GROUP_COLOR = new int[] {
		0xff2ecc71,	// emerald
		//0xffe74c3c,	// alizarin
		0xff3498db,	// peter river
		0xff9b59b6,	// amethyst
		0xffe67e22,	// carrot
		0xff34495e,	// wet asphalt
		0xff1abc9c,	// turquoise
		0xfff1c40f,	// sun flower
	};
	
	private static final int FLAG_SCALING = 0x80000000;
	
	private static final int STAT_NULL = 0;
	private static final int STAT_HP = 1;
	private static final int STAT_HPR = 2;
	private static final int STAT_MP = 3;
	private static final int STAT_MPR = 4;
	private static final int STAT_AD = 5;
	//private static final int STAT_BASE_AS = asdf;
	private static final int STAT_ASP = 6;
	private static final int STAT_AR = 7;
	private static final int STAT_MR = 8;
	private static final int STAT_MS = 9;
	private static final int STAT_RANGE = 10;
	private static final int STAT_CRIT = 11;
	private static final int STAT_AP = 12;
	private static final int STAT_LS = 13;
	private static final int STAT_MSP = 14;
	private static final int STAT_CDR = 15;
	private static final int STAT_ARPEN = 16;
	private static final int STAT_NRG = 17;
	private static final int STAT_NRGR = 18;
	private static final int STAT_GP10 = 19;
	private static final int STAT_MRP = 20;
	private static final int STAT_CD = 21;
	private static final int STAT_DT = 22;
	private static final int STAT_APP = 23;
	private static final int STAT_SV = 24;
	private static final int STAT_MPENP = 25;
	private static final int STAT_APENP = 26;
	
	private static final int STAT_TOTAL_AR = 40;
	private static final int STAT_TOTAL_AD = 41;
	private static final int STAT_TOTAL_HP = 42;
	private static final int STAT_CD_MOD = 43;
	private static final int STAT_TOTAL_AP = 44;
	private static final int STAT_TOTAL_MS = 45;
	private static final int STAT_TOTAL_MR = 46;
	private static final int STAT_AS = 47;
	
	private static final int STAT_BONUS_AD = 50;
	private static final int STAT_BONUS_HP = 51;
	private static final int STAT_BONUS_MS = 52;
	private static final int STAT_BONUS_AP = 44;	// note that cause base AP is always 0, bonusAp always = totalAp
	private static final int STAT_BONUS_AR = 53;
	private static final int STAT_BONUS_MR = 54;

	private static final int STAT_AA_DPS = 60;

	private static final int MAX_STATS = 70;
	private static final int MAX_ACTIVE_ITEMS = 6;
	
	private static final String JSON_KEY_RUNES = "runes";
	private static final String JSON_KEY_ITEMS = "items";
	
	private static Map<String, Integer> statKeyToIndex;
	
	private static ItemLibrary itemLibrary;
	private static RuneLibrary runeLibrary;
	
	static {
		statKeyToIndex = new HashMap<String, Integer>();
		statKeyToIndex.put("FlatArmorMod", 			STAT_AR);
		statKeyToIndex.put("FlatAttackSpeedMod", 	STAT_NULL);
		statKeyToIndex.put("FlatBlockMod", 			STAT_NULL);
		statKeyToIndex.put("FlatCritChanceMod", 	STAT_CRIT);
		statKeyToIndex.put("FlatCritDamageMod", 	STAT_NULL);
		statKeyToIndex.put("FlatEXPBonus", 			STAT_NULL);
		statKeyToIndex.put("FlatEnergyPoolMod", 	STAT_NULL);
		statKeyToIndex.put("FlatEnergyRegenMod", 	STAT_NULL);
		statKeyToIndex.put("FlatHPPoolMod", 		STAT_HP);
		statKeyToIndex.put("FlatHPRegenMod", 		STAT_HPR);
		statKeyToIndex.put("FlatMPPoolMod", 		STAT_MP);
		statKeyToIndex.put("FlatMPRegenMod", 		STAT_MPR);
		statKeyToIndex.put("FlatMagicDamageMod", 	STAT_AP);
		statKeyToIndex.put("FlatMovementSpeedMod", 	STAT_MS);
		statKeyToIndex.put("FlatPhysicalDamageMod", STAT_AD);
		statKeyToIndex.put("FlatSpellBlockMod", 	STAT_MR);
		statKeyToIndex.put("FlatCoolDownRedMod", 	STAT_CDR);
		statKeyToIndex.put("PercentArmorMod", 		STAT_NULL);
		statKeyToIndex.put("PercentAttackSpeedMod", STAT_ASP);
		statKeyToIndex.put("PercentBlockMod", 		STAT_NULL);
		statKeyToIndex.put("PercentCritChanceMod", 	STAT_NULL);
		statKeyToIndex.put("PercentCritDamageMod", 	STAT_NULL);
		statKeyToIndex.put("PercentDodgeMod", 		STAT_NULL);
		statKeyToIndex.put("PercentEXPBonus", 		STAT_NULL);
		statKeyToIndex.put("PercentHPPoolMod", 		STAT_NULL);
		statKeyToIndex.put("PercentHPRegenMod", 	STAT_NULL);
		statKeyToIndex.put("PercentLifeStealMod", 	STAT_LS);
		statKeyToIndex.put("PercentMPPoolMod", 		STAT_NULL);
		statKeyToIndex.put("PercentMPRegenMod", 	STAT_NULL);
		statKeyToIndex.put("PercentMagicDamageMod", STAT_APP);
		statKeyToIndex.put("PercentMovementSpeedMod",	STAT_MSP);
		statKeyToIndex.put("PercentPhysicalDamageMod", 	STAT_NULL);
		statKeyToIndex.put("PercentSpellBlockMod", 		STAT_NULL);
		statKeyToIndex.put("PercentSpellVampMod", 		STAT_SV);
		
		statKeyToIndex.put("rFlatArmorModPerLevel", 			STAT_AR | FLAG_SCALING);	
		statKeyToIndex.put("rFlatArmorPenetrationMod", 			STAT_ARPEN);	
		statKeyToIndex.put("rFlatArmorPenetrationModPerLevel", 	STAT_ARPEN | FLAG_SCALING);
		statKeyToIndex.put("rFlatEnergyModPerLevel", 			STAT_NRG | FLAG_SCALING);	
		statKeyToIndex.put("rFlatEnergyRegenModPerLevel", 		STAT_NRGR | FLAG_SCALING);	
		statKeyToIndex.put("rFlatGoldPer10Mod", 				STAT_GP10);	
		statKeyToIndex.put("rFlatHPModPerLevel", 				STAT_HP | FLAG_SCALING);	
		statKeyToIndex.put("rFlatHPRegenModPerLevel", 			STAT_HPR | FLAG_SCALING);	
		statKeyToIndex.put("rFlatMPModPerLevel", 				STAT_MP | FLAG_SCALING);	
		statKeyToIndex.put("rFlatMPRegenModPerLevel", 			STAT_MPR | FLAG_SCALING);	
		statKeyToIndex.put("rFlatMagicDamageModPerLevel", 		STAT_AP | FLAG_SCALING);	
		statKeyToIndex.put("rFlatMagicPenetrationMod", 			STAT_MRP);	
		statKeyToIndex.put("rFlatMagicPenetrationModPerLevel", 	STAT_MRP | FLAG_SCALING);
		statKeyToIndex.put("rFlatPhysicalDamageModPerLevel", 	STAT_AD | FLAG_SCALING);	
		statKeyToIndex.put("rFlatSpellBlockModPerLevel", 		STAT_MR | FLAG_SCALING);
		statKeyToIndex.put("rPercentCooldownMod", 				STAT_CD);					// negative val...
		statKeyToIndex.put("rPercentCooldownModPerLevel", 		STAT_CD | FLAG_SCALING);
		statKeyToIndex.put("rPercentTimeDeadMod", 				STAT_DT);	
		statKeyToIndex.put("rPercentTimeDeadModPerLevel", 		STAT_DT | FLAG_SCALING);	
		statKeyToIndex.put("rPercentMagicPenetrationMod",		STAT_MPENP);
		statKeyToIndex.put("rPercentArmorPenetrationMod",		STAT_APENP);
		
		
		// keys used for skills...
		statKeyToIndex.put("spelldamage", 			STAT_TOTAL_AP);
		statKeyToIndex.put("attackdamage", 			STAT_TOTAL_AD);
		statKeyToIndex.put("bonushealth", 			STAT_BONUS_HP);
		statKeyToIndex.put("armor", 				STAT_TOTAL_AR);
		statKeyToIndex.put("bonusattackdamage", 	STAT_BONUS_AD);
		statKeyToIndex.put("health", 				STAT_TOTAL_HP);
		statKeyToIndex.put("bonusarmor", 			STAT_BONUS_AR);
		statKeyToIndex.put("bonusspellblock", 		STAT_BONUS_MR);
		
		// special keys...
		statKeyToIndex.put("@special.BraumWArmor", 	STAT_NULL);
		statKeyToIndex.put("@special.BraumWMR", 	STAT_NULL);
		
		statKeyToIndex.put("@cooldownchampion", 	STAT_CD_MOD);
	}
	
	private List<BuildObserver> observers = new ArrayList<BuildObserver>();

	private int enabledBuildStart = 0;
	private int enabledBuildEnd = 0;
	
	private int currentGroupCounter = 0;
	
	private int[] runeCount = new int[4];

	private double[] stats = new double[MAX_STATS];
	
	private boolean itemBuildDirty = false;
	
	private OnRuneCountChangedListener onRuneCountChangedListener = new OnRuneCountChangedListener() {

		@Override
		public void onRuneCountChanged(BuildRune rune, int oldCount, int newCount) {
			Build.this.onRuneCountChanged(rune, oldCount, newCount);
		}
		
	};

	public Build() {
		itemBuild = new ArrayList<BuildItem>();
		runeBuild = new ArrayList<BuildRune>();
		itemDic = new SparseIntArray();
		
		if (itemLibrary == null) {
			itemLibrary = LibraryManager.getInstance().getItemLibrary();
		}
		if (runeLibrary == null) {
			runeLibrary = LibraryManager.getInstance().getRuneLibrary();
		}
	}
	
	private void clearGroups() {
		for (BuildItem item : itemBuild) {
			item.group = -1;
			item.to = null;
		}
		
		currentGroupCounter = 0;
	}
	
	private void recalculateAllGroups() {
		clearGroups();
		
		for (int i = 0; i < itemBuild.size(); i++) {
			labelAllIngredients(itemBuild.get(i), i);
		}
	}
	
	private BuildItem getFreeItemWithId(int id, int index) {
		for (int i = index - 1; i >= 0; i--) {
			if (itemBuild.get(i).getId() == id && itemBuild.get(i).to == null) {
				return itemBuild.get(i);
			}
		}
		return null;
	}
	
	private void labelAllIngredients(BuildItem item, int index) {
		int curGroup = currentGroupCounter;
		
		boolean grouped = false;
		Stack<Integer> from = new Stack<Integer>();
		from.addAll(item.info.from);
		while (!from.empty()) {
			int i = from.pop();
			BuildItem ingredient = getFreeItemWithId(i, index);
			
			if (ingredient != null && ingredient.to == null) {
				if (ingredient.group != -1) {
					curGroup = ingredient.group;
				}
				ingredient.to = item;
				item.from.add(ingredient);
				grouped = true;
			} else {
				from.addAll(itemLibrary.getItemInfo(i).from);
			}
		}
		
		if (grouped) {
			for (BuildItem i : item.from) {
				i.group = curGroup;
			}
			
			item.group = curGroup;
			
			if (curGroup == currentGroupCounter) {
				currentGroupCounter++;
			}
		}
	}

	public void addItem(ItemInfo item) {
		addItem(item, 1, true);
	}
	
	public void addItem(ItemInfo item, int count, boolean isAll) {
		BuildItem buildItem = null;
		BuildItem last = getLastItem();
		if (last != null && item == last.info) {
			if (item.stacks > last.count) {
				last.count += count;
				buildItem = last;
			}
		}
		
		if (isAll == false) {
			itemBuildDirty = true;
		}
		
		boolean itemNull = buildItem == null;
		if (itemNull) {
			buildItem = new BuildItem(item);
			buildItem.count = Math.min(item.stacks, count);
			// check if ingredients of this item is already part of the build...
			labelAllIngredients(buildItem, itemBuild.size());

			if (itemBuild.size() == enabledBuildEnd) {
				enabledBuildEnd++;
			}
			itemBuild.add(buildItem);
			itemDic.put(item.id, itemDic.get(item.id, 0) + 1);
		}

		if (isAll) {
			recalculateStats();
			if (itemBuildDirty) {
				itemBuildDirty = false;
				buildItem = null;
			}
			notifyItemAdded(buildItem, itemNull);
		}
	}
	
	public void removeItemAt(int position) {
		BuildItem item = itemBuild.get(position);
		itemBuild.remove(position);
		itemDic.put(item.info.id, itemDic.get(item.info.id));
		normalizeValues();
		recalculateAllGroups();

		recalculateStats();
		notifyBuildChanged();
	}

	private void clearStats() {
		for (int i = 0; i < stats.length; i++) {
			stats[i] = 0;	 
		}
	}

	private void recalculateStats() {
		clearStats();
		
		int active = 0;
		
		for (BuildRune r : runeBuild) {
			appendStat(r);
		}
		
		for (BuildItem item : itemBuild) {
			item.active = false;
		}
		
		HashSet<Integer> alreadyAdded = new HashSet<Integer>();
		
		for (int i = enabledBuildEnd - 1; i >= enabledBuildStart; i--) {
			BuildItem item = itemBuild.get(i);
			if (item.to == null || itemBuild.indexOf(item.to) >= enabledBuildEnd) {
				item.active = true;
				
				ItemInfo info = item.info;
				
				appendStat(info.stats);
				
				int id = info.id;
				if (info.uniquePassiveStat != null && !alreadyAdded.contains(id)) {
					alreadyAdded.add(info.id);
					appendStat(info.uniquePassiveStat);
				}
				
				
				active++;
				
				if (active == MAX_ACTIVE_ITEMS)
					break;
			}
		}
		
		calculateTotalStats();
		notifyBuildStatsChanged();
	}

	private void appendStat(JSONObject jsonStats) {
		Iterator<?> iter = jsonStats.keys();
		while (iter.hasNext()) {
			String key = (String) iter.next();
			try {
				stats[statKeyToIndex.get(key)] += jsonStats.getDouble(key);

			} catch (JSONException e) {
				DebugLog.e(TAG, e);
			}
		}
	}
	
	private void appendStat(BuildRune rune) {
		RuneInfo info = rune.info;
		Iterator<?> iter = info.stats.keys();
		while (iter.hasNext()) {
			String key = (String) iter.next();
			try {
				int f = statKeyToIndex.get(key);
				if ((f & FLAG_SCALING) != 0) {
					stats[f & ~FLAG_SCALING] += info.stats.getDouble(key) * (champLevel + 1) * rune.count;
				} else {
					stats[f] += info.stats.getDouble(key) * rune.count;
				}

			} catch (JSONException e) {
				DebugLog.e(TAG, e);
			}
		}
	}
	
	private void calculateTotalStats() {
		// do some stat normalization...
		stats[STAT_CDR] = Math.min(0.4, stats[STAT_CDR] - stats[STAT_CD]);
		
		stats[STAT_TOTAL_AR] = stats[STAT_AR] + champ.ar + champ.arG * champLevel;
		stats[STAT_TOTAL_AD] = stats[STAT_AD] + champ.ad + champ.adG * champLevel;
		stats[STAT_TOTAL_HP] = stats[STAT_HP] + champ.hp + champ.hpG * champLevel;
		stats[STAT_CD_MOD] = 1.0 - stats[STAT_CDR];
		stats[STAT_TOTAL_MS] = (stats[STAT_MS] + champ.ms) * stats[STAT_MSP] + stats[STAT_MS] + champ.ms;
		stats[STAT_TOTAL_AP] = stats[STAT_AP] * (stats[STAT_APP] + 1);
		stats[STAT_TOTAL_MR] = stats[STAT_MR] + champ.mr + champ.mrG * champLevel;
		stats[STAT_AS] = champ.as * (1 + champLevel * champ.asG + stats[STAT_ASP]);
		
		stats[STAT_BONUS_AD] = stats[STAT_TOTAL_AD] - champ.ad;
		stats[STAT_BONUS_HP] = stats[STAT_TOTAL_HP] - champ.hp;
		stats[STAT_BONUS_MS] = stats[STAT_TOTAL_MS] - champ.ms;
		stats[STAT_BONUS_AR] = stats[STAT_TOTAL_AR] - champ.ar;
		stats[STAT_BONUS_MR] = stats[STAT_TOTAL_MR] - champ.mr;
		
		// pure stats...
		stats[STAT_AA_DPS] = stats[STAT_TOTAL_AD] * stats[STAT_AS];
	}
	
	public BuildRune addRune(RuneInfo rune) {
		return addRune(rune, 1, true);
	}
	
	public BuildRune addRune(RuneInfo rune, int count, boolean isAll) {
		// Check if this rune is already in the build...
		
		BuildRune r = null;
		for (BuildRune br : runeBuild) {
			if (br.id == rune.id) {
				r = br;
				break;
			}
		}

		if (r == null) {
			r = new BuildRune(rune, rune.id);
			runeBuild.add(r);
			r.listener = onRuneCountChangedListener;
			notifyRuneAdded(r);
		}

		r.addRune(count);
		
		recalculateStats();
		
		return r;
	}
	
	public boolean canAdd(RuneInfo rune) {
		return runeCount[rune.runeType] + 1 <= RUNE_COUNT_MAX[rune.runeType];
	}
	
	public void removeRune(BuildRune rune) {
		rune.listener = null;
		runeBuild.remove(rune);
		
		recalculateStats();
		notifyRuneRemoved(rune);
	}
	
	private void onRuneCountChanged(BuildRune rune, int oldCount, int newCount) {
		int runeType = rune.info.runeType;
		if (runeCount[runeType] + (newCount - oldCount) > RUNE_COUNT_MAX[runeType]) {
			rune.count = oldCount;
			return;
		}
		
		runeCount[runeType] += (newCount - oldCount);
		
		if (rune.getCount() == 0) {
			removeRune(rune);
		} else {
			recalculateStats();
		}
	}
	
	public void setChampion(ChampionInfo champ) {
		this.champ = champ;
		
		recalculateStats();
	}
	
	public void setChampionLevel(int level) {
		champLevel = level;
		
		recalculateStats();
	}

	public void registerObserver(BuildObserver observer) {
		observers.add(observer);
	}

	public void unregisterObserver(BuildObserver observer) {
		observers.remove(observer);
	}

	private void notifyBuildChanged() {
		for (BuildObserver o : observers) {
			o.onBuildChanged(this);
		}
	}

	private void notifyItemAdded(BuildItem item, boolean isNewItem) {
		for (BuildObserver o : observers) {
			o.onItemAdded(this, item, isNewItem);
		}
	}
	
	private void notifyRuneAdded(BuildRune rune) {
		for (BuildObserver o : observers) {
			o.onRuneAdded(this, rune);
		}
	}
	
	private void notifyRuneRemoved(BuildRune rune) {
		for (BuildObserver o : observers) {
			o.onRuneRemoved(this, rune);
		}
	}

	private void notifyBuildStatsChanged() {
		for (BuildObserver o : observers) {
			o.onBuildStatsChanged();
		}
	}
	
	private void normalizeValues() {
		if (enabledBuildStart < 0) {
			enabledBuildStart = 0;
		}
		
		if (enabledBuildEnd > itemBuild.size()) {
			enabledBuildEnd = itemBuild.size();
		}
	}

	public BuildItem getItem(int index) {
		return itemBuild.get(index);
	}

	public int getBuildSize() {
		return itemBuild.size();
	}
	
	public BuildRune getRune(int index) {
		return runeBuild.get(index);
	}
	
	public int getRuneCount() {
		return runeBuild.size();
	}

	public BuildItem getLastItem() {
		if (itemBuild.size() == 0) return null;
		return itemBuild.get(itemBuild.size() - 1);
	}

	public double getBonusHp() {
		return stats[STAT_HP];
	}

	public double getBonusHpRegen() {
		return stats[STAT_HPR];
	}

	public double getBonusMp() {
		if (champ.partype == ChampionInfo.TYPE_MANA) {
			return stats[STAT_MP];
		} else {
			return 0;
		}
	}

	public double getBonusMpRegen() {
		if (champ.partype == ChampionInfo.TYPE_MANA) {
			return stats[STAT_MPR];
		} else {
			return 0;
		}
	}

	public double getBonusAd() {
		return stats[STAT_AD];
	}

	public double getBonusAs() {
		return stats[STAT_ASP];
	}

	public double getBonusAr() {
		return stats[STAT_AR];
	}

	public double getBonusMr() {
		return stats[STAT_MR];
	}

	public double getBonusMs() {
		return stats[STAT_BONUS_MS];
	}

	public double getBonusRange() {
		return stats[STAT_RANGE];
	}

	public double getBonusAp() {
		return stats[STAT_BONUS_AP];
	}
	
	public double getBonusEnergy() {
		return stats[STAT_NRG];
	}
	
	public double getBonusEnergyRegen() {
		return stats[STAT_NRGR];
	}
	
	public double[] getRawStats() {
		return stats;
	}
	
	public double getStat(String key) {
		Integer i = statKeyToIndex.get(key);
		if (i != null) {
			return stats[i];
		} else {
			throw new RuntimeException("Error. Stat with key " + key + " not found!");
		}
	}

	public void reorder(int itemOldPosition, int itemNewPosition) {
		BuildItem item = itemBuild.get(itemOldPosition);
		itemBuild.remove(itemOldPosition);
		itemBuild.add(itemNewPosition, item);
		
		recalculateAllGroups();

		recalculateStats();
		notifyBuildStatsChanged();
	}

	public int getEnabledBuildStart() {
		return enabledBuildStart;
	}

	public int getEnabledBuildEnd() {
		return enabledBuildEnd;
	}

	public void setEnabledBuildStart(int start) {
		enabledBuildStart = start;

		recalculateStats();
		notifyBuildStatsChanged();
	}

	public void setEnabledBuildEnd(int end) {
		enabledBuildEnd = end;

		recalculateStats();
		notifyBuildStatsChanged();
	}
	
	public JSONObject toJson() throws JSONException {
		JSONObject obj = new JSONObject();
		JSONObject runes = new JSONObject();
		JSONArray items = new JSONArray();
		
		for (BuildRune r : runeBuild) {
			runes.put(r.info.key, r.count);
		}
		
		for (BuildItem i : itemBuild) {
			items.put(i.info.id);
			items.put(i.count);
		}
		
		obj.put(JSON_KEY_RUNES, runes);
		obj.put(JSON_KEY_ITEMS, items);
		
		return obj;
	}
	
	public void fromJson(JSONObject o) throws JSONException {
		JSONObject runes = o.getJSONObject(JSON_KEY_RUNES);
		JSONArray items = o.getJSONArray(JSON_KEY_ITEMS);
		
		Iterator<?> it = runes.keys();
		while (it.hasNext()) {
			String key = (String) it.next();
			int count = runes.getInt(key);
			addRune(runeLibrary.getRuneInfo(Integer.valueOf(key)), count, !it.hasNext());
		}
		
		final int count = items.length();
		for (int i = 0; i < count; i += 2) {
			int itemId = items.getInt(i);
			int c = items.getInt(i + 1);
			addItem(itemLibrary.getItemInfo(itemId), c, i == count - 2);
		}
	}
	
	public static int getSuggestedColorForGroup(int groupId) {
		return GROUP_COLOR[groupId % GROUP_COLOR.length];
	}

	public static interface BuildObserver {
		public void onBuildChanged(Build build);
		public void onItemAdded(Build build, BuildItem item, boolean isNewItem);
		public void onRuneAdded(Build build, BuildRune rune);
		public void onRuneRemoved(Build build, BuildRune rune);
		public void onBuildStatsChanged();
	}
	
	public static class BuildItem {
		ItemInfo info;
		int group = -1;
		boolean active = true;
		
		int count = 1;
		
		List<BuildItem> from;
		BuildItem to;
		
		private BuildItem(ItemInfo info) {
			this.info = info;
			
			from = new ArrayList<BuildItem>();
		}
		
		public int getId() {
			return info.id;
		}
	}
	
	public static class BuildRune {
		RuneInfo info;
		Object tag;
		int id;
		
		private int count;
		private OnRuneCountChangedListener listener;
		private OnRuneCountChangedListener onRuneCountChangedListener;
		
		private BuildRune(RuneInfo info, int id) {
			this.info = info;
			count = 0;
			this.id = id;
		}
		
		public void addRune() {
			addRune(1);
		}
		
		public void addRune(int n) {
			count += n;
			
			int c = count;
			
			listener.onRuneCountChanged(this, count - n, count);
			if (c == count && onRuneCountChangedListener != null) {
				onRuneCountChangedListener.onRuneCountChanged(this, count - n, count);
			}
		}
		
		public void removeRune() {
			if (count == 0) return;
			count--;

			int c = count;
			
			listener.onRuneCountChanged(this, count + 1, count);
			if (c == count && onRuneCountChangedListener != null) {
				onRuneCountChangedListener.onRuneCountChanged(this, count + 1, count);
			}
		}
		
		public int getCount() {
			return count;
		}
		
		public void setOnRuneCountChangedListener(OnRuneCountChangedListener listener) {
			onRuneCountChangedListener = listener;
		}
	}
	
	public static interface OnRuneCountChangedListener {
		public void onRuneCountChanged(BuildRune rune, int oldCount, int newCount);
	}
}
