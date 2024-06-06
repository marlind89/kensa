from flask import Flask, request, jsonify
import markovify
import os

app = Flask(__name__)

model = None

@app.route('/reload-model', methods=['POST'])
def reload_model():
    global model

    messages_filename = request.json.get('messagesFilename')
    if not messages_filename:
        return "Missing 'messagesFilename' parameter", 400

    with open(messages_filename, encoding='utf-8') as file:
        model = markovify.Text(file.read(), state_size=3)

    return "OK", 200

@app.route('/generate-sentence', methods=['GET'])
def generate_sentence():
    if model is None:
        return "Model not loaded", 400

    return jsonify(sentence=model.make_sentence(tries=100))

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=6969)