package com.epmi_edu.terreplurielle.MVC.Models;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import com.epmi_edu.terreplurielle.MVC.Controllers.Fragments.RecordFragment;
import com.epmi_edu.terreplurielle.Utils.ErrorReporting;
import com.epmi_edu.terreplurielle.Utils.Functions;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

public class AudioRecorderModel extends BasicModel {
    final private float MAX_RECORDING_TIME = 60f;//maximum recording time allowed, in seconds.
    private boolean mIsRecording;
    private short[] mRecordedSamples;
    private int mSampleRate;
    private String mAudioFilePath;
    private RecordFragment mChartFragment;

    public AudioRecorderModel(RecordFragment chartFragment, int sampleRate, String filePath) {
        super(chartFragment.activity());
        mChartFragment = chartFragment;

        mSampleRate = sampleRate;
        mAudioFilePath = filePath;
    }

    public boolean recording() {
        return mIsRecording;
    }

    public void startRecording() {
        if (mIsRecording) return;
        mIsRecording = true;

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);

                    int bufferSize = AudioRecord.getMinBufferSize(mSampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);

                    if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE)
                        bufferSize = mSampleRate * 2;

                    AudioRecord record = new AudioRecord(MediaRecorder.AudioSource.MIC, mSampleRate,
                            AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);

                    if (record.getState() != AudioRecord.STATE_INITIALIZED) return;

                    record.startRecording();

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    BufferedOutputStream outputStream = new BufferedOutputStream(baos);
                    byte[] audioBuffer = new byte[bufferSize];

                    File file = new File(mAudioFilePath);
                    FileOutputStream fos = new FileOutputStream(file);

                    long recordedLength = 0;
                    boolean maxTimeReached = false;
                    while (mIsRecording) {
                        record.read(audioBuffer, 0, audioBuffer.length);
                        try {
                            outputStream.write(audioBuffer, 0, audioBuffer.length);
                            fos.write(audioBuffer);

                            recordedLength += audioBuffer.length;
                            float recordingTime = recordedLength / (mSampleRate * 2f);//in seconds,
                            // 2f : since it's a 16 bits (2 bytes) signal, audio length is twice the audioBuffer.length

                            if (recordingTime > MAX_RECORDING_TIME) {
                                mIsRecording = false;
                                maxTimeReached = true;
                                break;
                            }
                        } catch (IOException e) {
                            new ErrorReporting(e, this.getClass().getName());
                        }
                    }

                    try {
                        outputStream.close();

                        fos.flush();
                        fos.close();

                        mRecordedSamples = Functions.byteToShort(baos.toByteArray());//données audio à lire

                        record.stop();
                        record.release();

                        mChartFragment.audioRecordStopped(maxTimeReached);
                    } catch (IOException e) {
                        new ErrorReporting(e, this.getClass().getName());
                    }
                } catch (IllegalArgumentException e) {
                    new ErrorReporting(e, this.getClass().getName());
                } catch (SecurityException e) {
                    new ErrorReporting(e, this.getClass().getName());
                } catch (IllegalStateException e) {
                    new ErrorReporting(e, this.getClass().getName());
                } catch (FileNotFoundException e) {
                    new ErrorReporting(e, this.getClass().getName());
                }
            }
        }).start();
    }

    public void stop() {
        mIsRecording = false;
    }

    public short[] getSamples() {
        return mRecordedSamples;
    }

    @Override
    public void onMessage(String message, HashMap<String, Object> args) {

    }
}
