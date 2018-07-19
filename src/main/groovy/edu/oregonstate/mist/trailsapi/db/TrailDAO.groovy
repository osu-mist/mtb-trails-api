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

    /***********************************************************************************************
    POST /trails
    ***********************************************************************************************/
    @SqlUpdate ("""
        INSERT INTO TRAILS (ID, NAME, ZIPCODE, DIFFICULTY_ID, POLYLINE, LARGEDROP, SMALLDROP,
            WOODRIDE, SKINNY, LARGEJUMP, SMALLJUMP, GAP)
        VALUES (
            (:trail.id),
            (:trail.name),
            (:trail.zipCode),
            (SELECT DIFFICULTY_ID FROM TRAIL_DIFFICULTIES
                WHERE DIFFICULTY_COLOR = :trail.difficulty),
            (:trail.polyline),
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

    /**********************************************************************************************
    Function: getConflictingTrails
    Description: Checks that no trails sharing a name, zip code, and difficulty exist in
        the database.
    Names are compared case insensitive after removing single quotes and spaces.
    Input: Trail object that is to be checked for conflict
    Output: Returns true if at least one conflict exists, false otherwise
    **********************************************************************************************/
    @SqlQuery("""
        SELECT COUNT(*) FROM TRAILS WHERE
            UPPER(REPLACE(REPLACE(NAME, ' ', ''), '''', ''))
                = UPPER(REPLACE(REPLACE(:trail.name, ' ', ''), '''', ''))
        AND ZIPCODE = :trail.zipCode
        AND DIFFICULTY_ID = (SELECT DIFFICULTY_ID FROM TRAIL_DIFFICULTIES
            WHERE DIFFICULTY_COLOR = :trail.difficulty)
    """)
    Boolean getConflictingTrails(@BindBean("trail") Trail trail)

    /***********************************************************************************************
    GET /trails
    ***********************************************************************************************/
    @SqlQuery("""
        SELECT
            TRAILS.ID,
            TRAILS.NAME,
            TRAIL_DIFFICULTIES.DIFFICULTY_COLOR,
            TRAILS.POLYLINE,
            TRAILS.ZIPCODE,
            TRAILS.SMALLDROP,
            TRAILS.LARGEDROP,
            TRAILS.WOODRIDE,
            TRAILS.SKINNY,
            TRAILS.LARGEJUMP,
            TRAILS.SMALLJUMP,
            TRAILS.GAP
        FROM TRAILS
        LEFT JOIN TRAIL_DIFFICULTIES ON TRAILS.DIFFICULTY_ID = TRAIL_DIFFICULTIES.DIFFICULTY_ID
            WHERE (UPPER(REPLACE(REPLACE(TRAILS.NAME, ' ', ''), '''', ''))
                = UPPER(REPLACE(REPLACE(:name, ' ', ''), '''', '')) OR :name IS NULL)
     	    AND (UPPER(TRAIL_DIFFICULTIES.DIFFICULTY_COLOR) = UPPER(:difficulty)
                OR :difficulty IS NULL)
            AND (TRAILS.DIFFICULTY_ID <= (SELECT DIFFICULTY_ID FROM TRAIL_DIFFICULTIES WHERE
                UPPER(DIFFICULTY_COLOR) = UPPER(:mostDifficult)) OR :mostDifficult IS NULL)
            AND (TRAILS.DIFFICULTY_ID >= (SELECT DIFFICULTY_ID FROM TRAIL_DIFFICULTIES WHERE
                UPPER(DIFFICULTY_COLOR) = UPPER(:leastDifficult)) OR :leastDifficult IS NULL)
            AND (TRAILS.ZIPCODE = :zipCode OR :zipCode IS NULL)
            AND (TRAILS.SMALLDROP = :smallDrop OR :smallDrop IS NULL)
            AND (TRAILS.LARGEDROP = :largeDrop OR :largeDrop IS NULL)
            AND (TRAILS.WOODRIDE = :woodRide OR :woodRide IS NULL)
            AND (TRAILS.SKINNY = :skinny OR :skinny IS NULL)
            AND (TRAILS.LARGEJUMP = :largeJump OR :largeJump IS NULL)
            AND (TRAILS.SMALLJUMP = :smallJump OR :smallJump IS NULL)
            AND (TRAILS.GAP = :gap OR :gap IS NULL)
    """)
    List<Trail> getTrailByQuery(@Bind("name") String name,
                                @Bind("difficulty") String difficulty,
                                @Bind("mostDifficult") String mostDifficult,
                                @Bind("leastDifficult") String leastDifficult,
                                @Bind("zipCode") Integer zipCode,
                                @Bind("smallDrop") Boolean smallDrop,
                                @Bind("largeDrop") Boolean largeDrop,
                                @Bind("woodRide") Boolean woodRide,
                                @Bind("skinny") Boolean skinny,
                                @Bind("largeJump") Boolean largeJump,
                                @Bind("smallJump") Boolean smallJump,
                                @Bind("gap") Boolean gap)

    /***********************************************************************************************
    GET /trails/{trailID}
    ***********************************************************************************************/
    @SqlQuery("""
        SELECT
            TRAILS.ID,
            TRAILS.NAME,
            TRAIL_DIFFICULTIES.DIFFICULTY_COLOR,
            TRAILS.POLYLINE,
            TRAILS.ZIPCODE,
            TRAILS.SMALLDROP,
            TRAILS.LARGEDROP,
            TRAILS.WOODRIDE,
            TRAILS.SKINNY,
            TRAILS.LARGEJUMP,
            TRAILS.SMALLJUMP,
            TRAILS.GAP
       FROM TRAILS
       LEFT JOIN TRAIL_DIFFICULTIES ON TRAILS.DIFFICULTY_ID = TRAIL_DIFFICULTIES.DIFFICULTY_ID
       WHERE TRAILS.ID = :id
    """)
    Trail getTrailByID(@Bind("id") Integer id)

    /***********************************************************************************************
    PUT /trails/{trailID}
    Updates values if binded value is not null
    ***********************************************************************************************/
    @SqlUpdate("""
        UPDATE TRAILS
            SET
                NAME = :name,
                DIFFICULTY_ID = (SELECT DIFFICULTY_ID FROM TRAIL_DIFFICULTIES
                    WHERE DIFFICULTY_COLOR = :difficulty),
                ZIPCODE = :zipCode,
                SMALLDROP = :smallDrop,
                LARGEDROP = :largeDrop,
                WOODRIDE = :woodRide,
                SKINNY = :skinny,
                LARGEJUMP = :largeJump,
                SMALLJUMP = :smallJump,
                GAP = :gap
            WHERE ID = :id
    """)
    void updateTrail(@Bind("id") Integer id,
                     @Bind("name") String name,
                     @Bind("difficulty") String difficulty,
                     @Bind("zipCode") Integer zipCode,
                     @Bind("smallDrop") Boolean smallDrop,
                     @Bind("largeDrop") Boolean largeDrop,
                     @Bind("woodRide") Boolean woodRide,
                     @Bind("skinny") Boolean skinny,
                     @Bind("largeJump") Boolean largeJump,
                     @Bind("smallJump") Boolean smallJump,
                     @Bind("gap") Boolean gap)
}
