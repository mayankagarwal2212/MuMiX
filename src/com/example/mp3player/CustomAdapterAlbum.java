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

public class CustomAdapterAlbum extends ArrayAdapter<MediaItem> {

	static ArrayList<MediaItem> listOfAlbum;
	Context context;
	LayoutInflater inflator;

	@SuppressWarnings("static-access")
	public CustomAdapterAlbum(Context context, int resource, ArrayList<MediaItem> listOfAlbum) {
		super(context, resource, listOfAlbum);
		this.listOfAlbum = listOfAlbum;
		this.context = context;
		inflator = LayoutInflater.from(context);
	}

	private class ViewHolder {
		TextView textViewAlbumName;
		ImageView imageViewAlbumArt;
	}

	ViewHolder holder;

	@SuppressWarnings("deprecation")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View myView = convertView;
		if (convertView == null) {
			myView = inflator.inflate(R.layout.album_list, parent, false);
			holder = new ViewHolder();
			holder.imageViewAlbumArt = (ImageView) myView.findViewById(R.id.imageViewAlbumArt);
			holder.textViewAlbumName = (TextView) myView.findViewById(R.id.textViewAlbumName);
			myView.setTag(holder);
		} else {
			holder = (ViewHolder) myView.getTag();
		}
		MediaItem detail = listOfAlbum.get(position);
		Bitmap albumArt = detail.getAlbumart(context, detail.getAlbumId());
		if (albumArt != null)
			holder.imageViewAlbumArt.setBackgroundDrawable(new BitmapDrawable(albumArt));
		else
			holder.imageViewAlbumArt.setBackgroundDrawable(new BitmapDrawable(detail.getDefaultAlbumArt(context)));
		holder.textViewAlbumName.setText(detail.getAlbum());
		return myView;
	}
}
