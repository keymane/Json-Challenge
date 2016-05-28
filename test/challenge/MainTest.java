package challenge;
import java.util.ArrayList;
import java.util.List;

import mockit.Expectations;

import org.testng.Assert;
import org.testng.annotations.Test;

import challenge.Main;
import challenge.model.Community;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class MainTest {

	String url = "https://raw.githubusercontent.com/onaio/ona-tech/master/data/water_points.json";

	// ================================================================================
	// Unit tests
	// ================================================================================

	@Test
	public void testNullUrlString() {
		JsonObject jsonObject = new Main().calculate(null);
		Assert.assertNotNull(jsonObject, "Object should not be null");
		String error = jsonObject.get("error").getAsString();
		Assert.assertEquals(error, "Unable To download Json");
	}

	@Test
	public void testEmptyUrlString() {
		JsonObject jsonObject = new Main().calculate("");
		Assert.assertNotNull(jsonObject, "Object should not be null");
		String error = jsonObject.get("error").getAsString();
		Assert.assertEquals(error, "Unable To download Json");
	}

	@Test
	public void testMalformedUrlString() {
		JsonObject jsonObject = new Main().calculate("ABsdfaxc");
		Assert.assertNotNull(jsonObject, "Object should not be null");
		String error = jsonObject.get("error").getAsString();
		Assert.assertEquals(error, "Unable To download Json");
	}

	@Test
	public void testProcessingJsonArrayWithoutCommunityData() {
		final Main main = new Main();
		final JsonArray jsonArray = new JsonArray();

		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("dummy", "1");
		jsonArray.add(jsonObject);

		jsonObject = new JsonObject();
		jsonObject.addProperty("dummy", "2");
		jsonArray.add(jsonObject);

		new Expectations(main) {
			{
				main.download(url);
				result = jsonArray;
			}
		};

		JsonObject results = main.calculate(url);
		Assert.assertNotNull(jsonObject, "Object should not be null");
		String error = results.get("error").getAsString();
		Assert.assertEquals(error, "Json retrieved doesn't have community data");
	}

	// ================================================================================
	// Integration tests
	// ================================================================================

	@Test
	public void testCorrectCommunityListIsReturned() {
		JsonArray jsonArray = new JsonArray();

		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("communities_villages", "village1");
		jsonObject.addProperty("water_functioning", "yes");
		jsonArray.add(jsonObject);

		jsonObject = new JsonObject();
		jsonObject.addProperty("communities_villages", "village2");
		jsonObject.addProperty("water_functioning", "yes");
		jsonArray.add(jsonObject);

		jsonObject = new JsonObject();
		jsonObject.addProperty("communities_villages", "village3");
		jsonObject.addProperty("water_functioning", "no");
		jsonArray.add(jsonObject);

		jsonObject = new JsonObject();
		jsonObject.addProperty("communities_villages", "village1");
		jsonObject.addProperty("water_functioning", "yes");
		jsonArray.add(jsonObject);

		jsonObject = new JsonObject();
		jsonObject.addProperty("communities_villages", "village2");
		jsonObject.addProperty("water_functioning", "no");
		jsonArray.add(jsonObject);

		List<Community> communities = new Main().process(jsonArray);

		Assert.assertFalse(communities.isEmpty(),
				"Community List should Not Be empty");

		Community community1 = communities.get(0);
		Assert.assertEquals(community1.getName(), "village1");
		Assert.assertEquals(community1.getTotalWaterPoints().intValue(), 2);
		Assert.assertEquals(community1.getBrokenWaterPoints().intValue(), 0);
		Assert.assertEquals(community1.getBrokenPercentage().intValue(), 0);

		Community community2 = communities.get(1);
		Assert.assertEquals(community2.getName(), "village2");
		Assert.assertEquals(community2.getTotalWaterPoints().intValue(), 2);
		Assert.assertEquals(community2.getBrokenWaterPoints().intValue(), 1);
		Assert.assertEquals(community2.getBrokenPercentage().intValue(), 50);

		Community community3 = communities.get(2);
		Assert.assertEquals(community3.getName(), "village3");
		Assert.assertEquals(community3.getTotalWaterPoints().intValue(), 1);
		Assert.assertEquals(community3.getBrokenWaterPoints().intValue(), 1);
		Assert.assertEquals(community3.getBrokenPercentage().intValue(), 100);

	}

	@Test
	public void testCorrectJsonObjectIsReturned() {

		List<Community> communities = new ArrayList<Community>();

		Community community1 = new Community();
		community1.setName("village1");
		community1.setTotalWaterPoints(10);
		community1.setBrokenWaterPoints(0);
		communities.add(community1);

		Community community2 = new Community();
		community2.setName("village2");
		community2.setTotalWaterPoints(7);
		community2.setBrokenWaterPoints(3);
		communities.add(community2);

		Community community3 = new Community();
		community3.setName("village3");
		community3.setTotalWaterPoints(5);
		community3.setBrokenWaterPoints(5);
		communities.add(community3);

		JsonObject jsonObject = new Main().toJson(communities);
		Assert.assertNotNull(jsonObject, "Object should not be null");

		Assert.assertEquals(jsonObject.get("number_functional").getAsInt(), 14);

		JsonArray jsonArray = jsonObject.get("number_water_points")
				.getAsJsonArray();

		JsonElement jsonElement = jsonArray.get(0);
		Assert.assertEquals(jsonElement.getAsJsonObject().get("name")
				.getAsString(), "village1");
		Assert.assertEquals(jsonElement.getAsJsonObject().get("count")
				.getAsInt(), 10);

		jsonElement = jsonArray.get(1);
		Assert.assertEquals(jsonElement.getAsJsonObject().get("name")
				.getAsString(), "village2");
		Assert.assertEquals(jsonElement.getAsJsonObject().get("count")
				.getAsInt(), 7);

		jsonElement = jsonArray.get(2);
		Assert.assertEquals(jsonElement.getAsJsonObject().get("name")
				.getAsString(), "village3");
		Assert.assertEquals(jsonElement.getAsJsonObject().get("count")
				.getAsInt(), 5);

		jsonArray = jsonObject.get("community_ranking").getAsJsonArray();

		jsonElement = jsonArray.get(0);
		Assert.assertEquals(jsonElement.getAsJsonObject().get("name")
				.getAsString(), "village3");
		Assert.assertEquals(jsonElement.getAsJsonObject().get("percentage")
				.getAsInt(), 100);

		jsonElement = jsonArray.get(1);
		Assert.assertEquals(jsonElement.getAsJsonObject().get("name")
				.getAsString(), "village2");
		Assert.assertEquals(jsonElement.getAsJsonObject().get("percentage")
				.getAsInt(), 42);

		jsonElement = jsonArray.get(2);
		Assert.assertEquals(jsonElement.getAsJsonObject().get("name")
				.getAsString(), "village1");
		Assert.assertEquals(jsonElement.getAsJsonObject().get("percentage")
				.getAsInt(), 0);

	}

	// ================================================================================
	// Functional tests
	// ================================================================================

	@Test
	public void testCorrectNumberOfFunctionWaterPointsIsReturned() {
		JsonObject jsonObject = new Main().calculate(url);
		Assert.assertNotNull(jsonObject, "Object should not be null");

		Assert.assertEquals(jsonObject.get("number_functional").getAsInt(), 623);
	}

	@Test
	public void testCorrectNumberOfWaterPointIsReturned() {
		JsonObject jsonObject = new Main().calculate(url);
		Assert.assertNotNull(jsonObject, "Object should not be null");

		JsonArray jsonArray = jsonObject.get("number_water_points")
				.getAsJsonArray();

		for (JsonElement jsonElement : jsonArray) {
			if (jsonElement.getAsJsonObject().get("name").getAsString()
					.equals("Tantala")) {
				Assert.assertEquals(jsonElement.getAsJsonObject().get("count")
						.getAsInt(), 22);
			}

			if (jsonElement.getAsJsonObject().get("name").getAsString()
					.equals("Kanwaasa")) {
				Assert.assertEquals(jsonElement.getAsJsonObject().get("count")
						.getAsInt(), 9);
			}

			if (jsonElement.getAsJsonObject().get("name").getAsString()
					.equals("Zundem")) {
				Assert.assertEquals(jsonElement.getAsJsonObject().get("count")
						.getAsInt(), 30);
			}

			if (jsonElement.getAsJsonObject().get("name").getAsString()
					.equals("Jiningsa-Yipaala")) {
				Assert.assertEquals(jsonElement.getAsJsonObject().get("count")
						.getAsInt(), 3);
			}

			if (jsonElement.getAsJsonObject().get("name").getAsString()
					.equals("Jiniensa")) {
				Assert.assertEquals(jsonElement.getAsJsonObject().get("count")
						.getAsInt(), 1);
			}

		}

	}

	@Test
	public void testCorrectRankingByBrokenWaterPoints() {
		JsonObject jsonObject = new Main().calculate(url);
		Assert.assertNotNull(jsonObject, "Object should not be null");

		JsonArray jsonArray = jsonObject.get("community_ranking")
				.getAsJsonArray();

		JsonElement jsonElement = jsonArray.get(0);
		Assert.assertEquals(jsonElement.getAsJsonObject().get("name")
				.getAsString(), "Zukpeni");
		Assert.assertEquals(jsonElement.getAsJsonObject().get("percentage")
				.getAsInt(), 66);

		jsonElement = jsonArray.get(5);
		Assert.assertEquals(jsonElement.getAsJsonObject().get("name")
				.getAsString(), "Soo");
		Assert.assertEquals(jsonElement.getAsJsonObject().get("percentage")
				.getAsInt(), 42);

		jsonElement = jsonArray.get(20);
		Assert.assertEquals(jsonElement.getAsJsonObject().get("name")
				.getAsString(), "Jagsa");
		Assert.assertEquals(jsonElement.getAsJsonObject().get("percentage")
				.getAsInt(), 15);

		jsonElement = jsonArray.get(jsonArray.size() - 1);
		Assert.assertEquals(jsonElement.getAsJsonObject().get("name")
				.getAsString(), "Jiniensa");
		Assert.assertEquals(jsonElement.getAsJsonObject().get("percentage")
				.getAsInt(), 0);

		jsonElement = jsonArray.get(jsonArray.size() - 37);
		Assert.assertEquals(jsonElement.getAsJsonObject().get("name")
				.getAsString(), "Dorinsa");
		Assert.assertEquals(jsonElement.getAsJsonObject().get("percentage")
				.getAsInt(), 5);

	}
}
