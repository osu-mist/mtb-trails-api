package edu.oregonstate.mist.trailsapi

import edu.oregonstate.mist.api.Application
import edu.oregonstate.mist.api.Configuration
import edu.oregonstate.mist.trailsapi.TrailsConfiguration
import edu.oregonstate.mist.trailsapi.db.TrailDAO
import io.dropwizard.setup.Environment
import io.dropwizard.jdbi.DBIFactory
import org.skife.jdbi.v2.DBI

/**
 * Main application class.
 */
class TrailsApplication extends Application<TrailsConfiguration> {
    /**
     * Parses command-line arguments and runs the application.
     *
     * @param configuration
     * @param environment
     */
    @Override
    public void run(TrailsConfiguration configuration, Environment environment) {
        this.setup(configuration, environment)
	   final DBIFactory FACTORY = new DBIFactory()
	   final DBI JDBI = FACTORY.build(environment, configuration.getDataSourceFactory(), "jdbi")
	   final TrailDAO DAO = JDBI.onDemand(TrailDAO.class)
	   //environment.jersey().register(new TrailResource(dao))
    }

    /**
     * Instantiates the application class with command-line arguments.
     *
     * @param arguments
     * @throws Exception
     */
    public static void main(String[] arguments) throws Exception {
        new TrailsApplication().run(arguments)
    }
}
