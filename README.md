# Bots for Open Data Interactions (BODI) - Chatbot Generator for Open Data Sources

The objective of this project is to build a chatbot generator to interact with public open data sources such as .csv 
files so every citizen can get relevant information from a dataset in an easy way (via a chatbot interface).

The generated chatbots are built with [Xatkit](https://github.com/xatkit-bot-platform/xatkit), an open-source chatbot
development platform.


## Quick Start

1 - [Build the latest version of Xatkit](https://github.com/xatkit-bot-platform/xatkit/wiki/Build-Xatkit)

2 - [Install xatkit-metamodel-simple](https://github.com/xatkit-bot-platform/xatkit-metamodel-simple)

3 - Clone this repository

4 - Edit the [bot.properties](src/main/resources/bot.properties) file according to your purposes (see [Bot 
Configuration](#bot-configuration))

5 - Navigate to the root directory and build the project:

```bash
mvn clean compile
```

6 - You can run the bot generator!

```bash
mvn exec:java -Dexec.mainClass="bodi.generator.BodiGenerator"
```

8 - If you want to empower your chatbot with the default fallback state (recommended), you have to deploy a server
running a language model (see [DefaultFallback Configuration](#defaultfallback-configuration)). Currently, 2
solutions are available:

- Text-to-SQL language model:
  ```bash
  python3 transformers_server.py
  ```
- Text-to-Table language model: [mgv99/TabularSemanticParsing](https://github.com/mgv99/TabularSemanticParsing)

9 - Once the chatbot is generated, you can run it:
```bash
cd <bot-folder>
mvn clean compile
mvn exec:java -Dexec.mainClass="com.xatkit.bot.Bot"
```

> 📚 Check the Xatkit [Wiki](https://github.com/xatkit-bot-platform/xatkit/wiki) to learn more about Xatkit chatbots.

## Bot Configuration

You can customize your generated chatbots according to your objectives. Here are described the properties you can 
set in [bot.properties](src/main/resources/bot.properties)

| Name                     | Description                                                                                                                                                                  |
|--------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `xls.generator.bot.name` | The name of the generated chatbot                                                                                                                                            |
| `xls.generator.output`   | The path where the chatbot will be stored                                                                                                                                    |
| `xls.importer.xls`       | The name of the tabular data file the chatbot will have access to, stored in the [resources](src/main/resources) folder (currently only `csv` files are supported)           |
| `xatkit.server.port`     | The port where the chatbot will be running                                                                                                                                   |
| `bot.language`           | The language of the bot (users must talk to the chatbot in this language, and it also replies in this language). Available languages: **Catalan** (`ca`), **English** (`en`) |
| `xatkit.nlpjs.*`         | All the properties related to [NLP.js](https://github.com/xatkit-bot-platform/xatkit/wiki/Using-NLP.js) engine                                                               |
| `xatkit.dialogflow.*`    | All the properties related to [DialogFlow](https://github.com/xatkit-bot-platform/xatkit/wiki/Integrating-DialogFlow) engine                                                 |

> 📚 It is highly recommended to use an NLP engine supported in Xatkit (DialogFlow or NLP.js). Do not include 
> properties for both NLP engines.

There are a lot of other properties you can add to your bot, check them out [here](https://github.com/xatkit-bot-platform/xatkit/wiki/Xatkit-Options).

### DefaultFallback Configuration

When a user utterance is not matched with a bot intent, the DefaultFallback state is executed. We provide a 
technique to empower the chatbot with language models able to find answers to a wide range of inputs. A server 
running a language model can be deployed and then, from the DefaultFallback state, a request to this server is sent 
and a response is received. We consider 2 kinds of language models: 
- **Text-to-SQL**: The input of the language model is a natural language utterance. The output is a SQL statement 
  equivalent to the utterance. This SQL statement is then executed within the tabular data file of the chatbot to 
  obtain a result to the original utterance.

  See this example model: [mrm8488/t5-base-finetuned-wikiSQL](https://huggingface.co/mrm8488/t5-base-finetuned-wikiSQL)

- **Text-to-Table**: The input of the language model is a natural language utterance (together 
  with a tabular data source such as a `.csv` table). The output is the answer to the utterance (that is, a tabular 
  data result obtained from the original tabular data provided).

  See this example model: [google/tapas-base-finetuned-wtq](https://huggingface.co/google/tapas-base-finetuned-wtq) and
  [**our current favourite solution**](Bridging Textual and Tabular Data for Cross-Domain Text-to-SQL Semantic Parsing):

Here are described the properties you can
set in [bot.properties](src/main/resources/defaultFallback.properties) related to the default fallback state:

| Name                       | Description                                                                                                                                   |
|----------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------|
| `MODEL_NAME_SQL`           | The name of the Text-to-SQL language model (a [Huggingface endpoint](https://huggingface.co/models), e.g. `mrm8488/t5-base-finetuned-wikiSQL` |
| `SERVER_URL`               | The URL where the server is hosted (e.g. `127.0.0.1:5002`)                                                                                    |
| `RUN_MODEL_ENDPOINT_SQL`   | The server endpoint that runs a query in the Text-to-SQL language model                                                                       |
| `RUN_MODEL_ENDPOINT_TABLE` | The server endpoint that runs a query in the Text-to-Table language model                                                                     |
