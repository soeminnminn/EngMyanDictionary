package com.s16.engmyan;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Environment;
import android.os.storage.OnObbStateChangeListener;
import android.os.storage.StorageManager;
import android.text.TextUtils;
import android.util.Log;

public class ExpansionManager {
	
	protected static String TAG = "ExpansionManager";
	
	private static String EXP_PATH;	
	private static int MAIN_VERSION = 1000;
	
	private static long LOAD_TIMEOUT = 2000;
	
	private static final String STORAGE_SERVICE = "storage";
	private static StorageManager mStorageManager;
	private static boolean mMounted = false;
	private static String mMountedObbPath;
	
	public static File getExpansionFile(Context context) {
		if(EXP_PATH == null) {
			String packageName = context.getPackageName();
			EXP_PATH = Constants.ANDROID_OBB + packageName + File.separator + "main." + MAIN_VERSION + "." + packageName + ".obb";
		}
		
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			String path = Environment.getExternalStorageDirectory().getAbsolutePath() + EXP_PATH;
			return new File(path);
	    }
		
		return null;
	}
	
	public static boolean isExpansionExists(Context context) {
		File expFile = getExpansionFile(context);
		if (expFile == null) return false;
		return expFile.exists();
	}
	
	public static boolean tryMountExpansion(Context context) {
		File expFile = getExpansionFile(context);
		if (expFile == null) return false;
		if (!expFile.exists()) return false;
		
		if (mStorageManager == null) {
			mStorageManager = (StorageManager)context.getSystemService(STORAGE_SERVICE);
		}
		if (mStorageManager == null) {
			Log.i(TAG, "StorageManager is NULL. ");
			return false;
		}
		
		final String fileName = expFile.getPath();
		if (mStorageManager.isObbMounted(fileName)) return true;
		
		OnObbStateChangeListener listener = new OnObbStateChangeListener() {
			@Override
			public void onObbStateChange(String path, int state) {
				Log.i(TAG, "Mount Obb State Path : " + path + ", State : " + state);
				if (state == OnObbStateChangeListener.MOUNTED) {
					Log.i(TAG, "Obb State Mounted. ");
					mMounted = true;
					mMountedObbPath = mStorageManager.getMountedObbPath(fileName);
				}
			}
		};
		
		if (mStorageManager.mountObb(fileName, null, listener)) {
			return !TextUtils.isEmpty(mMountedObbPath);
		}
		
		return false;
	}
	
	public static boolean unmountExpansion(Context context, boolean force) {
		File expFile = getExpansionFile(context);
		if (expFile == null) return false;
		if (!expFile.exists()) return false;
		
		if (mStorageManager == null) {
			mStorageManager = (StorageManager)context.getSystemService(STORAGE_SERVICE);
		}
		
		if (mStorageManager == null) return false;
		final String fileName = expFile.getPath();
		if (mStorageManager.isObbMounted(fileName)) {
		
			OnObbStateChangeListener listener = new OnObbStateChangeListener() {
				@Override
				public void onObbStateChange(String path, int state) {
					Log.i(TAG, "Unmount Obb State Path : " + path + ", State : " + state); 
					if (state == OnObbStateChangeListener.UNMOUNTED) {
						Log.i(TAG, "Obb State Un-mounted. ");
					}
				}
			};
			
			return mStorageManager.unmountObb(fileName, force, listener);
		}
		
		return false;
	}
	
	public static boolean isObbMounted(Context context) {
		File expFile = getExpansionFile(context);
		if (expFile == null) return false;
		if (!expFile.exists()) return false;
		
		if (mStorageManager == null) {
			mStorageManager = (StorageManager)context.getSystemService(STORAGE_SERVICE);
		}
		if (mStorageManager == null) return false;
		
		final String fileName = expFile.getPath();
		if (mStorageManager.isObbMounted(fileName)) {
			if (TextUtils.isEmpty(mMountedObbPath)) {
				mMountedObbPath = mStorageManager.getMountedObbPath(fileName);
			}
			
			return !TextUtils.isEmpty(mMountedObbPath);
		} else {
			OnObbStateChangeListener listener = new OnObbStateChangeListener() {
				@Override
				public void onObbStateChange(String path, int state) {
					if (state == OnObbStateChangeListener.MOUNTED) {
						Log.i("ExpansionManager", "Obb State Mounted. ");
						mMounted = true;
						mMountedObbPath = mStorageManager.getMountedObbPath(fileName);
					}
				}
			};
			
			long beginTime = System.currentTimeMillis();
			if (mStorageManager.mountObb(fileName, null, listener)) {
				while (!mMounted) {
					if ((System.currentTimeMillis() - beginTime) > LOAD_TIMEOUT) break;
				};
				return !TextUtils.isEmpty(mMountedObbPath);
			}
			
			return false;
		}
	}
	
	public static InputStream getInputStreamExpansion(Context context, String path) {
		if (TextUtils.isEmpty(mMountedObbPath)) return null;
		if (TextUtils.isEmpty(path)) return null;
		if (!isObbMounted(context)) return null;
		
		String filePath = mMountedObbPath + "/" + path;
		File file = new File(filePath);
		if (!file.exists()) return null;
		try {
			return new FileInputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static boolean playSoundExpansion(Context context, String path) {
		if (TextUtils.isEmpty(path)) return false;
		if (!isObbMounted(context)) return false;
		
		String filePath = mMountedObbPath + "/" + path;
		Log.i(TAG + ".playSound", filePath);
		
		File file = new File(filePath);
		if (!file.exists()) return false;
		
		MediaPlayer player = new MediaPlayer();
	    player.setAudioStreamType(AudioManager.STREAM_MUSIC);
	    
	    try {
	        player.setDataSource(file.getPath());
	        player.prepare();
	        player.start();
	        return true;
	    } catch (IllegalArgumentException e) {
	        e.printStackTrace();
	    } catch (IllegalStateException e) {
	        e.printStackTrace();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	    
	    return false;
	}
	
	@SuppressWarnings("deprecation")
	public static Bitmap getBitmapExpansion(Context context, String path) {
		if (TextUtils.isEmpty(path)) return null;
		if (!isObbMounted(context)) return null;
		
		String filePath = mMountedObbPath + "/" + path;
		Log.i(TAG + ".getBitmap", filePath);
		File file = new File(filePath);
		if (!file.exists()) return null;
		
		InputStream stream = null;
		try {
			stream = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		//android.net.Uri.parse("//sdcard/img.zip!/1.png");
		
		if (stream != null) {
			BitmapFactory.Options option = new BitmapFactory.Options();
            option.inPurgeable = true;
            Bitmap bitmap = BitmapFactory.decodeStream(stream, null, option);
			
			try {
				stream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			return bitmap;
		}
		
		return null;
	}
	
	@SuppressWarnings("deprecation")
	public static Bitmap getBitmapAssert(Context context, String path) {
		if (TextUtils.isEmpty(path)) return null;
		
		InputStream stream = null;
		try {
			stream = context.getAssets().open(path);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if (stream != null) {
			BitmapFactory.Options option = new BitmapFactory.Options();
            option.inPurgeable = true;
            Bitmap bitmap = BitmapFactory.decodeStream(stream, null, option);
			
			try {
				stream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			return bitmap;
		}
		
		return null;
	}
}
