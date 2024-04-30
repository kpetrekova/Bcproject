package com.android.transcribeczech;

import static androidx.camera.view.CameraController.COORDINATE_SYSTEM_VIEW_REFERENCED;

import android.Manifest;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.camera2.Camera2Config;
import androidx.camera.core.CameraXConfig;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.mlkit.vision.MlKitAnalyzer;
import androidx.camera.view.LifecycleCameraController;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;


import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.google.android.gms.common.annotation.KeepName;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;


@KeepName
public final class MainActivity extends AppCompatActivity implements CameraXConfig.Provider{

    private boolean paused = false;
    private boolean torch = false;
    private PreviewView previewView;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private LifecycleCameraController cameraController;
    private GraphicOverlay graphicOverlay;
    static HashMap<String, String> map = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkPermissions();

        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);


        setContentView(R.layout.activity_main);

        previewView = findViewById(R.id.previewView);
        graphicOverlay = findViewById(R.id.graphic_overlay);

        try {
            InputStream is = getResources().openRawResource(R.raw.slovnik);
            InputStreamReader isr = new InputStreamReader(is);

            BufferedReader reader = new BufferedReader(isr);
            while (reader.ready()) {
                String line = reader.readLine();
                String[] aa = line.split(";");
                map.put(aa[0], aa[1]);
            }
        } catch (Exception e) {
            Log.e("OnCreate", e.getMessage(), e);
        }

        AppCompatActivity activity = this;
        ImageButton pauseButton = findViewById(R.id.pause);
        ImageButton torchButton = findViewById(R.id.torch);
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (paused) {
                   // startCameraSource();

                    cameraController.bindToLifecycle(activity);

                    paused = false;
                    pauseButton.setImageDrawable(getDrawable(R.drawable.ic_pause));
                }
                else {
                   // preview.stop();
                    cameraController.unbind();
                    paused = true;
                    pauseButton.setImageDrawable(getDrawable(R.drawable.ic_play));

                    torch= false;
                    torchButton.setImageDrawable(getDrawable(R.drawable.ic_flash_on));
                }
            }
        });

        torchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!paused) {
                    torch = !torch;
                    if (torch) {
                        previewView.getController().getCameraControl().enableTorch(true);
                        torchButton.setImageDrawable(getDrawable(R.drawable.ic_flash_off));
                    } else {
                        previewView.getController().getCameraControl().enableTorch(false);
                        torchButton.setImageDrawable(getDrawable(R.drawable.ic_flash_on));
                    }
                }
            }
        });

        cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled for this Future.
                // This should never be reached.
            }
        }, ContextCompat.getMainExecutor(this));

    }



    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        TextRecognizer textRecognizer = TextRecognition.getClient(new TextRecognizerOptions.Builder().build());
        ColorDetector colorDetector = new ColorDetector();
        cameraController = new LifecycleCameraController(this);

        MlKitAnalyzer mlKitAnalyzer = new MlKitAnalyzer(
                List.of(textRecognizer, colorDetector),
                COORDINATE_SYSTEM_VIEW_REFERENCED,
                ContextCompat.getMainExecutor(this), result -> {
            Text text = result.getValue(textRecognizer);
            graphicOverlay.clear();
            graphicOverlay.add(new TextGraphic(graphicOverlay, result.getValue(colorDetector), text));

        });

        cameraController.setImageAnalysisAnalyzer(ContextCompat.getMainExecutor(this), mlKitAnalyzer);
        cameraController.bindToLifecycle(this);
        previewView.setController(cameraController);
    }



    private void checkPermissions() {
        List<String> permissions = new ArrayList<>();
        String message = "";

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.CAMERA);
            // message += "\nTake photos.";
        }


        if (!permissions.isEmpty()) {
            String[] params = (String[]) permissions.toArray(new String[permissions.size()]);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(params, 124);
            }
        }
    }



    @NonNull
    @Override
    public CameraXConfig getCameraXConfig() {
        return Camera2Config.defaultConfig();
    }
}
