package com.yelj.mediademon.utils;

import android.os.Environment;
import android.text.TextUtils;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileUtils {

    public static final String SNK_PATH = getSDPath() + "MediaDemon/";
    public static final String VEDIO_PATH_SNK = SNK_PATH + "video/";
    public final static String IMG_PATH_PICTURE = SNK_PATH + "picture/";
    public static final String IMG_PATH_SNK_VEDIO_COVER = SNK_PATH + "video_cover/";

    public static final String VEDIO_FORMAT_MP4 = ".mp4";
    public static final String IMG_FORMAT_JPG = ".jpg";

    /**
     * 创建新文件和读取文件
     * @return 创建的文件
     */
    public static File createNewFile(String path) {
        if (TextUtils.isEmpty(path)) {
            return null;
        }
        String dirPath = path.substring(0, path.lastIndexOf("/") + 1);
        File dirFile = new File(getReadSDPath(dirPath));
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }
        File file = new File(getReadSDPath(path));
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                file = new File(path);
                if (!file.exists()) {
                    try {
                        file.createNewFile();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }
        return file;
    }

    /**
     * 因getWriteSDPath()方法获取到的SD卡路径只能写文件
     * 所以读取本地文件时需要将storage/sdcard0替换成storage/emulated/0
     */
    public static String getReadSDPath(String path) {
        if (TextUtils.isEmpty(path)) {
            return "";
        }
        if (path.startsWith(getWriteSDPath())) {
            return path.replace(getWriteSDPath(), Environment.getExternalStorageDirectory().getPath() + "/");
        } else {
            return path;
        }
    }

    /**
     * 储存文件到本地时获取SD卡根目录(部分手机加了emulated/限制，需要更替为sdcard)
     */
    public static String getWriteSDPath() {
        String sdPath = getSDPath();
        Pattern p = Pattern.compile("/?storage/emulated/\\d{1,2}");
        Matcher m = p.matcher(sdPath);
        if (m.find()) {
            sdPath = sdPath.replace("storage/emulated/", "storage/sdcard");
        }
        return sdPath;
    }

    public static String getSDPath() {
        String sdPath = null;
        boolean sdCardExist = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
        if (sdCardExist) {
            sdPath = Environment.getExternalStorageDirectory().getPath();
        }
        return sdPath + "/";
    }
}
