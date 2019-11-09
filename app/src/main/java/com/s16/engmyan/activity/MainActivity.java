package com.s16.engmyan.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


import com.s16.app.AboutPreference;
import com.s16.app.ProgressDialog;
import com.s16.engmyan.Common;
import com.s16.engmyan.Constants;
import com.s16.engmyan.data.DictionaryDataProvider;
import com.s16.engmyan.R;
import com.s16.engmyan.data.DictionaryItem;
import com.s16.engmyan.data.SearchQueryHelper;
import com.s16.engmyan.data.UserQueryHelper;
import com.s16.engmyan.fragment.DetailsFragment;
import com.s16.engmyan.fragment.MainListFragment;
import com.s16.engmyan.service.InstallationService;
import com.s16.widget.ActionBarNavigationButtons;
import com.s16.widget.SearchBarView;

import java.io.File;

/**
 * An activity representing a list of Items. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link DetailsActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class MainActivity extends AppCompatActivity
        implements SearchBarView.OnQueryTextListener,
        MainListFragment.OnListItemClickListener,
        ActionBarNavigationButtons.OnActionBarNavigationClickListener {

    private class InstallBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            int dataStatus = intent.getIntExtra(InstallationService.EXTENDED_DATA_STATUS, -1);
            if (dataStatus == InstallationService.STATE_ACTION_STARTED) {
                showInstallProgress();

            } else if (dataStatus == InstallationService.STATE_ACTION_PROGRESS) {
                int progress = intent.getIntExtra(InstallationService.EXTENDED_PROGRESS_VALUE, -1);
                if (progress > -1) {
                    setInstallProgress(progress);
                }

            } else if (dataStatus == InstallationService.STATE_ACTION_COMPLETE) {
                hideInstallProgress();
                Common.showMessage(getContext(), R.string.install_complete_message);
                openDatabase(Common.getDatabaseFile(getContext()));

            } else {
                Common.showMessage(getContext(), R.string.install_error_message);
                //System.exit(0);
            }
        }

    }

    private DetailsFragment.DetailsDataChangeListener mDataChangeListener = new DetailsFragment.DetailsDataChangeListener() {

        @Override
        public void onNavigationChanged(boolean navBackEnabled,
                                        boolean navForwardEnabled) {
            if (mActionBarContent != null) {
                mActionBarContent.setNavBackEnabled(navBackEnabled);
                mActionBarContent.setNavForwardEnabled(navForwardEnabled);
            }
        }

        @Override
        public void onLoadFinished() {
            setDetailsTitle();
            setIfFavorties();
        }

        @Override
        public DictionaryItem onLoadDetailData(long id, String word) {
            if (id > -1) {
                return DictionaryItem.getFrom(SearchQueryHelper.getInstance(getContext()), id);
            }
            return DictionaryItem.getFrom(SearchQueryHelper.getInstance(getContext()), word);
        }
    };

    private InstallBroadcastReceiver mInstallBroadcastReceiver;
    private ProgressDialog mInstallDialog;
    private SearchBarView mTextSearch;
    private ActionBarNavigationButtons mActionBarContent;
    private MainListFragment mListFragment;
    private DetailsFragment mDetailsFragment;

    private MenuItem mMenuItemFavorite;
    private MenuItem mMenuItemSound;
    private MenuItem mMenuItemPicture;

    protected Context getContext() {
        return this;
    }

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean isTwoPane() {
        // The detail container view will be present only in the
        // large-screen layouts (res/values-w900dp).
        // If this view is present, then the
        // activity should be in two-pane mode.
        return (findViewById(R.id.detailsContainer) != null);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String constraint = prefs.getString(Constants.SEARCH_TEXT_KEY, null);

        mTextSearch = findViewById(R.id.searchBarView);
        mTextSearch.setOnQueryTextListener(this);
        mTextSearch.setText(constraint);
        mTextSearch.requestFocus();

        FragmentManager manager = getSupportFragmentManager();
        mListFragment = (MainListFragment)manager.findFragmentById(R.id.listContainer);
        mListFragment.setOnListItemClickListener(this);

        initializeDetails(toolbar);
        performInstall();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (isTwoPane()) {
            getMenuInflater().inflate(R.menu.main_two_pane, menu);
        } else {
            getMenuInflater().inflate(R.menu.main, menu);
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        mMenuItemFavorite = menu.findItem(R.id.action_favorite);
        if (mMenuItemFavorite != null) {
            setIfFavorties();
        }

        mMenuItemSound = menu.findItem(R.id.action_sound);
        if (mMenuItemSound != null) {
            if (mDetailsFragment != null)
                mMenuItemSound.setVisible(mDetailsFragment.getHasSound());
            else
                mMenuItemSound.setVisible(false);
        }

        mMenuItemPicture = menu.findItem(R.id.action_picture);
        if (mMenuItemPicture != null) {
            if (mDetailsFragment != null)
                mMenuItemPicture.setVisible(mDetailsFragment.getHasPicture());
            else
                mMenuItemPicture.setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_favorite:
                performFavorite();
                break;
            case R.id.action_sound:
                performSpeak();
                break;
            case R.id.action_picture:
                toggleImageView();
                break;
            case R.id.action_recent:
                performManageRecents();
                break;
            case R.id.action_manage_favorites:
                performManageFavorites();
                break;
            case R.id.action_settings:
                performSettings();
                break;
            case R.id.action_about:
                performAbout();
                break;
            case R.id.action_exit:
                performExit();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initializeDetails(Toolbar toolbar) {

        FragmentManager manager = getSupportFragmentManager();
        Fragment fragment = manager.findFragmentById(R.id.detailsContainer);
        if (fragment != null) {
            mDetailsFragment = (DetailsFragment)fragment;
            getSupportActionBar().setDisplayShowTitleEnabled(false);

            mActionBarContent = (ActionBarNavigationButtons)toolbar.findViewById(R.id.frameToolbarContent);
            if (mActionBarContent != null) {
                mActionBarContent.setNavigationVisible(true);
                mActionBarContent.setNavBackEnabled(mDetailsFragment.getCanGoBack());
                mActionBarContent.setNavForwardEnabled(mDetailsFragment.getCanGoForward());
                mActionBarContent.setNavigationClickListener(this);
            }

            mDetailsFragment.setDetailsDataChangeListener(mDataChangeListener);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Common.isServiceRunning(getContext(), InstallationService.class)) {
            registerInstallReceiver();
        }
    }

    @Override
    protected void onDestroy() {
        cleanAndSave();
        super.onDestroy();
    }

    @Override
    public void onQueryTextChanged(CharSequence query, int count) {
        if (mListFragment != null) {
            mListFragment.performSearch(query);
        }
    }

    @Override
    public boolean onQuerySubmit(CharSequence query) {
        if (mListFragment != null) {
            return mListFragment.submitSearch(query);
        }
        return false;
    }

    @Override
    public void onListItemClick(long id, CharSequence searchText) {
        if (id < 0) return;

        DictionaryItem itemData = DictionaryItem.getFrom(SearchQueryHelper.getInstance(getContext()), id);
        UserQueryHelper.getInstance(getContext()).createHistory(itemData.word, id);

        if (mDetailsFragment == null) {
            Intent intent = new Intent(getBaseContext(), DetailsActivity.class);
            intent.putExtra(Constants.DETAIL_ID_KEY, id);
            ActivityCompat.startActivity(this, intent, null);

        } else {
            setDetailsData(itemData);
        }
    }

    @Override
    public void onNavigationForward(View v) {
        if (mDetailsFragment != null) {
            mDetailsFragment.performNavForward();
        }
    }

    @Override
    public void onNavigationBack(View v) {
        if (mDetailsFragment != null) {
            mDetailsFragment.performNavBack();
        }
    }

    private void performSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    private void performAbout() {
        AboutPreference.showAboutDialog(getContext());
    }

    private void performExit() {
        finish();
        System.exit(0);
    }

    private void performManageRecents() {
    }

    private void performManageFavorites() {
    }

    private void performFavorite() {

    }

    private void performSpeak() {
        if (mDetailsFragment != null) {
            mDetailsFragment.doSpeak();
        }
    }

    private void toggleImageView() {
        if (mDetailsFragment != null) {
            mDetailsFragment.toggleImageView();
        }
    }

    private void setIfFavorties() {
        if ((mDetailsFragment != null) && (mMenuItemFavorite != null)) {
            long id = mDetailsFragment.getDetailId();
            if ((id > -1) && (UserQueryHelper.getInstance(getContext()).isFavorited(id))) {
                mMenuItemFavorite.setIcon(R.drawable.ic_favorite_on_36dp);
            } else {
                mMenuItemFavorite.setIcon(R.drawable.ic_favorite_off_36dp);
            }
        }
    }

    private void showInstallProgress() {
        if (mInstallDialog == null) {
            CharSequence message = getContext().getText(R.string.install_message);
            mInstallDialog = new ProgressDialog(getContext());
            mInstallDialog.setMessage(message);
            mInstallDialog.setIndeterminate(false);
            mInstallDialog.setCancelable(false);
            mInstallDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mInstallDialog.setMax(100);
            mInstallDialog.show();
        } else {
            mInstallDialog.show();
        }
    }

    private void setInstallProgress(final int progress) {
        if (mInstallDialog != null && mInstallDialog.isShowing()) {
            mInstallDialog.setProgress(progress);
        }
    }

    private void hideInstallProgress() {
        if (mInstallDialog != null) {
            mInstallDialog.hide();
        }
    }

    private synchronized void performInstall() {
        File dbFile = Common.getDatabaseFile(getContext());
        if(dbFile == null) {
            Common.showMessage(getContext(), R.string.install_error_folder_create);
            return;
        }

        boolean isSuccess = dbFile.exists();
        //isSuccess = (isSuccess && Checksum.checkMD5(Constants.DATABASE_FILE_MD5, dbFile));
        isSuccess = (isSuccess && DictionaryDataProvider.versionCheck(this, dbFile));

        if(!isSuccess && dbFile.exists() && !dbFile.delete()) {
            Common.showMessage(getContext(), R.string.install_error_data_load);
            return;
        }

        if(!isSuccess || !dbFile.exists()) {
            final File dataFolder = dbFile.getParentFile();
            registerInstallReceiver();

            Bundle args = new Bundle();
            args.putString(InstallationService.INSTALL_ASSETS_NAME, Constants.ASSERT_ZIP_PKG);
            args.putString(InstallationService.INSTALL_FOLDER, dataFolder.getAbsolutePath());

            Intent serviceIntent = new Intent(getContext(), InstallationService.class);
            serviceIntent.putExtras(args);
            startService(serviceIntent);

        } else {
            openDatabase(dbFile);
        }
    }

    private void registerInstallReceiver() {
        if (mInstallBroadcastReceiver == null) {
            mInstallBroadcastReceiver = new InstallBroadcastReceiver();
            IntentFilter filter = new IntentFilter(InstallationService.BROADCAST_ACTION);
            filter.addCategory(Intent.CATEGORY_DEFAULT);
            LocalBroadcastManager.getInstance(getContext()).registerReceiver(mInstallBroadcastReceiver, filter);
        }
    }

    private void openDatabase(File dbFile) {
        if (dbFile == null) return;

        getContentResolver().call(DictionaryDataProvider.CONTENT_URI, DictionaryDataProvider.METHOD_OPEN,
                dbFile.getAbsolutePath(), Bundle.EMPTY);

        if (mListFragment != null) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            String constraint = prefs.getString(Constants.SEARCH_TEXT_KEY, "");
            long id = prefs.getLong(Constants.DETAIL_ID_KEY, -1);

            mListFragment.prepareSearch(constraint);

            if (id > 0) {
                DictionaryItem itemData = DictionaryItem.getFrom(SearchQueryHelper.getInstance(getContext()), id);
                setDetailsData(itemData);
            }
        }
    }

    private void setDetailsTitle() {
        if (!isTwoPane()) return;
        if (mDetailsFragment == null) return;

        if (mActionBarContent != null) {
            String detailTitle = mDetailsFragment.getTitle();
            String title = getString(R.string.app_name) + " [ " + detailTitle + " ]";
            mActionBarContent.setTitle(title);
        }
    }

    private void setDetailsData(DictionaryItem itemData) {
        if (!isTwoPane()) return;
        if (mDetailsFragment == null) return;
        if (itemData == null) return;

        mDetailsFragment.setData(itemData);
        setDetailsTitle();

        if (mMenuItemPicture != null) {
            mMenuItemPicture.setVisible(mDetailsFragment.getHasPicture());
        }

        if (mMenuItemSound != null) {
            mMenuItemSound.setVisible(mDetailsFragment.getHasSound());
        }

        if (mActionBarContent != null) {
            mActionBarContent.setNavBackEnabled(mDetailsFragment.getCanGoBack());
            mActionBarContent.setNavForwardEnabled(mDetailsFragment.getCanGoForward());
        }

        setIfFavorties();
    }

    private void cleanAndSave() {
        saveState();

        if (mInstallBroadcastReceiver != null) {
            LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mInstallBroadcastReceiver);
            mInstallBroadcastReceiver = null;
        }
    }

    private void saveState() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();

        CharSequence constraint = mTextSearch.getText();
        if (constraint == null) {
            editor.putString(Constants.SEARCH_TEXT_KEY, null);
        } else {
            editor.putString(Constants.SEARCH_TEXT_KEY, constraint.toString());
        }

        if (mDetailsFragment != null) {
            long id = mDetailsFragment.getDetailId();
            editor.putLong(Constants.DETAIL_ID_KEY, id);
        } else {
            editor.putLong(Constants.DETAIL_ID_KEY, -1);
        }

        editor.commit();
    }
}
