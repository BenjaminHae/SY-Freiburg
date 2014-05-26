package mfs.ese.scotlandyard;

import java.util.Timer;
import java.util.TimerTask;


import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;
import android.util.Log;

public class Tracking extends IntentService implements HttpResp {
    LocationByPlay mLocationByPlay;
    SharedPreferences mSettings;

    public Tracking() {
        super("TrackingService");
        mLocationByPlay = MainActivity.mLocationByPlay;
        mSettings = MainActivity.mSettings;
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        Reminder();
    }

    Timer timer;

    public void Reminder() {
        timer = new Timer();
        timer.schedule(new TrackingTask(this), 0, Integer.parseInt(mSettings.getString("pref_submit_interval", "30")) * 1000);
    }

    class TrackingTask extends TimerTask {
        HttpResp resp = null;

        public TrackingTask(HttpResp resp) {
            this.resp = resp;
        }

        public void run() {
            Log.d("std", "SY: Tracking");

            //Get current position
            Location location = mLocationByPlay.getLocation();

            if (location != null) {
                //Send position
                Http con = new Http("http://www.benjaminh.de/sy/ins.php", resp);
                con.setPost(true);
                con.execute("group=" + mSettings.getString("pref_group_id", ""), "position=" + location.getLatitude() + "," + location.getLongitude());
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        /*Log.d("std","SY: Timer stopped");//Offensichtlich der falsche Ort
        if (timer != null) {//TODO irgendwo aufrufen
            timer.cancel();
        }*/
    }

    @Override
    public void response(String action, String param, String resp) {
        Log.d("std", resp);
    }
}
