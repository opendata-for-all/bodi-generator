# Bots for Open Data Interactions (BODI) - Chatbot Generator for Open Data Sources

The objective of this project is to build a chatbot generator to interact with public open data sources such as .csv 
files so every citizen can get relevant information from a dataset in an easy way (via a chatbot interface).

The generated chatbots are built with [Xatkit](https://github.com/xatkit-bot-platform/xatkit), an open-source chatbot
development platform.


## Quick Start

1- [Build the latest version of Xatkit](https://github.com/xatkit-bot-platform/xatkit/wiki/Build-Xatkit)

2- [Install xatkit-metamodel-simple](https://github.com/xatkit-bot-platform/xatkit-metamodel-simple)

3- Clone this repository

4- Edit the [bot.properties](src/main/resources/bot.properties) file according to your purposes (see [Bot 
Configuration](#bot-configuration))

5- Navigate to the root directory and build the project:

```bash
mvn clean compile
```

6- You can run the bot generator!

```bash
mvn exec:java -Dexec.mainClass="bodi.generator.BodiGenerator"
```

7- Once the chatbot is generated, you can run it:
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
