package com.ming.androidhotfixunitytest;

import android.content.Context;
import android.util.Log;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import io.github.noodle1983.Boostrap;

import static com.unity3d.splash.services.core.properties.ClientProperties.getApplication;

/*--------------------------------------------------------------------------------------------------------------------------*/
class VersionData//版本数据
{
    public String message;
    public AppVersion data;
    public int code;
}
class AppVersion//app版本
{
    public int versionCode;
    public String version;
}
/*--------------------------------------------------------------------------------------------------------------------------*/
public class HotFixManager{
    private static HotFixManager hotFixManager;
    public static HotFixManager Instance() {
        if (null == hotFixManager) {
            try {
                // 模拟在创建对象之前做一些准备工作
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            hotFixManager = new HotFixManager();
        }
        return hotFixManager;
    }
    /*--------------------------------------------------------------------------------------------------------------------------*/
    private static final String VersionUrl = "http://192.168.2.20:8888/unity/Common/version.json";
    private static final String ResUrl = "http://192.168.2.20:8888/unity/Common/AllAndroidPatchFiles_Version";
    /*--------------------------------------------------------------------------------------------------------------------------*/
    private Context mainContext;
    /**
     * 初始化热更
     * */
    public void InitHotFix(Context context){
        mainContext=context;
        String initPath=context.getApplicationContext().getFilesDir().getPath();
        Boostrap.InitNativeLibBeforeUnityPlay(initPath);
        MainActivity.AddLog("Boostrap_InitPath: " + initPath);
    }
    /*--------------------------------------------------------------------------------------------------------------------------*/
    /**
    *  检测更新
    * */
    public void CheckHotFix(Context context){
        MainActivity.AddLog("开始检测更新");
        String basePath = context.getApplicationContext().getExternalFilesDir(null).getPath();
        Log.d("UpdateTest", "basePath: " + basePath);
        MainActivity.AddLog("basePath: " + basePath);
        String versionPath = basePath + "/common/version.json";
        MainActivity.AddLog("versionPath: " + versionPath);
        String resPath = basePath + "/common/AllAndroidPatchFiles_Version";
        MainActivity.AddLog("resPath: " + resPath);
        Gson gson = new Gson();
        File file = new File(versionPath);
        if (file.exists()) {
            try {
                MainActivity.AddLog("非首次下载安装,获取最新版本信息json");
                VersionData serverVerData = gson.fromJson(loadJson(VersionUrl), VersionData.class);
                int serverVerCode = serverVerData.data.versionCode;
                int localVerCode = gson.fromJson(ReadFile(versionPath), VersionData.class).data.versionCode;
                Log.d("UpdateTest", "serverVerCode: " + serverVerCode + ",localVerCode:" + localVerCode);
                MainActivity.AddLog("VerCode: " + "serverVerCode: " + serverVerCode + ",localVerCode:" + localVerCode);
                String unZipPath = resPath + serverVerData.data.version;
                //todo 服务器版本大于本地版本，更新
                if (serverVerCode > localVerCode) {
                    MainActivity.AddLog("服务器存在最新版本,开始更新版本");
                    String versionStr = serverVerData.data.version + ".zip";
                    resPath = resPath + versionStr;
                    downloadFile(ResUrl + versionStr, resPath);
                    //todo 解压
                    MainActivity.AddLog("资源更新完成，正在解压");
                    unZipFiles(resPath, unZipPath);
                    String il2cppPath = unZipPath + "/lib_" + Boostrap.getarchabi() + "_libil2cpp.so.zip";
                    unZipFiles(il2cppPath, unZipPath);
                    downloadFile(VersionUrl, versionPath);//替换本地版本信息
                }
                //todo hook
                String error=Boostrap.usedatadir(unZipPath,mainContext.getPackageName());
                if(error!=null){
                    Log.d("UpdateTest", "UseDataDirError: " +error);
                    MainActivity.AddLog("UseDataDirError: " +error);
                }
                //todo 删除il2cpp缓存
                String cacheDir=basePath+ "/il2cpp";
                File cacheDirFile =new File(cacheDir);
                if(cacheDirFile.exists()){
                    deleteDir(cacheDirFile);
                }
                MainActivity.AddLog("删除il2cpp缓存,更新完成");
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                MainActivity.AddLog("首次下载安装,更新版本信息json");
                downloadFile(VersionUrl, versionPath);//首个版本
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        MainActivity.OnEnterUnityEvent();
    }

    /**
     * 递归删除目录下的所有文件及子目录下所有文件
     * @param dir 将要删除的文件目录
     * @return boolean Returns "true" if all deletions were successful.
     *                 If a deletion fails, the method stops attempting to
     *                 delete and returns "false".
     */
    private static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();//递归删除目录中的子目录下
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        // 目录此时为空，可以删除
        return dir.delete();
    }
    /**
     * TODO 下载文件到本地
     *
     * @param fileUrl   远程地址
     * @param fileLocal 本地路径
     * @throws Exception
     * @author nadim
     * @date Sep 11, 2015 11:45:31 AM
     */
    public void downloadFile(String fileUrl, String fileLocal) throws Exception {
        Runnable runnable = new Runnable() {
            public void run() {
                try {
                    URL url = new URL(fileUrl);
                    HttpURLConnection urlCon = (HttpURLConnection) url.openConnection();
                    urlCon.setConnectTimeout(6000);
                    urlCon.setReadTimeout(6000);
                    urlCon.setRequestProperty("Accept-Encoding", "identity");
                    int code = urlCon.getResponseCode();
                    if (code != HttpURLConnection.HTTP_OK) {
                        throw new Exception("文件读取失败");
                    }
                    //读文件流
                    urlCon.getContentLength();
                    DataInputStream in = new DataInputStream(urlCon.getInputStream());
                    File file = new File(fileLocal.substring(0, fileLocal.lastIndexOf('/')));
                    if (!file.exists()) {
                        file.mkdirs();
                    }
                    DataOutputStream out = new DataOutputStream(new FileOutputStream(fileLocal));
                    byte[] buffer = new byte[2048];
                    int count = 0;
                    long curProcess=0;
                    long totalSize=urlCon.getContentLength();
                    while ((count = in.read(buffer)) > 0) {
                        out.write(buffer, 0, count);
                        curProcess+=count;
                        double process=(float) curProcess/(float) totalSize;
                        MainActivity.AddLog("当前下载进度："+process+","+curProcess+"/"+totalSize);
                    }
                    out.close();
                    in.close();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
        thread.join();
    }

    /**
     * 通过网络访问json并读取文件
     *
     * @param url:http://192.168.2.20:8888/Common/version.json
     * @return:json文件的内容
     */
    public static String loadJson(String url) throws InterruptedException {
        StringBuilder json = new StringBuilder();
        Runnable runnable = new Runnable() {
            public void run() {
                try {
                    URL urlObject = new URL(url);
                    URLConnection uc = urlObject.openConnection();
                    BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream(), "UTF-8"));
                    String inputLine = null;
                    while ((inputLine = in.readLine()) != null) {
                        json.append(inputLine);
                    }
                    in.close();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
        thread.join();
        return json.toString();
    }

    /**
     * 通过本地文件访问json并读取
     *
     * @param path：E:/svn/05.Hospital/templatedept_uuid.json
     * @return：json文件的内容
     */
    public static String ReadFile(String path) {
        String laststr = "";
        File file = new File(path);// 打开文件
        BufferedReader reader = null;
        try {
            FileInputStream in = new FileInputStream(file);
            reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));// 读取文件
            String tempString = null;
            while ((tempString = reader.readLine()) != null) {
                laststr = laststr + tempString;
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException el) {
                }
            }
        }
        return laststr;
    }

    /**
     * 解压到指定目录
     */
    public static void unZipFiles(String zipPath, String descDir) throws IOException {
        unZipFiles(new File(zipPath), descDir);
    }

    /**
     * 解压文件到指定目录
     */
    public static void unZipFiles(File zipFile, String descDir) throws IOException {
        File pathFile = new File(descDir);
        if (!pathFile.exists()) {
            pathFile.mkdirs();
        }
        //解决zip文件中有中文目录或者中文文件
        ZipFile zip =new ZipFile(zipFile);
        for (Enumeration entries = zip.entries(); entries.hasMoreElements(); ) {
            ZipEntry entry = (ZipEntry) entries.nextElement();
            String zipEntryName = entry.getName();
            InputStream in = zip.getInputStream(entry);
            String outPath = (descDir + "/" + zipEntryName).replaceAll("\\*", "/");
            ;
            //判断路径是否存在,不存在则创建文件路径
            File file = new File(outPath.substring(0, outPath.lastIndexOf('/')));
            if (!file.exists()) {
                file.mkdirs();
            }
            //判断文件全路径是否为文件夹,如果是上面已经上传,不需要解压
            if (new File(outPath).isDirectory()) {
                continue;
            }
            //输出文件路径信息
            System.out.println(outPath);
            OutputStream out = new FileOutputStream(outPath);
            byte[] buf1 = new byte[1024];
            int len;
            while ((len = in.read(buf1)) > 0) {
                out.write(buf1, 0, len);
            }
            in.close();
            out.close();
        }
        System.out.println("******************解压完毕********************");
    }
}
