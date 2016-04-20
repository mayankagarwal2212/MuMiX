package com.example.mp3player;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.OnGestureListener;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class AudioPlayerActivity extends Activity implements OnSeekBarChangeListener {

	LinearLayout header, footer;
	GestureDetector gestureDetector;
	Button btn1, btnrev, btnff;
	static Button btnPause, btnShfl, btnRpt;
	static Button btnPlay;
	static TextView textNowPlaying;
	static TextView textAlbumArtist;
	static TextView textComposer;
	static LinearLayout centerLayout;
	static SeekBar seekBar;
	SeekBar sb;
	static Context context;
	TextView textBufferDuration, textDuration;
	static boolean isRepeat, isShuffle;
	AudioManager am;
	int Volume = 0;
	private SensorManager mSensorManager;
	private ShakeEventListener mSensorListener;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.audio_player);
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mSensorListener = new ShakeEventListener();

		mSensorListener.setOnShakeListener(new ShakeEventListener.OnShakeListener() {

			public void onShake() {
				Toast.makeText(context, "Shake!", Toast.LENGTH_SHORT).show();
				Controls.nextControl(getApplicationContext());
			}
		});

		gestureDetector = new GestureDetector(new SwipeGestureDetector());
		context = this;
		init();
		if (Controls.isShuffle == true) {
			btnShfl.setBackgroundResource(R.drawable.shuffle_click);
		} else {
			btnShfl.setBackgroundResource(R.drawable.shuffle1);
		}
		if (Controls.isRepeat == true) {
			btnRpt.setBackgroundResource(R.drawable.repeat_click);
		} else {
			btnRpt.setBackgroundResource(R.drawable.repeat1);
		}
		sb.setVisibility(View.INVISIBLE);
		sb.setEnabled(false);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (gestureDetector.onTouchEvent(event)) {
			return true;
		}
		return super.onTouchEvent(event);
	}

	private void onLeftSwipe() {
		SongService.repeatCondition = 0;
		Controls.nextControl(getApplicationContext());
	}

	private void onRightSwipe() {
		Controls.previousControl(getApplicationContext());
	}

	private class SwipeGestureDetector extends SimpleOnGestureListener implements OnGestureListener {
		private static final int SWIPE_MIN_DISTANCE = 120;
		private static final int SWIPE_MAX_OFF_PATH = 200;
		private static final int SWIPE_THRESHOLD_VELOCITY = 200;

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			try {
				float diffAbs = Math.abs(e1.getY() - e2.getY());
				float diff = e1.getX() - e2.getX();

				if (diffAbs > SWIPE_MAX_OFF_PATH)
					return false;

				if (diff > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
					AudioPlayerActivity.this.onLeftSwipe();
				} else if (-diff > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
					AudioPlayerActivity.this.onRightSwipe();
				}
			} catch (Exception e) {
				Log.e("AudioPlayerActivity", "Error on gestures");
			}
			return false;
		}

		@Override
		public boolean onDown(MotionEvent arg0) {
			// TODO Auto-generated method stub

			return false;
		}

		@Override
		public void onLongPress(MotionEvent arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2, float arg3) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void onShowPress(MotionEvent arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public boolean onSingleTapUp(MotionEvent arg0) {
			// TODO Auto-generated method stub
			if (seekBar.getVisibility() == View.VISIBLE) {
				seekBar.setVisibility(View.GONE);
				textBufferDuration.setVisibility(View.GONE);
				textDuration.setVisibility(View.GONE);
			} else {
				seekBar.setVisibility(View.VISIBLE);
				textBufferDuration.setVisibility(View.VISIBLE);
				textDuration.setVisibility(View.VISIBLE);
			}

			return false;
		}
	}

	@SuppressLint("HandlerLeak")
	private void init() {
		getViews();
		setListeners();
		seekBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.white), Mode.SRC_IN);
		PlayerConstants.PROGRESSBAR_HANDLER = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				Integer i[] = (Integer[]) msg.obj;
				textBufferDuration.setText(UtilFunctions.getDuration(i[0]));
				textDuration.setText(UtilFunctions.getDuration(i[1]));
				seekBar.setProgress(i[2]);
			}
		};
	}

	private void setListeners() {

		btnPause.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Controls.pauseControl(getApplicationContext());
			}
		});

		btnShfl.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Controls.shuffleControl(getApplicationContext());
				if (Controls.isShuffle == true) {
					btnShfl.setBackgroundResource(R.drawable.shuffle_click);
				} else {
					btnShfl.setBackgroundResource(R.drawable.shuffle1);
				}
			}
		});

		btnRpt.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Controls.repeatControl(getApplicationContext());
				if (Controls.isRepeat == true) {
					btnRpt.setBackgroundResource(R.drawable.repeat_click);
				} else {
					btnRpt.setBackgroundResource(R.drawable.repeat1);
				}
			}
		});

		btnPlay.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Controls.playControl(getApplicationContext());
			}
		});
	}

	public static void changeUI() {
		updateUI();
		changeButton();
	}

	private void getViews() {

		header = (LinearLayout) findViewById(R.id.header);
		header.setBackgroundColor(SettingsActivity.color);
		footer = (LinearLayout) findViewById(R.id.footer);
		footer.setBackgroundColor(SettingsActivity.color);
		centerLayout = (LinearLayout) findViewById(R.id.centerLayout);
		btnShfl = (Button) findViewById(R.id.btnShuffle);
		btnrev = (Button) findViewById(R.id.btnfback);
		btnff = (Button) findViewById(R.id.btnfforward);
		btnRpt = (Button) findViewById(R.id.btnRepeat);
		btnPause = (Button) findViewById(R.id.btnPause);
		btnPlay = (Button) findViewById(R.id.btnPlay);
		textNowPlaying = (TextView) findViewById(R.id.textNowPlaying);
		textAlbumArtist = (TextView) findViewById(R.id.textAlbumArtist);
		textComposer = (TextView) findViewById(R.id.textComposer);
		seekBar = (SeekBar) findViewById(R.id.SeekBar1);
		textBufferDuration = (TextView) findViewById(R.id.textBufferDuration);
		textDuration = (TextView) findViewById(R.id.textDuration);
		textNowPlaying.setSelected(true);
		textAlbumArtist.setSelected(true);
		seekBar.setOnSeekBarChangeListener(this);
		sb = (SeekBar) findViewById(R.id.sbVolume);
		am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		int maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		int curVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
		btn1 = (Button) findViewById(R.id.btn1);
		sb.setMax(maxVolume);
		sb.setProgress(curVolume);

		btnff.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (SongService.mp.getCurrentPosition() + 5000 < SongService.mp.getDuration())
					SongService.mp.seekTo(SongService.mp.getCurrentPosition() + 5000);
				else
					SongService.mp.seekTo(SongService.mp.getDuration());
			}
		});

		btnrev.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (SongService.mp.getCurrentPosition() - 5000 > 0)
					SongService.mp.seekTo(SongService.mp.getCurrentPosition() - 5000);
				else
					SongService.mp.seekTo(0);
			}
		});

		btn1.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				int curVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
				sb.setProgress(curVolume);
				// TODO Auto-generated method stub
				if (sb.isEnabled() == true) {
					sb.setVisibility(View.INVISIBLE);
					sb.setEnabled(false);
				} else {
					sb.setVisibility(View.VISIBLE);
					sb.setEnabled(true);
				}
			}
		});

		sb.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				Toast.makeText(getApplicationContext(), "Volume: " + Integer.toString(Volume), Toast.LENGTH_SHORT)
						.show();
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				// TODO Auto-generated method stub
				am.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
				Volume = progress;
			}
		});

	}

	@Override
	protected void onPause() {
		mSensorManager.unregisterListener(mSensorListener);
		super.onPause();
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		Intent i = new Intent(context, MainActivity.class);
		startActivity(i);

		super.onBackPressed();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
			sb.setVisibility(View.VISIBLE);
			sb.setEnabled(true);
			int curVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
			am.setStreamVolume(AudioManager.STREAM_MUSIC, curVolume--, 0);
			sb.setProgress(curVolume);
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
			sb.setVisibility(View.VISIBLE);
			sb.setEnabled(true);
			int curVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
			am.setStreamVolume(AudioManager.STREAM_MUSIC, curVolume++, 0);
			sb.setProgress(curVolume);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_UI);
		boolean isServiceRunning = UtilFunctions.isServiceRunning(SongService.class.getName(), getApplicationContext());
		if (isServiceRunning) {
			updateUI();
		}
		changeButton();
	}

	public static void changeButton() {
		if (PlayerConstants.SONG_PAUSED) {
			btnPause.setVisibility(View.GONE);
			btnPlay.setVisibility(View.VISIBLE);
		} else {
			btnPause.setVisibility(View.VISIBLE);
			btnPlay.setVisibility(View.GONE);
		}
	}

	@SuppressWarnings("deprecation")
	private static void updateUI() {
		try {
			String songName = PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getTitle();
			String artist = PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getArtist();
			String album = PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getAlbum();
			String composer = PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getComposer();
			textNowPlaying.setText(songName);
			textAlbumArtist.setText(artist + " - " + album);
			if (composer != null && composer.length() > 0) {
				textComposer.setVisibility(View.VISIBLE);
				textComposer.setText(composer);
			} else {
				textComposer.setVisibility(View.GONE);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			long albumId = PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getAlbumId();
			Bitmap albumArt = UtilFunctions.getAlbumart(context, albumId);
			if (albumArt != null) {
				centerLayout.setBackgroundDrawable(new BitmapDrawable(albumArt));
			} else {
				centerLayout.setBackgroundDrawable(new BitmapDrawable(UtilFunctions.getDefaultAlbumArt(context)));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		// TODO Auto-generated method stub
		if (fromUser)
			SongService.mp.seekTo((seekBar.getProgress() * SongService.mp.getDuration()) / 100);
	}

	@Override
	public void onStartTrackingTouch(final SeekBar seekBar) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStopTrackingTouch(final SeekBar seekBar) {
		// TODO Auto-generated method stub
	}
}
