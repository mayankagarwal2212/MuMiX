package com.example.mp3player;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CustomAdapter extends ArrayAdapter<MediaItem> {

	static ArrayList<MediaItem> listOfSongs;
	Context context;
	LayoutInflater inflator;

	@SuppressWarnings("static-access")
	public CustomAdapter(Context context, int resource, ArrayList<MediaItem> listOfSongs) {
		super(context, resource, listOfSongs);
		this.listOfSongs = listOfSongs;
		this.context = context;
		inflator = LayoutInflater.from(context);
	}

	private class ViewHolder {
		TextView textViewSongName, textViewArtist, textViewDuration;
		ImageView imageViewAlbumArt;
	}

	ViewHolder holder;

	@SuppressWarnings("deprecation")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View myView = convertView;
		if (convertView == null) {
			myView = inflator.inflate(R.layout.custom_list, parent, false);
			holder = new ViewHolder();
			holder.imageViewAlbumArt = (ImageView) myView.findViewById(R.id.imageViewAlbumArt);
			holder.textViewSongName = (TextView) myView.findViewById(R.id.textViewSongName);
			holder.textViewArtist = (TextView) myView.findViewById(R.id.textViewArtist);
			holder.textViewDuration = (TextView) myView.findViewById(R.id.textViewDuration);
			myView.setTag(holder);
		} else {
			holder = (ViewHolder) myView.getTag();
		}
		MediaItem detail = listOfSongs.get(position);
		Bitmap albumArt = detail.getAlbumart(context, detail.getAlbumId());
		if (albumArt != null)
			holder.imageViewAlbumArt.setBackgroundDrawable(new BitmapDrawable(albumArt));
		else
			holder.imageViewAlbumArt.setBackgroundDrawable(new BitmapDrawable(detail.getDefaultAlbumArt(context)));
		holder.textViewSongName.setText(detail.toString());
		holder.textViewArtist.setText(detail.getAlbum() + " - " + detail.getArtist());
		holder.textViewDuration.setText(UtilFunctions.getDuration(detail.getDuration()));
		return myView;
	}
}
