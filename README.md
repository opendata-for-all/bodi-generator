# Bots for Open Data Interactions (BODI) - Chatbot Generator for Open Data Sources

The objective of this project is to build a chatbot generator to interact with public open data sources such as .csv 
files so every citizen can get relevant information from a dataset in an easy way (via a chatbot interface).

The generated chatbots are built with [Xatkit](https://github.com/xatkit-bot-platform/xatkit), an open-source chatbot
development platform.


## Quick Start

1 - [Build the latest version of Xatkit](https://github.com/xatkit-bot-platform/xatkit/wiki/Build-Xatkit)

2 - Install [xatkit-bot-platform/labs-bot-testing-tools](https://github.com/xatkit-bot-platform/labs-bot-testing-tools)

3 - Clone this repository

4 - Edit the [bodi-generator.properties](src/main/resources/bodi-generator.properties) file according to your purposes 
(see [Bot Configuration](#bot-configuration))

5 - Navigate to the root directory and build the project:

```bash
mvn clean compile
```

6 - You can run the bot generator!

```bash
mvn exec:java -Dexec.mainClass="bodi.generator.BodiGenerator"
```

7 - If you want to empower your chatbot with NLP functionalities (to provide answers to questions not implemented in 
the chatbot), you have to deploy a server that runs language models to perform NLP tasks. This server can be found 
at [opendata-for-all/bodi-nlp-server](https://github.com/opendata-for-all/bodi-nlp-server)
(see [NLP Server Configuration](#nlp-server-configuration)).

8 - Once the chatbot is generated, you can run it:
```bash
cd <bot-folder>
mvn clean compile
```

To run the chatbot tests:

```bash
mvn test
```

To run the chatbot itself:

```bash
mvn exec:java -Dexec.mainClass="com.xatkit.bot.Bot"
```

> 📚 Check the Xatkit [Wiki](https://github.com/xatkit-bot-platform/xatkit/wiki) to learn more about Xatkit chatbots.

## Bot Configuration

### bodi-generator.properties

You can customize your generated chatbots according to your objectives. Here are described the properties you can 
set in [bodi-generator.properties](src/main/resources/bodi-generator.properties)

| Name                     | Description                                                                                                                                                                                                                                                                  |
|--------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `xls.generator.bot.name` | The name of the generated chatbot                                                                                                                                                                                                                                            |
| `xls.generator.output`   | The path where the chatbot will be stored                                                                                                                                                                                                                                    |
| `xls.importer.xls`       | The name of the tabular data file the chatbot will have access to, stored in the [resources](src/main/resources) folder (currently only `csv` files are supported)                                                                                                           |
| `csv.delimiter`          | The delimiter or separator of the tabular data file cells (e.g. `,`, `\t` (tab))                                                                                                                                                                                             |
| `enable_testing`         | `true` if you want to enable chatbot testing, `false` otherwise                                                                                                                                                                                                              |
| `maxNumDifferentValues`  | the maximum number of different values that a field must have to add them to the generated `fields.json` file (e.g. 15)                                                                                                                                                      |
| `xatkit.intent.provider` | The intent provider the generated chatbot will use. The currently available intent providers are: Dialogflow (`com.xatkit.core.recognition.dialogflow.DialogFlowIntentRecognitionProvider`) and  NLP.js (`com.xatkit.core.recognition.nlpjs.NlpjsIntentRecognitionProvider`) |
| `xatkit.logs.database`   | The database used to store the chatbot tracing: `com.xatkit.core.recognition.RecognitionMonitorPostgreSQL`. If there is no database, leave this property empty.                                                                                                              |

### bot.properties

Once the chatbot is generated, it will contain a file named `src/main/resources/bot.properties`, containing some 
duplicated properties from [bodi-generator.properties](src/main/resources/bodi-generator.properties) and also new 
properties. These will contain default values, so they should be set according to the chatbot purpose.

| Name                                   | Description                                                                                                                                                                                       |
|----------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `xatkit.server.port`                   | The port where the chatbot will be running                                                                                                                                                        |
| `bot.language`                         | The language of the bot (users must talk to the chatbot in this language, and it also replies in this language). Available languages: **Spanish** (`es`),  **Catalan** (`ca`), **English** (`en`) |
| `bot.pageLimit`                        | The maximum number of entries of a table that are displayed at once in the chatbot chat box, that is, the page size (e.g. 10)                                                                     |
| `bot.maxEntriesToDisplay`              | If the number of entries of a result set generated after a chatbot query is less or equal than this number, the result set is displayed immediately afterwards (e.g. 7)                           |
| `xatkit.nlpjs.*`                       | All the properties related to [NLP.js](https://github.com/xatkit-bot-platform/xatkit/wiki/Using-NLP.js) engine (if used)                                                                          |
| `xatkit.dialogflow.*`                  | All the properties related to [DialogFlow](https://github.com/xatkit-bot-platform/xatkit/wiki/Integrating-DialogFlow) engine (if used)                                                            |
| `xatkit.recognition.enable_monitoring` | If `true`, enables chatbot monitoring, if `false`, not                                                                                                                                            |
| `xatkit.postgresql*`                   | All the properties related to [PostgreSQL](https://github.com/xatkit-bot-platform/xatkit/wiki/Using-PostgreSQL) database (if used)                                                                |
| `bot.odata.title.*`                    | (Optional) The title of the open data resource used by the bot, in different languages (e.g. `bot.odata.title.es`). The bot will read the one matching with `bot.language`                        |
| `bot.odata.url.*`                      | (Optional) The url of the open data resource used by the bot, in different languages (e.g. `bot.odata.url.es`). The bot will read the one matching with `bot.language`                            |

> 📚 It is highly recommended using an NLP engine supported in Xatkit (DialogFlow or NLP.js). Do not include 
> properties for both NLP engines.

There are a lot of other properties you can add to your bot, check them out [here](https://github.com/xatkit-bot-platform/xatkit/wiki/Xatkit-Options).

### NLP Server Configuration

We provide a solution for when a user utterance is not matched with a bot intent. The objective is to empower the 
chatbot with language models able to find answers to a wide range of inputs. A server running these models can be 
deployed and then, when no intent is matched, a request to this server is sent and a response is received. Our 
solution is implemented in [opendata-for-all/bodi-nlp-server](https://github.com/opendata-for-all/bodi-nlp-server).

Here are described the properties you can
set in `bot.properties` related to the NLP Server:

| Name                     | Description                                                               |
|--------------------------|---------------------------------------------------------------------------|
| `SERVER_URL`             | The URL where the server is hosted (e.g. `127.0.0.1:5002`)                |
| `TEXT_TO_TABLE_ENDPOINT` | The server endpoint that runs a query in the Text-to-Table language model |
