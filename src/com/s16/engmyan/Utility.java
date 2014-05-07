package com.s16.engmyan;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.text.Html;
import android.view.ContextThemeWrapper;

public class Utility {
	
	private static final int NULL_CHAR = 0x00;
	
	public static CharSequence RegexReplace(CharSequence data, CharSequence find, CharSequence replacement) {
		if ((data == null) || (data == "")) return data;
		StringBuffer result = new StringBuffer();
		Pattern pattern = Pattern.compile(find.toString());
		Matcher m = pattern.matcher(data);
		while(m.find()) {
			StringBuffer replacementBuffer = new StringBuffer();
			boolean foundGroup = false;
			for(int c = 0; c < replacement.length(); c++) {
				char ch = replacement.charAt(c);
				if(foundGroup) {
					switch(ch) {
						case '1':
							replacementBuffer.append((m.group(1) == null ? "" : "$1"));
							break;
						case '2':
							replacementBuffer.append((m.group(2) == null ? "" : "$2"));
							break;
						case '3':
							replacementBuffer.append((m.group(3) == null ? "" : "$3"));
							break;
						case '4':
							replacementBuffer.append((m.group(4) == null ? "" : "$4"));
							break;
						case '5':
							replacementBuffer.append((m.group(5) == null ? "" : "$5"));
							break;
						case '6':
							replacementBuffer.append((m.group(6) == null ? "" : "$6"));
							break;
						case '7':
							replacementBuffer.append((m.group(7) == null ? "" : "$7"));
							break;
						case '8':
							replacementBuffer.append((m.group(8) == null ? "" : "$8"));
							break;
						case '9':
							replacementBuffer.append((m.group(9) == null ? "" : "$9"));
							break;
						default:
							break;
					}
					
					foundGroup = false;
					continue;
				}
				
				if (ch == '$') {
					foundGroup = true;
					continue;
				}
				
				replacementBuffer.append(ch);
			}
			
			m.appendReplacement(result, replacementBuffer.toString());
		}
		m.appendTail(result);
		return result.toString();
	}
	
	public static boolean isNull(Cursor cursor, String column) {
		if (cursor == null) return true;
		if (cursor.getCount() < 1) return true;
		if ((column == null) || (column == "")) return true;
		int columnIndex = cursor.getColumnIndex(column);
		if (columnIndex < 0) return true;
		return cursor.isNull(columnIndex);
	}
	
	public static boolean isMyChar(int code) {
		return (code >= 0x1000 && code <= 0x109F) || (code >= 0xAA60 && code <= 0xAA7B);
	}
	
	public static boolean isMyChar(int[] codes) {
		if(codes == null) return false;
		boolean isMyChar = false;
    	for(int i = 0; i < codes.length; i++) {
    		if(isMyChar(codes[i])) {
    			isMyChar = true;
    			break;
    		}
    	}
		return isMyChar;
	}
	
	public static boolean isMyChar(CharSequence label) {
		if(label == null) return false;
		boolean isMyChar = false;
    	for(int i = 0; i < label.length(); i++) {
    		if(isMyChar(label.charAt(i))) {
    			isMyChar = true;
    			break;
    		}
    	}
		return isMyChar;
	}

	public static CharSequence ZawGyiDrawFix(CharSequence input) {
		return ZawGyiDrawFix(input, 0xEA00);
	}
		
	public static CharSequence ZawGyiDrawFix(CharSequence input, int fixCode) {
		if (fixCode== 0x0) return input;
		String output = input.toString();
		int index = 0;
		char[] chArray = new char[output.length()];
		for(int i = 0; i < output.length(); i++) {
			int ch = (int)output.charAt(i);
			if((ch != NULL_CHAR) && (isMyChar(ch))) {
				chArray[index++] = (char)(ch + fixCode); // 0xEA00
			}
			else {
				chArray[index++] = (char)ch;
			}
    	}
		return String.valueOf(chArray);
	}

	public static int getConfigScreenSize(Context context) {
		return context.getResources().getInteger(R.integer.config_screen);
	}
	
	public static int getConfigScreenOrientation(Context context) {
		return context.getResources().getConfiguration().orientation;
	}
	
	public static void showAboutDialog(Context context) {
    	
		final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(new ContextThemeWrapper(context, R.style.DialogTheme));
		dialogBuilder.setIcon(android.R.drawable.ic_dialog_info);
		dialogBuilder.setTitle(R.string.action_about);
		String html = context.getText(R.string.about_text).toString();
		dialogBuilder.setMessage(Html.fromHtml(html));
		
		dialogBuilder.setNegativeButton(context.getText(android.R.string.cancel), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		dialogBuilder.setPositiveButton(null, null);
		dialogBuilder.show();
    }
}
