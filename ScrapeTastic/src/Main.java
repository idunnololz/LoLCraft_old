import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class Main {
	private static final String[][] SPECIAL_NAMES = {
		{"ChoGath", "Chogath"},
		{"Fiddlesticks", "FiddleSticks"},
		{"KhaZix", "Khazix"},
		{"LeBlanc", "Leblanc"},
		{"VelKoz", "Velkoz"},
		{"Wukong", "MonkeyKing"},
	};
	
	public static void main(String[] args) {
		try {
			// Load the JSON object containing champ data first...
			URL url = Main.class.getClassLoader().getResource("res/champion.json");

			InputStream is = url.openStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));

			StringBuilder builder = new StringBuilder();
			String readLine = null;

			// While the BufferedReader readLine is not null 
			while ((readLine = br.readLine()) != null) {
				builder.append(readLine);
			}

			System.out.println(builder.toString());

			JSONObject o = new JSONObject(builder.toString());

			o = o.getJSONObject("data");



			Document doc = Jsoup.connect("http://leagueoflegends.wikia.com/wiki/Base_champion_statistics").get();
			Elements tables = doc.select("table.wikitable");
			Element dataTable = tables.get(0);

			// For some reason select tbody doesn't do anything... so just ignore first row...
			Elements trs = dataTable.select("tr");

			for (int i = 1; i < trs.size(); i++) {
				Elements cols = trs.get(i).select("td");

				String n = cols.get(0).select("a").get(1).text().replace("'", "").replace(".", "").replace(" ", "");
				
				for (String[] s : SPECIAL_NAMES) {
					if (n.equals(s[0])) {
						n = s[1];
					}
				}
				JSONObject champ = o.getJSONObject(n);
				champ.put("hp", cols.get(1).text());
				champ.put("hp+", cols.get(2).text());
				champ.put("hp5", cols.get(3).text());
				champ.put("hp5+", cols.get(4).text());
				champ.put("mp", cols.get(5).text());
				champ.put("mp+", cols.get(6).text());
				champ.put("mp5", cols.get(7).text());
				champ.put("mp5+", cols.get(8).text());
				champ.put("ad", cols.get(9).text());
				champ.put("ad+", cols.get(10).text());
				champ.put("as", cols.get(11).text());
				champ.put("as+", cols.get(12).text());
				champ.put("ar", cols.get(13).text());
				champ.put("ar+", cols.get(14).text());
				champ.put("mr", cols.get(15).text());
				champ.put("mr+", cols.get(16).text());
				champ.put("ms", cols.get(17).text());
				champ.put("range", cols.get(18).text());
			}


			System.out.println(o);

		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
