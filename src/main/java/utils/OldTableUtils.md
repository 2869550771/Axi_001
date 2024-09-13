~~~java
package utils;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Scanner;

public class TableUtils
{
//该对象用于存放表字段转换成属性的字符串
static StringBuilder objStr = new StringBuilder();
//该对象用于存放对应的getter、setter字符串
static StringBuilder gSMethod = new StringBuilder();
//该对象用于存放toString字符串
static StringBuilder tSMethod = new StringBuilder("\n\t@Override\n\tpublic String toString() {\n\t\treturn ");
//连接数据库的方法
public static Connection Conn(String tableName)
{
String url="jdbc:mysql://localhost:3306/"+tableName+"?userUnicode=true&characterEncoding=utf8";
String user="root";
String password="123456";
Connection connection = null;
try
{
connection =  DriverManager.getConnection(url,user,password);
System.out.println("\n==============数据库连接成功==============");
return connection;
}
catch (Exception e)
{
e.printStackTrace();
System.out.println("\n============连接失败,请查看错误信息============");
return connection;
}
}

    //转换表字段到字符串的方法
    public static void getTableField(Connection conn,String fromName)
    {
        PreparedStatement pst = null;
        try
        {
            objStr.append("public class ").append(fromName).append("{");
            tSMethod.append("\"").append(fromName).append("{\"").append("+\n\t\t");
            String sql = "select * from "+fromName;
            pst = conn.prepareStatement(sql);
            ResultSetMetaData rsMd = pst.executeQuery().getMetaData();

            for(int i = 0; i < rsMd.getColumnCount(); i++)
            {
                String fieldType = rsMd.getColumnTypeName(i + 1);
                String fieldName = rsMd.getColumnName(i + 1);
                System.out.print((i+1)+"."+"字段名称是："+fieldName+" ");
                //首字母大写
                String upF = fieldName.substring(0,1).toUpperCase()
                        + fieldName.substring(1);
                /*根据字段的数据类型，设置属性的数据类型（不知能否直接获取java类型，感觉可优化）
                 、getter、setter方法和toString方法
                 */
                switch (fieldType)
                {
                    case "INT":
                        System.out.println("字段类型是："+"int");
                        objStr.append("\n\tprivate ").append("int ").append(fieldName).append(";");
                        //get
                        gSMethod.append("\n\tpublic int get").append(upF).append("() {\n\t\treturn ")
                                .append(fieldName).append(";\n\t}");
                        //set
                        gSMethod.append("\n\tpublic void set").append(upF).append("(").append("int ")
                                .append(fieldName).append(") {\n\t\tthis.").append(fieldName)
                                .append(" = ").append(fieldName).append(";\n\t}");
                        break;

                    case "VARCHAR":
                        System.out.println("字段类型是："+"String");
                        objStr.append("\n\tprivate ").append("String ").append(fieldName).append(";");
                        //get
                        gSMethod.append("\n\tpublic String get").append(upF).append("() {\n\t\treturn ")
                                .append(fieldName).append(";\n\t}");
                        //set
                        gSMethod.append("\n\tpublic void set").append(upF).append("(").append("String ")
                                .append(fieldName).append(") {\n\t\tthis.").append(fieldName)
                                .append(" = ").append(fieldName).append(";\n\t}");
                        break;
                }
                /*
                获取表结构的方法
                "java类型：rsMd.getColumnClassName(i + 1)
                数据库类型:"+rsMd.getColumnTypeName(i + 1)
                字段名称:"+rsMd.getColumnName(i + 1)
                字段长度:"+rsMd.getColumnDisplaySize(i + 1)
                */

                //toString
                if(i!=0)
                {
                    tSMethod.append("\", ").append(fieldName).append("=\"").append(" + ")
                            .append(fieldName).append(" + \n\t\t");
                }
                else
                {
                    tSMethod.append("\"").append(fieldName).append("=\"").append(" + ")
                            .append(fieldName).append(" + \n\t\t");
                }
            }
            //使toString字符串结束
            tSMethod.append("'").append("}").append("'").append(";").append("\n\t}");
            //在属性后，需要添加getter、setter、toString
            objStr.append("\n\n//getter setter方法").append(gSMethod)
                    .append("\n\n//toString方法").append(tSMethod);
            //最后添加大括号，使实体类字符串结束
            objStr.append("\n}");
            System.out.println("===========================================");
            System.out.println("该表生成的实体类字符串如下:\n"+objStr);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            System.out.println("\n===============您输入的表名可能不存在===============");
        }
        //最后需要释放资源
        finally
        {
            try
            {
                if (conn!=null)
                {
                    conn.close();
                }
                if (pst!=null)
                {
                    pst.close();
                }
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
    }

    //使用io流，将字符串写入文件输出到磁盘上的方法
    public static void StrToObject(String path,String fromName)
    {
        FileOutputStream fileOutputStream = null;
        try
        {   //该变量用于记录java文件夹的位置,以便获取实体类对应的软件包位置
            int packNum = 0;
            StringBuilder packName = new StringBuilder("package ");
            String [] arr = path.split("\\\\");
            for (int i = 0; i < arr.length; i++)
            {
                if(arr[i].equals("java"))
                {
                    packNum = i+1;
                    break;
                }
            }
            for (int i = packNum; i < arr.length; i++)
            {
                packName.append(arr[i]);
                if(i==arr.length-1)
                {
                    break;
                }
                packName.append(".");
            }

            System.out.println("\n\n该实体类应处于的软件包位置是："+packName);
            fileOutputStream = new FileOutputStream(path+"\\\\"+fromName+".java");
            fileOutputStream.write(packName.toString().getBytes());
            fileOutputStream.write(";\n".getBytes());
            fileOutputStream.write(objStr.toString().getBytes());

            System.out.println("实体类生成完成，请至对应的输出路径下查看");
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
            System.out.println("文件路径指定异常");
        }
        catch (IOException e)
        {
            e.printStackTrace();
            System.out.println("写入异常");
        }
        finally
        {
            try
            {
                if(fileOutputStream!=null)
                {
                    fileOutputStream.close();
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    public static void fromToObject()
    {
        Scanner sc = new Scanner(System.in);
        System.out.println("请要连接的数据库名");
        String tableName = sc.next();
        System.out.println("请输入要转化成实体类的表名");
        String fromName = sc.next();
        System.out.println("请输入实体类要存放的路径(绝对路径)");
        String pathName = sc.next();
        //连接数据库获取表字段并转换成字符串
        TableUtils.getTableField(TableUtils.Conn(tableName),fromName);
        //使用io流写入文件到指定位置
        TableUtils.StrToObject(pathName,fromName);
    }
}

~~~