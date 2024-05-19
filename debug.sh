echo "Running the Docker image with RIE for local lambda API Gateway debugging..."
docker kill java-opencv-lambda || true
docker rm java-opencv-lambda || true

# ref: https://github.com/aws/aws-lambda-runtime-interface-emulator?tab=readme-ov-file#installing
docker run \
    --name java-opencv-lambda \
    -it \
    --platform linux/arm64 \
    -v ~/.aws-lambda-rie:/aws-lambda \
    -p 9000:8080 \
    --entrypoint /aws-lambda/aws-lambda-rie \
    java-opencv-lambda \
    java -cp './*:/var/runtime/lib/*' com.amazonaws.services.lambda.runtime.api.client.AWSLambda com.mycodefu.Main::handleRequest
