set -e

mvn clean package --activate-profiles linux-aarch64
rm -rf ./temp-build
mkdir temp-build
cp target/java-opencv-lambda.jar ./temp-build/
mv ./temp-build/java-opencv-lambda.jar ./temp-build/java-opencv-lambda.zip
cd temp-build
unzip java-opencv-lambda.zip
rm java-opencv-lambda.zip

cd ..
rm -rf ./docker/code
mkdir -p ./docker/code
cp -r ./temp-build/* ./docker/code/
rm -rf ./temp-build

cd docker
docker build -t java-opencv-lambda .

if [ -z "$DOCKER_TAG" ]; then
  echo "DOCKER_TAG is not set. Exiting..."
  exit 1
fi

aws ecr --profile personal get-login-password --region ap-southeast-2  | docker login --username AWS --password-stdin 204244381428.dkr.ecr.ap-southeast-2.amazonaws.com
REPOSITORY_TAG="204244381428.dkr.ecr.ap-southeast-2.amazonaws.com/java-opencv-lambda:${DOCKER_TAG}"
REPOSITORY_TAG_LATEST="204244381428.dkr.ecr.ap-southeast-2.amazonaws.com/java-opencv-lambda:latest"
echo "$(date): Building and pushing ${REPOSITORY_TAG}..."
docker buildx build --push -t "${REPOSITORY_TAG}" -t "${REPOSITORY_TAG_LATEST}" --platform linux/arm64 .
