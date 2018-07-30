package edu.oregonstate.mist.trailsapi

import edu.oregonstate.mist.api.jsonapi.ResultObject
import edu.oregonstate.mist.api.jsonapi.ResourceObject
import edu.oregonstate.mist.trailsapi.core.Trail
import edu.oregonstate.mist.trailsapi.db.TrailDAO
import edu.oregonstate.mist.trailsapi.resources.TrailsResource

import groovy.mock.interceptor.StubFor
import org.junit.Test

class TrailsResourceTest {

    // Test POST trail
    @Test
    void testPost() {
        def mockDAO = new StubFor(TrailDAO)
        mockDAO.demand.getNextId() { -> 1 }
        mockDAO.demand.postTrail() { Integer id,
                                     String name,
                                     String difficulty,
                                     Integer zipCode,
                                     String polyline,
                                     Boolean smallDrop,
                                     Boolean largeDrop,
                                     Boolean woodRide,
                                     Boolean skinny,
                                     Boolean largeJump,
                                     Boolean smallJump,
                                     Boolean gap
                                     -> void
        }
        mockDAO.demand.difficultyValidator() { String difficulty -> true }
        mockDAO.demand.getConflictingTrails() { Trail t -> false }
        def dao = mockDAO.proxyInstance()
        TrailsResource resource = new TrailsResource(dao, null)

        // Test posting a trail with valid Parameters
        Trail validTrail = new Trail(
            name: "The Face",
            zipCode: 97330,
            difficulty: "Black",
            polyline: "fj348f9pjwejaeoi434344334kjllkofij34fo",
            largeDrop: false,
            smallDrop: true,
            woodRide: false,
            skinny: false,
            largeJump: false,
            smallJump: false,
            gap: false
        )
        def validPost = resource.postTrail(resource.trailResult(validTrail))
        validateResponse(validPost, 201, null, null)

        // Test posting a trail with null data
        ResultObject nullResult = new ResultObject( data: null)
        def nullPost = resource.postTrail(nullResult)
        validateResponse(nullPost, 400, 1400,
            "All data is null, or invalid data type for at least one field")

        // Test posting a trail with a required field missing
        Trail fieldMissingTrail = new Trail(
            zipCode: 97330,
            difficulty: "Black",
            polyline: "fj348f9pjwejaeoi434344334kjllkofij34fo",
            largeDrop: false,
            smallDrop: true,
            woodRide: false,
            skinny: false,
            largeJump: false,
            smallJump: false,
            gap: false
        )
        def fieldMissingPost = resource.postTrail(resource.trailResult(fieldMissingTrail))
        validateResponse(fieldMissingPost, 400, 1400,
            "Required field missing or inavlid (name, zip code, or difficulty)")

        // Test posting a trail that conflicts with an existing trails
        mockDAO = new StubFor(TrailDAO)
        mockDAO.demand.getNextId() { -> 1 }
        mockDAO.demand.postTrail() { Integer id,
                                     String name,
                                     String difficulty,
                                     Integer zipCode,
                                     String polyline,
                                     Boolean smallDrop,
                                     Boolean largeDrop,
                                     Boolean woodRide,
                                     Boolean skinny,
                                     Boolean largeJump,
                                     Boolean smallJump,
                                     Boolean gap
                                     -> void
        }
        mockDAO.demand.difficultyValidator() { String difficulty -> true }
        mockDAO.demand.getConflictingTrails() { Trail t -> true}
        dao = mockDAO.proxyInstance()
        resource = new TrailsResource(dao, null)
        def conflictingPost = resource.postTrail(resource.trailResult(validTrail))
        validateResponse(conflictingPost, 409, 1409, "Conflict - the request could not be processed"
            + " because of conflict in the request. Check the API call.")
    }

    // Test GET trail by Query
    @Test
    void testGetByQuery() {
        def mockDAO = new StubFor(TrailDAO)

        // Test with a full set of valid parameters
        mockDAO.demand.getTrailByQuery() { String name,
                                      String difficulty,
                                      String mostDifficult,
                                      String leastDifficult,
                                      Integer zipCode,
                                      Boolean smallDrop,
                                      Boolean largeDrop,
                                      Boolean woodRide,
                                      Boolean skinny,
                                      Boolean largeJump,
                                      Boolean smallJump,
                                      Boolean gap
                                      -> []
        }
        def dao = mockDAO.proxyInstance()
        TrailsResource resource = new TrailsResource(dao, null)
        def fullParameterGet = resource.getByQuery( "Non existent trail", "Black", "Black",
            "Black", 97330, true, false, false, false, false, false, false)
        validateResponse(fullParameterGet, 200, null, null)

        // Test with a partial set of valid parameters

        // Test with invalid parameters
    }

    void validateResponse(def response, Integer status, Integer code, String message) {
        if (status) {
            assert status == response.status
        }
        if (code) {
            assert code == response.entity.code
        }
        if (message) {
            assert message == response.entity.developerMessage
        }
    }
}
