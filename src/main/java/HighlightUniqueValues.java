import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class HighlightUniqueValues {

	byte[] excelData;
	String sheetName = "Sheet1";
	int[] columnIndex;     //Ex: {2, 6, 8, 13}; Column index (0-based) to select and highlight unique values

	public byte[] highlightUniqueValues(byte[] excelData, int[] columnIndex) {
		this.excelData = excelData;
		this.sheetName = "Sheet1";
		this.columnIndex = columnIndex;

		try (InputStream inputStream = new ByteArrayInputStream(excelData);
				Workbook workbook = WorkbookFactory.create(inputStream)) {
			Sheet sheet = workbook.getSheet(sheetName);
			if (sheet != null) {
				XSSFCellStyle redCellStyle = (XSSFCellStyle) workbook.createCellStyle();
				redCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

				byte[] rgb = new byte[]{(byte) 255, (byte) 204, (byte) 204};
				XSSFColor lightRedColor = new XSSFColor(rgb, null);
				redCellStyle.setFillForegroundColor(lightRedColor);

				Font font = workbook.createFont();
				font.setColor(IndexedColors.DARK_RED.getIndex());
				redCellStyle.setFont(font);

				// Iterate over each row and get the cell value of the specified column
				for (Row row : sheet) {
					for (int column : columnIndex) {
						Cell cell = row.getCell(column);
						if (cell != null) {
							String cellValue = cell.getStringCellValue();
							if (this.isUniqueValue(sheet, column, cellValue)) {
								cell.setCellStyle(redCellStyle);
							}
						}
					}
				}
				
				//Enable Filters
				this.enableFilters(sheet);
				
				//Highlight Headers
				this.highlightHeaders(sheet, workbook);

				// Save the modified spreadsheet to a byte array
				ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
				workbook.write(byteArrayOutputStream);
				byteArrayOutputStream.flush();
				byte[] modifiedExcelData = byteArrayOutputStream.toByteArray();
				byteArrayOutputStream.close();
				System.out.println("JAVA :: Unique values highlighted successfully!");
				return modifiedExcelData;

			} else {
				System.out.println("Sheet '" + sheetName + "' not found!");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;	
	}

	public boolean isUniqueValue(Sheet sheet, int columnIndex, String cellValue) {
		// Check if the specified cell value is unique in the column
		int count = 0;
		for (Row row : sheet) {
			Cell cell = row.getCell(columnIndex);
			if (cell != null && cell.getStringCellValue().equals(cellValue)) {
				count++;
				if (count > 1) {
					return false;
				}
			}
		}
		return true;
	}

	public void highlightHeaders(Sheet sheet, Workbook workbook) {

		CellStyle headerCellStyle = workbook.createCellStyle();
		headerCellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
		headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

		Font font = workbook.createFont();
		font.setColor(IndexedColors.BLACK.getIndex());
		font.setBold(true);
		headerCellStyle.setFont(font);

		Row headerRow = sheet.getRow(0); 

		// Iterate over each cell in the header row and apply the style

		for (Cell headerCell : headerRow) {
			headerCell.setCellStyle(headerCellStyle);
		}
	}
	
	public void enableFilters(Sheet sheet) {  
		// Enable filters for the header row      
		sheet.setAutoFilter(CellRangeAddress.valueOf(sheet.getSheetName() + "!A1:" + getLastColumnReference(sheet) + "1")); 
	}    

	public String getLastColumnReference(Sheet sheet) {     
		Row headerRow = sheet.getRow(0); // Assuming the header row is the first row   
		int lastCellNum = headerRow.getLastCellNum();    
		return CellReference.convertNumToColString(lastCellNum - 1);   
	}

	public static void main(String[] args) {

	}
}