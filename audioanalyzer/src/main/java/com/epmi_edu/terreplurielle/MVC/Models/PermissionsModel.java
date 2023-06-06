package com.epmi_edu.terreplurielle.MVC.Models;
//Yacine BOURADA : 25/04/2018

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.epmi_edu.terreplurielle.MVC.Controllers.Activities.BasicActivity;
import com.epmi_edu.terreplurielle.Utils.Constants;
import com.epmi_edu.terreplurielle.Utils.ErrorReporting;
import com.epmi_edu.terreplurielle.Utils.Functions;
import com.epmi_edu.terreplurielle.audioanalyzer.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class PermissionsModel extends BasicModel {
    private static HashMap<String, Boolean> permissions = new HashMap<>();

    public PermissionsModel(BasicActivity controller) {
        super(controller);
    }

    public static boolean requestPermission(final Activity activity, final String[] permissions, final int msgResource) {
        boolean ret = true;

        if (Build.VERSION.SDK_INT < 23) return ret;

        int permissionLen = permissions.length;
        final List listOfPermissions = new ArrayList();
        for (int i = 0; i < permissionLen; i++) {
            listOfPermissions.add(permissions[i]);
            if (!hasPermission(activity, permissions[i])) ret = false;
        }

        if (ret) return true;

        final String[] neededPermission = neededPermissions(activity, Arrays.asList(permissions));

        if (neededPermission != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, neededPermission[0])) {
                            HashMap<String, Runnable> buttons = new HashMap<>();
                            buttons.put("Oui", new Runnable() {
                                public void run() {
                                    try {
                                        String[] neededPermission = neededPermissions(activity, Arrays.asList(permissions));
                                        if (neededPermission != null) {
                                            ActivityCompat.requestPermissions(activity, neededPermission,
                                                    Constants.ACTIVITY_REQUEST_PERMISSION);
                                        }
                                    } catch (Exception e) {
                                        new ErrorReporting(e, this.getClass().getName());
                                    }
                                }
                            });

                            buttons.put("Non", null);

                            Functions.ShowDialog(activity, "Permission", activity.getString(msgResource),
                                    null, buttons, R.drawable.bkg_app, R.drawable.bkg_dialog_button,
                                    0, R.color.icon_fore, R.drawable.ic_close);
                        } else {
                            ActivityCompat.requestPermissions(activity, neededPermission,
                                    Constants.ACTIVITY_REQUEST_PERMISSION);
                        }
                    } catch (Exception e) {
                        new ErrorReporting(e, PermissionsModel.class.getName());
                    }
                }
            }).start();
        }

        return false;
    }

    public static boolean addPermission(String permission[], int[] PermissionResult) {
        if (Build.VERSION.SDK_INT < 23) return true;

        boolean permissionGranted = false;
        int grated_count = PermissionResult.length;
        if (grated_count > 0) {
            for (int i = 0; i < grated_count; i++) {
                if (PermissionResult[i] == PackageManager.PERMISSION_GRANTED) {
                    permissions.put(permission[i], true);
                    permissionGranted = true;
                }
            }
        }

        return permissionGranted;
    }

    private static String[] neededPermissions(Context context, List<String> permissionNames) {
        String[] needed_permissions = null;

        ArrayList<String> requestedPermissions = new ArrayList<>();
        int count = permissionNames.size();
        for (int i = 0; i < count; i++) {
            String permissionName = permissionNames.get(i);
            if (ContextCompat.checkSelfPermission(context, permissionName) != PackageManager.PERMISSION_GRANTED) {
                permissions.put(permissionName, false);
                requestedPermissions.add(permissionName);//This permission needs to be approved.
            } else permissions.put(permissionName, true);
        }

        int requestPermissionCount = requestedPermissions.size();
        if (requestPermissionCount > 0) {
            needed_permissions = new String[requestPermissionCount];

            for (int i = 0; i < requestPermissionCount; i++)
                needed_permissions[i] = requestedPermissions.get(i);
        }

        return needed_permissions;
    }

    public static boolean hasPermission(Context context, String permission) {
        if (Build.VERSION.SDK_INT < 23) return true;

        if (permissions.containsKey(permission)) return permissions.get(permission);

        if (ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
            permissions.put(permission, true);
            return true;
        }

        return false;
    }

    @Override
    public void onMessage(String message, HashMap<String, Object> args) {

    }
}
