@echo off
setlocal

if "%~1"=="" (
  echo Usage: run-web.bat ^<indexDir^> [port] [topK]
  exit /b 1
)

if "%~2"=="" (
  java -cp out\classes searchengine.SearchEngineApp serve "%~1"
) else if "%~3"=="" (
  java -cp out\classes searchengine.SearchEngineApp serve "%~1" "%~2"
) else (
  java -cp out\classes searchengine.SearchEngineApp serve "%~1" "%~2" "%~3"
)
