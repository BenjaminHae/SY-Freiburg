package mfs.ese.scotlandyard;

import android.location.Location;

public class Vars {
    public static int UPDATING_INTERVAL = 10000; //neu laden der Karte

    public static void SendLocation(int gpid, String _comment, String _transportation, String _direction, Location location, HttpResp resp) {
        if (location != null) {
            String position = location.getLatitude() + "," + location.getLongitude();
            SendLocation(gpid, _comment,_transportation,_direction,position, resp);
        }
    }

    public static void SendLocation(int gpid, String _comment, String _transportation, String _direction, String _location, HttpResp resp) {
        //Set Parameters
        String groupId = "group=" + String.valueOf(gpid);
        String position = "position=" + _location;
        String transportation = "transportation=" + _transportation;
        String direction = "direction=" + _direction;
        String comment = "comment=" + _comment;
        //Send position
        Http con = new Http("http://www.benjaminh.de/sy/ins.php", resp);
        con.setPost(true);
        con.execute(groupId, position, transportation, direction, comment);

    }
}