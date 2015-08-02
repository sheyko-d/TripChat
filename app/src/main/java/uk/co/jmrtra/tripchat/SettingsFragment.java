package uk.co.jmrtra.tripchat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

public class SettingsFragment extends Fragment {

    private SharedPreferences mPrefs;
    private SwitchCompat mVibrateSwitch;
    private SwitchCompat mSoundSwitch;

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    public SettingsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_settings,
                container, false);

        mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        mVibrateSwitch = (SwitchCompat) rootView.findViewById(R.id.settings_vibrate_switch);
        mSoundSwitch = (SwitchCompat) rootView.findViewById(R.id.settings_sound_switch);

        initSwitches();

        ((TextView) rootView.findViewById(R.id.settings_name_txt))
                .setText(mPrefs.getString("name", ""));
        ((TextView) rootView.findViewById(R.id.settings_email_txt))
                .setText(mPrefs.getString("email", ""));

        rootView.findViewById(R.id.settings_exit_btn).setOnClickListener(new View
                .OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), SplashActivity.class));
                getActivity().finish();
            }
        });

        rootView.findViewById(R.id.settings_feedback_btn).setOnClickListener(new View
                .OnClickListener() {
            @Override
            public void onClick(View v) {
                final String appPackageName = getActivity().getPackageName();
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id="
                            + appPackageName)));
                } catch (android.content.ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse
                            ("https://play.google.com/store/apps/details?id=" + appPackageName)));
                }
            }
        });

        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder().cacheInMemory(true)
                .cacheOnDisk(true).showImageOnLoading(R.color.placeholder_bg)
                .showImageOnFail(R.drawable.avatar_placeholder)
                .displayer(new FadeInBitmapDisplayer(250, true, false, false))
                .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                getActivity()).defaultDisplayImageOptions(defaultOptions).build();
        ImageLoader.getInstance().init(config);
        ImageLoader.getInstance().displayImage(mPrefs.getString("avatar", ""), ((ImageView) rootView
                .findViewById(R.id.settings_avatar_img)), defaultOptions);

        return rootView;
    }

    private void initSwitches() {
        mVibrateSwitch.setChecked(mPrefs.getBoolean("vibrate", true));
        mSoundSwitch.setChecked(mPrefs.getBoolean("sound", true));

        mVibrateSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mPrefs.edit().putBoolean("vibrate", isChecked).apply();
            }
        });
        mSoundSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mPrefs.edit().putBoolean("sound", isChecked).apply();
            }
        });
    }

}