package utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.poi.xssf.usermodel.*;
import javax.swing.filechooser.FileSystemView;

public class ExcelUtil {

     public static void Excel(String [][] newfile) throws IOException {
         //创建Excel对象
         XSSFWorkbook workbook = new XSSFWorkbook();

         //创建工作表单
         XSSFSheet sheet = workbook.createSheet("姓名+货+价格");
//         XSSFSheet sheet1 = workbook.createSheet("姓名+电话+地址");

         //设置sheet第一行表头
         XSSFRow first_row = sheet.createRow(0);
//         XSSFRow first_row1 = sheet1.createRow(0);
         for(int n = 0 ; n < 3 ; n++) {
             XSSFCell cell = first_row.createCell(n);
//             XSSFCell cell1 = first_row1.createCell(n);

             if(n == 0) {
                 cell.setCellValue("姓名");
//                 cell1.setCellValue("姓名");
             }else if(n == 1) {
                 cell.setCellValue("商品");
//                 cell1.setCellValue("电话");
             }else if(n == 2){
                 cell.setCellValue("价格");
//                 cell1.setCellValue("地址");
             }
         }

         //设置数据
         for (int i = 0 ; i < newfile.length ; i++) {
                 //创建HSSFRow对象（行）
             XSSFRow row = sheet.createRow(i+1);
//             XSSFRow row1 = sheet1.createRow(i+1);

             for (int j = 0 ; j < newfile[i].length; j++) {
                 //创建HSSFCell对象（单元格）
                 XSSFCell cell = row.createCell(j);
//                 XSSFCell cell1 = row1.createCell(j);

                 if (j == 0) {
                     //设置sheet的姓名
                     cell.setCellValue(newfile[i][2]);
                     //设置sheet1的姓名
//                     cell1.setCellValue(newfile[i][2]);
                 }else if( j == 1){
                     //设置sheet的商品
                     cell.setCellValue(newfile[i][1]);
                     sheet.setColumnWidth(1, newfile[i][1].getBytes().length*256);
                 }else if( j == 2){
                     //设置sheet的价格
                     cell.setCellValue(newfile[i][0]);
                 }else if( j == 3){
                     //设置sheet1的电话
//                     XSSFCell cell3 = row1.createCell(j-2);
//                     cell3.setCellValue(newfile[i][3]);
                 }else if( j == 4){
                     //设置sheet1的地址
//                     XSSFCell cell4 = row1.createCell(j-2);
//                     cell4.setCellValue(newfile[i][4]);
//                     sheet1.setColumnWidth(2, newfile[i][4].getBytes().length*256);
                 }
             }
         }

         sheet.setColumnWidth(2, 4608);
//         sheet1.setColumnWidth(1, 4608);
         sheet.setColumnWidth(0, 3328);
//         sheet1.setColumnWidth(0, 3328);

         try {
              //获取当前日期作为表格名称
              SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
              Date date = new Date(System.currentTimeMillis());

              String File_Path = "\\" + formatter.format(date).split(" ")[0] + "_"
                                      + formatter.format(date).split(" ")[2].split(":")[0] + "_"
                                      + formatter.format(date).split(" ")[2].split(":")[1] + "_"
                                      + formatter.format(date).split(" ")[2].split(":")[2] + ".xlsx";

              //输出Excel文件到桌面
              File desktopDir = FileSystemView.getFileSystemView().getHomeDirectory();
              String desktopPath = desktopDir.getAbsolutePath();
              File file = new File(desktopPath + File_Path);

              FileOutputStream output = new FileOutputStream(file);
              workbook.write(output);
              output.flush();
         }catch (Exception e){
              System.err.println(e.getMessage());
         }
     }
}
