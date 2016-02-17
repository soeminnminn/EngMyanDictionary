package com.s16.engmyan.fragment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;

import com.s16.app.ActionBarUtils;
import com.s16.engmyan.R;
import com.s16.engmyan.adapters.FavoritesListAdapter;
import com.s16.engmyan.data.UserDataProvider;
import com.s16.engmyan.data.UserQueryHelper;

public class FavoritesFragment extends DialogFragment {

	protected static String TAG = FavoritesFragment.class.getSimpleName();
	
	public interface OnFavoritesListItemClickListener {
		public void onFavoritesListItemClick(DialogInterface dialog, View view, long id, long refId);
	}
	
	private AdapterView.OnItemClickListener mItemClickListener = new AdapterView.OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			if ((mListFavorites != null) && (mListAdapter != null) && (!isEditMode()) 
					&& (mOnFavoritesListItemClickListener != null)) {
				
				Cursor cursor = (Cursor)mListAdapter.getItem(position);
				int colIdx = cursor.getColumnIndex(UserDataProvider.COLUMN_REFRENCE_ID);
				if (colIdx != -1) {
					long refId = cursor.getLong(colIdx);
					mOnFavoritesListItemClickListener.onFavoritesListItemClick(getDialog(), view, id, refId);
				}
			}
		}
	};
	
	private AdapterView.OnItemLongClickListener mItemLongClickListener = new AdapterView.OnItemLongClickListener() {

		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view, int position,
				long id) {
			if ((mListFavorites != null) && setEditMode()) {
				mListFavorites.setItemChecked(position, true);
				return true;
			}
			return false;
		}
	};
	
	private ViewGroup mFrameTitle;
	private ViewGroup mFrameTitleEdit;
	private View mActionEdit;
	private ListView mListFavorites;
	private FavoritesListAdapter mListAdapter;
	private OnFavoritesListItemClickListener mOnFavoritesListItemClickListener;
	
	@SuppressLint("InlinedApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setStyle(DialogFragment.STYLE_NO_TITLE, R.style.AppTranslucentTheme);
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Dialog dialog = super.onCreateDialog(savedInstanceState);
		
		dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
		
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				if (keyCode ==  KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
					if (onBackPressed()) {
						return true;
					}
					
					dismiss();
					return true;
				}
				return false;
			}
		});
		
		return dialog;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_favorites, container, false);
		
		mFrameTitle = (ViewGroup)rootView.findViewById(R.id.frameFavoritesTitle);
		mFrameTitleEdit = (ViewGroup)rootView.findViewById(R.id.frameFavoritesEditTitle);
		mFrameTitleEdit.setVisibility(View.GONE);
		
		View actionClose = mFrameTitle.findViewById(R.id.closeButton);
		ActionBarUtils.getInstance(getContext()).makeActionButton(actionClose);
		actionClose.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
		
		mActionEdit = mFrameTitle.findViewById(R.id.editButton);
		ActionBarUtils.getInstance(getContext()).makeActionButton(mActionEdit);
		mActionEdit.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				setEditMode();
			}
		});
		
		View actionDone = mFrameTitleEdit.findViewById(R.id.doneButton);
		ActionBarUtils.getInstance(getContext()).makeActionButton(actionDone);
		actionDone.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				performDone();
			}
		});
		
		View actionDelete = mFrameTitleEdit.findViewById(R.id.deleteButton);
		ActionBarUtils.getInstance(getContext()).makeActionButton(actionDelete);
		actionDelete.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				performDelete();
			}
		});
		
		View actionSelectAll = mFrameTitleEdit.findViewById(R.id.selectAllButton);
		ActionBarUtils.getInstance(getContext()).makeActionButton(actionSelectAll);
		actionSelectAll.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				performSelectAll();
			}
		});
		
		mListFavorites = (ListView)rootView.findViewById(R.id.listViewFavorites);
		mListFavorites.setSmoothScrollbarEnabled(true);
		mListFavorites.setOnItemClickListener(mItemClickListener);
		mListFavorites.setLongClickable(true);
		mListFavorites.setOnItemLongClickListener(mItemLongClickListener);
		
		setCancelable(false);
		bindList();
		
		return rootView;
	}
	
	private boolean onBackPressed() {
		if (isEditMode()) {
			performDone();
			return true;
		}
		return false;
	}
	
	private boolean bindList() {
		boolean hasData = false;
		if (mListFavorites != null) {
			Cursor cursor = UserQueryHelper.getInstance(getContext()).getAllFavorites();
			if (cursor != null) {
				mListAdapter = new FavoritesListAdapter(getContext(), cursor, UserDataProvider.COLUMN_WORD);
				mListFavorites.setAdapter(mListAdapter);
				hasData = mListAdapter.getCount() > 0;
			}
		}
		
		if (mActionEdit != null) {
			mActionEdit.setEnabled(hasData);
		}
		return hasData;
	}
	
	private boolean setEditMode() {
		if ((mListFavorites != null) && (mListAdapter != null)) {
			if (!mListAdapter.getCheckable()) {
				mListAdapter.setCheckable(true);
				mListAdapter.notifyDataSetInvalidated();
				
				if (mFrameTitleEdit != null) {
					mFrameTitleEdit.setVisibility(View.VISIBLE);
		        }
				if (mFrameTitle != null) {
					mFrameTitle.setVisibility(View.GONE);
		        }
				
				mListFavorites.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
			}
			return true;
		}
		return false;
	}
	
	private void releaseEditMode() {
		if (mFrameTitleEdit != null) {
			mFrameTitleEdit.setVisibility(View.GONE);
        }
		if (mFrameTitle != null) {
			mFrameTitle.setVisibility(View.VISIBLE);
        }
	}
	
	private void performDone() {
		bindList();
		releaseEditMode();
	}
	
	private void performDelete() {
		if ((mListFavorites != null) && (mListAdapter != null)) {
			final long[] checkedItems = mListFavorites.getCheckedItemIds();
			if ((checkedItems != null) && (checkedItems.length > 0)) {
				final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
				dialogBuilder.setIcon(android.R.drawable.ic_dialog_info);
				dialogBuilder.setTitle(R.string.favorites_edit_title);
				dialogBuilder.setMessage(R.string.favorites_delete_message);
				
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
						
						for(long id : checkedItems) {
							UserQueryHelper.getInstance(getContext()).removeFavorite(id);
						}
						
						if (bindList()) {
							setEditMode();
						} else {
							releaseEditMode();
						}
					}
				});
				dialogBuilder.show();
			}
		}
	}
	
	private void performSelectAll() {
		if (mListFavorites != null) {
			long[] checkedItems = mListFavorites.getCheckedItemIds();
			if ((checkedItems != null) && (checkedItems.length > 0)) {
				for(int i = 0; i < mListFavorites.getCount(); i++) {
					mListFavorites.setItemChecked(i, false);
				}
			} else {
				for(int i = 0; i < mListFavorites.getCount(); i++) {
					mListFavorites.setItemChecked(i, true);
				}
			}
		}
	}
	
	private boolean isEditMode() {
		if (mFrameTitleEdit != null) {
        	return mFrameTitleEdit.getVisibility() == View.VISIBLE;
        }
		return false;
	}
	
	public void setOnFavoritesListItemClickListener(OnFavoritesListItemClickListener listener) {
		mOnFavoritesListItemClickListener = listener;
	}
}
