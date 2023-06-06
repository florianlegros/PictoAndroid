package com.epmi_edu.terreplurielle.MVC.Controllers.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.epmi_edu.terreplurielle.audioanalyzer.R;

public class TeacherFragment extends RecordFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return createViews(inflater, container, R.layout.fragment_teacher);
    }
}
