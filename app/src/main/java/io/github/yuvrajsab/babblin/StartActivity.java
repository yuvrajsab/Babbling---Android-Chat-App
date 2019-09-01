package io.github.yuvrajsab.babblin;

import android.Manifest;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;
import com.github.paolorotolo.appintro.model.SliderPage;

public class StartActivity extends AppIntro {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setFadeAnimation();

        SliderPage sliderPage1 = new SliderPage();
        sliderPage1.setTitle("Welcome to Babblin");
        sliderPage1.setImageDrawable(R.drawable.babblin_logo);
        sliderPage1.setBgColor(Color.parseColor("#2196f3"));
        sliderPage1.setDescription(getResources().getString(R.string.babblin_start_intro));
        addSlide(AppIntroFragment.newInstance(sliderPage1));

        addSlide(SampleSlide.newInstance(R.layout.onboard_frag2));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            addSlide(SampleSlide.newInstance(R.layout.onboard_frag3));
            askForPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 3);
        }

        addSlide(SampleSlide.newInstance(R.layout.onboard_frag4));

        showSkipButton(false);
        showSeparator(false);
        showStatusBar(false);
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        Intent registerIntent = new Intent(StartActivity.this, RegisterActivity.class);
        registerIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(registerIntent);
        finish();
    }

    public static class SampleSlide extends Fragment {

        private static final String ARG_LAYOUT_RES_ID = "layoutResId";
        private int layoutResId;

        public static SampleSlide newInstance(int layoutResId) {
            SampleSlide sampleSlide = new SampleSlide();

            Bundle args = new Bundle();
            args.putInt(ARG_LAYOUT_RES_ID, layoutResId);
            sampleSlide.setArguments(args);

            return sampleSlide;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            if (getArguments() != null && getArguments().containsKey(ARG_LAYOUT_RES_ID)) {
                layoutResId = getArguments().getInt(ARG_LAYOUT_RES_ID);
            }
        }

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            return inflater.inflate(layoutResId, container, false);
        }
    }
}
