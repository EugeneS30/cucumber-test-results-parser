package com.github.eugene.parse;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import lombok.extern.slf4j.Slf4j;

import org.apache.poi.ss.formula.functions.Column;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.github.eugene.containers.FeatureFileElement;
import com.github.eugene.containers.Scenario;

@Slf4j
public class ParseResults {

    // private static final String filePath =
    // "C:\\Users\\Polar\\Google Drive\\work\\376\\cucumber.json";
    private static final String buildsDirPath = "C:\\.jenkins\\jobs\\Connect Automated Functional Tests - Firefox 32\\builds";
    private static final String jsonRelativePath = "\\htmlreports\\Functional_Test_Report\\cucumber.json";

    // private static final String myDirectoryPath = "C:\\Users\\Polar\\Google Drive\\work";

    public static void main(String[] args) throws Exception {
        
        File dir = new File(buildsDirPath);
        File[] buildsDir = dir.listFiles();

        // List<List<FeatureFileElement>> buildResults = new ArrayList<List<FeatureFileElement>>();
        Map<Integer, List<FeatureFileElement>> buildsResults = new TreeMap<Integer, List<FeatureFileElement>>(Collections.reverseOrder());
        
        for (File buildPath : buildsDir) {
            int buildNumber = Integer.parseInt(buildPath.getName().toString());

            try {
                FileReader reader = new FileReader(String.join("\\", buildPath.toString(), jsonRelativePath));

                JSONParser jsonParser = new JSONParser();

                JSONArray jsonFileData = (JSONArray) jsonParser.parse(reader);
                List<JSONObject> allJSONFileElementsList = new ArrayList<JSONObject>();
                allJSONFileElementsList.addAll(jsonFileData);

                List<FeatureFileElement> featureFileObjectsList = new ArrayList<FeatureFileElement>();

                int iter = 0;
                for (JSONObject featureFileElement : allJSONFileElementsList) {
                    log.info("Processing new featureFileElement: " + iter);
                    iter++ ;
                    String name = (String) featureFileElement.get("name");
                    String uri = (String) featureFileElement.get("uri");
                    JSONArray elements = (JSONArray) featureFileElement.get("elements");

                    featureFileObjectsList.add(new FeatureFileElement(name, uri, elements));
                    
                    // buildResults.add(featureFileObjectsList);

                }

                buildsResults.put(buildNumber, featureFileObjectsList);
                
            }

            catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        
        // A list of UniqueScenario objects (buildNum, scenario, isFailed)
        List<UniqueScenario> allBuildResults = new ArrayList<UniqueScenario>();
        
        for (Entry<Integer, List<FeatureFileElement>> build: buildsResults.entrySet()) {
            for (FeatureFileElement ffe : build.getValue()){
                for (Scenario scenario : ffe.getScenarios()) {
                    allBuildResults.add(new UniqueScenario(build.getKey(), scenario, scenario.isFailed()));
                }
            }
        }
        
        // Generates a list of unique scenario names from all the builds.
        Set<String> uniqueScenarioNamesSet = new HashSet<String>();
        
        for (UniqueScenario scenario : allBuildResults) {
            uniqueScenarioNamesSet.add(scenario.generateUriScenarioPair());
        }
         
        // A list of all build numbers
        List<Integer> buildsList = new ArrayList<Integer>();
        for (Integer buildNum : buildsResults.keySet()) {
            buildsList.add(buildNum);
        }
        
        //Map<String, List<Map<Integer, Boolean>>> finalResultsMap = new HashMap<String, List<Map<Integer, Boolean>>>();
        
        Workbook wb = new XSSFWorkbook();

        // CellStyle style = wb.createCellStyle();
        // style = wb.createCellStyle();
        // style.setFillForegroundColor(IndexedColors.ORANGE.getIndex());
        // style.setFillPattern(CellStyle.SOLID_FOREGROUND);

        Sheet sheet = wb.createSheet("Test Results");
        Row topRow = sheet.createRow(0);
        
        Cell cellFilename = topRow.createCell(0);
        Cell cellScenario = topRow.createCell(1);
        
        cellFilename.setCellValue("Filename");
        cellScenario.setCellValue("Scenario");

        
        int startCol = 2;
                
        // Set all the column names as build numbers
        Map<Integer, Integer> buildToColumnMapper = new HashMap<Integer, Integer>();
        
        for (Integer buildNum : buildsList) {
            
            buildToColumnMapper.put(buildNum, startCol);
            
            Cell cell = topRow.createCell(startCol);
            cell.setCellValue(buildNum);
            startCol++;
        }
        
        Map<String, Integer> scenarioNameToRowMapper = new HashMap<String, Integer>();
        
        int currentRowNumber = 1;
        for (String uniqueScenario : uniqueScenarioNamesSet) {
            
            Row currentRow = sheet.createRow(currentRowNumber);
            Cell currentFilenameCell = currentRow.createCell(0);
            Cell currentScenarioCell = currentRow.createCell(1);
            
            scenarioNameToRowMapper.put(uniqueScenario.split(",")[1], currentRowNumber);
            
            //TODO use constants here instead of 0 and 1
            currentFilenameCell.setCellValue(uniqueScenario.split(",")[0]); //set the feature file path
            currentScenarioCell.setCellValue(uniqueScenario.split(",")[1]); //set the scenario name
            currentRowNumber++;
        }
        
        
        
        //int currentCol = -1;
        //int currentRow = -1;
        for (UniqueScenario entry : allBuildResults) {
            System.out.println("=-=-=-=-=-=-=-=-=-=-=");
            
            //List<FeatureFileElement> featureFileElements = entry.getValue();

            int buildNum = entry.getBuildNum();
            Scenario scenario = entry.getScenario();
            boolean scenarioFailStatus = entry.isFailed();
            
//            if (finalResultsMap.get(scenario.getUri()) != null) {
//                finalResultsMap.put(buildNum, scenario.isFailed())
//            }
            
            int currentColNum = buildToColumnMapper.get(buildNum);
            int currentRowNum = scenarioNameToRowMapper.get(scenario.getScenarioName());
            
            log.info("Current cell and row:");
            System.out.println(currentColNum + "," + currentRowNum);
            
            Row currentRow = sheet.createRow(currentRowNum);
            
            Cell cell = currentRow.createCell(currentColNum);
            cell.setCellValue(scenarioFailStatus);
            
            
            //cellBuildResult.setCellValue();
            
            /*
             * Iterate over FeatureFileElements (currently 58)
             */
//            int j = 0;
//            for (FeatureFileElement ffe : featureFileElements) {
//                j++;
//                Map<String, Boolean> resultPairs = ffe.generateScenarioResultPairs();
//                
//                for (Map.Entry<String, Boolean> resultPair : resultPairs.entrySet()) {
//                    Cell c = rowTest.createCell(startCol);
//                }
//                
//            }
//            System.out.println(j);


//            for (FeatureFileElement featureElement : featureElements) {
//                Row rowTest = sheet.createRow(startRow);
//                for (Scenario scenario : featureElement.getScenarios()) {
//                    Cell cellBuildResult = rowTest.createCell(startCol);
//                    cellBuildResult.setCellValue(scenario.getScenarioName() + scenario.getScenarioType());
//                    startRow++;
//                }
//            }

            startCol++ ;
        }

        FileOutputStream fileOut = new FileOutputStream("workbook.xlsx");

        wb.write(fileOut);
        fileOut.close();

    }
}

