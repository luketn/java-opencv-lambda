package com.mycodefu;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import nu.pattern.OpenCV;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.Base64;
import java.util.Map;

import static org.opencv.imgcodecs.Imgcodecs.imdecode;
import static org.opencv.imgcodecs.Imgcodecs.imread;

public class Main implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {
    static {
        OpenCV.loadLocally();
    }

    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent apiGatewayV2HTTPEvent, Context context) {
//        System.out.println("Received request:\n" + apiGatewayV2HTTPEvent);

        // Decode the base64 string to get image data
        byte[] imageData = Base64.getDecoder().decode(apiGatewayV2HTTPEvent.getBody());

        // Use OpenCV's imdecode() to convert byte array to a Mat object
        Mat image = Imgcodecs.imdecode(new MatOfByte(imageData), Imgcodecs.IMREAD_COLOR);

        // You can process the image here as needed
        Mat grayImage = new Mat();
        Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);

        // For the purpose of this example, let's convert the image back to a base64 string and return it
        MatOfByte matOfByte = new MatOfByte();
        Imgcodecs.imencode(".png", grayImage, matOfByte);
        String encodedImage = Base64.getEncoder().encodeToString(matOfByte.toArray());

        return APIGatewayV2HTTPResponse.builder()
                .withStatusCode(200)
                .withBody(encodedImage)
                .withIsBase64Encoded(true)
                .withHeaders(Map.of("Content-Type", "image/png"))
                .build();
    }
}