package com.github.eugene.parse;

import java.io.FileOutputStream;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.*;
import org.apache.poi.ss.usermodel.charts.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelWriter {
	Workbook wb = new XSSFWorkbook();
	Sheet sheet = wb.createSheet("Sheet 1");
	

}
