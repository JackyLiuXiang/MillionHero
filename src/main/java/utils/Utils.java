package utils;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by lingfengsan on 2018/1/12.
 *
 * @author lingfengsan
 */
public class Utils {
    /**
     * ABD_PATH此处应更改为自己的adb目录
     * HERO_PATH更改自己存放图片的地址
     */
    private String adbPath;
    private String imagePath;
    private static final Long MIN_IMAGE_SIZE = 1000L;
    public Utils(){}
    public Utils(String adbPath, String imagePath) {
        this.adbPath = adbPath;
        this.imagePath = imagePath;
    }

    public void openBrowser(String path) {
        try {
            //获取操作系统的名字
            String osName = System.getProperty("os.name", "");

            if (osName.startsWith("Mac OS")) {
                //苹果的打开方式
                Class fileMgr = Class.forName("com.apple.eio.FileManager");
                Method openURL = fileMgr.getDeclaredMethod("openURL", new Class[]{String.class});
                openURL.invoke(null, new Object[]{path});
            } else if (osName.startsWith("Windows")) {
                //windows的打开方式。
                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + path);
            } else if (osName.startsWith("Linux")) {
                //Linux的打开方式
                Desktop desktop = Desktop.getDesktop();
                desktop.browse(URI.create(path));
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    
    /***
     * 对识别后图片文字json数据进行解析
     * @param json
     * @return
     */
    public static String informationParse(String str) {
    	StringBuilder questionStr = new StringBuilder();
        try {
            JSONObject jsonObject = new JSONObject(str);
            
            {
                JSONArray jsonArray = jsonObject.getJSONArray("words_result");
                for (int i = 0; i < jsonArray.length(); i++) {
                	Information person = new Information(str, null);
                    JSONObject wordslist = jsonArray.getJSONObject(i);
                    String words = wordslist.getString("words");
                    questionStr.append(words);
                }
                
//                return questionStr.toString().substring(1,questionStr.indexOf("?"));
                return questionStr.toString();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            System.out.println("JsonParseActivity:json解析出现了问题");
        }

        return null;
    }

    
    /**
     * 获得题目的问题答案信息
     * @param str
     * @return
     */
    public Information getInformation(String str) {
        //先去除空行
        str = str.replaceAll("((\r\n)|\n)[\\s\t ]*(\\1)+", "$1").
                replaceAll("^((\r\n)|\n)", "");
        str=str.replace('.',' ').replace(" ","").replace("？","?");
        //问号统一替换为英文问号防止报错
        str=str.replace("？","?");
        String question = informationParse(str);
        
        String remain = str.substring(str.indexOf("?") + 1);
        String[] ans = remain.trim().split("\n");
        return new Information(question,ans);
    }
    /**
     * 从手机截图发到电脑
     * @return
     */
    public String getImage() {
        //       记录开始时间
        long startTime;
        //       记录结束时间
        long endTime;
        startTime = System.currentTimeMillis();
        
        //获取当前时间作为名字
        Date current = new Date();
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
        String curDate = df.format(current);
        File curPhoto = new File(imagePath, curDate + ".png");
        //截屏存到手机本地
        try {
            while (!curPhoto.exists() || curPhoto.length() < MIN_IMAGE_SIZE) {
                Process process = Runtime.getRuntime().exec(adbPath
                        + " shell /system/bin/screencap -p /sdcard/screenshot.png");
                process.waitFor();
                //将截图放在电脑本地
                process = Runtime.getRuntime().exec(adbPath
                        + " pull /sdcard/screenshot.png " + curPhoto.getAbsolutePath());
                int waitFor = process.waitFor();
            }
            
            endTime = System.currentTimeMillis();
            float excTime = (float) (endTime - startTime);

            System.out.println("执行时间：" + excTime + "ms");
            //返回当前图片名字
            return curPhoto.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.err.println("获取图片失败");
        return null;
    }


    /**
     * 对rank值进行排序
     *
     * @param floats pmi值
     * @return 返回排序的rank
     */
    public static int[] rank(float[] floats) {
        int[] rank = new int[floats.length];
        float[] f = Arrays.copyOf(floats, floats.length);
        Arrays.sort(f);
        for (int i = 0; i < floats.length; i++) {
            for (int j = 0; j < floats.length; j++) {
                if (f[i] == floats[j]) {
                    rank[i] = j;
                }
            }
        }
        return rank;
    }

    public static void main(String[] args) throws IOException {
        String adb = "E:\\DevInstall\\platform-tools\\adb";
        File file = new File("Photo");
        if(!file.exists()) {
        	file.mkdirs();
        }
        String imagePath = "Photo";
        Utils utils = new Utils(adb, imagePath);
//      utils.getImage();
        
        File jsonfile = new File("information_test2.json");
        String content = FileUtils.readFileToString(jsonfile);
    	System.out.println("content:\n"+content);

//    	utils.getInformation(content);
    	
    	utils.informationParse(content);
    }
}
