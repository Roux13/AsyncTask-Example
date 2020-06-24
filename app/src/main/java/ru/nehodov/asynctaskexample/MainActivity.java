package ru.nehodov.asynctaskexample;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class MainActivity extends AppCompatActivity {

    private static final String BUNDLE_PROGRESS = "progress";

    private ProgressBar progressBar;
    private TextView info;

    private Disposable disposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button loadBtn = findViewById(R.id.loadBtn);
        Button stopBtn = findViewById(R.id.stopBtn);
        progressBar = findViewById(R.id.progressBar);
        info = findViewById(R.id.info);
        boolean recreated = savedInstanceState != null;
        final int currentProgress =
                recreated ? savedInstanceState.getInt(BUNDLE_PROGRESS, 0) : 0;
        info.setText(currentProgress + "%");
        loadBtn.setOnClickListener(v -> start(currentProgress));
        stopBtn.setOnClickListener(this::stop);
        if (recreated) {
            start(currentProgress);
        }
    }

    public void start(int currentProgress) {
        if (disposable == null) {
            progressBar.setVisibility(View.VISIBLE);
            this.disposable = Observable.interval(1, TimeUnit.SECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .takeWhile(v -> v < 101)
                    .doOnComplete(() -> {
                        Toast.makeText(this, "Finish", Toast.LENGTH_SHORT).show();
                        info.setText("0%");
                        progressBar.setProgress(0);
                        progressBar.setVisibility(View.INVISIBLE);
                        disposable.dispose();
                        disposable = null;
                    })
                    .subscribe(v -> {
                        info.setText(currentProgress + v.intValue() + "%");
                        progressBar.setProgress(currentProgress + v.intValue());
                    });
        }
    }

    public void stop(View view) {
        if (disposable != null) {
            disposable.dispose();
            progressBar.setProgress(0);
            info.setText("0%");
            progressBar.setVisibility(View.INVISIBLE);
            Toast.makeText(this, "Stoped", Toast.LENGTH_SHORT).show();
            disposable = null;
        }
    }

    @Override
    protected void onDestroy() {
        if (this.disposable != null) {
            this.disposable.dispose();
        }
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(BUNDLE_PROGRESS, progressBar.getProgress());
    }
}