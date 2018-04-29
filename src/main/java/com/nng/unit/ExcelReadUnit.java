package com.nng.unit;

import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class ExcelReadUnit implements Util{
    private DataFormatter dataFormatter=new DataFormatter();//用于格式化单元格数据

    /*
     * POI解析含有合并单元格的Excel文件内容
     */
    //获取单元格的值(若是合并的单元格，也返回它的值，方便以后数据存入数据库的处理)
    public String getCellValue(Sheet sheet, int row, int column) {
        Row tempRow = null;
        Cell tempCell = null;
        //获取合并格个数
        int sheetMergeCount = sheet.getNumMergedRegions();
        for (int i = 0; i < sheetMergeCount; i++) {
            //获取合并的区域
            CellRangeAddress range = sheet.getMergedRegion(i);
            int firstColumn = range.getFirstColumn();
            int lastColumn = range.getLastColumn();
            int firstRow = range.getFirstRow();
            int lastRow = range.getLastRow();
            if (row >= firstRow && row <= lastRow && row > 0) {

                if (column >= firstColumn && column <= lastColumn) {
                    tempRow = sheet.getRow(firstRow);
                    tempCell = tempRow.getCell(firstColumn);
                    return dataFormatter.formatCellValue(tempCell);
                }
            }
        }
        tempRow = sheet.getRow(row);
        tempCell = tempRow.getCell(column);
        return dataFormatter.formatCellValue(tempCell);
    }

    //读取学生数据
    public List<Map> readStudents() {
        //存储所有学生信息的列表，单个学生信息用Map存储
        List<Map> list=new ArrayList<Map>();
        //需要解析的Excel文件
        File file = new File("/Users/chenzhaohe/File/Students.xlsx");
        try {
            //创建Excel，读取文件内容
            XSSFWorkbook workbook = new XSSFWorkbook(FileUtils.openInputStream(file));

            //循环遍历所有工作表的内容
            int sheetIndex = 0;

            //需要读取的列的索引
            Map<String, Integer> colindex = new HashMap<String, Integer>();

            while (sheetIndex < workbook.getNumberOfSheets()) {
                Sheet sheet = workbook.getSheetAt(sheetIndex);
                int firstRowNum = 0;
                //获取sheet中最后一行的行号
                int lastRowNum = sheet.getLastRowNum();
                //循环行，要小于等于，不然会丢失最后一行的数据
                for (int i = firstRowNum; i <= lastRowNum; i++) {
                    //用于存储单个学生数据
                    Map<String,String> stu=new HashMap<String, String>();

                    //获取当前行
                    Row row = sheet.getRow(i);

                    //判断本行是否为空
                    if (dataFormatter.formatCellValue(row.getCell(0)).equals(""))
                    {
                        continue;
                    }

                    //获取当前行最后一个单元格列号
                    int lastCellNum = row.getLastCellNum();
                    //循环列
                    for (int j = 0; j < lastCellNum; j++) {
                        String value = getCellValue(sheet, i, j);

                        //获取指定的列的值，通过判断第一行的列名
                        if (i == firstRowNum) {
                            switch (value) {
                                case "学号":
                                    colindex.put("学号", j);
                                    break;
                                case "姓名":
                                    colindex.put("姓名", j);
                                    break;
                                case "专业":
                                    colindex.put("专业", j);
                                    break;
                            }
                            continue;
                        } else {
                            //判断是否是指定的列，如果是则输出内容
                            for (int k = 0; k < colindex.size(); k++) {
                                if (j == colindex.get("学号")) {
                                    //存储学生数据
                                    stu.put("学号",value);
                                }
                                if (j == colindex.get("姓名")) {
                                    stu.put("姓名",value);
                                }
                                if (j == colindex.get("专业")) {
                                    stu.put("专业",value);
                                }
                            }
                        }
                    }
                    //将本行学生数据加入列表,排除第一列
                    if (i!=firstRowNum){
                        list.add(stu);
                    }
                }
                //下一张工作表
                sheetIndex++;
            }
            //关闭工作表
            workbook.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return list;
    }

    //读取教师数据
    public List<Map> readTeachers() {
        //存储所有教师信息的列表，单个教师信息用Map存储
        List<Map> list=new ArrayList<Map>();
        //需要解析的Excel文件
        File file = new File("/Users/chenzhaohe/File/Teachers.xlsx");
        try {
            //创建Excel，读取文件内容
            XSSFWorkbook workbook = new XSSFWorkbook(FileUtils.openInputStream(file));

            //循环遍历所有工作表的内容
            int sheetIndex = 0;

            //需要读取的列的索引
            Map<String, Integer> colindex = new HashMap<String, Integer>();

            while (sheetIndex < workbook.getNumberOfSheets()) {
                Sheet sheet = workbook.getSheetAt(sheetIndex);
                int firstRowNum = 0;
                //获取sheet中最后一行的行号
                int lastRowNum = sheet.getLastRowNum();
                //循环行，要小于等于，不然会丢失最后一行的数据
                for (int i = firstRowNum; i <= lastRowNum; i++) {
                    //用于存储单个教师数据
                    Map<String,String> stu=new HashMap<String, String>();

                    //获取当前行
                    Row row = sheet.getRow(i);

                    //判断本行是否为空
                    if (dataFormatter.formatCellValue(row.getCell(0)).equals(""))
                    {
                        continue;
                    }

                    //获取当前行最后一个单元格列号
                    int lastCellNum = row.getLastCellNum();
                    //循环列
                    for (int j = 0; j < lastCellNum; j++) {
                        String value = getCellValue(sheet, i, j);

                        //获取指定的列的值，通过判断第一行的列名
                        if (i == firstRowNum) {
                            switch (value) {
                                case "教师姓名":
                                    colindex.put("教师姓名", j);
                                    break;
                                case "联系电话":
                                    colindex.put("联系电话", j);
                                    break;
                                case "Email/QQ":
                                    colindex.put("Email/QQ", j);
                                    break;
                            }
                            continue;
                        } else {
                            //判断是否是指定的列，如果是则输出内容
                            for (int k = 0; k < colindex.size(); k++) {
                                if (j == colindex.get("教师姓名")) {
                                    //存储学生数据
                                    stu.put("教师姓名",value);
                                }
                                if (j == colindex.get("联系电话")) {
                                    stu.put("联系电话",value);
                                }
                                if (j == colindex.get("Email/QQ")) {
                                    stu.put("Email/QQ",value);
                                }
                            }
                        }
                    }
                    //将本行教师数据加入列表,排除第一列
                    if (i!=firstRowNum){
                        list.add(stu);
                    }
                }
                //下一张工作表
                sheetIndex++;
            }
            //关闭工作表
            workbook.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return list;
    }

    //读取题目数据
    public List<Map> readSubjects() {
        //存储所有题目信息的列表，单个题目信息用Map存储
        List<Map> list=new ArrayList<Map>();
        //需要解析的Excel文件
        File file = new File("/Users/chenzhaohe/File/Subjects.xlsx");
        try {
            //创建Excel，读取文件内容
            XSSFWorkbook workbook = new XSSFWorkbook(FileUtils.openInputStream(file));

            //循环遍历所有工作表的内容
            int sheetIndex = 0;

            //需要读取的列的索引
            Map<String, Integer> colindex = new HashMap<String, Integer>();

            while (sheetIndex < workbook.getNumberOfSheets()) {
                Sheet sheet = workbook.getSheetAt(sheetIndex);
                int firstRowNum = 0;
                //获取sheet中最后一行的行号
                int lastRowNum = sheet.getLastRowNum();
                //循环行，要小于等于，不然会丢失最后一行的数据
                for (int i = firstRowNum; i <= lastRowNum; i++) {
                    //用于存储单个教师数据
                    Map<String,String> stu=new HashMap<String, String>();

                    //获取当前行
                    Row row = sheet.getRow(i);

                    //判断本行是否为空
                    if (dataFormatter.formatCellValue(row.getCell(0)).equals(""))
                    {
                        continue;
                    }

                    //获取当前行最后一个单元格列号
                    int lastCellNum = row.getLastCellNum();
                    //循环列
                    for (int j = 0; j < lastCellNum; j++) {
                        String value = getCellValue(sheet, i, j);

                        //获取指定的列的值，通过判断第一行的列名
                        if (i == firstRowNum) {
                            switch (value) {
                                case "题目":
                                    colindex.put("题目", j);
                                    break;
                                case "所需人数":
                                    colindex.put("所需人数", j);
                                    break;
                                case "备注":
                                    colindex.put("备注", j);
                                    break;
                                case "可选专业":
                                    colindex.put("可选专业", j);
                                    break;
                            }
                            continue;
                        } else {
                            //判断是否是指定的列，如果是则输出内容
                            for (int k = 0; k < colindex.size(); k++) {
                                if (j == colindex.get("题目")) {
                                    //存储学生数据
                                    stu.put("题目",value);
                                }
                                if (j == colindex.get("所需人数")) {
                                    stu.put("所需人数",value);
                                }
                                if (j == colindex.get("备注")) {
                                    stu.put("备注",value);
                                }
                                if (j == colindex.get("可选专业")) {
                                    stu.put("可选专业",value);
                                }

                            }
                        }
                    }
                    //将本行题目数据加入列表,排除第一列
                    if (i!=firstRowNum){
                        list.add(stu);
                    }
                }
                //下一张工作表
                sheetIndex++;
            }
            //关闭工作表
            workbook.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return list;
    }
}
