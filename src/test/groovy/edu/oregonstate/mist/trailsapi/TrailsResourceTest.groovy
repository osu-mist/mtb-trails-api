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
        // Test posting a trail with valid Parameters
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
        Trail validTrail = new Trail(
            name: "The Face",
            zipCode: 97330,
            difficulty: "Black")
        def validPost = resource.postTrail(resource.trailResult(validTrail))
        responseValidator(validPost, 201, null, null)

        // Test posting a trail with null data
        ResultObject nullResult = new ResultObject( data: null)
        def nullPost = resource.postTrail(nullResult)
        responseValidator(nullPost, 400, 1400,
            "All data is null, or invalid data type for at least one field")

        // Test posting a trail with a required field missing
        Trail fieldMissingTrail = new Trail(
            zipCode: 97330,
            difficulty: "Black")
        def fieldMissingPost = resource.postTrail(resource.trailResult(fieldMissingTrail))
        responseValidator(fieldMissingPost, 400, 1400,
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
        responseValidator(conflictingPost, 409, 1409, "Conflict - the request could not be processed"
            + " because of conflict in the request. Check the API call.")
    }

    // Test GET trail by query
    @Test
    void testGetByQuery() {
        // Test with a full set of valid parameters
        def mockDAO = new StubFor(TrailDAO)
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
        mockDAO.demand.difficultyValidator(3..3) { String d -> true }
        def dao = mockDAO.proxyInstance()
        TrailsResource resource = new TrailsResource(dao, null)
        def fullParameterGet = resource.getByQuery( "Non existent trail", "Black", "Black",
            "Black", "97330", true, false, false, false, false, false, false)
        responseValidator(fullParameterGet, 200, null, null)

        // Test with a partial set of valid parameters
        mockDAO.demand.difficultyValidator() { String d -> true }
        dao = mockDAO.proxyInstance()
        resource = new TrailsResource(dao, null)
        def partialParameterGet = resource.getByQuery("Non existent trail", "Black", null,
            null, null, true, false, null, null, null, null, null)
        responseValidator(partialParameterGet, 200, null, null)

        // Test with invalid zip code
        dao = mockDAO.proxyInstance()
        resource = new TrailsResource(dao, null)
        def invalidZipCodeGet = resource.getByQuery( "Non existent trail", "Black", null,
            null, "nine seven seven zero two", true, false, null, null, null, null, null)
        responseValidator(invalidZipCodeGet, 400, 1400,
            "zipCode invalid - must be in form of 12345 or 12345-6789")

        // Test with invalid difficulty
        mockDAO = new StubFor(TrailDAO)
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
        mockDAO.demand.difficultyValidator() { String d -> false}
        dao = mockDAO.proxyInstance()
        resource = new TrailsResource(dao, null)
        def invalidDifficultyGet = resource.getByQuery( "Non existent trail", "Gnarly", null,
            null, "97330", true, false, null, null, null, null, null)
        responseValidator(invalidDifficultyGet, 400, 1400,
            "difficulty invalid - consult API documentation for valid difficulties")
    }

    // Test GET trail by ID
    @Test
    void testGetByID() {
        // Test with existing ID
        def mockDAO = new StubFor(TrailDAO)
        Trail trail = new Trail( id: 1,
                                 name: "Test Trail",
                                 zipCode: 97330,
                                 difficulty: "Green")
        mockDAO.demand.getTrailByID() { Integer id -> trail }
        def dao = mockDAO.proxyInstance()
        TrailsResource resource = new TrailsResource(dao, null)
        def existingIDGet = resource.getByID(1)
        responseValidator(existingIDGet, 200, null, null)

        // Test with non-existent ID
        mockDAO = new StubFor(TrailDAO)
        mockDAO.demand.getTrailByID() { Integer id -> null }
        dao = mockDAO.proxyInstance()
        resource = new TrailsResource(dao, null)
        def nonExistentIDGet = resource.getByID(4635)
        responseValidator(nonExistentIDGet, 404, 1404,
            "Not Found - the resource requested was not found. Check the API call.")
    }

    // Test PUT trails
    @Test
    void testPut() {
        // Test with non-existent ID
        def mockDAO = new StubFor(TrailDAO)
        mockDAO.demand.getTrailByID() { Integer id -> null }
        def dao = mockDAO.proxyInstance()
        TrailsResource resource = new TrailsResource(dao, null)
        Trail validTrail = new Trail( id: 1,
                                 name: "Test Trail",
                                 zipCode: 97330,
                                 difficulty: "Green")
        def nonExistentIDPut = resource.putTrail(4635, resource.trailResult(validTrail))
        responseValidator(nonExistentIDPut, 404, 1404,
            "Not Found - the resource requested was not found. Check the API call.")

        // Test with valid trail and existing ID
        mockDAO = new StubFor(TrailDAO)
        mockDAO.demand.getTrailByID() { Integer id -> validTrail }
        mockDAO.demand.difficultyValidator() { String d -> true }
        mockDAO.demand.updateTrail() { Integer i,
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
        dao = mockDAO.proxyInstance()
        resource = new TrailsResource(dao, null)
        def validPut = resource.putTrail(1, resource.trailResult(validTrail))
        responseValidator(validPut, 200, null, null)

        // Test with null data
        dao = mockDAO.proxyInstance()
        resource = new TrailsResource(dao, null)
        ResultObject nullResult = new ResultObject( data: null)
        def nullPut = resource.putTrail(1, nullResult)
        responseValidator(nullPut, 400, 1400,
            "All data is null, or invalid data type for at least one field")

        // Test posting a trail with a required field missing
        dao = mockDAO.proxyInstance()
        resource = new TrailsResource(dao, null)
        Trail fieldMissingTrail = new Trail(
                zipCode: 97330,
                difficulty: "Black")
        def fieldMissingPut = resource.putTrail(1, resource.trailResult(fieldMissingTrail))
            responseValidator(fieldMissingPut, 400, 1400,
                "Required field missing or inavlid (name, zip code, or difficulty)")
    }

    /**********************************************************************************************
    Function: responseValidator
    Description: Checks equality of status, code, and message between expected and tested response
    Input: Tested response, and expected status, code, and message
    Output: Returns true if all expected fields match the test response, and false otherwise
    **********************************************************************************************/
    void responseValidator(def response, Integer status, Integer code, String message) {
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
