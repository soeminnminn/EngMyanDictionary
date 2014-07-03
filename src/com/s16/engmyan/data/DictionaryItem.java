package com.s16.engmyan.data;

import com.s16.engmyan.Utility;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

public class DictionaryItem implements Parcelable {

	public int id;
	public String word;
	public String stripword;
	public String title;
	public String definition;
	public String keywords;
	public String synonym;
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
	
	public static DictionaryItem getFrom(DictionaryDataProvider dataProvider, long id) {
		if ((dataProvider != null) && (id >= 0)) {
			Cursor cursor = dataProvider.queryDefinition(id);
			DictionaryItem itemData = new DictionaryItem(cursor);
			cursor.close();
		
			return itemData;
		}
		return null;
	}
	
	public static DictionaryItem getFrom(DictionaryDataProvider dataProvider, String word) {
		DictionaryItem[] itemArr = getArrayFrom(dataProvider, word);
		if ((itemArr != null) && (itemArr.length > 0)) {
			return itemArr[0];
		}
		return null;
	}
	
	public static DictionaryItem[] getArrayFrom(DictionaryDataProvider dataProvider, String word) {
		if ((dataProvider != null) && (!TextUtils.isEmpty(word))) {
			Cursor wordCursor = dataProvider.stripQuery(word);
			if (wordCursor != null) {
				int count = wordCursor.getCount();
				if (count == 1) {
					int id = -1;
					if (!Utility.isNull(wordCursor, DictionaryDataProvider.COLUMN_ID)) {
						int colIdx = wordCursor.getColumnIndex(DictionaryDataProvider.COLUMN_ID);
						id = wordCursor.getInt(colIdx);
					}
					wordCursor.close();
					DictionaryItem item = getFrom(dataProvider, id);
					if (item != null) return new DictionaryItem[] { item };
					
				} else if (count > 1) {
					DictionaryItem[] itemArr = new DictionaryItem[count];
					int idx = 0;
					do {
						int id = -1;
						if (!Utility.isNull(wordCursor, DictionaryDataProvider.COLUMN_ID)) {
							int colIdx = wordCursor.getColumnIndex(DictionaryDataProvider.COLUMN_ID);
							id = wordCursor.getInt(colIdx);
						}
						itemArr[idx++] = getFrom(dataProvider, id);
					} while(wordCursor.moveToNext());
					wordCursor.close();
					return itemArr;
				}
				wordCursor.close();
			}
		}
		return null;
	}
	
	public DictionaryItem() {
	}
	
	public DictionaryItem(final Cursor cursor) {
		this();
		if (cursor != null) {
			if (!Utility.isNull(cursor, DictionaryDataProvider.COLUMN_ID)) {
				int colIdx = cursor.getColumnIndex(DictionaryDataProvider.COLUMN_ID);
				id = cursor.getInt(colIdx);
			}
			
			if (!Utility.isNull(cursor, DictionaryDataProvider.COLUMN_WORD)) {
				int colIdx = cursor.getColumnIndex(DictionaryDataProvider.COLUMN_WORD);
				word = cursor.getString(colIdx);
			}
			
			if (!Utility.isNull(cursor, DictionaryDataProvider.COLUMN_STRIPWORD)) {
				int colIdx = cursor.getColumnIndex(DictionaryDataProvider.COLUMN_STRIPWORD);
				stripword = cursor.getString(colIdx);
			}
			
			if (!Utility.isNull(cursor, DictionaryDataProvider.COLUMN_TITLE)) {
				int colIdx = cursor.getColumnIndex(DictionaryDataProvider.COLUMN_TITLE);
				title = cursor.getString(colIdx);
			}
			
			if (!Utility.isNull(cursor, DictionaryDataProvider.COLUMN_DEFINITION)) {
				int colIdx = cursor.getColumnIndex(DictionaryDataProvider.COLUMN_DEFINITION);
				definition = cursor.getString(colIdx);
			}
			
			if (!Utility.isNull(cursor, DictionaryDataProvider.COLUMN_KEYWORDS)) {
				int colIdx = cursor.getColumnIndex(DictionaryDataProvider.COLUMN_KEYWORDS);
				keywords = cursor.getString(colIdx);
			}
			
			if (!Utility.isNull(cursor, DictionaryDataProvider.COLUMN_SYNONYM)) {
				int colIdx = cursor.getColumnIndex(DictionaryDataProvider.COLUMN_SYNONYM);
				synonym = cursor.getString(colIdx);
			}
			
			if (!Utility.isNull(cursor, DictionaryDataProvider.COLUMN_FILENAME)) {
				int colIdx = cursor.getColumnIndex(DictionaryDataProvider.COLUMN_FILENAME);
				filename = cursor.getString(colIdx);
			}
			
			if (!Utility.isNull(cursor, DictionaryDataProvider.COLUMN_PICTURE)) {
				int colIdx = cursor.getColumnIndex(DictionaryDataProvider.COLUMN_PICTURE);
				picture = cursor.getInt(colIdx) == 1;
			}
			
			if (!Utility.isNull(cursor, DictionaryDataProvider.COLUMN_SOUND)) {
				int colIdx = cursor.getColumnIndex(DictionaryDataProvider.COLUMN_SOUND);
				sound = cursor.getInt(colIdx) == 1;
			}
		}
	}
	
	private DictionaryItem(Parcel source) {
		this();
		id = source.readInt();
		word = source.readString();
		stripword = source.readString();
		title = source.readString();
		definition = source.readString();
		keywords = source.readString();
		synonym = source.readString();
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
		dest.writeString(stripword);
		dest.writeString(title);
		dest.writeString(definition);
		dest.writeString(keywords);
		dest.writeString(synonym);
		dest.writeString(filename);
		dest.writeInt(picture ? 1 : 0);
		dest.writeInt(sound ? 1 : 0);
	}

	@Override
	public String toString() {
		if (TextUtils.isEmpty(word)) return "";
		return word;
	}
}
