package com.example.coronadiagnosticapp.ui.fragments.oxymeter;

import android.hardware.Camera;
import android.util.Log;

import com.example.coronadiagnosticapp.ui.activities.ImageProcessing;

import java.util.Queue;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;

class OxymeterThread extends Thread {
    private static final String TAG = "OxThread";
    public Oxymeter oxymeter;
    private int framesPassedToOxymeter = 0;
    private Queue<byte[]> framesQueue;
    private Camera cam;
    private Camera.Size previewSize;
    private Double samplingFrequency;
    private boolean enabled = false;
    private int totalFrames;
    private double previewFps;
    private Function1<? super Integer, Unit> onUpdateView;
    private Function2<? super Integer, ? super Double, Unit> onUpdateGraphView;
    private OxymeterThreadEventListener eventListener;

    OxymeterThread(Queue<byte[]> framesQueue,
                   Camera cam,
                   Camera.Size previewSize,
                   int totalFrames,
                   double previewFps,
                   Function1<? super Integer, Unit> onUpdateView,
                   Function2<? super Integer, ? super Double, Unit> onUpdateGraphView,
                   OxymeterThreadEventListener eventListener) {
        this.framesQueue = framesQueue;
        this.cam = cam;
        this.previewSize = previewSize;
        this.totalFrames = totalFrames;
        this.previewFps = previewFps;
        this.onUpdateView = onUpdateView;
        this.onUpdateGraphView = onUpdateGraphView;
        this.eventListener = eventListener;
    }

    private synchronized void onFingerRemoved() {
        enabled = false;
        this.oxymeter = null;
        eventListener.onFingerRemoved();
    }

    private synchronized void onInvalidData() {
        eventListener.onInvalidData();
        this.interrupt();
    }

    private synchronized void startWithNewOxymeter() {
        oxymeter = new OxymeterImpl(previewFps / 1000);
        oxymeter.setUpdateView(onUpdateView);
        oxymeter.setUpdateGraphView(onUpdateGraphView);
        oxymeter.setOnInvalidData(() -> {
            this.onInvalidData();
            return null;
        });
        enabled = true;
        framesQueue.clear();
        framesPassedToOxymeter = 0;
        eventListener.onStartWithNewOxymeter();
    }

    public void run() {
        Log.i(TAG, "Starting oxymeter thread.");
        // Keep running until we passed `totalFrames` frames to the oxymeter
        while (framesPassedToOxymeter < totalFrames) {
            if (interrupted()) {
                return;
            }
            if (!framesQueue.isEmpty()) {
                byte[] frame = framesQueue.remove();
                boolean fingerOnCamera = isFingerOnCamera(frame);
                if (enabled && !fingerOnCamera) {
                    // Should stop
                    onFingerRemoved();
                } else if (!enabled && fingerOnCamera) {
                    // Should start
                    startWithNewOxymeter();
                } else if (enabled) {
                    // Should keep goingOxymeter returned
                    passFrameToOxymeter(frame);
                }
            }
        }
        Log.i(TAG, "Finished Oxymeter!");
        eventListener.onSuccess(oxymeter);
    }

    private void passFrameToOxymeter(byte[] frame) {
        oxymeter.updateWithFrame(frame, cam);
        framesPassedToOxymeter++;
        eventListener.onFrame(framesPassedToOxymeter);
    }

    private boolean isFingerOnCamera(byte[] data) {
        double redAvg = ImageProcessing.decodeYUV420SPtoRedBlueGreenAvg(data, previewSize.height, previewSize.width, 1); //1 stands for red intensity, 2 for blue, 3 for green
        return redAvg >= 200;
    }
}
