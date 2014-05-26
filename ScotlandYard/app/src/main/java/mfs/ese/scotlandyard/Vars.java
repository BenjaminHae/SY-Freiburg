package mfs.ese.scotlandyard;

import android.location.Location;

public class Vars {
    public static int MAP_UPDATING_INTERVAL = 15000; //neu laden der Karte

    public static void SendLocation(int groupId, String _comment, String _transportation, String _direction, Location location, HttpResp resp) {
        String position;
        if (location != null) {
            position = location.getLatitude() + "," + location.getLongitude();
        } else
            position = "";
        SendLocation(groupId, _comment, _transportation, _direction, position, resp);
    }

    public static void SendLocation(int _groupId, String _comment, String _transportation, String _direction, String _location, HttpResp resp) {
        //Set Parameters
        //TODO URL_ENCODE!
        String groupId = "group=" + String.valueOf(_groupId);
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