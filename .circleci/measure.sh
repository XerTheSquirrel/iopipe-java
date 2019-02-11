#!/bin/sh

# This makes things much easier
if ! which zipmerge
then
	echo "zipmerge does not exist, please install it!"
	echo "On Debian/Ubuntu: apt-get install zipmerge"
	exit 1
fi

# Use these secret keys for measuring
export AWS_ACCESS_KEY_ID="$MEASURE_AWS_ACCESS_KEY_ID"
export AWS_SECRET_ACCESS_KEY="$MEASURE_AWS_SECRET_ACCESS_KEY"

# Go to the CircleCI directory and do stuff there
if [ ! -f config.yml ]
then
	cd .circleci
fi

# Go to source directory and try to build the measuring JAR
cd ..
mvn clean -DoutputDirectory=.circleci/target

# Build package
if ! mvn package -Dmaven.test.skip=true -DoutputDirectory=.circleci/target
then
	echo "Failed to package."
	exit 1
fi

# Copy needed dependencies
if ! mvn dependency:copy-dependencies -DincludeScope=runtime -DoutputDirectory=.circleci/target
then
	echo "Failed to copy dependencies."
	exit 1
fi

# Go back to the CircleCI directory
cd .circlci

# Merge JARs into single JAR
rm -vf measurefunc.jar
if ! zipmerge measurefunc.jar target/*.jar
then
	echo "Failed to merge ZIP."
	exit 1
fi

exit 2

# Deploy the function
if ! sls deploy
then
	echo "Could not deploy function"
	exit 1
fi

