package com.ggstudios.lolcraft;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ggstudios.utils.DebugLog;

import android.graphics.drawable.Drawable;

public class ChampionInfo {
	int id;
	Drawable icon;
	String name;
	String title;
	String key;

	double hp;
	double hpG;
	double hpRegen;
	double hpRegenG;
	double ms;
	double mp;
	double mpG;
	double mpRegen;
	double mpRegenG;
	double range;
	double ad;
	double adG;
	double as;
	double asG;
	double ar;
	double arG;
	double mr;
	double mrG;

	boolean skillsLoaded = false;

	public void getSkills(final OnSkillsLoadedListener listener) {
		if (skills == null) {
			new Thread() {
				@Override
				public void run() {
					synchronized(skillLock) {
						if (skills == null) {
							try {
								skillLock.wait();
							} catch (InterruptedException e) {}

							listener.onSkillsLoaded(skills);
						}
					}
				}
			}.start();
		} else {
			listener.onSkillsLoaded(skills);
		}

	}

	public void setSkills(Skill[] skills) {
		synchronized(skillLock) {
			this.skills = skills;
			skillLock.notifyAll();
		}
	}

	private Object skillLock = new Object();
	private Skill[] skills;
	private Passive passive;

	public static class Passive extends Skill {
		
	}
	
	public static class Skill {
		private static final String TAG = "Skill";

		String id;
		String name;
		String desc;
		String details;
		Map<String, Scaling> varToScaling = new HashMap<String, Scaling>();
		Drawable icon;

		String iconAssetName;

		JSONArray rawEffect;
		JSONArray rawEffectBurn;
		String completedDesc;
		String scaledDesc;

		String[] effects;

		private static Pattern argPattern = Pattern.compile("\\{\\{ ([a-z][0-9]+) \\}\\}");

		private String getEffect(int index) {
			if (effects == null) {
				try {
					StringBuilder sb = new StringBuilder();

					effects = new String[rawEffect.length()];
					for (int i = 0; i < rawEffect.length(); i++) {
						sb.setLength(0);
						JSONArray arr = rawEffect.optJSONArray(i);
						if (arr != null) {
							for (int j = 0; j < arr.length(); j++) {
								sb.append(arr.getString(j));
								if (j != arr.length() - 1) {
									sb.append(" / ");
								}
							}

							effects[i] = sb.toString();
						}

					}

				} catch (JSONException e) {
					DebugLog.e(TAG, e);
				}
			}

			return effects[index];
		}

		public String getCompletedDesc() {
			if (completedDesc == null) {
				String d;
				d = desc.replaceAll("<span class=\"color([a-fA-F\\d]{6})\">(.*?)</span>",
						"<font color=\"#$1\">$2</font>");
				d = ChampionInfo.themeHtml(d);

				Matcher matcher = argPattern.matcher(d);

				StringBuffer sb = new StringBuffer();
				while(matcher.find()) {
					String match = matcher.group(1);
					char argType = match.charAt(0);
					int i = Integer.valueOf(match.substring(1));

					switch (argType) {
					case 'e':
						try {
							matcher.appendReplacement(sb, rawEffectBurn.getString(i));
						} catch (JSONException e) {
							DebugLog.e(TAG, e);
						}
						break;
					default:
						break;
					}
				}
				matcher.appendTail(sb);

				completedDesc = sb.toString();
			}

			return completedDesc;
		}

		public String calculateScaling(Build build, DecimalFormat format) {
			Matcher matcher = argPattern.matcher(getCompletedDesc());

			StringBuffer sb = new StringBuffer();
			while(matcher.find()) {
				String match = matcher.group(1);

				Scaling sc = varToScaling.get(match);

				if (sc != null) {
					if (sc.coeff instanceof Double) {
						double d = (build.getStat(sc.link) * (Double)sc.coeff);
						matcher.appendReplacement(sb, format.format(d));
					} else if (sc.coeff instanceof JSONArray) {
						JSONArray arr = (JSONArray) sc.coeff;

						try {
							StringBuilder sb2 = new StringBuilder();
							for (int i = 0; i < arr.length(); i++) {
								sb2.append(arr.getDouble(i));
								if (i != arr.length() - 1)
									sb2.append(" / ");
							}
							
							matcher.appendReplacement(sb, sb2.toString());
							
						} catch (JSONException e) {
							DebugLog.e(TAG, e);
						}
					}
				}
			}
			matcher.appendTail(sb);

			scaledDesc = sb.toString();

			return scaledDesc;
		}

		public String getScaledDesc() {
			return scaledDesc;
		}
	}

	public static class Scaling {
		String var;
		Object coeff;
		String link;
	}

	public static interface OnSkillsLoadedListener {
		public void onSkillsLoaded(Skill[] skills);
	}

	private static Map<String, String> darkThemeToLightThemeMap = new HashMap<String, String>();
	private static Pattern pattern;


	static {
		String patternString = "[A-F0-9]{6}";
		pattern = Pattern.compile(patternString);

		Map<String, String> m = darkThemeToLightThemeMap;
		m.put("0000FF", "0000FF");
		m.put("00DD33", "00DD33");
		m.put("33FF33", "33FF33");
		m.put("44DDFF", "44DDFF");
		m.put("5555FF", "5555FF");
		m.put("6655CC", "6655CC");
		m.put("88FF88", "88FF88");
		m.put("99FF99", "99CC00");
		m.put("CC3300", "CC3300");
		m.put("CCFF99", "CCFF99");
		m.put("DDDD77", "DDDD77");
		m.put("EDDA74", "EDDA74");
		m.put("F50F00", "F50F00");
		m.put("F88017", "F88017");
		m.put("FF0000", "FF0000");
		m.put("FF00FF", "FF00FF");
		m.put("FF3300", "FF3300");
		m.put("FF6633", "FF6633");
		m.put("FF8C00", "FF8C00");
		m.put("FF9900", "FF9900");
		m.put("FF9999", "FF9999");
		m.put("FFAA33", "FFAA33");
		m.put("FFD700", "FFD700");
		m.put("FFDD77", "FFDD77");
		m.put("FFF673", "CCC55C");	// light yellow... make it darker yellow
		m.put("FFFF00", "F1C40F");	// yellow illegible on white bg... so use a more orangy yellow
		m.put("FFFF33", "FFFF33");
		m.put("FFFF99", "FFFF99");
		m.put("FFFFFF", "000000");
	}

	public static String convertDarkThemeColorToLight(String color) {
		return darkThemeToLightThemeMap.get(color);
	}

	public static String themeHtml(String htmlString) {
		Matcher matcher = pattern.matcher(htmlString);

		StringBuffer sb = new StringBuffer();
		while(matcher.find()) {
			matcher.appendReplacement(sb, convertDarkThemeColorToLight(matcher.group(0)));
		}
		matcher.appendTail(sb);

		return sb.toString();
	}
}
