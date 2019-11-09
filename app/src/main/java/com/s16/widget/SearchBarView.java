package com.s16.widget;

import com.s16.engmyan.R;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Filterable;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.TextView.BufferType;

public class SearchBarView extends FrameLayout {

	protected static String TAG = SearchBarView.class.getSimpleName();
	protected static CharSequence EMPTY_STRING = "";
	
	private AutoCompleteTextView mTextSearch;
	private ImageButton mButtonSearch;
	private ImageButton mButtonClear;
	private CharSequence mSearchText;
	private boolean mIsSearching;
	
	private ListAdapter mAdapter;
	private OnQueryTextListener mOnQueryTextListener;
	
	public static interface OnQueryTextListener {

		void onQueryTextChanged(CharSequence query, int count);
        boolean onQuerySubmit(CharSequence query);
    }
	
	private final TextWatcher mTextSearchTextWatcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                int count) {
        	onQueryTextChanged(s, count);
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
        	if (mButtonClear == null) return;
        	if (TextUtils.isEmpty(mTextSearch.getText())) {
        		mButtonClear.setVisibility(View.GONE);
            } else
                mButtonClear.setVisibility(View.VISIBLE);
        }
        
    };

    
    private final OnKeyListener mTextSearchOnKeyListener = new OnKeyListener() {

        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (event.getAction() == KeyEvent.ACTION_UP) {
                switch (keyCode) {
	                case KeyEvent.KEYCODE_ENTER:
	                	return onQuerySubmit();
	                case KeyEvent.KEYCODE_ESCAPE:
	                	clearText();
	                    return true;
                }
            }

            return false;
        }
    };
    
    private final TextView.OnEditorActionListener mTextSearchOnEditorActionListener = new TextView.OnEditorActionListener() {

        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        	switch(actionId) {
				case EditorInfo.IME_ACTION_GO:
				case EditorInfo.IME_ACTION_NEXT:
				case EditorInfo.IME_ACTION_DONE:
				case EditorInfo.IME_ACTION_SEARCH:
				case EditorInfo.IME_ACTION_SEND:
				case EditorInfo.IME_ACTION_UNSPECIFIED:
					return onQuerySubmit();
				default:
					break;
			}
			
		    return false;
        }
    };
	
	public SearchBarView(Context context) {
		super(context);
		initialize(context, null);
	}
	
	public SearchBarView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public SearchBarView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize(context, attrs);
	}
	
	private void initialize(Context context, AttributeSet attrs) {
		if (isInEditMode()) {
			return;
		}
		
		TypedArray a = context.obtainStyledAttributes(attrs, new int[] { android.R.attr.text, android.R.attr.textColor, 
				android.R.attr.hint, android.R.attr.textColorHint });
		CharSequence text = a.getText(0);
		int textColor = a.getColor(1, -1);
		CharSequence hint = a.getText(2);
		int hintColor = a.getColor(3, -1);
		a.recycle();
		
		LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.searchbar_view, this, true);
        
        mButtonSearch = (ImageButton)findViewById(R.id.btnSearchNormal);
        mTextSearch = (AutoCompleteTextView)findViewById(R.id.txtSearch);
        mButtonClear = (ImageButton)findViewById(R.id.btnSearchClear);
        
        mTextSearch.addTextChangedListener(mTextSearchTextWatcher);
        mTextSearch.setOnKeyListener(mTextSearchOnKeyListener);
        mTextSearch.setOnEditorActionListener(mTextSearchOnEditorActionListener);
        mTextSearch.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        
        if (text != null) mTextSearch.setText(text);
        if (textColor != -1) mTextSearch.setTextColor(textColor);
        if (hint != null) mTextSearch.setHint(hint);
        if (hintColor != -1) mTextSearch.setHintTextColor(hintColor);
        
		mTextSearch.setFocusableInTouchMode(true);
		mTextSearch.setSaveEnabled(true);
		mTextSearch.requestFocus();
        
        mButtonClear.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				clearText();
			}
		});
        mButtonClear.setVisibility(View.GONE);
	}
	
	@Override
    public Parcelable onSaveInstanceState() {
		Bundle bundle = new Bundle();
	    bundle.putParcelable("instanceState", super.onSaveInstanceState());
	    bundle.putCharSequence("searchText", mSearchText);
	    return bundle;
	}
	
	@Override
    public void onRestoreInstanceState(Parcelable state) {
		if (state instanceof Bundle) {
	        Bundle bundle = (Bundle)state;
	        mSearchText = bundle.getCharSequence("searchText");
	        super.onRestoreInstanceState(bundle.getParcelable("instanceState"));
	        
	        mTextSearch.setText(mSearchText);
	        return;
		}
		
		super.onRestoreInstanceState(state);
	}
	
	@Override
    public void setEnabled(boolean enabled) {
		if (isEnabled() == enabled)
            return;

        for (View v : new View[] { mButtonSearch, mTextSearch, mButtonClear })
            v.setEnabled(enabled);
        
		super.setEnabled(enabled);
	}
	
    public final void setHint(CharSequence hint) {
    	if (mTextSearch != null) {
    		mTextSearch.setHint(hint);
    	}
	}
    
    public final void setHint(int resid) {
    	if (mTextSearch != null) {
    		mTextSearch.setHint(resid);
    	}
    }
    
    public Typeface getTypeface() {
    	if (mTextSearch != null) {
    		return mTextSearch.getTypeface();
    	}
    	return Typeface.DEFAULT;
    }
    
    public void setTypeface(Typeface tf) {
    	if (mTextSearch != null) {
    		mTextSearch.setTypeface(tf);
    	}
    }
    
    public void setTextColor(int color) {
    	if (mTextSearch != null) {
    		mTextSearch.setTextColor(color);
    	}
    }
    
    public void setTextColor(ColorStateList colors) {
    	if (mTextSearch != null) {
    		mTextSearch.setTextColor(colors);
    	}
    }
	
    public final void setHintTextColor(int color) {
    	if (mTextSearch != null) {
    		mTextSearch.setHintTextColor(color);
    	}
    }
    
    public final void setHintTextColor(ColorStateList colors) {
    	if (mTextSearch != null) {
    		mTextSearch.setHintTextColor(colors);
    	}
    }
    
	@Deprecated
	public OnQueryTextListener getOnQueryTextListener() {
        return mOnQueryTextListener;
    }
	
	public void setOnQueryTextListener(OnQueryTextListener listener) {
        mOnQueryTextListener = listener;
    }
	
	public CharSequence getText() {
		if (mTextSearch != null) {
			mSearchText = mTextSearch.getText();
		}
        return mSearchText;
    }
	
	public void setText(CharSequence text, BufferType type) {
		mSearchText = text;
		if (mTextSearch != null) {
			mTextSearch.setText(mSearchText, type);
		}
	}
	
	public final void setText(char[] text, int start, int len) {
		mSearchText = String.valueOf(text);
		if (mTextSearch != null) {
			mTextSearch.setText(text, start, len);
		}
	}
	
	public final void setText(int resid) {
		setText(getContext().getResources().getText(resid));
	}
	
	public final void setText(int resid, BufferType type) {
		setText(getContext().getResources().getText(resid), type);
	}
	
	public void setText(CharSequence text) {
		mSearchText = text;
		if (mTextSearch != null) {
			mTextSearch.setText(mSearchText);
		}
	}
	
	public void setSelection(int index) {
		if (mTextSearch != null) {
			mTextSearch.setSelection(index);
		}
	}
	
	public void setSelection(int start, int stop) {
		if (mTextSearch != null) {
			mTextSearch.setSelection(start, stop);
		}
	}
	
	public ListAdapter getAdapter() {
        return mAdapter;
    }
	
	public <T extends ListAdapter & Filterable> void setAdapter(T adapter) {
		mAdapter = adapter;
		if (mTextSearch != null) {
			mTextSearch.setAdapter(adapter);
		}
	}
	
	public TextView getTextView() {
		return mTextSearch;
	}
	
	protected void onQueryTextChanged(CharSequence s, int count) {
		if (mTextSearch == null) return;
		mSearchText = s;
		if (!mIsSearching && mOnQueryTextListener != null) {
			mOnQueryTextListener.onQueryTextChanged(s, count);
		}
	}
	
	protected boolean onQuerySubmit() {
		if (mTextSearch == null) return false;
		if (!mIsSearching && mOnQueryTextListener != null) {
			if (mSearchText == null) {
				mSearchText = EMPTY_STRING;
			}
			return mOnQueryTextListener.onQuerySubmit(mSearchText);
		}
		return false;
	}
	
	public void clearText() {
		if (mTextSearch != null) {
			mTextSearch.clearComposingText();
			mTextSearch.setText(EMPTY_STRING);
			mSearchText = EMPTY_STRING;
		}
	}
	
	public boolean isSearching() {
		return mIsSearching;
	}
	
	public void setIsSearching(boolean value) {
		mIsSearching = value;
	}
}
