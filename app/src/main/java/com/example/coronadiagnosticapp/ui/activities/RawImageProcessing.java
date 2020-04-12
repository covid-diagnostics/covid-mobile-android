package com.example.coronadiagnosticapp.ui.activities;


import android.hardware.camera2.CameraCharacteristics;
import android.media.Image;

import java.nio.ShortBuffer;

public abstract class RawImageProcessing {


    /**
     * Given a byte array representing a yuv420sp image, determine the average
     * amount of red in the image. Note: returns 0 if the byte array is NULL.
     *
     * @param img    Raw Image object
     * @param height Height of the image.
     * @param width  Width of the image.
     * @param bayer  the bayer filter arrangement
     * @return int representing the average amount of red in the image.
     */
    public static Double[] decodeCentralSquareInRawImage(Image img, int height, int width, int bayer) {
        double totRed = 0;
        double totBlue = 0;
        double squareSize = 50; // Half the length of a side of square, 50 for a 100x100 square

        ShortBuffer data = img.getPlanes()[0].getBuffer().asShortBuffer();
        int startHeight = (int) (height / 2.0 - squareSize);
        int startWidth = (int) (width / 2.0 - squareSize);


        switch (bayer) {
            case CameraCharacteristics.SENSOR_INFO_COLOR_FILTER_ARRANGEMENT_BGGR:
                for (int i = startHeight; i < startHeight + squareSize; i = i + 2) {
                    for (int j = startWidth; j < startWidth + squareSize; j = j + 2) {
                        totBlue += data.get(i * width + j);
                        totRed += data.get((i + 1) * width + j + 1);
                    }
                }
                break;
            case CameraCharacteristics.SENSOR_INFO_COLOR_FILTER_ARRANGEMENT_GBRG:
                for (int i = startHeight; i < startHeight + squareSize; i = i + 2) {
                    for (int j = startWidth; j < startWidth + squareSize; j = j + 2) {
                        totBlue += data.get(i * width + j + 1);
                        totRed += data.get((i + 1) * width + j);
                    }
                }
                break;
            case CameraCharacteristics.SENSOR_INFO_COLOR_FILTER_ARRANGEMENT_GRBG:
                for (int i = startHeight; i < startHeight + squareSize; i = i + 2) {
                    for (int j = startWidth; j < startWidth + squareSize; j = j + 2) {
                        totRed += data.get(i * width + j + 1);
                        totBlue += data.get((i + 1) * width + j);
                    }
                }
                break;
            case CameraCharacteristics.SENSOR_INFO_COLOR_FILTER_ARRANGEMENT_RGGB:
                for (int i = startHeight; i < startHeight + squareSize; i = i + 2) {
                    for (int j = startWidth; j < startWidth + squareSize; j = j + 2) {
                        totRed += data.get(i * width + j);
                        totBlue += data.get((i + 1) * width + j + 1);
                    }
                }
                break;
        }

        double avgRed = totRed / (squareSize * squareSize * 4);
        double avgBlue = totBlue / (squareSize * squareSize * 4);

        return new Double[]{
                avgRed, avgBlue
        };

    }
}
