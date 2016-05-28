package challenge;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import challenge.model.Community;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Main {
	Logger logger = Logger.getAnonymousLogger();

	public static void main(String[] args) {
		JsonObject jsonObject = new Main()
				.calculate("https://raw.githubusercontent.com/onaio/ona-tech/master/data/water_points.json");
		Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls()
				.setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
				.create();
		String result = gson.toJson(jsonObject);
		System.out.println("Results: " + result);
	}

	public JsonObject calculate(String urlString) {

		JsonArray jsonArray = download(urlString);
		if (jsonArray == null) {
			JsonObject errorJson = new JsonObject();
			errorJson.addProperty("error", "Unable To download Json");
			return errorJson;
		}

		List<Community> communityList = process(jsonArray);
		if (communityList.isEmpty()) {
			JsonObject errorJson = new JsonObject();
			errorJson.addProperty("error",
					"Json retrieved doesn't have community data");
			return errorJson;
		}

		return toJson(communityList);
	}

	public JsonArray download(String urlString) {
		try {

			if (urlString == null || urlString.isEmpty()) {
				return null;
			}

			URL url = new URL(urlString);

			HttpURLConnection request = (HttpURLConnection) url
					.openConnection();
			request.connect();
			InputStream in = (InputStream) request.getContent();

			JsonParser jp = new JsonParser();
			JsonElement root = jp.parse(new InputStreamReader(in));
			return root.getAsJsonArray();

		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception Thrown: ", e);
		}
		return null;
	}

	public List<Community> process(JsonArray jsonArray) {
		try {
			List<Community> communityList = new ArrayList<Community>();
			for (JsonElement jsonElement : jsonArray) {

				boolean functioning = false;
				boolean isNew = false;

				JsonObject jsonObject = jsonElement.getAsJsonObject();
				String waterFunctioning = jsonObject.get("water_functioning")
						.getAsString();

				if (waterFunctioning.equals("yes")) {
					functioning = true;
				}

				String name = jsonObject.get("communities_villages")
						.getAsString();

				Community community = getCommunity(communityList, name);

				if (community == null) {
					isNew = true;
					community = new Community();
					community.setName(name);
				}

				community
						.setTotalWaterPoints(community.getTotalWaterPoints() + 1);

				if (!functioning)
					community.setBrokenWaterPoints(community
							.getBrokenWaterPoints() + 1);

				if (isNew)
					communityList.add(community);

			}

			return communityList;

		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception Thrown: ", e);
		}
		return new ArrayList<Community>();
	}

	public JsonObject toJson(List<Community> communityList) {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("number_functional",
				getAllFunctionalWaterpoints(communityList));

		JsonArray waterPointsArray = new JsonArray();
		JsonArray brokenPercentageArray = new JsonArray();
		for (Community community : communityList) {
			JsonObject waterPoints = new JsonObject();
			waterPoints.addProperty("name", community.getName());
			waterPoints.addProperty("count", community.getTotalWaterPoints());
			waterPointsArray.add(waterPoints);
		}

		jsonObject.add("number_water_points", waterPointsArray);

		Collections.sort(communityList, new Comparator<Community>() {
			@Override
			public int compare(Community o1, Community o2) {
				return o2.getBrokenPercentage().compareTo(
						o1.getBrokenPercentage());
			}
		});

		for (int i = 0; i < communityList.size(); i++) {
			Community community = communityList.get(i);

			JsonObject brokenPercentage = new JsonObject();
			brokenPercentage.addProperty("name", community.getName());
			brokenPercentage.addProperty("percentage",
					community.getBrokenPercentage());
			brokenPercentageArray.add(brokenPercentage);
		}
		jsonObject.add("community_ranking", brokenPercentageArray);

		return jsonObject;

	}

	private Community getCommunity(List<Community> list, String name) {
		if (list == null || name == null) {
			return null;
		}

		for (Community community : list) {
			if (community.getName().equals(name)) {
				return community;
			}
		}
		return null;
	}

	private Integer getAllFunctionalWaterpoints(List<Community> list) {
		if (list == null) {
			return 0;
		}

		int count = 0;
		for (Community community : list) {
			count += community.getFunctionalWaterPoints();
		}
		return count;
	}
}
