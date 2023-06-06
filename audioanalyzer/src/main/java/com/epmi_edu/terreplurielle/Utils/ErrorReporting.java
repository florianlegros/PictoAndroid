package com.epmi_edu.terreplurielle.Utils;

import android.content.ContentValues;
import android.content.Context;

import com.epmi_edu.terreplurielle.AudioAnalyzerLib;
import com.epmi_edu.terreplurielle.MVC.Models.AsyncWSModel;
import com.epmi_edu.terreplurielle.MVC.Models.DAOModel;
import com.epmi_edu.terreplurielle.MVC.Models.PreferencesModel;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ErrorReporting implements Serializable {
    private DAOModel myLocalDB = null;
    private int PENDING_ERRORS_TIMER_FREQ = 5000;
    private Functions.ErrorDispatcher myDaoErrorDispatcher = null;

    //This constructor is called by the AppGlobal class to check for pending errors
    public ErrorReporting() {
    }

    //This constructor is called inside a catch block
    public ErrorReporting(final Context context, Exception e, String srcClassName) {
        try {
            reportFromACatch(context, e, srcClassName);
        } catch (Exception e1) {
            reportFromACatch(context, e1, this.getClass().getName());
        }
    }

    public ErrorReporting(Context context, StackTraceElement stackElm, String message) {
        doAsyncReport(context, stackElm, message);
    }

    public ErrorReporting(Exception e, String srcClassName) {
        Context context = AudioAnalyzerLib.context;
        try {
            reportFromACatch(context, e, srcClassName);
        } catch (Exception e1) {
            reportFromACatch(context, e1, this.getClass().getName());
        }
    }

    private static HashMap<String, String> getReport(Context context, StackTraceElement stackElm, String message, String source) {
        HashMap<String, String> reportParams = new HashMap<>();
        String className = stackElm.getClassName().replaceAll(".*\\.", "");//recuperer le nom de la classe uniquement

        reportParams.put("class", className);
        reportParams.put("line", String.valueOf(stackElm.getLineNumber()));
        reportParams.put("method", stackElm.getMethodName());
        reportParams.put("app", context.getApplicationInfo().loadLabel(context.getPackageManager()).toString());
        reportParams.put("source", source);
        reportParams.put("file", stackElm.getFileName());
        reportParams.put("date", String.valueOf(System.currentTimeMillis()));

        if (message == null) message = "";
        reportParams.put("message", message);

        return reportParams;
    }

    //This metod is called by the by ErrorService class
    public void startReport(Context context, HashMap<String, String> params) {
        if (Functions.isConnection(context)) remoteReporting(context, params, true);
        else localDBReporting(context, params);
    }

    public void checkPendingErrors(final Context context) {
        final String pendingErrors = getPendingErrors(context);
        if (pendingErrors.isEmpty()) return;

        Functions.AsyncCall(context, "check-pending-errors",
                Functions.isConnection(context) ? 0 : PENDING_ERRORS_TIMER_FREQ,
                new Runnable() {
                    public void run() {
                        try {
                            if (Functions.isConnection(context))
                                sendToServer(context, pendingErrors, true);
                            else checkPendingErrors(context);
                        } catch (Exception e1) {
                            reportFromACatch(context, e1, this.getClass().getName());
                        }
                    }
                });
    }

    /*************************************************** Private methods ********************************************/
    private void reportFromACatch(final Context context, final Exception e, String srcClassName) {
        StackTraceElement[] errorStack = e.getStackTrace();
        int stackLen = errorStack.length;
        for (int i = 0; i < stackLen; i++) {
            final StackTraceElement stackElm = errorStack[i];
            if (srcClassName.equals(stackElm.getClassName())) {
                doAsyncReport(context, stackElm, e.getMessage());
                break;
            }
        }
    }

    private void doAsyncReport(final Context context, final StackTraceElement stackElm, final String message) {
        Functions.AsyncCall(context, "error-report", 0,
                new Runnable() {

                    public void run() {
                        try {
                            startReport(context, getReport(context, stackElm, message, "Android"));
                        } catch (Exception e1) {
                            reportFromACatch(context, e1, this.getClass().getName());
                        }
                    }
                });
    }

    private void remoteReporting(final Context context, final HashMap<String, String> rowParams, final boolean useLocalDB) {
        if (rowParams.isEmpty()) return;

        Set<Map.Entry<String, String>> values = rowParams.entrySet();

        JsonObject jsonRow = new JsonObject();
        for (Map.Entry<String, String> entry : values)
            jsonRow.addProperty(entry.getKey(), entry.getValue().toString());

        StringBuilder errors = new StringBuilder();
        errors.append("[" + jsonRow.toString() + "]");

        sendToServer(context, errors.toString(), useLocalDB);
    }

    private void sendToServer(final Context context, String errors, final boolean useLocalDB) {
        if (errors.isEmpty()) return;

        AsyncWSModel asyncWebService = new AsyncWSModel(null);
        asyncWebService.addParams("errors", errors.toString());
        asyncWebService.execute(context, Constants.URL_SW + "log_errors.php",
                new AsyncWSModel.WSCallback() {
                    public void response(JsonObject dataObj) {
                        //errors were successfully reported to the server, now we can empty the local table.
                        if (useLocalDB && myLocalDB != null) myLocalDB.empty();
                        PreferencesModel.removePreferenceString("errors");
                    }

                    public void error(String error) {
                        //There was a server error, send an email or sms to the app creator : errors + error.
                    }
                }, null, false);
    }

    private void localDBReporting(final Context context, HashMap<String, String> error) {
        createDB(context);
        if (myLocalDB != null) myLocalDB.insert(error);

        reCheckConnection(context, error.toString());
    }

    private void sharedPrefReporting(Context context, HashMap<String, String> error) {
        JsonObject jsonRow = new JsonObject();
        for (Map.Entry<String, String> entry : error.entrySet())
            jsonRow.addProperty(entry.getKey(), entry.getValue().toString());

        String paramString = jsonRow.toString();
        String storedErrorString = PreferencesModel.getPreferenceString("errors");
        if (!storedErrorString.isEmpty()) paramString += "," + storedErrorString;

        PreferencesModel.addPreferenceString("errors", paramString);
        paramString = "[" + paramString + "]";

        reCheckConnection(context, paramString);
    }

    private void reCheckConnection(final Context context, final String paramRow) {
        int delay = Functions.isConnection(context) ? 0 : PENDING_ERRORS_TIMER_FREQ;
        Functions.AsyncCall(context, "check-connection", delay,
                new Runnable() {

                    public void run() {
                        try {
                            if (Functions.isConnection(context)) {
                                String rowString = paramRow.isEmpty() ? getPendingErrors(context) : paramRow;
                                sendToServer(context, rowString, true);
                            } else reCheckConnection(context, "");
                        } catch (Exception e1) {
                            reportFromACatch(context, e1, this.getClass().getName());
                        }
                    }
                });
    }

    private String getSharedPendingErrors(Context context) {
        String sharedErrors = PreferencesModel.getPreferenceString("errors");
        if (sharedErrors.isEmpty()) return sharedErrors;

        Gson gson = new Gson();
        JsonArray errorList = gson.fromJson("[" + sharedErrors + "]", JsonArray.class);

        int count = errorList.size();
        StringBuilder errors = new StringBuilder();
        if (count > 0) {
            for (int i = 0; i < count; i++) {
                JsonObject jsonRow = errorList.get(i).getAsJsonObject();
                errors.append(jsonRow.toString() + (i == count - 1 ? "" : ","));
            }
        }

        if (errors.length() > 0) {
            errors.insert(0, "[");
            errors.insert(errors.length(), "]");
        }

        return errors.toString();
    }

    private String getPendingErrors(Context context) {
        createDB(context);

        ArrayList<ContentValues> contentValuesList = myLocalDB.getAll(new String[]{"*"}, "id");
        int count = contentValuesList.size();
        StringBuilder errors = new StringBuilder();
        if (count > 0) {
            for (int i = 0; i < count; i++) {
                ContentValues row = contentValuesList.get(i);
                Set<Map.Entry<String, Object>> values = row.valueSet();
                JsonObject jsonRow = new JsonObject();
                for (Map.Entry<String, Object> entry : values)
                    jsonRow.addProperty(entry.getKey(), entry.getValue().toString());

                errors.append(jsonRow.toString() + (i == count - 1 ? "" : ","));
            }
        }

        String sharedPendingErrors = getSharedPendingErrors(context);
        if (!sharedPendingErrors.isEmpty()) errors.append("," + sharedPendingErrors);

        if (errors.length() > 0) {
            errors.insert(0, "[");
            errors.insert(errors.length(), "]");
        }

        return errors.toString();
    }

    private void createDB(final Context context) {
        if (myLocalDB != null) return;

        String tableName = "errors";
        String createTable = "CREATE TABLE IF NOT EXISTS " + tableName
                + "(id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "source VARCHAR(30) NOT NULL, app VARCHAR(30) NOT NULL,"
                + "file VARCHAR(30) NOT NULL, class VARCHAR(30) NOT NULL,"
                + "method VARCHAR(30) NOT NULL, message TEXT NOT NULL,"
                + "line INTEGER NOT NULL, date INTEGER NOT NULL)";

        if (myDaoErrorDispatcher == null) {
            myDaoErrorDispatcher = new Functions.ErrorDispatcher() {

                public void error(String message) {//An error occurred, somewhere, in the sqlite databse, report it.
                    if (Functions.isConnection(context)) {
                        remoteReporting(context, getReport(context,
                                        new Throwable().getStackTrace()[0], message, "Sqlite"),
                                false);
                    } else
                        sharedPrefReporting(context,
                                getReport(context, new Throwable().getStackTrace()[0], message, "Sqlite"));
                }
            };
        }

        myLocalDB = new DAOModel(null, context, "errors", tableName, createTable, 1, myDaoErrorDispatcher);
    }
}