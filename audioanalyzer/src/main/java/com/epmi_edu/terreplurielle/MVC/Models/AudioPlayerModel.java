package com.epmi_edu.terreplurielle.MVC.Models;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import com.epmi_edu.terreplurielle.MVC.Controllers.Activities.BasicActivity;
import com.epmi_edu.terreplurielle.Utils.Functions;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ShortBuffer;
import java.util.HashMap;

public class AudioPlayerModel extends BasicModel {
    private boolean mIsPlaying = false;
    private ShortBuffer mSamples;
    private int mNumSamples;
    private Functions.ProgressListener mListener;
    private int mAudioFormat;
    private String mAudioFile;
    private int mSampleRate;

    public AudioPlayerModel(BasicActivity controller, short[] samples, Functions.ProgressListener listener,
                            int audioFormat, int sampleRate) {
        super(controller);

        init(samples, listener, audioFormat, sampleRate);
    }

    private void init(short[] samples, Functions.ProgressListener listener, int audioFormat, int sampleRate) {
        mSamples = ShortBuffer.wrap(samples);
        mNumSamples = samples.length;
        mListener = listener;

        mAudioFormat = audioFormat == 0 ? AudioFormat.ENCODING_PCM_16BIT : audioFormat;
        mSampleRate = sampleRate;
    }

    public void setParams(short[] samples, int sampleRate) {
        mSamples = ShortBuffer.wrap(samples);
        mSampleRate = sampleRate;
        mNumSamples = samples.length;
    }

    public void setFile(String audioFile) {
        mAudioFile = audioFile;

        File file = new File(mAudioFile);
        mNumSamples = file.exists() ? (int) file.length() : 0;
    }

    public boolean playing() {
        return mIsPlaying;
    }

    public void play() {
        if (mIsPlaying) return;

        mIsPlaying = true;

        new Thread(new Runnable() {
            @Override
            public void run() {
                startPlayback();
            }
        }).start();
    }

    public void stop() {
        mIsPlaying = false;
    }

    private void startPlayback() {
        int bufferSize = AudioTrack.getMinBufferSize(mSampleRate, AudioFormat.CHANNEL_OUT_MONO, mAudioFormat);

        if (bufferSize == AudioTrack.ERROR || bufferSize == AudioTrack.ERROR_BAD_VALUE)
            bufferSize = mSampleRate * 2;

        AudioTrack audioTrack = new AudioTrack(
                AudioManager.STREAM_MUSIC,
                mSampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                mAudioFormat,
                bufferSize,
                AudioTrack.MODE_STREAM);

        audioTrack.setPlaybackPositionUpdateListener(new AudioTrack.OnPlaybackPositionUpdateListener() {
            @Override
            public void onPeriodicNotification(AudioTrack track) {
                if (mListener != null && track.getPlayState() == AudioTrack.PLAYSTATE_PLAYING)
                    mListener.progress((track.getPlaybackHeadPosition() * 1000) / mSampleRate);
            }

            @Override
            public void onMarkerReached(AudioTrack track) {
                int progress = (track.getPlaybackHeadPosition() * 1000) / mSampleRate;
                track.stop();
                track.release();
                if (mListener != null) {
                    mListener.progress(progress);
                    mListener.complete();
                }
            }
        });

        audioTrack.setPositionNotificationPeriod(mSampleRate / 20); // 20 times per second

        if (mSamples == null) {
            audioTrack.setNotificationMarkerPosition(mNumSamples);

            byte[] s = new byte[bufferSize];
            try {
                FileInputStream fin = new FileInputStream(mAudioFile);
                DataInputStream dis = new DataInputStream(fin);

                audioTrack.play();

                int i = 0;
                while ((i = dis.read(s, 0, bufferSize)) > -1 && mIsPlaying)
                    audioTrack.write(s, 0, i);

                audioTrack.stop();
                audioTrack.release();
                dis.close();
                fin.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            audioTrack.setNotificationMarkerPosition(mNumSamples);

            audioTrack.play();

            mSamples.rewind();

            if (mListener != null) mListener.start();

            int limit = mNumSamples;
            short[] buffer = new short[bufferSize];
            while (mSamples.position() < limit && mIsPlaying) {
                int numSamplesLeft = limit - mSamples.position(), samplesToWrite;

                if (numSamplesLeft >= buffer.length) {
                    mSamples.get(buffer);
                    samplesToWrite = buffer.length;
                } else {
                    for (int i = numSamplesLeft; i < buffer.length; i++) buffer[i] = 0;

                    mSamples.get(buffer, 0, numSamplesLeft);
                    samplesToWrite = numSamplesLeft;
                }

                audioTrack.write(buffer, 0, samplesToWrite);
            }
        }

        if (!mIsPlaying) {
            audioTrack.stop();
            audioTrack.release();
        }

        mIsPlaying = false;
    }

    @Override
    public void onMessage(String message, HashMap<String, Object> args) {

    }
}