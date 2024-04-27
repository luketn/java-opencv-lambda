package com.mycodefu;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import nu.pattern.OpenCV;

public class Main implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {
    static {
        OpenCV.loadLocally();
    }

    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent apiGatewayV2HTTPEvent, Context context) {
        System.out.println("Received request:\n" + apiGatewayV2HTTPEvent);
        return APIGatewayV2HTTPResponse.builder().withStatusCode(200).withBody(apiGatewayV2HTTPEvent.getBody()).build();
    }
}