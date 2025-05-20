#!/bin/bash
echo "Starting Python Sentiment Analysis Service..."

# Check if Python is installed
if ! command -v python3 &> /dev/null; then
    echo "Python 3 is not installed or not in PATH"
    echo "Please install Python 3.7 or higher"
    exit 1
fi

# Check if pip is installed
if ! command -v pip3 &> /dev/null; then
    echo "pip3 is not installed or not in PATH"
    echo "Please install pip3"
    exit 1
fi

# Check if requirements are installed
echo "Checking requirements..."
pip3 install -r requirements.txt

# Start the service
echo "Starting service on port 5000..."
python3 app.py 