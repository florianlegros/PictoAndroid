package com.epmi_edu.terreplurielle.MVC.Controllers.Activities;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.epmi_edu.terreplurielle.MVC.Models.PermissionsModel;
import com.epmi_edu.terreplurielle.Utils.Constants;
import com.epmi_edu.terreplurielle.Utils.ErrorReporting;

import java.util.HashMap;

public abstract class BasicActivity extends AppCompatActivity implements View.OnClickListener {
    public abstract void onMessage(String message, HashMap<String, Object> args);

    public abstract void onClick(View v);

    public void onRequestPermissionsResult(int RequestCode, String permission[], int[] PermissionResult) {
        try {
            if (RequestCode == Constants.ACTIVITY_REQUEST_PERMISSION)
                requestPermissionResult(PermissionsModel.addPermission(permission, PermissionResult), permission);
        } catch (Exception e) {
            new ErrorReporting(this, e, this.getClass().getName());
        }
    }

    protected abstract void requestPermissionResult(boolean permissionGranted, String permission[]);

    public void addOnClick(int[] clickableViewIds) {
        try {
            int len = clickableViewIds.length;

            for (int i = 0; i < len; i++) {
                View v = this.findViewById(clickableViewIds[i]);
                if (v != null) v.setOnClickListener(this);
            }
        } catch (Exception e) {
            new ErrorReporting(this, e, this.getClass().getName());
        }
    }

    public void setViewTag(int resourceId, Object tag) {
        try {
            View v = findViewById(resourceId);
            if (v != null) v.setTag(tag);
        } catch (Exception e) {
            new ErrorReporting(this, e, this.getClass().getName());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}