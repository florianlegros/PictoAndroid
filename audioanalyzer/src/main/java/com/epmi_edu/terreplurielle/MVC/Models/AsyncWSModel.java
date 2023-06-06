package com.epmi_edu.terreplurielle.MVC.Models;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;

import com.epmi_edu.terreplurielle.MVC.Controllers.Activities.BasicActivity;
import com.epmi_edu.terreplurielle.Utils.ErrorReporting;
import com.epmi_edu.terreplurielle.Utils.Functions;
import com.epmi_edu.terreplurielle.audioanalyzer.R;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

/*
 * Yacine BOURADA on 2018/04/21.
 */

public class AsyncWSModel extends BasicModel {
    HashMap<String, String> requestParams;

    public AsyncWSModel(BasicActivity controller) {
        super(controller);
        this.requestParams = new HashMap<>();
    }

    @Override
    public void onMessage(String message, HashMap<String, Object> args) {

    }

    public void execute(final Context context, final String url, final WSCallback wsCallback, HashMap<String,
            String> jsonProgress, final boolean reportErrorToServer) {
        ProgressDialog progress = null;
        if (jsonProgress != null) {
            progress = new ProgressDialog(context);

            String message = jsonProgress.get("message") == null ? context.getString(R.string.wait_msg) : jsonProgress.get("message");
            progress.setMessage(message);

            String title = jsonProgress.get("title") == null ? context.getString(R.string.send_title) : jsonProgress.get("title");
            progress.setTitle(title);

            progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progress.show();
        }

        final ProgressDialog finalProgress = progress;
        Functions.AsyncCall(context, "execute_http_request", 0,
                new Runnable() {
                    public void run() {
                        try {
                            HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(url).openConnection();
                            httpURLConnection.setRequestMethod("POST");
                            httpURLConnection.setRequestProperty("Accept-Charset", "UTF-8");

                            if (wsCallback != null) {
                                String response = getResponse(context, httpURLConnection);
                                String error = "";
                                JsonObject dataObj = null;

                                if (response.isEmpty()) dataObj = new JsonObject();
                                else {
                                    response = response.trim();
                                    char cStart = response.charAt(0);
                                    char cEnd = response.charAt(response.length() - 1);
                                    if ((cStart == '[' && cEnd == ']') || (cStart == '{' && cEnd == '}')) {
                                        JsonElement jsonRoot = new JsonParser().parse(response);
                                        if (jsonRoot == null) {
                                            if (response.isEmpty()) dataObj = new JsonObject();
                                            else error = "Server error : " + response;
                                        } else {
                                            if (jsonRoot.isJsonObject()) {
                                                JsonObject rootObj = jsonRoot.getAsJsonObject();
                                                if (rootObj.get("data") == null) {
                                                    JsonElement jsonError = rootObj.get("error");

                                                    error = "Server error : "
                                                            + (jsonError == null ? response : jsonError.getAsString());
                                                } else dataObj = rootObj;
                                            } else dataObj = new JsonObject();
                                        }
                                    } else error = response;
                                }

                                if (dataObj != null) wsCallback.response(dataObj);
                                else {
                                    if (reportErrorToServer) {
                                        new ErrorReporting(context, new Throwable().getStackTrace()[0],
                                                "File : " + url + ". " + error);
                                    }

                                    wsCallback.error(error);
                                }
                            }

                            if (finalProgress != null) {
                                Functions.HandleUIFromAnotherThread((Activity) context, new Runnable() {
                                    public void run() {
                                        if (finalProgress != null) finalProgress.dismiss();
                                    }
                                });
                            }
                        } catch (IOException e) {
                            new ErrorReporting(context, e, this.getClass().getName());
                        }
                    }
                });
    }

    public void setParams(HashMap<String, String> requestParams) {
        this.requestParams = requestParams;
    }

    public void addParams(String key, String value) {
        this.requestParams.put(key, value);
    }

    private String getResponse(Context context, HttpURLConnection httpURLConnection) {
        String result = "";
        try {
            if (requestParams != null && !requestParams.isEmpty()) {
                OutputStream os = httpURLConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(Functions.getPostDataString(requestParams));

                writer.flush();
                writer.close();
                os.close();
            }

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));

            String line;
            while ((line = bufferedReader.readLine()) != null) result += line;
        } catch (IOException e) {
            //Exception will be handled by the execute mehtod above
        } finally {
            httpURLConnection.disconnect();
        }

        return result;
    }

    private String getOkhttpResponse(Context context, String url) {
        String result = "";
        /*try {
            OkHttpClient client = new OkHttpClient();
            FormBody.Builder formBuilder = new FormBody.Builder();
            for(Map.Entry<String, String> entry : requestParams.entrySet()) formBuilder.add(entry.getKey(), entry.getValue());

            RequestBody formBody = formBuilder.build();
            Request request = new Request.Builder().method("POST", formBody).url(url).build();

            Response response = client.newCall(request).execute();
            if(response.isSuccessful()) result = response.body().string();
        }
        catch(IOException e)
        {
            //Exception will be handled by the "execute" mehtod above
        }
        */
        return result;
    }

    public interface WSCallback {
        void response(JsonObject dataObj);

        void error(String error);
    }
}