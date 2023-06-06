package com.epmi_edu.terreplurielle.MVC.Controllers.Fragments;


import android.view.View;
import android.view.ViewParent;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.epmi_edu.terreplurielle.MVC.Controllers.Activities.BasicActivity;

import java.util.HashMap;

public abstract class BasicFragment extends Fragment {
    public abstract void onMessage(String message, HashMap<String, Object> args);

    public BasicActivity activity() {
        BasicActivity activity = (BasicActivity) getActivity();
        if (activity == null) {
            View view = getView();
            if (view != null) {
                ViewParent viewParent = view.getParent();
                if (viewParent instanceof FragmentActivity)
                    activity = (BasicActivity) viewParent;
            }
        }

        return activity;
    }
}
