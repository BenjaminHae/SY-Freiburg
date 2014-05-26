package mfs.ese.scotlandyard;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class MyMap extends Activity implements HttpResp{

	public HttpResp resp = this;
	public Activity act = this;
	
	boolean isUpdating=false;
    private List<SYGroup> groups = new ArrayList<SYGroup>();
    private Handler mHandler;

    class SYGroup implements Serializable {
		public int groupNumber;
		public LatLng position;
        public LatLng prevPosition;
		public String comment;
		public String timestamp;
		public String direction;
		public String transportation;
		public transient Marker marker;
        public Polyline line;
		public boolean isXGroup;
		
		
		public SYGroup(String[] groupVals, boolean misterx) {
            groupNumber = Integer.parseInt(groupVals[0]);
			position = new LatLng(Double.parseDouble(groupVals[1].split(",")[0]),Double.parseDouble(groupVals[1].split(",")[1]));
            prevPosition = position;//keine Alte Position übergeben, also die gleiche nehmen
            isXGroup = misterx;
            direction = groupVals[3];
            transportation = groupVals[4];
            comment = groupVals[5];
            timestamp = groupVals[6].substring(11, 16); //Nur Uhrzeit
        }
	}
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        if (!isUpdating) {
	      //Get current position
		    /*LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		    Criteria criteria = new Criteria();
		    String provider = locationManager.getBestProvider(criteria, false);
		    Location location = locationManager.getLastKnownLocation(provider);
		    
		    if (location != null){*/

            GoogleMap mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
		        /*CameraPosition cameraPosition = new CameraPosition.Builder().target(
		                new LatLng(location.getLatitude(), location.getLongitude())).zoom(12).build();
		        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
		    }*/

            DrawGroups();
        	UpdateMap();
		}
        else
        {
            Log.d("std", " already updating");
        }
        isUpdating = true;
        
    }
    
    Runnable mRefreshMap;

    public void UpdateMap() {
        mRefreshMap = new Runnable() {
            @Override
            public void run() {
            // Get positions
                new Http("http://www.benjaminh.de/sy/ajax.php", MyMap.this.resp)
                        .execute("AJAX=hgroups");
                new Http("http://www.benjaminh.de/sy/ajax.php", MyMap.this.resp)
                        .execute("AJAX=xgroups");
                mHandler.postDelayed(this, Vars.MAP_UPDATING_INTERVAL);
            }
        };
        mHandler = new Handler();
	}

    private void DrawGroups()
    {
        if (act.hasWindowFocus()){
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable(){

                @Override
                public void run() {
                    try {
                        GoogleMap mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

                        for (SYGroup gp : groups) {
                            if (gp.marker != null) {
                                Log.d("std", "Updating existing marker");
                                gp.marker.setPosition(gp.position);
                            } else {
                                Log.d("std", "Creating new marker");
                                float color = BitmapDescriptorFactory.HUE_AZURE;
                                if (gp.isXGroup) color = BitmapDescriptorFactory.HUE_RED;
                                if (gp.groupNumber < 10) {
                                    gp.marker = mMap.addMarker(new MarkerOptions()
                                            .position(gp.position)
                                            .flat(true) //necessary for rotation
                                            .icon(BitmapDescriptorFactory.defaultMarker(color))
                                            .title("Gruppe " + gp.groupNumber + " - " + gp.transportation + " - " + gp.direction)
                                            .snippet(gp.comment + " - " + gp.timestamp));
                                } else {
                                    gp.marker = mMap.addMarker(new MarkerOptions()
                                            .position(gp.position)
                                            .flat(true) //necessary for rotation
                                            .icon(BitmapDescriptorFactory.defaultMarker(color))
                                            .title("Gruppe " + gp.groupNumber)
                                            .snippet(gp.timestamp));
                                }
                            }
                            if (gp.isXGroup) {//Bewegungslinien zeigen
                                if (gp.line != null) {
                                    Log.d("std", "Updating existing link");
                                    List<LatLng> pts = new ArrayList<LatLng>();
                                    pts.add(gp.position);
                                    pts.add(gp.prevPosition);
                                    gp.line.setPoints(pts);
                                } else {
                                    Log.d("std", "Creating new link");
                                    gp.line = mMap.addPolyline(new PolylineOptions()
                                            .add(gp.position, gp.prevPosition)
                                            .width(3)
                                            .color(Color.RED));
                                }
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.d("std", e.getMessage());
                    }

                }
            });
        }
    }

	@Override
	public void response(String url, String param, String resp) {
        if (param.equals("AJAX=hgroups") || param.equals("AJAX=xgroups"))
        {
            boolean misterX=false;
            if (param.equals("AJAX=xgroups")) misterX = true;

            String[] sGroups = resp.split("<br/>");
            for (String group : sGroups) {
                String[] groupVals = group.split(" \r\n");
                updateGroup(groupVals, misterX);
            }
            DrawGroups();
        }
    }
	
	public void updateGroup(String[] groupValues, boolean misterX) {
		try {
			boolean alreadyInList = false;

			for (SYGroup gp : groups) {
				if (gp.groupNumber == Integer.parseInt(groupValues[0])) {
					alreadyInList = true;
                    LatLng oldPos = gp.position;
					gp.position = new LatLng(Double.parseDouble(groupValues[1].split(",")[0]),Double.parseDouble(groupValues[1].split(",")[1]));
                    if (!gp.position.equals(oldPos))
                        gp.prevPosition = oldPos;
					gp.direction = groupValues[3];
					gp.transportation = groupValues[4];
					gp.comment = groupValues[5];
					gp.timestamp = groupValues[6].substring(11,16); //Nur Uhrzeit
					
					Log.d("std", "Group " + groupValues[0] + " was updated.");
				}
				
			}

			if (!alreadyInList) {
				groups.add(new SYGroup(groupValues, misterX));
				Log.d("std", "Group " + groupValues[0] + " was added.");
			}

			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.map, menu);
        return true;
    }

    public void showInteract() {
        Intent intent = new Intent(this, Interact.class);
        startActivity(intent);
    }

    public void showSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_interact:
                showInteract();
                return true;
            case R.id.action_refresh:
		        Toast.makeText(getApplicationContext(), "Lade Positionen", Toast.LENGTH_SHORT).show();
                // Get positions
                new Http("http://www.benjaminh.de/sy/ajax.php", resp)
                        .execute("AJAX=hgroups");

                new Http("http://www.benjaminh.de/sy/ajax.php", resp)
                        .execute("AJAX=xgroups");//TODO Refreshing oder so anzeigen
                return true;
            case R.id.action_settings:
                showSettings();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onResume()
    {
        super.onResume();
        mHandler.postDelayed(mRefreshMap, 500);

    }
    @Override
    public void onPause()
    {
        super.onPause();
        mHandler.removeCallbacks(mRefreshMap);
    }
}