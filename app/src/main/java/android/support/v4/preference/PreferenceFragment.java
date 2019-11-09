package android.support.v4.preference;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class PreferenceFragment extends ListFragment
		implements PreferenceManagerCompat.OnPreferenceTreeClickListener {

	private static final String PREFERENCES_TAG = "android:preferences";
	/**
     * The starting request code given out to preference framework.
     */
    private static final int FIRST_REQUEST_CODE = 100;

    private static final int MSG_BIND_PREFERENCES = 1;
    
    /**
     * Interface that PreferenceFragment's containing activity should
     * implement to be able to process preference items that wish to
     * switch to a new fragment.
     */
    public interface OnPreferenceStartFragmentCallback {
        /**
         * Called when the user has clicked on a Preference that has
         * a fragment class name associated with it.  The implementation
         * to should instantiate and switch to an instance of the given
         * fragment.
         */
        boolean onPreferenceStartFragment(PreferenceFragment caller, Preference pref);
    }
    
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {

                case MSG_BIND_PREFERENCES:
                    bindPreferences();
                    break;
            }
        }
    };

    final private Runnable mRequestFocus = new Runnable() {
        public void run() {
        	if (getListView() != null) {
        		getListView().focusableViewAvailable(getListView());
        	}
        }
    };
	
	private PreferenceManager mPreferenceManager;
	private boolean mHavePrefs;
    private boolean mInitDone;

    @Override
	public void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);
		mPreferenceManager = PreferenceManagerCompat.newInstance(getActivity(), FIRST_REQUEST_CODE);
	}
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
    		Bundle savedInstanceState) {
    	View root = super.onCreateView(inflater, container, savedInstanceState);
    	
    	ListView listView = (ListView)root.findViewById(android.R.id.list);
    	if (listView != null) {
    		int horizontalPadding = dpToPx(8);
    		listView.setPadding(horizontalPadding, 0, horizontalPadding, 0);
    		listView.setClipToPadding(false);
    		listView.setScrollBarStyle(0x02000000); // outsideOverlay
    	}
    	return root;
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
    	super.onActivityCreated(savedInstanceState);
    	if (mHavePrefs) {
            bindPreferences();
        }

        mInitDone = true;

        if (savedInstanceState != null) {
            Bundle container = savedInstanceState.getBundle(PREFERENCES_TAG);
            if (container != null) {
                final PreferenceScreen preferenceScreen = getPreferenceScreen();
                if (preferenceScreen != null) {
                    preferenceScreen.restoreHierarchyState(container);
                }
            }
        }
    }
	
	@Override
	public void onStart() {
		super.onStart();
		PreferenceManagerCompat.setOnPreferenceTreeClickListener(mPreferenceManager, this);
	}
	
	@Override
	public void onStop() {
		super.onStop();
		PreferenceManagerCompat.dispatchActivityStop(mPreferenceManager);
		PreferenceManagerCompat.setOnPreferenceTreeClickListener(mPreferenceManager, null);
	}
	
	@Override
	public void onDestroyView() {
		mHandler.removeCallbacks(mRequestFocus);
		mHandler.removeMessages(MSG_BIND_PREFERENCES);
		super.onDestroyView();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		PreferenceManagerCompat.dispatchActivityDestroy(mPreferenceManager);
	}
	
	@Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        final PreferenceScreen preferenceScreen = getPreferenceScreen();
        if (preferenceScreen != null) {
            Bundle container = new Bundle();
            preferenceScreen.saveHierarchyState(container);
            outState.putBundle(PREFERENCES_TAG, container);
        }
    }
    
    @Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
        PreferenceManagerCompat.dispatchActivityResult(mPreferenceManager, requestCode, resultCode, data);
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {
		if (getActivity() instanceof OnPreferenceStartFragmentCallback) {
            return ((OnPreferenceStartFragmentCallback)getActivity()).onPreferenceStartFragment(
                    this, preference);
        }
        return false;
	}
	
	protected int dpToPx(int dp) {
		float density = getActivity().getResources().getDisplayMetrics().density;
	    return Math.round((float)dp * density);
	}
	
	/**
     * Returns the {@link PreferenceManager} used by this fragment.
     * @return The {@link PreferenceManager}.
     */
    public PreferenceManager getPreferenceManager() {
        return mPreferenceManager;
    }
    
    /**
     * Sets the root of the preference hierarchy that this fragment is showing.
     *
     * @param preferenceScreen The root {@link PreferenceScreen} of the preference hierarchy.
     */
    public void setPreferenceScreen(PreferenceScreen preferenceScreen) {
        if (PreferenceManagerCompat.setPreferences(mPreferenceManager, preferenceScreen) && preferenceScreen != null) {
            mHavePrefs = true;
            if (mInitDone) {
                postBindPreferences();
            }
        }
    }
    
    /**
     * Gets the root of the preference hierarchy that this fragment is showing.
     *
     * @return The {@link PreferenceScreen} that is the root of the preference
     *         hierarchy.
     */
    public PreferenceScreen getPreferenceScreen() {
        return PreferenceManagerCompat.getPreferenceScreen(mPreferenceManager);
    }
    
    /**
     * Adds preferences from activities that match the given {@link Intent}.
     *
     * @param intent The {@link Intent} to query activities.
     */
    public void addPreferencesFromIntent(Intent intent) {
        requirePreferenceManager();
        setPreferenceScreen(PreferenceManagerCompat.inflateFromIntent(mPreferenceManager, intent, getPreferenceScreen()));
    }
    
    /**
     * Inflates the given XML resource and adds the preference hierarchy to the current
     * preference hierarchy.
     *
     * @param preferencesResId The XML resource ID to inflate.
     */
    public void addPreferencesFromResource(int preferencesResId) {
        requirePreferenceManager();
        setPreferenceScreen(PreferenceManagerCompat.inflateFromResource(mPreferenceManager, getActivity(),
        		preferencesResId, getPreferenceScreen()));
    }
    
    /**
     * Finds a {@link Preference} based on its key.
     *
     * @param key The key of the preference to retrieve.
     * @return The {@link Preference} with the key, or null.
     * @see PreferenceGroup#findPreference(CharSequence)
     */
    public Preference findPreference(CharSequence key) {
        if (mPreferenceManager == null) {
            return null;
        }
        return mPreferenceManager.findPreference(key);
    }
    
    private void requirePreferenceManager() {
        if (mPreferenceManager == null) {
            throw new RuntimeException("This should be called after super.onCreate.");
        }
    }
	
    private void postBindPreferences() {
        if (mHandler.hasMessages(MSG_BIND_PREFERENCES)) return;
        mHandler.obtainMessage(MSG_BIND_PREFERENCES).sendToTarget();
    }
    
    private void bindPreferences() {
        final PreferenceScreen preferenceScreen = getPreferenceScreen();
        if (preferenceScreen != null) {
            preferenceScreen.bind(getListView());
        }
        setListShownNoAnimation(true);
    }
}
