import logging
import torch
import numpy as np
from transformers import AutoModelForSequenceClassification, AutoTokenizer
from sentence_transformers import SentenceTransformer
from sklearn.metrics.pairwise import cosine_similarity

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

class EnhancedSentimentModel:
    """
    Enhanced sentiment analysis model using pre-trained transformers.
    Combines both sentiment classification and contextual understanding.
    """
    
    def __init__(self):
        self.device = "cuda" if torch.cuda.is_available() else "cpu"
        logger.info(f"Using device: {self.device}")
        
        # Load sentiment classification model
        try:
            logger.info("Loading sentiment classification model...")
            self.sentiment_model_name = "distilbert-base-uncased-finetuned-sst-2-english"
            self.sentiment_tokenizer = AutoTokenizer.from_pretrained(self.sentiment_model_name)
            self.sentiment_model = AutoModelForSequenceClassification.from_pretrained(self.sentiment_model_name)
            self.sentiment_model.to(self.device)
            logger.info("Sentiment classification model loaded successfully")
        except Exception as e:
            logger.error(f"Error loading sentiment model: {str(e)}")
            self.sentiment_model = None
            
        # Load sentence embedding model for semantic similarity
        try:
            logger.info("Loading sentence embedding model...")
            self.embedding_model_name = "all-MiniLM-L6-v2"
            self.embedding_model = SentenceTransformer(self.embedding_model_name)
            self.embedding_model.to(self.device)
            logger.info("Sentence embedding model loaded successfully")
        except Exception as e:
            logger.error(f"Error loading embedding model: {str(e)}")
            self.embedding_model = None
        
        # Initialize emotion detection
        self.emotion_categories = ["joy", "sadness", "anger", "fear", "surprise", "confusion", "gratitude", "hope"]
        self.emotion_examples = {
            "joy": ["I feel so happy today", "I'm excited about this", "This makes me so delighted"],
            "sadness": ["I feel so sad", "I'm depressed lately", "Everything makes me feel down"],
            "anger": ["I'm so angry about this", "This is really frustrating", "I'm annoyed at what happened"],
            "fear": ["I'm scared about what might happen", "This makes me anxious", "I'm worried about tomorrow"],
            "surprise": ["I can't believe this happened", "This is so unexpected", "I'm shocked by this news"],
            "confusion": ["I don't understand what's going on", "I'm not sure what to do", "This is all so confusing"],
            "gratitude": ["I'm so thankful for everything", "I appreciate your help", "I'm grateful for this opportunity"],
            "hope": ["I'm hopeful things will get better", "I'm looking forward to the future", "Things will improve soon"]
        }
        
        # Pre-compute emotion embeddings
        if self.embedding_model:
            self.emotion_embeddings = {}
            for emotion, examples in self.emotion_examples.items():
                self.emotion_embeddings[emotion] = self.embedding_model.encode(examples)
    
    def analyze_sentiment(self, text):
        """
        Analyze sentiment of the given text using transformer models.
        
        Args:
            text (str): Input text to analyze
            
        Returns:
            dict: Sentiment analysis results
        """
        if not text or not isinstance(text, str):
            return self._create_default_result()
        
        result = {}
        
        try:
            # Get sentiment scores (positive/negative/neutral)
            sentiment_scores = self._get_sentiment_scores(text)
            result.update(sentiment_scores)
            
            # Get specific emotion
            specific_emotion = self._detect_emotion(text)
            result["specific_emotion"] = specific_emotion
            
            # Calculate emotional intensity based on confidence
            dominant_sentiment = result["dominant_sentiment"]
            dominant_score = max(result["positive_score"], result["negative_score"], result["neutral_score"])
            
            # Emotional intensity is higher for non-neutral emotions and increases with confidence
            if dominant_sentiment != "neutral":
                result["emotional_intensity"] = min(0.3 + (dominant_score * 0.6), 0.9)
            else:
                result["emotional_intensity"] = 0.3
            
        except Exception as e:
            logger.error(f"Error in sentiment analysis: {str(e)}")
            return self._create_default_result()
            
        return result
    
    def _get_sentiment_scores(self, text):
        """Get sentiment scores using pre-trained model"""
        if self.sentiment_model is None:
            return self._create_default_result()
        
        try:
            # Tokenize and get model outputs
            inputs = self.sentiment_tokenizer(text, return_tensors="pt", truncation=True, max_length=512).to(self.device)
            with torch.no_grad():
                outputs = self.sentiment_model(**inputs)
            
            # Get probabilities with softmax
            scores = torch.nn.functional.softmax(outputs.logits, dim=1)[0].cpu().numpy()
            
            # For distilbert-sst2, index 0 is negative, index 1 is positive
            negative_score = float(scores[0])
            positive_score = float(scores[1])
            
            # Calculate neutral score (inverse of confidence)
            confidence = abs(positive_score - negative_score)
            neutral_score = max(0.1, 1.0 - min(confidence * 1.5, 0.9))
            
            # Normalize to ensure they sum to 1
            total = positive_score + negative_score + neutral_score
            positive_score = positive_score / total
            negative_score = negative_score / total
            neutral_score = neutral_score / total
            
            # Determine dominant sentiment
            if positive_score > negative_score and positive_score > neutral_score:
                dominant_sentiment = "positive"
            elif negative_score > positive_score and negative_score > neutral_score:
                dominant_sentiment = "negative"
            else:
                dominant_sentiment = "neutral"
            
            return {
                "dominant_sentiment": dominant_sentiment,
                "positive_score": positive_score,
                "negative_score": negative_score,
                "neutral_score": neutral_score
            }
        except Exception as e:
            logger.error(f"Error in sentiment scoring: {str(e)}")
            return self._create_default_result()
    
    def _detect_emotion(self, text):
        """Detect specific emotion using semantic similarity"""
        if self.embedding_model is None:
            return "neutral"
        
        try:
            # Get text embedding
            text_embedding = self.embedding_model.encode([text])[0]
            
            # Compare with emotion examples
            best_emotion = "neutral"
            best_score = 0.5  # Threshold for considering an emotion
            
            for emotion, embeddings in self.emotion_embeddings.items():
                # Calculate similarities with all examples
                similarities = cosine_similarity([text_embedding], embeddings)[0]
                # Use the highest similarity score for this emotion
                emotion_score = max(similarities)
                
                if emotion_score > best_score:
                    best_score = emotion_score
                    best_emotion = emotion
            
            return best_emotion
        except Exception as e:
            logger.error(f"Error in emotion detection: {str(e)}")
            return "neutral"
    
    def _create_default_result(self):
        """Create default sentiment result when analysis fails"""
        return {
            "dominant_sentiment": "neutral",
            "positive_score": 0.2,
            "negative_score": 0.2,
            "neutral_score": 0.6,
            "specific_emotion": "neutral",
            "emotional_intensity": 0.3
        }


# Singleton instance
_model_instance = None

def get_model():
    """Get or create the sentiment model instance"""
    global _model_instance
    if _model_instance is None:
        _model_instance = EnhancedSentimentModel()
    return _model_instance 