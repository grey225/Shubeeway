package com.jerrymannel.smsgateway;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.sHubeeway.R;

public class MySMSGatewayMainActivity extends Activity {

	private LinearLayout linerLayout_server;
	private LinearLayout linearLayout_message;
	private Switch switch_server;
	private TextView textView_serverStatus;
	private TextView textView_comment;
	private SharedPreferences prefs;

	private String ipAddress;
	private int port;
	private HTTPServer server;
	private SimpleDateFormat sdf;
	private String currentTime;

	private SmsManager sms;
	private String phoneNumber;
	private String message;
	
	private String result;
	private boolean history;

	private static final String TAG = "mysHubeeway";
	
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		sms = SmsManager.getDefault();
		prefs = this.getSharedPreferences("com.jerrymannel.mysmsgateway",
				Context.MODE_PRIVATE);
		if (prefs.getInt("port", 0) == 0) {
			SharedPreferences.Editor editor = prefs.edit();
			editor.putInt("port", 18080);
			editor.commit();
			port = 18080;
		}
		
		
		linerLayout_server = (LinearLayout) findViewById(R.id.linearLayout_server);
		linearLayout_message = (LinearLayout) findViewById(R.id.linearLayout_message);
		textView_serverStatus = (TextView) findViewById(R.id.textView_serverStaus);
		textView_comment = (TextView) findViewById(R.id.textView_comment);
		switch_server = (Switch) findViewById(R.id.switch_server);
		server = null;
		
		   if( checkNetworkState()== true){
		    getLocalIpAddress();

			boolean isChecked = true;
			switch_server.setChecked(isChecked);
				textView_serverStatus.setText(R.string.serverOn);
				linerLayout_server
						.setBackgroundResource(R.drawable.backgroud_start);
				port = prefs.getInt("port", 0);
				Log.i(TAG, "Port set to " + port);
				textView_comment
						.setText(getString(R.string.connectComment)
								+ "http://" + ipAddress + ":" + port);

				Log.i(TAG, "Starting server ...");
				server = new HTTPServer();
				server.execute("");
		   }
		   else{
			  
				textView_serverStatus.setText(R.string.serverOff);
				linerLayout_server
						.setBackgroundResource(R.drawable.backgroud_stop);
				textView_comment.setText(R.string.stopComment);
				server.cancel(true);
				server = null;
				Log.i(TAG, "Server stopped!");
		   }
		switch_server.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView,	boolean isChecked) {
					if (!isChecked) {
						
						textView_serverStatus.setText(R.string.serverOff);
						linerLayout_server
								.setBackgroundResource(R.drawable.backgroud_stop);
						textView_comment.setText(R.string.stopComment);
						server.cancel(true);
						server = null;
						Log.i(TAG, "Server stopped!");

					} else {
						textView_serverStatus.setText(R.string.serverOn);
						linerLayout_server
								.setBackgroundResource(R.drawable.backgroud_start);
						port = prefs.getInt("port", 0);
						Log.i(TAG, "Port set to " + port);
						textView_comment
								.setText(getString(R.string.connectComment)
										+ "http://" + ipAddress + ":" + port);

						Log.i(TAG, "Starting server ...");
						server = new HTTPServer();
						server.execute("");
						
					}
				
			}
		});
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater mi = getMenuInflater();
		mi.inflate(R.menu.options_menu, menu);
		return true;
	}
	
/*	final Button button = (Button) findViewById(R.id.button1);
    button.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
        	startActivity(new Intent(this, History.class));
        }
    }*/

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.item_setting:
			startActivity(new Intent(this, SettingsView.class));
			break;
		case R.id.item_about:
			startActivity(new Intent(this, AboutView.class));
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	

	
	private boolean checkNetworkState() {
		    ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		    NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		    WifiManager wiM = (WifiManager) getSystemService(WIFI_SERVICE);
		    
		if (wiM.isWifiEnabled()){
	    	
	    }else{
	    	wiM.setWifiEnabled(true);
	    }

	        int i=0;

	        while (!wifi.isConnected())   {
	            i++;
	            try {
	                Thread.sleep(2000);
	            } catch (InterruptedException e) {
	                e.printStackTrace();
	            }

	            if (i > 5) {
	                break;
	            }
	        } if(wifi.isConnected()) {
			return true;
		}
	        else {
	        	showAlert("No active network connections \navailable.");
	        	return false;
	        }
		
	}
	// Toast
	private void showAlert(final String s) {
		runOnUiThread(new Runnable() {
			public void run() {
				Toast.makeText(MySMSGatewayMainActivity.this, s,
						Toast.LENGTH_SHORT).show();
			}
		});
	}
	
private void getLocalIpAddress() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				if (intf.getName().contentEquals("wlan0")) {
					for (Enumeration<InetAddress> enumIpAddr = intf
							.getInetAddresses(); enumIpAddr.hasMoreElements();) {
						InetAddress inetAddress = enumIpAddr.nextElement();
						if (!inetAddress.isLoopbackAddress()) {
							ipAddress = inetAddress.getHostAddress().toString();
						}
					}
				}
			}
		} catch (SocketException ex) {
			System.out.println(ex.toString());
		}
	}

	private void sendSMS()  {
		sdf = (SimpleDateFormat) SimpleDateFormat.getTimeInstance();
		currentTime = sdf.format(new Date());
		String SENT = "SMS_SENT";
		history = true;

		PendingIntent sentPI = PendingIntent.getBroadcast(MySMSGatewayMainActivity.this, 0, new Intent(SENT), 0);
		
						registerReceiver(new BroadcastReceiver(){
				            @Override
				            //Get the statue of the sending message
				            public void onReceive(Context arg0, Intent arg1) {
				                switch (getResultCode())
				                {
				                    case Activity.RESULT_OK:
				                    	break;
				                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
				                    	result = ("Message Non Envoye (Generic failure) \r\n\n" + " [" + currentTime + "]" + " \r\n\n");
				                    	history = false;
				                    	break;
				                    case SmsManager.RESULT_ERROR_NO_SERVICE:
				                    	result = ("Message Non Envoye (No Service) \r\n\n" + " [" + currentTime + "]" + " \r\n\n");
				                    	history = false;
				                    	break;
				                    case SmsManager.RESULT_ERROR_NULL_PDU:
				                    	result = ("Message Non Envoye (Null PDU) \r\n\n" + " [" + currentTime + "]" + " \r\n\n");
				                    	history = false;
				                    	break;
				                    case SmsManager.RESULT_ERROR_RADIO_OFF:
				                    	result = ("Message Non Envoye (Radio off) \r\n\n" + " [" + currentTime + "]" + " \r\n\n");
				                    	history = false;
				                    	break;
				                }
				            }
				        }, new IntentFilter(SENT));
						
						sms.sendTextMessage(phoneNumber, null, message, sentPI, null);
						
				final MessageView v1 = new MessageView(	linearLayout_message.getContext(), null);	
				
				if (history == true){
					result = ("Message Envoye \r\n" + phoneNumber + ": " + message + "\r\n  [" + currentTime +"]" + "\r\n\n");
					v1.setData(phoneNumber, message, currentTime);
					showAlert("Message Sent at : " + currentTime);
					}
				
				else  {
					v1.setData(phoneNumber, "[FAIL] :" + message, currentTime);
					showAlert("SMS failed, please try again later!");
					}

				//linearLayout_message.addView(v1);

	}
	

	private class HTTPServer extends AsyncTask<String, Void, Void> {
		protected Void doInBackground(String... params) {
			try {
				ServerSocket server = new ServerSocket(port);
				Log.i(TAG, "Port Set. Server started!");
				while (true) {
					Socket socket = server.accept();
					if (isCancelled()) {
						socket.close();
						server.close();
						break;
					}
					BufferedReader in = new BufferedReader(
							new InputStreamReader(socket.getInputStream()));
					DataOutputStream out = new DataOutputStream(
							socket.getOutputStream());
					// get the first line of the HTTP GET request
					// sample : GET /?phone=+911234567890&message=HelloWorld HTTP/1.1
					String data = in.readLine();
					// get the substring after GET /?
					data = data.substring(6);
					
					if (data.matches("(?i).*phone=.*") && data.matches("(?i).*&message=.*") ) {
						// get the data before  HTTP/1.1
						data = data.substring(0, data.length() - 9);
						String[] myparams = data.split("&");
						if (data.contains("=")) {
							phoneNumber = myparams[0].split("=")[1];
							message = myparams[1].split("=")[1];
							message = message.replaceAll("%20", " ");
							
							Log.i(TAG, "Got a request to sent an SMS.");
							Log.i(TAG, "Phone Number: " + phoneNumber);
							Log.i(TAG, "Message: " + message);

							sendSMS();
							//result = message +" : "+ phoneNumber;
							//Get the state of sensSMS & return it to the browser
							out.writeBytes(result);
							
						}

					// if the URL doesn't contain the string phone and string message, do nothing.
					else {
						
						showAlert("Invalid URL");
						out.writeBytes("Message Non Envoye (URL Invalide) \r\n" + " [" + currentTime + "]" + " \r\n\n");
					}

					out.writeBytes("HTTP/1.1 200 OK \r\n");
					out.writeBytes("Connection: close\r\n");
					out.writeBytes("\r\n");
					out.close();
					in.close();
				}
			}
		}
				catch (IOException e) {
				e.printStackTrace();
			}
			return null;
			}
	}
}
		
	


