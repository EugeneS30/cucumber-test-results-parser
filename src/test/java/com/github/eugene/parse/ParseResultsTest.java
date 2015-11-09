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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
// import org.apache.poi.common.usermodel.Hyperlink;
import org.apache.poi.hssf.record.cf.PatternFormatting;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Test;

import com.github.eugene.config.ConfigurationClass;
import com.github.eugene.containers.FeatureFileElement;
import com.github.eugene.containers.Scenario;
import com.github.eugene.containers.UniqueScenario;

public class ParseResultsTest {

    final static Logger log = Logger.getLogger(ParseResultsTest.class);

    private final String buildsDirPath = ConfigurationClass.buildsDirPath;
    private final String jsonRelativePath = ConfigurationClass.jsonRelativePath;

    private static int getExcelColumnNumber(String column) {
        int result = 0;
        for (int i = 0; i < column.length(); i++ ) {
            result *= 26;
            result += column.charAt(i) - 'A' + 1;
        }
        return result;
    }

    public static String getExcelColumnName(int number) {
        final StringBuilder sb = new StringBuilder();

        int num = number - 1;
        while (num >= 0) {
            int numChar = (num % 26) + 65;
            sb.append((char) numChar);
            num = (num / 26) - 1;
        }
        return sb.reverse().toString();
    }

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
                log.warn("Not a valid folder name. Skipping...: " + buildPath.getName().toString());
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
                    allBuildResults.add(new UniqueScenario(build.getKey(), scenario, scenario.getRunResult(), scenario.getTags()));
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
        // fontArial.setBoldweight(Font.BOLDWEIGHT_NORMAL);

        Font fontArialBold = workbook.createFont();
        fontArialBold.setFontHeightInPoints((short) 10);
        fontArialBold.setFontName("Arial");
        fontArialBold.setBoldweight(Font.BOLDWEIGHT_BOLD);

        // Styles
        CellStyle resultCellsStyle = workbook.createCellStyle();
        resultCellsStyle.setFont(fontArial);

        CellStyle topRowStyle = workbook.createCellStyle();
        topRowStyle.setFont(fontArialBold);

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
            currentFilenameCell.setCellValue(uniqueScenario.split(",")[0]); // set feature file path
            currentFilenameCell.setCellStyle(resultCellsStyle);
            currentScenarioCell.setCellValue(uniqueScenario.split(",")[1]); // set the scenario name
            currentScenarioCell.setCellStyle(resultCellsStyle);
            currentRowNumber++ ;
        }

        CellStyle fillColorGreen = workbook.createCellStyle();
        CellStyle fillColorRed = workbook.createCellStyle();
        CellStyle fillColorYellow = workbook.createCellStyle();
        CellStyle foreGroundFill = workbook.createCellStyle();

        fillColorGreen.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        fillColorGreen.setFillPattern(PatternFormatting.SOLID_FOREGROUND);
        fillColorGreen.setFont(fontArial);

        fillColorRed.setFillForegroundColor(IndexedColors.ROSE.getIndex());
        fillColorRed.setFillPattern(CellStyle.SOLID_FOREGROUND);
        fillColorRed.setFont(fontArial);

        fillColorYellow.setFillForegroundColor(IndexedColors.LEMON_CHIFFON.getIndex());
        fillColorYellow.setFillPattern(CellStyle.SOLID_FOREGROUND);
        fillColorGreen.setFont(fontArial);

        CellStyle hlink_style = workbook.createCellStyle();
        CreationHelper createHelper = workbook.getCreationHelper();

        Font hlink_font = workbook.createFont();
        hlink_font.setUnderline(Font.U_SINGLE);
        hlink_font.setColor(IndexedColors.BLUE.getIndex());
        hlink_style.setFont(hlink_font);

        for (UniqueScenario entry : allBuildResults) {

            int buildNum = entry.getBuildNum();
            Scenario scenario = entry.getScenario();
            String scenarioRunResult = entry.getRunResult();
            String scenarioName = scenario.getScenarioName();
            List<String> tags = entry.getTags();

            log.debug("=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");
            log.debug("Processing UniqueScenario: #" + scenarioName + "#");

            int currentColNum = buildToColumnMapper.get(buildNum);
            int currentRowNum = scenarioNameToRowMapper.get(scenario.generateUriScenarioPair());

            Row currentRow = resultsSheet.getRow(currentRowNum);

            Cell currentCell = currentRow.createCell(currentColNum);
            currentCell.setCellValue(scenarioRunResult);

            // TAGS
            CreationHelper factory = workbook.getCreationHelper();
            Drawing drawing = resultsSheet.createDrawingPatriarch();
            ClientAnchor anchor = factory.createClientAnchor();
            RichTextString str = factory.createRichTextString(tags.toString());

            if ("pass".equalsIgnoreCase(scenarioRunResult)) {
                currentCell.setCellStyle(fillColorGreen);
            }

            else if ("fail".equalsIgnoreCase(scenarioRunResult)) {
                String screenShotName = "";

                Pattern pattern = Pattern.compile("screenshot-\\d*");
                try {
                    Matcher matcher = pattern.matcher(scenario.getScreenShotPath());

                    if (matcher.find()) {
                        screenShotName = matcher.group(0);
                    }
                } catch (NullPointerException e) {
                    log.error("Couldn't get screenshotpath for scenario: \"" + entry.getScenarioName() + "\" in build "
                            + entry.getBuildNum());
                }

                String URL = "http://10.25.67.130:8080/job/Connect%20Automated%20Functional%20Tests%20-%20Firefox%2032/" + buildNum
                        + "/Functional_Test_Report/screenshots/" + screenShotName + ".png";

                currentCell.setCellStyle(fillColorRed); // Set style

                // Check if known issue
                if (tags.contains("KnownIssue")) {
                    Comment comment = drawing.createCellComment(anchor);
                    comment.setString(str);
                    currentCell.setCellComment(comment); // Assign the comment to the cell
                }

                Hyperlink link = createHelper.createHyperlink(Hyperlink.LINK_FILE);
                link.setAddress(URL);

                currentCell.setHyperlink(link);
                // currentCell.setCellStyle(hlink_style);
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
         * Post processing
         */

        int lastRowNum = resultsSheet.getLastRowNum();
        Row passStatsRow = resultsSheet.createRow(lastRowNum);
        Row failStatsRow = resultsSheet.createRow(lastRowNum + 1);

        // Count and print the number of passed scenarios
        for (int i = 2; i < topRow.getLastCellNum(); i++ ) { // For each cell in the row
            Cell cell = passStatsRow.createCell(i);
            String colName = getExcelColumnName(i + 1); // "Translate" the column number to column
                                                        // name. Example: C = 2
            String formula = "COUNTIF(" + colName + "2:" + colName + "341, \"Pass\")";
            cell.setCellFormula(formula);
        }

        // Count and print the number of failed scenarios
        for (int i = 2; i < topRow.getLastCellNum(); i++ ) { // For each cell in the row
            Cell cell = failStatsRow.createCell(i);
            String colName = getExcelColumnName(i + 1); // "Translate" the column number to column
                                                        // name. Example: C = 2
            String formula = "COUNTIF(" + colName + "2:" + colName + "341, \"Fail\")";
            cell.setCellFormula(formula);
        }

        // Cell statsCell = statsRow.createCell(1);

        for (int i = 0; i < topRow.getLastCellNum(); i++ ) {// For each cell in the row
            resultsSheet.autoSizeColumn(i);
            topRow.getCell(i).setCellStyle(topRowStyle);// Set the style
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
