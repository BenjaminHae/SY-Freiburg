package mfs.ese.scotlandyard;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;


public class Interact extends Activity implements HttpResp, NumberPickerDialog.NumberPickerDialogListener{
    public HttpResp resp = this;
    private Resources mResources;
    private SharedPreferences mSettings;

    public void refresh(String state)
    {
        if (state.equals(""))
        {
            state="allComments&max=10";
        }
        TextView output = (TextView) findViewById(R.id.commentText);
        output.setText("");
        String Parameters = "AJAX="+state;
        //Toast.makeText(getApplicationContext(),"Lade Kommentare", Toast.LENGTH_SHORT).show();
        new Http("http://www.benjaminh.de/sy/ajax.php", resp)
                .execute(Parameters);
    }

    @Override
    public void response(String url, String param, String resp) {
        if (param.contains("AJAX=allComments")||param.contains("AJAX=commentsBy"))
        {
            String[] sComments = resp.split("<br/>");
            String outputText ="";
            for (String group : sComments) {
                if (!group.trim().equals("")) {
                    String[] groupVals = group.split("\r\n");
                    outputText += generateCommentHTML(groupVals[0], groupVals[6], groupVals[5], groupVals[2]) + "<br/>";
                }
            }
            ((TextView) findViewById(R.id.commentText)).setText(Html.fromHtml(outputText));
            if (param.contains("max"))
                ((Button) findViewById(R.id.buttonMoreComments)).setVisibility(View.VISIBLE);
            else
                ((Button) findViewById(R.id.buttonMoreComments)).setVisibility(View.GONE);
        }
        else if (url.equals(mResources.getString(R.string.URL_ins)))
        {
            if (resp.equals("OK")) {
                refresh("");
                ((EditText) findViewById(R.id.editComment)).setText("");
            } else {
                ((TextView) findViewById(R.id.commentText)).setText(Html.fromHtml(resp));
            }
        }
    }

    public String generateCommentHTML(String no, String Time, String Comment, String Address) {
        String result = "<b>Gruppe " + no + "</b> um <em>" + Time.split(" ")[1] + "</em>"+" in "+Address+"<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + Comment;
        return result;
    }

    //Karte zeigen
    public void showMap() {
        Intent intent = new Intent(this, MyMap.class);
        startActivity(intent);
    }

    public void showSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interact);
        mResources = this.getResources();
        mSettings = PreferenceManager.getDefaultSharedPreferences(this);

        final Button buttonSend = (Button) findViewById(R.id.sendComment);//Kommentar absenden
        buttonSend.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String text = ((EditText) findViewById(R.id.editComment)).getText().toString();
                if (!text.trim().equals("")) {
                    String group = mSettings.getString("pref_group_id", "11");
                    Location location = null;
                    if (((CheckBox) findViewById(R.id.cbAddLocation)).isChecked())
                    {
                        location = MainActivity.mLocationByPlay.getLocation();
                    }
                    Toast.makeText(getApplicationContext(),getString(R.string.sending), Toast.LENGTH_SHORT).show();
                    Vars.SendLocation(Integer.parseInt(group), text, "", "", location, resp);
                }
            }
        });
        final Button buttonMoreComments= (Button) findViewById(R.id.buttonMoreComments);//More Comments schauen
        buttonMoreComments.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                refresh("allComments");
            }
        });
        refresh("");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.interact, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_map:
                showMap();
                return true;
            case R.id.action_settings:
                showSettings();
                return true;
            case R.id.action_refresh:
                refresh("");
                return true;
            case R.id.action_CommentsBy:
                DialogFragment newFragment = new NumberPickerDialog();
                newFragment.show(getFragmentManager(), "missiles");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    // The dialog fragment receives a reference to this Activity through the
    // Fragment.onAttach() callback, which it uses to call the following methods
    // defined by the NoticeDialogFragment.NoticeDialogListener interface
    @Override
    public void onDialogPositiveClick(NumberPickerDialog dialog) {
        // User touched the dialog's positive button
        //(TextView) findViewById(R.id.commentText);
        int group = dialog.getResult();
        refresh("commentsBy&group="+Integer.toString(group));
    }

    @Override
    public void onDialogNegativeClick(NumberPickerDialog dialog) {
        // User touched the dialog's negative button

    }
}
