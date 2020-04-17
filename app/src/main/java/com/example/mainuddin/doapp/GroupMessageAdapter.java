package com.example.mainuddin.doapp;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mainuddin.doapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.infinityandroid.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

import static android.text.Spanned.SPAN_INCLUSIVE_INCLUSIVE;

public class GroupMessageAdapter extends RecyclerView.Adapter<GroupMessageAdapter.MessageViewHolder>
{
    private List<GroupMessage> userMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    MediaPlayer player = null;
    int length;


    public GroupMessageAdapter (List<GroupMessage> userMessagesList)
    {
        this.userMessagesList = userMessagesList;
    }



    public class MessageViewHolder extends RecyclerView.ViewHolder
    {
        public TextView senderMessageText, receiverMessageText;
        public CircleImageView receiverProfileImage;
        public RoundedImageView messageSenderPicture, messageReceiverPicture,likeS,likeR;
        public RelativeLayout audioIn,audioOut;
        TextView dateS,dateR;
        ImageButton playS,playR;
        SeekBar seekS,seekR;
        TextView audioTS,audioTR;
        TextView sender_seen,sender_img_seen,sender_m_seen;



        public MessageViewHolder(@NonNull View itemView)
        {
            super(itemView);

            senderMessageText = (TextView) itemView.findViewById(R.id.sender_messsage_text);
            receiverMessageText = (TextView) itemView.findViewById(R.id.receiver_message_text);
            receiverProfileImage = (CircleImageView) itemView.findViewById(R.id.message_profile_image);
            messageReceiverPicture = itemView.findViewById(R.id.message_receiver_image_view);
            messageSenderPicture = itemView.findViewById(R.id.message_sender_image_view);
            audioIn = itemView.findViewById(R.id.audio_in);
            audioOut = itemView.findViewById(R.id.audio_out);
            dateS = audioOut.findViewById(R.id.date_text);
            dateR = audioIn.findViewById(R.id.date_text);

            playS = audioOut.findViewById(R.id.thumbnail_video_icon);
            playR = audioIn.findViewById(R.id.thumbnail_video_icon);

            seekR = audioIn.findViewById(R.id.progressBar2);
            seekS = audioOut.findViewById(R.id.progressBar2);

            audioTR = audioIn.findViewById(R.id.txt_audio_time);
            audioTS = audioOut.findViewById(R.id.txt_audio_time);

            sender_seen = itemView.findViewById(R.id.sender_messsage_seen);
            sender_img_seen = itemView.findViewById(R.id.sender_img_seen);
            sender_m_seen = itemView.findViewById(R.id.sender_m_seen);

            likeS = itemView.findViewById(R.id.message_sender_like_view);
            likeR = itemView.findViewById(R.id.message_receiver_like_view);


        }
    }




    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
    {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.custom_messages_layout, viewGroup, false);

        mAuth = FirebaseAuth.getInstance();

        return new MessageViewHolder(view);
    }



    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder messageViewHolder, final int i)
    {
        String messageSenderId = mAuth.getCurrentUser().getUid();
        final GroupMessage messages = userMessagesList.get(i);

        String fromUserID = messages.getFrom();
        String fromMessageType = messages.getType();

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.hasChild("image"))
                {
                    String receiverImage = dataSnapshot.child("image").getValue().toString();

                    Picasso.get().load(receiverImage).placeholder(R.drawable.profile_image).into(messageViewHolder.receiverProfileImage);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });





        messageViewHolder.receiverMessageText.setVisibility(View.GONE);
        messageViewHolder.receiverProfileImage.setVisibility(View.GONE);
        messageViewHolder.senderMessageText.setVisibility(View.GONE);
        messageViewHolder.messageSenderPicture.setVisibility(View.GONE);
        messageViewHolder.messageReceiverPicture.setVisibility(View.GONE);
        messageViewHolder.audioIn.setVisibility(View.GONE);
        messageViewHolder.audioOut.setVisibility(View.GONE);
        messageViewHolder.likeS.setVisibility(View.GONE);
        messageViewHolder.likeR.setVisibility(View.GONE);
        messageViewHolder.sender_seen.setVisibility(View.GONE);
        messageViewHolder.sender_img_seen.setVisibility(View.GONE);
        messageViewHolder.sender_m_seen.setVisibility(View.GONE);


        if (fromMessageType.equals("text")) {
            if (fromUserID.equals(messageSenderId))
            {

                messageViewHolder.senderMessageText.setVisibility(View.VISIBLE);

                messageViewHolder.senderMessageText.setBackgroundResource(R.drawable.sender_messages_layout);
                messageViewHolder.senderMessageText.setTextColor(Color.WHITE);

                String text= messages.getName() + "\n\n" + messages.getMessage() + "\n\n"+ messages.getTime() + " - " + messages.getDate();
                String oth = messages.getTime() + " - " + messages.getDate();
                SpannableStringBuilder ssb = new SpannableStringBuilder(text);

                ForegroundColorSpan fcsRed = new ForegroundColorSpan(Color.parseColor("#34495E"));
                AbsoluteSizeSpan fcsGreen = new AbsoluteSizeSpan(20,true);
                AbsoluteSizeSpan bcsYellow =new AbsoluteSizeSpan(10,true);

                ssb.setSpan(fcsRed, 0, messages.getName().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                ssb.setSpan(fcsGreen, messages.getName().length()+2, messages.getName().length()+2+messages.getMessage().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                ssb.setSpan(bcsYellow, messages.getName().length()+2+messages.getMessage().length()+2, messages.getName().length()+2+messages.getMessage().length()+2+oth.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);



                messageViewHolder.senderMessageText.setText(ssb);

                messageViewHolder.senderMessageText.setLinkTextColor(Color.parseColor("#A3CEF7"));

                Linkify.addLinks(messageViewHolder.senderMessageText, Linkify.WEB_URLS | Linkify.PHONE_NUMBERS);
                Linkify.addLinks(messageViewHolder.senderMessageText, Linkify.ALL );
            }
            else
            {
                messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                messageViewHolder.receiverMessageText.setVisibility(View.VISIBLE);

                messageViewHolder.receiverMessageText.setBackgroundResource(R.drawable.receiver_messages_layout);
                messageViewHolder.receiverMessageText.setTextColor(Color.BLACK);



                String text= messages.getName() + "\n\n" + messages.getMessage() + "\n\n"+ messages.getTime() + " - " + messages.getDate();
                String oth = messages.getTime() + " - " + messages.getDate();
                SpannableStringBuilder ssb = new SpannableStringBuilder(text);

                ForegroundColorSpan fcsRed = new ForegroundColorSpan(Color.parseColor("#12B6F3"));
                AbsoluteSizeSpan fcsGreen = new AbsoluteSizeSpan(20,true);
                AbsoluteSizeSpan bcsYellow =new AbsoluteSizeSpan(10,true);

                ssb.setSpan(fcsRed, 0, messages.getName().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                ssb.setSpan(fcsGreen, messages.getName().length()+2, messages.getName().length()+2+messages.getMessage().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                ssb.setSpan(bcsYellow, messages.getName().length()+2+messages.getMessage().length()+2, messages.getName().length()+2+messages.getMessage().length()+2+oth.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);



                messageViewHolder.receiverMessageText.setText(ssb);


                messageViewHolder.receiverMessageText.setLinkTextColor(Color.parseColor("#2f6699"));

                Linkify.addLinks(messageViewHolder.receiverMessageText, Linkify.WEB_URLS | Linkify.PHONE_NUMBERS);
                Linkify.addLinks(messageViewHolder.receiverMessageText, Linkify.ALL );

            }





        }





    }




    @Override
    public int getItemCount()
    {
        return userMessagesList.size();
    }
    private void downloadFileq(Context context,String url,int pos) {


        String file_path = Environment.getExternalStorageDirectory().getAbsolutePath()  +
                "/ChatApp";
        File dir = new File(file_path);


        if(!dir.exists()) {
            dir.mkdirs();
        }

        if(!isFileExists(userMessagesList.get(pos).getName())){

            downloadFile(context,userMessagesList.get(pos).getName(),".mp3",file_path,url);
        }else{


        }







    }
    public void stopplay(){
        if(player!=null ) {
            if(player.isPlaying())
                player.stop();
        }
    }
    private boolean isFileExists(String filename){

        File folder1 = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +"/ChatApp/" + filename.replaceAll(":", "_")+".mp3");
        return folder1.exists();


    }

    public long downloadFile(Context context, String fileName, String fileExtension, String destinationDirectory, String url) {



        DownloadManager downloadmanager = (DownloadManager) context.
                getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(uri);

        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir("/ChatApp",fileName.replaceAll(":", "_")+fileExtension);

        return downloadmanager.enqueue(request);
    }



}