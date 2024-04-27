
# why use JNI not the new Foreign Function & Memory API?
I looked at that at first, but read that it may not be ready for C++
usage yet:


# Building Java Docker lambdas
https://docs.aws.amazon.com/lambda/latest/dg/java-image.html
https://docs.aws.amazon.com/lambda/latest/dg/images-create.html

Repo for lambda java image:
https://gallery.ecr.aws/lambda/java

# using snapstart
https://aws.amazon.com/blogs/compute/re-platforming-java-applications-using-the-updated-aws-serverless-java-container/

# Building OpenCV with Java JNI bindings:
https://delabassee.com/OpenCVJava/
(if you don't want to use something like this prebundled Maven dep)

# interesting ways to run existing java frameworks in lambda
https://github.com/aws/serverless-java-container/tree/main
