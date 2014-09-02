import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class Main {
	public static void p(String p) {
		System.out.println(p);
	}

	public static void fixItemJson() throws IOException, JSONException {
		// Load the JSON object containing champ data first...
		URL url = Main.class.getClassLoader().getResource("res/item.json");

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

		//System.out.println(builder.toString());

		JSONObject itemData = new JSONObject(builder.toString());

		itemData = itemData.getJSONObject("data");
		//p(itemData.toString());
		
		Pattern p = Pattern.compile("\\+([0-9]+)% *Cooldown Reduction");

		Iterator<?> iter = itemData.keys();
		while (iter.hasNext()) {
			String key = (String) iter.next();
			JSONObject value = itemData.getJSONObject(key);

			String desc = value.getString("description");
			
			//p(desc);
			
			Matcher matcher = p.matcher(desc);
			if (matcher.find()) {
				//p(matcher.group(0));
				
				double d = Double.valueOf(matcher.group(1)) / 100;
				value.getJSONObject("stats").put("FlatCoolDownRedMod", d);
			}
			
		}
		
		p(itemData.toString());
	}

	public static void main(String[] args) {
		try {
			fixItemJson();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
