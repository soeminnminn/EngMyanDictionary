package com.s16.engmyan.data;

import android.provider.BaseColumns;

public interface DataContents extends BaseColumns {
	
	public static final String USER_DATABASE_NAME = "database";
	public static final int USER_DATABASE_VERSION = 1;
	
	public static String DICTIONARY_TABLE = "dictionary";
	
	public static String COLUMN_ID = BaseColumns._ID;
	public static String COLUMN_WORD = "word";
	public static String COLUMN_STRIPWORD = "stripword";
	
	public static String COLUMN_TITLE = "title";
	public static String COLUMN_DEFINITION = "definition";
	public static String COLUMN_KEYWORDS = "keywords";
	public static String COLUMN_SYNONYM = "synonym";
	public static String COLUMN_FILENAME = "filename";
	public static String COLUMN_PICTURE = "picture";
	public static String COLUMN_SOUND = "sound";
	
	public static String FAVORITES_TABLE = "favorites";
	public static String HISTORIES_TABLE = "histories";
	
	public static String COLUMN_REFRENCE_ID = "refrence_id";
	public static String COLUMN_TIMESTAMP = "timestamp";

	static final int MATCH_ANY = 0;
	static final int MATCH_SEARCH = 1;
	static final int MATCH_ALL = 2;
	static final int MATCH_ID = 4;
	
	public static String METHOD_OPEN = "methodOpen";
	public static String METHOD_CLOSE = "methodClose";
}
