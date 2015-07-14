package com.github.eugene.parse;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import lombok.extern.slf4j.Slf4j;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.github.eugene.config.AppConfig;
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
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
        
        
        
        Workbook wb = new XSSFWorkbook();
        
//        CellStyle style = wb.createCellStyle(); 
//        style = wb.createCellStyle();
//        style.setFillForegroundColor(IndexedColors.ORANGE.getIndex());
//        style.setFillPattern(CellStyle.SOLID_FOREGROUND);
        
        Sheet sheet = wb.createSheet("Test Results");
        Row row = sheet.createRow(0);
        
        Cell cellFilename = row.createCell(0);
        Cell cellScenario = row.createCell(1);
        
        cellFilename.setCellValue("Filename");
        cellScenario.setCellValue("Scenario");
                
        /*
         * Iterate over FeatureFileElement objects
         */
        int startCol = 2;
        for (Map.Entry<Integer, List<FeatureFileElement>> entry : buildsResults.entrySet()) {
            List<FeatureFileElement> featureElements = entry.getValue();

            Integer key = entry.getKey();
            List<FeatureFileElement> value = entry.getValue();
            
            Cell cell = row.createCell(startCol);
            cell.setCellValue(key);
                        
            for (FeatureFileElement featureElement : featureElements) {
                Row row1 = sheet.createRow(1);
                for (Scenario scenario : featureElement.getScenarios()) {
                    log.info(scenario.toString());
                }
            }
            
            
            startCol++;            
        }
        
        FileOutputStream fileOut = new FileOutputStream("workbook.xlsx");
                
        wb.write(fileOut);
        fileOut.close();

    }
}
