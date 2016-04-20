package com.example.mp3player;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.View;

public class SettingsActivity extends PreferenceActivity {
	static int color = Color.parseColor("#006400");

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.pref);
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		SharedPreferences s = PreferenceManager.getDefaultSharedPreferences(this);
		String skin_color = s.getString("skinColor", "Green");

		if (skin_color.equals("Green")) {
			color = Color.parseColor("#006400");
		} else if (skin_color.equals("Magenta")) {
			color = Color.MAGENTA;
		} else if (skin_color.equals("Blue")) {
			color = Color.BLUE;
		} else if (skin_color.equals("Light Green")) {
			color = Color.GREEN;
		} else if (skin_color.equals("Grey")) {
			color = Color.GRAY;
		} else if (skin_color.equals("Red")) {
			color = Color.RED;
		} else if (skin_color.equals("Yellow")) {
			color = Color.YELLOW;
		} else if (skin_color.equals("Orange")) {
			color = Color.parseColor("#ff6600");
		}

		MainActivity.tb.getTabWidget().setBackgroundColor(SettingsActivity.color);
		MainActivity.linearLayoutPlayingSong.setBackgroundColor(color);

		if (SongService.mp == null) {

		} else {
			String msg1 = s.getString("stime", "Never");
			if (msg1.equals(SongService.msg)) {

			} else {
				SongService.msg = msg1;
				if (SongService.handlesong == null) {

				} else {
					SongService.handlesong.removeCallbacksAndMessages(null);
				}
				SongService.timetostop();
				if (SongService.timestop != 10) {
					SongService.handlesong = new Handler();

					SongService.handlesong.postDelayed(new Runnable() {
						@Override
						public void run() {
							Intent i = new Intent(getApplicationContext(), SongService.class);
							stopService(i);
							MainActivity.linearLayoutPlayingSong.setVisibility(View.GONE);
						}

					}, SongService.timestop);
				}
			}

		}
		super.onBackPressed();
	}
}
