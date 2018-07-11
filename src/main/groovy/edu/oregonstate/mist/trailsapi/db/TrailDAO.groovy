package edu.oregonstate.mist.trailsapi.db

import edu.oregonstate.mist.trailsapi.core.Trail
import edu.oregonstate.mist.trailsapi.mapper.TrailMapper
import org.skife.jdbi.v2.sqlobject.SqlUpdate
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper
import org.skife.jdbi.v2.sqlobject.SqlQuery
import org.skife.jdbi.v2.sqlobject.Bind
import org.skife.jdbi.v2.sqlobject.BindBean

@RegisterMapper(TrailMapper)
interface TrailDAO extends Closeable {
	@SqlUpdate ("""
		INSERT INTO TRAILS (ID, NAME, ZIPCODE, DIFFICULTY_ID, LARGEDROP, SMALLDROP, WOODRIDE, SKINNY,
		LARGEJUMP, SMALLJUMP, GAP)
		VALUES (
				(:trail.id),
				(:trail.name),
				(:trail.zipCode),
				(SELECT DIFFICULTY_ID FROM TRAIL_DIFFICULTIES WHERE DIFFICULTY_COLOR = :trail.difficulty),
				(:trail.largeDrop),
				(:trail.smallDrop),
				(:trail.woodRide),
				(:trail.skinny),
				(:trail.largeJump),
				(:trail.smallJump),
				(:trail.gap)
			)
	""")
	void postTrail(@BindBean("trail") Trail trail)

	@SqlQuery("""
		SELECT TRAIL_SEQ.NEXTVAL FROM DUAL
	""")
	Integer getNextId()

	@SqlQuery("""
		SELECT ID FROM TRAILS WHERE
			NAME = :trail.name
			AND ZIPCODE = :trail.zipCode
			AND DIFFICULTY_ID = (SELECT DIFFICULTY_ID FROM TRAIL_DIFFICULTIES
				WHERE DIFFICULTY_COLOR = :trail.difficulty)
	""")
	List <Integer> getConflictingTrails(@BindBean("trail") Trail trail)
}
