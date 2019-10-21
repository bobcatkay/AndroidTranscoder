package com.github.transcoder;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.transcoder.jni.FFmpegCmd;
import com.github.transcoder.util.MediaTool;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class TranscodeActivity extends AppCompatActivity implements View.OnClickListener {

    private final static int CODE_REQUEST_WRITE_EXTERNAL = 0x100;
    private int PICK_VIDEO_REQUEST = 0x2;
    private String mVideoPath;
    private ImageView mIvCover;
    private TextView mTvResolution;
    private TextView mTvFps;
    private TextView mTvBitrate;
    private TextView mTvDuration;
    private TextView mTvVcodec;
    private TextView mTvRotation;
    private EditText mEditTargetWidth;
    private EditText mEditTargetHeight;
    private EditText mEditTargetFPS;
    private EditText mEditTargetBitrate;
    private EditText mEditSavePath;
    private ProgressBar mPbTranscode;
    private TextView mTvTimeSpent;
    private TextView mTvProgress;
    private MediaTool.VideoInfo mInfo;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private Button mBtnStartTranscode;
    private Spinner mSpinnerPresets;
    private String mPreset;

    public static String getDetailTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.getDefault());
        return sdf.format(System.currentTimeMillis());
    }

    public static String getVideoPath() {
        String path = null;
        File folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
        if (!folder.exists()) {
            boolean mkdirs = folder.mkdirs();
        }
        path = folder.getAbsolutePath();
        if (!path.endsWith("/")) {
            return path + "/";
        } else {
            return path;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transcode);

        mIvCover = findViewById(R.id.iv_cover);
        mIvCover.setOnClickListener(this);
        mBtnStartTranscode = findViewById(R.id.btn_start_transcode);
        mBtnStartTranscode.setOnClickListener(this::onClick);
        mTvResolution = findViewById(R.id.tv_resolution);
        mTvFps = findViewById(R.id.tv_fps);
        mTvBitrate = findViewById(R.id.tv_bitrate);
        mTvDuration = findViewById(R.id.tv_duration);
        mTvVcodec = findViewById(R.id.tv_vcodec);
        mTvRotation = findViewById(R.id.tv_rotation);

        mEditTargetWidth = findViewById(R.id.edit_width);
        mEditTargetHeight = findViewById(R.id.edit_height);
        mEditTargetFPS = findViewById(R.id.edit_fps);
        mEditTargetBitrate = findViewById(R.id.edit_bitrate);
        mEditSavePath = findViewById(R.id.edit_save_path);
        mSpinnerPresets = findViewById(R.id.spinner_preset);
        mSpinnerPresets.setDropDownWidth(300);
        mSpinnerPresets.setDropDownHorizontalOffset(100);
        mSpinnerPresets.setDropDownVerticalOffset(100);
        String[] presets = getResources().getStringArray(R.array.presets);
        mPreset = presets[1];
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_selected_preset, presets);
        adapter.setDropDownViewResource(R.layout.item_presets);
        mSpinnerPresets.setAdapter(adapter);
        mSpinnerPresets.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mPreset = presets[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mSpinnerPresets.setSelection(1);

        mPbTranscode = findViewById(R.id.pb_transcode);
        mTvTimeSpent = findViewById(R.id.tv_time_spent);
        mTvProgress = findViewById(R.id.tv_trascode_progress);

        checkPermission();
    }

    private void checkPermission() {
        int permissions = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissions != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    CODE_REQUEST_WRITE_EXTERNAL
            );
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_cover:
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, PICK_VIDEO_REQUEST);
                break;
            case R.id.btn_start_transcode:
                mIvCover.setClickable(false);
                mBtnStartTranscode.setEnabled(false);
                mTvTimeSpent.setText("耗时：00:00");
                new Thread(this::startTranscode).start();
                break;
        }
    }

    private void startTranscode() {
        long startTime = System.currentTimeMillis();

        FFmpegCmd.transcode(mVideoPath,
                mEditSavePath.getText().toString(),
                Integer.valueOf(mEditTargetFPS.getText().toString()),
                Integer.valueOf(mEditTargetBitrate.getText().toString()),
                Integer.valueOf(mEditTargetWidth.getText().toString()),
                Integer.valueOf(mEditTargetHeight.getText().toString()),
                mInfo.duration,
                mPreset,
                progress -> mHandler.post(() -> {
                    mPbTranscode.setProgress(progress);
                    mTvProgress.setText(progress + "%");
                    int time = (int) ((System.currentTimeMillis() - startTime) / 1000);
                    mTvTimeSpent.setText("耗时：" + MediaTool.parseTime(time));
                }));
        MediaTool.insertMedia(getApplicationContext(), mEditSavePath.getText().toString());
        mHandler.postDelayed(() -> {
            mIvCover.setClickable(true);
            mBtnStartTranscode.setEnabled(true);
            Uri uri = Uri.parse(mEditSavePath.getText().toString());
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "video/mp4");
            try {
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 1000);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == PICK_VIDEO_REQUEST && resultCode == RESULT_OK && null != data) {
            Uri selectedVideo = data.getData();
            String[] filePathColumn = {MediaStore.Video.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedVideo,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            mVideoPath = cursor.getString(columnIndex);
            cursor.close();
            updateVideo();
        }
    }

    private void updateVideo() {
        Bitmap videoFrame = MediaTool.getVideoFrame(mVideoPath, 5000000);
        mIvCover.setImageBitmap(videoFrame);
        mInfo = MediaTool.getVideoInfo(mVideoPath);
        mTvResolution.setText("分辨率：" + mInfo.width + "x" + mInfo.height);
        mTvBitrate.setText("码率(Kbps)：" + mInfo.bitrate / 1024);
        mTvFps.setText("FPS：" + mInfo.fps);
        mTvDuration.setText("视频时长：" + MediaTool.parseTime((int) (mInfo.duration / 1000)));
        mTvVcodec.setText("Video Codec:" + mInfo.videoCodec);
        mTvRotation.setText("Video Rotation:" + mInfo.rotation);

        mEditTargetBitrate.setText("4000");
        mEditTargetFPS.setText("30");
//        mEditTargetWidth.setText(mInfo.width + "");
//        mEditTargetHeight.setText(mInfo.height + "");
        String path = getVideoPath() + getDetailTime() + ".mp4";
        mEditSavePath.setText(path);
    }
}
