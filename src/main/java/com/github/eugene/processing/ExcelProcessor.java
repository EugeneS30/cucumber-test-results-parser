package com.github.eugene.processing;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;

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

import com.github.eugene.containers.Scenario;
import com.github.eugene.containers.UniqueScenario;

@Slf4j
public class ExcelProcessor {

    static Workbook workbook = new XSSFWorkbook();

    static Font fontArial;
    static Font fontArialBold;
    static Font hlink_font;

    static CellStyle resultCellsStyle;
    static CellStyle topRowStyle;

    static CellStyle fillColorGreen;
    static CellStyle fillColorRed;
    static CellStyle fillColorYellow;
    static CellStyle foreGroundFill;

    static CreationHelper createHelper;

    static CellStyle hlink_style;

    static Row topRow;

    static Sheet resultsSheet;
    static Sheet runStatsSheet;

    static Map<Integer, Integer> buildToColumnMapper = new HashMap<Integer, Integer>();
    static Map<String, Integer> scenarioNameToRowMapper = new HashMap<String, Integer>();

    static String fileName = "results.xlsx";
    static FileOutputStream fileOut;

    static String URL = "http://10.25.67.130:8080/job/Connect%20Automated%20Functional%20Tests%20-%20Firefox%2032/";

    public static void prepareProcessor(List<Integer> allBuildsNumbersList, SortedSet<String> uniqueScenarioNamesSet) {

        preFormat(allBuildsNumbersList);
        populateScenariosNames(uniqueScenarioNamesSet);

    }

    public static void finaliseProcessor() throws IOException {

        postFormat();
        writeToFile();
        finalise();

    }

    private static void createFonts() {
        fontArial = workbook.createFont();
        fontArial.setFontHeightInPoints((short) 10);
        fontArial.setFontName("Arial");
        fontArial.setBoldweight(Font.BOLDWEIGHT_NORMAL);

        fontArialBold = workbook.createFont();
        fontArialBold.setFontHeightInPoints((short) 10);
        fontArialBold.setFontName("Arial");
        fontArialBold.setBoldweight(Font.BOLDWEIGHT_BOLD);

        hlink_font = workbook.createFont();
    }

    private static void createStyles() {
        resultCellsStyle = workbook.createCellStyle();
        topRowStyle = workbook.createCellStyle();

        fillColorGreen = workbook.createCellStyle();
        fillColorRed = workbook.createCellStyle();
        fillColorYellow = workbook.createCellStyle();
        foreGroundFill = workbook.createCellStyle();

        hlink_style = workbook.createCellStyle();

    }

    private static void setDesignSettings() {
        resultCellsStyle.setFont(fontArial);
        topRowStyle.setFont(fontArialBold);

        fillColorGreen.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        fillColorGreen.setFillPattern(PatternFormatting.SOLID_FOREGROUND);
        fillColorGreen.setFont(fontArial);

        fillColorRed.setFillForegroundColor(IndexedColors.ROSE.getIndex());
        fillColorRed.setFillPattern(CellStyle.SOLID_FOREGROUND);
        fillColorRed.setFont(fontArial);

        fillColorYellow.setFillForegroundColor(IndexedColors.LEMON_CHIFFON.getIndex());
        fillColorYellow.setFillPattern(CellStyle.SOLID_FOREGROUND);
        fillColorGreen.setFont(fontArial);

        createHelper = workbook.getCreationHelper();

        hlink_font.setUnderline(Font.U_SINGLE);
        hlink_font.setColor(IndexedColors.BLUE.getIndex());
        hlink_style.setFont(hlink_font);

    }

    private static void preFormat(List<Integer> allBuildsNumbersList) {

        resultsSheet = workbook.createSheet("Test Results");
        runStatsSheet = workbook.createSheet("Run Statistics");

        resultsSheet.createFreezePane(0, 1);

        topRow = resultsSheet.createRow(0);

        Cell cellFilename = topRow.createCell(0);
        Cell cellScenario = topRow.createCell(1);

        // Fonts are set into a style so create a new one to use.
        // CellStyle style = workbook.createCellStyle();

        cellFilename.setCellValue("Filename");
        cellScenario.setCellValue("Scenario");

        int startCol = 2;

        // Set all the column names as build numbers
        for (Integer buildNum : allBuildsNumbersList) {

            buildToColumnMapper.put(buildNum, startCol);

            Cell cell = topRow.createCell(startCol);
            cell.setCellValue(buildNum);
            startCol++ ;
        }

        createFonts();
        createStyles();
        setDesignSettings();
    }

    private static void populateScenariosNames(SortedSet<String> uniqueScenarioNamesSet) {

        int currentRowNumber = 1;
        for (String uniqueScenario : uniqueScenarioNamesSet) {

            Row currentRow = resultsSheet.createRow(currentRowNumber);
            Cell currentFilenameCell = currentRow.createCell(0);
            Cell currentScenarioCell = currentRow.createCell(1);

            scenarioNameToRowMapper.put(uniqueScenario, currentRowNumber);

            currentFilenameCell.setCellValue(uniqueScenario.split(",")[0]); // set feature file path
            currentFilenameCell.setCellStyle(resultCellsStyle);
            currentScenarioCell.setCellValue(uniqueScenario.split(",")[1]); // set the scenario name
            currentScenarioCell.setCellStyle(resultCellsStyle);
            currentRowNumber++ ;
        }

    }

    public static void processData(List<UniqueScenario> uniqueScenariosList) throws IOException {

        for (UniqueScenario entry : uniqueScenariosList) {

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

            // results formatting
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
                    log.warn("Couldn't get screenshotpath for scenario: \"" + entry.getScenarioName() + "\" in build "
                            + entry.getBuildNum());
                }

                String URLString = URL + buildNum + "/Functional_Test_Report/screenshots/" + screenShotName + ".png";

                currentCell.setCellStyle(fillColorRed); // Set style

                // Check if known issue
                if (tags.contains("KnownIssue")) {
                    Comment comment = drawing.createCellComment(anchor);
                    comment.setString(str);
                    currentCell.setCellComment(comment); // Assign the comment to the cell
                }

                Hyperlink link = createHelper.createHyperlink(Hyperlink.LINK_FILE);
                link.setAddress(URLString);

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

    }

    private static void postFormat() {

        int lastRowNum = resultsSheet.getLastRowNum();
        Row passStatsRow = resultsSheet.createRow(lastRowNum + 2);
        Row failStatsRow = resultsSheet.createRow(lastRowNum + 3);

        Cell passCountCell = passStatsRow.createCell(1);
        Cell failCountCell = failStatsRow.createCell(1);

        passCountCell.setCellValue("Passed:");
        failCountCell.setCellValue("Failed:");

        passCountCell.setCellStyle(topRowStyle);
        failCountCell.setCellStyle(topRowStyle);

        // Count and print the number of passed scenarios
        for (int i = 2; i < topRow.getLastCellNum(); i++ ) { // For each cell in the row
            Cell cell = passStatsRow.createCell(i);
            String colName = getExcelColumnName(i + 1); // "Translate" the column number to column
                                                        // name. Example: C = 2
            String formula = "COUNTIF(" + colName + "2:" + colName + (lastRowNum + 1) + ", \"Pass\")";
            cell.setCellFormula(formula);
        }

        // Count and print the number of failed scenarios
        for (int i = 2; i < topRow.getLastCellNum(); i++ ) { // For each cell in the row
            Cell cell = failStatsRow.createCell(i);
            String colName = getExcelColumnName(i + 1); // "Translate" the column number to column
                                                        // name. Example: C = 2
            String formula = "COUNTIF(" + colName + "2:" + colName + (lastRowNum + 1) + ", \"Fail\")";
            cell.setCellFormula(formula);
        }
        
//        for (int i = 2; i < topRow.getLastCellNum(); i++ ) { // For each cell in the row
//            Cell cell = failStatsRow.createCell(i);
//            String colName = getExcelColumnName(i + 1); // "Translate" the column number to column
//                                                        // name. Example: C = 2
//            String formula = "COUNTIF(" + colName + "2:" + colName + (lastRowNum + 1) + ", \"Fail\")";
//            cell.setCellFormula(formula);
//        }        


        for (int i = 0; i < topRow.getLastCellNum(); i++ ) {// For each cell in the row
            resultsSheet.autoSizeColumn(i);
            topRow.getCell(i).setCellStyle(topRowStyle);// Set the style
        }

        resultsSheet.setColumnWidth(1, 20000);

    }

    private static void writeToFile() {
        try {
            fileOut = new FileOutputStream(fileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Write save and close the workbook to file
     * 
     * @throws IOException
     */
    private static void finalise() throws IOException {

        log.info("=-=-=-=-=-=-=-=");
        log.info("Writing the results into excel file...");
        log.info("=-=-=-=-=-=-=-=");
        workbook.write(fileOut);
        fileOut.close();
        workbook.close();
        log.info("File saved!");

    }

    /**
     * 
     * Helper functions
     * 
     */

    // Column name to integer converter
    private static int getExcelColumnNumber(String column) {
        int result = 0;
        for (int i = 0; i < column.length(); i++ ) {
            result *= 26;
            result += column.charAt(i) - 'A' + 1;
        }
        return result;
    }

    // Column number to name converter
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

}
