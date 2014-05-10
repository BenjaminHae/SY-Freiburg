package mfs.ese.scotlandyard;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

public class MainActivity extends Activity implements HttpResp {

	private boolean isTracking=false;
    public static LocationByPlay mLocationByPlay;
	public HttpResp resp = this;
    private Resources mResources;
    public static SharedPreferences mSettings;


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

        populateOnClick();//Buttons zuweisen

        //Einstellungen laden
        mSettings = PreferenceManager.getDefaultSharedPreferences(this);
        String group= mSettings.getString("pref_group_id", "11");
        ((TextView) findViewById(R.id.groupIdText)).setText(group);
	}

    private void populateOnClick() {
        final Switch _switch = (Switch) findViewById(R.id.switchLocation);
        _switch.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (_switch.isChecked()) {
                    mLocationByPlay.StartTracking();
                } else
                    mLocationByPlay.PauseTracking();
            }
        });
        final Button button = (Button) findViewById(R.id.button1);//AutoTracking aktivieren //TODO mit Settings verbinden
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                int gpid = -1;

                try {
                    gpid = Integer
                            .parseInt(mSettings.getString("pref_group_id", "11"));
                } catch (Exception e) {
                    //Parse error
                    e.printStackTrace();
                }

                if (gpid >= 0 && gpid < 600) {
                    startTracking();
                    showMap();
                } else if (gpid >= 0 && gpid > 600) {
                    MsgBox("Fehler", "Mister X Gruppen müssen manuell ihre Position senden!");
                } else {
                    MsgBox("Fehler", "Bitte eine gültige Gruppennummer eingeben!");
                    MainActivity.this.showSettings();
                }

            }
        });

        final Button button2 = (Button) findViewById(R.id.button2);//Karte anzeigen
        button2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showMap();
            }
        });

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

	public void startTracking() {
        mLocationByPlay.StartTracking();
		if (!isTracking) {
			Intent intent = new Intent(this, Tracking.class);
			startService(intent);
		}
		isTracking = true;
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
                MsgBox("OK", "Position erfolgreich übertragen!");
            } else {
                MsgBox("Fehler", "Es gab ein Problem bei der Übertragung: " + resp);
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
