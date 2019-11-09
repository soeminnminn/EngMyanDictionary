package com.s16.app;

import android.content.Context;
import android.graphics.Rect;
import android.support.v4.view.GravityCompat;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

public class ActionBarUtils {
	
	private static ActionBarUtils INSTANCE;
	private final Context mContext;

	private View.OnLongClickListener mViewOnLongClickListener = new View.OnLongClickListener() {
		
		@Override
		public boolean onLongClick(View v) {
			if (!v.isEnabled()) return false;
			final CharSequence description = v.getContentDescription();
			if (TextUtils.isEmpty(description)) return false;
			
			final int[] screenPos = new int[2];
	        final Rect displayFrame = new Rect();
	        v.getLocationOnScreen(screenPos);
	        v.getWindowVisibleDisplayFrame(displayFrame);

			final Context context = getContext();
	        final int width = v.getWidth();
	        final int height = v.getHeight();
	        final int midy = screenPos[1] + height / 2;
	        final int screenWidth = context.getResources().getDisplayMetrics().widthPixels;

	        Toast cheatSheet = Toast.makeText(context, description, Toast.LENGTH_SHORT);
	        if (midy < displayFrame.height()) {
	            // Show along the top; follow action buttons
	            cheatSheet.setGravity(Gravity.TOP | GravityCompat.END, screenWidth - screenPos[0] - width / 2, height);
	        } else {
	            // Show along the bottom center
	            cheatSheet.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, height);
	        }
	        cheatSheet.show();
			return true;
		}
	};
	
	public static ActionBarUtils getInstance(Context context) {
		if (INSTANCE == null) {
			INSTANCE = new ActionBarUtils(context);
		}
		return INSTANCE;
	}
	
	private ActionBarUtils(Context context) {
		mContext = context;
	}
	
	private Context getContext() {
		return mContext;
	}
	
	public void makeActionButton(View view) {
		if (view != null) {
			view.setLongClickable(true);
			view.setOnLongClickListener(mViewOnLongClickListener);
		}
	}
}
