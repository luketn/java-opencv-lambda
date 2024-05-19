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
import java.util.Base64;

public class ImageProcessor {
    static {
        OpenCV.loadLocally();
    }

    public enum Mode {
        Grayscale,
        GreenBlueRedSplit,
        FaceDetection,
    }

    public static String processImage(String encodedInputImage, Mode mode) {
        byte[] imageData = Base64.getDecoder().decode(encodedInputImage);
        Mat image = Imgcodecs.imdecode(new MatOfByte(imageData), Imgcodecs.IMREAD_COLOR);

        Mat result = switch (mode) {
            case Grayscale -> processGrayscale(image);
            case GreenBlueRedSplit -> processGreenBlueRedSplit(image);
            case FaceDetection -> processFaceDetection(image);
        };

        MatOfByte matOfByte = new MatOfByte();
        Imgcodecs.imencode(".png", result, matOfByte);
        String encodedImage = Base64.getEncoder().encodeToString(matOfByte.toArray());
        return encodedImage;
    }

    /**
     * Detect faces in an image using a Haar Cascade Classifier.
     * Ref: https://docs.opencv.org/4.9.0/dc/d88/tutorial_traincascade.html
     */
    private static Mat processFaceDetection(Mat image) {
        CascadeClassifier faceCascade = getFaceClassifier();

        MatOfRect faceDetections = new MatOfRect();
        faceCascade.detectMultiScale(image, faceDetections);

        for (Rect rect : faceDetections.toArray()) {
            Imgproc.rectangle(image, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 255, 0));
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
