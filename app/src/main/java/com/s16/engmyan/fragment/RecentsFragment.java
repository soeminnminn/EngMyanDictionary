package com.s16.engmyan.fragment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;

import com.s16.app.ActionBarUtils;
import com.s16.engmyan.R;
import com.s16.engmyan.adapters.RecentsListAdapter;
import com.s16.engmyan.data.UserDataProvider;
import com.s16.engmyan.data.UserQueryHelper;

public class RecentsFragment extends DialogFragment {

	protected static String TAG = RecentsFragment.class.getSimpleName();
	
	public interface OnRecentsListItemClickListener {
		public void onRecentsListItemClick(DialogInterface dialog, View view, long id, long refId);
	}
	
	private AdapterView.OnItemClickListener mItemClickListener = new AdapterView.OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			if ((mListRecents != null) && (mListAdapter != null)  
					&& (mOnRecentsListItemClickListener != null)) {
				Cursor cursor = (Cursor)mListAdapter.getItem(position);
				int colIdx = cursor.getColumnIndex(UserDataProvider.COLUMN_REFRENCE_ID);
				if (colIdx != -1) {
					long refId = cursor.getLong(colIdx);
					mOnRecentsListItemClickListener.onRecentsListItemClick(getDialog(), view, id, refId);
				}
			}
		}
	};
	
	private View mClearButton;
	private ListView mListRecents;
	private RecentsListAdapter mListAdapter;
	private OnRecentsListItemClickListener mOnRecentsListItemClickListener;
	
	@SuppressLint("InlinedApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setStyle(DialogFragment.STYLE_NO_TITLE, R.style.AppTheme_Translucent);
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Dialog dialog = super.onCreateDialog(savedInstanceState);
		dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		return dialog;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_recents, container, false);
		
		View actionClose = rootView.findViewById(R.id.closeButton);
		ActionBarUtils.getInstance(getContext()).makeActionButton(actionClose);
		actionClose.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
		
		mClearButton = rootView.findViewById(R.id.clearButton);
		ActionBarUtils.getInstance(getContext()).makeActionButton(mClearButton);
		mClearButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				performClearRecents();
			}
		});
		
		mListRecents = (ListView)rootView.findViewById(R.id.listViewRecents);
		mListRecents.setSmoothScrollbarEnabled(true);
		mListRecents.setOnItemClickListener(mItemClickListener);
		
		bindList();
		
		return rootView;
	}
	
	private void bindList() {
		boolean hasData = false;
		if (mListRecents != null) {
			Cursor cursor = UserQueryHelper.getInstance(getContext()).getAllHistories();
			if (cursor != null) {
				mListAdapter = new RecentsListAdapter(getContext(), cursor, UserDataProvider.COLUMN_WORD);
				mListRecents.setAdapter(mListAdapter);
				hasData = mListAdapter.getCount() > 0;
			}
		}
		if (mClearButton != null) {
			mClearButton.setEnabled(hasData);
		}
	}
	
	private void performClearRecents() {
		final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
		dialogBuilder.setIcon(android.R.drawable.ic_dialog_info);
		dialogBuilder.setTitle(R.string.clear_recent_title);
		dialogBuilder.setMessage(R.string.clear_recent_message);
		
		dialogBuilder.setNegativeButton(getText(android.R.string.cancel), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		dialogBuilder.setPositiveButton(getText(android.R.string.ok), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				UserQueryHelper.getInstance(getContext()).removeAllHistory();
				bindList();
			}
		});
		dialogBuilder.show();
	}
	
	public void setOnRecentsListItemClickListener(OnRecentsListItemClickListener listener) {
		mOnRecentsListItemClickListener = listener;
	}
}
