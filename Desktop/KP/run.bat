@echo off
setlocal

set "JAVA_BIN=%JAVA_HOME%\bin"
if exist "%JAVA_BIN%\java.exe" goto have_java

set "JAVA_BIN=%USERPROFILE%\.jdks\openjdk-26\bin"
if exist "%JAVA_BIN%\java.exe" goto have_java

echo Java was not found.
echo Set JAVA_HOME or install a JDK with java.exe.
exit /b 1

:have_java
"%JAVA_BIN%\java.exe" -cp Files MainGUI
