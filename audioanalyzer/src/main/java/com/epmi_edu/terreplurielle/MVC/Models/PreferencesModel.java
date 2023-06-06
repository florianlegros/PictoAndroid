package com.epmi_edu.terreplurielle.MVC.Models;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;

import com.epmi_edu.terreplurielle.AudioAnalyzerLib;
import com.epmi_edu.terreplurielle.MVC.Controllers.Activities.BasicActivity;
import com.epmi_edu.terreplurielle.Utils.Constants;

import java.util.HashMap;
import java.util.Map;

public class PreferencesModel extends BasicModel {
    public PreferencesModel(BasicActivity controller) {
        super(controller);
    }

    public static String getPreferenceString(String key) {
        SharedPreferences sharedPreferences = AudioAnalyzerLib.context.getSharedPreferences(Constants.SHARED_PREFERENCES, MODE_PRIVATE);
        return sharedPreferences.getString(key, "");
    }

    public static boolean addPreferenceString(String key, String value) {
        return addPreferenceString(key, value, true);
    }

    private static boolean addPreferenceString(String key, String value, boolean overwright) {
        SharedPreferences sharedPreferences = AudioAnalyzerLib.context.getSharedPreferences(Constants.SHARED_PREFERENCES, MODE_PRIVATE);
        //Ouvre le fichier en mode edition (mode ecriture)
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (!sharedPreferences.contains(key) || overwright) editor.putString(key, value);

        return editor.commit();
    }

    public static void addPreferenceString(HashMap<String, String> params) {
        SharedPreferences sharedPreferences = AudioAnalyzerLib.context.getSharedPreferences(Constants.SHARED_PREFERENCES, MODE_PRIVATE);

        //Ouvre le fichier en mode edition (mode ecriture)
        SharedPreferences.Editor editor = sharedPreferences.edit();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            editor.putString(entry.getKey(), entry.getValue());
        }

        editor.commit();
    }

    public static Boolean removePreferenceString(String key) {
        SharedPreferences sharedPreferences = AudioAnalyzerLib.context.getSharedPreferences(Constants.SHARED_PREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        Boolean retValue = false;
        if (sharedPreferences.contains(key)) {
            editor.remove(key);
            retValue = true;
        }

        if (editor.commit()) return retValue;
        else return false;
    }

    public static Boolean clearPreferences() {
        SharedPreferences sharedPreferences = AudioAnalyzerLib.context.getSharedPreferences(Constants.SHARED_PREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        return editor.commit();
    }

    @Override
    public void onMessage(String message, HashMap<String, Object> args) {

    }
}
