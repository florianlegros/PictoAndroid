package com.epmi_edu.terreplurielle.MVC.Controllers.Fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;

import com.epmi_edu.terreplurielle.MVC.Models.AudioRecorderModel;
import com.epmi_edu.terreplurielle.MVC.Models.PermissionsModel;
import com.epmi_edu.terreplurielle.Utils.ErrorReporting;
import com.epmi_edu.terreplurielle.Utils.Functions;
import com.epmi_edu.terreplurielle.audioanalyzer.R;

import java.io.File;

public class RecordFragment extends ChartFragment {
    private AudioRecorderModel mAudioRecorder;

    public RecordFragment() {
        // Required empty public constructor
        mAudioFilePath = Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/com.epmi_edu_" + this.getClass().getSimpleName();
    }

    public static RecordFragment newInstance() {
        return new RecordFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            File file = new File(mAudioFilePath);
            if (file.exists()) file.delete();
        }
    }

    public void startRecording(Runnable action, int fragmentIndex) {
        if (PermissionsModel.hasPermission(activity(), Manifest.permission.RECORD_AUDIO))
            action.run();
        else {
            Activity activity = activity();
            Intent intent = activity.getIntent();
            intent.putExtra("fragment-index", fragmentIndex);
            PermissionsModel.requestPermission(activity, new String[]{Manifest.permission.RECORD_AUDIO},
                    R.string.audio_permission_msg);
        }
    }

    public void startAudioRecord(int fragmentIndex) {
        try {
            if (mAudioRecorder == null)
                mAudioRecorder = new AudioRecorderModel(this, mSampleRate, mAudioFilePath);

            if (mAudioRecorder.recording()) return;

            startRecording(new Runnable() {
                @Override
                public void run() {
                    mAudioRecorder.startRecording();
                }
            }, fragmentIndex);
        } catch (Exception e) {
            new ErrorReporting(e, this.getClass().getName());
        }
    }

    public void audioRecordStopped(boolean maxTimeReached) {
        if (maxTimeReached) {
            activity().onMessage("audio-record-max-time-reached", null);
            mSpectrogramData = null;
            mAudioSamples = null;
        }

        checkSampleData();
        drawIfVisible(-1);
    }

    public void stopAudioRecord() {
        if (mAudioRecorder != null && mAudioRecorder.recording()) {
            mAudioRecorder.stop();

            mSpectrogramData = null;
            mAudioSamples = null;

            enableViews(true);
        }
    }

    public void stopMedias() {
        if (mAudioRecorder != null) mAudioRecorder.stop();
        super.stopMedias();
    }

    protected void checkSampleData() {
        if (mAudioSamples == null && mAudioRecorder != null) {
            mAudioSamples = mAudioRecorder.getSamples();
            mGraphicalData = Functions.convertToLong(mAudioSamples);
        }
    }
}