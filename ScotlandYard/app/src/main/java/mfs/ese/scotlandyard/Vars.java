package mfs.ese.scotlandyard;

import android.location.Location;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

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
        String groupId = null;
        String position = null;
        String transportation = null;
        String direction = null;
        String comment = null;
        try {
            groupId = "group=" + URLEncoder.encode(String.valueOf(_groupId), "UTF-8");
            position = "position=" + URLEncoder.encode(_location, "UTF-8");
            transportation = "transportation=" + URLEncoder.encode(_transportation, "UTF-8");
            direction = "direction=" + URLEncoder.encode(_direction, "UTF-8");
            comment = "comment=" + URLEncoder.encode(_comment, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        //Send position
        Http con = new Http("http://www.benjaminh.de/sy/ins.php", resp);
        con.setPost(true);
        con.execute(groupId, position, transportation, direction, comment);

    }
}