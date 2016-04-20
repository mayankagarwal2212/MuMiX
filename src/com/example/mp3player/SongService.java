package com.example.mp3player;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.RemoteControlClient;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.RemoteControlClient.MetadataEditor;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Handler.Callback;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

public class SongService extends Service implements AudioManager.OnAudioFocusChangeListener {
	String LOG_CLASS = "SongService";
	static MediaPlayer mp;
	int NOTIFICATION_ID = 1111;
	static int m, repeatCondition = 1;
	static int timestop;
	static String msg;
	static Handler handlesong = null;

	public static final String NOTIFY_PREVIOUS = "com.example.mp3player.previous";
	public static final String NOTIFY_DELETE = "com.example.mp3player.delete";
	public static final String NOTIFY_PAUSE = "com.example.mp3player.pause";
	public static final String NOTIFY_PLAY = "com.example.mp3player.play";
	public static final String NOTIFY_NEXT = "com.example.mp3player.next";

	private ComponentName remoteComponentName;
	private RemoteControlClient remoteControlClient;
	AudioManager audioManager;
	Bitmap mDummyAlbumArt;
	private static Timer timer;
	private static boolean currentVersionSupportBigNotification = false;
	private static boolean currentVersionSupportLockScreenControls = false;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		mp = new MediaPlayer();
		audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
		currentVersionSupportBigNotification = UtilFunctions.currentVersionSupportBigNotification();
		currentVersionSupportLockScreenControls = UtilFunctions.currentVersionSupportLockScreenControls();
		timer = new Timer();
		SharedPreferences s = PreferenceManager.getDefaultSharedPreferences(this);
		msg = s.getString("stime", "Never");
		timetostop();
		if (timestop != 10) {
			handlesong = new Handler();
			handlesong.postDelayed(new Runnable() {
				@Override
				public void run() {
					Intent i = new Intent(getApplicationContext(), SongService.class);
					stopService(i);
					MainActivity.linearLayoutPlayingSong.setVisibility(View.GONE);
				}

			}, timestop);
		}
		PhoneStateListener phoneStateListener = new PhoneStateListener() {
			@Override
			public void onCallStateChanged(int state, String incomingNumber) {
				if (state == TelephonyManager.CALL_STATE_RINGING) {
					// INCOMING call
					if (mp.isPlaying()) {
						m = 1;
						mp.pause();
					}
				} else if (state == TelephonyManager.CALL_STATE_IDLE) {
					// Not IN CALL
					if (m == 1) {
						m = 0;
						mp.start();
					}
				} else if (state == TelephonyManager.CALL_STATE_OFFHOOK) {
					// A call is dialing, active or on hold
					if (mp.isPlaying()) {
						m = 1;
						mp.pause();
					}
				}
				super.onCallStateChanged(state, incomingNumber);
			}
		};// end PhoneStateListener

		TelephonyManager mgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		if (mgr != null) {
			mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
		}

		mp.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				repeatCondition = 1;
				Controls.nextControl(getApplicationContext());
				repeatCondition = 0;
			}
		});
		super.onCreate();
	}

	static void timetostop() {
		// TODO Auto-generated method stub

		if (msg.equals("2 minutes")) {
			timestop = 120000;
		} else if (msg.equals("15 minutes")) {
			timestop = 900000;
		} else if (msg.equals("30 minutes")) {
			timestop = 1800000;
		} else if (msg.equals("60 minutes")) {
			timestop = 3600000;
		} else if (msg.equals("90 minutes")) {
			timestop = 5400000;
		} else if (msg.equals("120 minutes")) {
			timestop = 7200000;
		} else {
			timestop = 10;
		}
	}

	private class MainTask extends TimerTask {
		public void run() {
			handler.sendEmptyMessage(0);
		}
	}

	@SuppressLint("HandlerLeak")
	private final Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (mp != null) {
				int progress = (mp.getCurrentPosition() * 100) / mp.getDuration();
				Integer i[] = new Integer[3];
				i[0] = mp.getCurrentPosition();
				i[1] = mp.getDuration();
				i[2] = progress;
				try {
					PlayerConstants.PROGRESSBAR_HANDLER.sendMessage(PlayerConstants.PROGRESSBAR_HANDLER.obtainMessage(
							0, i));
				} catch (Exception e) {
				}
			}
		}
	};

	@SuppressLint("NewApi")
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		try {
			if (PlayerConstants.SONGS_LIST.size() <= 0) {
				PlayerConstants.SONGS_LIST = UtilFunctions.listOfSongs(getApplicationContext());
			}
			MediaItem data = PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER);
			if (currentVersionSupportLockScreenControls) {
				RegisterRemoteClient();
			}
			String songPath = data.getPath();
			playSong(songPath, data);
			newNotification();

			PlayerConstants.SONG_CHANGE_HANDLER = new Handler(new Callback() {
				@Override
				public boolean handleMessage(Message msg) {
					MediaItem data = PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER);
					String songPath = data.getPath();
					newNotification();
					try {
						playSong(songPath, data);
						MainActivity.changeUI();
						AudioPlayerActivity.changeUI();
					} catch (Exception e) {
						e.printStackTrace();
					}
					return false;
				}
			});

			PlayerConstants.PLAY_PAUSE_HANDLER = new Handler(new Callback() {
				@Override
				public boolean handleMessage(Message msg) {
					String message = (String) msg.obj;
					if (mp == null)
						return false;
					if (message.equalsIgnoreCase(getResources().getString(R.string.play))) {
						PlayerConstants.SONG_PAUSED = false;
						if (currentVersionSupportLockScreenControls) {
							remoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);
						}
						mp.start();
					} else if (message.equalsIgnoreCase(getResources().getString(R.string.pause))) {
						PlayerConstants.SONG_PAUSED = true;
						if (currentVersionSupportLockScreenControls) {
							remoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PAUSED);
						}
						mp.pause();
					}
					newNotification();
					try {
						MainActivity.changeButton();
						AudioPlayerActivity.changeButton();
					} catch (Exception e) {
					}
					Log.d("TAG", "TAG Pressed: " + message);
					return false;
				}
			});

		} catch (Exception e) {
			e.printStackTrace();
		}
		return START_STICKY;
	}

	@SuppressLint("NewApi")
	private void newNotification() {
		String songName = PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getTitle();
		String albumName = PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getAlbum();
		RemoteViews simpleContentView = new RemoteViews(getApplicationContext().getPackageName(),
				R.layout.custom_notification);
		RemoteViews expandedView = new RemoteViews(getApplicationContext().getPackageName(), R.layout.big_notification);

		Notification notification = new NotificationCompat.Builder(getApplicationContext())
				.setSmallIcon(R.drawable.ic_music).setContentTitle(songName).build();

		setListeners(simpleContentView);
		setListeners(expandedView);

		notification.contentView = simpleContentView;
		if (currentVersionSupportBigNotification) {
			notification.bigContentView = expandedView;
		}

		try {
			long albumId = PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getAlbumId();
			Bitmap albumArt = UtilFunctions.getAlbumart(getApplicationContext(), albumId);
			if (albumArt != null) {
				notification.contentView.setImageViewBitmap(R.id.imageViewAlbumArt, albumArt);
				if (currentVersionSupportBigNotification) {
					notification.bigContentView.setImageViewBitmap(R.id.imageViewAlbumArt, albumArt);
				}
			} else {
				notification.contentView.setImageViewResource(R.id.imageViewAlbumArt, R.drawable.default_album_art);
				if (currentVersionSupportBigNotification) {
					notification.bigContentView.setImageViewResource(R.id.imageViewAlbumArt,
							R.drawable.default_album_art);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (PlayerConstants.SONG_PAUSED) {
			notification.contentView.setViewVisibility(R.id.btnPause, View.GONE);
			notification.contentView.setViewVisibility(R.id.btnPlay, View.VISIBLE);

			if (currentVersionSupportBigNotification) {
				notification.bigContentView.setViewVisibility(R.id.btnPause, View.GONE);
				notification.bigContentView.setViewVisibility(R.id.btnPlay, View.VISIBLE);
			}
		} else {
			notification.contentView.setViewVisibility(R.id.btnPause, View.VISIBLE);
			notification.contentView.setViewVisibility(R.id.btnPlay, View.GONE);

			if (currentVersionSupportBigNotification) {
				notification.bigContentView.setViewVisibility(R.id.btnPause, View.VISIBLE);
				notification.bigContentView.setViewVisibility(R.id.btnPlay, View.GONE);
			}
		}

		notification.contentView.setTextViewText(R.id.textSongName, songName);
		notification.contentView.setTextViewText(R.id.textAlbumName, albumName);
		if (currentVersionSupportBigNotification) {
			notification.bigContentView.setTextViewText(R.id.textSongName, songName);
			notification.bigContentView.setTextViewText(R.id.textAlbumName, albumName);
		}
		notification.flags |= Notification.FLAG_ONGOING_EVENT;
		startForeground(NOTIFICATION_ID, notification);
	}

	public void setListeners(RemoteViews view) {
		Intent previous = new Intent(NOTIFY_PREVIOUS);
		Intent delete = new Intent(NOTIFY_DELETE);
		Intent pause = new Intent(NOTIFY_PAUSE);
		Intent next = new Intent(NOTIFY_NEXT);
		Intent play = new Intent(NOTIFY_PLAY);
		Intent openMain = new Intent(getApplicationContext(), AudioPlayerActivity.class);

		PendingIntent pMain = PendingIntent.getActivity(getApplicationContext(), 0, openMain,
				PendingIntent.FLAG_UPDATE_CURRENT);
		view.setOnClickPendingIntent(R.id.imageViewAlbumArt, pMain);

		PendingIntent pPrevious = PendingIntent.getBroadcast(getApplicationContext(), 0, previous,
				PendingIntent.FLAG_UPDATE_CURRENT);
		view.setOnClickPendingIntent(R.id.btnPrevious, pPrevious);

		PendingIntent pDelete = PendingIntent.getBroadcast(getApplicationContext(), 0, delete,
				PendingIntent.FLAG_UPDATE_CURRENT);
		view.setOnClickPendingIntent(R.id.btnDelete, pDelete);

		PendingIntent pPause = PendingIntent.getBroadcast(getApplicationContext(), 0, pause,
				PendingIntent.FLAG_UPDATE_CURRENT);
		view.setOnClickPendingIntent(R.id.btnPause, pPause);

		PendingIntent pNext = PendingIntent.getBroadcast(getApplicationContext(), 0, next,
				PendingIntent.FLAG_UPDATE_CURRENT);
		view.setOnClickPendingIntent(R.id.btnNext, pNext);

		PendingIntent pPlay = PendingIntent.getBroadcast(getApplicationContext(), 0, play,
				PendingIntent.FLAG_UPDATE_CURRENT);
		view.setOnClickPendingIntent(R.id.btnPlay, pPlay);

	}

	@Override
	public void onDestroy() {
		if (mp != null) {
			mp.stop();
			mp = null;
		}
		super.onDestroy();
	}

	@SuppressLint("NewApi")
	private void playSong(String songPath, MediaItem data) {
		try {
			if (currentVersionSupportLockScreenControls) {
				UpdateMetadata(data);
				remoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);
			}
			mp.reset();
			mp.setDataSource(songPath);
			mp.prepare();
			mp.start();
			timer.scheduleAtFixedRate(new MainTask(), 0, 100);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@SuppressLint("NewApi")
	private void RegisterRemoteClient() {
		remoteComponentName = new ComponentName(getApplicationContext(), new NotificationBroadcast().ComponentName());
		try {
			if (remoteControlClient == null) {
				audioManager.registerMediaButtonEventReceiver(remoteComponentName);
				Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
				mediaButtonIntent.setComponent(remoteComponentName);
				PendingIntent mediaPendingIntent = PendingIntent.getBroadcast(this, 0, mediaButtonIntent, 0);
				remoteControlClient = new RemoteControlClient(mediaPendingIntent);
				audioManager.registerRemoteControlClient(remoteControlClient);
			}
			remoteControlClient.setTransportControlFlags(RemoteControlClient.FLAG_KEY_MEDIA_PLAY
					| RemoteControlClient.FLAG_KEY_MEDIA_PAUSE | RemoteControlClient.FLAG_KEY_MEDIA_PLAY_PAUSE
					| RemoteControlClient.FLAG_KEY_MEDIA_STOP | RemoteControlClient.FLAG_KEY_MEDIA_PREVIOUS
					| RemoteControlClient.FLAG_KEY_MEDIA_NEXT);
		} catch (Exception ex) {
		}
	}

	@SuppressLint("NewApi")
	private void UpdateMetadata(MediaItem data) {
		if (remoteControlClient == null)
			return;
		MetadataEditor metadataEditor = remoteControlClient.editMetadata(true);
		metadataEditor.putString(MediaMetadataRetriever.METADATA_KEY_ALBUM, data.getAlbum());
		metadataEditor.putString(MediaMetadataRetriever.METADATA_KEY_ARTIST, data.getArtist());
		metadataEditor.putString(MediaMetadataRetriever.METADATA_KEY_TITLE, data.getTitle());
		mDummyAlbumArt = UtilFunctions.getAlbumart(getApplicationContext(), data.getAlbumId());
		if (mDummyAlbumArt == null) {
			mDummyAlbumArt = BitmapFactory.decodeResource(getResources(), R.drawable.default_album_art);
		}
		metadataEditor.putBitmap(RemoteControlClient.MetadataEditor.BITMAP_KEY_ARTWORK, mDummyAlbumArt);
		metadataEditor.apply();
		audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
	}

	@Override
	public void onAudioFocusChange(int focusChange) {
	}
}