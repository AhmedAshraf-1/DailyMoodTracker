import logging
import random
import json
import os
import numpy as np
from sentence_transformers import SentenceTransformer
from sklearn.metrics.pairwise import cosine_similarity

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

class ConversationModel:
    """
    Advanced conversational model using a retrieval-based approach
    combined with context-awareness and therapeutic approaches.
    """
    
    def __init__(self):
        self.device = "cuda" if os.environ.get("USE_CUDA", "0") == "1" else "cpu"
        logger.info(f"Conversation model using device: {self.device}")
        
        # Load sentence embedding model for response matching
        try:
            logger.info("Loading conversation embedding model...")
            self.embedding_model = SentenceTransformer("all-MiniLM-L6-v2")
            self.embedding_model.to(self.device)
            logger.info("Conversation embedding model loaded successfully")
        except Exception as e:
            logger.error(f"Error loading conversation model: {str(e)}")
            self.embedding_model = None
        
        # Load therapeutic response templates
        self.response_templates = self._load_response_templates()
        
        # Compute embeddings for all templates
        if self.embedding_model:
            logger.info("Computing template embeddings...")
            self.template_embeddings = {}
            for category, templates in self.response_templates.items():
                texts = [t["text"] for t in templates]
                self.template_embeddings[category] = {
                    "templates": templates,
                    "embeddings": self.embedding_model.encode(texts)
                }
            logger.info("Computed embeddings for all response templates")
            
        # Conversation history (simple system)
        self.recent_templates = set()
        self.max_recent_history = 15  # To avoid repetition
    
    def _load_response_templates(self):
        """Load response templates from data file or use default"""
        templates_file = os.path.join(os.path.dirname(__file__), "response_templates.json")
        
        try:
            if os.path.exists(templates_file):
                with open(templates_file, 'r', encoding='utf-8') as f:
                    templates = json.load(f)
                logger.info(f"Loaded {sum(len(v) for v in templates.values())} response templates")
                return templates
        except Exception as e:
            logger.error(f"Error loading response templates: {str(e)}")
            
        # Default templates if file not found or error
        return {
            "positive": [
                {"text": "I'm glad to hear you're feeling positive! What's contributing to these good feelings?", "tags": ["inquire", "reflection"]},
                {"text": "That sounds wonderful! Would you like to share more about what's making you feel this way?", "tags": ["encourage", "sharing"]},
                {"text": "It's great that you're in a positive mood. How can we build on these good feelings?", "tags": ["reinforce", "forward-looking"]},
                {"text": "I'm happy to hear that! What other positive things have been happening for you?", "tags": ["explore", "positive"]}
            ],
            "negative": [
                {"text": "I'm sorry to hear you're not feeling your best. Would you like to talk more about what's going on?", "tags": ["empathy", "inquiry"]},
                {"text": "That sounds challenging. What support would be most helpful for you right now?", "tags": ["support", "needs-assessment"]},
                {"text": "I understand why that might be difficult. How have you been coping with these feelings?", "tags": ["validation", "coping"]},
                {"text": "Thank you for sharing those feelings with me. Is there anything specific you'd like to focus on today?", "tags": ["gratitude", "directive"]}
            ],
            "neutral": [
                {"text": "Thank you for sharing. How else have you been feeling lately?", "tags": ["acknowledgment", "exploration"]},
                {"text": "I appreciate your thoughts. Is there anything specific on your mind today?", "tags": ["appreciation", "focus"]},
                {"text": "Thank you for expressing that. What would you like to talk about next?", "tags": ["gratitude", "direction"]},
                {"text": "I understand. Is there any particular area of your life you'd like to discuss?", "tags": ["acknowledgment", "specificity"]}
            ],
            "joy": [
                {"text": "It's wonderful to hear you're feeling joy! What's bringing you happiness right now?", "tags": ["joy", "inquiry"]},
                {"text": "I love hearing that you're experiencing joy! What activities contribute most to this feeling?", "tags": ["joy", "activities"]},
                {"text": "That sounds like a really positive experience. How might you create more moments like this?", "tags": ["joy", "future"]}
            ],
            "sadness": [
                {"text": "I'm sorry you're feeling sad. It's okay to feel this way, and I'm here to listen.", "tags": ["sadness", "validation"]},
                {"text": "When you're feeling sad like this, what typically helps you feel even a little better?", "tags": ["sadness", "coping"]},
                {"text": "Sadness can be really hard to sit with. Would it help to explore what triggered these feelings?", "tags": ["sadness", "exploration"]}
            ],
            "anger": [
                {"text": "I can hear that you're feeling angry. That's a valid emotion - would it help to talk about what triggered it?", "tags": ["anger", "validation"]},
                {"text": "Anger often tells us something important about our boundaries or needs. What do you think your anger might be telling you?", "tags": ["anger", "insight"]},
                {"text": "When you notice yourself feeling angry, what helps you process that emotion in a healthy way?", "tags": ["anger", "coping"]}
            ],
            "fear": [
                {"text": "It sounds like you're experiencing some fear or anxiety. Would it help to explore what's causing these feelings?", "tags": ["fear", "exploration"]},
                {"text": "Fear can be really uncomfortable. What helps you feel safer when you're afraid?", "tags": ["fear", "safety"]},
                {"text": "I hear that you're feeling afraid. Is this something you've experienced before, or is this new?", "tags": ["fear", "history"]}
            ],
            "surprise": [
                {"text": "That seems quite surprising! How are you processing this unexpected situation?", "tags": ["surprise", "processing"]},
                {"text": "Unexpected events can really throw us off balance. How are you adjusting to this surprise?", "tags": ["surprise", "adjustment"]},
                {"text": "I can imagine that was surprising. Has this changed how you think about things?", "tags": ["surprise", "perspective"]}
            ],
            "confusion": [
                {"text": "It seems like you might be feeling uncertain or confused. Let's try to bring some clarity together.", "tags": ["confusion", "clarity"]},
                {"text": "When things feel confusing, sometimes it helps to break them down into smaller pieces. Where would you like to start?", "tags": ["confusion", "simplify"]},
                {"text": "I hear that you're feeling confused. What part feels most unclear right now?", "tags": ["confusion", "focus"]}
            ],
            "gratitude": [
                {"text": "I love that you're expressing gratitude. Appreciating the positive things can be so powerful.", "tags": ["gratitude", "reinforcement"]},
                {"text": "Gratitude is such a wonderful emotion to cultivate. How does expressing gratitude affect your overall wellbeing?", "tags": ["gratitude", "impact"]},
                {"text": "It's beautiful to hear you express gratitude. What other things in your life are you thankful for?", "tags": ["gratitude", "expansion"]}
            ],
            "hope": [
                {"text": "It's great to hear that hopeful tone in your message. What are you looking forward to?", "tags": ["hope", "future"]},
                {"text": "Hope can be such a powerful force. What helps you maintain that sense of hope?", "tags": ["hope", "maintenance"]},
                {"text": "I really appreciate your hopeful perspective. What steps are you taking toward the future you envision?", "tags": ["hope", "action"]}
            ],
            "general": [
                {"text": "How have things been going for you lately?", "tags": ["opening", "general"]},
                {"text": "What's been on your mind today?", "tags": ["opening", "present"]},
                {"text": "Is there anything specific you'd like to talk about?", "tags": ["directive", "specific"]},
                {"text": "How have you been taking care of yourself recently?", "tags": ["self-care", "inquiry"]},
                {"text": "What's one small positive thing that happened recently?", "tags": ["positive", "specific"]}
            ],
            "crisis": [
                {"text": "It sounds like you're going through a really difficult time. Would it help to talk about some immediate coping strategies?", "tags": ["crisis", "coping"]},
                {"text": "I'm concerned about what you're sharing. Have you considered reaching out to a crisis helpline or mental health professional?", "tags": ["crisis", "resources"]},
                {"text": "This sounds really challenging. What support do you have available right now?", "tags": ["crisis", "support"]}
            ],
            "therapy": [
                {"text": "That's a really insightful observation about yourself. How did you come to that understanding?", "tags": ["insight", "exploration"]},
                {"text": "I wonder if we could explore that feeling a bit more deeply. When did you first notice it?", "tags": ["depth", "origins"]},
                {"text": "That's a pattern I've noticed in what you're sharing. Have you recognized this pattern before?", "tags": ["patterns", "awareness"]}
            ]
        }
    
    def generate_response(self, user_text, sentiment_data, conversation_history=None):
        """
        Generate a contextually appropriate response based on user input and sentiment.
        
        Args:
            user_text (str): The user's message
            sentiment_data (dict): Sentiment analysis results
            conversation_history (list, optional): Previous exchanges
            
        Returns:
            str: Generated response
        """
        if not self.embedding_model:
            return self._get_fallback_response(sentiment_data)
        
        try:
            # Extract key sentiment information
            dominant_sentiment = sentiment_data.get("dominant_sentiment", "neutral")
            specific_emotion = sentiment_data.get("specific_emotion", "neutral")
            
            # Determine which response categories to consider based on sentiment
            categories = ["general"]  # Always include general responses
            
            # Add sentiment-specific categories
            if dominant_sentiment in self.response_templates:
                categories.append(dominant_sentiment)
                
            # Add emotion-specific category if it exists
            if specific_emotion in self.response_templates:
                categories.append(specific_emotion)
            
            # Check for crisis keywords
            crisis_keywords = ["suicide", "kill myself", "end my life", "don't want to live", 
                               "hurting myself", "harm myself", "self harm"]
            if any(keyword in user_text.lower() for keyword in crisis_keywords):
                categories = ["crisis"]  # Prioritize crisis responses
                
            # Generate user text embedding
            user_embedding = self.embedding_model.encode([user_text])[0]
            
            # Find the best matching response
            best_response = None
            best_score = -1
            
            for category in categories:
                if category not in self.template_embeddings:
                    continue
                    
                # Get embeddings and templates for this category
                category_data = self.template_embeddings[category]
                embeddings = category_data["embeddings"]
                templates = category_data["templates"]
                
                # Calculate similarity scores
                similarities = cosine_similarity([user_embedding], embeddings)[0]
                
                # Consider all responses with decent similarity
                for i, score in enumerate(similarities):
                    template = templates[i]
                    template_id = f"{category}_{i}"
                    
                    # Skip recently used templates to avoid repetition
                    if template_id in self.recent_templates:
                        continue
                        
                    # Apply randomness to encourage variety (0.8-1.0 range)
                    adjusted_score = score * (0.8 + 0.2 * random.random())
                    
                    if adjusted_score > best_score:
                        best_score = adjusted_score
                        best_response = template["text"]
                        
                        # Remember this template to avoid repetition
                        self.recent_templates.add(template_id)
                        if len(self.recent_templates) > self.max_recent_history:
                            self.recent_templates.pop()
            
            # Use the best response or fallback
            if best_response:
                return best_response
                
            return self._get_fallback_response(sentiment_data)
            
        except Exception as e:
            logger.error(f"Error generating conversation response: {str(e)}")
            return "I'm here to listen and support you. How can I help today?"
    
    def _get_fallback_response(self, sentiment_data):
        """Get a fallback response based on sentiment"""
        sentiment = sentiment_data.get("dominant_sentiment", "neutral")
        
        if sentiment == "positive":
            responses = [
                "I'm glad you're feeling positive! What's contributing to these good feelings?",
                "That sounds wonderful! How can we build on these positive emotions?"
            ]
        elif sentiment == "negative":
            responses = [
                "I'm sorry to hear you're not feeling your best. Would you like to talk more about what's going on?",
                "That sounds challenging. What support would be most helpful for you right now?"
            ]
        else:
            responses = [
                "I'm here to listen. Is there anything specific you'd like to talk about today?",
                "Thank you for sharing. How can I support you right now?"
            ]
            
        return random.choice(responses)
    
    def save_conversation_data(self, user_text, sentiment_data, bot_response):
        """
        Save conversation data for future training.
        
        Args:
            user_text (str): User's message
            sentiment_data (dict): Sentiment analysis results
            bot_response (str): Generated response
        """
        try:
            data_dir = os.path.join(os.path.dirname(__file__), "conversation_data")
            
            # Create directory if it doesn't exist
            if not os.path.exists(data_dir):
                os.makedirs(data_dir)
                
            # Create a data point
            data_point = {
                "user_message": user_text,
                "sentiment": sentiment_data,
                "bot_response": bot_response,
                "timestamp": None  # Use default serialization
            }
            
            # Append to file (one JSON object per line for easy processing)
            with open(os.path.join(data_dir, "conversations.jsonl"), "a", encoding="utf-8") as f:
                f.write(json.dumps(data_point) + "\n")
                
        except Exception as e:
            logger.error(f"Error saving conversation data: {str(e)}")
            # Non-critical error, continue execution

# Singleton instance
_conversation_model = None

def get_conversation_model():
    """Get or create the conversation model instance"""
    global _conversation_model
    if _conversation_model is None:
        _conversation_model = ConversationModel()
    return _conversation_model 