package com.epmi_edu.terreplurielle.MVC.Models;

import com.epmi_edu.terreplurielle.MVC.Controllers.Activities.BasicActivity;

import java.util.HashMap;

public abstract class BasicModel {
    protected BasicActivity mController;

    public BasicModel(BasicActivity controller) {
        mController = controller;
    }

    public abstract void onMessage(String message, HashMap<String, Object> args);
}
