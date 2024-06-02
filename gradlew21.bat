@echo off
set JDK_21=C:\Program Files\Eclipse Adoptium\jdk-21.0.3+9
echo Running gradlew with %JDK_21%
gradlew -Dorg.gradle.java.home="%JDK_21%" %*