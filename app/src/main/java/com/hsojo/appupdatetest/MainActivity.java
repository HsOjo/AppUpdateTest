package com.hsojo.appupdatetest;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.hsojo.appupdatetest.service.GitHubService;
import com.hsojo.appupdatetest.task.UpdateTask;
import com.hsojo.appupdatetest.util.VersionUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MainActivity extends AppCompatActivity {
    private String TAG = "MainActivity";
    private UpdateTask t_update;
    private TextView tv_file_name;
    private TextView tv_sub_progress;
    private TextView tv_total_progress;
    private ProgressBar pb_sub;
    private ProgressBar pb_total;
    private Context app_context;
    private String app_version;
    private GitHubService.GitHub service_github;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab_test = findViewById(R.id.fab_test);
        fab_test.setOnClickListener(view -> callbackButtonTest());

        this.tv_file_name = findViewById(R.id.tv_file_name);
        this.tv_sub_progress = findViewById(R.id.tv_sub_progress);
        this.tv_total_progress = findViewById(R.id.tv_total_progress);
        this.pb_sub = findViewById(R.id.pb_sub);
        this.pb_total = findViewById(R.id.pb_total);

        this.service_github = GitHubService.generate();

        this.app_context = getApplicationContext();
    }

    public UpdateTask buildUpdateTask() {
        return new UpdateTask(
                this.app_context,
                String.format("%s/%s", this.app_context.getFilesDir().getPath(), "update_data"),
                new UpdateTask.Callback() {
                    @Override
                    public void onTotalProgressUpdate(int current, int max) {
                        runOnUiThread(() -> {
                            pb_total.setProgress(current);
                            pb_total.setMax(max);
                            tv_total_progress.setText(String.format("%s/%s", current, max));
                        });
                    }

                    @Override
                    public void onSubProgressUpdate(int current, int max) {
                        runOnUiThread(() -> {
                            pb_sub.setProgress(current);
                            pb_sub.setMax(max);
                            tv_sub_progress.setText(String.format("%s/%s", current, max));
                        });
                    }

                    @Override
                    public void onTaskAssetUpdate(GitHubService.Asset asset) {
                        runOnUiThread(() -> {
                            tv_file_name.setText(asset.name);
                            tv_sub_progress.setText(String.format("%s/%s", 0, asset.size));
                        });
                    }
                }
        );
    }


    public ArrayList<UpdateTask.Asset> getNewReleaseAssets(List<GitHubService.Release> releases, String currnet_version) {
        int current_version_num = VersionUtil.toIntVer(currnet_version);
        ArrayList<UpdateTask.Asset> assets = new ArrayList<>();
        for (GitHubService.Release release : releases) {
            int version_num = VersionUtil.toIntVer(release.tag_name);
            if (version_num > current_version_num) {
                if (release.assets.size() >= 1) {
                    GitHubService.Asset asset = release.assets.get(0);
                    assets.add(new UpdateTask.Asset(asset, release.tag_name));
                }
            }
        }

        return assets;
    }

    public void callbackButtonTest() {
        this.app_version = VersionUtil.getVersion(app_context);
        Log.d(TAG, "callbackButtonTest: [current_version] " + this.app_version);

        if (this.t_update != null) {
            this.t_update.cancel(true);
        }

        this.t_update = buildUpdateTask();

        Call<List<GitHubService.Release>> call = this.service_github.releases("HsOjo", "SleeperX");
        call.enqueue(new Callback<List<GitHubService.Release>>() {
            @Override
            public void onResponse(@NonNull Call<List<GitHubService.Release>> call, @NonNull Response<List<GitHubService.Release>> response) {
                assert response.body() != null;

                List<GitHubService.Release> releases = response.body();
                Collections.reverse(releases);
                ArrayList<UpdateTask.Asset> assets = getNewReleaseAssets(releases, app_version);

                Log.d(TAG, "onResponse: " + t_update.getStatus());
                if (t_update.getStatus() != AsyncTask.Status.RUNNING)
                    t_update.execute((UpdateTask.Asset[]) assets.toArray(new UpdateTask.Asset[0]));
            }

            @Override
            public void onFailure(@NonNull Call<List<GitHubService.Release>> call, @NonNull Throwable t) {
                System.out.println("Failed to get Release.");
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
