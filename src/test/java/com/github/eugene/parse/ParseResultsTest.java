package com.github.eugene.parse;

import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.junit.Test;

import com.github.eugene.config.PropertiesContainer;
import com.github.eugene.containers.FeatureFileElement;
import com.github.eugene.containers.UniqueScenario;
import com.github.eugene.processing.DataParse;
import com.github.eugene.processing.ExcelProcessor;

public class ParseResultsTest {

	@Test
	public void testFunction() throws Exception {

		// Parsing the data from all builds to a map of form (buildNumber,
		// FeatureFileElement)
		Map<Integer, List<FeatureFileElement>> buildsData = DataParse.extractBuildsData();

		// A list of UniqueScenario objects (buildNum, scenario, isFailed)
		List<UniqueScenario> uniqueScenariosList = DataParse.extractUniqueScenarios(buildsData);
		List<UniqueScenario> uniqueScenariosListCleaned = DataParse.removeLegacyScenarios(uniqueScenariosList);

		if (PropertiesContainer.getProperty("removeLegacyScenarios").equals("true")) {

			// Generates a list of unique scenario names from all the builds.
			SortedSet<String> uniqueScenarioNamesSet = DataParse.createUniqueScenriosNames(uniqueScenariosListCleaned);

			// A list of all builds numbers
			List<Integer> allBuildsNumbersList = DataParse.getAllBuildsNumbers(buildsData);

			ExcelProcessor.prepareProcessor(allBuildsNumbersList, uniqueScenarioNamesSet);
			ExcelProcessor.processData(uniqueScenariosListCleaned);

		} else if (PropertiesContainer.getProperty("removeLegacyScenarios").equals("false")) {

			SortedSet<String> uniqueScenarioNamesSet = DataParse.createUniqueScenriosNames(uniqueScenariosList);

			// A list of all builds numbers
			List<Integer> allBuildsNumbersList = DataParse.getAllBuildsNumbers(buildsData);

			ExcelProcessor.prepareProcessor(allBuildsNumbersList, uniqueScenarioNamesSet);
			ExcelProcessor.processData(uniqueScenariosList);

		} else {

			throw new Exception("removeLegacyScenarios property must be set to false or true");

		}

		ExcelProcessor.finaliseProcessor();

	}
}
