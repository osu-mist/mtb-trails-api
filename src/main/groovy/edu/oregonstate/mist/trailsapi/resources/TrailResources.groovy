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
        )
    }

    ResultObject trailResult(List<Trail> trails) {
        new ResultObject( data: trails.collect { trail -> trailResource(trail) })
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    Response postTrail (@Valid ResultObject newResultObject) {
        Trail trail
        Response response
        trail = (Trail)newResultObject.data.attributes
        if (!trailValidator(trail)) {
            //required field missing
            response = badRequest(
                "Required field missing or inavlid (name, zip code, or difficulty)").build()
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

    @GET
    Response getByQuery (@QueryParam("name") String name,
                         @QueryParam("difficulty") String difficulty,
                         @QueryParam("mostDifficult") String mostDifficult,
                         @QueryParam("leastDifficult") String leastDifficult,
                         @QueryParam("zipCode") Integer zipCode,
                         @QueryParam("smallDrop") Boolean smallDrop,
                         @QueryParam("largeDrop") Boolean largeDrop,
                         @QueryParam("woodRide") Boolean woodRide,
                         @QueryParam("skinny") Boolean skinny,
                         @QueryParam("largeJump") Boolean largeJump,
                         @QueryParam("smallJump") Boolean smallJump,
                         @QueryParam("gap") Boolean gap) {
        List<Trail> trails = trailDAO.getTrailByQuery(name, difficulty, mostDifficult,
            leastDifficult, zipCode, smallDrop,largeDrop, woodRide, skinny,
            largeJump, smallJump, gap)
        ok(trailResult(trails)).build()
    }

    @GET
    @Path ('/{id: \\d+}')
    Response getByID (@PathParam('id') Integer id) {
        Trail trail = trailDAO.getTrailByID(id)
        if (trail) {
            ok(trailResource(trail)).build()
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
            if (newResultObject && trailValidator((Trail)newResultObject.data.attributes)) {
                Trail trail = (Trail)newResultObject.data.attributes
                trailDAO.updateTrail(id, trail.name, trail.difficulty, trail.zipCode,
                    trail.smallDrop, trail.largeDrop, trail.woodRide, trail.skinny,
                    trail.largeJump, trail.smallJump, trail.gap)
                //Trail has been updated
                ok(trail).build()
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

    /**********************************************************************************************
    Function: trailValidator
    Description: Checks for validity of trail object
    Input: Trail object that is to be POST or PUT
    Output: Returns true if name, zip code, and difficulty are not null, and false otherwise
    **********************************************************************************************/
    Boolean trailValidator(Trail trail) {
        trail.name && trail.zipCode && trailDAO.difficultyValidator(trail.difficulty)
    }
}
