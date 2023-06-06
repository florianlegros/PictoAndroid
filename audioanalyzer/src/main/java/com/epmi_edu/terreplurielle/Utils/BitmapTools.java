package com.epmi_edu.terreplurielle.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Build;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.core.content.ContextCompat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class BitmapTools {
    public static Bitmap bitmapFromNetwork(Context context, String url) {
        Bitmap bitmap = null;
        try {
            if (Functions.isConnection(context))
                bitmap = BitmapFactory.decodeStream(new java.net.URL(url).openStream());
        } catch (IOException e) {
            if (e.getMessage() == null)
                new ErrorReporting(context, new Throwable().getStackTrace()[0], " Can't load file : " + url);
            else new ErrorReporting(context, e, Functions.class.getName());
        }

        return bitmap;
    }

    public static Bitmap bitmapFromLocalStorage(Context context, String path) {
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream(new FileInputStream(new File(path)));
        } catch (Exception e) {
            new ErrorReporting(context, e, BitmapTools.class.getName());
        }

        return bitmap;
    }

    public static String base64EncodeBitmap(Context context, Bitmap bitmap) {
        String data = "";
        try {
            ByteArrayOutputStream bao = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, bao);
            byte[] ba = bao.toByteArray();
            data = Base64.encodeToString(ba, Base64.NO_WRAP);
        } catch (Exception e) {
            new ErrorReporting(context, e, BitmapTools.class.getName());
        }

        return data;
    }

    public static Bitmap base64DecodeBitmap(Context context, String base64Image) {
        Bitmap bitmap = null;
        try {
            byte[] data = Base64.decode(base64Image, Base64.DEFAULT);
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inMutable = true;
            bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, opt);
        } catch (Exception e) {
            new ErrorReporting(context, e, BitmapTools.class.getName());
        }

        return bitmap;
    }

    public static String saveToInternalStorage(Context context, Bitmap photo, String imageName, String directoryName) {
        File pathFile = null;
        try {
            ContextWrapper contextWrapper = new ContextWrapper(context);
            File directory = contextWrapper.getDir(directoryName, Context.MODE_PRIVATE);
            pathFile = new File(directory, imageName);
            int i = 0;
            while (pathFile.exists())
                pathFile = new File(directory, imageName + (i++) + "");

            FileOutputStream fos = new FileOutputStream(pathFile);
            int size = photo.getByteCount();
            int quality = size > 1000 ? 80 : 100;

            photo.compress(Bitmap.CompressFormat.JPEG, quality, fos);
        } catch (Exception e) {
            new ErrorReporting(context, e, BitmapTools.class.getName());
        }

        return pathFile == null ? "" : pathFile.getAbsolutePath();
    }

    public static Bitmap createCircularBitmapWithBorder(Context context, Bitmap bitmap, int borderWidth) {
        Bitmap resBitmap = null;
        try {
            if (bitmap != null && !bitmap.isRecycled()) {
                int width = bitmap.getWidth() + borderWidth;
                int height = bitmap.getHeight() + borderWidth;

                width += borderWidth;
                height += borderWidth;

                resBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);//bitmap.getConfig());//
                Canvas canvas = new Canvas(resBitmap);

                BitmapShader shader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
                Paint paint = new Paint();
                paint.setAntiAlias(true);
                paint.setShader(shader);
                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));

                float radius = ((float) Math.min(width, height)) / 2f;
                canvas.drawCircle(width / 2f, height / 2f, radius - borderWidth / 2f, paint);

                //Drawing the blue ("#446688") outer border
                paint.setShader(null);
                paint.setStyle(Paint.Style.STROKE);
                paint.setColor(Color.parseColor("#446688"));
                paint.setStrokeWidth(borderWidth);
                canvas.drawCircle(width / 2f, height / 2f, radius, paint);

                //Drawing the white inner border
                paint.setShader(null);
                paint.setStyle(Paint.Style.STROKE);
                paint.setColor(Color.WHITE);
                paint.setStrokeWidth(borderWidth);
                canvas.drawCircle(width / 2f, height / 2f, radius - borderWidth / 2f, paint);
            }
        } catch (Exception e) {
            new ErrorReporting(context, e, BitmapTools.class.getName());
        }

        return resBitmap;
    }

    public static Bitmap createRoundedCornerBitmap_1(Context context, Bitmap bitmap) {
        int width = bitmap.getScaledWidth(DisplayMetrics.DENSITY_560);
        int height = bitmap.getScaledHeight(DisplayMetrics.DENSITY_560);
        int width1 = bitmap.getWidth();
        int height1 = bitmap.getHeight();

        Bitmap output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(output);
        Paint paint = new Paint();
        Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        RectF rectF = new RectF(rect);

        int size = Math.min(width, height);
        float roundPx = Math.max(3f, size / 15f);

        //BitmapShader shader = new BitmapShader(bitmap, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
        //paint.setShader(shader);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(0xff424242);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    public static Bitmap createRoundedCornerBitmap(Context context, Bitmap source, float rounding) {
        Bitmap bitmap = null;
        try {
            int w = source.getWidth();
            int h = source.getHeight();

            int size = Math.min(w, h);

            int x = (w - size) / 2;
            int y = (h - size) / 2;

            Bitmap squaredBitmap = Bitmap.createBitmap(source, 0, 0, w, h);
            //if(squaredBitmap != source) source.recycle();

            bitmap = Bitmap.createBitmap(w, h, source.getConfig());

            Canvas canvas = new Canvas(bitmap);
            Paint paint = new Paint();
            BitmapShader shader = new BitmapShader(squaredBitmap, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
            paint.setShader(shader);
            paint.setAntiAlias(true);

            if (rounding == 0) rounding = Math.max(3f, size / 15f);

            //int w = source.getWidth();
            //int h = source.getHeight();
            canvas.drawRoundRect(new RectF(0, 0, w, h), rounding, rounding, paint);

        /*Paint paint1 = new Paint();
        paint1.setColor(Color.RED);
        paint1.setStyle(Paint.Style.STROKE);
        paint1.setAntiAlias(true);
        paint1.setStrokeWidth(5);
        canvas.drawRoundRect(new RectF(0, 0, w, h), r, r, paint1);*/

            //canvas.drawCircle((source.getWidth() - margin)/2, (source.getHeight() - margin)/2, radius-2, paint1);
            //squaredBitmap.recycle();


            /***
             *
             * GradientDrawable gd = new GradientDrawable();

             // Set the color array to draw gradient
             gd.setColors(new int[]{
             Color.RED,
             Color.GREEN,
             Color.YELLOW,
             Color.CYAN
             });

             // Set the GradientDrawable gradient type linear gradient
             gd.setGradientType(GradientDrawable.LINEAR_GRADIENT);

             // Set GradientDrawable shape is a rectangle
             gd.setShape(GradientDrawable.RECTANGLE);

             // Set 3 pixels width solid blue color border
             gd.setStroke(3, Color.BLUE);

             // Set GradientDrawable width and in pixels
             gd.setSize(450, 150); // Width 450 pixels and height 150 pixels

             // Set GradientDrawable as ImageView source image
             imageView.setImageDrawable(gd);
             */
        } catch (Exception e) {
            new ErrorReporting(context, e, BitmapTools.class.getName());
        }

        return bitmap;
    }

    public static void loadImage(final Context context, final String path, final Bitmap bitmap, final ImageView imageView,
                                 final int w, final int h, final float rounding, final String roundType, final Runnable endCallback) {

        Runnable callback = new Runnable() {
            @Override
            public void run() {
                try {
                    Bitmap copyBitmap = bitmap;
                    if (path != null && !path.isEmpty()) {
                        copyBitmap = path.matches("^http(s)?\\:.+") ?
                                bitmapFromNetwork(context, path) : bitmapFromLocalStorage(context, path);
                    }

                    final Bitmap finalCopyBitmap = copyBitmap;
                    Functions.HandleUIFromAnotherThread((Activity) context, new Runnable() {
                        public void run() {
                            try {
                                if (finalCopyBitmap != null) {
                                    Bitmap finalCopyBitmap1 = finalCopyBitmap;
                                    switch (roundType) {
                                        case "circular":
                                            finalCopyBitmap1 = createCircularBitmapWithBorder(context, finalCopyBitmap, 2);
                                            break;

                                        case "rounded":
                                            finalCopyBitmap1 = createRoundedCornerBitmap(context, finalCopyBitmap, rounding);
                                    }

                                    if (w > 0 && h > 0) {
                                        int bitmapWith = finalCopyBitmap1.getWidth();
                                        int bitmapHeight = finalCopyBitmap1.getHeight();

                                        float ratio = (float) bitmapHeight / (float) bitmapWith;

                                        LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams((int) (w / ratio), h);
                                        imageView.setAdjustViewBounds(true);
                                        imageView.setLayoutParams(layout);
                                    }

                                    if (finalCopyBitmap1 != null)
                                        imageView.setImageBitmap(finalCopyBitmap1);
                                }

                                if (endCallback != null) endCallback.run();
                            } catch (Exception e) {
                                new ErrorReporting(context, e, this.getClass().getName());
                            }
                        }
                    });
                } catch (Exception e) {
                    new ErrorReporting(context, e, this.getClass().getName());
                }
            }
        };

        if (bitmap != null || (path != null && !path.matches("^http(s)?\\:.+")))
            callback.run();
        else Functions.AsyncCall(context, "load-image", 0, callback);
    }

    public static Bitmap bitmapFromDrawable(Context context, int drawableId) {
        return BitmapFactory.decodeResource(context.getResources(), drawableId);
    }

    public static void setBackgroundFromDrawable(View view, Context context, int drawable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            view.setBackground(context.getDrawable(drawable));
        else view.setBackgroundDrawable(ContextCompat.getDrawable(context, drawable));
    }
}