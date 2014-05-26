package mfs.ese.scotlandyard;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity implements HttpResp {

    public static LocationByPlay mLocationByPlay;
    public HttpResp resp = this;
    public static SharedPreferences mSettings;
    
    private boolean isTracking=false;
    private Resources mResources;
    private Intent mIntentTracking;
    private SharedPreferences.OnSharedPreferenceChangeListener mSharedPreferenceChangeListener;
    private static Timer mTimer;
    private static String mLastKnownAddress;
    private static String mLastSentAddress;
    private static Date mLastSentTime;
    private static Date mLastKnownTime;
    private boolean mDidSendComment = false;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mResources = this.getResources();
        if (mLastSentTime==null)
            mLastSentTime = new Date();
        if (mLastSentAddress==null)
            mLastSentAddress = new String();
        if (mLastKnownTime==null)
            mLastKnownTime = new Date();
        if (mLastKnownAddress==null)
            mLastKnownAddress = new String();

        setContentView(R.layout.activity_main);

        mLocationByPlay = new LocationByPlay(this);
        mLocationByPlay.setConnectListener(new LocationByPlay.IAsyncFetchListener() {
            @Override
            public void onConnect() {
                MainActivity.this.playConnected();
            }
        });

        mLocationByPlay.setNewLocationListener(new LocationByPlay.IAsyncFetchLocationListener() {
            @Override
            public void onNewLocation(Location location) {
                MainActivity.this.mLastKnownAddress = mLocationByPlay.getAddress(location, getApplicationContext());
                MainActivity.this.mLastKnownTime = new Date();
                MainActivity.this.showTrackingInfo();
            }
        });

        populateOnClick();//Buttons zuweisen

        //Einstellungen laden
        mSettings = PreferenceManager.getDefaultSharedPreferences(this);
        mSharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                Log.d("std", "SY: SharedPreferenceChanged " + key);
                if (key.equals("pref_auto_submit_location") || key.equals("pref_submit_interval"))
                {
                    Log.d("std", "SY: SharedPreferenceChanged Tracking restart");
                    stopTracking();
                    setTracking();
                }
                refreshView();
            }
        };
        mSettings.registerOnSharedPreferenceChangeListener(mSharedPreferenceChangeListener);
        refreshView();
        setTracking();//Tracking in abhängigkeit von den Einstellungen setzen
    }

    private void refreshView() {
        String group = mSettings.getString("pref_group_id", "11");
        ((TextView) findViewById(R.id.groupIdText)).setText(group);
        boolean tracking = mSettings.getBoolean("pref_auto_submit_location", false);
        ((Switch) findViewById(R.id.switchLocation)).setChecked(tracking);
        showTrackingInfo();
        //Tracking anzeigen
        if (Integer.parseInt(group) > 690)//MrX
        {
            ((RelativeLayout) findViewById(R.id.viewMrX)).setVisibility(View.VISIBLE);
        } else {
            ((RelativeLayout) findViewById(R.id.viewMrX)).setVisibility(View.GONE);
        }
    }
    private void showTrackingInfo() {
        if ((mLastSentTime != null) && (mLastSentAddress != null))
            ((TextView) findViewById(R.id.textViewLastSentLocation)).setText(getString(R.string.main_location_lastSent) + " (" + new SimpleDateFormat("HH:mm:ss").format(mLastSentTime) + "):\n" + mLastSentAddress);
        if ((mLastKnownAddress != null) && (mLastKnownTime != null))
            ((TextView) findViewById(R.id.textViewLastKnownLocation)).setText(getString(R.string.main_location_lastKnown) + " (" + new SimpleDateFormat("HH:mm:ss").format(mLastKnownTime) + "):\n" + mLastKnownAddress);
    }

    private void populateOnClick() {
        final TextView textViewError = (TextView) findViewById(R.id.textViewError);//Fehleranzeige
        textViewError.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                textViewError.setText("");
            }
        });

        final Button sendCatch = (Button) findViewById(R.id.sendCatch);//Gefangen von absenden
        sendCatch.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int GroupId = -1;
                try {
                    GroupId = Integer.parseInt(mSettings.getString("pref_group_id", "11"));
                } catch (Exception e) {
                    //Parse error
                    e.printStackTrace();
                }
                if (GroupId < 0) {
                    MsgBox(getString(R.string.error), getString(R.string.error_invalidGroupId));
                    MainActivity.this.showSettings();
                } else
                    Vars.SendLocation(GroupId, getString(R.string.comment_caughtBy) + ((EditText) findViewById(R.id.editCatch)).getText().toString(), "", "", mLocationByPlay.getLocation(), resp);
            }
        });

        final Button submit = (Button) findViewById(R.id.submitPositionButton);//Position manuell senden
        submit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String comment = ((EditText) findViewById(R.id.commentText)).getText().toString();
                String direction = ((EditText) findViewById(R.id.directionText)).getText().toString();
                int groupId = -1;
                try {
                    groupId = Integer.parseInt(mSettings.getString("pref_group_id", "11"));
                } catch (Exception e) {//Parse error
                    e.printStackTrace();
                }
                if (groupId < 0) {
                    MsgBox(getString(R.string.error), getString(R.string.error_invalidGroupId));
                    MainActivity.this.showSettings();
                } else
                    Toast.makeText(getApplicationContext(), getString(R.string.toast_sendingPosition), Toast.LENGTH_SHORT).show();
                Vars.SendLocation(groupId, comment, "", direction, mLocationByPlay.getLocation(), resp);
            }
        });
    }

    //Karte zeigen
    public void showMap() {
		Intent intent = new Intent(this, MyMap.class);
		startActivity(intent);
	}

    public void showInteract() {
        Intent intent = new Intent(this, Interact.class);
        startActivity(intent);
    }

    public void showSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    public void setTracking() {
        if (mSettings.getBoolean("pref_auto_submit_location",false)) {
            mLocationByPlay.connect();
            //weiter geht's in playConnected
        }
        else
            mLocationByPlay.disconnect();
    }

    public void playConnected() {
        mLocationByPlay.StartTracking();
        if (!isTracking) {
            //TODO eventuell das Verbunden wieder anzeigen
            //Toast.makeText(getApplicationContext(),"Verbunden, Tracking läuft", Toast.LENGTH_SHORT).show();
            Log.d("std", "SY: Connected, Tracking runs");
            if (mTimer != null)
                mTimer.cancel();
            mTimer = new Timer();
            mTimer.schedule(new TrackingTask(this), 0, Integer.parseInt(mSettings.getString("pref_submit_interval", "30")) * 1000);
        }
        isTracking = true;
    }
    
    private class TrackingTask extends TimerTask {//Position regelmäßig senden
        HttpResp resp = null;

        public TrackingTask(HttpResp resp) {
            this.resp = resp;
        }//TODO das leakt die Aktivity!!

        public void run() {
            Log.d("std", "SY: Tracking");

            //Get current position
            Location location = mLocationByPlay.getLocation();
            String comment = "";
            String direction = "";
            if (((CheckBox) findViewById(R.id.cbInputReady)).isChecked()) {
                try {
                    comment = URLEncoder.encode(((EditText) findViewById(R.id.commentText)).getText().toString(), "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    comment = "";
                }
                try {
                    direction = URLEncoder.encode(((EditText) findViewById(R.id.directionText)).getText().toString(), "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    direction = "";
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((CheckBox) findViewById(R.id.cbInputReady)).setChecked(false);
                    }
                });
                mDidSendComment = true;
            }

            if (location != null) {
                //Send position
                Http con = new Http("http://www.benjaminh.de/sy/ins.php", resp);
                con.setPost(true);
                try {
                    con.execute("group=" + mSettings.getString("pref_group_id", ""), "position=" + location.getLatitude() + "," + location.getLongitude(), "comment=" + comment, "direction=" + direction, "address=" + URLEncoder.encode(mLastKnownAddress.toString(), "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    con.execute("group=" + mSettings.getString("pref_group_id", ""), "position=" + location.getLatitude() + "," + location.getLongitude(), "comment=" + comment, "direction=" + direction);
                }
                Log.d("std", "SY: " + mLastKnownAddress);
            }
        }
    }

    public void stopTracking() {
        if (mTimer != null) {
            Log.d("std","SY: End Tracking");
            Toast.makeText(getApplicationContext(),getString(R.string.toast_trackingEnd), Toast.LENGTH_SHORT).show();
            mTimer.cancel();
        }
        isTracking = false;
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_map:
                showMap();
                return true;
            case R.id.action_interact:
                showInteract();
                return true;
            case R.id.action_settings:
                showSettings();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

	@Override
	public void response(String url, String param, String resp) {
        if (url.equals(mResources.getString(R.string.URL_ins))) {
            if (resp.equals("OK")) {
                //Toast.makeText(getApplicationContext(), "Übertragung erfolgreich", Toast.LENGTH_SHORT).show();
                mLastSentAddress = mLocationByPlay.getAddress(mLocationByPlay.getLocation(), getApplicationContext());
                mLastSentTime = new Date();
                if (mDidSendComment) {
                    ((EditText) findViewById(R.id.commentText)).setText("");
                    ((EditText) findViewById(R.id.directionText)).setText("");
                    mDidSendComment = false;
                }
                if (param.contains(getString(R.string.comment_caughtBy)))
                {
                    ((EditText) findViewById(R.id.editCatch)).setText("");
                }
                showTrackingInfo();
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.error_transmission), Toast.LENGTH_LONG).show();
                ((TextView) findViewById(R.id.textViewError)).append("\n"+new SimpleDateFormat("HH:mm:ss").format(new Date()) + " " + resp);
            }
        }
    }

    public void MsgBox(String title, String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("OK", null);
        builder.show();
    }
}
