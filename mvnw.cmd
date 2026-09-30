@echo off
REM Maven Wrapper script for Windows
REM Falls back to system Maven if wrapper JAR is not available

if exist ".mvn\wrapper\maven-wrapper.jar" (
    java -jar .mvn\wrapper\maven-wrapper.jar %*
) else (
    echo Maven wrapper JAR not found. Falling back to system Maven...
    call mvn %*
)
