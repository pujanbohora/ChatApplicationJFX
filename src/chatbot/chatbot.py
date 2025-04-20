package chatbot;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Random;

/**
 * Python-based chatbot implementation using DialoGPT.
 * This script provides both a fallback simple response system and
 * integration with Hugging Face's DialoGPT model if available.
 */

// Try to import transformers for DialoGPT if available
try:
    from transformers import AutoModelForCausalLM, AutoTokenizer
    import torch

    # Initialize DialoGPT model
    tokenizer = AutoTokenizer.from_pretrained('microsoft/DialoGPT-small')
    model = AutoModelForCausalLM.from_pretrained('microsoft/DialoGPT-small')

    has_dialogpt = True
    print('ChatBot initialized with DialoGPT')
except ImportError:
    has_dialogpt = False
    print('ChatBot initialized with basic response system')

# Basic responses if DialoGPT is not available
responses = [
    'Hello there!',
    'How can I help you today?',
    'That\'s interesting!',
    'Tell me more about that.',
    'I understand.',
    'That\'s a good point.',
    'I\'m not sure I follow.',
    'Could you explain that differently?',
    'Let me think about that.',
    'I appreciate your perspective.',
    'What else would you like to talk about?',
    'I\'m here to chat whenever you need.',
    'That sounds challenging.',
    'I\'m glad to hear that!',
    'I\'m sorry to hear that.',
    'That must be difficult.',
    'Congratulations!',
    'That\'s wonderful news!',
    'I\'m learning new things every day.',
    'Thanks for sharing that with me.'
]

def generate_response(input_text):
    if has_dialogpt:
        try:
            # Encode the input text
            input_ids = tokenizer.encode(input_text + tokenizer.eos_token, return_tensors='pt')

            # Generate a response
            output = model.generate(
                input_ids,
                max_length=1000,
                pad_token_id=tokenizer.eos_token_id,
                do_sample=True,
                top_k=50,
                top_p=0.95
            )

            # Decode the response
            response = tokenizer.decode(output[:, input_ids.shape[-1]:][0], skip_special_tokens=True)
            return response if response else random.choice(responses)
        except Exception as e:
            print(f'Error generating DialoGPT response: {e}')
            return random.choice(responses)
    else:
        return random.choice(responses)

# Main loop to process input
while True:
    try:
        # Read input from Java
        input_text = input()

        # Generate and print response
        response = generate_response(input_text)
        print(response)
        import sys
        sys.stdout.flush()
    except EOFError:
        # Exit when input is closed
        break
    except Exception as e:
        print(f'Error: {e}')
        import sys
        sys.stdout.flush()
