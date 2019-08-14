package com.hsojo.appupdatetest.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MiscUtil {
    public static String pathToFileName(String path) {
        return path.substring(path.lastIndexOf("/") + 1);
    }

    public static String pathToDirName(String path) {
        return path.substring(0, path.lastIndexOf("/"));
    }

    public static boolean createDirectory(String path) {
        File file = new File(path);
        boolean result = false;
        if (!file.exists())
            result = file.mkdirs();
        return result;
    }

    private static void copyStream(InputStream is, OutputStream os, ProgressCallback callback) {
        byte[] buffer = new byte[4096];
        try {
            int len, progress;
            progress = 0;
            while ((len = is.read(buffer)) > -1) {
                os.write(buffer, 0, len);
                progress += len;
                if (callback != null)
                    callback.execute(progress);
            }
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean writeFile(InputStream is, String path, ProgressCallback callback) {
        File file = new File(path);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            copyStream(is, fos, callback);
            fos.close();
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    @FunctionalInterface
    public interface ProgressCallback {
        void execute(int progress);
    }
}
