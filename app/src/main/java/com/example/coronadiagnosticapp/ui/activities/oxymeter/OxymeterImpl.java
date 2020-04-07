package com.example.coronadiagnosticapp.ui.activities.oxymeter;

import android.hardware.Camera;
import android.util.Log;

import com.example.coronadiagnosticapp.ui.activities.ImageProcessing;
import com.example.coronadiagnosticapp.ui.activities.Math.Fft;
import com.example.coronadiagnosticapp.ui.activities.Math.Fft2;
import com.example.coronadiagnosticapp.ui.activities.OxymeterActivity;
import com.example.coronadiagnosticapp.ui.activities.SMA;
import com.opencsv.CSVWriter;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;

import static java.lang.Math.ceil;
import static java.lang.Math.sqrt;

public class OxymeterImpl implements Oxymeter {
    private static final String TAG = "Oxymeter";
    private int counter = 0;
    private static long startTime;
    private double sumred = 0;
    private double sumblue = 0;
    private double peakBpm = 0;

    //Arraylist
    private ArrayList<Double> RedAvgList = new ArrayList<>();
    private ArrayList<Double> BlueAvgList = new ArrayList<>();
    private ArrayList<Double> GreenAvgList = new ArrayList<>();

    private double SamplingFreq;
    //Initialize an object that calculates the rolling average of last 15 samples
    private SMA calc_mov_avg = new SMA(15);

    private String[] HEADER = new String[]{"id", "RED_VALUE", "RED_AVG_VALUE"};
    private List<String[]> rows = new ArrayList<String[]>(){{add(HEADER);}}; // Initializing List of string array's to generate CSV file, adding the HEADER which will be the fields inside the CSV
    private Function0<Unit> onBadFinger;

    //CSV Writing
    private String baseDir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
    private String fileName = "AnalysisData.csv";
    private String filePath = baseDir + File.separator + fileName;


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
        double RedAvg;
        double BlueAvg;
        double GreenAvg;

        RedAvg = getColorIntensities(data.clone(),height,width, OxymeterActivity.RGB.RED); // Get red intensity
        sumred = sumred + RedAvg;
        BlueAvg = getColorIntensities(data.clone(),height,width, OxymeterActivity.RGB.BLUE); // Get red intensity
        sumblue = sumblue + BlueAvg;
        GreenAvg = getColorIntensities(data.clone(),height,width, OxymeterActivity.RGB.GREEN); // Get red intensity

        RedAvgList.add(RedAvg);
        BlueAvgList.add(BlueAvg);
        GreenAvgList.add(GreenAvg);

        //To check if we got a good red intensity to process if not return to the condition and set it again until we get a good red intensity
        if (checkImageIsBad(RedAvg)) {
            Log.w(TAG, "Bad Image");
            stop();
            return;
        }
        ++counter; //countes number of frames in 30 seconds
    }

    private double[] calculateAverageFourierBreathAndBPM(Double[] Red, Double[] Green, Double[] Blue){
        double bufferAvgBr = 0;
        double bufferAvgB = 0;
        double HRFreq = Fft.FFT(Green, counter, SamplingFreq);
        double bpmGreen = (int) ceil(HRFreq * 60);
        double HR1Freq = Fft.FFT(Red, counter, SamplingFreq);
        double bpmRed = (int) ceil(HR1Freq * 60);

        double RRFreq = Fft2.FFT(Green, counter, SamplingFreq);
        double breathGreen = (int) ceil(RRFreq * 60);
        double RR1Freq = Fft2.FFT(Red, counter, SamplingFreq);
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

        return new double[]{bufferAvgBr,bufferAvgB};
    }

    private double calculateSPO2(Double[] red, Double[] blue){
        double Stdr = 0;
        double Stdb = 0;
        double meanr = sumred / counter;
        double meanb = sumblue / counter;
        for (int i = 0; i < counter - 1; i++) {
            Double bufferb = blue[i];
            Stdb = Stdb + ((bufferb - meanb) * (bufferb - meanb));
            Double bufferr = red[i];
            Stdr = Stdr + ((bufferr - meanr) * (bufferr - meanr));
        }
        double varr = sqrt(Stdr / (counter - 1));
        double varb = sqrt(Stdb / (counter - 1));

        double R = (varr / meanr) / (varb / meanb);

        double spo2 = 100 - 5 * (R);

        return spo2;
    }

    private ArrayList<Double> calculateMovingAverage(){
        ArrayList<Double> MovAvgRed = new ArrayList<Double>();
        double avg_hr = (sumred) / (RedAvgList.size());
        for (int i = 0; i < RedAvgList.size(); i++) {
            if (i < 15) {                                   //Assign the average red received to the first 15 samples
                calc_mov_avg.compute(avg_hr);               //Add the value to the moving average object
                MovAvgRed.add(i, avg_hr);                   //Add the value to the MobAvgRed list
                rows.add(new String[]{Integer.toString(i), RedAvgList.get(i).toString(), Double.toString(avg_hr)});       ///Add to CSV
            } else {
                MovAvgRed.add(calc_mov_avg.compute(RedAvgList.get(i)));
//                Log.e(TAG, "Current Average = " + calc_mov_avg.currentAverage());
                rows.add(new String[]{Integer.toString(i), RedAvgList.get(i).toString(), Double.toString(calc_mov_avg.currentAverage())});
            }
        }

        return MovAvgRed;
    }

    private ArrayList<Integer> createWindowsToFindPeaks(ArrayList<Double> MovAvgRed){
        //peakPositionList represents a list which containts the peak positions
        ArrayList<Integer> peakPositionList = new ArrayList<Integer>();
        //Create window which start's when RedAvg > MovAvg and ends when RedAvg < MovAvg and calculate's the highest point (peak) within the window
        ArrayList<Double> window = new ArrayList<Double>();
        int windowIndex = 0;
        for (int i = 0; i < RedAvgList.size(); i++) {
            if (MovAvgRed.get(i) > RedAvgList.get(i) && window.isEmpty())
                continue;
            else if (RedAvgList.get(i) > MovAvgRed.get(i)) {
                window.add(windowIndex, RedAvgList.get(i));
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

    private void FindIntervalsAndCalculateBPM(ArrayList<Integer> peakPositionList) {
        //Calculating the intervals or distance between the peaks, between then storing the result in milliseconds into RR_List ArrayList
        ArrayList<Double> RR_List = new ArrayList<Double>();
        for (int i = 0; i < peakPositionList.size() - 1; i++) {
            int RR_interval = peakPositionList.get(i + 1) - peakPositionList.get(i);
            double ms_dist = ((RR_interval / SamplingFreq) * 1000d);
            RR_List.add(ms_dist);
        }
        //Calculating the average interval
        double sumRR_List = 0;
        for (int i = 0; i < RR_List.size(); i++) {
            sumRR_List += RR_List.get(i);
        }
        double avgRR_List = (sumRR_List) / RR_List.size();

        //Providing result
        peakBpm = 60000 / (avgRR_List);
    }

    private double getColorIntensities(byte[] data, int height, int width, OxymeterActivity.RGB choice){
        if(choice == OxymeterActivity.RGB.RED)
            return  ImageProcessing.decodeYUV420SPtoRedBlueGreenAvg(data, width, height, 1); //1 stands for red intensity, 2 for blue, 3 for green
        if(choice == OxymeterActivity.RGB.BLUE)
            return ImageProcessing.decodeYUV420SPtoRedBlueGreenAvg(data, width, height, 2); //1 stands for red intensity, 2 for blue, 3 for green
        if(choice == OxymeterActivity.RGB.GREEN)
            return ImageProcessing.decodeYUV420SPtoRedBlueGreenAvg(data, width, height, 3); //1 stands for red intensity, 2 for blue, 3 for green
        return 0;
    }

    private boolean checkImageIsBad(double redIntensity){
        //Image is bad!
        return redIntensity < 200;
    }

    private void writeCSV(List<String[]> data) {
        try {
            CSVWriter writer = new CSVWriter(new FileWriter(filePath));
            writer.writeAll(data);
            writer.close();
        } catch (IOException ex) {
            Log.e(TAG, "FileWriter Exception: " + ex);
        }
    }


    @Override
    public OxymeterData finish() {
        long endTime = System.currentTimeMillis();
        double totalTimeInSecs = (endTime - startTime) / 1000d;
        SamplingFreq = (counter / totalTimeInSecs);
        ArrayList<Double> RedMoveAverage = calculateMovingAverage();
//--------------------------CSV Writing-------------------------------
        writeCSV(rows);
//--------------------------------------------------------------------
        ArrayList<Integer> peaksList = createWindowsToFindPeaks(RedMoveAverage);
        FindIntervalsAndCalculateBPM(peaksList);

        Double[] Red = RedAvgList.toArray(new Double[RedAvgList.size()]);
        Double[] Blue = BlueAvgList.toArray(new Double[BlueAvgList.size()]);
        Double[] Green = GreenAvgList.toArray(new Double[GreenAvgList.size()]);

        // double HRFreq = Fft.FFT(Red, counter, SamplingFreq);
        // double bpm = (int) ceil(HRFreq * 60);


        int o2 = (int) calculateSPO2(Red, Blue);
        //RespirationRate variable
        int breath = (int) calculateAverageFourierBreathAndBPM(Red, Green, Blue)[0]; // 0 stands for breath respiration value
        //HeartRate variables
        int beats = (int) calculateAverageFourierBreathAndBPM(Red, Green, Blue)[1]; // 0 stands for Heart Rate value


        if (!(o2 < 80 || o2 > 99) && !(beats < 45 || beats > 200) && !(peakBpm < 45 || peakBpm > 200)) {
            int BpmAvg = (int) ceil((beats + peakBpm) / 2);
            return new OxymeterData(o2, BpmAvg , breath);
        } else if (!(o2 < 80 || o2 > 99) && (beats < 45 || beats > 200) && !(peakBpm < 45 || peakBpm > 200)) {
            return new OxymeterData(o2, (int) peakBpm , breath);
        } else if (!(o2 < 80 || o2 > 99) && !(beats < 45 || beats > 200) && (peakBpm < 45 || peakBpm > 200)) {
            return new OxymeterData(o2, beats, breath);
        } else  {
            return null;
        }
    }

    private void stop() {
        // Invokes the onStop callback
        if (onBadFinger != null)
            onBadFinger.invoke();
    }

    @Override
    public void setOnBadFinger(@NotNull Function0<Unit> callback) {
        onBadFinger = callback;
    }
}
