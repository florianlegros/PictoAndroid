package com.epmi_edu.terreplurielle.MVC.Views;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.epmi_edu.terreplurielle.MVC.Controllers.Activities.BasicActivity;
import com.epmi_edu.terreplurielle.MVC.Controllers.Adapters.ActionsRecyclerViewAdapter;
import com.epmi_edu.terreplurielle.MVC.Controllers.Adapters.ChartViewPagerAdapter;
import com.epmi_edu.terreplurielle.MVC.Controllers.Adapters.PictogramsRecyclerViewAdapter;
import com.epmi_edu.terreplurielle.MVC.Controllers.Fragments.KidFragment;
import com.epmi_edu.terreplurielle.MVC.Controllers.Fragments.TTSFragment;
import com.epmi_edu.terreplurielle.MVC.Controllers.Fragments.TeacherFragment;
import com.epmi_edu.terreplurielle.Utils.ErrorReporting;
import com.epmi_edu.terreplurielle.Utils.Functions;
import com.epmi_edu.terreplurielle.audioanalyzer.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AudioAnalyzerView extends BasicView {
    private NumberPicker mFFTWindowLenNumberPicker;//Sort FFT window Length picker.
    private PictogramsRecyclerViewAdapter mPictogramsRecyclerViewAdapter;
    private ActionsRecyclerViewAdapter mActionsRecyclerViewAdapter;
    private ChartViewPagerAdapter chartViewPagerAdapter;
    private SeekBar mVolumeSeekBar;

    public AudioAnalyzerView(BasicActivity controller, int volume, int maxVolume, Bundle savedInstanceState) {
        super(controller, R.layout.activity_audio_analyzer);
        try {
            Intent intent = mController.getIntent();
            String phrase = intent.getStringExtra("tts-text");

            ((TextView) mController.findViewById(R.id.text_view_phrase)).setText(phrase);

            List iconIds = new ArrayList();
            iconIds.add(R.drawable.ic_play_tts);
            iconIds.add(R.drawable.ic_record_teacher);
            iconIds.add(R.drawable.ic_play_teacher);
            iconIds.add(R.drawable.ic_record_kid);
            iconIds.add(R.drawable.ic_play_kid);
            iconIds.add(R.drawable.ic_plot);
            iconIds.add(R.drawable.ic_volume);

            mActionsRecyclerViewAdapter =
                    new ActionsRecyclerViewAdapter((RecyclerView) mController.findViewById(R.id.recycler_view_actions),
                            LinearLayoutManager.HORIZONTAL, "linear", 0, iconIds);

            Bundle bundle = intent.getExtras();
            List pictograms = bundle.getParcelableArrayList("tts-pictograms");

            mPictogramsRecyclerViewAdapter = new PictogramsRecyclerViewAdapter((RecyclerView) mController.findViewById(R.id.recycler_view_pictograms),
                    LinearLayoutManager.HORIZONTAL, "linear", 0, pictograms);

            mFFTWindowLenNumberPicker = (NumberPicker) mController.findViewById(R.id.number_picker_fft_window_len);

            int minPower = 1, maxPower = (int) Math.ceil(Math.log(2048) / Math.log(2));

            mFFTWindowLenNumberPicker.setMaxValue(maxPower);
            mFFTWindowLenNumberPicker.setMinValue(minPower);
            mFFTWindowLenNumberPicker.setValue(7);

            mFFTWindowLenNumberPicker.setFormatter(new NumberPicker.Formatter() {
                @Override
                public String format(int value) {
                    return String.valueOf((int) Math.pow(2, value));
                }
            });

            Paint paint = new Paint();
            int w = Functions.toScreenValue((int) (paint.measureText("2048") * 1.5f));
            LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(w, LinearLayout.LayoutParams.MATCH_PARENT);
            mFFTWindowLenNumberPicker.setLayoutParams(layout);

            mFFTWindowLenNumberPicker.setWrapSelectorWheel(true);
            mFFTWindowLenNumberPicker.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);

            /** The code bellow is correction of an android's bug (the number picker currrent value is not displayed) **/
            mFFTWindowLenNumberPicker.invalidate();
            View firstItem = mFFTWindowLenNumberPicker.getChildAt(0);
            if (firstItem != null) firstItem.setVisibility(View.INVISIBLE);

            int windowLen = (int) Math.pow(2, mFFTWindowLenNumberPicker.getValue());
            chartViewPagerAdapter = new ChartViewPagerAdapter(mController.getSupportFragmentManager(), mController,
                    R.id.viewpager_chart, R.id.viewpager_tabs,
                    new Class[]{TTSFragment.class, TeacherFragment.class, KidFragment.class}, windowLen);

            mVolumeSeekBar = (SeekBar) mController.findViewById(R.id.seek_bar_volume);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) mVolumeSeekBar.setMin(0);

            mVolumeSeekBar.setMax(maxVolume);
            mVolumeSeekBar.setProgress(volume);

            mVolumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    HashMap<String, Object> args = new HashMap<>();
                    args.put("value", progress);
                    mController.onMessage("volume-changed", args);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

            if (savedInstanceState != null && savedInstanceState.getInt("volumeControlVisibilityState") == VISIBLE)
                mVolumeSeekBar.setVisibility(VISIBLE);
        } catch (Exception e) {
            new ErrorReporting(e, this.getClass().getName());
        }
    }

    public int getVolumeControlVisibilityState() {
        return mVolumeSeekBar.getVisibility();
    }

    /**
     * onMessage receives messages sent from the Presenter using the MVP model
     *
     * @param message : string identifying the view's method to invoke
     * @param args    : arguments used by the the invoked view's method (null if the method requires no arguments)
     */
    @Override
    public void onMessage(String message, HashMap<String, Object> args) {
        switch (message) {
            case "on-header-button":
                int id = (int) args.get("iconId");
                if (id == R.drawable.ic_volume)
                    mVolumeSeekBar.setVisibility(mVolumeSeekBar.getVisibility() == VISIBLE ? GONE : VISIBLE);
                else {
                    enableViews(false);
                    if (id == R.drawable.ic_play_tts)
                        chartViewPagerAdapter.onMessage("play-tts", null);
                    if (id == R.drawable.ic_stop_play_tts)
                        chartViewPagerAdapter.onMessage("stop-play-tts", null);
                    else if (id == R.drawable.ic_record_teacher)
                        chartViewPagerAdapter.onMessage("record-teacher", null);
                    else if (id == R.drawable.ic_stop_record_teacher)
                        chartViewPagerAdapter.onMessage("stop-record-teacher", null);
                    else if (id == R.drawable.ic_play_teacher)
                        chartViewPagerAdapter.onMessage("play-teacher", null);
                    else if (id == R.drawable.ic_stop_play_teacher)
                        chartViewPagerAdapter.onMessage("stop-play-teacher", null);
                    else if (id == R.drawable.ic_play_kid)
                        chartViewPagerAdapter.onMessage("play-kid", null);
                    else if (id == R.drawable.ic_stop_play_kid)
                        chartViewPagerAdapter.onMessage("stop-play-kid", null);
                    else if (id == R.drawable.ic_record_kid)
                        chartViewPagerAdapter.onMessage("record-kid", null);
                    else if (id == R.drawable.ic_stop_record_kid)
                        chartViewPagerAdapter.onMessage("stop-record-kid", null);
                    else if (id == R.drawable.ic_plot) {
                        HashMap<String, Object> args1 = new HashMap<>();
                        args1.put("fft_window_length", (int) Math.pow(2, mFFTWindowLenNumberPicker.getValue()));
                        chartViewPagerAdapter.onMessage("plot", args1);
                    }
                }

                break;

            case "tts-pictogram-speak":
                chartViewPagerAdapter.onMessage("tts-pictogram-speak", args);
                break;
            case "set-sample-rate":
                chartViewPagerAdapter.onMessage("set-sample-rate", args);
                break;

            case "update-number-picker-max-index"://called by ChartFragment if
                // the fragment's window length < pow(2, number picker's current value)

                int newMaxIndex = (int) args.get("fftWindowMaxIndex");
                if (mFFTWindowLenNumberPicker.getMaxValue() != newMaxIndex) {
                    Paint paint = new Paint();
                    int maxValue = (int) args.get("fftWindowMaxLength"),
                            w = Functions.toScreenValue((int) (paint.measureText(String.valueOf(maxValue)) * 1.5f));

                    LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(w, LinearLayout.LayoutParams.MATCH_PARENT);
                    mFFTWindowLenNumberPicker.setLayoutParams(layout);

                    mFFTWindowLenNumberPicker.setMaxValue(newMaxIndex);
                }
                break;

            case "update-number-picker-index":
                final int index = (int) args.get("fftWindowMaxIndex");
                if (mFFTWindowLenNumberPicker.getValue() != index)
                    mFFTWindowLenNumberPicker.setValue(index);
                break;

            case "start-audio-record":
                chartViewPagerAdapter.onMessage("start-audio-record", args);
                break;
            case "generate-tts-data":
                chartViewPagerAdapter.onMessage("generate-tts-data", args);
                break;
            case "check-fragment":
                chartViewPagerAdapter.onMessage("check-fragment", args);
                break;
            case "record-stopped":
                enableViews(true);
                break;

            case "enable-views":
                enableViews((Boolean) args.get("enable"));
                break;
            case "clear-chart-data":
                if (chartViewPagerAdapter != null)
                    chartViewPagerAdapter.onMessage("clear-chart-data", null);
                break;

            case "audio-player-complete":
                enableViews(true);
                mActionsRecyclerViewAdapter.onMessage("audio-player-complete", args);
                break;

            case "audio-record-max-time-reached":
                enableViews(true);
                mActionsRecyclerViewAdapter.onMessage("audio-record-max-time-reached", args);
                break;

            case "stop-medias":
                if (chartViewPagerAdapter != null)
                    chartViewPagerAdapter.onMessage("stop-medias", null);
        }
    }

    public int getFFTWindowLength() {
        return (int) Math.pow(2, mFFTWindowLenNumberPicker.getValue());
    }

    private void enableViews(boolean enable) {
        mFFTWindowLenNumberPicker.setEnabled(enable);
        mActionsRecyclerViewAdapter.enableButtons(enable);
        mPictogramsRecyclerViewAdapter.enableRecyclerView(enable);
        chartViewPagerAdapter.enableView(enable);
    }
}