package com.s16.engmyan.fragment;

import com.s16.engmyan.R;
import com.s16.engmyan.Utility;
import com.s16.engmyan.data.RecentsListAdapter;
import com.s16.engmyan.data.UserDataProvider;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

public class RecentsFragment extends Fragment 
		implements OnItemClickListener, AnimationListener{

	private Context mContext;
	private int mVisibility;
	private Animation mShowAnimation;
	private Animation mHideAnimation;
	private ListView mListRecents;
	private RecentsListAdapter mListAdapter;
	private OnVisibilityChangeListener mOnVisibilityChangeListener;
	private OnRecentsListItemClickListener mOnRecentsListItemClickListener;
	
	public interface OnVisibilityChangeListener {
		void onVisibilityChanged(int visible);
	}
	
	public interface OnRecentsListItemClickListener {
		public void onRecentsListItemClick(View view, long id, long refId);
	}
	
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
	
	public RecentsFragment() {
		super();
		mVisibility = View.VISIBLE;
	}
	
	public RecentsFragment(Context context) {
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
		
		View view = inflater.inflate(R.layout.recents_fragment, container, false);
		if (mContext == null) {
			mContext = inflater.getContext();
		}
		
		ImageButton closeButton = (ImageButton)view.findViewById(R.id.closeButton);
		closeButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				performClose();
			}
		});
		closeButton.setLongClickable(true);
		closeButton.setOnLongClickListener(mViewOnLongClickListener);
		
		mListRecents = (ListView)view.findViewById(R.id.listViewRecents);
		mListRecents.setSmoothScrollbarEnabled(true);
		mListRecents.setSaveEnabled(true);
		mListRecents.setOnItemClickListener(this);
		
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
		if ((mListRecents != null) && (mListAdapter != null)  
				&& (mOnRecentsListItemClickListener != null)) {
			Cursor cursor = (Cursor)mListAdapter.getItem(position);
			if (!Utility.isNull(cursor, UserDataProvider.COLUMN_REFRENCE_ID)) {
				int refIdColumnIndex = cursor.getColumnIndex(UserDataProvider.COLUMN_REFRENCE_ID); 
				long refId = cursor.getLong(refIdColumnIndex);
				mOnRecentsListItemClickListener.onRecentsListItemClick(view, id, refId);
			}
		}
	}
	
	protected void setListData() {
		if (mListRecents != null) {
			Cursor cursor = UserDataProvider.getAllHistories(getContext());
			if (cursor != null) {
				mListAdapter = new RecentsListAdapter(getContext(), cursor
						, UserDataProvider.COLUMN_ID, UserDataProvider.COLUMN_WORD);
				mListRecents.setAdapter(mListAdapter);
			}
		}
	}
	
	protected void performClose() {
		hide();
	}
	
	public void setOnVisibilityChangeListener(OnVisibilityChangeListener listener) {
		mOnVisibilityChangeListener = listener;
	}
	
	public void setOnRecentsListItemClickListener(OnRecentsListItemClickListener listener) {
		mOnRecentsListItemClickListener = listener;
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
