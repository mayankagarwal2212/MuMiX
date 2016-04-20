package com.example.mp3player;

import java.io.File;
import java.util.Random;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TabHost;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnSeekBarChangeListener {
	String LOG_CLASS = "MainActivity";
	static String name1;
	static String name;
	CustomAdapter customAdapter = null;
	CustomAdapterAlbum customAdapter1 = null;
	static TextView playingSong;
	Button btnPlayer;
	static Button btnPause, btnPlay, btnNext, btnPrevious, btnRepeat;
	LinearLayout mediaLayout;
	static LinearLayout linearLayoutPlayingSong;
	static ListView mediaListView, albumListView;
	SeekBar seekBar;
	TextView textBufferDuration, textDuration;
	static ImageView imageViewAlbumArt;
	static Context context;
	static int pos = 0, temp = 0;
	static MediaItem songData;
	static EditText textForSearchSong, textForSearchAlbum;
	static TabHost tb;
	TabSpec spec;
	static int wait_for_album = 0;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// wake-lock
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		final ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		BitmapDrawable background = new BitmapDrawable(BitmapFactory.decodeResource(getResources(), R.drawable.bg1));
		background.setTileModeX(android.graphics.Shader.TileMode.REPEAT);
		actionBar.setBackgroundDrawable(background);

		context = MainActivity.this;
		init();
		if (SongService.mp == null) {
			SharedPreferences s = PreferenceManager.getDefaultSharedPreferences(this);
			String msg = s.getString("welcometext", "Developed by Mayank Agarwal");
			Toast.makeText(this, "Hello " + msg, Toast.LENGTH_SHORT).show();
			String skin_color = s.getString("skinColor", "Green");
			if (skin_color.equals("Green")) {
				SettingsActivity.color = Color.parseColor("#006400");
			} else if (skin_color.equals("Magenta")) {
				SettingsActivity.color = Color.MAGENTA;
			} else if (skin_color.equals("Blue")) {
				SettingsActivity.color = Color.BLUE;
			} else if (skin_color.equals("Light Green")) {
				SettingsActivity.color = Color.GREEN;
			} else if (skin_color.equals("Grey")) {
				SettingsActivity.color = Color.GRAY;
			} else if (skin_color.equals("Red")) {
				SettingsActivity.color = Color.RED;
			} else if (skin_color.equals("Yellow")) {
				SettingsActivity.color = Color.YELLOW;
			} else if (skin_color.equals("Orange")) {
				SettingsActivity.color = Color.parseColor("#ff6600");
			}
			tb.getTabWidget().setBackgroundColor(SettingsActivity.color);
			linearLayoutPlayingSong.setBackgroundColor(SettingsActivity.color);
		}
	}

	private void init() {
		tb = (TabHost) findViewById(R.id.tabhost);
		tb.setup();

		spec = tb.newTabSpec("tag1");
		spec.setContent(R.id.tab1);
		spec.setIndicator("Songs");
		tb.addTab(spec);
		spec = tb.newTabSpec("tag2");
		spec.setContent(R.id.tab2);
		spec.setIndicator("Album");
		tb.addTab(spec);
		tb.getTabWidget().setBackgroundColor(SettingsActivity.color);
		getViews();
		setListeners();
		playingSong.setSelected(true);
		seekBar.getProgressDrawable().setColorFilter(

		getResources().getColor(R.color.white), Mode.SRC_IN);
		if (PlayerConstants.SONGS_LIST.size() <= 0) {
			PlayerConstants.SONGS_LIST = UtilFunctions.listOfSongs(getApplicationContext());
		}
		if (PlayerConstants.ALBUM_LIST.size() <= 0) {
			PlayerConstants.ALBUM_LIST = UtilFunctions.listOfAlbum(getApplicationContext());
		}
		setListItems();
	}

	private void setListItems() {
		customAdapter = new CustomAdapter(this, R.layout.custom_list, PlayerConstants.SONGS_LIST);
		mediaListView.setAdapter(customAdapter);
		customAdapter1 = new CustomAdapterAlbum(this, R.layout.album_list, PlayerConstants.ALBUM_LIST);
		albumListView.setAdapter(customAdapter1);
	}

	private void getViews() {
		playingSong = (TextView) findViewById(R.id.textNowPlaying);
		btnRepeat = (Button) findViewById(R.id.btnRepeat);
		btnPlayer = (Button) findViewById(R.id.btnMusicPlayer);
		mediaListView = (ListView) findViewById(R.id.listViewMusic);
		mediaListView.setTextFilterEnabled(true);
		mediaLayout = (LinearLayout) findViewById(R.id.linearLayoutMusicList);
		btnPause = (Button) findViewById(R.id.btnPause);
		btnPlay = (Button) findViewById(R.id.btnPlay);
		linearLayoutPlayingSong = (LinearLayout) findViewById(R.id.linearLayoutPlayingSong);
		linearLayoutPlayingSong.setBackgroundColor(SettingsActivity.color);

		seekBar = (SeekBar) findViewById(R.id.SeekBar1);
		textBufferDuration = (TextView) findViewById(R.id.textBufferDuration);
		textDuration = (TextView) findViewById(R.id.textDuration);
		imageViewAlbumArt = (ImageView) findViewById(R.id.imageViewAlbumArt);
		btnNext = (Button) findViewById(R.id.btnNext);
		btnPrevious = (Button) findViewById(R.id.btnPrevious);
		registerForContextMenu(mediaListView);
		seekBar.setOnSeekBarChangeListener(this);
		albumListView = (ListView) findViewById(R.id.albumList);
		registerForContextMenu(albumListView);
		albumListView.setTextFilterEnabled(true);
		textForSearchSong = (EditText) findViewById(R.id.editText1);
		textForSearchAlbum = (EditText) findViewById(R.id.editText2);

		textForSearchSong.setVisibility(View.GONE);
		textForSearchAlbum.setVisibility(View.GONE);
		mediaListView.setTextFilterEnabled(true);

		textForSearchSong.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				PlayerConstants.SONGS_LIST = UtilFunctions.listOfSongs(getApplicationContext());
				setListItems();
			}
		});

		textForSearchAlbum.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				PlayerConstants.ALBUM_LIST = UtilFunctions.listOfAlbum(getApplicationContext());
				setListItems();
			}
		});

	}

	private void setListeners() {

		albumListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				// TODO Auto-generated method stub

				MediaItem detail = CustomAdapterAlbum.listOfAlbum.get(arg2);
				String name = detail.getAlbum();
				int i = 0;
				Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
				Cursor c = context.getContentResolver().query(uri, null, MediaStore.Audio.Media.IS_MUSIC + " != 0",
						null, MediaStore.Audio.Media.IS_MUSIC + " ASC");

				String[] songs_list = new String[c.getCount()];
				c.moveToFirst();
				while (c.moveToNext()) {
					String title = c.getString(c.getColumnIndex(MediaStore.Audio.Media.TITLE));
					String album = c.getString(c.getColumnIndex(MediaStore.Audio.Media.ALBUM));
					if (album.equals(name)) {
						songs_list[i] = title;
						i++;
					}
				}
				c.close();
				final String[] options_items = new String[i];
				for (int j = 0; j < i; j++) {
					options_items[j] = songs_list[j];
				}

				AlertDialog.Builder options_builder = new AlertDialog.Builder(context);
				options_builder.setTitle(name);
				options_builder.setItems(options_items, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
						Cursor c = context.getContentResolver().query(uri, null,
								MediaStore.Audio.Media.IS_MUSIC + " != 0", null,
								MediaStore.Audio.Media.IS_MUSIC + " ASC");

						c.moveToFirst();
						while (c.moveToNext()) {
							String title = c.getString(c.getColumnIndex(MediaStore.Audio.Media.TITLE));
							if (title.equals(options_items[item])) {
								int k = c.getPosition();
								Log.d("TAG", "TAG Tapped INOUT(IN)");
								PlayerConstants.SONG_PAUSED = false;
								PlayerConstants.SONG_NUMBER = k - 1;
								boolean isServiceRunning = UtilFunctions.isServiceRunning(SongService.class.getName(),
										getApplicationContext());
								if (!isServiceRunning) {
									Intent i = new Intent(getApplicationContext(), SongService.class);
									startService(i);
								} else {
									PlayerConstants.SONG_CHANGE_HANDLER
											.sendMessage(PlayerConstants.SONG_CHANGE_HANDLER.obtainMessage());
								}
								updateUI();
								changeButton();
								Log.d("TAG", "TAG Tapped INOUT(OUT)");
								break;
							}
						}
						c.close();
					}
				});
				options_builder.show();

				if (textForSearchAlbum.getVisibility() == View.VISIBLE) {
					textForSearchAlbum.setText("");
					textForSearchAlbum.setVisibility(View.GONE);
					InputMethodManager inputManager = (InputMethodManager) getSystemService(
							Context.INPUT_METHOD_SERVICE);
					inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
							InputMethodManager.HIDE_NOT_ALWAYS);
				}

			}
		});

		albumListView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				// TODO Auto-generated method stub
				temp = position;
				return false;
			}
		});

		mediaListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View item, int position, long id) {
				Log.d("TAG", "TAG Tapped INOUT(IN)");
				MediaItem detail = CustomAdapter.listOfSongs.get(position);
				String songName = detail.getTitle();

				Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
				Cursor c = context.getContentResolver().query(uri, null, MediaStore.Audio.Media.IS_MUSIC + " != 0",
						null, MediaStore.Audio.Media.IS_MUSIC + " ASC");
				c.moveToFirst();
				while (c.moveToNext()) {
					String title = c.getString(c.getColumnIndex(MediaStore.Audio.Media.TITLE));
					if (title.equals(songName)) {
						position = c.getPosition() - 1;
					}
				}
				PlayerConstants.SONG_PAUSED = false;
				PlayerConstants.SONG_NUMBER = position;
				boolean isServiceRunning = UtilFunctions.isServiceRunning(SongService.class.getName(),
						getApplicationContext());
				if (!isServiceRunning) {
					Intent i = new Intent(getApplicationContext(), SongService.class);
					startService(i);
				} else {
					PlayerConstants.SONG_CHANGE_HANDLER
							.sendMessage(PlayerConstants.SONG_CHANGE_HANDLER.obtainMessage());
				}
				Log.d("TAG", "TAG Tapped INOUT(OUT)");
				if (textForSearchSong.getVisibility() == View.VISIBLE) {
					textForSearchSong.setText("");
					textForSearchSong.setVisibility(View.GONE);
					linearLayoutPlayingSong.setVisibility(View.VISIBLE);
					InputMethodManager inputManager = (InputMethodManager) getSystemService(
							Context.INPUT_METHOD_SERVICE);
					inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
							InputMethodManager.HIDE_NOT_ALWAYS);
				}
				updateUI();
				changeButton();

			}
		});

		mediaListView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				// TODO Auto-generated method stub
				pos = position;
				return false;
			}
		});

		btnRepeat.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Controls.repeatControl(getApplicationContext());
				if (Controls.isRepeat == true) {
					btnRepeat.setBackgroundResource(R.drawable.repeat_click);
				} else {
					btnRepeat.setBackgroundResource(R.drawable.repeat1);
				}
			}
		});

		btnPlayer.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Controls.shuffleControl(getApplicationContext());
				if (Controls.isShuffle == true) {
					btnPlayer.setBackgroundResource(R.drawable.shuffle_click);
				} else {
					btnPlayer.setBackgroundResource(R.drawable.shuffle1);
				}
			}
		});
		btnPlay.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Controls.playControl(getApplicationContext());
			}
		});
		btnPause.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Controls.pauseControl(getApplicationContext());
			}
		});
		btnNext.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Controls.nextControl(getApplicationContext());
			}
		});
		btnPrevious.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Controls.previousControl(getApplicationContext());
			}
		});
		imageViewAlbumArt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent(MainActivity.this, AudioPlayerActivity.class);
				startActivity(i);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		MenuInflater mn = getMenuInflater();
		mn.inflate(R.menu.main_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		if (item.getItemId() == R.id.details) {
			Dialog mdialog = new Dialog(this);
			mdialog.setContentView(R.layout.help);
			mdialog.setTitle("Help");
			mdialog.setCancelable(true);
			mdialog.show();
		} else if (item.getItemId() == R.id.settings) {
			Intent set = new Intent(context, SettingsActivity.class);
			startActivity(set);
		} else if (item.getItemId() == R.id.action_search) {
			if (tb.getCurrentTab() == 0) {
				if (textForSearchSong.getVisibility() == View.GONE) {
					textForSearchSong.setVisibility(View.VISIBLE);
					textForSearchSong.requestFocus();
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.showSoftInput(textForSearchSong, InputMethodManager.SHOW_IMPLICIT);
					linearLayoutPlayingSong.setVisibility(View.GONE);

				} else {
					textForSearchSong.setText("");
					textForSearchSong.setVisibility(View.GONE);
					linearLayoutPlayingSong.setVisibility(View.VISIBLE);
					InputMethodManager inputManager = (InputMethodManager) getSystemService(
							Context.INPUT_METHOD_SERVICE);
					inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
							InputMethodManager.HIDE_NOT_ALWAYS);
				}

			} else {
				if (textForSearchAlbum.getVisibility() == View.GONE) {
					textForSearchAlbum.setVisibility(View.VISIBLE);
					textForSearchAlbum.requestFocus();
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.showSoftInput(textForSearchAlbum, InputMethodManager.SHOW_IMPLICIT);
					linearLayoutPlayingSong.setVisibility(View.GONE);
				} else {
					textForSearchAlbum.setText("");
					textForSearchAlbum.setVisibility(View.GONE);
					InputMethodManager inputManager = (InputMethodManager) getSystemService(
							Context.INPUT_METHOD_SERVICE);
					inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
							InputMethodManager.HIDE_NOT_ALWAYS);
				}
			}
		} else if (item.getItemId() == R.id.refresh) {
			PlayerConstants.SONGS_LIST = UtilFunctions.listOfSongs(getApplicationContext());
			PlayerConstants.ALBUM_LIST = UtilFunctions.listOfAlbum(getApplicationContext());
			setListItems();
			Toast.makeText(context, "Operation Completed!!", Toast.LENGTH_SHORT).show();
		} else if (item.getItemId() == R.id.shufmusic) {
			PlayerConstants.SONG_PAUSED = false;
			Random rand = new Random();
			PlayerConstants.SONG_NUMBER = rand.nextInt(PlayerConstants.SONGS_LIST.size() - 1) + 0;
			boolean isServiceRunning = UtilFunctions.isServiceRunning(SongService.class.getName(),
					getApplicationContext());
			if (!isServiceRunning) {
				Intent i = new Intent(getApplicationContext(), SongService.class);
				startService(i);
			} else {
				PlayerConstants.SONG_CHANGE_HANDLER.sendMessage(PlayerConstants.SONG_CHANGE_HANDLER.obtainMessage());
			}
			updateUI();
			changeButton();
			Log.d("TAG", "TAG Tapped INOUT(OUT)");
			Controls.isShuffle = true;
			btnPlayer.setBackgroundResource(R.drawable.shuffle_click);
		} else
			finish();
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		// TODO Auto-generated method stub
		super.onCreateContextMenu(menu, v, menuInfo);
		if (tb.getCurrentTab() == 0) {
			MenuInflater mi = getMenuInflater();
			mi.inflate(R.menu.main, menu);
		} else if (tb.getCurrentTab() == 1) {
			MenuInflater mi = getMenuInflater();
			mi.inflate(R.menu.main_1, menu);
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {

		if (item.getItemId() == R.id.playnxt) {
			boolean isServiceRunning = UtilFunctions.isServiceRunning(SongService.class.getName(), context);
			if (isServiceRunning)
				Controls.playNext(getApplicationContext());
			else {
				Log.d("TAG", "TAG Tapped INOUT(IN)");
				PlayerConstants.SONG_PAUSED = false;
				PlayerConstants.SONG_NUMBER = pos;
				Intent i = new Intent(getApplicationContext(), SongService.class);
				startService(i);
				updateUI();
				changeButton();
				Log.d("TAG", "TAG Tapped INOUT(OUT)");
				Controls.isShuffle = false;
				Controls.isRepeat = false;
				btnPlayer.setBackgroundResource(R.drawable.shuffle1);
				btnRepeat.setBackgroundResource(R.drawable.repeat1);
			}
		} else if (item.getItemId() == R.id.media_info) {
			Intent media_inf = new Intent(context, Detail.class);
			media_inf.putExtra("num", pos);
			startActivity(media_inf);
		} else if (item.getItemId() == R.id.menu_item_share) {

			Intent share = new Intent(Intent.ACTION_SEND);
			share.setType("audio/*");
			share.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + PlayerConstants.SONGS_LIST.get(pos).path));
			startActivity(Intent.createChooser(share, "Share Sound File"));
		} else if (item.getItemId() == R.id.delete_media) {

			if (pos == PlayerConstants.SONG_NUMBER) {
				AlertDialog.Builder build = new AlertDialog.Builder(this);
				build.setMessage("Media busy!!! Try again later");
				build.setCancelable(true);
				build.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						dialog.cancel();
					}
				});
				AlertDialog alert = build.create();
				alert.show();

			} else {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage("Are you sure you want to delete ?")
						.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								File f_media = new File(PlayerConstants.SONGS_LIST.get(pos).path);
								f_media.delete();
								context.getContentResolver().delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
										MediaStore.Audio.Media._ID + "=" + PlayerConstants.SONGS_LIST.get(pos).songId,
										null);

								PlayerConstants.SONGS_LIST = UtilFunctions.listOfSongs(getApplicationContext());
								PlayerConstants.ALBUM_LIST = UtilFunctions.listOfAlbum(getApplicationContext());
								setListItems();
								Toast.makeText(context, "Deleted!!", Toast.LENGTH_SHORT).show();

							}
						}).setNegativeButton("No", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {

							}
						});
				AlertDialog alert = builder.create();
				alert.show();
			}
		} else if (item.getItemId() == R.id.playnxtAlbum) {

			MediaItem detail = CustomAdapterAlbum.listOfAlbum.get(temp);
			name = detail.getAlbum();
			name1 = PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getAlbum();
			Toast.makeText(context, "Playing: " + name1, Toast.LENGTH_SHORT).show();
			Toast.makeText(context, "Next: " + name, Toast.LENGTH_SHORT).show();
			wait_for_album = 1;

		} else if (item.getItemId() == R.id.shflAlbum) {
			int k;
			MediaItem detail = CustomAdapterAlbum.listOfAlbum.get(temp);
			String name = detail.getAlbum();
			Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
			Cursor c = context.getContentResolver().query(uri, null, MediaStore.Audio.Media.IS_MUSIC + " != 0", null,
					MediaStore.Audio.Media.IS_MUSIC + " ASC");
			c.moveToFirst();
			while (c.moveToNext()) {
				String album = c.getString(c.getColumnIndex(MediaStore.Audio.Media.ALBUM));
				if (album.equals(name)) {
					k = c.getPosition();
					PlayerConstants.SONG_PAUSED = false;
					PlayerConstants.SONG_NUMBER = k - 1;
					boolean isServiceRunning = UtilFunctions.isServiceRunning(SongService.class.getName(),
							getApplicationContext());
					if (!isServiceRunning) {
						Intent in = new Intent(getApplicationContext(), SongService.class);
						startService(in);
					} else {
						PlayerConstants.SONG_CHANGE_HANDLER
								.sendMessage(PlayerConstants.SONG_CHANGE_HANDLER.obtainMessage());
					}
					updateUI();
					changeButton();
					break;
				}
			}
			c.close();
		}
		return super.onContextItemSelected(item);
	}

	@SuppressLint("HandlerLeak")
	@Override
	protected void onResume() {
		super.onResume();
		if (Controls.isShuffle == true) {
			btnPlayer.setBackgroundResource(R.drawable.shuffle_click);
		} else {
			btnPlayer.setBackgroundResource(R.drawable.shuffle1);
		}
		if (Controls.isRepeat == true) {
			btnRepeat.setBackgroundResource(R.drawable.repeat_click);
		} else {
			btnRepeat.setBackgroundResource(R.drawable.repeat1);
		}
		try {
			boolean isServiceRunning = UtilFunctions.isServiceRunning(SongService.class.getName(),
					getApplicationContext());
			if (isServiceRunning) {
				updateUI();
			} else {
				linearLayoutPlayingSong.setVisibility(View.GONE);
			}
			changeButton();
			PlayerConstants.PROGRESSBAR_HANDLER = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					Integer i[] = (Integer[]) msg.obj;
					textBufferDuration.setText(UtilFunctions.getDuration(i[0]));
					textDuration.setText(UtilFunctions.getDuration(i[1]));
					seekBar.setProgress(i[2]);
				}
			};
		} catch (Exception e) {
		}
	}

	@SuppressWarnings("deprecation")
	public static void updateUI() {
		try {
			MediaItem data = PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER);
			playingSong.setText(data.getTitle() + " " + data.getArtist() + "-" + data.getAlbum());
			Bitmap albumArt = UtilFunctions.getAlbumart(context, data.getAlbumId());
			if (albumArt != null) {
				imageViewAlbumArt.setBackgroundDrawable(new BitmapDrawable(albumArt));
			} else {
				imageViewAlbumArt.setBackgroundDrawable(new BitmapDrawable(UtilFunctions.getDefaultAlbumArt(context)));
			}
			linearLayoutPlayingSong.setVisibility(View.VISIBLE);
		} catch (Exception e) {
		}
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

	public static void changeUI() {
		updateUI();
		changeButton();
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		// TODO Auto-generated method stub
		if (fromUser)
			SongService.mp.seekTo((seekBar.getProgress() * SongService.mp.getDuration()) / 100);
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onBackPressed() {

		if (textForSearchSong.getVisibility() == View.VISIBLE) {
			textForSearchSong.setText("");
			textForSearchSong.setVisibility(View.GONE);
		} else if (textForSearchAlbum.getVisibility() == View.VISIBLE) {
			textForSearchAlbum.setText("");
			textForSearchAlbum.setVisibility(View.GONE);
		} else if (linearLayoutPlayingSong.getVisibility() == View.GONE || SongService.mp.isPlaying()) {
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_HOME);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
			finish();
		} else {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Do you want to exit or minimize?")
					.setPositiveButton("Exit", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							/*
							 * SharedPreferences pref =
							 * getSharedPreferences("music_app", 0);
							 * SharedPreferences.Editor e = pref.edit();
							 * e.putInt("song_number",
							 * PlayerConstants.SONG_NUMBER); e.commit();
							 */
							Intent i = new Intent(getApplicationContext(), SongService.class);
							stopService(i);
							finish();
						}
					}).setNegativeButton("Minimize", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							finish();
						}
					});
			AlertDialog alert = builder.create();
			alert.show();
		}
	}
}