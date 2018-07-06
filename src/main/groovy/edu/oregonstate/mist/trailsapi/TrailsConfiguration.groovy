package edu.oregonstate.mist.trailsapi

import edu.oregonstate.mist.api.Configuration
import com.fasterxml.jackson.annotation.JsonProperty
import io.dropwizard.db.DataSourceFactory
import javax.validation.Valid
import javax.validation.constraints.NotNull

public class TrailsConfiguration extends Configuration {
	@NotNull
	@JsonProperty("database")
	private DataSourceFactory database = new DataSourceFactory()

	@JsonProperty("database")
	public void setDataSourceFactory(DataSourceFactory factory) {
		this.database = factory
	}

	@JsonProperty("database")
	public DataSourceFactory getDataSourceFactory() {
		database
	}
}
