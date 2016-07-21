package com.github.eugene.parse;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.junit.Test;

import com.github.eugene.config.ConfigurationClass;
import com.github.eugene.containers.FeatureFileElement;
import com.github.eugene.containers.UniqueScenario;
import com.github.eugene.processing.DataParse;
import com.github.eugene.processing.ExcelProcessor;

public class ParseResultsTest {

    @Test
    public void testFunction() throws IOException {

        // Parsing the data from all builds to a map of form (buildNumber, FeatureFileElement)
        Map<Integer, List<FeatureFileElement>> buildsData = DataParse.extractBuildsData();

        // A list of UniqueScenario objects (buildNum, scenario, isFailed)
        List<UniqueScenario> uniqueScenariosList = DataParse.extractUniqueScenarios(buildsData);
        List<UniqueScenario> uniqueScenariosListCleaned = DataParse.removeLegacyScenarios(uniqueScenariosList);

        if (ConfigurationClass.removeLegacyScenarios) {

            // Generates a list of unique scenario names from all the builds.
            SortedSet<String> uniqueScenarioNamesSet = DataParse.createUniqueScenriosNames(uniqueScenariosListCleaned);

            // A list of all builds numbers
            List<Integer> allBuildsNumbersList = DataParse.getAllBuildsNumbers(buildsData);

            ExcelProcessor.prepareProcessor(allBuildsNumbersList, uniqueScenarioNamesSet);
            ExcelProcessor.processData(uniqueScenariosListCleaned);

        } else {

            SortedSet<String> uniqueScenarioNamesSet = DataParse.createUniqueScenriosNames(uniqueScenariosList);

            // A list of all builds numbers
            List<Integer> allBuildsNumbersList = DataParse.getAllBuildsNumbers(buildsData);

            ExcelProcessor.prepareProcessor(allBuildsNumbersList, uniqueScenarioNamesSet);
            ExcelProcessor.processData(uniqueScenariosList);
        }

        ExcelProcessor.finaliseProcessor();

    }
}
