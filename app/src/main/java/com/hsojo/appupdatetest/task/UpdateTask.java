package com.hsojo.appupdatetest.task;

import android.annotation.SuppressLint;
import android.os.AsyncTask;

import com.hsojo.appupdatetest.service.GitHubService;
import com.hsojo.appupdatetest.util.DownloadUtil;
import com.hsojo.appupdatetest.util.MiscUtil;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import java.io.InputStream;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class UpdateTask extends AsyncTask<GitHubService.Asset, Void, String> {
    private String dir_cache;
    private String dir_app_data;
    private Queue<UpdateTaskCore> tasks;
    private Callback callback;
    private int current_max;
    private int current_value;

    public UpdateTask(String dir_cache, String dir_app_data, Callback callback) {
        this.dir_cache = dir_cache;
        this.dir_app_data = dir_app_data;
        this.tasks = new ConcurrentLinkedQueue<>();
        this.callback = callback;
        this.resetCurrent();
    }

    private void resetCurrent() {
        this.current_max = 0;
        this.current_value = 0;
        this.tasks.clear();
    }

    private String saveTempFile(InputStream is, String file_name, MiscUtil.ProgressCallback callback) {
        String path = String.format("%s/%s", dir_cache, file_name);
        if (MiscUtil.writeFile(is, path, callback))
            return path;
        else
            return null;
    }

    private boolean installUpdate(String zip_path) {
        try {
            new ZipFile(zip_path).extractAll(dir_app_data);
            return true;
        } catch (ZipException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void executeNextTask() {
        if (tasks.size() > 0) {
            UpdateTaskCore task = Objects.requireNonNull(tasks.poll());
            callback.onTaskAssetUpdate(task.asset);
            task.execute();
        }
    }

    @SuppressLint("WrongThread")
    @Override
    protected String doInBackground(GitHubService.Asset... assets) {
        current_max += assets.length;
        for (GitHubService.Asset asset : assets) {
            System.out.println(String.format("Add %s", asset.name));
            tasks.add(new UpdateTaskCore(asset, new CoreCallback() {
                @Override
                public void onSuccess() {
                    executeNextTask();
                    current_value++;
                    callback.onTotalProgressUpdate(current_value, current_max);
                }

                @Override
                public void onError() {
                    callback.onTotalProgressUpdate(-1, current_max);
                    resetCurrent();
                }

                @SuppressLint("DefaultLocale")
                @Override
                public void onProgressUpdate(int current, int max) {
                    callback.onSubProgressUpdate(current, max);
                }
            }));
        }

        callback.onTotalProgressUpdate(current_value, current_max);
        this.executeNextTask();
        return null;
    }

    public interface Callback {
        void onTotalProgressUpdate(int current, int max);

        void onSubProgressUpdate(int current, int max);

        void onTaskAssetUpdate(GitHubService.Asset asset);
    }


    public interface CoreCallback {
        void onSuccess();

        void onError();

        void onProgressUpdate(int current, int max);
    }

    @SuppressLint("StaticFieldLeak")
    private class UpdateTaskCore extends AsyncTask<Void, Void, Void> {
        private GitHubService.Asset asset;
        private CoreCallback callback;

        UpdateTaskCore(GitHubService.Asset asset, CoreCallback callback) {
            this.asset = asset;
            this.callback = callback;
        }

        @Override
        protected Void doInBackground(Void... params) {
            String url = this.asset.browser_download_url;
            InputStream is = DownloadUtil.download(url);

            if (is != null) {
                String file_path = saveTempFile(is, this.asset.name, (int current) -> {
                    this.callback.onProgressUpdate(current, this.asset.size);
                });
                if (file_path != null) {
                    boolean result = installUpdate(file_path);
                    if (result)
                        this.callback.onSuccess();
                }
            } else {
                this.callback.onError();
            }

            return null;
        }
    }
}
