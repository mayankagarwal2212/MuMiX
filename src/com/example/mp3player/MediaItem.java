package com.example.mp3player;

import java.io.FileDescriptor;

import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

public class MediaItem {
	String title;
	String artist;
	String album;
	String path;
	long duration;
	long albumId;
	String composer;
	long songId;

	@Override
	public String toString() {
		return title;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getArtist() {
		return artist;
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}

	public String getAlbum() {
		return album;
	}

	public void setAlbum(String album) {
		this.album = album;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public long getAlbumId() {
		return albumId;
	}

	public long getSongId() {
		return songId;
	}

	public void setAlbumId(long albumId) {
		this.albumId = albumId;
	}

	public void setSongId(long songId) {
		this.songId = songId;
	}

	public String getComposer() {
		return composer;
	}

	public void setComposer(String composer) {
		this.composer = composer;
	}

	public Bitmap getDefaultAlbumArt(Context context) {
		// TODO Auto-generated method stub
		Bitmap bm = null;
		BitmapFactory.Options options = new BitmapFactory.Options();
		try {
			bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.default_album_art, options);
		} catch (Error ee) {
		} catch (Exception e) {
		}
		return bm;
	}

	public Bitmap getAlbumart(Context context, Long album_id) {
		// TODO Auto-generated method stub
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
}