package Controller;


import com.github.tsohr.JSONArray;
import com.github.tsohr.JSONObject;
import utils.Base64Util;
import utils.ExcelUtil;
import utils.FileUtil;
import utils.HttpUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Thread.sleep;


public class OCRController {

    //百度API_key 和 Secret_key
    private static final String OCR_API_Key  = "xxxxxxxxxxxxxxxxxxxxxxxx";
    private static final String OCR_Secret_Key = "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx";

    //不同识别精度url
    private static final String other_Host = "https://aip.baidubce.com/rest/2.0/ocr/v1/general";
    private static final String basic_Host = "https://aip.baidubce.com/rest/2.0/ocr/v1/general_basic";
    private static final String accurate_Host = "https://aip.baidubce.com/rest/2.0/ocr/v1/accurate";
    private static final String accurate_basic_Host = "https://aip.baidubce.com/rest/2.0/ocr/v1/accurate_basic";

    public static void main(String args[]) {

        for (int i = 0; i < args.length; i++) {
            System.out.println("参数" + (i + 1) + "的值为：" + args[i]);
        }
        // 本地图片路径
//        String path = args[0];
        String path = "E:\\杂项\\image_test";
        List<File> file = new ArrayList();
        getFiles(file, path);

        try {
            /**
             * 线上环境access_token有过期时间， 客户端可自行缓存，过期后重新获取。
             */
            String accessToken = getAuth(OCR_API_Key, OCR_Secret_Key);

            int size = file.size();
            String [][] newfile = new String[size][5];

            for(int i = 0 ; i < size ; i++) {
                //获取图片数据
                byte[] imgData = FileUtil.readFileByBytes(file.get(i).toString());
                //base64转码
                String imgStr = Base64Util.encode(imgData);
                String params = URLEncoder.encode("image", "UTF-8") + "=" + URLEncoder.encode(imgStr, "UTF-8");

                //高精度基础版 QPS 限制为2，每次等待0.35s
                if(2 <= size){
                    sleep(350);
                }

                //获取识别内容
                String total_list = HttpUtil.post(accurate_basic_Host, accessToken, params);

                //将识别内容由json格式转换成list格式
                JSONObject jsonObject = new JSONObject(total_list);
                JSONArray result = jsonObject.getJSONArray("words_result");
                List list = result.toList();

                //图片中存在换行，将所有数据拼接成一条字符串
                String list_data = ConvertArraryToString(list);

                           //分割字符，提取金额 商品 姓名 电话等信息
            List list_data1 = getKeyWords(list_data, i+1);
            System.out.println("list_data1-------------"+list_data1);
            for(int j = 0 ; j < list_data1.size(); j++) {
                newfile[i][j] = list_data1.get(j).toString();
            }
        }

            //导出到excel
            ExcelUtil.Excel(newfile);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String ConvertArraryToString(List list){
        String temp = "" ;

        for(int i = 0; i < list.size(); i++){
            String str1 = list.get(i).toString().split("\\=")[1];
            String str2 = str1.substring(0, str1.length()-1);

            Pattern r = Pattern.compile("[`<]");
            Matcher m = r.matcher(str2);
            if(m.find()){
                temp = temp + str2 + ",";
            }else{
                temp = temp  + str2;
            }

        }
        System.out.println(temp);
        return temp;
    }

    /*
    * 函数 getKeyWords()
    * 一、获取固定格式的数据内容，如￥100，（商品，姓名，电话，地址）
    * 二、通过正则匹配所需内容（待实现）
    * */
    public static List getKeyWords(String list_data, int i){
        //匹配数据正则表达式
//        String priace_symbol = "\\￥";
//        String address = "地[\\u4e00-\\u9fa5]址[\\u4e00-\\u9fa5]";
//        String phone_number = "0?(13|14|15|18)[0-9]{9}";

        String datatemp = "";
        String pricetemp = "";

        int priceStartIndex = list_data.indexOf("￥");
        int priceEndIndex = list_data.indexOf(".");
        try{
            pricetemp = list_data.substring(priceStartIndex, priceEndIndex).substring(("￥").length());
        }catch (Exception e){
            pricetemp = "第"+ i + "张图片获取价格失败";
        }

        int strStartIndex = list_data.indexOf("(");
        int strEndIndex = list_data.indexOf("。");

        if (strStartIndex < 0) {
            strStartIndex = list_data.indexOf("（");

            if (strEndIndex < 0) {
                strEndIndex = list_data.indexOf(".");
            }

            try {
                datatemp = list_data.substring(strStartIndex, strEndIndex).substring(("（").length());
            }catch (Exception e){
                datatemp = "第"+ i + "张图片格式错误获取商品失败," +
                            "第" + i + "张图片格式错误获取姓名失败," +
                            "第" + i + "张图片格式错误获取电话失败," +
                            "第" + i + "张图片格式错误获取地址失败" ;
            }

        } else {
            try {
                datatemp = list_data.substring(strStartIndex, strEndIndex).substring(("(").length());
            }catch (Exception e){
                datatemp = "第"+ i + "张图片格式错误获取商品失败," +
                        "第" + i + "张图片格式错误获取姓名失败," +
                        "第" + i + "张图片格式错误获取电话失败," +
                        "第" + i + "张图片格式错误获取地址失败" ;
            }
        }

        datatemp = pricetemp + "," + datatemp;
        List list_temp = Arrays.asList(datatemp.split(","));
        List arrList_temp = new ArrayList(list_temp);

        String str = list_temp.get(1).toString().split("）")[0];
        if (str.length() == list_temp.get(1).toString().length()) {
            String str1 = list_temp.get(1).toString().split("\\)")[0];
            arrList_temp.remove(1);
            arrList_temp.add(1, str1);
        } else {
            arrList_temp.remove(1);
            arrList_temp.add(1, str);
        }

//        for(int i = 0; i < list_temp.size(); i++){
//            String str = list_temp.get(i).toString();
//            Pattern r1 = Pattern.compile(iterms);
//            Matcher m1 = r1.matcher(str);
//            if(m1.find()){
//                String str1 = "111 " + str;
//                list_temp.add(0, str1);
//            }else{
//                Pattern r2 = Pattern.compile(phone_number);
//                Matcher m2 = r2.matcher(str);
//                if(m2.find()){
//                    String str2 = "222 " + m2.group();
//                    list_temp.add(1, str2);
//                }else{
//                    Pattern r3 = Pattern.compile(phone_number);
//                    Matcher m3 = r3.matcher(str);
//                    if(m3.find()) {
//                        String str3 = "333 " + m3.group();
//                        list_temp.add(2, str3);
//                    }
//                }
//            }
//        }
        return arrList_temp;
    }

    public static List<File> getFiles(List<File>fileList, String path) {
        try {
            int i = 1;
            File file = new File(path);
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                for (File fileIndex : files) {
                    //如果这个文件是目录，则进行递归搜索
                    if (fileIndex.isDirectory()) {
                        getFiles(fileList, fileIndex.getPath());
                    } else {
                        //如果文件是普通文件，则将文件句柄放入集合中
                        //根据顺序重命名图片，方便查找对比
                        String filePath = fileIndex.getAbsolutePath();
                        String fileName = filePath.substring(0, filePath.lastIndexOf("\\")) + "\\" + i +
                                            filePath.substring(filePath.lastIndexOf("."));
                        File orFile = new File(filePath);
                        orFile.renameTo( new File(fileName));
                        //添加图片路径到list
                        fileList.add(new File(fileName));
                    }
                    i++;
                }
            }
        } catch (Exception e) {

        }
        return fileList;
    }

    public static String getAuth(String API_Key, String Secret_Key) {
        // 获取token地址
        String authHost = "https://aip.baidubce.com/oauth/2.0/token?";
        String getAccessTokenUrl = authHost
                // 1. grant_type为固定参数
                + "grant_type=client_credentials"
                // 2. 官网获取的 API Key
                + "&client_id=" + API_Key
                // 3. 官网获取的 Secret Key
                + "&client_secret=" + Secret_Key;
        try {
            URL realUrl = new URL(getAccessTokenUrl);
            // 打开和URL之间的连接
            HttpURLConnection connection = (HttpURLConnection) realUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            // 获取所有响应头字段
            Map<String, List<String>> map = connection.getHeaderFields();
            // 遍历所有的响应头字段
            for (String key : map.keySet()) {
                System.err.println(key + "--->" + map.get(key));
            }
            // 定义 BufferedReader输入流来读取URL的响应
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String result = "";
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
            /**
             * 返回结果示例
             */
            System.err.println("result:" + result);
            JSONObject jsonObject = new JSONObject(result);
            String access_token = jsonObject.getString("access_token");
            return access_token;
        } catch (Exception e) {
            System.err.printf("获取token失败！");
            e.printStackTrace(System.err);
        }
        return null;
    }
}
