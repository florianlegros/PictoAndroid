package com.epmi_edu.terreplurielle.MVC.Models;

/*
    class TTSModel
*/

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.AudioManager;
import android.media.MediaFormat;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import com.epmi_edu.terreplurielle.AudioAnalyzerLib;
import com.epmi_edu.terreplurielle.MVC.Controllers.Fragments.BasicFragment;
import com.epmi_edu.terreplurielle.Utils.ErrorReporting;
import com.epmi_edu.terreplurielle.Utils.Functions;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class TTSModel extends BasicModel {
    private TextToSpeech mTTS;
    private String mAudioFilePath, mMode = "";
    private String mUtteranceId;
    private String mTextToSpeak = "";
    private String mVoicePackage = "";
    private int mSampleRate = -1;
    private short[] mAudioSamples = null;
    private BasicFragment mFragment;

    public TTSModel(BasicFragment fragment, String audioPath) {
        super(fragment.activity());

        mFragment = fragment;
        mAudioFilePath = audioPath;
        mUtteranceId = AudioAnalyzerLib.context.getPackageName();
    }

    public static String[][] getInstalledVoices() {
        final Intent ttsIntent = new Intent();
        ttsIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);

        PackageManager pm = AudioAnalyzerLib.context.getPackageManager();
        List<ResolveInfo> list = pm.queryIntentActivities(ttsIntent, PackageManager.GET_META_DATA);

        int voiceLen = list.size();
        String[][] voices = new String[2][voiceLen];
        for (int i = 0; i < voiceLen; i++) {
            ResolveInfo info = list.get(i);
            if (info != null) {
                voices[0][i] = info.loadLabel(pm).toString();
                voices[1][i] = info.activityInfo.applicationInfo.packageName;
            }
        }

        return voices;
    }

    @Override
    public void onMessage(String message, HashMap<String, Object> args) {
        switch (message) {
            case "save-to-file":
                saveToFile((String) args.get("text"));
                break;
            case "speak":
                doSpeak((String) args.get("text"));
        }
    }

    public void speak(String textToSpeak) {
        mMode = "speak";
        execute(textToSpeak, mMode);
    }

    public void generateAudioData(final String textToSpeak, boolean resetData) {
        if (resetData) {
            mTextToSpeak = "";
            mSampleRate = -1;
        }

        execute(textToSpeak, "save-to-file");
    }

    private void execute(final String textToSpeak, final String message) {
        try {
            String storedVoice = PreferencesModel.getPreferenceString("voice");
            if (storedVoice == null || storedVoice.isEmpty()) {
                mVoicePackage = "com.google.android.tts";
                PreferencesModel.addPreferenceString("voice", mVoicePackage);
            } else {
                if (!mVoicePackage.isEmpty() && !mVoicePackage.equals(storedVoice)) releaseTTS();

                mVoicePackage = storedVoice;
            }

            if (mTTS == null) {
                mTTS = new TextToSpeech(AudioAnalyzerLib.context, new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int result) {
                        if (result == TextToSpeech.SUCCESS) mTTS.setLanguage(Locale.FRENCH);

                        mTTS.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                            @Override
                            public void onBeginSynthesis(String utteranceId, int sampleRateInHz, int audioFormat, int channelCount) {
                                super.onBeginSynthesis(utteranceId, sampleRateInHz, audioFormat, channelCount);
                            }

                            @Override
                            public void onStart(String utteranceId) {
                            }

                            @Override
                            public void onDone(String utteranceId) {
                                switch (mMode) {
                                    case "speak":
                                        mFragment.onMessage("tts-speak-complete", null);
                                        break;

                                    case "data":
                                        mFragment.onMessage("tts-data-complete", getAudioData());
                                }

                                mMode = "";
                            }

                            @Override
                            public void onError(String utteranceId) {
                                this.onError(utteranceId, TextToSpeech.ERROR);
                            }
                        });

                        HashMap<String, Object> params = new HashMap<>();
                        params.put("text", textToSpeak);
                        onMessage(message, params);
                    }
                }, mVoicePackage);
            } else {
                HashMap<String, Object> params = new HashMap<>();
                params.put("text", textToSpeak);
                onMessage(message, params);
            }
        } catch (Exception e) {
            new ErrorReporting(e, TTSModel.class.getName());
        }
    }

    private void doSpeak(String textToSpeak) {
        Bundle params = new Bundle();
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, mUtteranceId);
        AudioManager audioManager = (AudioManager) AudioAnalyzerLib.context.getSystemService(AudioAnalyzerLib.context.AUDIO_SERVICE);
        params.putString(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(audioManager.STREAM_MUSIC));

        if (android.os.Build.VERSION.SDK_INT >= 11)
            params.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, audioManager.getStreamMaxVolume(audioManager.STREAM_MUSIC));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            mTTS.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, params, mUtteranceId);
        else mTTS.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null);
    }

    public HashMap<String, Object> getAudioData() {
        HashMap<String, Object> audioData = null;
        if (mAudioSamples == null)
            mAudioSamples = Functions.byteToShort(Functions.extractBytes(mAudioFilePath));

        if (mAudioSamples != null) {
            if (mSampleRate == -1) {
                MediaFormat mf = Functions.audioFileParamsGetter(mAudioFilePath);
                //mChannelCount = mf.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
                mSampleRate = mf.getInteger(MediaFormat.KEY_SAMPLE_RATE);
            }

            audioData = new HashMap<>();
            audioData.put("sampleRate", mSampleRate);
            audioData.put("samples", mAudioSamples);
        }

        return audioData;
    }

    private void saveToFile(final String textToSpeak) {
        if (textToSpeak.isEmpty()) return;

        if (mTextToSpeak.equals(textToSpeak)) return;

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (mMode.isEmpty()) {
                    try {
                        mMode = "data";

                        mTextToSpeak = textToSpeak;
                        int textLength = mTextToSpeak.length(), maxLength = mTTS.getMaxSpeechInputLength();
                        mTextToSpeak = mTextToSpeak.substring(0, Math.min(textLength, maxLength));

                        mTTS.synthesizeToFile(mTextToSpeak, null, new File(mAudioFilePath), mUtteranceId);
                    } catch (Exception e) {
                        new ErrorReporting(e, TTSModel.class.getName());
                    }
                } else saveToFile(textToSpeak);
            }
        }).start();
    }

    public void release() {
        releaseTTS();
    }

    private void releaseTTS() {
        if (mTTS != null) {
            mTTS.stop();
            mTTS.shutdown();
        }

        mTTS = null;
    }
}
