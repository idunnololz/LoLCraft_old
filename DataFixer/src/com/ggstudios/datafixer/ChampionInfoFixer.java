package com.ggstudios.datafixer;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ggstudios.datafixer.ChampionInfo.Passive;
import com.ggstudios.datafixer.ChampionInfo.Skill;

import static com.ggstudios.datafixer.Main.p;

public class ChampionInfoFixer {

	private static final int METHOD_AOE = 0x80000000,
			METHOD_AMP = 0x40000000,
			METHOD_DOT = 0x20000000,
			AP = 0x10000000,
			AD = 0x0F000000,

			METHOD_DPS = 1,
			METHOD_SUSTAIN = 2,
			METHOD_BURST = 3,
			METHOD_CC = 4,
			METHOD_TANK = 5,
			METHOD_MOBILITY = 6,

			METHOD_AOE_BURST = METHOD_BURST | METHOD_AOE,
			METHOD_BURST_AMP = METHOD_BURST | METHOD_AMP,
			METHOD_DOT_BURST = METHOD_BURST | METHOD_DOT;

	private static final int CC_KNOCKUP = 1,
			CC_SLOW = 2, CC_CHARM = 3;

	private static final int AMP_MAGIC = 1;

	private static final int MOBI_BLINK = 1, MOBI_DASH = 2, MOBI_FLAT_MS = 3, MOBI_MSP = 4;

	private static final Object[] CHAMPION_SPELL_DAMAGE = new Object[] {
		// 0th item is the number of methods a skill can be considered
		// 1st item is the method in which the skill should be considered
		// 2nd item is the number of scalings

		// For passives, the third is the number of level segments

		// For METHOD_CC, the duration is followed, for CC_SLOW, the slow amount is followed by the duration

		// defined scalings are either of the form: <SCALING>, <SCALING_TYPE>
		//										or	<SCALING_LV1>, <SCALING_LV2>..., <SCALING_TYPE>
		// It is up to the interpreter to figure out which form each spell uses...

		// Calculation for the different categories will go as:
		// METHOD_DPS = Stats will be added in before DPS calculations, this can also be stats that will be considered when determining dps such as range boosts
		// METHOD_SUSTAIN = All stats counted as HP regain
		// METHOD_BURST = All stats counted as damage. To be totaled
		// METHOD_CC = Define type of CC duration and strength
		// METHOD_TANK = Define tanking stats (such as hp game, mr gain, ar gain, etc)
		// METHOD_MOBILITY = Define modifiers to movement speed or distance coverage

		// METHOD_DPS has a second field which defines the number of stats that will be modified by the skill

		//			0  	1 			 		2  3	
		"Aatrox", 	1, 	METHOD_DPS, 		0, 1, 6, 1, 0.3, 4, 0.35, 7, 0.4, 10, 0.45, 13, 0.5, 16, 0.55, "PercentAttackSpeedMod",
		2, 	METHOD_BURST|AD, 	1, 70, 115, 160, 205, 250, 0.6, "bonusattackdamage",
		METHOD_CC,			0, CC_KNOCKUP, 1, 1, 1, 1, 1,
		3, 	METHOD_BURST|AD, 	1, 60, 95, 130, 165, 200, 1, "bonusattackdamage",
		METHOD_DPS, 		1, 1, 20, 31.66, 43.33, 55, 66.66, 0.33, "bonusattackdamage", "FlatPhysicalDamageMod",
		METHOD_SUSTAIN, 	1, 20, 25, 30, 35, 40, 0.75, "bonusattackdamage",
		2, 	METHOD_AOE_BURST|AP,2, 75, 110, 145, 180, 215, 0.6, "spelldamage", 0.6, "bonusattackdamage",
		METHOD_CC,			0, CC_SLOW, 0.4, 1.75, 0.4, 2, 0.4, 2.25, 0.4, 2.5, 0.4, 2.75,
		2, 	METHOD_AOE_BURST|AP,1, 200, 300, 400, 1, "spelldamage",
		METHOD_DPS,			0, 2, 0.4, 0.5, 0.6, "PercentAttackSpeedMod", 175, 175, 175, "RangeMod", 

		"Ahri",		1, 	METHOD_SUSTAIN, 	2, 0, 2, 1, "level", 0.09, "spelldamage",
		1,	METHOD_AOE_BURST|AP,1, 80, 130, 180, 230, 280, 0.7, "spelldamage",
		1,	METHOD_BURST|AP,	1, 64, 104, 144, 184, 224, 0.64, "spelldamage",
		2,	METHOD_BURST_AMP|AP,1, 60, 90, 120, 150, 180, 0.35, "spelldamage", 0.2, AMP_MAGIC,
		METHOD_CC,			0, CC_CHARM, 1, 1.25, 1.5, 1.75, 2,
		2,	METHOD_BURST|AP,	1, 210, 330, 450, 0.9, "spelldamage",
		METHOD_MOBILITY,	0, MOBI_DASH, 450, 450, 450,

		"Sivir",	1, 	METHOD_MOBILITY,	0, 5, MOBI_FLAT_MS, 1, 30, 6, 35, 11, 40, 16, 45, 18, 50,
		1,	METHOD_BURST|AD,	2, 46.25, 83.25, 120.25, 159.1, 194.25, 0.925, "spelldamage", 129.5, 148, 166.5, 185, 203.5, "attackdamage",
		0,
		0,
		2,	METHOD_MOBILITY,	0, MOBI_MSP, 0.6, 0.6, 0.6,
		METHOD_DPS,			0, 1, 0.4, 0.6, 0.8, "PercentAttackSpeedMod",

		// this is read as: Passive with 1 stat mod, giving bonus to 1 stat which has 0 level segments. The stat is 0 (base) + (9*(level-1)).
		//					And the stat is award to range
		"Tristana",	1,	METHOD_DPS,			1, 1, 0, 0, 9, "levelMinusOne", "RangeMod",
		1,	METHOD_DPS,			0, 1, 0.3, 0.45, 0.6, 0.75, 0.9, "PercentAttackSpeedMod",
		3,	METHOD_AOE_BURST|AP,1, 70, 115, 160, 205, 250, 0.8, "spelldamage",
		METHOD_MOBILITY,	0, MOBI_DASH, 900, 900, 900, 900, 900,
		METHOD_CC,			0, CC_SLOW, 0.6, 1, 0.6, 1.5, 0.6, 2, 0.6, 2.5, 0.6, 3,
		1,	METHOD_DOT_BURST|AP,1, 80, 125, 170, 215, 260, 1, "spelldamage",
		1,	METHOD_BURST|AP,	1, 300, 400, 500, 1.5, "spelldamage",
	};

	public static int getBaseMethod(int method) {
		return method & 0x00FFFFFF;
	}

	public static JSONObject loadJsonObj(String filename) throws JSONException, IOException {
		// Load the JSON object containing champ data first...
		URL url = Main.class.getClassLoader().getResource("res/" + filename);

		InputStream is = url.openStream();
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

		return new JSONObject(builder.toString());
	}

	public static void saveJsonObj(String filename, JSONObject obj) throws JSONException, IOException {
		OutputStream is = new FileOutputStream("out/" + filename);
		BufferedWriter br = new BufferedWriter(new OutputStreamWriter(is));

		br.write(obj.toString());
		br.close();
	}

	public static ChampionInfo completeChampionInfo(JSONObject champData) {
		ChampionInfo info = new ChampionInfo();
		try {
			StringBuilder builder = new StringBuilder();

			JSONArray skillsJson = champData.getJSONArray("spells");

			//DebugLog.d(TAG, "Data: " + champData.toString());
			//DebugLog.d(TAG, "Data: " + skills.toString());

			Skill[] skills = new Skill[5];

			String[] keys = {"q", "w", "e", "r"};

			skills[0] = new ChampionInfo.Passive();
			for (int i = 1; i < skills.length; i++) {
				skills[i] = new Skill();
				skills[i].defaultKey = keys[i - 1];
			}

			Passive passive = (Passive) skills[0];

			JSONObject p = champData.getJSONObject("passive");
			passive.desc = p.getString("description");
			passive.name = p.getString("name");
			passive.iconAssetName = p.getJSONObject("image").getString("full");

			JSONObject o;

			for (int i = 0; i < skillsJson.length(); i++) {
				builder.setLength(0);
				o = skillsJson.getJSONObject(i);
				Skill s = skills[i + 1];

				s.ranks = o.getInt("maxrank");
				s.id = o.getString("id");
				s.name = o.getString("name");
				s.desc = o.getString("tooltip");
				s.iconAssetName = o.getJSONObject("image").getString("full");
				s.rawEffect = o.getJSONArray("effect");
				s.rawEffectBurn = o.getJSONArray("effectBurn");

				if (o.has("rangeBurn")) {
					String range = o.getString("rangeBurn");
					if (!range.equals("self")) {
						builder.append("Range: ");
						builder.append(range);
						builder.append(' ');
					}
				}

				if (o.has("costBurn")) {
					String cost = o.getString("costBurn");
					if (!cost.equals("0")) {
						builder.append("Cost: ");
						builder.append(cost);
						builder.append(' ');
					}
				}

				if (o.has("cooldownBurn")) {
					String cd = o.getString("cooldownBurn");
					if (!cd.equals("0")) {
						builder.append("Cooldown: ");
						builder.append(cd);
						builder.append(' ');
					}
				}

				s.details = builder.toString();
			}

			info.lore = champData.getString("lore");
			JSONArray roles = champData.getJSONArray("tags");
			if (roles.length() == 1) {
				info.primRole = roles.getString(0);
			} else {
				info.primRole = roles.getString(0);
				info.secRole = roles.getString(1);
			}

			JSONObject stats = champData.getJSONObject("info");
			info.attack = stats.getInt("attack");
			info.defense = stats.getInt("defense");
			info.magic = stats.getInt("magic");
			info.difficulty = stats.getInt("difficulty");

			info.setSkills(skills);
			info.fullyLoaded();

		} catch (JSONException e) {
			e.printStackTrace();
		}

		return info;
	}

	public static double toDouble(Object o) {
		if (o instanceof Double) {
			return (Double) o;
		} else {
			return (Integer) o;
		}
	}

	public static void fixChampionInfo() throws IOException, JSONException {
		Object[] a = CHAMPION_SPELL_DAMAGE;
		for (int i = 0; i < a.length;) {
			String champKey = (String) a[i++];

			JSONObject o = loadJsonObj("champions/" + champKey + ".json");
			JSONObject champData = o.getJSONObject("data").getJSONObject(champKey);
			ChampionInfo info = completeChampionInfo(champData);

			JSONArray arr = new JSONArray();

			// this is a passive!
			JSONArray methods = new JSONArray();
			int methodCount = (Integer) a[i++];
			for (int j = 0; j < methodCount; j++) {
				JSONArray method = new JSONArray();
				i = makeMethod(null, a, i, method, true);

				methods.put(method);
			}

			arr.put(methods);

			// process other 4 skills
			for (int j = 0; j < 4; j++) {
				methods = new JSONArray();
				methodCount = (Integer) a[i++];

				for (int k = 0; k < methodCount; k++) {
					JSONArray method = new JSONArray();
					i = makeMethod(info.getSkill(j), a, i, method, false);

					methods.put(method);
				}

				arr.put(methods);
			}


			champData.put("analysis", arr);
			saveJsonObj(champKey + ".json", o);
		}
	}
	
	private static int parseScaling(Object[] a, int i, JSONArray method, int ranks) throws JSONException {
		double scaling = toDouble(a[i++]);
		Object next = a[i++];
		method.put(scaling);
		
		if (next instanceof String) {
			String type = (String) next;
			method.put(type);
		} else {
			scaling = toDouble(next);
			method.put(scaling);
			for (int j = 2; j < ranks; j++) {
				// this is a ranked based scaling 
				scaling = toDouble(a[i++]);
				method.put(scaling);
			}
			
			String type = (String) a[i++];
			method.put(type);
		}
		return i;
	}

	private static int makeMethod(Skill s, Object[] a, int i, JSONArray method, boolean passive) throws JSONException {
		try {
			int methodType = (Integer) a[i++];
			int scalings = (Integer) a[i++];

			method.put(methodType);
			method.put(scalings);

			int baseMethod = getBaseMethod(methodType);

			int dpsStats = 0;
			if (methodType == METHOD_DPS) {
				dpsStats = (Integer) a[i++];
				method.put(dpsStats);
			}

			int levelSegs = 0;
			int skillRanks = 0;
			boolean levelDivided = false;
			if (passive) {
				levelSegs = (Integer) a[i++];
				method.put(levelSegs);
				if (levelSegs == 0) {
					skillRanks = 1;
				} else {
					skillRanks = levelSegs;
					levelDivided = true;
				}
			} else {
				skillRanks = s.ranks;
			}


			switch (baseMethod) {
			case METHOD_DPS:
				for (int j = 0; j < dpsStats; j++) {
					for (int k = 0; k < skillRanks; k++) {
						if (levelDivided) {
							int level = (Integer) a[i++];
							method.put(level);
						}

						double bonus = toDouble(a[i++]);
						method.put(bonus);
					}
					for (int k = 0; k < scalings; k++) {
						double scaling = toDouble(a[i++]);
						String type = (String) a[i++];
						method.put(scaling);
						method.put(type);
					}
					String statType = (String) a[i++];
					method.put(statType);
				}
				break;
			case METHOD_SUSTAIN:
				for (int k = 0; k < skillRanks; k++) {
					if (levelDivided) {
						int level = (Integer) a[i++];
						method.put(level);
					}
					double bonus = toDouble(a[i++]);
					method.put(bonus);
				}

				for (int k = 0; k < scalings; k++) {
					double scaling = toDouble(a[i++]);
					String type = (String) a[i++];
					method.put(scaling);
					method.put(type);
				}
				break;
			case METHOD_BURST:
				for (int k = 0; k < skillRanks; k++) {
					if (levelDivided) {
						int level = (Integer) a[i++];
						method.put(level);
					}
					double bonus = toDouble(a[i++]);
					method.put(bonus);
				}

				for (int k = 0; k < scalings; k++) {
					i = parseScaling(a, i, method, skillRanks);
				}
				if ((methodType & METHOD_AMP) != 0) {
					double amp = toDouble(a[i++]);
					int ampType = (Integer) a[i++];
					method.put(amp);
					method.put(ampType);
				}
				break;
			case METHOD_CC:
				int ccType = (Integer) a[i++];
				int vals = 1;
				if (ccType == CC_SLOW) {
					vals = 2;
				}
				for (int k = 0; k < skillRanks; k++) {
					if (levelDivided) {
						int level = (Integer) a[i++];
						method.put(level);
					}
					for (int m = 0; m < vals; m++) {
						double v = toDouble(a[i++]);
						method.put(v);
					}
				}

				for (int k = 0; k < scalings; k++) {
					double scaling = (Double) a[i++];
					String type = (String) a[i++];
					method.put(scaling);
					method.put(type);
				}
				break;
			case METHOD_TANK:
				break;
			case METHOD_MOBILITY:
				int mobiType = (Integer) a[i++];
				method.put(mobiType);
				for (int k = 0; k < skillRanks; k++) {
					if (levelDivided) {
						int level = (Integer) a[i++];
						method.put(level);
					}
					double bonus = toDouble(a[i++]);
					method.put(bonus);
				}

				for (int k = 0; k < scalings; k++) {
					double scaling = (Double) a[i++];
					String type = (String) a[i++];
					method.put(scaling);
					method.put(type);
				}
				break;
			}
			return i;
		} catch (ClassCastException e) {
			p("i=" + i + "; a[i]=" + a[i]);
			e.printStackTrace();
			throw e;
		}
	}
}
