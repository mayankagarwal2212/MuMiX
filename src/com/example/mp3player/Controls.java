package com.example.mp3player;

import java.util.Random;

import android.content.Context;
import android.widget.Toast;

public class Controls {
	static String LOG_CLASS = "Controls";
	static boolean isShuffle = false;
	static boolean isRepeat = false;

	public static void playControl(Context context) {
		sendMessage(context.getResources().getString(R.string.play));
	}

	public static void pauseControl(Context context) {
		sendMessage(context.getResources().getString(R.string.pause));
	}

	public static void nextControl(Context context) {

		boolean isServiceRunning = UtilFunctions.isServiceRunning(SongService.class.getName(), context);
		if (!isServiceRunning)
			return;
		if (PlayerConstants.SONGS_LIST.size() > 0) {
			if (PlayerConstants.SONG_NUMBER < (PlayerConstants.SONGS_LIST.size() - 1)) {
				if (isRepeat == true) {
					if (SongService.repeatCondition == 1) {

					} else
						PlayerConstants.SONG_NUMBER++;
				} else if (isShuffle == false)
					PlayerConstants.SONG_NUMBER++;
				else {
					Random rand = new Random();
					PlayerConstants.SONG_NUMBER = rand.nextInt(PlayerConstants.SONGS_LIST.size() - 1) + 0;
				}
				PlayerConstants.SONG_CHANGE_HANDLER.sendMessage(PlayerConstants.SONG_CHANGE_HANDLER.obtainMessage());
			} else {
				PlayerConstants.SONG_NUMBER = 0;
				PlayerConstants.SONG_CHANGE_HANDLER.sendMessage(PlayerConstants.SONG_CHANGE_HANDLER.obtainMessage());
			}
		}
		PlayerConstants.SONG_PAUSED = false;

	}

	public static void previousControl(Context context) {
		boolean isServiceRunning = UtilFunctions.isServiceRunning(SongService.class.getName(), context);
		if (!isServiceRunning)
			return;
		if (PlayerConstants.SONGS_LIST.size() > 0) {
			if (SongService.mp.getCurrentPosition() < 3000) {
				if (PlayerConstants.SONG_NUMBER > 0) {
					PlayerConstants.SONG_NUMBER--;
					PlayerConstants.SONG_CHANGE_HANDLER
							.sendMessage(PlayerConstants.SONG_CHANGE_HANDLER.obtainMessage());
				} else {
					PlayerConstants.SONG_NUMBER = PlayerConstants.SONGS_LIST.size() - 1;
					PlayerConstants.SONG_CHANGE_HANDLER
							.sendMessage(PlayerConstants.SONG_CHANGE_HANDLER.obtainMessage());
				}
			} else {
				PlayerConstants.SONG_CHANGE_HANDLER.sendMessage(PlayerConstants.SONG_CHANGE_HANDLER.obtainMessage());
			}
		}
		PlayerConstants.SONG_PAUSED = false;
	}

	private static void sendMessage(String message) {
		try {
			PlayerConstants.PLAY_PAUSE_HANDLER
					.sendMessage(PlayerConstants.PLAY_PAUSE_HANDLER.obtainMessage(0, message));
		} catch (Exception e) {
		}
	}

	public static void shuffleControl(Context context) {
		// TODO Auto-generated method stub
		if (isShuffle == true) {
			isShuffle = false;
			Toast.makeText(context, "Shuffle : OFF", Toast.LENGTH_SHORT).show();
		} else {
			isShuffle = true;
			Toast.makeText(context, "Shuffle : ON", Toast.LENGTH_SHORT).show();
		}
	}

	public static void repeatControl(Context context) {
		// TODO Auto-generated method stub
		if (isRepeat == true) {
			isRepeat = false;
			Toast.makeText(context, "Repeat : OFF", Toast.LENGTH_SHORT).show();
		} else {
			isRepeat = true;
			Toast.makeText(context, "Repeat : ON", Toast.LENGTH_SHORT).show();
		}
	}

	public static void playNext(Context applicationContext) {
		// TODO Auto-generated method stub
		PlayerConstants.SONG_NUMBER = MainActivity.pos - 1;
	}
}
