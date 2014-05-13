package mfs.ese.scotlandyard;

import java.util.Timer;
import java.util.TimerTask;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.location.Location;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements HttpResp {

    public static LocationByPlay mLocationByPlay;
    public HttpResp resp = this;
    public static SharedPreferences mSettings;
    
    private boolean isTracking=false;
    private Resources mResources;
    private Intent mIntentTracking;
    private SharedPreferences.OnSharedPreferenceChangeListener mSharedPreferenceChangeListener;
    private Timer mTimer;


    @Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mResources = this.getResources();
        setContentView(R.layout.activity_main);

        Spinner spinner = (Spinner) findViewById(R.id.transportationSpinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.transportation_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        mLocationByPlay = new LocationByPlay(this);
        mLocationByPlay.setConnectListener(new LocationByPlay.IAsyncFetchListener() {
            @Override
            public void onConnect() {
                MainActivity.this.playConnected();
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
            ((RelativeLayout) findViewById(R.id.viewMrX)).setVisibility(View.INVISIBLE);
        }
    }
    private void showTrackingInfo()
    {
        //ToDo Infos übers Tracking anzeigen, z.b. Fehler
    }

    private void populateOnClick() {
        final Button sendCatch = (Button) findViewById(R.id.sendCatch);//Gefangen von absenden
        sendCatch.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int gpid = -1;
                try {
                    gpid = Integer.parseInt(mSettings.getString("pref_group_id", "11"));
                } catch (Exception e) {
                    //Parse error
                    e.printStackTrace();
                }
                if (gpid < 0) {
                    MsgBox("Fehler", "Bitte eine gültige Gruppennummer eingeben!");
                    MainActivity.this.showSettings();
                } else
                    Vars.SendLocation(gpid, "Gefangen von " + ((EditText) findViewById(R.id.editCatch)).getText().toString(), "", "", mLocationByPlay.getLocation(), resp);
            }
        });

        final Button submit = (Button) findViewById(R.id.submitPositionButton);//Position manuell senden
        submit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String comment = ((EditText) findViewById(R.id.commentText)).getText().toString();
                String transportation = ((Spinner) findViewById(R.id.transportationSpinner)).getSelectedItem().toString();
                String direction = ((EditText) findViewById(R.id.directionText)).getText().toString();
                int gpid = -1;
                try {
                    gpid = Integer.parseInt(mSettings.getString("pref_group_id", "11"));
                } catch (Exception e) {
//Parse error
                    e.printStackTrace();
                }
                if (gpid < 0) {
                    MsgBox("Fehler", "Bitte eine gültige Gruppennummer eingeben!");
                    MainActivity.this.showSettings();
                } else
                    Vars.SendLocation(gpid, comment, transportation, direction, mLocationByPlay.getLocation(), resp);
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
            Log.d("std","SY: Start Tracking");
            mLocationByPlay.connect();
            //weiter geht's in playConnected
        }
        else
            mLocationByPlay.disconnect();
    }

    public void playConnected() {
        mLocationByPlay.StartTracking();
        if (!isTracking) {
            Log.d("std", "SY: Connected, Tracking runs");
            if (mTimer != null)
                mTimer.cancel();
            mTimer = new Timer();
            mTimer.schedule(new TrackingTask(this), 0, Integer.parseInt(mSettings.getString("pref_submit_interval", "30")) * 1000);
        }
        isTracking = true;
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

    public void stopTracking() {
        if (mTimer != null) {
            Log.d("std","SY: End Tracking");
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
                Toast.makeText(getApplicationContext(), "Übertragung erfolgreich", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "Es gab ein Problem bei der Übertragung: " + resp, Toast.LENGTH_LONG).show();
            }
        } else if (url.equals(mResources.getString(R.string.URL_ajax))) {
            if (param.contains("exX"))
            {
                //Frage nach welche MrX existieren
            }
            else if(param.contains("commentsBy"))
            {
                //Kommentare anzeigen
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
