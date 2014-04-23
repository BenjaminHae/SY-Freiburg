package mfs.ese.scotlandyard;

import java.net.HttpURLConnection;
import java.net.URL;
import java.io.*;


import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.os.AsyncTask;
import android.util.Log;


public class Http extends AsyncTask<String, Boolean, String> {

	private boolean postMethod = false;
	private String address="";
	private String param="";
	private HttpResp resp;
	
	public static String serverAddress="";
	public static String key="";

	public Http(String address, HttpResp resp){
		this.resp = resp;
		this.address = address;
	}
	
	public void setPost(boolean post) {
		postMethod = post;
	}

	protected String doInBackground(String... params) {
		
		String url = address;
		String para = "";
		
		if (params.length > 0) para += params[0];
		
		for (int i=1; i<params.length; i++)
			para += "&"+params[i];
		
		param = para;
		Log.d("std", "Trying to reach "+url+" with params "+para+" and POST="+postMethod);
		
		String result = "";
		if (postMethod)	
			result = sendPost(url, para);
		else{
			result = executeGet(url, para);
		}
			

		return result;

	}

	protected void onPreExecute() {
		//textView.setText("Aktueller Kontostand: ...");
	}
	

	protected void onPostExecute(String res) {
		if (resp!=null) resp.response(address, param, res.replace("\t", ""));
	}

	
	public String executeGet(String targetURL, String para) {
		String responseString = "";
		targetURL +="?"+para;
		System.out.println("Connecting to "+targetURL);
		try {
			// set the connection timeout value to 5 seconds (5000 milliseconds)
		    final HttpParams httpParams = new BasicHttpParams();
		    HttpConnectionParams.setConnectionTimeout(httpParams, 5000);
			HttpClient httpclient = new DefaultHttpClient();
			HttpResponse response = httpclient.execute(new HttpGet(targetURL));
			StatusLine statusLine = response.getStatusLine();
			if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				response.getEntity().writeTo(out);
				out.close();
				responseString = out.toString();
				// ..more logic
			} else {
				// Closes the connection.
				response.getEntity().getContent().close();
				throw new IOException(statusLine.getReasonPhrase());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return responseString;
	}
	
	// HTTP POST request
		private String sendPost(String url, String urlParameters) {
			String USER_AGENT = "";
			HttpURLConnection con=null;
			StringBuffer response = new StringBuffer();
			URL obj;
			try {
				obj = new URL(url);
			
				con = (HttpURLConnection) obj.openConnection();
			
			
				//add request header
				con.setRequestMethod("POST");
				con.setRequestProperty("User-Agent", USER_AGENT);
				con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
				
			
				// Send post request
				con.setDoOutput(true);
				DataOutputStream wr = new DataOutputStream(con.getOutputStream());
				wr.writeBytes(urlParameters);
				wr.flush();
				wr.close();
		 
				int responseCode = con.getResponseCode();
				Log.d("std","\nSending 'POST' request to URL : " + url);
				Log.d("std","Post parameters : " + urlParameters);
				Log.d("std","Response Code : " + responseCode);
		 
				BufferedReader in = new BufferedReader(
				        new InputStreamReader(con.getInputStream()));
				String inputLine;
				
		 
				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();
			
			} catch (Exception e) {
				Log.d("std", e.getMessage());
				e.printStackTrace();
			}
			//print result
			return response.toString();
	 
		}
	/*
	 * URL url; HttpURLConnection connection = null; try { // Create connection
	 * url = new URL(targetURL); connection = (HttpURLConnection)
	 * url.openConnection(); connection.setRequestMethod("GET");
	 * connection.setRequestProperty("Content-Type",
	 * "application/x-www-form-urlencoded");
	 * 
	 * connection.setRequestProperty("Content-Length", "" +
	 * Integer.toString(urlParameters.getBytes().length));
	 * connection.setRequestProperty("Content-Language", "en-US");
	 * 
	 * connection.setUseCaches(false); connection.setDoInput(true);
	 * connection.setDoOutput(true);
	 * 
	 * // Send request DataOutputStream wr = new DataOutputStream(
	 * connection.getOutputStream()); wr.writeBytes(urlParameters); wr.flush();
	 * wr.close();
	 * 
	 * // Get Response InputStream is = connection.getInputStream();
	 * BufferedReader rd = new BufferedReader(new InputStreamReader(is)); String
	 * line; StringBuffer response = new StringBuffer(); while ((line =
	 * rd.readLine()) != null) { response.append(line); response.append('\r'); }
	 * rd.close(); return response.toString();
	 * 
	 * } catch (Exception e) {
	 * 
	 * e.printStackTrace(); return null;
	 * 
	 * } finally {
	 * 
	 * if (connection != null) { connection.disconnect(); } } }
	 */
}
