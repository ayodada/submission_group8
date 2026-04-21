@echo off
setlocal

if not exist out\classes mkdir out\classes

javac -encoding UTF-8 -d out\classes src\searchengine\*.java
if errorlevel 1 exit /b 1

echo Compilation complete. Classes are in out\classes

