package com.jema.fancoin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class UploadVideoActivity extends AppCompatActivity {

    private static final int PICK_FROM_FILE = 1;
    private Button uploadVideo;
    private ImageView iconUpload, playBtn, pauseBtn, backBtn;
    String DocumentId;
    TextView selector;
    CardView bottomCard;
    StorageReference videoRef;

    Uri vidUri = null;
    PlayerView playerView;
    ExoPlayer simpleExoPlayer;
    ProgressDialog progressBar;
    private int progressBarStatus = 0;
    private Handler progressBarHandler = new Handler();
    private long fileSize = 0;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_video);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();


        Intent i = getIntent();
        DocumentId = i.getStringExtra("DocumentId");

        uploadVideo = findViewById(R.id.reply_upload_btn);
        playerView = findViewById(R.id.upload_video_selected);
        iconUpload = findViewById(R.id.upload_video_icon);
        playBtn = findViewById(R.id.upload_exo_play);
        pauseBtn = findViewById(R.id.upload_exo_pause);
        backBtn = findViewById(R.id.upload_video_back_btn);
        selector = findViewById(R.id.upload_video_selector);
        bottomCard = findViewById(R.id.upload_video_bottom_card);

        simpleExoPlayer = new ExoPlayer.Builder(UploadVideoActivity.this).build();

        playerView.setClipToOutline(true);

        selector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iconUpload.setVisibility(View.GONE);
                Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                i.setType("video/*");
                startActivityForResult(i, PICK_FROM_FILE);
            }
        });

        uploadVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!vidUri.toString().equalsIgnoreCase("")){
                    uploadVideo(vidUri);
                }
                else {
                    Toast.makeText(UploadVideoActivity.this, "Choose Valid Video", Toast.LENGTH_SHORT).show();

                }
            }
        });


        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent i = new Intent(UploadVideoActivity.this, RequestsActivity.class);
                startActivity(i);
                finish();
            }
        });

        pauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                simpleExoPlayer.pause();
                pauseBtn.setVisibility(View.GONE);
                playBtn.setVisibility(View.VISIBLE);
            }
        });

        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                simpleExoPlayer.play();
                pauseBtn.setVisibility(View.VISIBLE);
                playBtn.setVisibility(View.GONE);
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK)
            return;

        if (requestCode == PICK_FROM_FILE) {
            vidUri = data.getData();

            bottomCard.setVisibility(View.VISIBLE);
            selector.setVisibility(View.GONE);

//            uploadVideo(vidUri);

        }


        playerView.setPlayer(simpleExoPlayer);
        playerView.setVisibility(View.VISIBLE);
        pauseBtn.setVisibility(View.VISIBLE);
        playBtn.setVisibility(View.GONE);

        MediaItem mediaItem = MediaItem.fromUri(vidUri);
        simpleExoPlayer.setMediaItem(mediaItem);
        simpleExoPlayer.prepare();
        simpleExoPlayer.seekTo(1);
        simpleExoPlayer.play();

    }

    private void uploadVideo(Uri videoUri) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();

//        the path of the video file is going to contain, the order's uid, date and time to make it unique
        Date now = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd_MM_yyyy_HH-m-s"); // we change the format to give a valid name string
        String strDate= formatter.format(now);
        String filename = DocumentId.concat("_" + strDate + ".mp4"); // the name containing the document id, date formatted and the extension

        videoRef = storageRef.child("/orders/" + filename); // the directory on firebase


//        String str = DateUtils.getRelativeDateTimeString(UploadVideoActivity.this, now, DateUtils.MINUTE_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, 0);

        if (videoUri != null) {
            UploadTask uploadTask = videoRef.putFile(videoUri);

            // creating progress bar dialog
            progressBar = new ProgressDialog(UploadVideoActivity.this);
            progressBar.setCancelable(true);
            progressBar.setMessage("File uploading ...");
            progressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressBar.setProgress(0);
            progressBar.setMax(100);
            progressBar.show();
            //reset progress bar and filesize status
            progressBarStatus = 0;
            fileSize = 0;
            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        progressBar.dismiss();

//                        updating video url into the orders document
                        HashMap<String , Object> order = new HashMap<>();
                        order.put("video" , filename);
                        order.put("upload_date", now);
                        Log.d("JemaTag", DocumentId);
                        db.collection("Orders").document(DocumentId).update(order).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(UploadVideoActivity.this, "Video Upload Completed", Toast.LENGTH_SHORT).show();
//                                finish(); // goes to previous activity
                            }
                        });

                    }
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                    double progress = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                    progressBar.setProgress((int) progress);
                }
            });
        } else {
            progressBar.dismiss();
            Toast.makeText(UploadVideoActivity.this, "upload failed!", Toast.LENGTH_SHORT).show();
        }

    }

// StorageReference videoRef;
}