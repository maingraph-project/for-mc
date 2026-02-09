@echo off
setlocal

if "%1"=="" goto help
if "%1"=="fabric" goto fabric
if "%1"=="neoforge" goto neoforge
if "%1"=="clean" goto clean
if "%1"=="build" goto build

:fabric
echo Starting Fabric client...
call gradlew.bat :fabric:runClient
goto end

:neoforge
echo Starting NeoForge client...
call gradlew.bat :neoforge:runClient
goto end

:clean
echo Cleaning project...
call gradlew.bat clean
goto end

:build
echo Building project...
call gradlew.bat build
goto end

:help
echo MGMC Quick Run Script
echo Usage:
echo   run.bat fabric      - Start Fabric client
echo   run.bat neoforge    - Start NeoForge client
echo   run.bat build       - Build all platforms
echo   run.bat clean       - Clean project
goto end

:end
pause
