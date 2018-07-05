package edu.oregonstate.mist.trailsapi

import edu.oregonstate.mist.api.Application
import edu.oregonstate.mist.api.Configuration
import io.dropwizard.setup.Environment

/**
 * Main application class.
 */
class TrailsApplication extends Application<Configuration> {
    /**
     * Parses command-line arguments and runs the application.
     *
     * @param configuration
     * @param environment
     */
    @Override
    public void run(Configuration configuration, Environment environment) {
        this.setup(configuration, environment)
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
