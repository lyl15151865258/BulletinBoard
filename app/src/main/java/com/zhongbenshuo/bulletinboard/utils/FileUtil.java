package com.zhongbenshuo.bulletinboard.utils;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by LiYuliang on 2017/3/7 0007.
 * 计算文件大小、删除文件的工具类
 */

public class FileUtil {


    // 创建图片类型数组
    private static final String[] IMAGE_TYPES = {"bmp", "wbmp", "jpg", "jpeg", "png", "webp", "tiff", "gif", "pcx", "tga", "exif", "fpx", "svg", "psd", "ico",
            "cdr", "pcd", "dxf", "ufo", "eps", "ai", "raw", "wmf"};
    // 创建文档类型数组
    private static final String[] DOC_TYPES = {"txt", "log", "doc", "docx", "xls", "xlsx", "htm", "html", "jsp", "rtf", "wpd", "pdf", "ppt", "pptx"};
    // 创建视频类型数组
    private static final String[] VIDEO_TYPES = {"mp4", "avi", "mov", "wm", "wmv", "asf", "navi", "3gp", "mkv", "f4v", "rmvb", "webm", "flv", "mpeg", "movie", "mpg", "m4e"};
    // 创建音乐类型数组
    private static final String[] AUDIO_TYPES = {"mp3", "wma", "wav", "mod", "ra", "cd", "md", "asf", "aac", "vqf", "ape", "mid", "ogg", "m4a", "vqf"};
    /**
     * 获取文件大小单位为B的double值
     */
    public static final int SIZETYPE_B = 1;
    /**
     * 获取文件大小单位为KB的double值
     */
    public static final int SIZETYPE_KB = 2;
    /**
     * 获取文件大小单位为MB的double值
     */
    public static final int SIZETYPE_MB = 3;
    /**
     * 获取文件大小单位为GB的double值
     */
    public static final int SIZETYPE_GB = 4;

    /**
     * 获取文件指定文件的指定单位的大小
     *
     * @param filePath 文件路径
     * @param sizeType 获取大小的类型1为B、2为KB、3为MB、4为GB
     * @return double值的大小
     */
    public static double getFileOrFilesSize(String filePath, int sizeType) {
        File file = new File(filePath);
        long blockSize = 0;
        try {
            if (file.isDirectory()) {
                blockSize = getFileSizes(file);
            } else {
                blockSize = getFileSize(file);
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.d("获取文件大小", "获取失败!");
        }
        return formatFileSize(blockSize, sizeType);
    }

    /**
     * 调用此方法自动计算指定文件或指定文件夹的大小
     *
     * @param filePath 文件路径
     * @return 计算好的带B、KB、MB、GB的字符串
     */
    public static String getAutoFileOrFilesSize(String filePath) {
        File file = new File(filePath);
        long blockSize = 0;
        try {
            if (file.isDirectory()) {
                blockSize = getFileSizes(file);
            } else {
                blockSize = getFileSize(file);
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.d("获取文件大小", "获取失败!");
        }
        return formatFileSize(blockSize);
    }

    /**
     * 调用此方法自动计算指定文件或指定文件夹的大小
     *
     * @param file 文件
     * @return 计算好的带B、KB、MB、GB的字符串
     */
    public static String getAutoFileOrFilesSize(File file) {
        long blockSize = 0;
        try {
            if (file.isDirectory()) {
                blockSize = getFileSizes(file);
            } else {
                blockSize = getFileSize(file);
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.d("获取文件大小", "获取失败!");
        }
        return formatFileSize(blockSize);
    }

    /**
     * 获取指定文件大小
     *
     * @param
     * @return
     * @throws Exception
     */
    private static long getFileSize(File file) throws Exception {
        long size = 0;
        if (file.exists()) {
            FileInputStream fis = new FileInputStream(file);
            size = fis.available();
            fis.close();
        } else {
            file.createNewFile();
            LogUtils.d("获取文件大小", "文件不存在!");
        }
        return size;
    }

    /**
     * 获取指定文件夹
     *
     * @param f
     * @return
     * @throws Exception
     */
    private static long getFileSizes(File f) throws Exception {
        long size = 0;
        File flist[] = f.listFiles();
        for (int i = 0; i < flist.length; i++) {
            if (flist[i].isDirectory()) {
                size = size + getFileSizes(flist[i]);
            } else {
                size = size + getFileSize(flist[i]);
            }
        }
        return size;
    }

    /**
     * 转换文件大小
     *
     * @param fileS
     * @return
     */
    public static String formatFileSize2(long fileS) {
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        String wrongSize = "0B";
        if (fileS == 0) {
            return wrongSize;
        }
        if (fileS < 1024) {
            fileSizeString = df.format((double) fileS) + "B";
        } else if (fileS < 1048576) {
            fileSizeString = df.format((double) fileS / 1024) + "KB";
        } else if (fileS < 1073741824) {
            fileSizeString = df.format((double) fileS / 1048576) + "MB";
        } else {
            fileSizeString = df.format((double) fileS / 1073741824) + "GB";
        }
        return fileSizeString;
    }

    /**
     * 格式化文件大小
     *
     * @param size
     * @return
     */
    public static String formatFileSize(long size) {
        if (size <= 0) return "0";
        final String[] units = new String[]{"b", "kb", "M", "G", "T"};
        //计算单位的，原理是利用lg,公式是 lg(1024^n) = nlg(1024)，最后 nlg(1024)/lg(1024) = n。
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        //计算原理是，size/单位值。单位值指的是:比如说b = 1024,KB = 1024^2
        return new DecimalFormat("#,##0.##").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    /**
     * 转换文件大小,指定转换的类型
     *
     * @param fileS
     * @param sizeType
     * @return
     */
    private static double formatFileSize(long fileS, int sizeType) {
        DecimalFormat df = new DecimalFormat("#.00");
        double fileSizeLong = 0;
        switch (sizeType) {
            case SIZETYPE_B:
                fileSizeLong = Double.valueOf(df.format((double) fileS));
                break;
            case SIZETYPE_KB:
                fileSizeLong = Double.valueOf(df.format((double) fileS / 1024));
                break;
            case SIZETYPE_MB:
                fileSizeLong = Double.valueOf(df.format((double) fileS / 1048576));
                break;
            case SIZETYPE_GB:
                fileSizeLong = Double.valueOf(df.format((double) fileS / 1073741824));
                break;
            default:
                break;
        }
        return fileSizeLong;
    }


    // 删除指定文件夹下所有文件
    // param path 文件夹完整绝对路径
    public static boolean delAllFile(String path) {
        boolean flag = false;
        File file = new File(path);
        if (!file.exists() || !file.isDirectory()) {
            return false;
        }
        String[] tempList = file.list();
        File temp;
        for (String string : tempList) {
            if (path.endsWith(File.separator)) {
                temp = new File(path + string);
            } else {
                temp = new File(path + File.separator + string);
            }
            if (temp.isFile()) {
                temp.delete();
            }
            if (temp.isDirectory()) {
                delAllFile(path + "/" + string);// 先删除文件夹里面的文件
//              delFolder(path + "/" + string);// 再删除空文件夹
                flag = true;
            }
        }
        return flag;
    }

    //递归删除文件和文件夹
    public static void deleteFile(File file) {
        if (file.exists()) {
            if (file.isFile()) {
                file.delete();
                return;
            }
            if (file.isDirectory()) {
                File[] childFile = file.listFiles();
                if (childFile == null || childFile.length == 0) {
                    file.delete();
                    return;
                }
                for (File f : childFile) {
                    deleteFile(f);
                }
                file.delete();
            }
        }
    }

    /**
     * 获取单个文件的MD5值！
     *
     * @param file 需要获取MD5的文件
     * @return 该文件的MD5值，如果文件不存在则返回空字符串
     */

    public static String getFileMD5(File file) {
        if (!file.isFile()) {
            return null;
        }
        MessageDigest digest;
        FileInputStream in;
        byte buffer[] = new byte[1024];
        int len;
        try {
            digest = MessageDigest.getInstance("MD5");
            in = new FileInputStream(file);
            while ((len = in.read(buffer, 0, 1024)) != -1) {
                digest.update(buffer, 0, len);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        BigInteger bigInt = new BigInteger(1, digest.digest());
        return bigInt.toString(16).toUpperCase();
    }

    /**
     * 获取文件夹中文件的MD5值
     *
     * @param file
     * @param listChild ;true递归子目录中的文件
     * @return
     */
    public static Map<String, String> getDirMD5(File file, boolean listChild) {
        if (!file.isDirectory()) {
            return null;
        }
        Map<String, String> map = new HashMap<String, String>();
        String md5;
        File files[] = file.listFiles();
        for (int i = 0; i < files.length; i++) {
            File f = files[i];
            if (f.isDirectory() && listChild) {
                map.putAll(getDirMD5(f, listChild));
            } else {
                md5 = getFileMD5(f);
                if (md5 != null) {
                    map.put(f.getPath(), md5);
                }
            }
        }
        return map;
    }

    /**
     * 获取assets文件夹下的json文件并转成字符串
     *
     * @param fileName 文件名
     * @param context  Context
     * @return 字符串json
     */
    public static String getJson(String fileName, Context context) {
        //将json数据变成字符串
        StringBuilder stringBuilder = new StringBuilder();
        try {
            //获取assets资源管理器
            AssetManager assetManager = context.getAssets();
            //通过管理器打开文件并读取
            BufferedReader bf = new BufferedReader(new InputStreamReader(assetManager.open(fileName)));
            String line;
            while ((line = bf.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    /**
     * 复制文件
     *
     * @param source 输入文件
     * @param target 输出文件
     */
    public void copy(File source, File target) throws IOException {
        FileInputStream fileInputStream = null;
        FileOutputStream fileOutputStream = null;
        try {
            fileInputStream = new FileInputStream(source);
            fileOutputStream = new FileOutputStream(target);
            byte[] buffer = new byte[1024];
            while (fileInputStream.read(buffer) > 0) {
                fileOutputStream.write(buffer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
        }
    }

    /**
     *  * 专为Android4.4设计的从Uri获取文件绝对路径，以前的方法已不好使
     *  
     */
    @SuppressLint("NewApi")
    public static String getPath(final Context context, final Uri uri) {
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};
                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    /**
     *  * Get the value of the data column for this Uri. This is useful for
     *  * MediaStore Uris, and other file-based ContentProviders.
     *  *
     *  * @param context    The context.
     *  * @param uri     The Uri to query.
     *  * @param selection  (Optional) Filter used in the query.
     *  * @param selectionArgs (Optional) Selection arguments used in the query.
     *  * @return The value of the _data column, which is typically a file path.
     *  
     */

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     *  * @param uri The Uri to check.
     *  * @return Whether the Uri authority is ExternalStorageProvider.
     *  
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     *  * @param uri The Uri to check.
     *  * @return Whether the Uri authority is DownloadsProvider.
     *  
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     *  * @param uri The Uri to check.
     *  * @return Whether the Uri authority is MediaProvider.
     *  
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }    // 缓存文件头信息-文件头信息

    public static final HashMap<String, String> mFileTypes = new HashMap<>();

    static {
        // images
        mFileTypes.put("FFD8FF", "jpg");
        mFileTypes.put("89504E47", "png");
        mFileTypes.put("47494638", "gif");
        mFileTypes.put("49492A00", "tif");
        mFileTypes.put("424D", "bmp");
        //
        mFileTypes.put("41433130", "dwg"); // CAD
        mFileTypes.put("38425053", "psd");
        mFileTypes.put("7B5C727466", "rtf"); // 日记本
        mFileTypes.put("3C3F786D6C", "xml");
        mFileTypes.put("68746D6C3E", "html");
        mFileTypes.put("44656C69766572792D646174653A", "eml"); // 邮件
        mFileTypes.put("D0CF11E0", "doc");
        mFileTypes.put("D0CF11E0", "xls");//excel2003版本文件
        mFileTypes.put("5374616E64617264204A", "mdb");
        mFileTypes.put("252150532D41646F6265", "ps");
        mFileTypes.put("255044462D312E", "pdf");
        mFileTypes.put("504B0304", "docx");
        mFileTypes.put("504B0304", "xlsx");//excel2007以上版本文件
        mFileTypes.put("52617221", "rar");
        mFileTypes.put("57415645", "wav");
        mFileTypes.put("41564920", "avi");
        mFileTypes.put("2E524D46", "rm");
        mFileTypes.put("000001BA", "mpg");
        mFileTypes.put("000001B3", "mpg");
        mFileTypes.put("6D6F6F76", "mov");
        mFileTypes.put("3026B2758E66CF11", "asf");
        mFileTypes.put("4D546864", "mid");
        mFileTypes.put("1F8B08", "gz");
    }

    /**
     * @param filePath 文件路径
     * @return 文件头信息
     * @author guoxk
     * <p>
     * 方法描述：根据文件路径获取文件头信息
     */
    public static String getFileTypeByHeader(String filePath) {
//		System.out.println(getFileHeader(filePath));
//		System.out.println(mFileTypes.get(getFileHeader(filePath)));
        return mFileTypes.get(getFileHeader(filePath));
    }

    /**
     * @param filePath 文件路径
     * @return 文件头信息
     * @author guoxk
     * <p>
     * 方法描述：根据文件路径获取文件头信息
     */
    public static String getFileHeader(String filePath) {
        FileInputStream is = null;
        String value = null;
        try {
            is = new FileInputStream(filePath);
            byte[] b = new byte[4];
            /*
             * int read() 从此输入流中读取一个数据字节。int read(byte[] b) 从此输入流中将最多 b.length
             * 个字节的数据读入一个 byte 数组中。 int read(byte[] b, int off, int len)
             * 从此输入流中将最多 len 个字节的数据读入一个 byte 数组中。
             */
            is.read(b, 0, b.length);
            value = bytesToHexString(b);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return value;
    }

    /**
     * @param src 要读取文件头信息的文件的byte数组
     * @return 文件头信息
     * @author guoxk
     * <p>
     * 方法描述：将要读取文件头信息的文件的byte数组转换成string类型表示
     */
    private static String bytesToHexString(byte[] src) {
        StringBuilder builder = new StringBuilder();
        if (src == null || src.length <= 0) {
            return null;
        }
        String hv;
        for (int i = 0; i < src.length; i++) {
            // 以十六进制（基数 16）无符号整数形式返回一个整数参数的字符串表示形式，并转换为大写
            hv = Integer.toHexString(src[i] & 0xFF).toUpperCase();
            if (hv.length() < 2) {
                builder.append(0);
            }
            builder.append(hv);
        }
//		System.out.println(builder.toString());
        return builder.toString();
    }

    /**
     * 根据后缀判断文件类型
     *
     * @param fileName 文件名
     * @return 文件类型
     */
    public static String getFileTypeBySuffix(String fileName) {
        if (fileName == null) {
            fileName = "文件名为空";
            return fileName;
        } else {
            // 获取文件后缀名并转化为写，用于后续比较
            String fileType = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();

            for (String s : IMAGE_TYPES) {
                if (s.equals(fileType)) {
                    return "image";
                }
            }

            for (String s : DOC_TYPES) {
                if (s.equals(fileType)) {
                    return "document";
                }
            }

            for (String s : VIDEO_TYPES) {
                if (s.equals(fileType)) {
                    return "video";
                }
            }
            for (String music1 : AUDIO_TYPES) {
                if (music1.equals(fileType)) {
                    return "music";
                }
            }
        }
        return "others";
    }

    /**
     * 判断是否是图片类型
     * Is image
     *
     * @param name
     * @return
     */
    public static boolean isImage(String name) {
        if (TextUtils.isEmpty(name)) {
            return false;
        }
        String fileName = name.toLowerCase();
        for (int i = 0; i < IMAGE_TYPES.length; i++) {
            if (fileName.endsWith(IMAGE_TYPES[i])) {
                return true;
            }
        }
        return false;

    }

    /**
     * 判断是否是视频类型
     * Is video
     *
     * @param name
     * @return
     */
    public static boolean isVideo(String name) {
        if (TextUtils.isEmpty(name)) {
            return false;
        }
        String fileName = name.toLowerCase();
        for (int i = 0; i < VIDEO_TYPES.length; i++) {
            if (fileName.endsWith(VIDEO_TYPES[i])) {
                return true;
            }
        }
        return false;

    }

    /**
     * 判断是否文档类型
     * Is document
     *
     * @param name
     * @return
     */
    public static boolean isDoc(String name) {
        if (TextUtils.isEmpty(name)) {
            return false;
        }
        String fileName = name.toLowerCase();
        for (int i = 0; i < DOC_TYPES.length; i++) {
            if (fileName.endsWith(DOC_TYPES[i])) {
                return false;
            }
        }
        return true;
    }


    /**
     * 判断是否是音频类型
     * Is audio
     *
     * @param name
     * @return
     */
    public static boolean isAudio(String name) {
        if (TextUtils.isEmpty(name)) {
            return false;
        }
        String fileName = name.toLowerCase();
        for (int i = 0; i < AUDIO_TYPES.length; i++) {
            if (fileName.endsWith(AUDIO_TYPES[i])) {
                return true;
            }
        }
        return false;

    }

    /**
     * 获取文件扩展名
     * Get the suffix of the file
     *
     * @param fileName
     * @return
     */
    public static String getSuffix(String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            return "";
        }
        String suffix = "";
        if (!fileName.contains(".")) {
            suffix = "";
        } else {
            suffix = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length()).toLowerCase();
        }
        return suffix;

    }

    /**
     * 根据文件路径获取文件名字（不带后缀）
     *
     * @param pathandname 路径+文件名
     * @return 文件名字
     */
    public static String getFileNameByPath(String pathandname) {
        int start = pathandname.lastIndexOf("/");
        int end = pathandname.lastIndexOf(".");
        if (start != -1 && end != -1) {
            return pathandname.substring(start + 1, end);
        } else {
            return null;
        }
    }

}
