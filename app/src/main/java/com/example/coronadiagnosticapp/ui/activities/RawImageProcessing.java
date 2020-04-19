package com.example.coronadiagnosticapp.ui.activities;


import android.hardware.camera2.CameraCharacteristics;
import android.media.Image;

import java.nio.ShortBuffer;

public abstract class RawImageProcessing {


    /**
     * Given a raw image, calculate the average RGB.
     *
     * @param img    Raw Image object
     * @param height Height of the image.
     * @param width  Width of the image.
     * @param bayer  the bayer filter arrangement
     * @return three doubles, the averages of red, green, and blue.
     */
    public static Double[] decodeCentralSquareInRawImage(Image img, int height, int width, int bayer) {
        // Optimization idea for ancient/cheap 32 bit phones: Use int instead of long.
        // Make sure that height * width * CameraCharacteristics.SENSOR_INFO_WHITE_LEVEL
        // does not cause a integer overflow.
        long totRed = 0;
        long totGreen = 0;
        long totBlue = 0;
        int squareSize = 300; // Half the length of a side of square, 50 for a 100x100 square

        ShortBuffer data = img.getPlanes()[0].getBuffer().asShortBuffer();
        int startHeight = (int) (height / 2.0 - squareSize);
        int startWidth = (int) (width / 2.0 - squareSize);

//        Log.e("RawImageProcessing",
//            "RAW 2x2 center pixel (bayer=" + bayer + "): [" +
//                    data.get((height / 2) * width + (width / 2)) + ", " +
//                    data.get((height / 2) * width + (width / 2) + 1) + ", " +
//                    data.get(((height / 2) + 1) * width + (width / 2)) + ", " +
//                    data.get(((height / 2) + 1) * width + (width / 2) + 1) + "],");

        switch (bayer) {
            case CameraCharacteristics.SENSOR_INFO_COLOR_FILTER_ARRANGEMENT_BGGR:
                for (int i = startHeight; i < startHeight + squareSize; i = i + 2) {
                    for (int j = startWidth; j < startWidth + squareSize; j = j + 2) {
                        totBlue += data.get(i * width + j);
                        totGreen += data.get(i * width + j + 1);
                        totRed += data.get((i + 1) * width + j + 1);
                    }
                }
                break;
            case CameraCharacteristics.SENSOR_INFO_COLOR_FILTER_ARRANGEMENT_GBRG:
                for (int i = startHeight; i < startHeight + squareSize; i = i + 2) {
                    for (int j = startWidth; j < startWidth + squareSize; j = j + 2) {
                        totGreen += data.get(i * width + j);
                        totBlue += data.get(i * width + j + 1);
                        totRed += data.get((i + 1) * width + j);
                    }
                }
                break;
            case CameraCharacteristics.SENSOR_INFO_COLOR_FILTER_ARRANGEMENT_GRBG:
                for (int i = startHeight; i < startHeight + squareSize; i = i + 2) {
                    for (int j = startWidth; j < startWidth + squareSize; j = j + 2) {
                        totGreen += data.get(i * width + j);
                        totRed += data.get(i * width + j + 1);
                        totBlue += data.get((i + 1) * width + j);
                    }
                }
                break;
            case CameraCharacteristics.SENSOR_INFO_COLOR_FILTER_ARRANGEMENT_RGGB:
                for (int i = startHeight; i < startHeight + squareSize; i = i + 2) {
                    for (int j = startWidth; j < startWidth + squareSize; j = j + 2) {
                        totRed += data.get(i * width + j);
                        totGreen += data.get(i * width + j + 1);
                        totBlue += data.get((i + 1) * width + j + 1);
                    }
                }
                break;
            default:
                throw new AssertionError("Unexpected raw sensor arrangement.");
        }

        double avgRed = totRed / (squareSize * squareSize / 4.);
        double avgGreen = totGreen / (squareSize * squareSize / 4.);
        double avgBlue = totBlue / (squareSize * squareSize / 4.);

        return new Double[]{avgRed, avgGreen, avgBlue};
    }
}
