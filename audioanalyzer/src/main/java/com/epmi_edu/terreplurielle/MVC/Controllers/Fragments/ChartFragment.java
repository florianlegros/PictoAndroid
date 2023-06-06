/*
Yacine BOURADA - 06 / 22 / 2018
Class ChartFragment.
 */

package com.epmi_edu.terreplurielle.MVC.Controllers.Fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;

import com.epmi_edu.terreplurielle.MVC.Controllers.Activities.AudioAnalyzerActivity;
import com.epmi_edu.terreplurielle.MVC.Controllers.Activities.BasicActivity;
import com.epmi_edu.terreplurielle.MVC.Controllers.Adapters.BasicViewPagerAdapter;
import com.epmi_edu.terreplurielle.MVC.Models.AudioPlayerModel;
import com.epmi_edu.terreplurielle.MVC.Views.SpectrogramView;
import com.epmi_edu.terreplurielle.Utils.ErrorReporting;
import com.epmi_edu.terreplurielle.Utils.FFT;
import com.epmi_edu.terreplurielle.Utils.Functions;
import com.epmi_edu.terreplurielle.audioanalyzer.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public abstract class ChartFragment extends BasicViewPagerAdapter.BasicViewPagerFragment {
    protected int mSampleRate = 0, mFFTWindowLen = 0;
    protected ArrayList mSpectrogramData;
    protected long[] mGraphicalData = null, mFFTData = null;
    protected short[] mAudioSamples = null;
    protected String mAudioFilePath;
    boolean mConfigChanged = false;
    private AudioPlayerModel mAudioPlayer;
    private SpectrogramView mSpectrogramView;
    private RelativeLayout mLayoutWaitMsg;

    public View createViews(LayoutInflater inflater, ViewGroup container, int fragmentResId) {
        View view = null;
        try {
            view = inflater.inflate(fragmentResId, container, false);
            mSpectrogramView = (SpectrogramView) view.findViewById(R.id.chart_view);
            mLayoutWaitMsg = (RelativeLayout) view.findViewById(R.id.layout_wait_msg);
        } catch (Exception e) {
            new ErrorReporting(e, e.getMessage());
        }

        HashMap<String, Object> params = new HashMap<>();
        params.put("fragment", this);
        activity().onMessage("check-fragment", params);

        return view;
    }

    protected void enableViews(boolean enable) {
        HashMap<String, Object> args = new HashMap<>();
        args.put("enable", enable);
        activity().onMessage("enable-views", args);
    }

    public void setWindowLen(int windowLen) {
        mFFTWindowLen = windowLen;
    }

    private void plotData(final Runnable endPlotCB) {
        Activity activity = getActivity();
        if (activity == null) return;

        Functions.HandleUIFromAnotherThread(activity, new Runnable() {
            @Override
            public void run() {
                try {
                    if (mSpectrogramData != null && mSpectrogramView != null)
                        mSpectrogramView.draw(mGraphicalData, mFFTData, mSpectrogramData, mSampleRate);

                    if (mLayoutWaitMsg != null) mLayoutWaitMsg.setVisibility(View.GONE);

                    enableViews(true);

                    if (endPlotCB != null) endPlotCB.run();
                } catch (Exception e) {
                    new ErrorReporting(e, this.getClass().getName());
                }
            }
        });
    }

    protected void drawSpectrums(final Runnable endPlotCB) {
        final BasicActivity activity = activity();
        if (activity == null || mGraphicalData == null) return;

        mConfigChanged = false;
        Functions.HandleUIFromAnotherThread(getActivity(), new Runnable() {
            @Override
            public void run() {
                try {
                    if (mLayoutWaitMsg != null) mLayoutWaitMsg.setVisibility(View.VISIBLE);
                } catch (Exception e) {
                    new ErrorReporting(e, this.getClass().getName());
                }
            }
        });

        if (mSpectrogramData == null || mFFTData == null) {
            new Thread(() -> {
                checkSampleData();

                if (mGraphicalData == null) return;

                if (mSpectrogramData == null) {
                    updateMaxFFTWindowPickerNumber();

                    if (mFFTWindowLen == 0)
                        mFFTWindowLen = ((AudioAnalyzerActivity) activity).getFFTWindowLength();

                    mSpectrogramData = FFT.short_fft(Arrays.copyOf(mGraphicalData, mGraphicalData.length), mFFTWindowLen);
                }

                if (mFFTData == null) mFFTData = FFT.fft(mGraphicalData);

                plotData(endPlotCB);
            }).start();
        } else plotData(endPlotCB);
    }

    protected void drawIfVisible(int selection) {
        if (selection == -1) selection = mViewPagerAdapter.visibleIndex();
        if (mViewPagerAdapter != null && this == mViewPagerAdapter.getItem(selection))
            drawSpectrums(null);
    }

    public void plot(int windowLength) {
        mFFTWindowLen = windowLength;
        mSpectrogramData = null;

        drawIfVisible(-1);
    }

    public void playSamples() {
        try {
            extractData();

            if (mAudioSamples == null) {
                audioPlayerComplete();
                return;
            }

            Runnable audioPlayAction = new Runnable() {
                @Override
                public void run() {
                    if (mAudioPlayer == null) {
                        mAudioPlayer = new AudioPlayerModel(activity(), mAudioSamples, new Functions.ProgressListener() {
                            @Override
                            public void start() {
                                activity().onMessage("transcript-voice", null);
                                mSpectrogramView.setMarkerPosition(0);
                            }

                            @Override
                            public void progress(int progress) {
                                mSpectrogramView.setMarkerPosition(progress);
                            }

                            @Override
                            public void complete() {
                                audioPlayerComplete();
                            }
                        }, 0, mSampleRate);
                    } else mAudioPlayer.setParams(mAudioSamples, mSampleRate);

                    if (!mAudioPlayer.playing()) mAudioPlayer.play();
                }
            };

            /*if(mSpectrogramData == null)    drawSpectrums(audioPlayAction);
            else                            */
            audioPlayAction.run();
        } catch (Exception e) {
            new ErrorReporting(e, this.getClass().getName());
        }
    }

    @Override
    public void loadUI(Object data) {
        extractData();
        if (mConfigChanged || mSpectrogramData == null) drawSpectrums(null);
        else {
            mSpectrogramView.clearMarker();
            updateMaxFFTWindowPickerNumber();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (outState != null) {
            outState.putInt("sampleRate", mSampleRate);
            outState.putInt("fftWindowLength", mFFTWindowLen);
            outState.putInt("viewPagerIndex", mViewPagerAdapter.visibleIndex());
            outState.putBoolean("draw", mSpectrogramData != null);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null && mSpectrogramData == null) {
            mConfigChanged = true;

            mSampleRate = savedInstanceState.getInt("sampleRate");
            extractData();

            mFFTWindowLen = savedInstanceState.getInt("fftWindowLength");
            if (mFFTWindowLen != 0) {
                HashMap<String, Object> args = new HashMap();
                args.put("fftWindowMaxIndex", (int) (Math.log(mFFTWindowLen) / Math.log(2)));
                activity().onMessage("update-number-picker-index", args);

                if (savedInstanceState.getBoolean("draw"))
                    drawIfVisible(savedInstanceState.getInt("viewPagerIndex"));
            }
        }
    }

    private void updateMaxFFTWindowPickerNumber() {
        int powOf2Length = (int) (Math.pow(2, Math.ceil(Math.log(mGraphicalData.length) / Math.log(2))) / 2f);
        if (powOf2Length < mFFTWindowLen) mFFTWindowLen = powOf2Length;

        HashMap<String, Object> args = new HashMap();
        int fftWindowMaxIndex = (int) (Math.log(powOf2Length) / Math.log(2));
        args.put("fftWindowMaxIndex", fftWindowMaxIndex);
        args.put("fftWindowMaxLength", powOf2Length);
        activity().onMessage("update-number-picker-max-index", args);
    }

    public void stopPlay() {
        if (mAudioPlayer != null && mAudioPlayer.playing()) mAudioPlayer.stop();
    }

    public void stopMedias() {
        if (mAudioPlayer != null && mAudioPlayer.playing()) mAudioPlayer.stop();
    }

    public void release() {
        stopMedias();

        if (mAudioFilePath != null && !mAudioFilePath.isEmpty()) {
            File file = new File(mAudioFilePath);
            if (file.exists()) file.delete();
        }
    }

    public void setSampleRate(int sampleRate) {
        mSampleRate = sampleRate;
    }

    protected void checkSampleData() {
    }

    @Override
    public void onMessage(String message, HashMap<String, Object> args) {

    }

    private void extractData() {
        if (mAudioSamples != null) return;

        byte[] data = Functions.extractBytes(mAudioFilePath);
        if (data != null) {
            mAudioSamples = Functions.byteToShort(data);
            mGraphicalData = Functions.convertToLong(mAudioSamples);
        }
    }

    private void audioPlayerComplete() {
        HashMap<String, Object> args = new HashMap();
        args.put("fragment", this.getClass().getSimpleName());
        activity().onMessage("audio-player-complete", args);
    }
}
