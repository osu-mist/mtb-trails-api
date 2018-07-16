package edu.oregonstate.mist.trailsapi.resources

import edu.oregonstate.mist.trailsapi.core.Trail
import edu.oregonstate.mist.trailsapi.db.TrailDAO
import edu.oregonstate.mist.api.Resource
import edu.oregonstate.mist.api.jsonapi.ResourceObject
import edu.oregonstate.mist.api.jsonapi.ResultObject
import io.dropwizard.jersey.params.IntParam
import com.google.common.base.Optional
import org.slf4j.LoggerFactory

import javax.ws.rs.DELETE
import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.PUT
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.Response
import javax.ws.rs.core.Response.ResponseBuilder
import javax.ws.rs.core.MediaType
import javax.ws.rs.QueryParam
import javax.validation.Valid
import javax.ws.rs.Consumes
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.skife.jdbi.v2.DBI

@Path("/trails")
@Produces(MediaType.APPLICATION_JSON)
public class TrailsResource extends Resource {
	private final TrailDAO trailDAO
	private DBI dbi

	TrailsResource(TrailDAO trailDAO, DBI dbi) {
	    this.trailDAO = trailDAO
	    this.dbi = dbi
	}

	ResourceObject trailResource(Trail trail) {
	    new ResourceObject(
			  id: trail.id,
			  type: 'Trail',
			  attributes: trail,
			  links: null
	    )
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	Response postTrail (@Valid ResultObject newResultObject) {
		Trail trail
		Response response
		trail = (Trail)newResultObject.data.attributes
		if (!trailValidator(trail)) {
				//required field missing
				response = badRequest("Required field missing (name, zip code, or difficulty)").build()
		} else {
			Boolean conflictingTrails = trailDAO.getConflictingTrails(trail)
				if (!conflictingTrails) {
						Integer id = trailDAO.getNextId()
						trail.id = id
						trailDAO.postTrail(trail)
						//trail object created
						response = created(trailResource(trail)).build()
				} else {
					//Trail already exists
					response = conflict().build()
				}
		}
		response
	}

	/*************************************************************************************************
	Function: trailValidator
	Description: Checks for validity of trail object
	Input: Trail object that is to be POST or PUT
	Output: Returns true if name, zip code, and difficulty are not null, and false otherwise
	*************************************************************************************************/
	Boolean trailValidator(Trail trail) {
		trail.name && trail.zipCode && trail.difficulty
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	Response getTrail (@QueryParam("name") Optional<String> name,
					@QueryParam("difficulty") Optional<String> difficulty,
					@QueryParam("mostDifficult") Optional<String> mostDifficult,
					@QueryParam("leastDifficult") Optional<String> leastDifficult,
					@QueryParam("zipCode") Optional<Integer> zipCode,
					@QueryParam("smallDrop") Optional<Boolean> smallDrop,
					@QueryParam("largeDrop") Optional<Boolean> largeDrop,
					@QueryParam("woodRide") Optional<Boolean> woodRide,
					@QueryParam("skinny") Optional<Boolean> skinny,
					@QueryParam("largeJump") Optional<Boolean> largeJump,
					@QueryParam("smallJump") Optional<Boolean> smallJump,
					@QueryParam("gap") Optional<Boolean> gap) {
			List<Trail> trails = trailDAO.getTrailByQuery(name.or(""), difficulty.or(""),
				mostDifficult.or(""), leastDifficult.or(""), zipCode.orNull(), smallDrop.orNull(),
				largeDrop.orNull(), woodRide.orNull(), skinny.orNull(), largeJump.orNull(), smallJump.orNull(),
				gap.orNull())
			ok(trails).build()
		}
}
