package com.xatkit.bot.showData;

import bodiGenerator.dataSource.ResultSet;
import bodiGenerator.dataSource.Statement;
import com.xatkit.bot.library.ContextKeys;
import com.xatkit.bot.library.Intents;
import com.xatkit.bot.library.Utils;
import com.xatkit.dsl.state.StateProvider;
import com.xatkit.execution.State;
import com.xatkit.plugins.react.platform.ReactPlatform;
import lombok.Getter;
import lombok.val;

import java.text.MessageFormat;
import java.util.stream.Collectors;

import static com.xatkit.bot.Bot.messages;
import static com.xatkit.dsl.DSL.intentIs;
import static com.xatkit.dsl.DSL.state;

public class ShowData {

    @Getter
    private State showDataState;

    public ShowData(ReactPlatform reactPlatform, StateProvider returnState) {
        val showDataState = state("ShowData");

        showDataState
                .body(context -> {
                            Statement statement = (Statement) context.getSession().get(ContextKeys.statement);
                            ResultSet resultSet = statement.executeQuery();
                            int pageLimit = 10;
                            int pageCount = 1;
                            if (context.getIntent().getMatchedInput().equals(Intents.showNextPageIntent.getTrainingSentences().get(0))) {
                                pageCount = (int) context.getSession().get(ContextKeys.pageCount) + 1;
                            }
                            int totalEntries = resultSet.getNumRows();
                            int totalPages = totalEntries / pageLimit;
                            if (totalEntries % pageLimit != 0) {
                                totalPages += 1;
                            }
                            if (pageCount > totalPages) {
                                // Page overflow
                                pageCount = 1;
                            }
                            int offset = (pageCount - 1) * pageLimit;
                            context.getSession().put(ContextKeys.pageCount, pageCount);

                            if (totalEntries > 0) {
                                // Print table
                                String header =
                                        "|" + String.join("|", resultSet.getHeader()) + "|" + "\n" +
                                                "|" + String.join("|", resultSet.getHeader().stream().map(e ->"---").collect(Collectors.joining("|"))) + "|" + "\n";
                                String data = "";
                                for (int i = offset; i < totalEntries && i < offset + pageLimit; i++) {
                                    data += "|" + String.join("|", resultSet.getRow(i).getValues()) + "|" + "\n";
                                }
                                int selectedEntries = (offset + pageLimit > totalEntries ? totalEntries - offset : pageLimit);
                                reactPlatform.reply(context, MessageFormat.format(messages.getString("ShowingRecords"), selectedEntries, totalEntries));
                                reactPlatform.reply(context, MessageFormat.format(messages.getString("PageCount"), pageCount, totalPages));
                                reactPlatform.reply(context, header + data, Utils.getFirstTrainingSentences(
                                        Intents.showNextPageIntent,
                                        Intents.stopViewIntent));
                            } else {
                                reactPlatform.reply(context, messages.getString("NothingFound"),
                                        Utils.getFirstTrainingSentences(Intents.stopViewIntent));
                            }
                        }
                )
                .next()
                .when(intentIs(Intents.showNextPageIntent)).moveTo(showDataState)
                .when(intentIs(Intents.stopViewIntent)).moveTo(returnState)
                ;
        this.showDataState = showDataState.getState();
    }
}
