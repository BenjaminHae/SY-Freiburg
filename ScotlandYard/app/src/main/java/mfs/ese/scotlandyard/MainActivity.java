package mfs.ese.scotlandyard;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class MainActivity extends Activity implements HttpResp {

	private boolean isTracking=false;
	public HttpResp resp = this;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		Spinner spinner = (Spinner) findViewById(R.id.transportationSpinner);
		// Create an ArrayAdapter using the string array and a default spinner layout
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
		        R.array.transportation_array, android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		spinner.setAdapter(adapter);
			
		
		

		final Button button = (Button) findViewById(R.id.button1);
		button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				int gpid = -1;
				
				try{
				gpid = Integer
						.parseInt(((EditText) findViewById(R.id.groupIdText))
								.getText().toString());
				}
				catch(Exception e){
					//Parse error
					e.printStackTrace();
				}
				
				if (gpid>=0 && gpid < 600){
					
					//Store gpid globally for Tracking
					Vars.groupNumber = gpid;
				
					startTracking();	
					showMap();
				}
				else if (gpid>=0 && gpid > 600){
					MsgBox("Fehler", "Mister X Gruppen müssen manuell ihre Position senden!");
				}
				else{
					MsgBox("Fehler", "Bitte eine gültige Gruppennummer eingeben!");
	        		((EditText)findViewById(R.id.groupIdText)).requestFocus();
				}

			}
		});

		final Button button2 = (Button) findViewById(R.id.button2);
		button2.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				showMap();
				
			}
		});
		
		
		final Button submit = (Button) findViewById(R.id.submitPositionButton);
		submit.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				
				//Send position and other stuff
				//Get current position
	    	    LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	    	    Criteria criteria = new Criteria();
	    	    String provider = locationManager.getBestProvider(criteria, false);
	    	    Location location = locationManager.getLastKnownLocation(provider);
	    	    
	    	    int gpid=-1;
	    	    
	    	    if (location != null){
		    	    try{
		    	    	gpid = Integer.parseInt(((EditText) findViewById(R.id.groupIdText)).getText().toString());
		    	    }
		    	    catch(Exception e){
		    	    	//Parse error
		    	    	e.printStackTrace();
		    	    }
		    	    
		    	    
		    	    String groupId = "group="+String.valueOf(gpid);
		        	String position = "position="+location.getLatitude()+","+location.getLongitude();
		        	String transportation = "transportation="+((Spinner) findViewById(R.id.transportationSpinner)).getSelectedItem().toString();
		        	String direction = "direction="+((EditText) findViewById(R.id.directionText)).getText().toString();
		        	String comment = "comment="+((EditText) findViewById(R.id.commentText)).getText().toString();
		        	
		        	if (gpid < 0){
		        		MsgBox("Fehler", "Bitte eine gültige Gruppennummer eingeben!");
		        		((EditText)findViewById(R.id.groupIdText)).requestFocus();
		        	}
		        	else if (transportation.equals("transportation=[Bitte wählen]")){
		        		MsgBox("Fehler", "Bitte eine Fortbewegungsmittel auswählen!");
		        	}
		        	else{
			        	//Send position
			        	Http con = new Http("http://www.benjaminh.de/sy/ins.php", resp);
			        	con.setPost(true);
			        	con.execute(groupId,position,transportation,direction,comment);
		        	}
		        	
	    	    }

			}
		});
	}

	public void showMap() {
		Intent intent = new Intent(this, MyMap.class);
		startActivity(intent);
	}

	public void startTracking() {
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
	public void response(String url, String param, String resp) {
		if (resp.equals("OK")){
			MsgBox("OK", "Position erfolgreich übertragen!");
		}
		else{
			MsgBox("Fehler", "Es gab ein Problem bei der Übertragung: "+resp);
		}
		
	}
	
	public void MsgBox(String title, String message){
		Builder builder = new Builder(this);
		builder.setTitle(title);
		builder.setMessage(message);
		builder.setPositiveButton("OK", null);
		builder.show();
	}

}
