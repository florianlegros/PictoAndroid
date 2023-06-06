/*
Yacine BOURADA - 06 / 22 / 2018
Class MySpectrogramView : plots 3 vertically-lay-out graphs : a frequency / time / colored amplitude graph
 *                                                      a time graph and a frequency graph.
 */

package com.epmi_edu.terreplurielle.MVC.Views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Picture;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.ViewGroup;

import androidx.core.content.ContextCompat;

import com.epmi_edu.terreplurielle.MVC.Controllers.Activities.BasicActivity;
import com.epmi_edu.terreplurielle.Utils.ErrorReporting;
import com.epmi_edu.terreplurielle.audioanalyzer.R;

import java.util.ArrayList;
import java.util.HashMap;

public class SpectrogramView extends BasicView {
    private float mScreenDensity;
    private Paint mStrokePaint, mAxisPaint, mFillPaint, mMarkerPaint;

    private int m_axis_legend_size, m_spectrogram_top, m_time_spectrum_top, m_frequency_spectrum_top,
            mWidth, mHeight, m_graph_w, m_graph_r, m_spectrogram_h, mMarkerPosition = -1, m_graph_h;//2 graphs : time and FFT graphs

    private float mTimeLength/*in seocnds*/, mMaxFrequency/* in kilo hertz */, mMarkerStep;
    private long[][] mSpectrumData;
    private long[] mSamplesData, mFFTData;

    private int mShortFFTLength;
    private long[] mMinMaxAmplitudes;

    private Picture mCachedWaveform;
    private Bitmap mCachedWaveformBitmap;
    private boolean mUpdateSize = true;

    /**********************************************************/
    public SpectrogramView(Context context) {
        super((BasicActivity) context, 0);
        initView(context, null, 0);
    }

    public SpectrogramView(Context context, AttributeSet attrs) {
        super((BasicActivity) context, attrs, 0);
        initView(context, attrs, 0);
    }

    public SpectrogramView(Context context, AttributeSet attrs, int defStyle) {
        super((BasicActivity) context, attrs, defStyle, 0);
        initView(context, attrs, defStyle);
    }

    @Override
    public void onMessage(String message, HashMap<String, Object> args) {

    }

    private void initView(Context context, AttributeSet attrs, int defStyle) {
        mScreenDensity = getContext().getResources().getDisplayMetrics().density;
        m_axis_legend_size = (int) (35 * mScreenDensity);
        m_spectrogram_top = (int) (0.6 * m_axis_legend_size);

        final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.DataView, defStyle, 0);

        int strokeColor = a.getColor(R.styleable.DataView_Color,
                ContextCompat.getColor(context, R.color.default_waveform)),
                fillColor = a.getColor(R.styleable.DataView_FillColor,
                        ContextCompat.getColor(context, R.color.default_waveformFill));

        a.recycle();

        mStrokePaint = new Paint();
        mStrokePaint.setColor(strokeColor);
        mStrokePaint.setStyle(Paint.Style.STROKE);
        mStrokePaint.setStrokeWidth(1f);
        mStrokePaint.setAntiAlias(true);

        mFillPaint = new Paint();
        mFillPaint.setStyle(Paint.Style.FILL);
        mFillPaint.setAntiAlias(true);
        mFillPaint.setStrokeCap(Paint.Cap.ROUND);
        mFillPaint.setStrokeJoin(Paint.Join.ROUND);
        mFillPaint.setStrokeMiter(100);
        mFillPaint.setColor(fillColor);

        mAxisPaint = new Paint();
        mAxisPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mAxisPaint.setColor(0xAA88FF66);
        mAxisPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mAxisPaint.setStrokeWidth(1f);
        mAxisPaint.setAntiAlias(true);
        mAxisPaint.setStrokeCap(Paint.Cap.ROUND);
        mAxisPaint.setStrokeJoin(Paint.Join.ROUND);
        mAxisPaint.setTextSize(mScreenDensity * 11);

        mMarkerPaint = new Paint();
        mMarkerPaint.setStyle(Paint.Style.STROKE);
        mMarkerPaint.setStrokeWidth(0);
        mMarkerPaint.setAntiAlias(true);
        mMarkerPaint.setColor(0xFFFFFF55);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (!mUpdateSize) return;

        mUpdateSize = false;

        mWidth = (int) (w - (10 * mScreenDensity));
        mHeight = (int) (h - (10 * mScreenDensity));

        int gap = 10, titleHeight = (int) Math.ceil(0.6f * m_axis_legend_size);
        m_graph_h = (int) ((mHeight - titleHeight - (2 * gap)) / 4f);

        int minSpectrogramHeight = 500, dh = 325 - m_graph_h;
        boolean updateHeight = false;
        if (dh > 0) {
            m_graph_h += dh;
            int spectrogramDH = h - titleHeight - (2 * m_graph_h) - (2 * gap) - minSpectrogramHeight;
            if (spectrogramDH < 0) {
                h -= spectrogramDH;
                updateHeight = true;
            }
        }

        m_graph_w = (int) (mWidth - (2.22f * m_axis_legend_size));
        m_graph_r = m_graph_w + m_axis_legend_size;

        mHeight = (int) (h - (10 * mScreenDensity));

        m_spectrogram_h = mHeight - titleHeight - (2 * m_graph_h) - (2 * gap);

        m_time_spectrum_top = m_spectrogram_top + m_spectrogram_h + gap;
        m_frequency_spectrum_top = m_time_spectrum_top + m_graph_h + gap;

        ViewGroup.LayoutParams params = getLayoutParams();
        if (params.width < 0 || updateHeight) {
            if (params.width < 0) params.width = w;
            if (updateHeight || params.height < 0) params.height = h;

            setLayoutParams(params);
        }

        super.onSizeChanged(w, h, oldw, oldh);

        drawData();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mUpdateSize = true;
            }
        }, 500);
    }

    public void setMarkerPosition(int markerPosition) {
        mMarkerPosition = markerPosition;
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.translate(5 * mScreenDensity, 5 * mScreenDensity);

        if (mCachedWaveform != null) canvas.drawPicture(mCachedWaveform);
        else if (mCachedWaveformBitmap != null)
            canvas.drawBitmap(mCachedWaveformBitmap, null, new Rect(0, 0, mWidth, mHeight), null);

        if (mMarkerPosition > -1 && mMarkerPosition < (mTimeLength * 1000f)) {
            canvas.drawLine(m_axis_legend_size + mMarkerStep * mMarkerPosition,
                    m_time_spectrum_top,
                    m_axis_legend_size + mMarkerStep * mMarkerPosition,
                    m_time_spectrum_top + m_graph_h - m_axis_legend_size, mMarkerPaint);
        }
    }

    public void draw(long[] samples, long[] fftData, ArrayList spectrogramData, float sampleRate) {
        mMarkerPosition = -1;

        mSamplesData = samples;
        mFFTData = fftData;

        mSpectrumData = (long[][]) spectrogramData.get(0);
        if (mSpectrumData == null || mSpectrumData.length == 0) return;

        mShortFFTLength = mSpectrumData[0].length;

        mMinMaxAmplitudes = (long[]) spectrogramData.get(1);

        mTimeLength = mSamplesData.length / sampleRate;
        mMarkerStep = m_graph_w / (mTimeLength * 1000f);

        mMaxFrequency = (float) Math.floor(sampleRate / 2000f);//half of the Nyquist frequency (see FFT algorithm) in kilo hertz

        drawData();
        postInvalidate();//this will call the onDraw() method to invalidate the view cache
    }

    public void clearMarker() {
        mMarkerPosition = -1;
        postInvalidate();
    }

    private void drawData() {
        try {
            if (mWidth <= 0 || mHeight <= 0 || mSpectrumData == null) return;

            Canvas canvas;
            if (Build.VERSION.SDK_INT >= 23 && isHardwareAccelerated()) {
                mCachedWaveform = new Picture();
                canvas = mCachedWaveform.beginRecording(mWidth, mHeight);
            } else {
                mCachedWaveformBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
                canvas = new Canvas(mCachedWaveformBitmap);
            }

            drawTitle(canvas);
            drawSpectrogram(canvas);
            drawTimeSpectrum(canvas);
            drawFFTSpectrum(canvas);

            if (mCachedWaveform != null) mCachedWaveform.endRecording();
        } catch (Exception e) {
            new ErrorReporting(e, this.getClass().getName());
        }
    }

    private void drawTitle(Canvas canvas) {
        float textSize = mScreenDensity * 12;
        String txt = "Fréquence Signal : " + String.format("%d", (int) (mMaxFrequency * 2)) + " khz. Fenêtre : " + String.format("%d", (mShortFFTLength * 2)) + " points.";
        createText(canvas, txt, textSize, textSize + 1f, m_axis_legend_size, m_graph_w, true);
    }

    private void createText(Canvas canvas, String txt, float textSize,
                            float top, float left, float width, boolean createRoundRect) {
        Paint paint = new Paint();
        paint.setTextSize(textSize);
        paint.setAntiAlias(true);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);

        float textWidth = (int) paint.measureText(txt);
        width = width == -1 ? textWidth : width;

        float l = left + (int) (Math.ceil((width - textWidth) / 2f));

        paint.setStyle(Paint.Style.FILL);
        if (createRoundRect) {
            paint.setColor(0x9922BB55);

            float offset = 1.1f * textSize;
            canvas.drawRoundRect(l - 4, top - offset, l + textWidth + 4, top + (textSize / 2.5f),
                    8f, 8f, paint);
        }

        paint.setColor(0xFFFFFFAA);
        canvas.drawText(txt, l, top, paint);
    }

    private void drawSpectrogram(Canvas canvas) {
        long maxAmplitude = mMinMaxAmplitudes[0], minAmplitude = mMinMaxAmplitudes[1];
        float range = maxAmplitude - minAmplitude;

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);

        float rectHeight, dataStep, dataLen = mSpectrumData.length,
                windowPixelLength = m_graph_w / dataLen, xPixel = m_axis_legend_size,
                height = m_spectrogram_h - m_axis_legend_size, spectrogramBottom = m_spectrogram_top + height;

        if (height < mShortFFTLength) {
            dataStep = mShortFFTLength / height;
            rectHeight = 1f;
        } else {
            dataStep = 1;
            rectHeight = height / (mShortFFTLength * 1f);
        }

        int maxHue = 300;
        for (int dataIndex = 0; dataIndex < dataLen; dataIndex++) {
            float k = 0f;
            long[] windowData = mSpectrumData[dataIndex];
            int windowDataLen = windowData.length;

            for (float i = 0; i < height; i += rectHeight) {
                int j = (int) Math.floor(k);

                if (j > windowDataLen - 1) break;

                int j1 = (int) Math.min(windowDataLen - 1, Math.floor(k + dataStep));
                k += dataStep;

                long amplitude = Long.MIN_VALUE;
                do {
                    amplitude = Math.max(amplitude, windowData[j++]);
                } while (j < j1);

                int h = maxHue - (int) ((int) (maxHue * (amplitude - minAmplitude)) / range),
                        clr = amplitude == 0 ? Color.HSVToColor(new float[]{0, 0f, 0f})
                                : Color.HSVToColor(new float[]{h, 1f, 1f});

                paint.setColor(clr);

                int l = (int) Math.floor(xPixel), r = (int) Math.min(m_graph_r, Math.floor(xPixel + windowPixelLength));

                float bottom = spectrogramBottom - i - rectHeight;
                if ((j1 == windowDataLen - 1) && windowDataLen < mShortFFTLength)
                    bottom = spectrogramBottom - height;

                canvas.drawRect(l, spectrogramBottom - i, r, bottom, paint);
            }

            xPixel += windowPixelLength;
        }

        drawColorLegend(canvas, minAmplitude, maxAmplitude, maxHue);

        float pixelStep = 40 * mScreenDensity;
        dataStep = mTimeLength * pixelStep / m_graph_w;
        drawXAxis(canvas, spectrogramBottom, spectrogramBottom, "Temps (secondes)", pixelStep, dataStep, mTimeLength);

        drawYAxis(canvas, m_spectrogram_top, spectrogramBottom, 0, mMaxFrequency, "Fréquence (khz)");
    }

    private void drawSpectrum(Canvas canvas, long[] samplesData, float spectrumTop, String xAxisLegend, String yAxisLegend,
                              float pixelStep, float timeStep, float dataMax) {
        long mainMin = Long.MAX_VALUE, mainMax = Long.MIN_VALUE;
        int sampleLength = samplesData.length;
        long[][] plotData = new long[m_graph_w][];

        double k = 0f, step;
        if (m_graph_w < sampleLength) {
            step = sampleLength / (m_graph_w * 1f);
            for (int i = 0; i < m_graph_w; i++) {
                int j = (int) Math.ceil(k), j1 = (int) Math.min(sampleLength, Math.ceil(k + step));
                k += step;

                long min = Long.MAX_VALUE, max = Long.MIN_VALUE;
                do {
                    long data = samplesData[j++];
                    min = Math.min(min, data);
                    max = Math.max(max, data);
                } while (j < j1);

                mainMin = Math.min(min, mainMin);
                mainMax = Math.max(max, mainMax);

                plotData[i] = new long[]{max, min};
            }
        } else {
            step = m_graph_w / (sampleLength * 1f);
            for (int i = 0; i < sampleLength; i++) {
                long data = samplesData[i];
                mainMin = Math.min(data, mainMin);
                mainMax = Math.max(data, mainMax);

                long[] plotPoint = new long[]{data, data};

                int j = (int) Math.ceil(k), j1 = (int) Math.ceil(k + step);
                do {
                    plotData[j++] = plotPoint;
                } while (j < j1);

                k += step;
            }
        }

        float height = m_graph_h - m_axis_legend_size, spectrumBottom = spectrumTop + height,
                xAxisYCoord = spectrumBottom, yAxisLegendPos = spectrumBottom;

        if (mainMin < 0) {
            float dataSpan = mainMax - mainMin;
            if (dataSpan == 0) dataSpan = 1;

            float offset = height * mainMin / dataSpan;
            xAxisYCoord += offset;
            yAxisLegendPos += (offset / 2.5f);
        }

        Path path = new Path();
        path.moveTo(m_axis_legend_size, xAxisYCoord);

        // draw maximums
        for (int x = 0; x < m_graph_w; x++) {
            float y = spectrumTop + ((mainMax - plotData[x][0]) * height) / (mainMax - mainMin);
            path.lineTo(m_axis_legend_size + x, y);
        }

        if (sampleLength > m_graph_w) {
            //draw minimums
            for (int x = m_graph_w - 1; x >= 0; x--) {
                float y = spectrumTop + ((mainMax - plotData[x][1]) * height) / (mainMax - mainMin);
                path.lineTo(m_axis_legend_size + x, y);
            }
        }

        canvas.drawPath(path, mFillPaint);
        canvas.drawPath(path, mStrokePaint);

        drawXAxis(canvas, xAxisYCoord, yAxisLegendPos, xAxisLegend, pixelStep, timeStep, dataMax);

        drawYAxis(canvas, spectrumTop, spectrumBottom, mainMin / 1000f, mainMax / 1000f, yAxisLegend);
    }

    private void drawTimeSpectrum(Canvas canvas) {
        float pixelStep = 40 * mScreenDensity, timeStep = mTimeLength * pixelStep / m_graph_w;

        drawSpectrum(canvas, mSamplesData, m_time_spectrum_top,
                "Temps (secondes)", "Amplitude (mille)", pixelStep, timeStep, mTimeLength);
    }

    private void drawFFTSpectrum(Canvas canvas) {
        float[] steps = calculateAxisUnitSteps(m_graph_w, mMaxFrequency, 15 * mScreenDensity);
        float pixelStep = steps[0], frequencyStep = steps[1];

        drawSpectrum(canvas, mFFTData, m_frequency_spectrum_top,
                "Fréquence (khz)", "Norme (millions)", pixelStep, frequencyStep, mMaxFrequency);
    }

    private void drawColorLegend(Canvas canvas, float minAmplitude, float maxAmplitude, int maxHue) {
        float amplitudeRange = maxAmplitude - minAmplitude, height = m_spectrogram_h - m_axis_legend_size,
                legendLeft = m_graph_r + 12, rightLegend = legendLeft + (14 * mScreenDensity);

        float[] steps = calculateAxisUnitSteps(height, amplitudeRange, 15 * mScreenDensity);
        float tickMarkStep = steps[0], amplitudeStep = steps[1], textSize = mScreenDensity * 11;

        Paint paint = new Paint();
        paint.setTextSize(textSize);
        paint.setStyle(Paint.Style.FILL);

        Path path = new Path();

        float yStep, colorStep, hueRange = maxHue + 1;
        if (hueRange <= height) {
            yStep = height / (hueRange * 1f);
            colorStep = 1f;
        } else {
            yStep = 1;
            colorStep = hueRange / height;
        }

        float bottom = m_spectrogram_top + height;
        for (float y = bottom, tick = bottom, amplitude = 0, colorIndex = maxHue; y >= m_spectrogram_top;
             y -= yStep, colorIndex -= colorStep) {

            if (y - m_spectrogram_top < yStep) {
                colorIndex = 0;
                yStep += y - m_spectrogram_top;
                y = m_spectrogram_top;
            }

            float top = y - yStep, off = 0f;
            int rectColor = 0;
            if (colorIndex == maxHue && minAmplitude == 0f) {
                rectColor = Color.HSVToColor(new float[]{0, 0f, 0f});
                off = 2 * yStep;
                top -= off;
            } else rectColor = Color.HSVToColor(new float[]{colorIndex, 1f, 1f});

            paint.setColor(rectColor);

            canvas.drawRect(legendLeft, top, rightLegend, y, paint);

            y -= off;
            //draw tick mark and values
            if (amplitude <= maxAmplitude) {
                if (maxAmplitude - amplitude < amplitudeStep) {
                    amplitude = maxAmplitude;
                    tickMarkStep += tick - m_spectrogram_top;
                    tick = m_spectrogram_top;
                }

                float tikPos = tick;
                path.moveTo(rightLegend, tick);
                path.lineTo(rightLegend + 10, tick);

                int h = maxHue - (int) ((int) (maxHue * (amplitude - minAmplitude)) / amplitudeRange),
                        clr = Color.HSVToColor(amplitude == 0 && minAmplitude == 0 ? new float[]{0, 0, 0}
                                : new float[]{h, 1f, 1f});

                paint.setColor(clr);
                float textTop = tikPos + (textSize / 2.5f);
                canvas.drawText(String.format("%d", (int) amplitude), rightLegend + 12, textTop, paint);

                if (tick < bottom) {//draw a tick mark at the half of each amplitude unit
                    tikPos += (tickMarkStep / 2f);
                    path.moveTo(rightLegend, tikPos);
                    path.lineTo(rightLegend + 5, tikPos);
                }

                canvas.drawPath(path, mAxisPaint);

                tick -= tickMarkStep;
                amplitude += amplitudeStep;
            }
        }

        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(0xFFFFEEAA);
        paint.setStrokeWidth(1f);
        canvas.drawRect(legendLeft - 1f, m_spectrogram_top, rightLegend + 1f,
                m_spectrogram_top + height + 1f, paint);

        createText(canvas, "dB", mScreenDensity * 10, m_axis_legend_size * 0.42f, legendLeft, -1, true);
    }

    private void drawXAxis(Canvas canvas, float yAxisPos, float yAxisLegendPos, String axisLegend,
                           float pixelStep, float dataStep, float dataMax) {
        Path path = new Path();

        path.moveTo(m_axis_legend_size, yAxisPos);
        path.lineTo(m_graph_r, yAxisPos);
        canvas.drawPath(path, mAxisPaint);

        float textSize = mScreenDensity * 11, top = yAxisPos + textSize + 5f;
        for (float i = 0, axisData = 0; axisData <= dataMax; i += pixelStep, axisData += dataStep) {
            float x = m_axis_legend_size + i;
            if (dataMax - axisData < dataStep) {
                axisData = dataMax;
                pixelStep += m_graph_r - x;
                x = m_graph_r;
            }

            path.moveTo(x, yAxisPos - 10);
            path.lineTo(x, yAxisPos + 10);

            canvas.drawPath(path, mAxisPaint);

            String txt = Math.ceil(axisData) == Math.floor(axisData) ?
                    String.format("%d", (int) axisData) : String.format("%.2f", axisData);

            float textHalWidth = mAxisPaint.measureText(txt) / 2f;
            canvas.drawText(txt, x - textHalWidth + (i == 0 ? (textHalWidth + 5f) : 0), top, mAxisPaint);
        }

        if (axisLegend == null || axisLegend.isEmpty()) return;

        createText(canvas, axisLegend, 13 * mScreenDensity,
                yAxisLegendPos + (0.75f * m_axis_legend_size), m_axis_legend_size, m_graph_w, false);
    }

    private void drawYAxis(Canvas canvas, float top, float bottom, float minAxisValue, float maxAxisValue, String axisLegend) {
        Path path = new Path();

        path.moveTo(m_axis_legend_size, top);
        path.lineTo(m_axis_legend_size, bottom);

        canvas.drawPath(path, mAxisPaint);

        float height = bottom - top;
        float[] steps = calculateAxisUnitSteps(height, maxAxisValue - minAxisValue, 20 * mScreenDensity);
        float pixelStep = steps[0], dataStep = steps[1], textSize = mScreenDensity * 11,
                h1 = maxAxisValue * height / (maxAxisValue - minAxisValue), startData = minAxisValue < 0 ? 0 : minAxisValue;
        //draw y axis positive part : 0 to maxAxisValue
        for (float i = 0, axisData = startData; axisData <= maxAxisValue; i += pixelStep, axisData += dataStep) {
            float y = top + h1 - i;
            if (maxAxisValue - axisData < dataStep) {
                axisData = maxAxisValue;
                pixelStep += y - top;
                y = top;
            }

            path.moveTo(m_axis_legend_size - 10, y);
            path.lineTo(m_axis_legend_size + 10, y);

            String txt = String.format("%d", (int) axisData);

            canvas.drawText(txt, m_axis_legend_size - mAxisPaint.measureText(txt) - 20,
                    y + (textSize / 2.4f), mAxisPaint);

            if (i > 0) {//draw a tick mark at the half of each axisData unit
                y += (pixelStep / 2f);
                path.moveTo(m_axis_legend_size - 5, y);
                path.lineTo(m_axis_legend_size + 5, y);
            }

            canvas.drawPath(path, mAxisPaint);
        }

        //draw y axis negative part : 0 to minAxisValue
        top += h1;
        for (float i = pixelStep, axisData = -dataStep; axisData >= minAxisValue; i += pixelStep, axisData -= dataStep) {
            float y = top + i;
            if (axisData - minAxisValue < dataStep) {
                axisData = minAxisValue;
                pixelStep += bottom - y;
                y = bottom;
            }

            path.moveTo(m_axis_legend_size - 10, y);
            path.lineTo(m_axis_legend_size + 10, y);

            String txt = String.format("%d", (int) axisData);

            canvas.drawText(txt, m_axis_legend_size - mAxisPaint.measureText(txt) - 20,
                    y + (textSize / 2.4f), mAxisPaint);

            //draw a tick mark at the half of each axisData unit
            y -= (pixelStep / 2f);
            path.moveTo(m_axis_legend_size - 5, y);
            path.lineTo(m_axis_legend_size + 5, y);

            canvas.drawPath(path, mAxisPaint);
        }

        if (axisLegend == null || axisLegend.isEmpty()) return;

        textSize = 11.5f * mScreenDensity;
        float y = bottom - (float) (Math.ceil(height - mAxisPaint.measureText(axisLegend)) / 2.5f), x = 25f;

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextSize(textSize);
        paint.setStrokeWidth(1f);

        canvas.save();
        canvas.rotate(-90, x, y);

        paint.setColor(0xFFFFFFAA);
        canvas.drawText(axisLegend, x, y, paint);

        canvas.restore();
    }

    private float[] calculateAxisUnitSteps(float axisSize, float dataRange, float minPixelsPerTick) {
        float dataUnit = 1f, tickUnit = axisSize / (dataRange * 1f);
        if (tickUnit < minPixelsPerTick) {
            dataUnit = (float) Math.ceil(dataRange * minPixelsPerTick / (axisSize * 1f));
            tickUnit = (dataUnit * axisSize) / (dataRange * 1f);
        }

        return new float[]{tickUnit, dataUnit};
    }
}