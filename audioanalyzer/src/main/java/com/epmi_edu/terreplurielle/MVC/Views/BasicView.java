package com.epmi_edu.terreplurielle.MVC.Views;

import android.util.AttributeSet;
import android.view.View;

import com.epmi_edu.terreplurielle.MVC.Controllers.Activities.BasicActivity;
import com.epmi_edu.terreplurielle.Utils.ErrorReporting;

import java.util.HashMap;

public abstract class BasicView extends View {
    protected BasicActivity mController;

    public BasicView(BasicActivity controller, int viewResourceId) {
        super(controller);
        mController = controller;
        init(controller, viewResourceId);
    }

    public BasicView(BasicActivity controller, AttributeSet attrs, int viewResourceId) {
        super(controller, attrs);
        mController = controller;
        init(controller, viewResourceId);
    }

    public BasicView(BasicActivity controller, AttributeSet attrs, int defStyle, int viewResourceId) {
        super(controller, attrs, defStyle);
        mController = controller;
        init(controller, viewResourceId);
    }

    private void init(BasicActivity controller, int viewResourceId) {
        try {
            if (viewResourceId > 0) controller.setContentView(viewResourceId);
        } catch (Exception e) {
            new ErrorReporting(controller, e, this.getClass().getName());
        }
    }

    public void onClick(View v) {
    }

    public abstract void onMessage(String message, HashMap<String, Object> args);
}
