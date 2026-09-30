#!/bin/sh
# Maven Wrapper script for macOS/Linux
# Falls back to system Maven if wrapper JAR is not available

if [ -f ".mvn/wrapper/maven-wrapper.jar" ]; then
    exec java -jar .mvn/wrapper/maven-wrapper.jar "$@"
else
    echo "Maven wrapper JAR not found. Falling back to system Maven..."
    exec mvn "$@"
fi
