package edu.oregonstate.mist.trailsapi.core

import com.fasterxml.jackson.annotation.JsonIgnore
import java.sql.Clob

//Optional fields have deafult values so that no exceptions are thrown in binding
class Trail {
	@JsonIgnore
	Integer id

	String name
	Integer zipCode
	String difficulty
	String polyline = ""
	Boolean largeDrop = false
	Boolean smallDrop = false
	Boolean woodRide = false
	Boolean skinny = false
	Boolean largeJump = false
	Boolean smallJump = false
	Boolean gap = false
}
