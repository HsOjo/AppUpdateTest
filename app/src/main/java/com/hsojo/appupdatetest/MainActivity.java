package com.hsojo.appupdatetest;

import android.content.Context;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    public String VERSION = "0.0.0";
    UpdateTask t_update;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab_test = findViewById(R.id.fab_test);
        fab_test.setOnClickListener(view -> callbackButtonTest());

        TextView tv_file_name = findViewById(R.id.tv_file_name);
        TextView tv_sub_progress = findViewById(R.id.tv_sub_progress);
        TextView tv_total_progress = findViewById(R.id.tv_total_progress);
        ProgressBar pb_sub = findViewById(R.id.pb_sub);
        ProgressBar pb_total = findViewById(R.id.pb_total);

        MainActivity this_activity = this;
        Context app_context = getApplicationContext();
        this.t_update = new UpdateTask(
                app_context.getCacheDir().getPath(),
                app_context.getFilesDir().getPath(),
                new UpdateTask.Callback() {
                    @Override
                    public void onTotalProgressUpdate(int current, int max) {
                        this_activity.runOnUiThread(() -> {
                            pb_total.setProgress(current);
                            pb_total.setMax(max);
                            tv_total_progress.setText(String.format("%s/%s", current, max));
                        });
                    }

                    @Override
                    public void onSubProgressUpdate(int current, int max) {
                        this_activity.runOnUiThread(() -> {
                            pb_sub.setProgress(current);
                            pb_sub.setMax(max);
                            tv_sub_progress.setText(String.format("%s/%s", current, max));
                        });
                    }

                    @Override
                    public void onTaskAssetUpdate(GitHubService.Asset asset) {
                        this_activity.runOnUiThread(() -> tv_file_name.setText(asset.name));
                    }
                }
        );
    }

    public int toIntVer(String version) {
        try {
            return Integer.parseInt(version.replace(".", ""));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public ArrayList<GitHubService.Asset> getNewReleaseAssets(List<GitHubService.Release> releases, String currnet_version) {
        int current_version_num = toIntVer(currnet_version);
        ArrayList<GitHubService.Asset> assets = new ArrayList<>();
        for (GitHubService.Release release : releases) {
            int version_num = toIntVer(release.tag_name);
            if (version_num > current_version_num) {
                if (release.assets.size() >= 1) {
                    GitHubService.Asset asset = release.assets.get(0);
                    assets.add(asset);
                }
            }
        }

        return assets;
    }

    public void callbackButtonTest() {
        try {
            GitHubService.GitHub github = GitHubService.generate();
            Call<List<GitHubService.Release>> call = github.releases("HsOjo", "SleeperX");
            call.enqueue(new Callback<List<GitHubService.Release>>() {
                @Override
                public void onResponse(@NonNull Call<List<GitHubService.Release>> call, @NonNull Response<List<GitHubService.Release>> response) {
                    assert response.body() != null;

                    List<GitHubService.Release> releases = response.body();
                    Collections.reverse(releases);
                    ArrayList<GitHubService.Asset> urls = getNewReleaseAssets(releases, VERSION);
                    t_update.execute((GitHubService.Asset[]) urls.toArray(new GitHubService.Asset[0]));
                }

                @Override
                public void onFailure(@NonNull Call<List<GitHubService.Release>> call, @NonNull Throwable t) {
                    System.out.println("Failed to get Release.");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
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
