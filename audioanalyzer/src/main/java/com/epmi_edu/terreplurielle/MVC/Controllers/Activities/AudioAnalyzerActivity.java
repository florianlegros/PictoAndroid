package com.epmi_edu.terreplurielle.MVC.Controllers.Activities;

import android.Manifest;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;

import androidx.annotation.RequiresApi;

import com.epmi_edu.terreplurielle.MVC.Models.PermissionsModel;
import com.epmi_edu.terreplurielle.MVC.Views.AudioAnalyzerView;
import com.epmi_edu.terreplurielle.Utils.ErrorReporting;
import com.epmi_edu.terreplurielle.Utils.Functions;
import com.epmi_edu.terreplurielle.audioanalyzer.R;

import java.util.HashMap;

public class AudioAnalyzerActivity extends BasicActivity {
    public static int TTS_FRAG = 0, TEACHER_FRAG = 1, KID_FRAG = 2;
    private AudioAnalyzerView mAudioAnalyzerView;
    private int TTS_CHECK_CODE = 0;
    private AudioManager mAudioManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);

            mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
            mAudioAnalyzerView = new AudioAnalyzerView(this,
                    mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC),
                    mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
                    savedInstanceState);

            Intent check = new Intent();
            check.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
            startActivityForResult(check, TTS_CHECK_CODE);
        } catch (IllegalStateException e) {
            new ErrorReporting(e, this.getClass().getName());
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (outState != null)
            outState.putInt("volumeControlVisibilityState", mAudioAnalyzerView.getVolumeControlVisibilityState());
    }

    /**
     * onMessage receives messages sent either by the Model or the View using the MVP model
     *
     * @param message : string identifying the view's method to invoke
     * @param args    : arguments used by the the invoked view's method (null if the method requires no arguments)
     */

    @Override
    public void onMessage(final String message, final HashMap<String, Object> args) {
        try {
            Functions.HandleUIFromAnotherThread(this, new Runnable() {
                public void run() {
                    switch (message) {
                        case "on-header-button":
                            mAudioAnalyzerView.onMessage("on-header-button", args);
                            break;

                        case "check-fragment":
                            mAudioAnalyzerView.onMessage("check-fragment", args);
                            break;

                        case "tts-pictogram-speak":
                            mAudioAnalyzerView.onMessage("tts-pictogram-speak", args);
                            break;

                        case "set-sample-rate":
                            mAudioAnalyzerView.onMessage("set-sample-rate", args);
                            break;

                        case "update-number-picker-max-index"://called by ChartFragment if
                            // the fragment's window length < pow(2, number picker's current value)
                            mAudioAnalyzerView.onMessage("update-number-picker-max-index", args);
                            break;

                        case "update-number-picker-index"://called by ChartFragment.
                            mAudioAnalyzerView.onMessage("update-number-picker-index", args);
                            break;

                        case "audio-record-max-time-reached":
                            mAudioAnalyzerView.onMessage("audio-record-max-time-reached", args);
                            break;

                        case "audio-player-complete":
                            mAudioAnalyzerView.onMessage("audio-player-complete", args);
                            break;

                        case "volume-changed":
                            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (int) args.get("value"), 0);
                            break;

                        case "enable-views":
                            enableViews((Boolean) args.get("enable"));
                    }
                }
            });
        } catch (Exception e) {
            new ErrorReporting(e, this.getClass().getName());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onClick(View v) {
        mAudioAnalyzerView.onClick(v);
    }

    @Override
    protected void requestPermissionResult(boolean permissionGranted, String permission[]) {
        if (permissionGranted) {
            if (permission[0].equals(Manifest.permission.RECORD_AUDIO)) {
                HashMap<String, Object> args = new HashMap<>();
                args.put("fragment-index", getIntent().getIntExtra("fragment-index", 1));
                mAudioAnalyzerView.onMessage("start-audio-record", args);
            } else if (permission[0].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE))
                executeTTS();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mAudioAnalyzerView.onMessage("stop-medias", null);
    }

    @Override
    public void finish() {
        super.finish();

        mAudioAnalyzerView.onMessage("clear-chart-data", null);
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        try {
            if (requestCode == TTS_CHECK_CODE) {
                if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) executeTTS();
                else {
                    Intent install = new Intent();
                    install.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                    startActivity(install);
                }
            }
        } catch (NullPointerException e) {
            new ErrorReporting(e, this.getClass().getName());
        } catch (Exception e) {
            new ErrorReporting(e, this.getClass().getName());
        }
    }

    public int getFFTWindowLength() {
        return mAudioAnalyzerView.getFFTWindowLength();
    }

    /**
     * executeTTS() : génère les données audio à partir de la phrase (text) de la synthèse vocale (TTS : Text To Speech).
     */
    private void executeTTS() {
        if (PermissionsModel.hasPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            HashMap<String, Object> args = new HashMap();

            String ttsText = getIntent().getStringExtra("tts-text");
            args.put("text", ttsText);
            mAudioAnalyzerView.onMessage("generate-tts-data", args);
        } else {
            PermissionsModel.requestPermission(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    R.string.storage_permission_msg);
        }
    }

    private void enableViews(boolean enable) {
        HashMap<String, Object> args = new HashMap();
        args.put("enable", enable);
        mAudioAnalyzerView.onMessage("enable-views", args);
    }
}
