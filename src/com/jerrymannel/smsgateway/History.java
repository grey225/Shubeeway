package com.jerrymannel.smsgateway;

import com.sHubeeway.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class History extends Activity {
	
	
	
		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.history);
}
		public boolean onCreateOptionsMenu(Menu menu) {
			MenuInflater mi = getMenuInflater();
			mi.inflate(R.menu.options_menu, menu);
			return true;
		}

		public boolean onOptionsItemSelected(MenuItem item) {
			switch (item.getItemId()) {
			case R.id.item_clear:
			//	linearLayout_message.removeAllViews();
				break;
			case R.id.item_setting:
				startActivity(new Intent(this, SettingsView.class));
				break;
			case R.id.item_about:
				startActivity(new Intent(this, AboutView.class));
				break;
			case R.id.item_history:
				startActivity(new Intent(this, History.class));
				break;
			}
			return super.onOptionsItemSelected(item);
		}
}
