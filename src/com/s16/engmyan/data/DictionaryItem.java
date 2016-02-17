package com.s16.engmyan.data;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

public class DictionaryItem implements Parcelable {

	public long id;
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
	
	public static DictionaryItem getFrom(Cursor cursor) {
		return new DictionaryItem(cursor);
	}
	
	public static DictionaryItem getFrom(SearchQueryHelper queryHelper, long id) {
		if (queryHelper != null && id > 0) {
			Cursor cursor = queryHelper.queryDefinition(id);
			DictionaryItem itemData = new DictionaryItem(cursor);
			cursor.close();
		
			return itemData;
		}
		return null;
	}
	
	public static DictionaryItem getFrom(SearchQueryHelper queryHelper, String word) {
		DictionaryItem[] itemArr = getArrayFrom(queryHelper, word);
		if ((itemArr != null) && (itemArr.length > 0)) {
			return itemArr[0];
		}
		return null;
	}
	
	public static DictionaryItem[] getArrayFrom(SearchQueryHelper queryHelper, String word) {
		if ((queryHelper != null) && (!TextUtils.isEmpty(word))) {
			Cursor wordCursor = queryHelper.stripQuery(word);
			DictionaryItem[] itemArr = null;
			if (wordCursor != null) {
				int count = wordCursor.getCount();
				itemArr = new DictionaryItem[count];
				int idx = 0;
				do {
					long id = -1;
					int colIdx = wordCursor.getColumnIndex(DictionaryDataProvider.COLUMN_ID);
					if (colIdx != -1) {
						id = wordCursor.getLong(colIdx);
					}
					itemArr[idx++] = getFrom(queryHelper, id);
				} while(wordCursor.moveToNext());
				wordCursor.close();
			}
			return itemArr;
		}
		return null;
	}
	
	public DictionaryItem() {
	}
	
	private DictionaryItem(final Cursor cursor) {
		this();
		if (cursor != null) {
			int colIdx = cursor.getColumnIndex(DictionaryDataProvider.COLUMN_ID);
			if (colIdx != -1) {
				id = cursor.getLong(colIdx);	
			}
			
			colIdx = cursor.getColumnIndex(DictionaryDataProvider.COLUMN_WORD);
			if (colIdx != -1) {
				word = cursor.getString(colIdx);	
			}
			
			colIdx = cursor.getColumnIndex(DictionaryDataProvider.COLUMN_STRIPWORD);
			if (colIdx != -1) {
				stripword = cursor.getString(colIdx);	
			}
			
			colIdx = cursor.getColumnIndex(DictionaryDataProvider.COLUMN_TITLE);
			if (colIdx != -1) {
				title = cursor.getString(colIdx);	
			}
			
			colIdx = cursor.getColumnIndex(DictionaryDataProvider.COLUMN_DEFINITION);
			if (colIdx != -1) {
				definition = cursor.getString(colIdx);	
			}
			
			colIdx = cursor.getColumnIndex(DictionaryDataProvider.COLUMN_KEYWORDS);
			if (colIdx != -1) {
				keywords = cursor.getString(colIdx);	
			}
			
			colIdx = cursor.getColumnIndex(DictionaryDataProvider.COLUMN_SYNONYM);
			if (colIdx != -1) {
				synonym = cursor.getString(colIdx);	
			}
			
			colIdx = cursor.getColumnIndex(DictionaryDataProvider.COLUMN_FILENAME);
			if (colIdx != -1) {
				filename = cursor.getString(colIdx);	
			}
			
			colIdx = cursor.getColumnIndex(DictionaryDataProvider.COLUMN_PICTURE);
			if (colIdx != -1) {
				picture = cursor.getInt(colIdx) == 1;	
			}
			
			colIdx = cursor.getColumnIndex(DictionaryDataProvider.COLUMN_SOUND);
			if (colIdx != -1) {
				sound = cursor.getInt(colIdx) == 1;	
			}
		}
	}
	
	private DictionaryItem(Parcel source) {
		id = source.readLong();
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
		dest.writeLong(id);
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
