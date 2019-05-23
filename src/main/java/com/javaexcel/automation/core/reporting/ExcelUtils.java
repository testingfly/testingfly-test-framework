package com.javaexcel.automation.core.reporting;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.poi.hssf.util.Region;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


public class ExcelUtils {
	
	public void cloneExcel(XSSFWorkbook templateWB, XSSFWorkbook newWorkBook, String newFileName){
		
		//Workbook templateWB = new XSSFWorkbook(new FileInputStream("C:\\input.xlsx"));
		//Workbook newWorkBook = new XSSFWorkbook();
		CellStyle newStyle = newWorkBook.createCellStyle(); // Need this to copy over styles from old sheet to new sheet. Next step will be processed below
		Row row;
		Cell cell;
		for (int i = 0; i < templateWB.getNumberOfSheets(); i++) {
		    XSSFSheet sheetFromOldWB = (XSSFSheet) templateWB.getSheetAt(i);
		    XSSFSheet sheetForNewWB = (XSSFSheet) newWorkBook.createSheet(sheetFromOldWB.getSheetName());
		    for (int rowIndex = 0; rowIndex < /*sheetFromOldWB.getPhysicalNumberOfRows()*/11; rowIndex++) {
		        row = sheetForNewWB.createRow(rowIndex); //create row in this new sheet
		        for (int colIndex = 0; colIndex < sheetFromOldWB.getRow(rowIndex).getPhysicalNumberOfCells(); colIndex++) {
		            cell = row.createCell(colIndex); //create cell in this row of this new sheet
		            Cell c = sheetFromOldWB.getRow(rowIndex).getCell(colIndex, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK ); //get cell from old/original WB's sheet and when cell is null, return it as blank cells. And Blank cell will be returned as Blank cells. That will not change.
		                if (c.getCellType() == Cell.CELL_TYPE_BLANK){
		                    System.out.println("This is BLANK " +  ((XSSFCell) c).getReference());
		                }
		                else {  //Below is where all the copying is happening. First It copies the styles of each cell and then it copies the content.              
		                CellStyle origStyle = c.getCellStyle();
		                newStyle.cloneStyleFrom(origStyle);
		                cell.setCellStyle(newStyle);            

		                 switch (c.getCellType()) {
		                    case Cell.CELL_TYPE_STRING:                            
		                        cell.setCellValue(c.getRichStringCellValue().getString());
		                        break;
		                    case Cell.CELL_TYPE_NUMERIC:
		                        if (DateUtil.isCellDateFormatted(cell)) {                             
		                            cell.setCellValue(c.getDateCellValue());
		                        } else {                              
		                            cell.setCellValue(c.getNumericCellValue());
		                        }
		                        break;
		                    case Cell.CELL_TYPE_BOOLEAN:

		                        cell.setCellValue(c.getBooleanCellValue());
		                        break;
		                    case Cell.CELL_TYPE_FORMULA:

		                        cell.setCellValue(c.getCellFormula());
		                        break;
		                    case Cell.CELL_TYPE_BLANK:
		                        cell.setCellValue("");
		                        break;
		                    default:
		                        System.out.println();
		                    }
		                }
		            }
		        }

		    }
		    //Write over to the new file
		    FileOutputStream fileOut;
			try {
				fileOut = new FileOutputStream(newFileName);
				 newWorkBook.write(fileOut);
				 templateWB.close();
				    newWorkBook.close();
				    fileOut.close();
			} catch ( IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		   
		
	}

	
	
	
	public static void copySheets(XSSFSheet newSheet, XSSFSheet sheet){
        copySheets(newSheet, sheet, true);
    }
    public static void copySheets(XSSFSheet newSheet, XSSFSheet sheet, boolean copyStyle){
        int maxColumnNum = 0;
        Map<Integer, XSSFCellStyle> styleMap = (copyStyle)
                ? new HashMap<Integer, XSSFCellStyle>() : null;
 
        for (int i = sheet.getFirstRowNum(); i <= sheet.getLastRowNum(); i++) {
            XSSFRow srcRow = sheet.getRow(i);
            XSSFRow destRow = newSheet.createRow(i);
            if (srcRow != null) {
            	ExcelUtils.copyRow(sheet, newSheet, srcRow, destRow, styleMap);
                if (srcRow.getLastCellNum() > maxColumnNum) {
                    maxColumnNum = srcRow.getLastCellNum();
                }
            }
        }
        for (int i = 0; i <= maxColumnNum; i++) {
            newSheet.setColumnWidth(i, sheet.getColumnWidth(i));
        }
    }
 
    public static void copyRow(XSSFSheet srcSheet, XSSFSheet destSheet, XSSFRow srcRow, XSSFRow destRow, Map<Integer, XSSFCellStyle> styleMap) {
        Set mergedRegions = new TreeSet();
        destRow.setHeight(srcRow.getHeight());
        for (int j = srcRow.getFirstCellNum(); j <= srcRow.getLastCellNum(); j++) {
            XSSFCell oldCell = srcRow.getCell(j);
            XSSFCell newCell = destRow.getCell(j);
            if (oldCell != null) {
                if (newCell == null) {
                    newCell = destRow.createCell(j);
                }
                copyCell(oldCell, newCell, styleMap);
               /* Region mergedRegion = getMergedRegion(srcSheet, srcRow.getRowNum(), oldCell.getCellNum());
                if (mergedRegion != null) {
//                    Region newMergedRegion = new Region( destRow.getRowNum(), mergedRegion.getColumnFrom(),
//                            destRow.getRowNum() + mergedRegion.getRowTo() - mergedRegion.getRowFrom(), mergedRegion.getColumnTo() );
                    Region newMergedRegion = new Region(mergedRegion.getRowFrom(), mergedRegion.getColumnFrom(),
                            mergedRegion.getRowTo(), mergedRegion.getColumnTo());
                    if (isNewMergedRegion(newMergedRegion, mergedRegions)) {
                        mergedRegions.add(newMergedRegion);
                        destSheet.addMergedRegion(newMergedRegion);
                    }
                }*/
            }
        }
         
    }
    public static void copyCell(XSSFCell oldCell, XSSFCell newCell, Map<Integer, XSSFCellStyle> styleMap) {
        if(styleMap != null) {
            if(oldCell.getSheet().getWorkbook() == newCell.getSheet().getWorkbook()){
                newCell.setCellStyle(oldCell.getCellStyle());
            } else{
                int stHashCode = oldCell.getCellStyle().hashCode();
                XSSFCellStyle newCellStyle = styleMap.get(stHashCode);
                if(newCellStyle == null){
                    newCellStyle = newCell.getSheet().getWorkbook().createCellStyle();
                    newCellStyle.cloneStyleFrom(oldCell.getCellStyle());
                    styleMap.put(stHashCode, newCellStyle);
                }
                newCell.setCellStyle(newCellStyle);
            }
        }
        switch(oldCell.getCellType()) {
            case XSSFCell.CELL_TYPE_STRING:
                newCell.setCellValue(oldCell.getStringCellValue());
                break;
            case XSSFCell.CELL_TYPE_NUMERIC:
                newCell.setCellValue(oldCell.getNumericCellValue());
                break;
            case XSSFCell.CELL_TYPE_BLANK:
                newCell.setCellType(XSSFCell.CELL_TYPE_BLANK);
                break;
            case XSSFCell.CELL_TYPE_BOOLEAN:
                newCell.setCellValue(oldCell.getBooleanCellValue());
                break;
            case XSSFCell.CELL_TYPE_ERROR:
                newCell.setCellErrorValue(oldCell.getErrorCellValue());
                break;
            case XSSFCell.CELL_TYPE_FORMULA:
                newCell.setCellFormula(oldCell.getCellFormula());
                break;
            default:
                break;
        }
         
    }
    public static Region getMergedRegion(XSSFSheet sheet, int rowNum, short cellNum) {
        for (int i = 0; i < sheet.getNumMergedRegions(); i++) {/*
            Region merged = sheet.getMergedRegionAt(i);
            if (merged.contains(rowNum, cellNum)) {
                return merged;
            }
        */}
        return null;
    }
 
    private static boolean isNewMergedRegion(Region region, Collection mergedRegions) {
        return !mergedRegions.contains(region);
    }
}
