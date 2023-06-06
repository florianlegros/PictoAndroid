package com.epmi_edu.terreplurielle;
/*
    Yacine BOURADA - 06 / 26 / 2018
    Class AudioAnalyzerLib : generates audio data from the "Text to Speech" android engine or/and from the mike recorded sound
                        and renders the data in a graphical representation.
*/

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.epmi_edu.terreplurielle.MVC.Models.TTSModel;

import java.io.Serializable;
import java.util.List;

public class AudioAnalyzerLib {
    public static Context context = null;
    public static float screenDensity = -1;

    public static void startActivity(String ttsText, List pictograms, Context context,
                                     float screenDensity, Activity srcActivity, Class destClass) {
        if (AudioAnalyzerLib.context == null) AudioAnalyzerLib.context = context;
        if (AudioAnalyzerLib.screenDensity == -1) AudioAnalyzerLib.screenDensity = screenDensity;

        Intent intent = new Intent(srcActivity, destClass);
        intent.putExtra("tts-text", ttsText);

        Bundle bundle = new Bundle();
        bundle.putSerializable("tts-pictograms", (Serializable) pictograms);

        intent.putExtras(bundle);
        srcActivity.startActivity(intent);
    }

    public static String[][] getInstalledVoices(Context context) {
        AudioAnalyzerLib.context = context;
        return TTSModel.getInstalledVoices();
    }
}
