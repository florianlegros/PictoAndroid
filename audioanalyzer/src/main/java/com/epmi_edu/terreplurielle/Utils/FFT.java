package com.epmi_edu.terreplurielle.Utils;

/**
 * Yacine BOURADA : 06/18/2018, Fast Fourier Transform (FFT) and Short FFT class.
 */

import java.util.ArrayList;
import java.util.Arrays;

public class FFT {
    private static double[] mCosTable = null;
    private static double[] mSinTable = null;
    private static int mTrigonometricTableLen = 0;

    private static int[] fftLengths(int nbSamples) {
        int powerOf2 = (int) Math.ceil(Math.log(nbSamples) / Math.log(2));
        return new int[]{powerOf2, (int) Math.pow(2, powerOf2)};
    }

    private static void createTrigonometricTable(int len) {
        if (mCosTable != null && len == mTrigonometricTableLen) return;

        mTrigonometricTableLen = len;
        int half = (int) (len / 2f);

        mCosTable = new double[half];
        mSinTable = new double[half];

        double pi_factor = -2 * Math.PI / len;
        for (int k = 0; k < half; k++) {
            double angle = pi_factor * k;
            mCosTable[k] = Math.cos(angle);
            mSinTable[k] = Math.sin(angle);
        }
    }

    private static void swap(long[] x, int i, int j) {
        long t1 = x[i];
        x[i] = x[j];
        x[j] = t1;
    }

    public static long[] fft(long[] samples) {
        if (samples == null) return null;

        int[] lengths = fftLengths(samples.length);

        int powerOf2 = lengths[0], nbSamples = lengths[1];

        createTrigonometricTable(nbSamples);

        long[] imaginary = new long[nbSamples];//zero-valued array imaginary part
        if (samples.length != nbSamples) samples = Arrays.copyOf(samples, nbSamples);

        int i, j = 0, n1, n2 = nbSamples / 2, iMax = nbSamples - 1;
        //Bit-reverse
        for (i = 1; i < iMax; i++) {
            n1 = n2;
            while (j >= n1) {
                j -= n1;
                n1 /= 2;
            }

            j += n1;

            if (i < j) swap(samples, i, j);
        }

        calculateLocalFFT(samples, imaginary, powerOf2, nbSamples, 0);//calculate FFT on all samples

        int half = nbSamples / 2;
        for (i = 0; i < half; i++)
            samples[i] = (long) ((long) Math.sqrt(Math.pow(samples[i], 2) + Math.pow(imaginary[i], 2)) / 1000f);

        return Arrays.copyOf(samples, half);
    }

    public static ArrayList short_fft(long[] samples, int window_len) {
        if (samples == null) return null;

        int oriLen = samples.length, nbSamples = fftLengths(samples.length)[1];

        long[] imaginary = new long[nbSamples];//zero-valued array imaginary part
        if (samples.length != nbSamples) samples = Arrays.copyOf(samples, nbSamples);

        window_len = (int) Math.min(nbSamples / 2, Math.max(2, Math.pow(2, (int) Math.ceil(Math.log(window_len) / Math.log(2)))));

        createTrigonometricTable(window_len);

        int powerOf2 = (int) Math.ceil(Math.log(window_len) / Math.log(2)), fft_window_nb = nbSamples / window_len;

        long[][] output = new long[fft_window_nb][];
        long[] minMaxAmplitudes = new long[2];

        //Bit-reverse samples
        int i, j = 0, step, L2 = window_len / 2, iMax = window_len - 1;
        for (i = 1; i < iMax; i++) {
            step = L2;
            while (j >= step) {
                j -= step;
                step /= 2;
            }

            j += step;

            if (i < j) {
                for (int shift = 0; shift < nbSamples; shift += window_len)
                    swap(samples, shift + i, shift + j);
            }
        }

        long min = Long.MAX_VALUE, max = Long.MIN_VALUE;
        /****** "Local" Short-FFT (FFT for all the windows (input samples blocks/chunks) ****************************************/
        int half = window_len / 2, window_index = 0;
        for (int shift = 0; shift < nbSamples && shift < oriLen; shift += window_len) {
            calculateLocalFFT(samples, imaginary, powerOf2, window_len, shift);

            int max_i = Math.min(oriLen, shift + half);

            window_index = shift / window_len;

            long[] short_fft = new long[max_i - shift];
            for (i = shift; i < max_i; i++) {
                long amplitude = 10 * ((long) Math.log10(Math.sqrt(Math.pow(samples[i], 2) + Math.pow(imaginary[i], 2))));

                if (min > amplitude) min = amplitude;
                else if (max < amplitude) max = amplitude;

                short_fft[i - shift] = amplitude;
            }

            output[window_index] = short_fft;
        }

        if (++window_index != fft_window_nb) output = Arrays.copyOf(output, window_index);

        minMaxAmplitudes[0] = max;
        minMaxAmplitudes[1] = min;

        ArrayList ret = new ArrayList();
        ret.add(0, output);
        ret.add(1, minMaxAmplitudes);

        return ret;
    }

    private static void calculateLocalFFT(long[] samples, long[] imaginary, int powerOf2, int sample_len, int shift) {
        int increment = 1, step;
        for (int level = 0; level < powerOf2; level++) {
            step = increment;
            increment *= 2;
            int trigoTblIndex = 0;

            for (int j = 0; j < step; j++) {
                double cosine = mCosTable[trigoTblIndex], sine = mSinTable[trigoTblIndex];
                trigoTblIndex += 1 << (powerOf2 - level - 1);

                int len = sample_len + shift;
                for (int k = shift + j; k < len; k += increment) {
                    long x_val = (long) ((cosine * samples[k + step]) - (sine * imaginary[k + step]));
                    long y_val = (long) ((sine * samples[k + step]) + (cosine * imaginary[k + step]));

                    samples[k + step] = samples[k] - x_val;
                    imaginary[k + step] = imaginary[k] - y_val;

                    samples[k] += x_val;
                    imaginary[k] += y_val;
                }
            }
        }
    }

    /*
    // step = 2 ^ (level-1)
    // increm = 2 ^ level;
    int step = 1;
    for (int level = 1; level <= _logPoints; level++)
    {
        int increm = step * 2;
        for (int j = 0; j < step; j++)
        {
            // U = exp ( - 2 PI j / 2 ^ level )
            Complex U = _W [level][j];
            for (int i = j; i < _Points; i += increm)
            {
                // in-place butterfly
                // Xnew[i]      = X[i] + U * X[i+step]
                // Xnew[i+step] = X[i] - U * X[i+step]
                Complex T = U;
                T *= _X [i+step];
                _X [i+step] = _X[i] - T;
                _X [i] += T;
            }
        }
        step *= 2;
    }
    */
}