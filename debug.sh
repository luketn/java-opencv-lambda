
docker run \
    --name java-opencv-lambda \
    -it \
    --platform linux/arm64 \
    -v ~/.aws-lambda-rie:/aws-lambda \
    -p 9000:8080 \
    --entrypoint /aws-lambda/aws-lambda-rie \
    java-opencv-lambda \
    java -cp './*:/var/runtime/lib/*' com.amazonaws.services.lambda.runtime.api.client.AWSLambda com.mycodefu.Main::handleRequest
