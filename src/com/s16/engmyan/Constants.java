package com.s16.engmyan;

import java.io.File;

import com.s16.engmyan.data.DictionaryDataProvider;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;
import android.preference.PreferenceManager;

public class Constants {
	
	public static final String EMPTY_STRING = "";
	
	public static final String ANDROID_DATA = "/Android/data/";
	public static final String ANDROID_OBB = "/Android/obb/";
	
	public static final String ASSERT_ZIP_PKG = "database/EMDictionary.zip";
	public static final String DATABASE_FILE = "EMDictionary.db";
	
	public static final String DATABASE_FILE_KEY = "database";
	
	public static final int SEARCH_LIST_ITEM_LIMIT = 50;
	public static final String SEARCH_TEXT_KEY = "search_text";
	public static final String DETAIL_ID_KEY = "detail_id";
	public static final String DETAIL_DATA_KEY = "detail_data";
	
	public static final String DETAIL_WORD_KEY = "detail_word";
	public static final String DETAIL_TITLE_KEY = "detail_title";
	public static final String DETAIL_DEFINITION_KEY = "detail_definition";
	public static final String DETAIL_FILENAME_KEY = "detail_filename";
	public static final String DETAIL_PICTURE_KEY = "detail_picture";
	public static final String DETAIL_SOUND_KEY = "detail_sound";
	
	public static final String URL_DEFAULT = "about:blank";
	public static final String URL_DEFINITION = "file:///android_asset/definition.html";
	public static final String URL_CREDIT = "file:///android_asset/credit.html";
	public static final String URL_NOT_FOUND = "file:///android_asset/not_found.html";
	
	public static final String PICTURE_FOLDER = "PICS/";
	public static final String SOUND_FOLDER = "SOUND/";
	
	public static final String MIME_TYPE = "text/html";
    public static final String ENCODING = "utf-8";
    
    public static final String PREFS_LOCALE = "prefs_locale";
    public static final String PREFS_USED_UNICODE_FIX = "prefs_used_unicode_fix";
    public static final String PREFS_USED_WORD_CLICKABLE = "prefs_used_word_clickable";
    public static final String PREFS_SHOW_SYNONYM = "prefs_show_synonym";
    public static final String PREFS_CREDIT = "prefs_credit";
    public static final String PREFS_ABOUT = "prefs_about";
    
    public static final int REQUEST_SETTINGS = 1;
    public static final int REQUEST_DETAIL = 2;
    
    public static final int DATA_VERSION = 3000;
	
	private static String DATA_FOLDER;
	private static DictionaryDataProvider DATA_PROVIDER;
	
	public static File getDataFolder(Context context) {
		if(DATA_FOLDER == null) {
			DATA_FOLDER = ANDROID_DATA + context.getPackageName() + File.separator  + "files/";
		}
		
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			String path = Environment.getExternalStorageDirectory().getAbsolutePath() + DATA_FOLDER;
			return new File(path);
	    }
		
		return null;
	}
	
	public static File getDatabaseFile(Context context) {
		File dataFolder = getDataFolder(context);
		if(dataFolder != null) {
			boolean success = true;
			if(!dataFolder.exists()) {
				success = dataFolder.mkdirs();
			}
			
			if (success) {
				String dbPath = dataFolder.getPath() + File.separator + DATABASE_FILE;
				
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
				Editor editor = prefs.edit();
				editor.putString(DATABASE_FILE_KEY, dbPath);
				editor.commit();
				
				return new File(dbPath);
			}
		}
		
		return null;
	}
	
	public static DictionaryDataProvider getDataProvider(Context context) {
		if (DATA_PROVIDER == null) {
			File dbFile = getDatabaseFile(context);
			DATA_PROVIDER = new DictionaryDataProvider(context, dbFile);
		}
		return DATA_PROVIDER;
	}
}
