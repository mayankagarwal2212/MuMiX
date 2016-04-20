package com.example.mp3player;

import java.io.FileDescriptor;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

public class Detail extends Activity {
	ImageView album_art;
	TextView album, artist, genre, location;
	static Context context;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_detail);
		getInit();
		context = Detail.this;

		Intent m = getIntent();
		Bundle b;
		b = m.getExtras();
		int p = b.getInt("num");
		MediaItem data = PlayerConstants.SONGS_LIST.get(p);
		String artist_n = data.getArtist();
		String album_n = data.getAlbum();
		String composer = data.getComposer();
		String locator = data.getPath();
		Bitmap albumArt = getAlbumart(context, data.getAlbumId());
		if (albumArt != null) {
			album_art.setBackgroundDrawable(new BitmapDrawable(albumArt));
			album.setText(album_n);
			artist.setText(artist_n);
			genre.setText(composer);
			location.setText(locator);

		} else {
			album_art.setBackgroundDrawable(new BitmapDrawable(getDefaultAlbumArt(context)));
			album.setText("Unknown");
			artist.setText("Unknown");
			genre.setText("Unknown");
		}
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

	private void getInit() {
		// TODO Auto-generated method stub
		album_art = (ImageView) findViewById(R.id.album_art);
		album = (TextView) findViewById(R.id.Album);
		artist = (TextView) findViewById(R.id.artist);
		genre = (TextView) findViewById(R.id.genre);
		location = (TextView) findViewById(R.id.file_location);
	}

}
