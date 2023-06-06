package com.epmi_edu.terreplurielle.MVC.Controllers.Adapters;

import static com.epmi_edu.terreplurielle.MVC.Controllers.Activities.AudioAnalyzerActivity.KID_FRAG;
import static com.epmi_edu.terreplurielle.MVC.Controllers.Activities.AudioAnalyzerActivity.TEACHER_FRAG;
import static com.epmi_edu.terreplurielle.MVC.Controllers.Activities.AudioAnalyzerActivity.TTS_FRAG;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.FragmentManager;

import com.epmi_edu.terreplurielle.MVC.Controllers.Activities.BasicActivity;
import com.epmi_edu.terreplurielle.MVC.Controllers.Fragments.ChartFragment;
import com.epmi_edu.terreplurielle.MVC.Controllers.Fragments.RecordFragment;
import com.epmi_edu.terreplurielle.MVC.Controllers.Fragments.TTSFragment;
import com.epmi_edu.terreplurielle.Utils.ErrorReporting;
import com.epmi_edu.terreplurielle.audioanalyzer.R;

import java.util.HashMap;

public class ChartViewPagerAdapter extends BasicViewPagerAdapter {
    public ChartViewPagerAdapter(FragmentManager fm, final Activity activity, int viewPagerId, int tabResourseId,
                                 Class[] framentClassNames, int windowLen) {

        super(fm, activity, viewPagerId, tabResourseId, framentClassNames,
                new ITabCustomView() {
                    @Override
                    public View getView(int position, String label, int iconId) {
                        View view = LayoutInflater.from(activity)
                                .inflate(R.layout.chart_view_pager_custom_tab, null);

                        TextView tv = (TextView) view.findViewById(R.id.text_view_label);
                        tv.setText(label);

                        if (iconId > 0) {
                            ImageView img = (ImageView) view.findViewById(R.id.image_view_icon);
                            img.setImageResource(iconId);
                        }

                        return view;
                    }
                });

        int fragmentCount1 = getCount();
        for (int i = 0; i < fragmentCount1; i++)
            ((ChartFragment) mFragmentList.get(i)).setWindowLen(windowLen);
    }

    public void onMessage(String message, HashMap<String, Object> args) {
        try {
            switch (message) {
                case "tts-pictogram-speak":
                    ((TTSFragment) mFragmentList.get(TTS_FRAG)).ttsSpeak((String) args.get("text"));
                    break;

                case "play-tts":
                    ((ChartFragment) mFragmentList.get(TTS_FRAG)).playSamples();
                    break;
                case "play-teacher":
                    ((ChartFragment) mFragmentList.get(TEACHER_FRAG)).playSamples();
                    break;
                case "play-kid":
                    ((ChartFragment) mFragmentList.get(KID_FRAG)).playSamples();
                    break;

                case "stop-play-tts":
                    ((ChartFragment) mFragmentList.get(TTS_FRAG)).stopPlay();
                    break;
                case "stop-play-teacher":
                    ((ChartFragment) mFragmentList.get(TEACHER_FRAG)).stopPlay();
                    break;
                case "stop-play-kid":
                    ((ChartFragment) mFragmentList.get(KID_FRAG)).stopPlay();
                    break;

                case "start-audio-record"://permission RECORD_AUDIO validée, on peut commencer l'enregistrement
                    int fragmentIndex = (int) args.get("fragment-index");
                    ((RecordFragment) mFragmentList.get(fragmentIndex)).startAudioRecord(fragmentIndex);
                    break;

                case "record-teacher"://enregistrement une fois que la permission RECORD_AUDIO aura été validée
                    ((RecordFragment) mFragmentList.get(TEACHER_FRAG)).startAudioRecord(TEACHER_FRAG);
                    break;

                case "record-kid"://enregistrement une fois que la permission RECORD_AUDIO aura été validée
                    ((RecordFragment) mFragmentList.get(KID_FRAG)).startAudioRecord(KID_FRAG);
                    break;

                case "stop-record-teacher":
                    ((RecordFragment) mFragmentList.get(TEACHER_FRAG)).stopAudioRecord();
                    break;
                case "stop-record-kid":
                    ((RecordFragment) mFragmentList.get(KID_FRAG)).stopAudioRecord();
                    break;

                case "plot":
                    int windowLength = Integer.decode(args.get("fft_window_length").toString()), count = getCount();
                    for (int i = 0; i < count; i++)
                        ((ChartFragment) mFragmentList.get(i)).plot(windowLength);
                    break;

                case "generate-tts-data":
                    ((TTSFragment) mFragmentList.get(TTS_FRAG)).generateAudioData((String) args.get("text"));
                    break;

                case "set-sample-rate":
                    int sampleRate = Integer.decode(args.get("sampleRate").toString()), fragmentCount = getCount();
                    for (int i = 0; i < fragmentCount; i++)
                        ((ChartFragment) mFragmentList.get(i)).setSampleRate(sampleRate);

                    break;

                case "check-fragment":
                    checkFragment((ChartFragment) args.get("fragment"));
                    break;

                case "stop-medias":
                    int fragmentCount1 = getCount();
                    for (int i = 0; i < fragmentCount1; i++)
                        ((ChartFragment) mFragmentList.get(i)).stopMedias();
                    break;

                case "clear-chart-data":
                    int fragmentCount2 = getCount();
                    for (int i = 0; i < fragmentCount2; i++)
                        ((ChartFragment) mFragmentList.get(i)).release();
            }
        } catch (Exception e) {
            new ErrorReporting(e, this.getClass().getName());
        }
    }

    private int fragmentIndex(ChartFragment fragment) {
        int index = -1;
        switch (fragment.getClass().getSimpleName()) {
            case "TTSFragment":
                index = TTS_FRAG;
                break;
            case "TeacherFragment":
                index = TEACHER_FRAG;
                break;
            case "KidFragment":
                index = KID_FRAG;
        }

        return index;
    }

    public void checkFragment(ChartFragment fragment) {
        int index = fragmentIndex(fragment);
        if (index != -1 && mFragmentList.get(index) != fragment) {
            mFragmentList.set(index, fragment);
            fragment.setViewPager(mViewPager);
        }
    }
}