package edu.oregonstate.mist.trailsapi.mapper

import edu.oregonstate.mist.trailsapi.core.Trail
import org.skife.jdbi.v2.StatementContext
import org.skife.jdbi.v2.tweak.ResultSetMapper
import java.sql.Clob
import java.sql.ResultSet
import java.sql.SQLException

public class TrailMapper implements ResultSetMapper<Trail> {
    public Trail map(int i, ResultSet rs, StatementContext sc) throws SQLException {
    new Trail (
        id: rs.getInt('ID'),
        name: rs.getString('NAME'),
        zipCode: rs.getInt('ZIPCODE'),
        difficulty: rs.getString('DIFFICULTY_COLOR'),
        polyline: clobToString(rs.getClob('POLYLINE')),
        largeDrop: rs.getBoolean('LARGEDROP'),
        smallDrop: rs.getBoolean('SMALLDROP'),
        woodRide: rs.getBoolean('WOODRIDE'),
        skinny: rs.getBoolean('SKINNY'),
        largeJump: rs.getBoolean('LARGEJUMP'),
        smallJump: rs.getBoolean('SMALLJUMP'),
        gap: rs.getBoolean('GAP')
        )
    }

    String clobToString(Clob clob) {
        if (clob) {
            Integer clobLength = clob.length()
            return clob.getSubString(1, clobLength)
        } else {
            return ""
        }
    }
}
