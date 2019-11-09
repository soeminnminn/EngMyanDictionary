package com.s16.engmyan.fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.s16.engmyan.Constants;
import com.s16.engmyan.R;
import com.s16.engmyan.activity.DetailsActivity;
import com.s16.engmyan.activity.MainActivity;
import com.s16.engmyan.data.DictionaryItem;
import com.s16.engmyan.widget.DefinitionView;
import com.s16.widget.AnimatingRelativeLayout;
import com.s16.widget.TouchImageView;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

/**
 * A fragment representing a single Item detail screen.
 * This fragment is either contained in a {@link MainActivity}
 * in two-pane mode (on tablets) or a {@link DetailsActivity}
 * on handsets.
 */
public class DetailsFragment extends Fragment
        implements AnimatingRelativeLayout.AnimationCompleteListener {

    protected static String TAG = DetailsFragment.class.getSimpleName();

    public interface DetailsDataChangeListener {
        void onNavigationChanged(boolean navBackEnabled, boolean navForwardEnabled);
        DictionaryItem onLoadDetailData(long id, String word);
        void onLoadFinished();
    }

    private DefinitionView.DefinitionViewClient mClient = new DefinitionView.DefinitionViewClient() {

        @Override
        public void onPageStarted(DefinitionView view, String url, DictionaryItem definition) {
            isDataLoading = true;
        }

        @Override
        public void onPageFinished(DefinitionView view, String url, DictionaryItem definition) {
            onPageLoaded(url, view.canGoBack(), view.canGoForward());
            isDataLoading = false;
        }

        @Override
        public void onLoadResource(DefinitionView view, String url, DictionaryItem definition) {
        }

        @Override
        public boolean onAnchorClick(DefinitionView view, String url, DictionaryItem definition) {
            return setDefinition(view, url);
        }

    };

    private static boolean isDataLoading = false;
    private TextToSpeech mTextToSpeech;
    private boolean mTextToSpeechEnabled;

    private AnimatingRelativeLayout mLayoutImageView;
    private TouchImageView mImageView;
    private ImageView mImageCaution;
    private DefinitionView mDefinitionView;

    private DictionaryItem mData;
    private DetailsDataChangeListener mDataChangeListener;

    public DetailsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_details, container, false);

        mLayoutImageView = rootView.findViewById(R.id.layoutImageView);
        mLayoutImageView.setAnimationCompleteListener(this);
        mLayoutImageView.setVisibility(View.GONE);

        mImageView = rootView.findViewById(R.id.imagePreView);
        mImageView.setMaxZoom(6);
        mImageCaution = rootView.findViewById(R.id.imageCaution);

        mDefinitionView = rootView.findViewById(R.id.layoutDefinitionView);
        mDefinitionView.setDefinitionViewClient(mClient);

        mTextToSpeech = new TextToSpeech(getContext(), new TextToSpeech.OnInitListener() {

            @Override
            public void onInit(int status) {
                mTextToSpeechEnabled = (status == TextToSpeech.SUCCESS);
            }
        });

        setImageBitmap();
        if (mData != null) {
            mDefinitionView.setDefinition(mData);
        } else {
            mDefinitionView.clear();
        }

        return rootView;
    }

    @Override
    public void onAnimationComplete() {
        if (mDefinitionView == null) return;
        if (mLayoutImageView == null) return;

        if (mLayoutImageView.isVisible()) {
            mDefinitionView.setVisibility(View.GONE);
        } else {
            mDefinitionView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        clear();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        clear();
    }

    private void onPageLoaded(String url, boolean navBackEnabled, boolean navForwardEnabled) {
        DictionaryItem item = loadDictionaryItem(url);
        if (item != null && (mData == null || mData.id != item.id)) {
            mData = item;
        }

        if (mDataChangeListener != null) {
            mDataChangeListener.onLoadFinished();
            mDataChangeListener.onNavigationChanged(navBackEnabled, navForwardEnabled);
        }
    }

    private DictionaryItem loadDictionaryItem(String url) {
        if (TextUtils.isEmpty(url)) return null;

        if (url.startsWith(Constants.URL_DEFINITION)) {
            Uri uri = Uri.parse(url);

            long id = -1;
            String queryParam = uri.getQueryParameter("id");
            if (!TextUtils.isEmpty(queryParam)) {
                id = Long.valueOf(queryParam);
            }
            queryParam = uri.getQueryParameter("w");

            if (mDataChangeListener != null) {
                return mDataChangeListener.onLoadDetailData(id, queryParam);
            }
        }
        return null;
    }

    private boolean setDefinition(DefinitionView view, String url) {
        if (view == null) return false;
        DictionaryItem itemData = loadDictionaryItem(url);
        if (itemData != null) {
            mData = itemData;
            view.setDefinition(itemData);
            return true;
        } else if (mData != null) {
            view.setDefinition(mData);
            return true;
        }

        return false;
    }

    private void setImageBitmap() {
        if (mData == null) return;

        Bitmap bitmap = null;
        if (mData.picture && !TextUtils.isEmpty(mData.filename)) {

            String picturePath = Constants.PICTURE_FOLDER + "/" + mData.filename + ".png";
            bitmap = getBitmapAssert(getContext(), picturePath);
        }

        if (mImageView != null) {
            if (bitmap != null) {
                if (mImageCaution != null) mImageCaution.setVisibility(View.GONE);
                mImageView.setVisibility(View.VISIBLE);
                mImageView.setImageBitmap(bitmap);
                mImageView.invalidate();
            } else {
                mImageView.setVisibility(View.GONE);
                if (mImageCaution != null) mImageCaution.setVisibility(View.VISIBLE);
            }
        }

        if ((mLayoutImageView != null) && (mLayoutImageView.isVisible())) {
            toggleImageView();
        }
    }

    public void setDetailsDataChangeListener(DetailsDataChangeListener listener) {
        mDataChangeListener = listener;
    }

    public void setData(DictionaryItem itemData) {
        if (isDataLoading) return;
        if ((itemData == null) || (itemData.id < 0)) return;

        isDataLoading = true;
        mData = itemData;
        setImageBitmap();
        mDefinitionView.setDefinition(itemData);
    }

    public long getDetailId() {
        if (mData != null) return mData.id;
        return -1;
    }

    public boolean getCanGoBack() {
        if (mDefinitionView == null) return false;
        return mDefinitionView.canGoBack();
    }

    public boolean getCanGoForward() {
        if (mDefinitionView == null) return false;
        return mDefinitionView.canGoForward();
    }

    public boolean getImageVisible() {
        if (mLayoutImageView != null) {
            return (mLayoutImageView.getVisibility() == View.VISIBLE);
        }
        return false;
    }

    public String getTitle() {
        if (mData != null) return mData.word;
        return Constants.EMPTY_STRING;
    }

    public boolean getHasPicture() {
        if (mData != null) return mData.picture;
        return false;
    }

    public boolean getHasSound() {
        return true;
    }

    public boolean performNavBack() {
        if (mDefinitionView == null) return false;
        if (isDataLoading) return false;
        if (mDefinitionView.canGoBack()) {
            mDefinitionView.goBack();

            if (mDataChangeListener != null) {
                mDataChangeListener.onNavigationChanged(mDefinitionView.canGoBack(), mDefinitionView.canGoForward());
            }
            return true;
        }
        return false;
    }

    public boolean performNavForward() {
        if (mDefinitionView == null) return false;
        if (isDataLoading) return false;
        if (mDefinitionView.canGoForward()) {
            mDefinitionView.goForward();

            if (mDataChangeListener != null) {
                mDataChangeListener.onNavigationChanged(mDefinitionView.canGoBack(), mDefinitionView.canGoForward());
            }
            return true;
        }
        return false;
    }

    public void toggleImageView() {
        if (mLayoutImageView == null) return;

        if (mLayoutImageView.isVisible()) {
            mLayoutImageView.hide();
        } else {
            mLayoutImageView.show();
        }
    }

    @SuppressWarnings("deprecation")
    public Bitmap getBitmapAssert(Context context, String path) {
        if (TextUtils.isEmpty(path)) return null;

        InputStream stream = null;
        try {
            stream = context.getAssets().open(path);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (stream != null) {
            BitmapFactory.Options option = new BitmapFactory.Options();
            option.inPurgeable = true;
            Bitmap bitmap = BitmapFactory.decodeStream(stream, null, option);

            try {
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return bitmap;
        }

        return null;
    }

    @SuppressWarnings("deprecation")
    public void doSpeak() {
        if (mData == null) return;
        if ((mTextToSpeech != null) && mTextToSpeechEnabled) {

            if (!TextUtils.isEmpty(mData.word)){
                try {
                    mTextToSpeech.setLanguage(Locale.US);

                    String text = mData.word.charAt(0) == '-' ? mData.word.substring(1) : mData.word;
                    if (mTextToSpeech.speak(text, 0, null) == TextToSpeech.SUCCESS) {
                        return;
                    }
                } catch(Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

        Toast.makeText(getContext(), getString(R.string.no_sound_message), Toast.LENGTH_LONG).show();
    }

    private void clear() {
        if (mTextToSpeech != null) {
            try {
                mTextToSpeech.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                mTextToSpeech.shutdown();
                mTextToSpeech = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
