package edu.oregonstate.mist.trailsapi

import edu.oregonstate.mist.api.jsonapi.ResultObject
import edu.oregonstate.mist.trailsapi.core.Trail
import edu.oregonstate.mist.trailsapi.db.TrailDAO
import edu.oregonstate.mist.trailsapi.resources.TrailsResource

import javax.ws.rs.core.Response
import groovy.mock.interceptor.StubFor
import org.junit.Test

class TrailsResourceTest {
    // Test POST trail
    @Test
    void testPostTrail() {
        Trail trail = new Trail(
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

        def mockDAO = new StubFor(TrailDAO)
        mockDAO.demand.getNextID() { -> 1 }
        mockDAO.demand.postTrail() {}
        mockDAO.demand.difficultyValidator() { -> true }
        mockDAO.demand.getConflictingTrails() { -> false }
        def dao = mockDAO.proxyInstance()
        TrailsResource resource = new TrailsResource(dao, null)

        // Test posting a card with valid Parameters
        def validTrailPost = resource.postTrail(resource.trailResult(trail))
        validateResponse(validTrailPost, 201)
    }

    void validateResponse(Response response, Integer status, Integer code, String message) {
        if (status) {
            assert status == response.status
        }
        if (code) {
            assert code == response.code
        }
        if (message) {
            assert message == response.message
        }
    }
}
