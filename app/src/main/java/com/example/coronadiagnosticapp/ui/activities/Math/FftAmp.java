package com.example.coronadiagnosticapp.ui.activities.Math;

import org.apache.commons.math3.analysis.function.Max;

import java.util.Arrays;

public class FftAmp {

    public static double FftAmp(Double[] in, int size) {
        double temp = 0;
        double POMP = 0;
        double frequency;
        double max=0;
        double[] output = new double[2*size];

        for(int i=0;i<output.length;i++)
            output[i] = 0;

        for(int x=0;x<size;x++){
            output[x]=in[x];
        }

        DoubleFft1d fft = new DoubleFft1d(size);
        fft.realForward(output);

        for(int x=0;x<2*size;x++){
            output[x]= Math.abs(output[x]);
        }


        for(int p=35; p<size; p++) {// 12 was chosen because it is a minimum frequency that we think people can get to determine heart rate.
            if(max < output[p]) {
                max = output[p];

            }
        }

        //String fftSring = "red=" + Arrays.toString(in)+"\nred_fft="+ Arrays.toString(output);
        return max;
    }
}