package com.android.contacts;

import java.util.ArrayList;

import android.R.integer;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class GalleryAdapter extends BaseAdapter{
	private Context context;
	private ArrayList<GalleryContactEntry> galleryContactEntries;
	private LayoutInflater inflater;
	public ImageView phone_contact_imageView;
	public GalleryAdapter(Context context,ArrayList<GalleryContactEntry> ga){
		this.context=context;
		this.galleryContactEntries=ga;
		inflater=LayoutInflater.from(context);
	}
	
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return galleryContactEntries.size();
	}

	@Override
	public Object getItem(int i) {
		// TODO Auto-generated method stub
		return galleryContactEntries.get(i);
	}

	@Override
	public long getItemId(int i) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int i, View view, ViewGroup viewgroup) {
		// TODO Auto-generated method stub
		View v=inflater.inflate(R.layout.gallery_item_contact, null);
		TextView  name=(TextView) v.findViewById(R.id.phone_contact_name);
		TextView phoneTextView =(TextView) v.findViewById(R.id.phone_contact_numbler);
		phone_contact_imageView=(ImageView) v.findViewById(R.id.phone_contact_image);
		
		final GalleryContactEntry gcEntry=galleryContactEntries.get(i);
		name.setText(gcEntry.getContactName());
		name.setOnClickListener(new android.view.View.OnClickListener() {
			
			@Override
			public void onClick(View view) {
				// TODO Auto-generated method stub
				Intent intentnameIntent=new Intent(context,PhoneEditActivity.class);
				
				//需要加入当前页面参数过去。
				intentnameIntent.putExtra("name", gcEntry.getContactName());
				intentnameIntent.putExtra("phone", gcEntry.getContactPhone());
				
				context.startActivity(intentnameIntent);
			}
		});
		phoneTextView.setText(gcEntry.getContactPhone());
		
		phoneTextView.setOnClickListener(new android.view.View.OnClickListener(){

			@Override
			public void onClick(View view) {
				// TODO Auto-generated method stub
				Intent intentnameIntent=new Intent(context,PhoneEditActivity.class);
				
				//需要加入当前页面参数过去。
				intentnameIntent.putExtra("name", gcEntry.getContactName());
				intentnameIntent.putExtra("phone", gcEntry.getContactPhone());
				
				context.startActivity(intentnameIntent);
			}
			
		});
		phone_contact_imageView.setOnClickListener(new android.view.View.OnClickListener(){

			@Override
			public void onClick(View view) {
				
				CharSequence[] items = {context.getString(R.string.contact_phone_photo_album),context.getString(R.string.contact_phone_photo_camera)};
				new AlertDialog.Builder(context).setItems(items, new android.content.DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface dialoginterface, int i) {
						// TODO Auto-generated method stub
						if(i== 0 ){

								Intent intent = new Intent(Intent.ACTION_GET_CONTENT);

								intent.addCategory(Intent.CATEGORY_OPENABLE);

								intent.setType("image/*");

								((Activity) context).startActivityForResult(Intent.createChooser(intent, context.getString(R.string.contact_phone_select_image)), gcEntry.getImageId());

						}else{

							Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

							((Activity) context).startActivityForResult(intent, gcEntry.getImageId());

						}


					}}).create().show();





			}
			
		});
		SQLiteDatabase db=context.openOrCreateDatabase("user.db", context.MODE_PRIVATE, null);
		 db.execSQL("create table if not exists usertbl(" +
	        		"_id integer primary key autoincrement," +
	        		"image_id text not null," +
	        		"path_image text not null" +
	        		")");
		Cursor cursor=db.rawQuery("select * from usertbl where image_id="+gcEntry.getImageId(), null);
		if(cursor!=null){
			while(cursor.moveToNext()){
				String pathString=cursor.getString(cursor.getColumnIndex("path_image"));
				phone_contact_imageView.setImageBitmap(BitmapFactory.decodeFile(pathString));
			}
		}
		cursor.close();
		db.close();
		return v;
	}
	
	public void upData(int i,String path){
		
		SQLiteDatabase db=context.openOrCreateDatabase("user.db", context.MODE_PRIVATE, null);
		 db.execSQL("create table if not exists usertbl(" +
	        		"_id integer primary key autoincrement," +
	        		"image_id text not null," +
	        		"path_image text not null" +
	        		")");
		 db.execSQL("insert into usertbl(image_id ,path_image) values('"+String.valueOf(i)+"','"+path+"')");
		 db.close();
		 //phone_contact_imageView.setImageBitmap(BitmapFactory.decodeFile(path));
		
		//phone_contact_imageView.setBackgroundDrawable(BitmapFactory.decodeFile(path));
		//notifyDataSetChanged();
	}
}
