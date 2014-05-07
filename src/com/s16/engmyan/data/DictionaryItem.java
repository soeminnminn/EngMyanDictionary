package com.s16.engmyan.data;

import com.s16.engmyan.Utility;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

public class DictionaryItem implements Parcelable {

	public int id;
	public String word;
	public String title;
	public String definition;
	public String filename;
	public boolean picture;
	public boolean sound;
	
	public static final Parcelable.Creator<DictionaryItem> CREATOR = new Parcelable.Creator<DictionaryItem>() {
		
		public DictionaryItem createFromParcel(Parcel in) {
		    return new DictionaryItem(in);
		}
		
		public DictionaryItem[] newArray(int size) {
		    return new DictionaryItem[size];
		}
		
	};
	
	public DictionaryItem() {
	}
	
	public DictionaryItem(final Cursor cursor) {
		this();
		if (cursor != null) {
			if (!Utility.isNull(cursor, DictionaryDataProvider.COLUMN_ID)) {
				int idCol = cursor.getColumnIndex(DictionaryDataProvider.COLUMN_ID);
				id = cursor.getInt(idCol);
			}
			
			if (!Utility.isNull(cursor, DictionaryDataProvider.COLUMN_WORD)) {
				int wordCol = cursor.getColumnIndex(DictionaryDataProvider.COLUMN_WORD);
				word = cursor.getString(wordCol);
			}
			
			if (!Utility.isNull(cursor, DictionaryDataProvider.COLUMN_TITLE)) {
				int titleCol = cursor.getColumnIndex(DictionaryDataProvider.COLUMN_TITLE);
				title = cursor.getString(titleCol);
			}
			
			if (!Utility.isNull(cursor, DictionaryDataProvider.COLUMN_DEFINITION)) {
				int definitionCol = cursor.getColumnIndex(DictionaryDataProvider.COLUMN_DEFINITION);
				definition = cursor.getString(definitionCol);
			}
			
			if (!Utility.isNull(cursor, DictionaryDataProvider.COLUMN_FILENAME)) {
				int fileNameCol = cursor.getColumnIndex(DictionaryDataProvider.COLUMN_FILENAME);
				filename = cursor.getString(fileNameCol);
			}
			
			if (!Utility.isNull(cursor, DictionaryDataProvider.COLUMN_PICTURE)) {
				int pictureCol = cursor.getColumnIndex(DictionaryDataProvider.COLUMN_PICTURE);
				picture = cursor.getInt(pictureCol) == 1;
			}
			
			if (!Utility.isNull(cursor, DictionaryDataProvider.COLUMN_SOUND)) {
				int soundCol = cursor.getColumnIndex(DictionaryDataProvider.COLUMN_SOUND);
				sound = cursor.getInt(soundCol) == 1;
			}
		}
	}
	
	private DictionaryItem(Parcel source) {
		this();
		id = source.readInt();
		word = source.readString();
		title = source.readString();
		definition = source.readString();
		filename = source.readString();
		picture = source.readInt() == 1;
		sound = source.readInt() == 1;
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(id);
		dest.writeString(word);
		dest.writeString(title);
		dest.writeString(definition);
		dest.writeString(filename);
		dest.writeInt(picture ? 1 : 0);
		dest.writeInt(sound ? 1 : 0);
	}

}
