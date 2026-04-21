@echo off
setlocal

if "%~4"=="" (
  echo Usage: run-search.bat ^<indexDir^> ^<topicsFile^> ^<outputFile^> ^<groupId^> [topK]
  exit /b 1
)

if "%~5"=="" (
  java -cp out\classes searchengine.SearchEngineApp run "%~1" "%~2" "%~3" "%~4"
) else (
  java -cp out\classes searchengine.SearchEngineApp run "%~1" "%~2" "%~3" "%~4" "%~5"
)

