@echo off
setlocal

if "%~2"=="" (
  echo Usage: run-index.bat ^<dataDir^> ^<indexDir^>
  exit /b 1
)

java -cp out\classes searchengine.SearchEngineApp index "%~1" "%~2"

