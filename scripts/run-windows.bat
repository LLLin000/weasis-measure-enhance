@echo off
chcp 65001 >nul
echo ========================================
echo  Weasis Measure Enhance Plugin Launcher
echo ========================================
echo.

REM Get the directory where this script is located
set SCRIPT_DIR=%~dp0

REM Check if Weasis is installed
if exist "C:\Program Files\Weasis\Weasis.exe" (
    set WEASIS_EXE="C:\Program Files\Weasis\Weasis.exe"
) else if exist "%LOCALAPPDATA%\Weasis\Weasis.exe" (
    set WEASIS_EXE="%LOCALAPPDATA%\Weasis\Weasis.exe"
) else (
    echo ERROR: Weasis not found!
    echo Please install Weasis from: https://weasis.org/
    pause
    exit /b 1
)

REM Check if plugin JAR exists
set PLUGIN_JAR=%SCRIPT_DIR%target\weasis-measure-enhance-1.0.0-SNAPSHOT.jar
if not exist "%PLUGIN_JAR%" (
    echo Plugin JAR not found. Building...
    echo.
    
    REM Try to build with Maven
    where mvn >nul 2>&1
    if %ERRORLEVEL% == 0 (
        cd /d "%SCRIPT_DIR%"
        call mvn clean package -DskipTests
        if %ERRORLEVEL% neq 0 (
            echo Build failed!
            pause
            exit /b 1
        )
    ) else (
        echo ERROR: Maven not found and plugin JAR does not exist!
        echo Please build the plugin first or install Maven.
        pause
        exit /b 1
    )
)

REM Create temporary config file with correct path
set CONFIG_FILE=%TEMP%\weasis-measure-enhance-config.json
echo { > "%CONFIG_FILE%"
echo   "weasisPreferences": [ >> "%CONFIG_FILE%"
echo     { >> "%CONFIG_FILE%"
echo       "code": "felix.auto.start.13", >> "%CONFIG_FILE%"
echo       "value": "file:///%PLUGIN_JAR:\=/%", >> "%CONFIG_FILE%"
echo       "description": "Weasis Measure Enhance Plugin" >> "%CONFIG_FILE%"
echo     } >> "%CONFIG_FILE%"
echo   ] >> "%CONFIG_FILE%"
echo } >> "%CONFIG_FILE%"

echo Starting Weasis with Measure Enhance Plugin...
echo Plugin: %PLUGIN_JAR%
echo.

start "" %WEASIS_EXE% "$weasis:config pro=\"felix.extended.config.properties file:///%CONFIG_FILE:\=/%\""

echo Weasis started with plugin loaded.
echo.
pause
