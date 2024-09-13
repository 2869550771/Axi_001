package utils;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Scanner;

//使用注解的方式生成，优化属性获取
public class NewTableUtils
{
    //该对象用于存放表字段转换成属性的字符串
    static StringBuilder objStr = new StringBuilder();
    private static Connection Conn()
    {
        String url="jdbc:postgresql://192.168.1.228:5432/gt_db_v2.0?userUnicode=true&characterEncoding=utf8";
        String user="postgres";
        String password="gtet-1234";
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

    //首字母大写的方法
    private static String upFirstCode(String str)
    {
        return str.substring(0,1).toUpperCase()
                + str.substring(1);
    }

    //下划线命名转化驼峰命名的方法
    private static String humpString(String str ,Integer type)
    {
        StringBuilder finalStr = new StringBuilder();
        String [] arr = str.split("_");
        if(type==0)
        {
            for (String s : arr)
            {
                String str2 = upFirstCode(s);
                finalStr.append(str2);
            }
        }
        else if (type==1)
        {
            //首字母不大写,先拼接第一个单词（即分割的数组的第0号元素）
            finalStr.append(arr[0]);
            for (int i = 1; i < arr.length; i++)
            {
                String str2 = upFirstCode(arr[i]);
                finalStr.append(str2);
            }
        }
        return finalStr.toString();
    }

    //转换表字段到字符串的方法
    private static void getTableField(Connection conn,String fromName)
    {
        PreparedStatement pst = null;
        try
        {   //先写入导包和注解
            /*objStr.append("import lombok.AllArgsConstructor;\n"+
                    "import lombok.Data;\n"+
                    "import lombok.NoArgsConstructor;\n"+
                    "import lombok.ToString;\n\n"+
                    "@Data\n"+"@AllArgsConstructor\n"+"@NoArgsConstructor\n"+"@ToString\n");*/

            objStr.append("import lombok.Data;\nimport javax.persistence.Table;\nimport java.util.Date;\n@Data\n@Table(name=\"");
            objStr.append(fromName);
            objStr.append("\")\n");
            //添加类名,首字母要大写（无法将非下划线命名的字段或表名设为驼峰，可优化）
            objStr.append("public class ").append(humpString(fromName,0)).append("{");
            String sql = "select * from "+fromName;
            pst = conn.prepareStatement(sql);
            ResultSetMetaData rsMd = pst.executeQuery().getMetaData();
            //遍历字段的数量的次数
            for(int i = 0; i < rsMd.getColumnCount(); i++)
            {
                //拿到数据库对应的java类型
                String className = rsMd.getColumnClassName(i + 1);
                //根据.分割java类型
                String[] javaType = className.split("\\.");
                //拿到分割后的最后的类型，就是字段的数据类型
                String finalJavaType = javaType[javaType.length - 1];
                //字段名称，调用转化驼峰命名的方法,使其不会将属性名的首字母大写
                String fieldName = humpString(rsMd.getColumnName(i + 1),1);
                System.out.print((i+1)+"."+"字段名称是："+fieldName+" ");
                System.out.println("字段类型是："+finalJavaType);
                if (finalJavaType.equals("Timestamp")) {
                    finalJavaType = "Date";
                }
                objStr.append("\n\t\n\tprivate ").append(finalJavaType).append(" ")
                        .append(fieldName).append(";");
                /*
                获取表结构的方法
                "java类型：rsMd.getColumnClassName(i + 1)
                数据库类型:"+rsMd.getColumnTypeName(i + 1)
                字段名称:"+rsMd.getColumnName(i + 1)
                字段长度:"+rsMd.getColumnDisplaySize(i + 1)
                */
            }
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
    private static void StrToObject(String path,String fromName)
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
            fileOutputStream = new FileOutputStream(path+"\\\\"+humpString(fromName,0)+".java");
            //fileOutputStream.write(packName.toString().getBytes());
            fileOutputStream.write("\n".getBytes());
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

    //暴露给外部的调用方法
    public static void fromToObject(String table,String path)
    {
        /*System.out.println("请先导入lombok再使用，若还没有导入，\n再输入连接数据库的名称的时候输入" +
                "\n 0exit0 \n来退出");
        Scanner sc = new Scanner(System.in);
        System.out.println("请要连接的数据库名");
        String tableName = sc.next();
        if("0exit0".equals(tableName))
        {
            System.out.println("退出成功");
            System.exit(0);
        }*/
       /* System.out.println("请输入要转化成实体类的表名");
        String fromName = sc.next();
        System.out.println("请输入实体类要存放的路径(绝对路径)");
        String pathName = sc.next();*/
        //连接数据库获取表字段并转换成字符串
        NewTableUtils.getTableField(NewTableUtils.Conn(),table);
        //使用io流写入文件到指定位置
        NewTableUtils.StrToObject(path,table);
    }
}


