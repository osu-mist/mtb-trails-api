package edu.oregonstate.mist.trailsapi.resources

import edu.oregonstate.mist.trailsapi.core.Trail
import edu.oregonstate.mist.trailsapi.db.TrailDAO
import edu.oregonstate.mist.api.Resource
import edu.oregonstate.mist.api.jsonapi.ResourceObject
import edu.oregonstate.mist.api.jsonapi.ResultObject
import io.dropwizard.jersey.params.IntParam
import com.google.common.base.Optional
import org.slf4j.LoggerFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.skife.jdbi.v2.DBI
import java.util.regex.Pattern

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
        )
    }

    ResultObject trailResult(Trail trail) {
        new ResultObject( data: trailResource(trail) )
    }

    ResultObject trailsResult(List<Trail> trails) {
        new ResultObject( data: trails.collect { trail -> trailResource(trail) })
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    Response postTrail (@Valid ResultObject newResultObject) {
        Trail trail
        try {
            trail = (Trail)newResultObject.data.attributes
        } catch (Exception e) {
            return badRequest(
                "All data is null, or invalid data type for at least one field").build()
        }
        if ((!trail) || !trailValidator(trail)) {
            //required field missing
            badRequest(
                "Required field missing or inavlid (name, zip code, or difficulty)").build()
        } else {
            Boolean conflictingTrails = trailDAO.getConflictingTrails(trail)
            if (!conflictingTrails) {
                Integer id = trailDAO.getNextId()
                trail.id = id
                trailDAO.postTrail(trail.id, trail.name, trail.difficulty, trail.zipCode,
                    trail.polyline, trail.smallDrop, trail.largeDrop, trail.woodRide,
                    trail.skinny, trail.largeJump, trail.smallJump, trail.gap)
                //trail object created
                created(trailResult(trail)).build()
            } else {
                //Trail already exists
                conflict().build()
             }
        }
    }

    @GET
    Response getByQuery (@QueryParam("name") String name,
                         @QueryParam("difficulty") String difficulty,
                         @QueryParam("mostDifficult") String mostDifficult,
                         @QueryParam("leastDifficult") String leastDifficult,
                         @QueryParam("zipCode") String zipCode,
                         @QueryParam("smallDrop") Boolean smallDrop,
                         @QueryParam("largeDrop") Boolean largeDrop,
                         @QueryParam("woodRide") Boolean woodRide,
                         @QueryParam("skinny") Boolean skinny,
                         @QueryParam("largeJump") Boolean largeJump,
                         @QueryParam("smallJump") Boolean smallJump,
                         @QueryParam("gap") Boolean gap) {
        String invalidParameter = parameterValidator(difficulty, mostDifficult,
            leastDifficult, zipCode)
        if (invalidParameter) {
            return badRequest(invalidParameter).build()
        }
        Integer zipCodeInteger
        if (zipCode) {
            zipCodeInteger = zipCode.toInteger()
        }
        List<Trail> trails = trailDAO.getTrailByQuery(name, difficulty, mostDifficult,
            leastDifficult, zipCodeInteger, smallDrop,largeDrop, woodRide, skinny,
            largeJump, smallJump, gap)
        ok(trailsResult(trails)).build()
    }

    @GET
    @Path ('/{id: \\d+}')
    Response getByID (@PathParam('id') Integer id) {
        Trail trail = trailDAO.getTrailByID(id)
        if (trail) {
            trail.id = id
            ok(trailResult(trail)).build()
        } else {
            notFound().build()
        }
    }

    @PUT
    @Path ('/{id: \\d+}')
    @Consumes(MediaType.APPLICATION_JSON)
    Response putTrail (@PathParam('id') Integer id, @Valid ResultObject newResultObject) {
        Trail currentTrail = trailDAO.getTrailByID(id)
        if (currentTrail) {
            Trail trail
            try {
                trail = (Trail)newResultObject.data.attributes
            } catch (Exception e) {
                return badRequest(
                    "All data is null, or invalid data type for at least one field").build()
            }
            if (trail && trailValidator(trail)) {
                trailDAO.updateTrail(id, trail.name, trail.difficulty, trail.zipCode,
                    trail.polyline, trail.smallDrop, trail.largeDrop, trail.woodRide,
                    trail.skinny, trail.largeJump, trail.smallJump, trail.gap)
                //Trail has been updated
                trail.id = id
                ok(trailResult(trail)).build()
            } else {
                //Body data is missing or trail is not valid
                badRequest(
                    "Required field missing or inavlid (name, zip code, or difficulty)").build()
            }
        } else {
            //No trail at ID exists
            return notFound().build()
        }
    }

    @DELETE
    @Path ('/{id: \\d+}')
    Response deleteTrail (@PathParam('id') Integer id) {
        Trail trail = trailDAO.getTrailByID(id)
        if (trail) {
            trailDAO.deleteTrail(id)
            ok().build()
        } else {
            notFound().build()
        }
    }

    /**********************************************************************************************
    Function: zipCodeValidator
    Description: Checks for validity of a zip code
    Input: String containing zip code
    Output: Returns true valid zip code, false otherwise
    **********************************************************************************************/
    Boolean zipCodeValidator(String zipCode) {
        String regex = '^[0-9]{5}(?:-[0-9]{4})?$'
        Pattern pattern = Pattern.compile(regex)
        pattern.matcher(zipCode).matches()
    }

    /**********************************************************************************************
    Function: trailValidator
    Description: Checks for validity of trail object
    Input: Trail object that is to be POST or PUT
    Output: Returns true if name, zip code, and difficulty are not null, and false otherwise
    **********************************************************************************************/
    Boolean trailValidator(Trail trail) {
        trail.name && trail.zipCode && zipCodeValidator(trail.zipCode.toString()) &&
            trailDAO.difficultyValidator(trail.difficulty)
    }

    /**********************************************************************************************
    Function: parameterValidator
    Description: Checks for validity of query parameters
    Input: Query parameters that could be invalid
    Output: Returns true if all parameters are valid in type or content if applicable, and false
        otherwise
    **********************************************************************************************/
    String parameterValidator(String difficulty, String mostDifficult,
        String leastDifficult, String zipCode) {
            if (difficulty && !trailDAO.difficultyValidator(difficulty)) {
                return "difficulty invalid"
            }
            if (mostDifficult && !trailDAO.difficultyValidator(mostDifficult)) {
                return "mostDifficult invalid"
            }
            if (leastDifficult && !trailDAO.difficultyValidator(leastDifficult)) {
                return "leastDifficult invalid"
            }
            if (zipCode && !zipCodeValidator(zipCode)) {
                return "zipCode invalid"
            }
        ""
    }
}
