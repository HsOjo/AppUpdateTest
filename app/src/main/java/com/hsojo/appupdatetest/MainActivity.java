package com.hsojo.appupdatetest;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import com.hsojo.appupdatetest.service.GitHubService;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {
    TextView tv_content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        this.tv_content = findViewById(R.id.tv_content);
        Button b_test = findViewById(R.id.b_test);
        b_test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callbackButtonTest();
            }
        });
    }

    public void callbackButtonTest() {
        try {
            GitHubService.GitHub github = GitHubService.generate();
            Call<List<GitHubService.Release>> call = github.releases("HsOjo", "SleeperX");
            call.enqueue(new Callback<List<GitHubService.Release>>() {
                @Override
                public void onResponse(Call<List<GitHubService.Release>> call, Response<List<GitHubService.Release>> response) {
                    assert response.body() != null;
                    for (GitHubService.Release r :
                            response.body()) {
                        String r_str = String.format("%s\n%s\n%s\n\n", r.name, r.tag_name, r.body);
                        tv_content.append(r_str);
                    }

                }

                @Override
                public void onFailure(Call<List<GitHubService.Release>> call, Throwable t) {

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
    public boolean onOptionsItemSelected(MenuItem item) {
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
