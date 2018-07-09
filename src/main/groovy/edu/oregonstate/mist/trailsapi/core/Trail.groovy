package edu.oregonstate.mist.trailsapi.core

import com.fasterxml.jackson.annotation.JsonIgnore

class Trail {
	@JsonIgnore
	Integer id
	
	String name
	Integer zipCode
	String difficulty
	Boolean largeDrop
	Boolean smallDrop
	Boolean woodRide
	Boolean skinny
	Boolean largeJump
	Boolean smallJump
	Boolean gap
}
