package utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
class PrintCodeGenerator {

    // 用于存储输入的日期
    private LocalDate inputDate;
    // 用于存储当天的打印编码
    private int dailyPrintCount = 0;

    // 构造函数，接受日期字符串并解析为LocalDate
    public PrintCodeGenerator(String dateString) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        this.inputDate = LocalDate.parse(dateString, formatter);
    }

    public String generatePrintCode() {
        // 递增打印计数
        dailyPrintCount++;

        // 格式化日期和打印编码为所需格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String datePart = inputDate.format(formatter);
        String codePart = String.format("%02d", dailyPrintCount); // 使用%02d来确保总是两位数

        // 组合并返回结果
        return datePart + codePart;
    }

    public static void main(String[] args) {
        int  dailyPrintCount = 10;
          dailyPrintCount++;
        String codePart = String.format("%02d", dailyPrintCount); // 使用%02d来确保总是两位数
        System.out.println(codePart);
    }
}