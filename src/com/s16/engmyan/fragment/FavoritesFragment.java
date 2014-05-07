package com.s16.engmyan.fragment;

import com.s16.engmyan.R;
import com.s16.engmyan.Utility;
import com.s16.engmyan.data.FavoritesListAdapter;
import com.s16.engmyan.data.UserDataProvider;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class FavoritesFragment extends Fragment
	implements OnItemClickListener, AnimationListener {
	
	protected static String TAG = FavoritesFragment.class.getSimpleName();
	
	private Context mContext;
	private int mVisibility;
	
	private FrameLayout mFrameFavoritesTitle;
	private FrameLayout mFrameFavoritesEdit;
	private Animation mShowAnimation;
	private Animation mHideAnimation;
	private ListView mListFavorites;
	private FavoritesListAdapter mListAdapter;
	private OnVisibilityChangeListener mOnVisibilityChangeListener;
	private OnFavoritesListItemClickListener mOnFavoritesListItemClickListener;
	
	public interface OnVisibilityChangeListener {
		void onVisibilityChanged(int visible);
	}
	
	public interface OnFavoritesListItemClickListener {
		public void onFavoritesListItemClick(View view, long id, long refId);
	}
	
	private final AdapterView.OnItemLongClickListener mOnItemLongClickListener = new AdapterView.OnItemLongClickListener() {

		@Override
		public boolean onItemLongClick(AdapterView<?> arg0, View view, int position, long arg3) {
			if ((mListFavorites != null) && setEditMode()) {
				mListFavorites.setItemChecked(position, true);
				return true;
			}
			return false;
		}
		
	};
	
	private final View.OnLongClickListener mViewOnLongClickListener = new View.OnLongClickListener() {
		
		@Override
		public boolean onLongClick(View v) {
			if (v != null) {
				final CharSequence description = v.getContentDescription();
				if (!TextUtils.isEmpty(description)) {
					Toast.makeText(getContext(), description, Toast.LENGTH_LONG).show();
					return true;	
				}
			}
			return false;
		}
	};
	
	public FavoritesFragment() {
		super();
		mVisibility = View.VISIBLE;
	}
	
	public FavoritesFragment(Context context) {
		this();
		mContext = context;
	}
	
	protected Context getContext() {
		return mContext;
	}
	
	@Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        
        View view = getView();
		if (view != null) {
			view.setVisibility(mVisibility);
		}
		
		setListData();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.favorites_fragment, container, false);
		if (mContext == null) {
			mContext = inflater.getContext();
		}
		ImageButton doneButton = (ImageButton)view.findViewById(R.id.doneButton);
		doneButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				performDone();
			}
		});
		doneButton.setLongClickable(true);
		doneButton.setOnLongClickListener(mViewOnLongClickListener);
		
		ImageButton selectAllButton = (ImageButton)view.findViewById(R.id.selectAllButton);
		selectAllButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				performSelectAll();
			}
		});
		selectAllButton.setLongClickable(true);
		selectAllButton.setOnLongClickListener(mViewOnLongClickListener);
		
		ImageButton deleteButton = (ImageButton)view.findViewById(R.id.deleteButton);
		deleteButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				performDelete();
			}
		});
		deleteButton.setLongClickable(true);
		deleteButton.setOnLongClickListener(mViewOnLongClickListener);
		
		ImageButton closeButton = (ImageButton)view.findViewById(R.id.closeButton);
		closeButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				performClose();
			}
		});
		closeButton.setLongClickable(true);
		closeButton.setOnLongClickListener(mViewOnLongClickListener);
		
		mFrameFavoritesTitle = (FrameLayout)view.findViewById(R.id.frameFavoritesTitle);
		mFrameFavoritesEdit = (FrameLayout)view.findViewById(R.id.frameFavoritesEdit);
		mFrameFavoritesEdit.setVisibility(View.GONE);
		
		mListFavorites = (ListView)view.findViewById(R.id.listViewFavorites);
		mListFavorites.setSmoothScrollbarEnabled(true);
		mListFavorites.setSaveEnabled(true);
		mListFavorites.setOnItemClickListener(this);
		mListFavorites.setLongClickable(true);
		mListFavorites.setOnItemLongClickListener(mOnItemLongClickListener);
		
		mShowAnimation = (Animation)AnimationUtils.loadAnimation(mContext, R.anim.dialog_enter);
        mShowAnimation.setAnimationListener(this);
        
        mHideAnimation = (Animation)AnimationUtils.loadAnimation(mContext, R.anim.dialog_exit);
        mHideAnimation.setAnimationListener(this);
		
		return view;
	}
	
	@Override
	public void onAnimationStart(Animation animation) {
	}

	@Override
	public void onAnimationEnd(Animation animation) {
		if (mOnVisibilityChangeListener != null) {
			mOnVisibilityChangeListener.onVisibilityChanged(mVisibility);
		}
	}

	@Override
	public void onAnimationRepeat(Animation animation) {
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if ((mListFavorites != null) && (mListAdapter != null) && (!isEditMode()) 
				&& (mOnFavoritesListItemClickListener != null)) {
			Cursor cursor = (Cursor)mListAdapter.getItem(position);
			if (!Utility.isNull(cursor, UserDataProvider.COLUMN_REFRENCE_ID)) {
				int refIdColumnIndex = cursor.getColumnIndex(UserDataProvider.COLUMN_REFRENCE_ID); 
				long refId = cursor.getLong(refIdColumnIndex);
				mOnFavoritesListItemClickListener.onFavoritesListItemClick(view, id, refId);
			}
		}
	}
	
	protected void setListData() {
		if (mListFavorites != null) {
			Cursor cursor = UserDataProvider.getAllFavorites(getContext());
			if (cursor != null) {
				mListAdapter = new FavoritesListAdapter(getContext(), cursor
						, UserDataProvider.COLUMN_ID, UserDataProvider.COLUMN_WORD);
				mListFavorites.setAdapter(mListAdapter);
			}
		}
	}
	
	protected boolean setEditMode() {
		if ((mListFavorites != null) && (mListAdapter != null)) {
			if (!mListAdapter.getCheckable()) {
				mListAdapter.setCheckable(true);
				mListAdapter.notifyDataSetInvalidated();
				
				if (mFrameFavoritesEdit != null) {
		        	mFrameFavoritesEdit.setVisibility(View.VISIBLE);
		        }
				
				if (mFrameFavoritesTitle != null) {
					mFrameFavoritesTitle.setVisibility(View.GONE);
		        }
				
				mListFavorites.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
			}
			return true;
		}
		return false;
	}
	
	protected void performDone() {
		setListData();
		
		if (mFrameFavoritesEdit != null) {
        	mFrameFavoritesEdit.setVisibility(View.GONE);
        }
		
		if (mFrameFavoritesTitle != null) {
			mFrameFavoritesTitle.setVisibility(View.VISIBLE);
        }
	}
	
	protected void performDelete() {
		if ((mListFavorites != null) && (mListAdapter != null)) {
			final long[] checkedItems = mListFavorites.getCheckedItemIds();
			if ((checkedItems != null) && (checkedItems.length > 0)) {
				final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(new ContextThemeWrapper(getContext(), R.style.DialogTheme));
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
							UserDataProvider.removeFavorite(getContext(), id);
						}
						setListData();
						setEditMode();
					}
				});
				dialogBuilder.show();
			}
		}
	}
	
	protected void performSelectAll() {
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
	
	protected void performClose() {
		hide();
	}
	
	protected boolean isEditMode() {
		if (mFrameFavoritesEdit != null) {
        	return mFrameFavoritesEdit.getVisibility() == View.VISIBLE;
        }
		return false;
	}
	
	public boolean performBackPress() {
		if (isEditMode()) {
			performDone();
			return true;
		}
		return false;
	}
	
	public void setOnVisibilityChangeListener(OnVisibilityChangeListener listener) {
		mOnVisibilityChangeListener = listener;
	}
	
	public void setOnFavoritesListItemClickListener(OnFavoritesListItemClickListener listener) {
		mOnFavoritesListItemClickListener = listener;
	}
	
	public int getVisibility() {
		View view = getView();
		if (view != null) {
			mVisibility = view.getVisibility();
		}
		return mVisibility;
	}
	
	public void setVisibility(int visible) {
		mVisibility = visible;
		View view = getView();
		if ((view != null) && (view.getVisibility() != mVisibility)) {
			view.setVisibility(mVisibility);
			if (mOnVisibilityChangeListener != null) {
				mOnVisibilityChangeListener.onVisibilityChanged(mVisibility);
			}
		}
	}
	
	public void show() {
		View view = getView();
		if ((view != null) && (view.getVisibility() != View.VISIBLE)) {
			view.startAnimation(mShowAnimation);
			view.setVisibility(View.VISIBLE);
			mVisibility = View.VISIBLE;
			setListData();
		}
	}
	
	public void hide() {
		View view = getView();
		if ((view != null) && (view.getVisibility() != View.GONE)) {
			view.startAnimation(mHideAnimation);
			view.setVisibility(View.GONE);
			mVisibility = View.GONE;
		}
	}
}
