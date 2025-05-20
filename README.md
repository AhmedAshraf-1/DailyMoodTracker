# Daily Mood Tracker

A Java application for tracking daily mood entries and visualizing mood trends over time.

## Features

- Record daily mood entries with notes
- View mood history and trends
- Simple and intuitive user interface
- Data persistence

## Setup

1. Clone this repository
2. Open the project in your preferred Java IDE
3. Run the application

## Usage

1. Launch the application
2. Enter your daily mood rating and optional notes
3. View your mood history and trends

## Sentiment Analysis Options

The application supports multiple sentiment analysis services:

1. **Python HTTP Service**: A Flask-based microservice that provides sentiment analysis capabilities locally
2. **Python Local Service**: Direct local implementation within the Java application
3. **IBM Watson NLU**: Commercial API for sentiment analysis (requires API key)
4. **Dummy Service**: Simulated sentiment analysis for testing purposes

### Python HTTP Service Setup

To use the Python HTTP sentiment analysis service:

1. Make sure Python is installed on your system
2. Navigate to the `python_sentiment_service` directory
3. Run the service:

```bash
# Windows
start_service.bat

# Linux/Mac
./start_service.sh
```

This will start the service on http://localhost:8080, which the application will automatically detect.

### Service Selection

You can select which sentiment analysis service to use in the application settings:

1. Open the application
2. Go to Settings
3. Select your preferred sentiment analysis service
4. Save settings

The application will automatically detect available services and use the best available option. If the preferred service is not available, it will fall back to the next available option.

## Database Setup

The application uses an embedded H2 database for data storage. No additional setup is required as the database file will be created automatically on first run.

Database files are stored in the user's home directory under `.dailymoodtracker/db`.

## Building and Running

### Prerequisites

- Java 21 or higher
- Maven 3.6 or higher
- Python 3.7 or higher (for Python sentiment analysis services)

### Build and Run

```bash
# Build the project
mvn clean package

# Run the application
java -jar target/DailyMoodTracker-1.0-SNAPSHOT-jar-with-dependencies.jar
```

Or use the included run script:

```bash
# Windows
run.bat

# Linux/Mac
./run.sh
```

## License

This project is licensed under the MIT License - see the LICENSE file for details. 