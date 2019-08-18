package com.hsojo.appupdatetest.task;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.hsojo.appupdatetest.service.GitHubService;
import com.hsojo.appupdatetest.util.DownloadUtil;
import com.hsojo.appupdatetest.util.MiscUtil;
import com.hsojo.appupdatetest.util.VersionUtil;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;

import java.io.File;
import java.io.InputStream;


public class UpdateTask extends AsyncTask<UpdateTask.Asset, Void, Void> {
    private static String TAG = "UpdateTask";
    @SuppressLint("StaticFieldLeak")
    private Context app_context;
    private String dir_cache;
    private String dir_data;
    private String path_data_pack;
    private Callback callback;

    public UpdateTask(Context context, String dir_data, String path_data_pack, Callback callback) {
        this.app_context = context;
        this.dir_cache = context.getCacheDir().getPath();
        this.dir_data = dir_data;
        this.path_data_pack = path_data_pack;
        this.callback = callback;
    }

    private String saveTempFile(InputStream is, String file_name, MiscUtil.ProgressCallback callback) {
        String path = String.format("%s/%s", dir_cache, file_name);
        if (MiscUtil.writeFile(is, path, callback))
            return path;
        else
            return null;
    }

    private boolean installUpdate(String path_zip) {
        try {
            File file_pack = new File(this.path_data_pack);
            if (file_pack.exists())
                new ZipFile(this.path_data_pack).extractAll(dir_data);
            new ZipFile(path_zip).extractAll(dir_data);

            if (file_pack.exists())
                file_pack.delete();
            ZipParameters params = new ZipParameters();
            params.setIncludeRootFolder(false);
            ZipFile pack = new ZipFile(this.path_data_pack);
            pack.addFolder(new File(dir_data), params);

            return true;
        } catch (ZipException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected Void doInBackground(Asset... assets) {
        for (int i = 0; i < assets.length; i++) {
            if (isCancelled())
                break;

            GitHubService.Asset asset = assets[i].asset;
            this.callback.onTaskAssetUpdate(asset);
            this.callback.onTotalProgressUpdate(i, assets.length);

            InputStream is = DownloadUtil.download(asset.browser_download_url);
            if (is != null) {
                String file_path = saveTempFile(is, asset.name, (int current) -> {
                    callback.onSubProgressUpdate(current, asset.size);
                    return !isCancelled();
                });
                if (file_path != null && !isCancelled()) {
                    boolean result = installUpdate(file_path);
                    if (result) {
                        VersionUtil.setVersion(this.app_context, assets[i].version);
                        this.callback.onTotalProgressUpdate(i + 1, assets.length);
                        continue;
                    }
                }
            }

            this.callback.onTotalProgressUpdate(-1, assets.length);
            break;
        }

        return null;
    }

    @Override
    protected void onCancelled() {
        Log.d(TAG, "onCancelled: " + isCancelled());
        super.onCancelled();
    }

    public interface Callback {
        void onTotalProgressUpdate(int current, int max);

        void onSubProgressUpdate(int current, int max);

        void onTaskAssetUpdate(GitHubService.Asset asset);
    }

    public static class Asset {
        GitHubService.Asset asset;
        String version;

        public Asset(GitHubService.Asset asset, String version) {
            this.asset = asset;
            this.version = version;
        }
    }
}
