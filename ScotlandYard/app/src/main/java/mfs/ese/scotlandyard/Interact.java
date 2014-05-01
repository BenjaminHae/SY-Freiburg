package mfs.ese.scotlandyard;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class Interact extends Activity implements HttpResp{
    public HttpResp resp = this;
    private String state = "allComments";
    private Resources mResources;

    public void refresh()
    {
        TextView output = (TextView) findViewById(R.id.commentText);
        output.setText("");
        new Http("http://www.benjaminh.de/sy/ajax.php", resp)
                .execute("AJAX="+state);
    }

    @Override
    public void response(String url, String param, String resp) {
        if (param.equals("AJAX=allComments"))
        {
            String[] sComments = resp.split("<br/>");
            String outputText ="";
            for (String group : sComments) {
                String[] groupVals = group.split(" \r\n");
                outputText +=generateCommentHTML(groupVals[0],groupVals[6],groupVals[5]) + "<br/>";
            }
            ((TextView) findViewById(R.id.commentText)).setText(Html.fromHtml(outputText));
        }
        else if (url.equals(mResources.getString(R.string.URL_ins)))
        {
            if (resp.equals("OK")) {
                refresh();
                ((EditText) findViewById(R.id.editComment)).setText("");
            } else {
                ((TextView) findViewById(R.id.commentText)).setText(Html.fromHtml(resp));
            }
        }
    }

    public String generateCommentHTML(String no, String Time, String Comment) {
        return "<b>Gruppe " + no + "</b> um <em>" + Time + "</em><br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + Comment;
    }

    //Karte zeigen
    public void showMap() {
        Intent intent = new Intent(this, MyMap.class);
        startActivity(intent);
    }

    public void showSettings() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interact);
        mResources = this.getResources();

        final Button buttonSend = (Button) findViewById(R.id.sendComment);//AutoTracking aktivieren
        buttonSend.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText text = (EditText) findViewById(R.id.editComment);
                Vars.SendLocation(0, text.getText().toString(), "", "", "", resp);//TODO gpid setzen
            }
        });
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
                refresh();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
