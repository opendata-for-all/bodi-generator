from flask import Flask, request
from transformers import AutoTokenizer, AutoModelForSeq2SeqLM
import logging
import configparser
from io import StringIO

logging.basicConfig(level=logging.INFO)
ROOT_SECTION = 'root'
CONFIG_FILE_PATH = 'Bot/src/main/resources/defaultFallback.properties'

def loadConfig():
    ini_str = '[' + ROOT_SECTION + ']\n' + open(CONFIG_FILE_PATH, 'r').read()
    ini_fp = StringIO(ini_str)
    config = configparser.RawConfigParser()
    config.read_file(ini_fp)
    return config

def setModelSeq2SeqLM(modelName):
    logging.info("Loading " + modelName + " tokenizer")
    tokenizer = AutoTokenizer.from_pretrained(modelName)
    logging.info(modelName + " tokenizer loaded")
    logging.info("Loading " + modelName + " model")
    model = AutoModelForSeq2SeqLM.from_pretrained(modelName)
    logging.info(modelName + " model loaded")
    return model, tokenizer

config = loadConfig()
model, tokenizer = setModelSeq2SeqLM(config.get(ROOT_SECTION, 'MODEL_NAME'))

app = Flask(__name__)
app.config['SERVER_NAME'] = config.get(ROOT_SECTION, 'SERVER_URL')

@app.route('/' + config.get(ROOT_SECTION, 'RUN_MODEL_ENDPOINT_SQL'), methods=['POST'])
def runModelSQL():
    body = request.get_json()
    question = body["input"]
        
    input_text = "translate English to SQL: %s </s>" % question
    features = tokenizer([input_text], return_tensors='pt')
    
    output = model.generate(input_ids=features['input_ids'], 
                 attention_mask=features['attention_mask'])
    
    answer = tokenizer.decode(output[0][1:-1]) # Ignore the beginning and end special tokens
    response = {
        "input": question,
        "output": answer,
    }
    return response, 200

if __name__ == '__main__':
    app.run(debug=False)
    