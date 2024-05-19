package com.mycodefu;

import nu.pattern.OpenCV;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class ImageProcessor {
    static {
        OpenCV.loadLocally();
    }

    public enum Mode {
        Grayscale,
        GreenBlueRedSplit,
        FaceDetection,
        Mandarin,
    }

    public static String processImage(String encodedInputImage, Mode mode) {
        byte[] imageData = Base64.getDecoder().decode(encodedInputImage);
        Mat image = Imgcodecs.imdecode(new MatOfByte(imageData), Imgcodecs.IMREAD_COLOR);

        Mat result = switch (mode) {
            case Grayscale -> processGrayscale(image);
            case GreenBlueRedSplit -> processGreenBlueRedSplit(image);
            case FaceDetection -> processFaceDetection(image);
            case Mandarin -> processMandarin(image);
        };

        MatOfByte matOfByte = new MatOfByte();
        Imgcodecs.imencode(".png", result, matOfByte);
        String encodedImage = Base64.getEncoder().encodeToString(matOfByte.toArray());
        return encodedImage;
    }

    private static Mat processMandarin(Mat image) {
        // Convert the image to the HSV color space
        Mat hsvImage = new Mat();
        Imgproc.cvtColor(image, hsvImage, Imgproc.COLOR_BGR2HSV);

        // Define a narrow range of orange color in HSV
        Scalar lowerOrange = new Scalar(8, 150, 100);
        Scalar upperOrange = new Scalar(18, 255, 255);

        // Threshold the HSV image to get only orange colors
        Mat mask = new Mat();
        Core.inRange(hsvImage, lowerOrange, upperOrange, mask);

        // Create a copy of the mask before applying morphology
        Mat maskBeforeMorphology = mask.clone();

        // Perform morphological operations to remove noise
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(5, 5));
        Imgproc.morphologyEx(mask, mask, Imgproc.MORPH_OPEN, kernel);
        Imgproc.morphologyEx(mask, mask, Imgproc.MORPH_CLOSE, kernel);

        // Find contours of the mask
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(mask, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        // Find the largest contour (assuming the mandarin is the largest orange object)
        double maxArea = 0;
        MatOfPoint largestContour = null;
        for (MatOfPoint contour : contours) {
            double area = Imgproc.contourArea(contour);
            if (area > maxArea) {
                    maxArea = area;
                    largestContour = contour;
            }
        }

        // If a contour is found, draw a circle around it
        Point center = new Point();
        if (largestContour != null) {
            // Find the minimum enclosing circle of the largest contour
            MatOfPoint2f contour2f = new MatOfPoint2f(largestContour.toArray());
            float[] radius = new float[1];
            Imgproc.minEnclosingCircle(contour2f, center, radius);

            // Draw the circle on the original image
            Scalar green = new Scalar(0, 255, 0);
            Imgproc.circle(image, center, (int) radius[0], green, 2);
        }



        // Draw the legend at the top left of the image
        int legendWidth = 100;
        int legendHeight = 10;
        int legendX = 5;
        int legendY = 5;

        // Create a new Mat for the legend
        Mat legend = new Mat(legendHeight, legendWidth, CvType.CV_8UC3, new Scalar(255, 255, 255));

        // Fill the legend with the range of orange colors
        for (int i = 0; i < legendWidth; i++) {
            double hue = lowerOrange.val[0] + (upperOrange.val[0] - lowerOrange.val[0]) * i / legendWidth;
            Scalar color = new Scalar(hue, 255, 255);
            Imgproc.line(legend, new Point(i, 0), new Point(i, legendHeight), color, 1);
        }

        // Convert the legend from HSV to BGR
        Imgproc.cvtColor(legend, legend, Imgproc.COLOR_HSV2BGR);

        // Add "Min" and "Max" text to the legend
        if (largestContour != null) {
            Scalar white = new Scalar(230, 230, 200);
            String text = "x: " + (int) center.x + ", y: " + (int) center.y;
            Imgproc.putText(legend, text, new Point(5, 8), Imgproc.FONT_HERSHEY_SIMPLEX, 0.3, white, 1, Imgproc.LINE_AA);
        }
        // Overlay the legend on the original image
        Mat legendAreaOfImage = image.submat(new Rect(legendX, legendY, legendWidth, legendHeight));
        legend.copyTo(legendAreaOfImage);



        // Create a 4-column wide image
        Mat result = new Mat(image.rows(), image.cols() * 4, CvType.CV_8UC3);

        // Place the current image on the left
        Mat firstPanel = result.submat(new Rect(0, 0, image.cols(), image.rows()));
        image.copyTo(firstPanel);

        // Place the mask before morphology in the second panel
        Mat maskBeforeMorphologyColorized = new Mat();
        Imgproc.cvtColor(maskBeforeMorphology, maskBeforeMorphologyColorized, Imgproc.COLOR_GRAY2BGR);
        Mat secondPanel = result.submat(new Rect(image.cols(), 0, image.cols(), image.rows()));
        maskBeforeMorphologyColorized.copyTo(secondPanel);

        // Add title "Mask Before Morphology" to the second panel
        Scalar white = new Scalar(230, 230, 200);
        Imgproc.putText(result, "Orange Mask (Before Morphology)", new Point(image.cols() + 5, 15),
                Imgproc.FONT_HERSHEY_SIMPLEX, 0.4, white, 1, Imgproc.LINE_AA);

        // Place the orange mask in the third panel
        Mat maskColorized = new Mat();
        Imgproc.cvtColor(mask, maskColorized, Imgproc.COLOR_GRAY2BGR);
        Mat thirdPanel = result.submat(new Rect(image.cols() * 2, 0, image.cols(), image.rows()));
        maskColorized.copyTo(thirdPanel);

        // Add title "Orange Mask" to the third panel
        Imgproc.putText(result, "Orange Mask", new Point(image.cols() * 2 + 5, 15),
                Imgproc.FONT_HERSHEY_SIMPLEX, 0.4, white, 1, Imgproc.LINE_AA);

        // Place the contours detected on the right (fourth panel)
        Mat contoursImage = Mat.zeros(image.size(), CvType.CV_8UC3);
        Scalar orange = new Scalar(0, 165, 255);
        for (MatOfPoint contour : contours) {
            if (contour == largestContour) {
                Imgproc.drawContours(contoursImage, Collections.singletonList(contour), -1, orange, 2);
            } else {
                Imgproc.drawContours(contoursImage, Collections.singletonList(contour), -1, white, 2);
            }
        }
        Mat fourthPanel = result.submat(new Rect(image.cols() * 3, 0, image.cols(), image.rows()));
        contoursImage.copyTo(fourthPanel);

        // Add title "Contours Found" to the fourth panel
        Imgproc.putText(result, "Contours Found", new Point(image.cols() * 3 + 5, 15),
                Imgproc.FONT_HERSHEY_SIMPLEX, 0.4, white, 1, Imgproc.LINE_AA);


        return result;
    }

    /**
     * Detect faces in an image using a Haar Cascade Classifier.
     * Ref: https://docs.opencv.org/4.9.0/dc/d88/tutorial_traincascade.html
     */
    private static Mat processFaceDetection(Mat image) {
        CascadeClassifier faceCascade = getFaceClassifier();

        MatOfRect faceDetections = new MatOfRect();
        faceCascade.detectMultiScale(image, faceDetections);

        int thickness = 3;
        Scalar magenta = new Scalar(255, 0, 255);
        for (Rect rect : faceDetections.toArray()) {
            Imgproc.rectangle(image, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), magenta, thickness);
        }

        return image;
    }

    private static CascadeClassifier FACE_CLASSIFIER = null;
    private static CascadeClassifier getFaceClassifier() {
        if (FACE_CLASSIFIER == null) {
            String tempPath = System.getProperty("java.io.tmpdir");
            Path faceCascadePath = Path.of(tempPath, "haarcascade_frontalface_default.xml");
            if (!faceCascadePath.toFile().exists()) {
                //Copy the file to the temp path
                try (InputStream is = ImageProcessor.class.getResourceAsStream("/haarcascade_frontalface_default.xml")) {
                    Files.copy(is, faceCascadePath);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            CascadeClassifier faceCascade = new CascadeClassifier();
            faceCascade.load(faceCascadePath.toString());
            FACE_CLASSIFIER = faceCascade;
        }
        return FACE_CLASSIFIER;
    }

    /**
     * OpenCV splits the image into channels in BGR order.
     * This method creates a new image with the blue, green, and red channels side by side.
     */
    private static Mat processGreenBlueRedSplit(Mat image) {
        int rows = image.rows();
        int cols = image.cols();

        // Get a Mat for each channel
        Mat blueChannel = new Mat();
        Core.extractChannel(image, blueChannel, 0);
        Mat greenChannel = new Mat();
        Core.extractChannel(image, greenChannel, 1);
        Mat redChannel = new Mat();
        Core.extractChannel(image, redChannel, 2);

        // Create a combined image 3x as wide copy each channel side by side (grayscale)
        Mat combinedImage = new Mat(rows, cols * 3, CvType.CV_8UC1);
        Mat roi1 = combinedImage.submat(new Rect(0, 0, cols, rows));
        blueChannel.copyTo(roi1);
        Mat roi2 = combinedImage.submat(new Rect(cols, 0, cols, rows));
        greenChannel.copyTo(roi2);
        Mat roi3 = combinedImage.submat(new Rect(cols * 2, 0, cols, rows));
        redChannel.copyTo(roi3);

        // Add text labels to the top left of each color channel
        Scalar color = new Scalar(255, 255, 255); // White color for text
        int fontFace = Imgproc.FONT_HERSHEY_TRIPLEX;
        double fontScale = 1.0;
        int thickness = 2;
        Point org1 = new Point(10, 30);
        Point org2 = new Point(cols + 10, 30);
        Point org3 = new Point(cols * 2 + 10, 30);

        Imgproc.putText(combinedImage, "Blue", org1, fontFace, fontScale, color, thickness);
        Imgproc.putText(combinedImage, "Green", org2, fontFace, fontScale, color, thickness);
        Imgproc.putText(combinedImage, "Red", org3, fontFace, fontScale, color, thickness);

        return combinedImage;
    }

    private static Mat processGrayscale(Mat image) {
        Mat grayImage = new Mat();
        Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);
        return grayImage;
    }
}
