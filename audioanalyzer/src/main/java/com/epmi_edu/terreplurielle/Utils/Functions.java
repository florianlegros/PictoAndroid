package com.epmi_edu.terreplurielle.Utils;

import static android.content.Context.NOTIFICATION_SERVICE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import com.epmi_edu.terreplurielle.AudioAnalyzerLib;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Functions {
    public static boolean isConnection(Context context) {
        boolean connected = false;
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

            connected = networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnectedOrConnecting() ? true : false;
        } catch (Exception e) {
            new ErrorReporting(e, Functions.class.getName());
        }

        return connected;
    }

    public static String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {

        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (first) first = false;
            else result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return result.toString();
    }

    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    public static void showSnackbar(View context, String message, Runnable callback) {
        final Snackbar snackbar = Snackbar.make(context, message, BaseTransientBottomBar.LENGTH_INDEFINITE);
        if (callback == null) {
            callback = new Runnable() {
                @Override
                public void run() {
                }
            };
        }

        final Runnable finalCallback = callback;
        snackbar.setAction("Ok", new View.OnClickListener() {
            public void onClick(View view) {
                if (finalCallback != null) finalCallback.run();
                snackbar.dismiss();
            }
        }).show();
    }

    public static int getResourceId(Context context, String pVariableName, String pResourcename) {
        int id = -1;
        try {
            id = context.getResources().getIdentifier(pVariableName, pResourcename, context.getPackageName());
        } catch (Exception e) {
            new ErrorReporting(context, e, Functions.class.getName());
        }

        return id;
    }

    public static String getStringResourceByName(String aString) {
        Context context = AudioAnalyzerLib.context;
        String packageName = context.getPackageName();
        int resId = context.getResources().getIdentifier(aString, "string", packageName);
        return context.getString(resId);
    }

    static public void createNotification(Context context, Class notificationClass, ContentValues intentData,
                                          int drawableResource) {

        Notification.Builder notificationBuilder = new Notification.Builder(context)
                .setContentTitle(intentData.getAsString("title"))
                .setContentText(intentData.getAsString("message"));

        if (drawableResource > 0)
            notificationBuilder = notificationBuilder.setSmallIcon(drawableResource);

        if (notificationClass != null) {
            Intent intent = new Intent(context, notificationClass);

            Set<Map.Entry<String, Object>> valueSet = intentData.valueSet();
            for (Map.Entry<String, Object> entry : valueSet)
                intent.putExtra(entry.getKey(), entry.getValue().toString());

            PendingIntent pItent = PendingIntent.getActivity(context, (int) System.currentTimeMillis(), intent, 0);
            notificationBuilder.setContentIntent(pItent);
        }

        Notification notification = notificationBuilder.build();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(0, notification);
    }

    @SuppressLint("WrongConstant")
    public static void ShowDialog(final Activity activity, final String title, final String message, final ViewGroup view,
                                  final HashMap<String, Runnable> buttons, final int titleBkgResId,
                                  final int buttonBkgResourceId, final int fontResourceId, final int msgTextColorId,
                                  final int closeResId) {
        Functions.HandleUIFromAnotherThread(activity, new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            public void run() {
                try {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
                    alertDialogBuilder.setCancelable(false);

                    LinearLayout dialogLayout = new LinearLayout(activity);
                    dialogLayout.setOrientation(LinearLayout.VERTICAL);
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);

                    dialogLayout.setLayoutParams(layoutParams);
                    dialogLayout.setGravity(Gravity.CENTER);

                    //Creating the Dialog's header : title + close button
                    LinearLayout titleLayout = new LinearLayout(activity);
                    titleLayout.setOrientation(LinearLayout.HORIZONTAL);
                    layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);

                    titleLayout.setPadding(15, 10, 15, 10);
                    titleLayout.setLayoutParams(layoutParams);
                    titleLayout.setGravity(Gravity.TOP);
                    if (titleBkgResId > 0) titleLayout.setBackgroundResource(titleBkgResId);

                    TextView titleTextView = new TextView(activity);
                    titleTextView.setText(title);
                    titleTextView.setTextColor(Color.parseColor("#ffffee"));

                    //if(fontResourceId > 0)
                    //titleTextView.setTypeface(ResourcesCompat.getFont(activity, fontResourceId));

                    titleTextView.setTextSize(20);
                    titleLayout.addView(titleTextView);

                    RelativeLayout imgCloseLayout = new RelativeLayout(activity);

                    layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);

                    layoutParams.weight = 1.0f;
                    layoutParams.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

                    imgCloseLayout.setLayoutParams(layoutParams);
                    imgCloseLayout.setGravity(Gravity.RIGHT);
                    ImageView imageViewClose = new ImageView(activity);
                    if (closeResId > 0) imageViewClose.setImageResource(closeResId);

                    imgCloseLayout.addView(imageViewClose);

                    titleLayout.addView(imgCloseLayout);
                    dialogLayout.addView(titleLayout);
                    final AlertDialog alertDialog = alertDialogBuilder.create();

                    if (view == null) {
                        LinearLayout msgLayout = new LinearLayout(activity);
                        msgLayout.setOrientation(LinearLayout.HORIZONTAL);
                        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT);
                        layoutParams.setMargins(10, 10, 10, 10);
                        msgLayout.setLayoutParams(layoutParams);
                        msgLayout.setGravity(Gravity.CENTER_HORIZONTAL);

                        TextView msgTextView = new TextView(activity);
                        //if(fontResourceId > 0)  msgTextView.setTypeface(ResourcesCompat.getFont(activity, fontResourceId));
                        if (msgTextColorId > 0)
                            msgTextView.setTextColor(ContextCompat.getColor(activity, msgTextColorId));

                        msgTextView.setTextSize(19);
                        msgTextView.setText(message);
                        msgLayout.addView(msgTextView);

                        dialogLayout.addView(msgLayout);

                        alertDialog.setView(dialogLayout);
                    } else {
                        alertDialog.setView(dialogLayout);
                        layoutParams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT);

                        view.setLayoutParams(layoutParams);

                        dialogLayout.addView(view);//for now view must always be a LinearLayout.
                    }

                    imageViewClose.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            alertDialog.dismiss();
                        }
                    });

                    HashMap<String, Runnable> copyButtons = buttons;
                    if (copyButtons == null) {
                        copyButtons = new HashMap<String, Runnable>();
                        copyButtons.put("Ok", null);
                    }

                    LinearLayout buttonsLayout = new LinearLayout(activity);
                    buttonsLayout.setOrientation(LinearLayout.HORIZONTAL);
                    layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);

                    layoutParams.setMargins(0, 10, 0, 10);
                    buttonsLayout.setLayoutParams(layoutParams);
                    buttonsLayout.setGravity(Gravity.BOTTOM | Gravity.CENTER);
                    dialogLayout.addView(buttonsLayout);

                    Set<Map.Entry<String, Runnable>> buttonList = copyButtons.entrySet();
                    HashMap<String, Integer> margins = new HashMap<>();
                    margins.put("left", 6);
                    margins.put("right", 6);
                    for (final Map.Entry<String, Runnable> entry : buttonList) {
                        Runnable clickEventAction = new Runnable() {
                            @Override
                            public void run() {
                                Runnable action = entry.getValue();
                                if (action != null) action.run();

                                alertDialog.dismiss();
                            }
                        };

                        Button button = Functions.createButton(activity, "#FFFFFF", entry.getKey(), margins,
                                buttonBkgResourceId, fontResourceId, 18, 0, 0, 0,
                                clickEventAction);

                        buttonsLayout.addView(button);
                    }

                    alertDialog.show();
                } catch (Resources.NotFoundException e) {
                    new ErrorReporting(activity, e, Functions.class.getName());
                }
            }
        });
    }

    public static void ShowDialog(Activity activity, String title, String message, int viewResource,
                                  HashMap<String, Runnable> buttons, int titleBkgResId, int buttonBkgResourceId,
                                  int fontResourceId, int msgTextColorId, int closeResId) {

        if (viewResource > 0) {
            ShowDialog(activity, title, message, (ViewGroup) LayoutInflater.from(activity).inflate(viewResource, null),
                    buttons, titleBkgResId, buttonBkgResourceId, fontResourceId, msgTextColorId, closeResId);
        } else {
            ShowDialog(activity, title, message, null, buttons, titleBkgResId, buttonBkgResourceId,
                    fontResourceId, msgTextColorId, closeResId);
        }
    }

    public static void ShowDialog(Activity activity, String title, String message) {
        ShowDialog(activity, title, message, null, null, 0, 0, 0, 0, 0);
    }

    public static Button createButton(Activity activity, String color, String label, HashMap<String, Integer> margins,
                                      int backgroundResId, int fontResId, int textSize, int gravity,
                                      int w, int h, final Runnable action) {

        Button button = new Button(activity);
        button.setTextColor(Color.parseColor(color));
        if (label != null && !label.isEmpty()) button.setText(label);

        if (w == 0) w = LinearLayout.LayoutParams.WRAP_CONTENT;
        if (h == 0) h = LinearLayout.LayoutParams.WRAP_CONTENT;

        LinearLayout.LayoutParams btnLayout = new LinearLayout.LayoutParams(w, h);

        if (margins != null) {
            int leftMargin = margins.containsKey("left") ? margins.get("left").intValue() : 0;
            int topMargin = margins.containsKey("top") ? margins.get("top").intValue() : 0;
            int rightMargin = margins.containsKey("right") ? margins.get("right").intValue() : 0;
            int bottomMrgin = margins.containsKey("bottom") ? margins.get("bottom").intValue() : 0;

            btnLayout.setMargins(leftMargin, topMargin, rightMargin, bottomMrgin);
        }

        button.setLayoutParams(btnLayout);

        if (gravity > 0) button.setGravity(gravity);

        if (backgroundResId > 0)
            BitmapTools.setBackgroundFromDrawable(button, activity, backgroundResId);

        button.setAllCaps(false);

        //if(fontResId > 0)   button.setTypeface(ResourcesCompat.getFont(activity, fontResId));

        if (textSize > 0) button.setTextSize(textSize);

        if (action != null) {
            button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    action.run();
                }
            });
        }

        return button;
    }

    static public void AsyncCall(final Context context, String threadName, int delay, final Runnable callback) {
        final HandlerThread asyncThread = new HandlerThread(threadName);
        asyncThread.start();

        new Handler(asyncThread.getLooper()).postDelayed(new Runnable() {
            public void run() {
                callback.run();
                asyncThread.quitSafely();
            }
        }, delay);
    }

    /**
     * Yacine : When handling a UI widget from a different thread than the one that created it,
     * we need the following code to manipulate the widget.
     */
    public static void HandleUIFromAnotherThread(Activity activity, final Runnable callback) {
        if (activity == null) return;
        Looper currentThread = Looper.myLooper();
        if (currentThread != null && currentThread.getThread() != null
                && currentThread.getThread().isAlive() && currentThread == Looper.getMainLooper()) {
            callback.run();
        } else activity.runOnUiThread(callback);
    }

    public static Object createClassInstance(String className) throws Exception {
        Class c = Class.forName(className);
        return c.newInstance();
    }

    public static float getFontSize(Context context, int textAppearance) {
        TypedValue typedValue = new TypedValue();

        context.getTheme().resolveAttribute(textAppearance, typedValue, true);

        int[] textSizeAttr = new int[]{android.R.attr.textSize};

        TypedArray arr = context.obtainStyledAttributes(typedValue.data, textSizeAttr);

        float fontSize = arr.getDimensionPixelSize(0, -1);
        arr.recycle();

        return fontSize;
    }

    public static byte[] shortToByte(short[] input) {
        int iterations = input.length;
        byte[] buffer = new byte[input.length * 2];
        for (int i = 0, j = 0; i != iterations; i++, j += 2) {
            buffer[j] = (byte) (input[i] & 0x00FF);
            buffer[j + 1] = (byte) ((input[i] & 0xFF00) >> 8);
        }

        return buffer;
    }

    public static byte[] longToBytes(long l) {
        byte[] result = new byte[8];
        for (int i = 7; i >= 0; i--) {
            result[i] = (byte) (l & 0xFF);
            l >>= 8;
        }

        return result;
    }
/*
    public byte[] longToBytes(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(x);
        return buffer.array();
    }

    public long bytesToLong(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.put(bytes);
        buffer.flip();//need flip
        return buffer.getLong();
    }
    */

    public static long bytesToLong(byte[] b) {
        long result = 0;
        for (int i = 0; i < 8; i++) {
            result <<= 8;
            result |= (b[i] & 0xFF);
        }
        return result;
    }

    public static void saveToFile(long[] data, String filePath) {
        try {
            File file = new File(filePath);
            FileOutputStream fos = new FileOutputStream(file);

            int i = 0, size = data.length;
            while (i < size) {
                byte[] bytes = longToBytes(data[i++]);
                int byte_len = bytes.length;
                for (int j = 0; j < byte_len; j++) fos.write(bytes[j]);
            }

            fos.flush();
            fos.close();
        } catch (IOException e) {
            new ErrorReporting(e, Functions.class.getName());
        }
    }

    public static void saveToFile(byte[] data, String filePath) {
        try {
            File file = new File(filePath);
            FileOutputStream fos = new FileOutputStream(file);

            int i = 0, size = data.length;
            while (i < size) {
                fos.write(data[i++]);

                /*byte[] bytes = longToBytes(data[i++]);
                int byte_len = bytes.length;
                for(int j = 0; j < byte_len; j++)   fos.write(bytes[j]);
                */
            }

            fos.flush();
            fos.close();
        } catch (IOException e) {
            new ErrorReporting(e, Functions.class.getName());
        }
    }

    public static byte[] extractBytes(String file) {
        byte[] bytes = null;
        if (!(new File(file).exists())) return bytes;

        try (FileInputStream inputStream = new FileInputStream(new File(file))) {
            bytes = new byte[inputStream.available()];
            inputStream.read(bytes);
            inputStream.close();
        } catch (IOException e) {
            new ErrorReporting(e, Functions.class.getName());
        }

        return bytes;
    }

    public static short[] byteToShort(byte[] data) {
        if (data == null) return null;

        short[] samples = null;
        try {
            ShortBuffer sb = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();
            samples = new short[sb.limit()];
            sb.get(samples);
        } catch (Exception e) {
            new ErrorReporting(e, Functions.class.getName());
        }

        return samples;
    }

    public static long[] byteToLong(byte[] data) {
        if (data == null) return null;

        long[] samples = null;
        try {
            LongBuffer sb = ByteBuffer.wrap(data).order(ByteOrder.BIG_ENDIAN).asLongBuffer();
            samples = new long[sb.limit()];
            sb.get(samples);
        } catch (Exception e) {
            new ErrorReporting(e, Functions.class.getName());
        }

        return samples;
    }

    public static int audioFileSampleRate(String path) {
        MediaExtractor mex = new MediaExtractor();
        try {
            mex.setDataSource(path);// the adresss location of the sound on sdcard.
        } catch (IOException e) {
            new ErrorReporting(e, Functions.class.getName());
        }

        MediaFormat mf = mex.getTrackFormat(0);
        return mf.getInteger(MediaFormat.KEY_SAMPLE_RATE);
    }

    public static MediaFormat audioFileParamsGetter(String path) {
        MediaExtractor mex = new MediaExtractor();
        MediaFormat mf = null;
        try {
            mex.setDataSource(path);
            mf = mex.getTrackFormat(0);
        } catch (IOException e) {
            new ErrorReporting(e, Functions.class.getName());
        }

        return mf;
    }

    public static int toScreenValue(int v) {
        return (int) (v * AudioAnalyzerLib.screenDensity);
    }

    public static int encodeColor(int R, int G, int B) {
        return 0xFF000000 | (R & 0xff) << 16 | (G & 0xff) << 8 | (B & 0xff);
    }

    public static int[] decodeColor(int color) {
        int A = (color >> 24) & 0xff; // or color >>> 24
        int R = (color >> 16) & 0xff;
        int G = (color >> 8) & 0xff;
        int B = (color) & 0xff;

        return new int[]{A, R, G, B};
    }

    public static long[] convertToLong(short[] data) {
        if (data == null) return null;

        int sz = data.length;
        long[] longData = new long[sz];
        for (int i = 0; i < sz; i++) longData[i] = data[i];

        return longData;
    }

    public interface ErrorDispatcher {
        void error(String message);
    }

    public interface ProgressListener {
        void start();

        void progress(int progress);

        void complete();
    }
}