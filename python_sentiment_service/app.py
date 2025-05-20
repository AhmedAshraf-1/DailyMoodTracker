import os
import json
import logging
import random
from flask import Flask, request, jsonify
from waitress import serve

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    handlers=[logging.StreamHandler()]
)
logger = logging.getLogger(__name__)

app = Flask(__name__)

# Emotion keyword mappings
emotion_keywords = {
    "joy": ["happy", "joy", "delighted", "excited", "pleased", "glad", "content",
            "wonderful", "great", "amazing", "awesome", "excellent", "fantastic"],
    
    "sadness": ["sad", "unhappy", "depressed", "gloomy", "miserable", "heartbroken",
                "down", "blue", "somber", "melancholy", "grief", "sorrow"],
    
    "anger": ["angry", "mad", "furious", "irritated", "annoyed", "frustrated",
              "rage", "hate", "upset", "bitter", "enraged", "outraged"],
    
    "fear": ["afraid", "scared", "terrified", "anxious", "worried", "nervous",
             "frightened", "horror", "panic", "dread", "concern", "stress"],
    
    "surprise": ["surprised", "shocked", "amazed", "astonished", "stunned", "unexpected",
                "startled", "wow", "whoa", "unexpected", "disbelief"],
    
    "confusion": ["confused", "perplexed", "puzzled", "uncertain", "unsure", "doubtful",
                 "bewildered", "lost", "disoriented", "unclear", "ambiguous"],
    
    "gratitude": ["grateful", "thankful", "appreciative", "blessed", "fortunate", "appreciate",
                 "thanks", "blessing", "gratitude", "indebted"],
    
    "hope": ["hopeful", "optimistic", "looking forward", "eager", "anticipate", "wish",
            "dream", "expect", "bright future", "promising"]
}

# Positive and negative word lists
positive_words = [
    "good", "happy", "great", "excellent", "wonderful", "love", "joy",
    "excited", "amazing", "fantastic", "delighted", "glad", "pleased",
    "positive", "beautiful", "nice", "perfect", "brilliant", "enjoy",
    "grateful", "awesome", "impressive", "spectacular", "terrific"
]

negative_words = [
    "bad", "sad", "terrible", "awful", "horrible", "hate", "disappointed",
    "upset", "angry", "depressed", "worried", "anxious", "stressed",
    "negative", "poor", "failed", "miserable", "unfortunate", "gloomy",
    "painful", "hurt", "worst", "awful", "dreadful", "scared"
]

# Therapeutic approaches mapping
therapy_approaches = {
    "joy": "Positive reinforcement and appreciation of current positive state",
    "sadness": "Empathetic listening and validation of feelings",
    "anger": "Validation and safe expression of emotions",
    "fear": "Grounding techniques and reassurance",
    "surprise": "Processing and making meaning of unexpected events",
    "confusion": "Clarification and providing structure",
    "gratitude": "Savoring positive experiences and building on strengths",
    "hope": "Goal-setting and future-oriented thinking",
    "neutral": "Open-ended exploration of experiences"
}

# Conversation needs mapping
conversation_needs = {
    "joy": "Celebrate successes and savor positive emotions",
    "sadness": "Provide comfort and space for expressing feelings",
    "anger": "Acknowledge feelings and explore underlying causes",
    "fear": "Offer reassurance and coping strategies",
    "surprise": "Help process unexpected information",
    "confusion": "Provide clarity and organize thoughts",
    "gratitude": "Expand awareness of positive aspects",
    "hope": "Encourage optimism while being realistic",
    "neutral": "General exploration of thoughts and feelings"
}

# Bot response templates
sentiment_responses = {
    "positive": [
        "I'm glad to hear you're feeling positive! What's contributing to these good feelings?",
        "That sounds wonderful! Would you like to share more about what's making you feel this way?",
        "It's great that you're in a positive mood. How can we build on these good feelings?",
        "I'm happy to hear that! What other positive things have been happening for you?"
    ],
    
    "negative": [
        "I'm sorry to hear you're not feeling your best. Would you like to talk more about what's going on?",
        "That sounds challenging. What support would be most helpful for you right now?",
        "I can understand why that might be difficult. How have you been coping with these feelings?",
        "Thank you for sharing those feelings with me. Is there anything specific you'd like to focus on today?"
    ],
    
    "neutral": [
        "Thank you for sharing. How else have you been feeling lately?",
        "I appreciate your thoughts. Is there anything specific on your mind today?",
        "Thank you for expressing that. What would you like to talk about next?",
        "I understand. Is there any particular area of your life you'd like to discuss?"
    ]
}

# Specific emotion responses
emotion_responses = {
    "joy": "It's wonderful to hear you're feeling joy! What's bringing you happiness right now?",
    "sadness": "I'm sorry you're feeling sad. It's okay to feel this way, and I'm here to listen.",
    "anger": "I can hear that you're feeling angry. That's a valid emotion - would it help to talk about what triggered it?",
    "fear": "It sounds like you're experiencing some fear or anxiety. Would it help to explore what's causing these feelings?",
    "surprise": "That seems quite surprising! How are you processing this unexpected situation?",
    "confusion": "It seems like you might be feeling uncertain or confused. Let's try to bring some clarity together.",
    "gratitude": "I love that you're expressing gratitude. Appreciating the positive things can be so powerful.",
    "hope": "It's great to hear that hopeful tone in your message. What are you looking forward to?"
}

def analyze_sentiment(text):
    """Analyze the sentiment of the given text"""
    if not text:
        return {
            "dominant_sentiment": "neutral",
            "positive_score": 0.1,
            "negative_score": 0.1,
            "neutral_score": 0.8
        }
    
    try:
        # Convert to lowercase for keyword matching
        text_lower = text.lower()
        
        # Count positive and negative words
        positive_count = sum(1 for word in positive_words if word in text_lower)
        negative_count = sum(1 for word in negative_words if word in text_lower)
        
        # Calculate scores
        if positive_count > 0 or negative_count > 0:
            # Calculate base scores from keyword counts
            positive_score = min(0.1 + (positive_count * 0.1), 0.9)
            negative_score = min(0.1 + (negative_count * 0.1), 0.9)
            
            # Add some randomness for variety
            positive_score += (random.random() * 0.1) - 0.05
            negative_score += (random.random() * 0.1) - 0.05
            
            # Ensure scores are within bounds
            positive_score = max(0.05, min(0.95, positive_score))
            negative_score = max(0.05, min(0.95, negative_score))
            
            # Calculate neutral score
            neutral_score = max(0.1, 1.0 - (positive_score + negative_score))
            
            # Normalize to ensure they sum to 1.0
            total = positive_score + negative_score + neutral_score
            positive_score = positive_score / total
            negative_score = negative_score / total
            neutral_score = neutral_score / total
        else:
            # Default to mostly neutral if no keywords found
            neutral_score = 0.7
            positive_score = 0.15
            negative_score = 0.15
        
        # Determine dominant sentiment
        if positive_score > negative_score and positive_score > neutral_score:
            dominant_sentiment = "positive"
        elif negative_score > positive_score and negative_score > neutral_score:
            dominant_sentiment = "negative"
        else:
            dominant_sentiment = "neutral"
        
        # Determine specific emotion
        specific_emotion = analyze_specific_emotion(text_lower)
        
        # Calculate emotional intensity (0.3-0.9)
        emotional_intensity = 0.5
        if specific_emotion != "neutral":
            # More intense if more keywords matched
            total_emotion_keywords = positive_count + negative_count
            emotional_intensity = min(0.3 + (total_emotion_keywords * 0.05), 0.9)
        
        # Get therapeutic approach and conversation needs
        therapeutic_approach = therapy_approaches.get(specific_emotion, 
                therapy_approaches.get("neutral"))
        
        conversation_need = conversation_needs.get(specific_emotion,
                conversation_needs.get("neutral"))
        
        return {
            "dominant_sentiment": dominant_sentiment,
            "positive_score": positive_score,
            "negative_score": negative_score,
            "neutral_score": neutral_score,
            "specific_emotion": specific_emotion,
            "emotional_intensity": emotional_intensity,
            "therapeutic_approach": therapeutic_approach,
            "conversation_needs": conversation_need
        }
        
    except Exception as e:
        logger.error(f"Error analyzing sentiment: {str(e)}")
        return {
            "error": str(e),
            "dominant_sentiment": "neutral",
            "positive_score": 0.1,
            "negative_score": 0.1,
            "neutral_score": 0.8
        }

def analyze_specific_emotion(text):
    """Analyze text for specific emotions using keyword matching"""
    # Count emotion keywords
    emotion_counts = {emotion: 0 for emotion in emotion_keywords}
    
    for emotion, keywords in emotion_keywords.items():
        for keyword in keywords:
            if keyword in text:
                emotion_counts[emotion] += 1
    
    # Find the emotion with the most matches
    dominant_emotion = "neutral"
    max_count = 0
    
    for emotion, count in emotion_counts.items():
        if count > max_count:
            max_count = count
            dominant_emotion = emotion
    
    # If no emotions detected or very weak signal, return neutral
    if max_count < 1:
        return "neutral"
    
    return dominant_emotion

@app.route('/', methods=['GET'])
def home():
    return jsonify({"status": "running", "message": "Sentiment Analysis Service is running"})

@app.route('/analyze', methods=['POST'])
def analyze():
    try:
        data = request.get_json()
        
        if not data or 'text' not in data:
            return jsonify({"error": "Missing 'text' parameter"}), 400
            
        text = data['text']
        user_id = data.get('user_id', 1)  # Default user_id to 1 if not provided
        
        logger.info(f"Analyzing sentiment for user {user_id}, text length: {len(text)}")
        
        result = analyze_sentiment(text)
        result['text'] = text
        result['user_id'] = user_id
        
        logger.info(f"Analysis result: dominant={result['dominant_sentiment']}, " 
                   f"emotion={result.get('specific_emotion', 'unknown')}")
        
        return jsonify(result)
        
    except Exception as e:
        logger.error(f"Error processing request: {str(e)}")
        return jsonify({"error": str(e)}), 500

@app.route('/bot_response', methods=['POST'])
def get_bot_response():
    try:
        data = request.get_json()
        
        if not data:
            return jsonify({"error": "Invalid request"}), 400
            
        # Extract sentiment data
        dominant_sentiment = data.get('dominant_sentiment', 'neutral')
        specific_emotion = data.get('specific_emotion', '')
        
        # Select response based on sentiment or specific emotion
        if specific_emotion and specific_emotion in emotion_responses:
            response = emotion_responses[specific_emotion]
        else:
            responses = sentiment_responses.get(dominant_sentiment, sentiment_responses["neutral"])
            response = random.choice(responses)
            
        return jsonify({"response": response})
        
    except Exception as e:
        logger.error(f"Error generating bot response: {str(e)}")
        return jsonify({"response": "I'm here to listen. How are you feeling today?"}), 200

if __name__ == "__main__":
    # Get port from environment or use default
    port = int(os.environ.get("PORT", 8080))
    
    # Use waitress for production-ready server
    logger.info(f"Starting server on port {port}")
    serve(app, host="127.0.0.1", port=port) 