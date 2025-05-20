@echo off
echo Starting Python Sentiment Analysis Service...

REM Check if Python is installed
python --version > nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo Python is not installed or not in PATH
    echo Please install Python 3.7 or higher
    pause
    exit /b 1
)

REM Check if pip is installed
pip --version > nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo pip is not installed or not in PATH
    echo Please install pip
    pause
    exit /b 1
)

REM Check if requirements are installed
echo Checking requirements...
pip install -r requirements.txt

REM Start the service
echo Starting service on port 5000...
python app.py

pause 