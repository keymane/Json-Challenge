package challenge.model;
public class Community {
	String name;

	Integer totalWaterPoints;

	Integer brokenWaterPoints;

	Integer brokenPercentage;

	public Community() {
		totalWaterPoints = 0;
		brokenWaterPoints = 0;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getTotalWaterPoints() {
		return totalWaterPoints;
	}

	public void setTotalWaterPoints(Integer totalWaterPoints) {
		this.totalWaterPoints = totalWaterPoints;
	}

	public Integer getBrokenWaterPoints() {
		return brokenWaterPoints;
	}

	public void setBrokenWaterPoints(Integer brokenWaterPoints) {
		this.brokenWaterPoints = brokenWaterPoints;
	}

	public Integer getBrokenPercentage() {
		if (this.brokenWaterPoints.intValue() == 0) {
			return 0;
		}

		return new Double(this.brokenWaterPoints * 100.0
				/ this.totalWaterPoints).intValue();
	}

	public Integer getFunctionalWaterPoints() {
		return totalWaterPoints - brokenWaterPoints;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Community)) {
			return false;
		}

		Community testCommunity = (Community) obj;

		if (testCommunity.getName() == null || this.getName() == null) {
			return false;
		}

		return this.getName().equals(testCommunity.getName());
	}
}
