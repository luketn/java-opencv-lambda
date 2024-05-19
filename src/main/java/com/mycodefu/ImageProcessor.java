package com.mycodefu;

import nu.pattern.OpenCV;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.Base64;

public class ImageProcessor {
    static {
        OpenCV.loadLocally();
    }

    public static String processImage(String encodedInputImage) {
        // Decode the base64 string to get image data
        byte[] imageData = Base64.getDecoder().decode(encodedInputImage);

        // Use OpenCV's imdecode() to convert byte array to a Mat object
        Mat image = Imgcodecs.imdecode(new MatOfByte(imageData), Imgcodecs.IMREAD_COLOR);



        // You can process the image here as needed
        Mat grayImage = new Mat();
        Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);

        // For the purpose of this example, let's convert the image back to a base64 string and return it
        MatOfByte matOfByte = new MatOfByte();
        Imgcodecs.imencode(".png", grayImage, matOfByte);
        String encodedImage = Base64.getEncoder().encodeToString(matOfByte.toArray());
        return encodedImage;
    }
}
