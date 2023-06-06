package com.epmi_edu.terreplurielle.MVC.Controllers.Fragments;

import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.epmi_edu.terreplurielle.MVC.Models.TTSModel;
import com.epmi_edu.terreplurielle.Utils.ErrorReporting;
import com.epmi_edu.terreplurielle.Utils.Functions;
import com.epmi_edu.terreplurielle.audioanalyzer.R;

import java.util.HashMap;

public class TTSFragment extends ChartFragment {
    private TTSModel mTTS = null;

    public TTSFragment() {
        // Required empty public constructor
        mAudioFilePath = Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/com.epmi_edu_" + this.getClass().getSimpleName() + ".wav";
    }

    public static TTSFragment newInstance() {
        return new TTSFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return createViews(inflater, container, R.layout.fragment_tts);
    }

    @Override
    public void onMessage(String message, HashMap<String, Object> args) {
        try {
            switch (message) {
                case "tts-speak-complete":
                    enableViews(true);
                    break;

                case "tts-data-complete":
                    mAudioSamples = (short[]) args.get("samples");
                    mGraphicalData = Functions.convertToLong(mAudioSamples);

                    activity().onMessage("set-sample-rate", args);

                    enableViews(true);

                    //drawSpectrums(null);
            }
        } catch (Exception e) {
            new ErrorReporting(e, this.getClass().getName());
        }
    }

    public void release() {
        if (mTTS != null) mTTS.release();
        mTTS = null;

        super.release();
    }

    public void ttsSpeak(String text) {
        if (mTTS == null) mTTS = new TTSModel(this, "");

        mTTS.speak(text);
    }

    public void generateAudioData(String text) {
        if (mTTS == null) mTTS = new TTSModel(this, mAudioFilePath);
        mTTS.generateAudioData(text, true);
    }
}
