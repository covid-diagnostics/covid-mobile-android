package com.example.coronadiagnosticapp.ui.activities;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

public class ArtifactCutter {
    private final int windowSize;
    private double samplingFreq;
    private double meanWindowGrad;
    private double stdDevWindowGrad;
    private Double[] signal;

    /**
     * @param samplingFreq Sampling frequency of the signal
     */

    public ArtifactCutter(double samplingFreq, int windowSize, Double[] signal) {
        if (samplingFreq <= 0) {
            throw new IllegalArgumentException("samplingFreq must be greater than zero");
        }
        this.samplingFreq = samplingFreq;
        this.signal = signal;
        this.windowSize = windowSize;
    }

    private double calculatePointGrad(int i) {
        double derivative;
        if (i == 0) {
            derivative = (signal[i] - signal[i + 1]) / (1 / samplingFreq);
        } else if (i == (signal.length - 1)) {
            derivative = (signal[i - 1] - signal[i]) / (1 / samplingFreq);
        } else {
            derivative = (signal[i - 1] - signal[i + 1]) / (2 / samplingFreq);
        }
        return derivative;
    }

    private double calculateMeanWindowGrad(int windowNum) {
        double gradSum = 0;
        double windowMean;
        if ((windowNum + 1) * windowSize > signal.length) {
            for (int i = windowNum * windowSize; i < signal.length; i++) {
                gradSum += calculatePointGrad(i);
            }
            windowMean = gradSum / (signal.length - windowSize * windowNum);
        } else {
            for (int i = windowNum * windowSize; i < (windowNum + 1) * windowSize; i++) {
                gradSum += calculatePointGrad(i);
            }
            windowMean = gradSum / windowSize;
        }
        return windowMean;

    }

    private Double[] calcualteMeanWindowGrads() {
        int numWindows = (int) Math.ceil(signal.length / windowSize);
        ArrayList<Double> windowMeans = new ArrayList<Double>();
        for (int i = 0; i < numWindows; i++) {
            windowMeans.add(calculateMeanWindowGrad(i));
        }

        Double[] meansArray = windowMeans.toArray(new Double[windowMeans.size()]);
        Mean mean = new Mean();
        StandardDeviation standardDeviation = new StandardDeviation();

        Double[] absoluteGrads = new Double[windowMeans.size()];
        for (int i = 0; i < windowMeans.size(); i++) {
            absoluteGrads[i] = Math.abs(windowMeans.get(i));
        }

        meanWindowGrad = mean.evaluate(ArrayUtils.toPrimitive(absoluteGrads));
        stdDevWindowGrad = standardDeviation.evaluate(ArrayUtils.toPrimitive(absoluteGrads));
        return meansArray;
    }

    public Integer[] findAllArtifactWindows(Double[] windowMeans) {
        ArrayList<Integer> artifactWindows = new ArrayList<Integer>();
        for (int i = 0; i < windowMeans.length - 1; i++) {
            Double absNext = Math.abs(windowMeans[i + 1]);
            Double absCurr = Math.abs(windowMeans[i]);
            if (absNext / absCurr > 2 && absNext > meanWindowGrad + 2 * stdDevWindowGrad) {
                artifactWindows.add(i + 1);
            }
        }
        return artifactWindows.toArray(new Integer[artifactWindows.size()]);

    }

    /*
    private Double getSubArrayAvg(Double[] arr, int start, int end) {
        Double[] sub = Arrays.copyOfRange(arr, start, end);
        Mean mean = new Mean();


        Double sum = 0.0;

        for (Double val : sub) {
            sum += val != null ? val : 0;
        }
        return sum / sub.length;
    }*/

    /**
     * Takes a signal and removes a certain window out of it
     *
     * @param uncutSignal The signal to clean
     * @param windowNum   The window number to cut
     * @return The signal after the window has been cut out of it
     */
    private Double[] cutWindow(Double[] uncutSignal, Integer windowNum) {
        Mean mean = new Mean();
        int preStartIndex = windowNum < 3 ? 0 : (windowNum - 3) * windowSize;
        int postEndIndex = (windowNum + 4) * windowSize > uncutSignal.length ? uncutSignal.length : (windowNum + 4) * windowSize;
        if (((windowNum + 1) * windowSize) >= uncutSignal.length) { //End of array
            return uncutSignal;
        }
        Double preAvg = mean.evaluate(ArrayUtils.toPrimitive(uncutSignal), preStartIndex, (windowNum + 1) * windowSize);
        Double postAvg = mean.evaluate(ArrayUtils.toPrimitive(uncutSignal), (windowNum + 1) * windowSize, postEndIndex);

        Double diff = postAvg - preAvg;
        Double[] preSignal = Arrays.copyOfRange(uncutSignal, 0, windowNum * windowSize);
        ArrayList<Double> postSignalList = new ArrayList<Double>();

        for (int i = (windowNum + 1) * windowSize; i < uncutSignal.length; i++) {
            postSignalList.add(uncutSignal[i] - diff);
        }
        Double[] postSignal = postSignalList.toArray(new Double[postSignalList.size()]);
        //Double[] filler = new Double[windowSize];
        //Arrays.fill(filler, 0.0);
        return ArrayUtils.addAll(preSignal, postSignal);

    }

    public Double[] cutArtifacts() {
        /*Double[] windowGrads = calcualteMeanWindowGrads();
        Integer[] artifactWindowIdxs = findAllArtifactWindows(windowGrads);
        for (int i = 0; i < artifactWindowIdxs.length; i++) {
            signal = cutWindow(signal, artifactWindowIdxs[i] - i); // every time we cut a window we shift the next window back by 1
        }

         */
        return signal;
    }

}