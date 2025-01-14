package com.example.mainuddin.doapp;

import android.app.ProgressDialog;
import android.content.ClipDescription;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.gsconrad.richcontentedittext.RichContentEditText;
import com.leocardz.link.preview.library.LinkPreviewCallback;
import com.leocardz.link.preview.library.SourceContent;
import com.leocardz.link.preview.library.TextCrawler;
import com.shivtechs.maplocationpicker.LocationPickerActivity;
import com.shivtechs.maplocationpicker.MapUtility;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.Sinch;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallClient;
import com.sinch.android.rtc.calling.CallClientListener;
import com.sinch.android.rtc.calling.CallListener;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.inputmethod.InputContentInfoCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;


public class ChatActivity extends AppCompatActivity implements ExampleBottomSheetDialog.BottomSheetListener
{
    private String messageReceiverID, messageReceiverName, messageReceiverImage, messageSenderID;

    private TextView userName, userLastSeen;
    private CircleImageView userImage;

    private Toolbar ChatToolBar;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;

    private ImageButton SendMessageButton, SendFilesButton,SendLikeButton;
    private RichContentEditText MessageInputText;

    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;
    private RecyclerView userMessagesList;

    public  String checker = "",myurl = "";
    private StorageTask uploadTask;
    private Uri fileUri;

    private ProgressDialog loadingBar;

    private String saveCurrentTime, saveCurrentDate;

    ValueEventListener seenListener;

    boolean notify = false;

    boolean update = false;
    private DatabaseReference  NotificationRef;

    Call call =null;
    SinchClient sinchClient;
    AlertDialog.Builder builder,builder1;

    private static final int ADDRESS_PICKER_REQUEST = 1020;

    private static final String AUDIO_RECORDER_FILE_EXT_3GP = ".3gp";
    private static final String AUDIO_RECORDER_FILE_EXT_MP4 = ".mp4";
    private static final String AUDIO_RECORDER_FOLDER = "AudioRecorder";
    private MediaRecorder recorder = null;
    private int currentFormat = 0;
    private int output_formats[] = { MediaRecorder.OutputFormat.MPEG_4,             MediaRecorder.OutputFormat.THREE_GPP };
    private String file_exts[] = { AUDIO_RECORDER_FILE_EXT_MP4, AUDIO_RECORDER_FILE_EXT_3GP };

    String fileName;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_chat);


        mAuth = FirebaseAuth.getInstance();
        messageSenderID = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();
        NotificationRef = FirebaseDatabase.getInstance().getReference().child("Notifications");

        messageReceiverID = getIntent().getExtras().get("visit_user_id").toString();
        messageReceiverName = getIntent().getExtras().get("visit_user_name").toString();
        messageReceiverImage = getIntent().getExtras().get("visit_image").toString();


        IntializeControllers();


        userName.setText(messageReceiverName);
        Picasso.get().load(messageReceiverImage).placeholder(R.drawable.profile_image).into(userImage);

        SendMessageButton.setVisibility(View.GONE);
        SendLikeButton.setVisibility(View.VISIBLE);


        MessageInputText.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
                if(!MessageInputText.getText().toString().equals("")){
                    System.out.println("kl");
                    SendMessageButton.setVisibility(View.VISIBLE);
                    SendLikeButton.setVisibility(View.GONE);
                }else{
                    SendMessageButton.setVisibility(View.GONE);
                    SendLikeButton.setVisibility(View.VISIBLE);
                }
            }

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {

            }
        });



        ImageButton buttonOpenBottomSheet = findViewById(R.id.send_options_btn);
        buttonOpenBottomSheet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ExampleBottomSheetDialog bottomSheet = new ExampleBottomSheetDialog();
                bottomSheet.show(getSupportFragmentManager(), "exampleBottomSheet");
            }
        });

        SendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                notify =true;
                SendMessage();
            }
        });

        SendLikeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notify = true;
                SendLike();
            }
        });


        DisplayLastSeen();


        SeenMessage();


        updateUserStatus("online");



        SendFilesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CharSequence Options[] = {
                        "Images",
                        "PDF Files",
                        "Text Files",
                        "Ms Power Point Files",
                        "Ms Word Files",
                        "Audio"

                };
                AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this,R.style.RoundedDialog);
                builder.setTitle("Select File");
                builder.setItems(Options,new DialogInterface.OnClickListener(){

                    @Override
                    public  void onClick(DialogInterface dialogInterface,int i){

                        if(i==0){
                            checker = "image";

                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            startActivityForResult(intent.createChooser(intent,"Select Images"),438);


                        }else if(i==1)
                        {
                            checker = "pdf";

                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/pdf");
                            startActivityForResult(intent.createChooser(intent,"Select PDF"),438);

                        }
                        else if(i==2)
                        {
                            checker = "txt";

                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("text/plain");
                            startActivityForResult(intent.createChooser(intent,"Select Text File"),438);

                        }
                        else if(i==3)
                        {
                            checker = "pptx";

                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/vnd.ms-powerpoint");
                            startActivityForResult(intent.createChooser(intent,"Select MS Power Point File"),438);

                        }
                        else if(i==4)
                        {
                            checker = "docx";

                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/msword");
                            startActivityForResult(intent.createChooser(intent,"Select MS Word File"),438);

                        }
                        else if(i==5)
                        {
                            checker = "mp3";

                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("audio/*");
                            startActivityForResult(intent.createChooser(intent,"Select Audio File"),438);

                        }


                    }

                });
                builder.show();
            }
        });




        // Instantiate a SinchClient using the SinchClientBuilder.
        android.content.Context context = this.getApplicationContext();
        sinchClient = Sinch.getSinchClientBuilder()
                .context(context)
                .applicationKey("bbe7241b-dbb7-4ed7-bd5d-e92092bc19eb")
                .applicationSecret("omkVJWhjQkCd6IpAvUn51w==")
                .environmentHost("clientapi.sinch.com")
                .userId(messageSenderID)
                .build();

        sinchClient.setSupportCalling(true);
        sinchClient.start();
        sinchClient.startListeningOnActiveConnection();
        sinchClient.getCallClient().addCallClientListener(new SinchCallClientListener());


        ImageButton call_user = findViewById(R.id.call_user);

        call_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(call==null){

                    call = sinchClient.getCallClient().callUser(messageReceiverID);
                    call.addCallListener(new SinchCallListener());

                    builder = new AlertDialog.Builder(ChatActivity.this,R.style.RoundedDialog);
                    builder.setMessage("Calling.");
                    builder.setCancelable(false);
                    builder.setPositiveButton("Hang Up", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                            try{
                                call.hangup();
                            }catch (Exception e){

                            }
                            Toast.makeText(getApplicationContext(),"Hang Up",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                    AlertDialog alert = builder.create();
                    alert.setCancelable(false);
                    alert.setTitle("Alert");
                    alert.show();
                }

            }
        });




    }

    private String getFilename(){
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath,AUDIO_RECORDER_FOLDER);

        if(!file.exists()){
            file.mkdirs();
        }

        fileName = file.getAbsolutePath() + "/" + System.currentTimeMillis() + file_exts[currentFormat];
        return (fileName);
    }

    private void startRecording(){
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(output_formats[currentFormat]);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setOutputFile(getFilename());
        recorder.setOnErrorListener(errorListener);
        recorder.setOnInfoListener(infoListener);

        try {
            recorder.prepare();
            recorder.start();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private MediaRecorder.OnErrorListener errorListener = new MediaRecorder.OnErrorListener() {
        @Override
        public void onError(MediaRecorder mr, int what, int extra) {
            AppLog.logString("Error: " + what + ", " + extra);
        }
    };

    private MediaRecorder.OnInfoListener infoListener = new MediaRecorder.OnInfoListener() {
        @Override
        public void onInfo(MediaRecorder mr, int what, int extra) {
            AppLog.logString("Warning: " + what + ", " + extra);
        }
    };

    private void stopRecording(){
        if(null != recorder){
            recorder.stop();
            recorder.reset();
            recorder.release();

            recorder = null;
        }
    }

    @Override
    public void onButtonClicked(String text) {

        if(text.equals("location")){
            locationPlacesIntent();
        }else if(text.equals("audio")){

            LayoutInflater layoutInflaterAndroid = LayoutInflater.from(ChatActivity.this);
            View mView = layoutInflaterAndroid.inflate(R.layout.options_r, null);
            AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(ChatActivity.this,R.style.RoundedDialog);
            alertDialogBuilderUserInput.setView(mView);

            final Button button = (Button) mView.findViewById(R.id.button1);

            button.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    // TODO Auto-generated method stub
                    switch(event.getAction()){
                        case MotionEvent.ACTION_DOWN:
                            AppLog.logString("Start Recording");
                            button.setText("Recording..");
                            button.setBackgroundColor(ContextCompat.getColor(ChatActivity.this,R.color.mapbox_plugins_green));
                            startRecording();
                            return true;
                        case MotionEvent.ACTION_UP:
                            AppLog.logString("stop Recording");
                            button.setText("Stopped");
                            button.setBackgroundColor(ContextCompat.getColor(ChatActivity.this,R.color.design_default_color_error));
                            stopRecording();




                            break;
                    }
                    return false;
                }
            });

            alertDialogBuilderUserInput.setCancelable(false)
                    .setPositiveButton("Send", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogBox, int id) {
                            // ToDo get user input here
                            SendAudio(Uri.fromFile(new File(fileName)));
                        }
                    })

                    .setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialogBox, int id) {
                                    dialogBox.cancel();
                                }
                            });



            AlertDialog alertDialogAndroid = alertDialogBuilderUserInput.create();
            alertDialogAndroid.show();




        }

    }

    private  class  SinchCallListener implements  CallListener{

        @Override
        public void onCallProgressing(Call call) {
            Toast.makeText(ChatActivity.this,"Ringing.....",Toast.LENGTH_LONG).show();
        }

        @Override
        public void onCallEstablished(Call call) {
            Toast.makeText(ChatActivity.this,"Call established",Toast.LENGTH_LONG).show();
            setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);

        }

        @Override
        public void onCallEnded(Call endedcall) {
            Toast.makeText(ChatActivity.this,"Call ended",Toast.LENGTH_LONG).show();
            call = null;
            endedcall.hangup();

            try {
                AlertDialog alert = builder.show();
                alert.setTitle("AlertDialogExample");
                alert.cancel();
            }catch (Exception E){

            }

            try {
                AlertDialog alert1 = builder1.show();
                alert1.setTitle("AlertDialogExample");
                alert1.cancel();
            }catch (Exception E){

            }




            setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);


        }

        @Override
        public void onShouldSendPushNotification(Call call, List<PushPair> list) {

        }
    }

    private class SinchCallClientListener implements CallClientListener{

        @Override
        public void onIncomingCall(CallClient callClient, final Call incomingcall) {


            builder1 = new AlertDialog.Builder(ChatActivity.this,R.style.RoundedDialog);
            builder1.setMessage("CALLING...");
            builder1.setPositiveButton("Reject", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                    call = incomingcall;
                    call.hangup();
                    Toast.makeText(getApplicationContext(),"Reject",
                            Toast.LENGTH_SHORT).show();
                }
            });

            builder1.setNegativeButton("Pick", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    call = incomingcall;
                    call.answer();
                    call.addCallListener(new SinchCallListener());
                    Toast.makeText(ChatActivity.this,"Hang Up",Toast.LENGTH_LONG).show();
                }
            });

            AlertDialog alert = builder1.create();
            alert.setTitle("AlertDialogExample");
            alert.show();

        }
    }





    private void IntializeControllers()
    {
        ChatToolBar = (Toolbar) findViewById(R.id.chat_toolbar);
        setSupportActionBar(ChatToolBar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setBackgroundDrawable(ContextCompat.getDrawable(this,R.drawable.message_edit));

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.custom_chat_bar, null);
        actionBar.setCustomView(actionBarView);

        ChatToolBar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_arrow_back_black_24dp));
        ChatToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //What to do on back clicked
                finish();
            }
        });

        userName = (TextView) findViewById(R.id.custom_profile_name);
        userLastSeen = (TextView) findViewById(R.id.custom_user_last_seen);
        userImage = (CircleImageView) findViewById(R.id.custom_profile_image);

        SendMessageButton = (ImageButton) findViewById(R.id.send_message_btn);
        SendLikeButton = findViewById(R.id.send_like_btn);
        SendFilesButton = (ImageButton) findViewById(R.id.send_files_btn);
        MessageInputText =  findViewById(R.id.input_message);
        MessageInputText.runListenerInBackground = false;
        MessageInputText.setOnRichContentListener(new RichContentEditText.OnRichContentListener() {
            // Called when a keyboard sends rich content
            @Override
            public void onRichContent(Uri contentUri, ClipDescription description) {
                if (description.getMimeTypeCount() > 0) {
                    System.out.println(contentUri);
                    StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Image Files");
                    String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
                    String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

                    DatabaseReference userMessageKeyRef = RootRef.child("Messages")
                            .child(messageSenderID).child(messageReceiverID).push();

                    String messagePushID = userMessageKeyRef.getKey();

                    final String fileExtension = MimeTypeMap.getSingleton()
                            .getExtensionFromMimeType(description.getMimeType(0));
                    final String filename = messagePushID+ "." + fileExtension;
                    File richContentFile = new File(getFilesDir(), filename);
                    if (!writeToFileFromContentUri(richContentFile, contentUri)) {
                        Toast.makeText(ChatActivity.this,
                                "failed", Toast.LENGTH_LONG).show();
                    } else {

                        final StorageReference filePath = storageReference.child(filename);


                        uploadTask = filePath.putFile(contentUri);
                        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        myurl = uri.toString();
                                        Map messageTextBody = new HashMap();
                                        messageTextBody.put("message", myurl);
                                        messageTextBody.put("name", contentUri.getLastPathSegment());
                                        messageTextBody.put("lat", "0");
                                        messageTextBody.put("lon", "0");
                                        messageTextBody.put("title", "");
                                        messageTextBody.put("des", "");
                                        messageTextBody.put("img", "");
                                        messageTextBody.put("type", "image");
                                        messageTextBody.put("from", messageSenderID);
                                        messageTextBody.put("to", messageReceiverID);
                                        messageTextBody.put("messageID", messagePushID);
                                        messageTextBody.put("time", saveCurrentTime);
                                        messageTextBody.put("date", saveCurrentDate);
                                        messageTextBody.put("isseen", false);

                                        Map messageBodyDetails = new HashMap();
                                        messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
                                        messageBodyDetails.put( messageReceiverRef + "/" + messagePushID, messageTextBody);

                                        RootRef.updateChildren(messageBodyDetails);
                                        loadingBar.dismiss();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        loadingBar.dismiss();
                                        Toast.makeText(ChatActivity.this,e.getMessage(),Toast.LENGTH_LONG);
                                    }
                                });
                            }


                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                loadingBar.dismiss();
                                Toast.makeText(ChatActivity.this,e.getMessage(),Toast.LENGTH_LONG);
                            }
                        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                double d = (100.0*taskSnapshot.getBytesTransferred())/taskSnapshot.getTotalByteCount();
                                loadingBar.setMessage((int)d+"% Uploading...");
                            }
                        });


                    }
                }
            }
        });


        messageAdapter = new MessageAdapter(messagesList);
        userMessagesList = (RecyclerView) findViewById(R.id.private_messages_list_of_users);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(messageAdapter);


        loadingBar = new ProgressDialog(this,R.style.RoundedDialog);

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());
    }

    public boolean writeToFileFromContentUri(File file, Uri uri) {
        if (file == null || uri == null) return false;
        try {
            InputStream stream = getContentResolver().openInputStream(uri);
            OutputStream output = new FileOutputStream(file);
            if (stream == null) return false;
            byte[] buffer = new byte[4 * 1024];
            int read;
            while ((read = stream.read(buffer)) != -1) output.write(buffer, 0, read);
            output.flush();
            output.close();
            stream.close();
            return true;
        } catch (FileNotFoundException e) {
            Log.e("CV", "Couldn't open stream: " + e.getMessage());
        } catch (IOException e) {
            Log.e("CV", "IOException on stream: " + e.getMessage());
        }
        return false;
    }

    private void locationPlacesIntent(){


        MapUtility.apiKey = getResources().getString(R.string.map_api_key);
        Intent i = new Intent(ChatActivity.this, LocationPickerActivity.class);
        startActivityForResult(i, ADDRESS_PICKER_REQUEST);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==438 && resultCode == RESULT_OK &&  data!=null && data.getData() !=null){

            loadingBar.setTitle("Seding File.");
            loadingBar.setMessage("Please wait, we are sending...");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            fileUri = data.getData();

            if(!checker.equals("image")){
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Document Files");

                final String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
                final String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

                DatabaseReference userMessageKeyRef = RootRef.child("Messages")
                        .child(messageSenderID).child(messageReceiverID).push();

                final  String messagePushID = userMessageKeyRef.getKey();

                final StorageReference filePath = storageReference.child(messagePushID+"."+checker);

                final UploadTask uploadTask = filePath.putFile(fileUri);

                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String download_url = uri.toString();
                                Map messageTextBody = new HashMap();
                                messageTextBody.put("message", download_url);
                                messageTextBody.put("name", fileUri.getLastPathSegment());
                                messageTextBody.put("lat", "0");
                                messageTextBody.put("lon", "0");
                                messageTextBody.put("title", "");
                                messageTextBody.put("des", "");
                                messageTextBody.put("img", "");
                                messageTextBody.put("type", checker);
                                messageTextBody.put("from", messageSenderID);
                                messageTextBody.put("to", messageReceiverID);
                                messageTextBody.put("messageID", messagePushID);
                                messageTextBody.put("time", saveCurrentTime);
                                messageTextBody.put("date", saveCurrentDate);
                                messageTextBody.put("isseen", false);

                                Map messageBodyDetails = new HashMap();
                                messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
                                messageBodyDetails.put( messageReceiverRef + "/" + messagePushID, messageTextBody);

                                RootRef.updateChildren(messageBodyDetails);
                                loadingBar.dismiss();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                loadingBar.dismiss();
                                Toast.makeText(ChatActivity.this,e.getMessage(),Toast.LENGTH_LONG);
                            }
                        });
                    }


                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        loadingBar.dismiss();
                        Toast.makeText(ChatActivity.this,e.getMessage(),Toast.LENGTH_LONG);
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double d = (100.0*taskSnapshot.getBytesTransferred())/taskSnapshot.getTotalByteCount();
                        loadingBar.setMessage((int)d+"% Uploading...");
                    }
                });

            }
            else if(checker.equals("image"))
            {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Image Files");

                final String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
                final String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

                DatabaseReference userMessageKeyRef = RootRef.child("Messages")
                        .child(messageSenderID).child(messageReceiverID).push();

                final  String messagePushID = userMessageKeyRef.getKey();

                final StorageReference filePath = storageReference.child(messagePushID+"."+"jpg");


                uploadTask = filePath.putFile(fileUri);
                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                myurl = uri.toString();
                                Map messageTextBody = new HashMap();
                                messageTextBody.put("message", myurl);
                                messageTextBody.put("name", fileUri.getLastPathSegment());
                                messageTextBody.put("lat", "0");
                                messageTextBody.put("lon", "0");
                                messageTextBody.put("title", "");
                                messageTextBody.put("des", "");
                                messageTextBody.put("img", "");
                                messageTextBody.put("type", checker);
                                messageTextBody.put("from", messageSenderID);
                                messageTextBody.put("to", messageReceiverID);
                                messageTextBody.put("messageID", messagePushID);
                                messageTextBody.put("time", saveCurrentTime);
                                messageTextBody.put("date", saveCurrentDate);
                                messageTextBody.put("isseen", false);

                                Map messageBodyDetails = new HashMap();
                                messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
                                messageBodyDetails.put( messageReceiverRef + "/" + messagePushID, messageTextBody);

                                RootRef.updateChildren(messageBodyDetails);
                                loadingBar.dismiss();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                loadingBar.dismiss();
                                Toast.makeText(ChatActivity.this,e.getMessage(),Toast.LENGTH_LONG);
                            }
                        });
                    }


                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        loadingBar.dismiss();
                        Toast.makeText(ChatActivity.this,e.getMessage(),Toast.LENGTH_LONG);
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double d = (100.0*taskSnapshot.getBytesTransferred())/taskSnapshot.getTotalByteCount();
                        loadingBar.setMessage((int)d+"% Uploading...");
                    }
                });



            }
            else{
                loadingBar.dismiss();
                Toast.makeText(ChatActivity.this,"Nothing Selected.",Toast.LENGTH_LONG);
            }
        }

        else if(requestCode==ADDRESS_PICKER_REQUEST ){

            if( resultCode == RESULT_OK)
            {

                try {
                    if (data != null && data.getStringExtra(MapUtility.ADDRESS) != null) {
                        String address = data.getStringExtra(MapUtility.ADDRESS);
                        String currentLatitude = Double.toString(data.getDoubleExtra(MapUtility.LATITUDE, 0.0));
                        String currentLongitude = Double.toString(data.getDoubleExtra(MapUtility.LONGITUDE, 0.0));

                        SendLocation(new MapModel(currentLatitude,currentLongitude),address);

                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }

        }
    }

    private void DisplayLastSeen()
    {
        RootRef.child("Users").child(messageReceiverID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        if (dataSnapshot.child("userState").hasChild("state"))
                        {
                            String state = dataSnapshot.child("userState").child("state").getValue().toString();
                            String date = dataSnapshot.child("userState").child("date").getValue().toString();
                            String time = dataSnapshot.child("userState").child("time").getValue().toString();

                            if (state.equals("online"))
                            {
                                userLastSeen.setText("online");
                            }
                            else if (state.equals("offline"))
                            {
                                userLastSeen.setText("Last Seen: " + date + " " + time);
                            }
                        }
                        else
                        {
                            userLastSeen.setText("offline");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    public  boolean findReceiver(){

        final boolean[] stated = {false};
        RootRef.child("Users").child(messageReceiverID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        if (dataSnapshot.child("userState").hasChild("state"))
                        {
                            String state = dataSnapshot.child("userState").child("state").getValue().toString();
                            String date = dataSnapshot.child("userState").child("date").getValue().toString();
                            String time = dataSnapshot.child("userState").child("time").getValue().toString();

                            if (state.equals("online"))
                            {
                                stated[0] = true;
                            }
                            else if (state.equals("offline"))
                            {
                                stated[0] = false;
                            }
                        }
                        else
                        {
                            userLastSeen.setText("offline");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
        return stated[0];
    }


    @Override
    protected void onPause() {
        super.onPause();

        messageAdapter.stopplay();

        RootRef.removeEventListener(seenListener);
        updateUserStatus("offline");



    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUserStatus("online");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //updateUserStatus("offline");

    }
    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onStart()
    {
        super.onStart();


        messagesList.clear();

        messageAdapter.notifyDataSetChanged();

        RootRef.child("Messages").child(messageSenderID).child(messageReceiverID)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s)
                    {
                        Messages messages = dataSnapshot.getValue(Messages.class);

                        messagesList.add(messages);

                        messageAdapter.notifyDataSetChanged();

                        userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    public  void SeenMessage(){
        System.out.println("called");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Messages").child(messageReceiverID).child(messageSenderID);
        seenListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    if (snapshot.getValue() != null){
                        Messages messages = snapshot.getValue(Messages.class);
                        if((messages.getTo().equals(messageReceiverID) && messages.getFrom().equals(messageSenderID) )
                                ||
                                (messages.getTo().equals(messageSenderID) && messages.getFrom().equals(messageReceiverID)  )){
                            HashMap<String,Object> hashMap = new HashMap<>();
                            hashMap.put("isseen",true);
                            snapshot.getRef().updateChildren(hashMap);
                            update = true;

                            RootRef.child("Messages").child(messageSenderID).child(messageReceiverID)
                                    .addChildEventListener(new ChildEventListener() {
                                        @Override
                                        public void onChildAdded(DataSnapshot dataSnapshot, String s)
                                        {

                                        }

                                        @Override
                                        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                                            Messages messages = dataSnapshot.getValue(Messages.class);

                                            if(update) {
                                                System.out.println("dd");
                                                messagesList.remove(messagesList.size()-1);
                                                messagesList.add(messages);
                                                messageAdapter.notifyDataSetChanged();
                                                userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());
                                                update = false;
                                            }
                                        }

                                        @Override
                                        public void onChildRemoved(DataSnapshot dataSnapshot) {

                                        }

                                        @Override
                                        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                        }
                    }
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });






    }

    private void SendLocation(MapModel mapModel,CharSequence address){
        final String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
        final String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

        DatabaseReference userMessageKeyRef = RootRef.child("Messages")
                .child(messageSenderID).child(messageReceiverID).push();

        final  String messagePushID = userMessageKeyRef.getKey();


        Map messageTextBody = new HashMap();
        messageTextBody.put("message", address.toString());
        messageTextBody.put("name", "location");
        messageTextBody.put("lat", mapModel.getLatitude());
        messageTextBody.put("lon", mapModel.getLongitude());
        messageTextBody.put("title", "");
        messageTextBody.put("des", "");
        messageTextBody.put("img", "");
        messageTextBody.put("type", "location");
        messageTextBody.put("from", messageSenderID);
        messageTextBody.put("to", messageReceiverID);
        messageTextBody.put("messageID", messagePushID);
        messageTextBody.put("time", saveCurrentTime);
        messageTextBody.put("date", saveCurrentDate);
        messageTextBody.put("isseen", false);

        Map messageBodyDetails = new HashMap();
        messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
        messageBodyDetails.put( messageReceiverRef + "/" + messagePushID, messageTextBody);

        RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task)
            {
                if (task.isSuccessful())
                {
                    Toast.makeText(ChatActivity.this, "Message Sent Successfully...", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void SendAudio(Uri audioUri){
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Document Files");

        final String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
        final String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

        DatabaseReference userMessageKeyRef = RootRef.child("Messages")
                .child(messageSenderID).child(messageReceiverID).push();

        final  String messagePushID = userMessageKeyRef.getKey();

        final StorageReference filePath = storageReference.child(messagePushID+"."+checker);

        final UploadTask uploadTask = filePath.putFile(audioUri);

        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String download_url = uri.toString();
                        Map messageTextBody = new HashMap();
                        messageTextBody.put("message", download_url);
                        messageTextBody.put("name", audioUri.getLastPathSegment());
                        messageTextBody.put("lat", "0");
                        messageTextBody.put("lon", "0");
                        messageTextBody.put("title", "");
                        messageTextBody.put("des", "");
                        messageTextBody.put("img", "");
                        messageTextBody.put("type", "mp3");
                        messageTextBody.put("from", messageSenderID);
                        messageTextBody.put("to", messageReceiverID);
                        messageTextBody.put("messageID", messagePushID);
                        messageTextBody.put("time", saveCurrentTime);
                        messageTextBody.put("date", saveCurrentDate);
                        messageTextBody.put("isseen", false);

                        Map messageBodyDetails = new HashMap();
                        messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
                        messageBodyDetails.put( messageReceiverRef + "/" + messagePushID, messageTextBody);

                        RootRef.updateChildren(messageBodyDetails);
                        loadingBar.dismiss();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        loadingBar.dismiss();
                        Toast.makeText(ChatActivity.this,e.getMessage(),Toast.LENGTH_LONG);
                    }
                });
            }


        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                loadingBar.dismiss();
                Toast.makeText(ChatActivity.this,e.getMessage(),Toast.LENGTH_LONG);
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double d = (100.0*taskSnapshot.getBytesTransferred())/taskSnapshot.getTotalByteCount();
                loadingBar.setMessage((int)d+"% Uploading...");
            }
        });
    }


    private void SendLike()
    {

        final String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
        final String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

        DatabaseReference userMessageKeyRef = RootRef.child("Messages")
                .child(messageSenderID).child(messageReceiverID).push();

        final  String messagePushID = userMessageKeyRef.getKey();


        Map messageTextBody = new HashMap();
        messageTextBody.put("message", "");
        messageTextBody.put("name", "like");
        messageTextBody.put("lat", "0");
        messageTextBody.put("lon", "0");
        messageTextBody.put("title", "");
        messageTextBody.put("des", "");
        messageTextBody.put("img", "");
        messageTextBody.put("type", "emoji");
        messageTextBody.put("from", messageSenderID);
        messageTextBody.put("to", messageReceiverID);
        messageTextBody.put("messageID", messagePushID);
        messageTextBody.put("time", saveCurrentTime);
        messageTextBody.put("date", saveCurrentDate);
        messageTextBody.put("isseen", false);

        Map messageBodyDetails = new HashMap();
        messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
        messageBodyDetails.put( messageReceiverRef + "/" + messagePushID, messageTextBody);

        RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task)
            {
                if (task.isSuccessful())
                {
                    Toast.makeText(ChatActivity.this, "Message Sent Successfully...", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                }
            }
        });





    }
    public static String[] extractLinks(String text) {
        List<String> links = new ArrayList<String>();
        Matcher m = Patterns.WEB_URL.matcher(text);
        while (m.find()) {
            String url = m.group();
            Log.d("URL", "URL extracted: " + url);
            links.add(url);
        }

        return links.toArray(new String[links.size()]);
    }

    private void SendMessage()
    {
        String messageText = MessageInputText.getText().toString();

        if (TextUtils.isEmpty(messageText))
        {
            Toast.makeText(this, "first write your message...", Toast.LENGTH_SHORT).show();
        }
        else
        {
            String[] output = extractLinks(messageText);

            for (String url:
                    output) {
                System.out.println(url);
            }

            if(output.length!=0) {
                new TextCrawler().makePreview(new LinkPreviewCallback() {
                    @Override
                    public void onPre() {

                    }

                    @Override
                    public void onPos(SourceContent sourceContent, boolean b) {
                        if (b || sourceContent.getFinalUrl().equals("")) {

                        } else {

                            if (sourceContent.getTitle().equals(""))
                                sourceContent.setTitle("");
                            if (sourceContent.getDescription().equals(""))
                                sourceContent
                                        .setDescription("");


                            String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
                            String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

                            DatabaseReference userMessageKeyRef = RootRef.child("Messages")
                                    .child(messageSenderID).child(messageReceiverID).push();

                            String messagePushID = userMessageKeyRef.getKey();

                            Map messageTextBody = new HashMap();
                            messageTextBody.put("message", messageText);
                            messageTextBody.put("type", "link");
                            messageTextBody.put("name", "link");
                            messageTextBody.put("lat", "0");
                            messageTextBody.put("lon", "0");
                            messageTextBody.put("title", sourceContent.getTitle());
                            messageTextBody.put("des", sourceContent.getDescription());
                            messageTextBody.put("img", sourceContent.getImages().get(0));
                            messageTextBody.put("from", messageSenderID);
                            messageTextBody.put("to", messageReceiverID);
                            messageTextBody.put("messageID", messagePushID);
                            messageTextBody.put("time", saveCurrentTime);
                            messageTextBody.put("date", saveCurrentDate);
                            messageTextBody.put("isseen", false);

                            Map messageBodyDetails = new HashMap();
                            messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
                            messageBodyDetails.put(messageReceiverRef + "/" + messagePushID, messageTextBody);

                            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(ChatActivity.this, "Message Sent Successfully...", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                    }
                                    MessageInputText.setText("");
                                }
                            });


                        }
                    }
                }, output[0]);

            }else{
                String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
                String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

                DatabaseReference userMessageKeyRef = RootRef.child("Messages")
                        .child(messageSenderID).child(messageReceiverID).push();

                String messagePushID = userMessageKeyRef.getKey();

                Map messageTextBody = new HashMap();
                messageTextBody.put("message", messageText);
                messageTextBody.put("type", "text");
                messageTextBody.put("name", "text");
                messageTextBody.put("lat", "0");
                messageTextBody.put("lon", "0");
                messageTextBody.put("title", "");
                messageTextBody.put("des", "");
                messageTextBody.put("img", "");
                messageTextBody.put("from", messageSenderID);
                messageTextBody.put("to", messageReceiverID);
                messageTextBody.put("messageID", messagePushID);
                messageTextBody.put("time", saveCurrentTime);
                messageTextBody.put("date", saveCurrentDate);
                messageTextBody.put("isseen", false);

                Map messageBodyDetails = new HashMap();
                messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
                messageBodyDetails.put( messageReceiverRef + "/" + messagePushID, messageTextBody);

                RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task)
                    {
                        if (task.isSuccessful())
                        {
                            Toast.makeText(ChatActivity.this, "Message Sent Successfully...", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                        }
                        MessageInputText.setText("");
                    }
                });
            }

        }



        SendChatRequest(messageText);





    }

    private void SendChatRequest(String message)
    {

        HashMap<String, String> chatNotificationMap = new HashMap<>();
        chatNotificationMap.put("from", messageSenderID);
        chatNotificationMap.put("to", messageReceiverID);
        chatNotificationMap.put("type", message );

        System.out.println("vcvcvcvcv");

        NotificationRef.child(messageReceiverID).push()
                .setValue(chatNotificationMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            System.out.println("done");
                        }
                    }
                });

    }



    @Override
    public void onBackPressed() {
        super.onBackPressed();

        messageAdapter.stopplay();
        updateUserStatus("online");

        finish();


    }

    private void updateUserStatus(String state)
    {
        String saveCurrentTime, saveCurrentDate;

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());

        HashMap<String, Object> onlineStateMap = new HashMap<>();
        onlineStateMap.put("time", saveCurrentTime);
        onlineStateMap.put("date", saveCurrentDate);
        onlineStateMap.put("state", state);

        RootRef.child("Users").child(messageSenderID).child("userState")
                .updateChildren(onlineStateMap);

    }
}
