mvn clean package
rm -rf ./temp-build
mkdir temp-build
cp target/java-opencv-lambda.jar ./temp-build/
mv ./temp-build/java-opencv-lambda.jar ./temp-build/java-opencv-lambda.zip
cd temp-build
unzip java-opencv-lambda.zip
rm java-opencv-lambda.zip

cd ..
mkdir -p ./docker/code
cp -r ./temp-build/* ./docker/code/
rm -rf ./temp-build

cd docker
docker build -t java-opencv-lambda .
