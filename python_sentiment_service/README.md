# Python Sentiment Analysis Service

A local sentiment analysis service that uses pre-trained models to analyze text sentiment, without needing external API credentials or internet access.

## Features

- Sentiment analysis (positive, negative, neutral)
- Specific emotion detection (joy, sadness, anger, fear, etc.)
- Therapeutic approach suggestions based on detected emotions
- Conversation needs analysis
- Bot response generation

## Requirements

- Python 3.7 or higher
- PyTorch
- Transformers library (Hugging Face)
- Flask
- Waitress (for production serving)

## Installation

1. Install required packages:

```bash
pip install -r requirements.txt
```

2. Run the service:

```bash
python app.py
```

The service will start on port 5000 by default.

## API Endpoints

### 1. Analyze Sentiment

**Endpoint:** `/analyze`
**Method:** POST
**Content-Type:** application/json

**Request Body:**
```json
{
  "text": "I'm feeling really happy today",
  "user_id": 1
}
```

**Response:**
```json
{
  "dominant_sentiment": "positive",
  "positive_score": 0.92,
  "negative_score": 0.03,
  "neutral_score": 0.05,
  "specific_emotion": "joy",
  "emotional_intensity": 0.7,
  "therapeutic_approach": "Positive reinforcement and appreciation of current positive state",
  "conversation_needs": "Celebrate successes and savor positive emotions",
  "text": "I'm feeling really happy today",
  "user_id": 1
}
```

### 2. Get Bot Response

**Endpoint:** `/bot_response`
**Method:** POST
**Content-Type:** application/json

**Request Body:**
```json
{
  "dominant_sentiment": "positive",
  "specific_emotion": "joy"
}
```

**Response:**
```json
{
  "response": "It's wonderful to hear you're feeling joy! What's bringing you happiness right now?"
}
```

## Integration with Java Application

To integrate with the Java app, create a new service implementation that makes HTTP requests to this Python service. 