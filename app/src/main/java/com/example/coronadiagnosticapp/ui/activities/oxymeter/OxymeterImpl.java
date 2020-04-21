package com.example.coronadiagnosticapp.ui.activities.oxymeter;

import android.hardware.Camera;

import com.example.coronadiagnosticapp.ui.activities.ImageProcessing;
import com.example.coronadiagnosticapp.ui.activities.Math.Fft;
import com.example.coronadiagnosticapp.ui.activities.Math.Fft2;
import com.example.coronadiagnosticapp.ui.activities.SMA;

import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;

import static java.lang.Math.ceil;
import static java.lang.Math.sqrt;

public class OxymeterImpl implements Oxymeter {
    public enum RGB {RED, GREEN, BLUE}

    private static final String TAG = "Oxymeter";
    private static long startTime;

    // window samples
    private final int WINDOW_TIME = 10; // the number of seconds for each window
    private final int FAILED_WINDOWS_MAX = 5; // the number of bad windows we allow to "throw away"
    private double RedTotalAvg;
    private SMA RedRollingAvg = null;
    final private int SMA_SIZE = 15;

    //Arraylist
    private ArrayList<Double> RedAvgList = new ArrayList<>();
    private ArrayList<Double> BlueAvgList = new ArrayList<>();
    private ArrayList<Double> GreenAvgList = new ArrayList<>();

    //Initialize an object that calculates the rolling average of last 15 samples
    private SMA calc_mov_avg = new SMA(15);

    @Inject
    public OxymeterImpl() {
        startTime = System.currentTimeMillis();
    }

    @Override
    public void updateWithFrame(@NotNull byte[] data, @NotNull Camera cam) {
        Camera.Size size = cam.getParameters().getPreviewSize();
        if (size == null) throw new NullPointerException();
        //put width + height of the camera inside the variables
        int width = size.width;
        int height = size.height;
        // Get color intensity
        double RedAvg = getColorIntensities(data.clone(), height, width, RGB.RED);
        double BlueAvg = getColorIntensities(data.clone(), height, width, RGB.BLUE);
        double GreenAvg = getColorIntensities(data.clone(), height, width, RGB.GREEN);

        RedAvgList.add(RedAvg);
        BlueAvgList.add(BlueAvg);
        GreenAvgList.add(GreenAvg);
    }

    @Override
    public OxymeterData finish(double samplingFreq) {
        RedTotalAvg = sumDouble(RedAvgList) / RedAvgList.size();
        int[] peakBpmAndO2 = calculateByWindowsBpmAndO2(RedAvgList, BlueAvgList, samplingFreq, WINDOW_TIME, FAILED_WINDOWS_MAX);
        int o2 = peakBpmAndO2[0];
        int peakBpm = peakBpmAndO2[1];
        int failed = peakBpmAndO2[2];
        if (failed == 1) {
            return null;
        }

        double[] breathAndBPM = calculateAverageFourierBreathAndBPM(RedAvgList, GreenAvgList, samplingFreq);
        int Breath = (int) breathAndBPM[0]; // 0 stands for breath respiration value
        double Beats = breathAndBPM[1]; // 1 stands for Heart Rate value
        // Calculate final result
        if (!(o2 < 80 || o2 > 99) && !(Beats < 45 || Beats > 200) && !(peakBpm < 45 || peakBpm > 200)) {
            double BpmAvg = ceil((Beats + peakBpm) / 2);
            return new OxymeterData(o2, (int) BpmAvg, Breath);
        } else if (!(o2 < 80 || o2 > 99) && (Beats < 45 || Beats > 200) && !(peakBpm < 45 || peakBpm > 200)) {
            return new OxymeterData(o2, peakBpm, Breath);
        } else if (!(o2 < 80 || o2 > 99) && !(Beats < 45 || Beats > 200) && (peakBpm < 45 || peakBpm > 200)) {
            return new OxymeterData(o2, (int) Beats, Breath);
        } else {
            return null;
        }
    }

    public double[] calculateAverageFourierBreathAndBPM(ArrayList<Double> RedList, ArrayList<Double> GreenList, double samplingFreq) {
        double bufferAvgBr = 0;
        double bufferAvgB = 0;
        Double[] Red = RedList.toArray(new Double[RedList.size()]);
        Double[] Green = GreenList.toArray(new Double[GreenList.size()]);
        double HRFreq = Fft.FFT(Green, Green.length, samplingFreq);
        double bpmGreen = (int) ceil(HRFreq * 60);
        double HR1Freq = Fft.FFT(Red, Red.length, samplingFreq);
        double bpmRed = (int) ceil(HR1Freq * 60);

        double RRFreq = Fft2.FFT(Green, Green.length, samplingFreq);
        double breathGreen = (int) ceil(RRFreq * 60);
        double RR1Freq = Fft2.FFT(Red, Red.length, samplingFreq);
        double breathRed = (int) ceil(RR1Freq * 60);
        if ((bpmGreen > 40 && bpmGreen < 200) || (breathGreen > 6 && breathGreen < 20)) {
            if ((bpmRed > 40 && bpmRed < 200) || (breathRed > 6 && breathRed < 24)) {
                bufferAvgB = (bpmGreen + bpmRed) / 2;
                bufferAvgBr = (breathGreen + breathRed) / 2;
            } else {
                bufferAvgB = bpmGreen;
                bufferAvgBr = breathGreen;
            }
        } else if ((bpmRed > 45 && bpmRed < 200) || (breathRed > 10 && breathRed < 20)) {
            bufferAvgB = bpmRed;
            bufferAvgBr = breathRed;
        }
        return new double[]{bufferAvgBr, bufferAvgB};
    }

    public double calculateSPO2(ArrayList<Double> red, ArrayList<Double> blue) {
        double Stdr = 0;
        double Stdb = 0;
        double meanr = sumDouble(red) / red.size();
        double meanb = sumDouble(blue) / blue.size();
        for (int i = 0; i < red.size() - 1; i++) {
            Double bufferb = blue.get(i);
            Stdb = Stdb + ((bufferb - meanb) * (bufferb - meanb));
            Double bufferr = red.get(i);
            Stdr = Stdr + ((bufferr - meanr) * (bufferr - meanr));
        }
        double varr = sqrt(Stdr / (red.size() - 1));
        double varb = sqrt(Stdb / (red.size() - 1));

        double R = (varr / meanr) / (varb / meanb);

        double spo2 = 100 - 5 * (R);

        return spo2;
    }

    public ArrayList<Double> calculateMovingRedWindowAverage(ArrayList<Double> list) {
        ArrayList<Double> MovAvgRed = new ArrayList<Double>();
        if (RedRollingAvg == null) {
            //Initialize an object that calculates the rolling average of last `SMA_SIZE` samples
            RedRollingAvg = new SMA(SMA_SIZE);
            for (int i = 0; i < list.size(); i++) {
                if (i < SMA_SIZE) {                                   //Assign the average red received to the first `SMA_SIZE` samples
                    RedRollingAvg.compute(RedTotalAvg);               //Add the value to the moving average object
                    MovAvgRed.add(i, RedTotalAvg);                   //Add the value to the MobAvgRed list
                } else {
                    MovAvgRed.add(RedRollingAvg.compute(list.get(i)));
                }
            }
        } else {
            for (int i = 0; i < list.size(); i++) {
                MovAvgRed.add(RedRollingAvg.compute(list.get(i)));
            }
        }
        return MovAvgRed;
    }

    private ArrayList<Integer> createWindowsToFindPeaks(ArrayList<Double> MovAvgRed, ArrayList<Double> list) {
        //peakPositionList represents a list which containts the peak positions
        ArrayList<Integer> peakPositionList = new ArrayList<Integer>();
        //Create window which start's when RedAvg > MovAvg and ends when RedAvg < MovAvg and calculate's the highest point (peak) within the window
        ArrayList<Double> window = new ArrayList<Double>();
        int windowIndex = 0;
        for (int i = 0; i < list.size(); i++) {
            if (MovAvgRed.get(i) > list.get(i) && window.isEmpty())
                continue;
            else if (list.get(i) > MovAvgRed.get(i)) {
                window.add(windowIndex, list.get(i));
                windowIndex++;
            } else {
                if (window.isEmpty())
                    continue;
                //Finding the maximum value within the window calculating it's position then storing it at peakPositionList variable
                Object maximum = Collections.max(window);
                int beatposition = i - window.size() + window.indexOf(maximum);
                peakPositionList.add(beatposition);
                window.clear();
                windowIndex = 0;
            }
        }
        return peakPositionList; //Return list of peak positions
    }

    private double findIntervalsAndCalculateBPM(ArrayList<Integer> peakPositionList, double samplingFreq) {
        //Calculating the intervals or distance between the peaks, between then storing the result in milliseconds into RR_List ArrayList
        ArrayList<Double> RR_List = new ArrayList<Double>();
        for (int i = 0; i < peakPositionList.size() - 1; i++) {
            int RR_interval = peakPositionList.get(i + 1) - peakPositionList.get(i);
            double ms_dist = ((RR_interval / samplingFreq) * 1000d);
            RR_List.add(ms_dist);
        }
        //Calculating the average interval
        double sumRR_List = 0;
        for (int i = 0; i < RR_List.size(); i++) {
            sumRR_List += RR_List.get(i);
        }
        double avgRR_List = (sumRR_List) / RR_List.size();
        //Providing result
        return 60000 / (avgRR_List);
    }

    private double getColorIntensities(byte[] data, int height, int width, RGB choice) {
        if (choice == RGB.RED)
            return ImageProcessing.decodeYUV420SPtoRedBlueGreenAvg(data, height, width, 1); //1 stands for red intensity, 2 for blue, 3 for green
        if (choice == RGB.BLUE)
            return ImageProcessing.decodeYUV420SPtoRedBlueGreenAvg(data, height, width, 2); //1 stands for red intensity, 2 for blue, 3 for green
        if (choice == RGB.GREEN)
            return ImageProcessing.decodeYUV420SPtoRedBlueGreenAvg(data, height, width, 3); //1 stands for red intensity, 2 for blue, 3 for green
        return 0;
    }

    private int[] calculateByWindowsBpmAndO2(ArrayList<Double> redList, ArrayList<Double> blueList, double samplingFreq, int window_time, int failed_windows_max) {
        final double WINDOW_FRAMES = samplingFreq * window_time;
        double[] results;
        double[] o2 = new double[30 - window_time + 1];
        double[] peakBpm = new double[30 - window_time + 1];
        int failed = 0;
        for (int i = 0; i <= 30 - window_time; i++) {
            int from = (int) (samplingFreq * i);
            int to = (int) (WINDOW_FRAMES + (int) (samplingFreq * i));
            results = calculateWindowSampleBpmAndO2(
                    new ArrayList(redList.subList(from, to)),
                    new ArrayList(blueList.subList(from, to)),
                    samplingFreq);
            o2[i] = results[0];     // if failed the result is 0
            peakBpm[i] = results[1];// if failed the result is 0
            failed += (int) results[2];
        }
        if (failed > failed_windows_max) { // too many failed windows, the samples are bad
            return new int[]{0, 0, 1};
        } else {
            return new int[]{
                    (int) (sumDouble(Arrays.asList(ArrayUtils.toObject(o2))) / (o2.length - failed)),
                    (int) (sumDouble(Arrays.asList(ArrayUtils.toObject(peakBpm))) / (peakBpm.length - failed)),
                    0};
        }
    }

    private double[] calculateWindowSampleBpmAndO2(ArrayList<Double> redList, ArrayList<Double> blueList, double samplingFreq) {
        ArrayList<Double> RedMoveAverage = calculateMovingRedWindowAverage(redList);
        ArrayList<Integer> peaksList = createWindowsToFindPeaks(RedMoveAverage, redList);
        double peakBpm = findIntervalsAndCalculateBPM(peaksList, samplingFreq);
        double o2 = calculateSPO2(redList, blueList);
        int peakBpmi = (int) peakBpm, o2i = (int) o2;
        // Calculate final result
        if (!(o2i < 80 || o2i > 99) && !(peakBpmi < 45 || peakBpmi > 200)) { // if any of the measurements is bad, the windows is bad sample
            return new double[]{o2, peakBpm, 0};
        }
        // this is a bad measurement
        RedRollingAvg = null;
        return new double[]{0, 0, 1};
    }

    private double sumDouble(List<Double> list) {
        double sum = 0;
        for (Double d : list)
            sum += d;
        return sum;
    }
}
