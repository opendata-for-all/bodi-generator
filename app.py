from flask import Flask, request
from transformers import AutoTokenizer, AutoModelForSeq2SeqLM
import logging
logging.basicConfig(level=logging.INFO)

model = None
tokenizer = None

app = Flask(__name__)

@app.route('/set-model-sql', methods=['POST'])
def setModelSQL():
    global model
    global tokenizer
    body = request.get_json()
    modelName = body["modelName"]
    logging.info("Loading " + modelName + " tokenizer")
    tokenizer = AutoTokenizer.from_pretrained(modelName)
    logging.info(modelName + " tokenizer loaded")
    logging.info("Loading " + modelName + " model")
    model = AutoModelForSeq2SeqLM.from_pretrained(modelName)
    logging.info(modelName + " model loaded")
    print("------------------------------------")
    response = {
        "status": "done"
        }
    return response, 200

@app.route('/run-model-sql', methods=['POST'])
def runModelSQL():
    body = request.get_json()
    question = body["input"]
        
    input_text = "translate English to SQL: %s </s>" % question
    features = tokenizer([input_text], return_tensors='pt')
    
    output = model.generate(input_ids=features['input_ids'], 
                 attention_mask=features['attention_mask'])
    
    answer = tokenizer.decode(output[0][1:-1])
    response = {
        "input": question,
        "output": answer,
    }
    return response, 200

