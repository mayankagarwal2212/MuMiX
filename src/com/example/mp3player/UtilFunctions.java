package com.example.mp3player;

import java.io.FileDescriptor;
import java.util.ArrayList;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;

public class UtilFunctions {
	static String LOG_CLASS = "UtilFunctions";
	static MediaItem songData;

	public static boolean isServiceRunning(String serviceName, Context context) {
		ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (serviceName.equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	public static ArrayList<MediaItem> listOfAlbum(Context context) {

		Cursor c1 = context.getContentResolver().query(MediaStore.Audio.Albums.getContentUri("external"),
				new String[] { MediaStore.Audio.Albums.ARTIST, MediaStore.Audio.Albums._ID,
						MediaStore.Audio.Albums.NUMBER_OF_SONGS, MediaStore.Audio.Albums.ALBUM },
				null, null, MediaStore.Audio.Albums.ALBUM + " ASC");
		ArrayList<MediaItem> listOfAlbum = new ArrayList<MediaItem>();

		if (c1 != null && c1.moveToFirst()) {
			int idColumn = c1.getColumnIndex(android.provider.MediaStore.Audio.Media._ID);
			int artistColumn = c1.getColumnIndex(android.provider.MediaStore.Audio.Media.ARTIST);
			int albumColumn = c1.getColumnIndex(android.provider.MediaStore.Audio.Media.ALBUM);

			MediaItem been1 = new MediaItem();
			been1.setAlbumId(c1.getLong(idColumn));
			been1.setAlbum(c1.getString(albumColumn));
			been1.setArtist(c1.getString(artistColumn));
			String currAlbum = c1.getString(c1.getColumnIndex(MediaStore.Audio.Media.ALBUM));
			if (MainActivity.textForSearchAlbum != null) {
				if (currAlbum.toLowerCase()
						.contains(MainActivity.textForSearchAlbum.getText().toString().toLowerCase())) {
					listOfAlbum.add(been1);
				}
			} else {
				listOfAlbum.add(been1);
			}
			c1.moveToNext();
			// add songs to list
			do {
				c1.moveToPrevious();
				String prevAlbum = c1.getString(c1.getColumnIndex(MediaStore.Audio.Media.ALBUM));
				c1.moveToNext();
				currAlbum = c1.getString(c1.getColumnIndex(MediaStore.Audio.Media.ALBUM));
				if (!prevAlbum.equals(currAlbum)) {
					MediaItem been = new MediaItem();
					been.setAlbumId(c1.getLong(idColumn));
					been.setAlbum(c1.getString(albumColumn));
					been.setArtist(c1.getString(artistColumn));
					if (MainActivity.textForSearchAlbum != null) {
						if (currAlbum.toLowerCase()
								.contains(MainActivity.textForSearchAlbum.getText().toString().toLowerCase())) {
							listOfAlbum.add(been);
						}
					} else {
						listOfAlbum.add(been);
					}
				}
			} while (c1.moveToNext());
		}
		return listOfAlbum;
	}

	public static ArrayList<MediaItem> listOfSongs(Context context) {
		Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
		Cursor c;
		c = context.getContentResolver().query(uri, null, MediaStore.Audio.Media.IS_MUSIC + " != 0", null,
				MediaStore.Audio.Media.IS_MUSIC + " ASC");
		ArrayList<MediaItem> listOfSongs = new ArrayList<MediaItem>();
		c.moveToFirst();
		while (c.moveToNext()) {
			MediaItem songData = new MediaItem();

			String title = c.getString(c.getColumnIndex(MediaStore.Audio.Media.TITLE));
			String artist = c.getString(c.getColumnIndex(MediaStore.Audio.Media.ARTIST));
			String album = c.getString(c.getColumnIndex(MediaStore.Audio.Media.ALBUM));
			long duration = c.getLong(c.getColumnIndex(MediaStore.Audio.Media.DURATION));
			String data = c.getString(c.getColumnIndex(MediaStore.Audio.Media.DATA));
			long albumId = c.getLong(c.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
			String composer = c.getString(c.getColumnIndex(MediaStore.Audio.Media.COMPOSER));
			long songId = c.getLong(c.getColumnIndex(MediaStore.Audio.Media._ID));

			songData.setTitle(title);
			songData.setAlbum(album);
			songData.setArtist(artist);
			songData.setDuration(duration);
			songData.setPath(data);
			songData.setAlbumId(albumId);
			songData.setComposer(composer);
			songData.setSongId(songId);
			
			if (MainActivity.textForSearchSong != null) {
				// if (title.regionMatches(true, 0,
				// MainActivity.textForSearch.getText().toString(), 0,
				if (title.toLowerCase().contains(MainActivity.textForSearchSong.getText().toString().toLowerCase())) {
					listOfSongs.add(songData);
				}
			} else {
				listOfSongs.add(songData);
			}

		}
		c.close();
		Log.d("SIZE", "SIZE: " + listOfSongs.size());
		return listOfSongs;
	}

	public static Bitmap getAlbumart(Context context, Long album_id) {
		Bitmap bm = null;
		BitmapFactory.Options options = new BitmapFactory.Options();
		try {
			final Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
			Uri uri = ContentUris.withAppendedId(sArtworkUri, album_id);
			ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
			if (pfd != null) {
				FileDescriptor fd = pfd.getFileDescriptor();
				bm = BitmapFactory.decodeFileDescriptor(fd, null, options);
				pfd = null;
				fd = null;
			}
		} catch (Error ee) {
		} catch (Exception e) {
		}
		return bm;
	}

	public static Bitmap getDefaultAlbumArt(Context context) {
		Bitmap bm = null;
		BitmapFactory.Options options = new BitmapFactory.Options();
		try {
			bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.default_album_art, options);
		} catch (Error ee) {
		} catch (Exception e) {
		}
		return bm;
	}

	public static String getDuration(long milliseconds) {
		long sec = (milliseconds / 1000) % 60;
		long min = (milliseconds / (60 * 1000)) % 60;
		long hour = milliseconds / (60 * 60 * 1000);

		String s = (sec < 10) ? "0" + sec : "" + sec;
		String m = (min < 10) ? "0" + min : "" + min;
		String h = "" + hour;

		String time = "";
		if (hour > 0) {
			time = h + ":" + m + ":" + s;
		} else {
			time = m + ":" + s;
		}
		return time;
	}

	public static boolean currentVersionSupportBigNotification() {
		int sdkVersion = android.os.Build.VERSION.SDK_INT;
		if (sdkVersion >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
			return true;
		}
		return false;
	}

	public static boolean currentVersionSupportLockScreenControls() {
		int sdkVersion = android.os.Build.VERSION.SDK_INT;
		if (sdkVersion >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			return true;
		}
		return false;
	}
}
