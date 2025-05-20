import logging
import os
import json
import torch
import numpy as np
from transformers import AutoModelForSequenceClassification, AutoTokenizer, Trainer, TrainingArguments
from transformers import DataCollatorWithPadding
from datasets import Dataset
from sklearn.model_selection import train_test_split

# Configure logging
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(name)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

class SentimentModelFineTuner:
    """
    Handles fine-tuning of sentiment analysis models on mental health conversation data.
    """
    
    def __init__(self, model_name="distilbert-base-uncased-finetuned-sst-2-english"):
        """Initialize with a pre-trained model to fine-tune"""
        self.model_name = model_name
        self.device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
        logger.info(f"Using device: {self.device}")
        
        self.tokenizer = None
        self.model = None
        self.output_dir = os.path.join(os.path.dirname(__file__), "models")
        
    def load_model(self):
        """Load the pre-trained model and tokenizer"""
        try:
            logger.info(f"Loading model: {self.model_name}")
            self.tokenizer = AutoTokenizer.from_pretrained(self.model_name)
            self.model = AutoModelForSequenceClassification.from_pretrained(self.model_name)
            self.model.to(self.device)
            logger.info("Model loaded successfully")
            return True
        except Exception as e:
            logger.error(f"Error loading model: {str(e)}")
            return False
            
    def collect_conversation_data(self):
        """Collect conversation data for fine-tuning"""
        data_path = os.path.join(os.path.dirname(__file__), "conversation_data", "conversations.jsonl")
        
        if not os.path.exists(data_path) or os.path.getsize(data_path) == 0:
            logger.warning("No conversation data found for fine-tuning")
            return None
            
        logger.info(f"Reading conversation data from {data_path}")
        
        # Load the data
        texts = []
        labels = []
        
        with open(data_path, 'r', encoding='utf-8') as f:
            for line in f:
                try:
                    data = json.loads(line.strip())
                    
                    # Extract the text
                    text = data.get("user_message", "")
                    
                    # Get sentiment label (convert to binary for simplicity)
                    sentiment = data.get("sentiment", {}).get("dominant_sentiment", "neutral")
                    
                    # Convert to binary label (0 = negative, 1 = positive)
                    if sentiment == "positive":
                        label = 1
                    elif sentiment == "negative":
                        label = 0
                    else:
                        # Skip neutral examples for binary classification
                        continue
                        
                    texts.append(text)
                    labels.append(label)
                    
                except Exception as e:
                    logger.error(f"Error processing line: {str(e)}")
                    continue
        
        logger.info(f"Collected {len(texts)} examples for fine-tuning")
        
        if len(texts) < 20:
            logger.warning("Not enough data for fine-tuning (minimum 20 examples needed)")
            return None
            
        return {"texts": texts, "labels": labels}
    
    def prepare_dataset(self, data):
        """Prepare dataset for training"""
        if data is None or len(data["texts"]) == 0:
            return None, None
            
        # Split into train and validation
        train_texts, val_texts, train_labels, val_labels = train_test_split(
            data["texts"], data["labels"], test_size=0.2, random_state=42)
            
        # Tokenize function
        def tokenize_function(examples):
            return self.tokenizer(
                examples["text"], 
                padding="max_length",
                truncation=True,
                max_length=128
            )
        
        # Create datasets
        train_dataset = Dataset.from_dict({"text": train_texts, "label": train_labels})
        val_dataset = Dataset.from_dict({"text": val_texts, "label": val_labels})
        
        # Tokenize datasets
        train_dataset = train_dataset.map(tokenize_function, batched=True)
        val_dataset = val_dataset.map(tokenize_function, batched=True)
        
        return train_dataset, val_dataset
        
    def fine_tune(self):
        """Fine-tune the model on conversation data"""
        if not self.load_model():
            return False
            
        # Collect and prepare data
        data = self.collect_conversation_data()
        train_dataset, val_dataset = self.prepare_dataset(data)
        
        if train_dataset is None:
            logger.warning("No training data available")
            return False
            
        # Ensure output directory exists
        os.makedirs(self.output_dir, exist_ok=True)
        
        # Define training arguments
        training_args = TrainingArguments(
            output_dir=self.output_dir,
            num_train_epochs=3,
            per_device_train_batch_size=16,
            per_device_eval_batch_size=16,
            warmup_steps=500,
            weight_decay=0.01,
            logging_dir=os.path.join(self.output_dir, "logs"),
            logging_steps=10,
            evaluation_strategy="epoch",
            save_strategy="epoch",
            load_best_model_at_end=True,
        )
        
        # Create trainer
        trainer = Trainer(
            model=self.model,
            args=training_args,
            train_dataset=train_dataset,
            eval_dataset=val_dataset,
            tokenizer=self.tokenizer,
            data_collator=DataCollatorWithPadding(self.tokenizer)
        )
        
        # Train the model
        logger.info("Starting fine-tuning")
        trainer.train()
        
        # Save the fine-tuned model
        fine_tuned_model_path = os.path.join(self.output_dir, "fine_tuned_sentiment")
        trainer.save_model(fine_tuned_model_path)
        self.tokenizer.save_pretrained(fine_tuned_model_path)
        
        logger.info(f"Fine-tuned model saved to {fine_tuned_model_path}")
        
        return True
    
    @classmethod
    def get_fine_tuned_model_path(cls):
        """Get the path to the fine-tuned model if it exists"""
        model_path = os.path.join(os.path.dirname(__file__), "models", "fine_tuned_sentiment")
        
        if os.path.exists(model_path):
            return model_path
        return None


class TrainingDataGenerator:
    """
    Generate synthetic training data for fine-tuning the sentiment model.
    
    This is useful for bootstrapping the model before real user data is collected.
    """
    
    def __init__(self):
        self.output_dir = os.path.join(os.path.dirname(__file__), "conversation_data")
        os.makedirs(self.output_dir, exist_ok=True)
        
        # Template examples to generate from
        self.templates = {
            "positive": [
                "I'm feeling great today!",
                "I had a wonderful day.",
                "I'm so happy with my progress.",
                "Everything is going well in my life.",
                "I accomplished something important today.",
                "I'm excited about my future.",
                "I had a breakthrough in therapy.",
                "My mood has been so good lately.",
                "I'm proud of myself.",
                "I've been feeling hopeful."
            ],
            "negative": [
                "I'm feeling sad today.",
                "I had a terrible day.",
                "I'm disappointed in myself.",
                "Everything seems to be going wrong.",
                "I'm really struggling right now.",
                "I don't know if I can keep going.",
                "I had an argument with someone important.",
                "My symptoms are getting worse.",
                "I'm overwhelmed with everything.",
                "I've been feeling hopeless."
            ]
        }
        
        # Variations to add to templates
        self.variations = {
            "beginning": [
                "Honestly, ", "To be honest, ", "I think ", "I feel like ", "Today, ",
                "Lately, ", "This week, ", "Recently, ", "I've noticed that ", "I've been thinking that "
            ],
            "ending": [
                " It's been like this for a while.", " I don't know what to do.", " What do you think?",
                " Any advice?", " I wanted to share that.", " Just needed to express that.",
                " I hope this changes.", " I want to talk about it.", " I needed to say that out loud."
            ],
            "intensity": [
                "really ", "very ", "so ", "extremely ", "incredibly ", "somewhat ", "kind of ", "a bit ",
                "slightly ", ""
            ]
        }
    
    def generate_synthetic_data(self, num_examples=100):
        """Generate synthetic training examples"""
        data_file = os.path.join(self.output_dir, "synthetic_training.jsonl")
        
        examples_generated = 0
        
        with open(data_file, 'w', encoding='utf-8') as f:
            # Generate balanced dataset with equal positive/negative examples
            for _ in range(num_examples // 2):
                # Generate positive example
                pos_text = self._generate_example("positive")
                pos_data = {
                    "user_message": pos_text,
                    "sentiment": {
                        "dominant_sentiment": "positive",
                        "positive_score": round(0.7 + random.random() * 0.2, 2),
                        "negative_score": round(random.random() * 0.1, 2),
                        "neutral_score": round(0.1 + random.random() * 0.1, 2)
                    },
                    "synthetic": True
                }
                f.write(json.dumps(pos_data) + "\n")
                
                # Generate negative example
                neg_text = self._generate_example("negative")
                neg_data = {
                    "user_message": neg_text,
                    "sentiment": {
                        "dominant_sentiment": "negative",
                        "positive_score": round(random.random() * 0.1, 2),
                        "negative_score": round(0.7 + random.random() * 0.2, 2),
                        "neutral_score": round(0.1 + random.random() * 0.1, 2)
                    },
                    "synthetic": True
                }
                f.write(json.dumps(neg_data) + "\n")
                
                examples_generated += 2
        
        logger.info(f"Generated {examples_generated} synthetic training examples")
        return examples_generated
    
    def _generate_example(self, sentiment):
        """Generate a single example with variations"""
        template = random.choice(self.templates[sentiment])
        
        # Add variations
        beginning = random.choice(self.variations["beginning"]) if random.random() > 0.5 else ""
        ending = random.choice(self.variations["ending"]) if random.random() > 0.5 else ""
        
        # Replace some words with intensity variations
        words = template.split()
        for i in range(len(words)):
            if random.random() > 0.8 and len(words[i]) > 3:
                # Add intensity modifier before adjectives or verbs
                words[i] = random.choice(self.variations["intensity"]) + words[i]
        
        modified_template = " ".join(words)
        
        return beginning + modified_template + ending


if __name__ == "__main__":
    # Generate synthetic data if file doesn't exist yet
    data_file = os.path.join(os.path.dirname(__file__), "conversation_data", "synthetic_training.jsonl")
    
    if not os.path.exists(data_file):
        generator = TrainingDataGenerator()
        generator.generate_synthetic_data(200)  # Generate 200 examples
    
    # Fine-tune the model
    fine_tuner = SentimentModelFineTuner()
    fine_tuner.fine_tune() 