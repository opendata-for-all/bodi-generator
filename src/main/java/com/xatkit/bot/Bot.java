package com.xatkit.bot;

import bodiGenerator.dataSource.TabularDataSource;
import com.xatkit.bot.customQuery.CustomQuery;
import com.xatkit.bot.library.ContextKeys;
import com.xatkit.bot.library.Entities;
import com.xatkit.bot.library.Intents;
import com.xatkit.bot.library.Utils;
import com.xatkit.bot.showData.ShowData;
import com.xatkit.bot.structuredQuery.SelectViewField;
import com.xatkit.bot.structuredQuery.StructuredFilter;
import com.xatkit.core.XatkitBot;
import com.xatkit.core.recognition.IntentRecognitionProviderFactoryConfiguration;
import com.xatkit.core.recognition.nlpjs.NlpjsConfiguration;
import com.xatkit.plugins.react.platform.ReactPlatform;
import com.xatkit.plugins.react.platform.io.ReactEventProvider;
import com.xatkit.plugins.react.platform.io.ReactIntentProvider;
import lombok.val;
import org.apache.commons.configuration2.BaseConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;

import static com.xatkit.dsl.DSL.eventIs;
import static com.xatkit.dsl.DSL.fallbackState;
import static com.xatkit.dsl.DSL.intentIs;
import static com.xatkit.dsl.DSL.model;
import static com.xatkit.dsl.DSL.state;

/*
 * This is an automatically generated bot
 */
public class Bot {

	public static String LANGUAGE = "cat";
	public static Locale LOCALE = new Locale(LANGUAGE);
	//public static CoreLibraryI18n CoreLibrary = new CoreLibraryI18n(LOCALE);
	public static ResourceBundle messages = ResourceBundle.getBundle("messages", LOCALE);

	public static void main(String[] args) {


		/*
		 * Instantiate the platform and providers we will use in the bot definition.
		 */
		ReactPlatform reactPlatform = new ReactPlatform();
		ReactEventProvider reactEventProvider = reactPlatform.getReactEventProvider();
		ReactIntentProvider reactIntentProvider = reactPlatform.getReactIntentProvider();

		/*
		 * Create the states we want to use in our bot.
		 */
		val init = state("Init");
		val awaitingInput = state("AwaitingInput");
		val startState = state("StartState");

		StructuredFilter structuredFilter = new StructuredFilter(reactPlatform, startState);
		ShowData showData = new ShowData(reactPlatform, startState);
		SelectViewField selectViewField = new SelectViewField(reactPlatform, startState);
		CustomQuery customQuery = new CustomQuery(reactPlatform, startState);

		/*
		 * Specify the content of the bot states (i.e. the behavior of the bot).
		 *
		 */
		init
				.next()
				/*
				 * We check that the received event matches the ClientReady event defined in the
				 * ReactEventProvider. The list of events defined in a provider is available in the provider's
				 * wiki page.
				 */
				.when(eventIs(ReactEventProvider.ClientReady)).moveTo(awaitingInput)
		;
		awaitingInput
				.body(context -> {
							if (!context.getSession().containsKey(ContextKeys.tabularDataSource)) {
								context.getSession().put(ContextKeys.tabularDataSource,
										new TabularDataSource(Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource(Utils.getInputDocName())).getPath()));
							}
							List<String> fields = new ArrayList<>();
							fields.addAll(Utils.getEntityValues(Entities.numericFieldEntity));
							fields.addAll(Utils.getEntityValues(Entities.textualFieldEntity));
							TabularDataSource tds = (TabularDataSource) context.getSession().get(ContextKeys.tabularDataSource);
							context.getSession().put(ContextKeys.statement, tds.createStatement());
							List<String> filterFieldOptions = new ArrayList<>(fields);
							context.getSession().put(ContextKeys.filterFieldOptions, filterFieldOptions);
							List<String> viewFieldOptions = new ArrayList<>(fields);
							context.getSession().put(ContextKeys.viewFieldOptions, viewFieldOptions);
							reactPlatform.reply(context, messages.getString("DataStructuresInitialized"));
						}
				)
				.next()
				.moveTo(startState)
		;
		startState
				.body(context -> {
							reactPlatform.reply(context, messages.getString("SelectOperation"),
									Utils.getFirstTrainingSentences(
											Intents.addFilterIntent,
											Intents.addFieldToViewIntent,
											Intents.showDataIntent,
											Intents.customQueryIntent,
											Intents.restartIntent));
						}
				)
				.next()
				.when(intentIs(Intents.addFilterIntent)).moveTo(structuredFilter.getSelectFilterFieldState())
				.when(intentIs(Intents.addFieldToViewIntent)).moveTo(selectViewField.getSelectViewFieldState())
				.when(intentIs(Intents.showDataIntent)).moveTo(showData.getShowDataState())
				.when(intentIs(Intents.customQueryIntent)).moveTo(customQuery.getAwaitingCustomQueryState())
				.when(intentIs(Intents.restartIntent)).moveTo(awaitingInput)
		;

		/*
		 * The state that is executed if the engine doesn't find any navigable transition in a state and the state
		 * doesn't contain a fallback.
		 */
		val defaultFallback = fallbackState()
				.body(context -> reactPlatform.reply(context, messages.getString("DefaultFallbackMessage")));

		/*
		 * Creates the bot model that will be executed by the Xatkit engine.
		 */
		val botModel = model()
				.usePlatform(reactPlatform)
				.listenTo(reactEventProvider)
				.listenTo(reactIntentProvider)
				.initState(init)
				.defaultFallbackState(defaultFallback);

		Configuration botConfiguration = new BaseConfiguration();
		/*
		 * Add configuration properties (e.g. authentication tokens, platform tuning, intent provider to use).
		 * Check the corresponding platform's wiki page for further information on optional/mandatory parameters and
		 * their values.
		 */
		Configurations configurations = new Configurations();
		try {
			botConfiguration = configurations.properties(Thread.currentThread().getContextClassLoader().getResource("bot.properties"));
		} catch (ConfigurationException e) {
			e.printStackTrace();
			System.out.println("Configuration file not found");
		}

		XatkitBot xatkitBot = new XatkitBot(botModel, botConfiguration);
		xatkitBot.run();
		/*
		 * The bot is now started, you can check http://localhost:5000/admin to test it.
		 * The logs of the bot are stored in the logs folder at the root of this project.
		 */
	}
}
