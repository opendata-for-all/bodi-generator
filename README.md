# Bots for Open Data Interactions (BODI) - Chatbot Generator for Open Data Sources

The objective of this project is to build a chatbot generator to interact with public open data sources such as .csv 
files so every citizen can get relevant information from a dataset in an easy way (via a chatbot interface).

The generated chatbots are built with [Xatkit](https://github.com/xatkit-bot-platform/xatkit), an open-source chatbot
development platform.


## Quick Start

1 - [Build the latest version of Xatkit](https://github.com/xatkit-bot-platform/xatkit/wiki/Build-Xatkit)

2 - Install [xatkit-bot-platform/labs-bot-testing-tools](https://github.com/xatkit-bot-platform/labs-bot-testing-tools) 
and [xatkit-bot-platform/xatkit-core-library-i18n](https://github.com/xatkit-bot-platform/xatkit-core-library-i18n)

3 - Clone this repository


4 - Navigate to the root directory and build the project:

```bash
mvn clean compile
```

5 - You can run the bot generator!

```bash
mvn exec:java -Dexec.mainClass="mvn exec:java -Dexec.mainClass="bodi.generator.ui.Application""
```

Then, you can access the bodi-generator UI at [http://localhost:8080/bodi-generator/](http://localhost:8080/bodi-generator/)

6 - We provide a solution for when a user utterance is not matched with a bot intent. The objective is to empower the
chatbot with language models able to find answers to a wide range of inputs. A server running these models can be
deployed and then, when no intent is matched, a request to this server is sent and a response is received. Our
solution is implemented in [opendata-for-all/bodi-nlp-server](https://github.com/opendata-for-all/bodi-nlp-server).

7 - Generated chatbots use [opendata-for-all/bodi-drillbit](https://github.com/opendata-for-all/bodi-drillbit) to query
the database containing the tabular data.

8 - You must use an intent recognition provider to detect user's intents. We recommend using our own implementation
[xatkit-nlu-server](https://github.com/xatkit-bot-platform/xatkit-nlu-server) with its bot
[client](https://github.com/xatkit-bot-platform/xatkit-nlu-client), although it is possible to use other options such as
[Dialogflow](https://github.com/xatkit-bot-platform/xatkit/wiki/Integrating-DialogFlow) or
[NLP.JS](https://github.com/xatkit-bot-platform/xatkit/wiki/Using-NLP.js)

9 - Once the chatbot is generated, you can run it:
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
mvn exec:java -Dexec.mainClass="com.xatkit.bot.App"
```

> 📚 Check the Xatkit [Wiki](https://github.com/xatkit-bot-platform/xatkit/wiki) to learn more about Xatkit chatbots.
