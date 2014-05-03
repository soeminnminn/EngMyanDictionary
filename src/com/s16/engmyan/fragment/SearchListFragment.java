package com.s16.engmyan.fragment;

import com.s16.engmyan.R;
import com.s16.engmyan.data.DataProvider;
import com.s16.engmyan.data.ListAdapter;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

public class SearchListFragment extends Fragment
		implements OnItemClickListener {
	
	public interface OnSearchListItemClickListener {
		public void onSearchListItemClick(long id, CharSequence searchText);
	}
	
	protected static String TAG = SearchListFragment.class.getSimpleName();
	
	private Context mContext;
	private DataProvider mDataProvider;
	private EditText mTextSearch;
	private ImageButton mButtonClear;
	private ListView mResultList;
	private OnSearchListItemClickListener mOnSearchListItemClickListener;
	
	private final TextWatcher mTextSearchTextWatcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                int count) {
            //
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
    
    private final View.OnKeyListener mTextSearchOnKeyListener = new View.OnKeyListener() {

        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (event.getAction() == KeyEvent.ACTION_UP) {
                switch (keyCode) {
                case KeyEvent.KEYCODE_ENTER:
                	return performSearch();
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
				return performSearch();
			default:
				break;
		}
		
	    return false;
        }
    };
	
	public SearchListFragment() {
		super();
	}
	
	public SearchListFragment(Context context, DataProvider dataProvider) {
		super();
		mContext = context;
		mDataProvider = dataProvider;
	}
	
	protected Context getContext() {
		return mContext;
	}
	
	public DataProvider getDataProvider() {
		return mDataProvider;
	}
	
	public void setDataProvider(DataProvider dataProvider) {
		mDataProvider = dataProvider;
	}
	
	public void setSearchText(String text) {
		if (mTextSearch != null) {
			mTextSearch.setText(text);
		}
	}
	
	public String getSearchText() {
		if (mTextSearch != null) {
			return mTextSearch.getText().toString(); 
		}
		return "";
	}
	
	public void setSelection() {
		if (mTextSearch != null) {
			CharSequence constraint = mTextSearch.getText();
			int index = TextUtils.isEmpty(constraint) ? 0 : constraint.length();
			mTextSearch.setSelection(index);
			mTextSearch.requestFocus();
		}
	}
	
	public void setOnSearchListItemClickListener(OnSearchListItemClickListener listener) {
		mOnSearchListItemClickListener = listener;
	}
	
	@Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.search_list_fragment, container, false);
		if (mContext == null) {
			mContext = inflater.getContext();
		}
		
		initialize(view);
		return view;
	}
	
	@Override
	public void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		
		if (mOnSearchListItemClickListener != null) {
			CharSequence searchText = mTextSearch.getText();
			mOnSearchListItemClickListener.onSearchListItemClick(id, searchText);
		}
	}

	private void initialize(View view) {
		
		mButtonClear = (ImageButton)view.findViewById(R.id.btnSearhClear);
		mButtonClear.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				clearText();
			}
		});
		
		mTextSearch = (EditText)view.findViewById(R.id.txtSearch);
		mTextSearch.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
		mTextSearch.setHint(R.string.search_hint);
		mTextSearch.setFocusableInTouchMode(true);
		mTextSearch.setOnKeyListener(mTextSearchOnKeyListener);
		mTextSearch.setOnEditorActionListener(mTextSearchOnEditorActionListener);
		mTextSearch.addTextChangedListener(mTextSearchTextWatcher);
		mTextSearch.requestFocus();
		mTextSearch.setSaveEnabled(true);
		
		mResultList = (ListView)view.findViewById(R.id.listViewResult);
		mResultList.setSmoothScrollbarEnabled(true);
		mResultList.setFocusable(false);
		mResultList.setSaveEnabled(true);
	}
	
	public void clearText() {
		if (mTextSearch != null) {
			mTextSearch.clearComposingText();
			mTextSearch.setText(R.string.empty_string);
		}
	}
	
	public void prepareSearch() {
		if ((mDataProvider != null) && (mDataProvider.isOpen())) {
			
			Cursor cursor = null;
			CharSequence searchText = mTextSearch.getText();
			if(!TextUtils.isEmpty(searchText)) {
				cursor = mDataProvider.query(searchText.toString());
			} else {
				cursor = mDataProvider.querySuggestWord();
			}
			
			ListAdapter listAdapter = new ListAdapter(getContext(), cursor, DataProvider.COLUMN_ID, DataProvider.COLUMN_WORD);
			listAdapter.setFilterQueryProvider(new FilterQueryProvider() {
		         public Cursor runQuery(CharSequence constraint) {
		             return mDataProvider.query(constraint.toString());
		         }
			});
			mResultList.setAdapter(listAdapter);
			mResultList.setOnItemClickListener(this);
			//listAdapter.initLoadMore(mResultList, 10);
		}
	}
	
	public boolean performSearch() {
		if (mResultList == null) return false;
		if (mTextSearch == null) return false;
		if (mDataProvider == null) return false;
		
		ListAdapter listAdapter = (ListAdapter)mResultList.getAdapter();
		if (listAdapter != null) {
			CharSequence constraint = mTextSearch.getText();
			listAdapter.getFilter().filter(constraint);
			return true;
		}
		
		return false;
	}
}
