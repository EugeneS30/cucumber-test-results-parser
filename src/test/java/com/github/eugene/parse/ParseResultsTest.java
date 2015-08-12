package com.github.eugene.parse;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.record.cf.PatternFormatting;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Test;

import com.github.eugene.containers.FeatureFileElement;
import com.github.eugene.containers.Scenario;
import com.github.eugene.containers.UniqueScenario;

public class ParseResultsTest {

    final static Logger log = Logger.getLogger(ParseResultsTest.class);

    private static final String buildsDirPath = "C:\\.jenkins\\jobs\\Connect Automated Functional Tests - Firefox 32\\builds";
    private static final String jsonRelativePath = "\\htmlreports\\Functional_Test_Report\\cucumber.json";

    @Test
    public void testFunction() throws IOException {

        File dir = new File(buildsDirPath);
        File[] buildsDir = dir.listFiles();

        Map<Integer, List<FeatureFileElement>> buildsResults = new TreeMap<Integer, List<FeatureFileElement>>(Collections.reverseOrder());

        for (File buildPath : buildsDir) {

            int buildNumber = -1; // setting dummy value

            try {
                buildNumber = Integer.parseInt(buildPath.getName().toString());
            } catch (NumberFormatException e) {
                log.error("Invalid folder name: " + buildPath.getName().toString());
                continue;
            }

            log.info("Parsing buildResults: " + buildPath);

            try {
                FileReader reader = new FileReader(String.join("\\", buildPath.toString(), jsonRelativePath));

                JSONParser jsonParser = new JSONParser();

                JSONArray jsonFileData = (JSONArray) jsonParser.parse(reader);
                List<JSONObject> allJSONFileElementsList = new ArrayList<JSONObject>();
                allJSONFileElementsList.addAll(jsonFileData);

                List<FeatureFileElement> featureFileObjectsList = new ArrayList<FeatureFileElement>();

                int iter = 0;
                for (JSONObject featureFileElement : allJSONFileElementsList) {
                    log.debug("Processing new featureFileElement: " + iter);
                    iter++ ;
                    String name = (String) featureFileElement.get("name");
                    String uri = (String) featureFileElement.get("uri");
                    JSONArray elements = (JSONArray) featureFileElement.get("elements");

                    featureFileObjectsList.add(new FeatureFileElement(name, uri, elements));

                }

                buildsResults.put(buildNumber, featureFileObjectsList);

            }

            catch (FileNotFoundException e) {
                log.error("File not found: " + buildPath.toString());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        // A list of UniqueScenario objects (buildNum, scenario, isFailed)
        List<UniqueScenario> allBuildResults = new ArrayList<UniqueScenario>();

        for (Entry<Integer, List<FeatureFileElement>> build : buildsResults.entrySet()) {
            for (FeatureFileElement ffe : build.getValue()) {
                for (Scenario scenario : ffe.getScenarios()) {
                    allBuildResults.add(new UniqueScenario(build.getKey(), scenario, scenario.getRunResult()));
                }
            }
        }

        // Generates a list of unique scenario names from all the builds.
        SortedSet<String> uniqueScenarioNamesSet = new TreeSet<String>();

        for (UniqueScenario scenario : allBuildResults) {
            uniqueScenarioNamesSet.add(scenario.generateUriScenarioPair());
        }

        // A list of all build numbers
        List<Integer> buildsList = new ArrayList<Integer>();
        for (Integer buildNum : buildsResults.keySet()) {
            buildsList.add(buildNum);
        }

        /**
         * EXCEL OUTPUT
         */

        Workbook workbook = new XSSFWorkbook();

        /**
         * Pre formatting (first row and column)
         */

        // Fonts
        Font fontArial = workbook.createFont();
        fontArial.setFontHeightInPoints((short) 10);
        fontArial.setFontName("Arial");

        // Styles
        CellStyle resultCellsStyle = workbook.createCellStyle();
        resultCellsStyle.setFont(fontArial);

        Sheet resultsSheet = workbook.createSheet("Test Results");
        Sheet runStatsSheet = workbook.createSheet("Run Statistics");
        
        resultsSheet.createFreezePane(0, 1);

        Row topRow = resultsSheet.createRow(0);

        Cell cellFilename = topRow.createCell(0);
        Cell cellScenario = topRow.createCell(1);

        // Fonts are set into a style so create a new one to use.
        // CellStyle style = workbook.createCellStyle();

        cellFilename.setCellValue("Filename");
        cellScenario.setCellValue("Scenario");

        int startCol = 2;

        // Set all the column names as build numbers
        Map<Integer, Integer> buildToColumnMapper = new HashMap<Integer, Integer>();

        for (Integer buildNum : buildsList) {

            buildToColumnMapper.put(buildNum, startCol);

            Cell cell = topRow.createCell(startCol);
            cell.setCellValue(buildNum);
            startCol++ ;
        }

        Map<String, Integer> scenarioNameToRowMapper = new HashMap<String, Integer>();

        int currentRowNumber = 1;
        for (String uniqueScenario : uniqueScenarioNamesSet) {

            Row currentRow = resultsSheet.createRow(currentRowNumber);
            Cell currentFilenameCell = currentRow.createCell(0);
            Cell currentScenarioCell = currentRow.createCell(1);

            scenarioNameToRowMapper.put(uniqueScenario, currentRowNumber);

            // TODO use constants here instead of 0 and 1
            currentFilenameCell.setCellValue(uniqueScenario.split(",")[0]); // set the feature file
                                                                            // path
            currentScenarioCell.setCellValue(uniqueScenario.split(",")[1]); // set the scenario name
            currentRowNumber++ ;
        }

        CellStyle fillColorGreen = workbook.createCellStyle();
        CellStyle fillColorRed = workbook.createCellStyle();
        CellStyle fillColorYellow = workbook.createCellStyle();
        CellStyle foreGroundFill = workbook.createCellStyle();

        fillColorGreen.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        fillColorRed.setFillForegroundColor(IndexedColors.ROSE.getIndex());
        fillColorYellow.setFillForegroundColor(IndexedColors.YELLOW.getIndex());

        fillColorGreen.setFillPattern(PatternFormatting.SOLID_FOREGROUND);
        fillColorRed.setFillPattern(CellStyle.SOLID_FOREGROUND);
        fillColorYellow.setFillPattern(CellStyle.BIG_SPOTS);

        for (UniqueScenario entry : allBuildResults) {

            int buildNum = entry.getBuildNum();
            Scenario scenario = entry.getScenario();
            String scenarioRunResult = entry.getRunResult();
            String scenarioName = scenario.getScenarioName();

            log.debug("=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");
            log.debug("Processing UniqueScenario: #" + scenarioName + "#");

            int currentColNum = buildToColumnMapper.get(buildNum);
            int currentRowNum = scenarioNameToRowMapper.get(scenario.generateUriScenarioPair());

            Row currentRow = resultsSheet.getRow(currentRowNum);

            Cell currentCell = currentRow.createCell(currentColNum);
            currentCell.setCellValue(scenarioRunResult);
            currentCell.setCellStyle(resultCellsStyle);

            if ("pass".equals(scenarioRunResult)) {
                currentCell.setCellStyle(fillColorGreen);
            }

            else if ("fail".equals(scenarioRunResult)) {
                currentCell.setCellStyle(fillColorRed);
            }

            else if ("pending".equalsIgnoreCase(scenarioRunResult)) {
                currentCell.setCellStyle(fillColorYellow);
            }

            else {
                log.error(scenarioRunResult.toString());
                throw new UnsupportedOperationException(scenarioRunResult.toString());
            }

        }

        /**
         * Post
         */
        
        int lastRowNum = resultsSheet.getLastRowNum();
        int statsRowNum = lastRowNum++;
        Row statsRow = resultsSheet.createRow(lastRowNum);
        
        Cell statsCell = statsRow.createCell(1);
        
        //statsCell.setCellValue("this is it!!!");

        fontArial.setBoldweight(Font.BOLDWEIGHT_BOLD);
        resultCellsStyle.setFont(fontArial);

        for (int i = 0; i < topRow.getLastCellNum(); i++ ) {// For each cell in the row
            resultsSheet.autoSizeColumn(i);
            topRow.getCell(i).setCellStyle(resultCellsStyle);// Set the style
        }

        resultsSheet.setColumnWidth(1, 20000);

        FileOutputStream fileOut = new FileOutputStream("results.xlsx");

        log.info("=-=-=-=-=-=-=-=");
        log.info("Writing the results into excel file...");
        log.info("=-=-=-=-=-=-=-=");
        workbook.write(fileOut);
        fileOut.close();
        log.info("File saved!");
    }
}
