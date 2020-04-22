package com.example.mainuddin.doapp;

import android.app.Activity;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
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
import java.io.InputStream;
import java.net.HttpURLConnection;
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

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static android.text.Spanned.SPAN_INCLUSIVE_INCLUSIVE;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>
{
    private List<Messages> userMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    MediaPlayer player = null;
    int length;


    public MessageAdapter (List<Messages> userMessagesList)
    {
        this.userMessagesList = userMessagesList;
    }



    public class MessageViewHolder extends RecyclerView.ViewHolder
    {
        public TextView senderMessageText, receiverMessageText;
        public CircleImageView receiverProfileImage;
        public RoundedImageView messageSenderPicture, messageReceiverPicture,likeS,likeR;
        public RelativeLayout audioIn,audioOut,mapIn,mapOut;
        public RelativeLayout fileIn,fileOut;
        TextView dateS,dateR;
        ImageButton playS,playR;
        SeekBar seekS,seekR;
        TextView audioTS,audioTR;
        TextView sender_seen,sender_img_seen,sender_m_seen,sender_file_seen,sender_loc_seen;
        ImageView fileS,fileR;
        RoundedImageView mapS,mapR;
        TextView fileST,fileRT,locS,locR,timeS,timeR;



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

            mapIn = itemView.findViewById(R.id.map_in);
            mapOut = itemView.findViewById(R.id.map_out);

            fileIn = itemView.findViewById(R.id.file_in);
            fileOut = itemView.findViewById(R.id.file_out);

            dateS = audioOut.findViewById(R.id.date_text);
            dateR = audioIn.findViewById(R.id.date_text);

            playS = audioOut.findViewById(R.id.thumbnail_video_icon);
            playR = audioIn.findViewById(R.id.thumbnail_video_icon);

            seekR = audioIn.findViewById(R.id.progressBar2);
            seekS = audioOut.findViewById(R.id.progressBar2);

            audioTR = audioIn.findViewById(R.id.txt_audio_time);
            audioTS = audioOut.findViewById(R.id.txt_audio_time);

            fileS = fileOut.findViewById(R.id.thumbnail_file_icon);
            fileR = fileIn.findViewById(R.id.thumbnail_file_icon);

            locS = mapOut.findViewById(R.id.location);
            locR = mapIn.findViewById(R.id.location);

            mapS = mapOut.findViewById(R.id.map);
            mapR = mapIn.findViewById(R.id.map);

            timeS = mapOut.findViewById(R.id.time);
            timeR = mapIn.findViewById(R.id.time);

            fileST = fileOut.findViewById(R.id.sender_file_text);
            fileRT = fileIn.findViewById(R.id.receiver_file_text);

            sender_seen = itemView.findViewById(R.id.sender_messsage_seen);
            sender_img_seen = itemView.findViewById(R.id.sender_img_seen);
            sender_m_seen = itemView.findViewById(R.id.sender_m_seen);
            sender_file_seen = itemView.findViewById(R.id.sender_file_seen);
            sender_loc_seen = itemView.findViewById(R.id.sender_loc_seen);

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
        final Messages messages = userMessagesList.get(i);

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
        messageViewHolder.mapIn.setVisibility(View.GONE);
        messageViewHolder.mapOut.setVisibility(View.GONE);
        messageViewHolder.fileIn.setVisibility(View.GONE);
        messageViewHolder.fileOut.setVisibility(View.GONE);
        messageViewHolder.likeS.setVisibility(View.GONE);
        messageViewHolder.likeR.setVisibility(View.GONE);
        messageViewHolder.sender_seen.setVisibility(View.GONE);
        messageViewHolder.sender_img_seen.setVisibility(View.GONE);
        messageViewHolder.sender_m_seen.setVisibility(View.GONE);
        messageViewHolder.sender_file_seen.setVisibility(View.GONE);
        messageViewHolder.sender_loc_seen.setVisibility(View.GONE);


        if (fromMessageType.equals("text")) {
            if (fromUserID.equals(messageSenderId))
            {

                messageViewHolder.sender_m_seen.setVisibility(View.VISIBLE);
                if(messages.isIsseen() )
                    {
                        messageViewHolder.sender_m_seen.setText("Seen");
                    }
                    else
                    {
                        messageViewHolder.sender_m_seen.setText("Delivered");

                    }



                messageViewHolder.senderMessageText.setVisibility(View.VISIBLE);

                messageViewHolder.senderMessageText.setBackgroundResource(R.drawable.sender_messages_layout);
                messageViewHolder.senderMessageText.setTextColor(Color.WHITE);

                SpannableString ss1=  new SpannableString(messages.getMessage());
                ss1.setSpan(new AbsoluteSizeSpan(20,true), 0, messages.getMessage().length(), SPAN_INCLUSIVE_INCLUSIVE); // set size

                SpannableString ss2=  new SpannableString(messages.getTime() + " - " + messages.getDate());
                ss2.setSpan(new AbsoluteSizeSpan(10,true), 0, ss2.length(), SPAN_INCLUSIVE_INCLUSIVE); // set size

                CharSequence finalText = TextUtils.concat(ss1, "\n \n" , ss2);

                messageViewHolder.senderMessageText.setText(finalText);

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

                SpannableString ss1=  new SpannableString(messages.getMessage());
                ss1.setSpan(new AbsoluteSizeSpan(20,true), 0, messages.getMessage().length(), SPAN_INCLUSIVE_INCLUSIVE); // set size

                SpannableString ss2=  new SpannableString(messages.getTime() + " - " + messages.getDate());
                ss2.setSpan(new AbsoluteSizeSpan(10,true), 0, ss2.length(), SPAN_INCLUSIVE_INCLUSIVE); // set size

                CharSequence finalText = TextUtils.concat(ss1, "\n \n" , ss2);

                messageViewHolder.receiverMessageText.setText(finalText);
                messageViewHolder.receiverMessageText.setLinkTextColor(Color.parseColor("#2f6699"));

                Linkify.addLinks(messageViewHolder.receiverMessageText, Linkify.WEB_URLS | Linkify.PHONE_NUMBERS);
                Linkify.addLinks(messageViewHolder.receiverMessageText, Linkify.ALL );

            }





        }
        else if(fromMessageType.equals("emoji")) {
            if(fromUserID.equals(messageSenderId))
            {

                messageViewHolder.sender_seen.setVisibility(View.VISIBLE);
                if(messages.isIsseen() )
                    {
                        messageViewHolder.sender_seen.setText("Seen");
                    }
                    else
                    {
                        messageViewHolder.sender_seen.setText("Delivered");
                    }


                messageViewHolder.likeS.setVisibility(View.VISIBLE);
            }else
            {

                messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                messageViewHolder.likeR.setVisibility(View.VISIBLE);

            }
        }
        else if(fromMessageType.equals("location")){
            if(fromUserID.equals(messageSenderId))
            {

                messageViewHolder.sender_loc_seen.setVisibility(View.VISIBLE);
                if(messages.isIsseen() )
                {
                    messageViewHolder.sender_loc_seen.setText("Seen");
                }
                else
                {
                    messageViewHolder.sender_loc_seen.setText("Delivered");
                }


                messageViewHolder.mapOut.setVisibility(View.VISIBLE);
                String URL = "https://api.mapbox.com/styles/v1/mapbox/streets-v10/static/pin-m(" + messages.getLon() +","+messages.getLat() +")/" + messages.getLon()+","+messages.getLat()+",15/400x300?access_token=pk.eyJ1IjoibWFpbnU3MyIsImEiOiJjazlhNGtqMmEwNDN1M25udnAyNjZ5dXJkIn0.ZRZ1T8LVImkeXmieZDYqUg";
                Picasso.get().load(URL).into(messageViewHolder.mapS);
                System.out.println(URL);
                //messageViewHolder.mapS.setImageBitmap(getBitmapFromURL(URL));

                messageViewHolder.locS.setText(messages.getMessage());

                SpannableString ss2=  new SpannableString(messages.getTime() + " - " + messages.getDate());
                ss2.setSpan(new AbsoluteSizeSpan(10,true), 0, ss2.length(), SPAN_INCLUSIVE_INCLUSIVE);
                messageViewHolder.timeS.setText(ss2);

                messageViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                });


            }else
            {

                messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);

                messageViewHolder.mapIn.setVisibility(View.VISIBLE);
                String URL = "https://api.mapbox.com/styles/v1/mapbox/streets-v10/static/pin-m(" + messages.getLon() +","+messages.getLat() +")/" + messages.getLon()+","+messages.getLat()+",15/400x300?access_token=pk.eyJ1IjoibWFpbnU3MyIsImEiOiJjazlhNGtqMmEwNDN1M25udnAyNjZ5dXJkIn0.ZRZ1T8LVImkeXmieZDYqUg";
                Picasso.get().load(URL).into(messageViewHolder.mapR);

                //messageViewHolder.mapR.setImageBitmap(getBitmapFromURL(URL));
                messageViewHolder.locR.setText(messages.getMessage());

                SpannableString ss2=  new SpannableString(messages.getTime() + " - " + messages.getDate());
                ss2.setSpan(new AbsoluteSizeSpan(10,true), 0, ss2.length(), SPAN_INCLUSIVE_INCLUSIVE);
                messageViewHolder.timeR.setText(ss2);

                messageViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                });


            }

        }
        else if (fromMessageType.equals("image")){
            if(fromUserID.equals(messageSenderId))
            {

                messageViewHolder.sender_img_seen.setVisibility(View.VISIBLE);
                if(messages.isIsseen() )
                    {
                        messageViewHolder.sender_img_seen.setText("Seen");
                    }
                    else
                    {
                        messageViewHolder.sender_img_seen.setText("Delivered");
                    }


                messageViewHolder.messageSenderPicture.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getMessage()).into(messageViewHolder.messageSenderPicture);

                messageViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Dialog builder = new Dialog(messageViewHolder.itemView.getContext(),R.style.RoundedDialog);
                        builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        builder.getWindow().setBackgroundDrawable(
                                new ColorDrawable(android.graphics.Color.TRANSPARENT));
                        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialogInterface) {
                                //nothing;
                            }
                        });

                        ImageView imageView = new ImageView(messageViewHolder.itemView.getContext());
                        Picasso.get().load(messages.getMessage()).into(imageView);
                        builder.addContentView(imageView, new RelativeLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT));
                        builder.show();
                    }
                });
            }else
            {

                messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                messageViewHolder.messageReceiverPicture.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getMessage()).into(messageViewHolder.messageReceiverPicture);

                messageViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Dialog builder = new Dialog(messageViewHolder.itemView.getContext(),R.style.RoundedDialog);
                        builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        builder.getWindow().setBackgroundDrawable(
                                new ColorDrawable(android.graphics.Color.TRANSPARENT));
                        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialogInterface) {
                                //nothing;
                            }
                        });

                        ImageView imageView = new ImageView(messageViewHolder.itemView.getContext());
                        Picasso.get().load(messages.getMessage()).into(imageView);
                        builder.addContentView(imageView, new RelativeLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT));
                        builder.show();
                    }
                });


            }
        }
        else if (fromMessageType.equals("pdf")){
            if(fromUserID.equals(messageSenderId))
            {

                messageViewHolder.sender_file_seen.setVisibility(View.VISIBLE);
                if(messages.isIsseen() )
                    {
                        messageViewHolder.sender_file_seen.setText("Seen");
                    }
                    else
                    {
                        messageViewHolder.sender_file_seen.setText("Delivered");
                    }

                messageViewHolder.fileOut.setVisibility(View.VISIBLE);
                messageViewHolder.fileS.setVisibility(View.VISIBLE);
                messageViewHolder.fileS.setBackgroundResource(R.drawable.pdf);

                messageViewHolder.fileST.setTextColor(Color.WHITE);

                String[] arrOfStr = messages.getName().split("/");
                SpannableString ss1=  new SpannableString(arrOfStr[arrOfStr.length-1]);
                ss1.setSpan(new AbsoluteSizeSpan(15,true), 0, ss1.length(), SPAN_INCLUSIVE_INCLUSIVE); // set size

                SpannableString ss2=  new SpannableString(messages.getTime() + " - " + messages.getDate());
                ss2.setSpan(new AbsoluteSizeSpan(10,true), 0, ss2.length(), SPAN_INCLUSIVE_INCLUSIVE); // set size

                CharSequence finalText = TextUtils.concat(ss1, "\n \n" , ss2);

                messageViewHolder.fileST.setText(finalText);

                messageViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(i).getMessage()));
                        messageViewHolder.itemView.getContext().startActivity(intent);
                    }
                });

            }else
            {

                messageViewHolder.fileIn.setVisibility(View.VISIBLE);
                messageViewHolder.fileR.setVisibility(View.VISIBLE);
                messageViewHolder.fileR.setBackgroundResource(R.drawable.pdf);

                messageViewHolder.fileRT.setTextColor(Color.BLACK);

                String[] arrOfStr = messages.getName().split("/");
                SpannableString ss1=  new SpannableString(arrOfStr[arrOfStr.length-1]);
                ss1.setSpan(new AbsoluteSizeSpan(15,true), 0, ss1.length(), SPAN_INCLUSIVE_INCLUSIVE); // set size

                SpannableString ss2=  new SpannableString(messages.getTime() + " - " + messages.getDate());
                ss2.setSpan(new AbsoluteSizeSpan(10,true), 0, ss2.length(), SPAN_INCLUSIVE_INCLUSIVE); // set size

                CharSequence finalText = TextUtils.concat(ss1, "\n \n" , ss2);

                messageViewHolder.fileRT.setText(finalText);

                messageViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(i).getMessage()));
                        messageViewHolder.itemView.getContext().startActivity(intent);
                    }
                });

            }
        }
        else if (fromMessageType.equals("docx")){
            if(fromUserID.equals(messageSenderId))
            {


                messageViewHolder.sender_file_seen.setVisibility(View.VISIBLE);
                if(messages.isIsseen() )
                    {
                        messageViewHolder.sender_file_seen.setText("Seen");
                    }
                    else
                    {
                        messageViewHolder.sender_file_seen.setText("Delivered");
                    }



                messageViewHolder.fileOut.setVisibility(View.VISIBLE);
                messageViewHolder.fileS.setVisibility(View.VISIBLE);
                messageViewHolder.fileS.setBackgroundResource(R.drawable.doc);

                messageViewHolder.fileST.setTextColor(Color.WHITE);

                String[] arrOfStr = messages.getName().split("/");
                SpannableString ss1=  new SpannableString(arrOfStr[arrOfStr.length-1]);
                ss1.setSpan(new AbsoluteSizeSpan(15,true), 0, ss1.length(), SPAN_INCLUSIVE_INCLUSIVE); // set size

                SpannableString ss2=  new SpannableString(messages.getTime() + " - " + messages.getDate());
                ss2.setSpan(new AbsoluteSizeSpan(10,true), 0, ss2.length(), SPAN_INCLUSIVE_INCLUSIVE); // set size

                CharSequence finalText = TextUtils.concat(ss1, "\n \n" , ss2);

                messageViewHolder.fileST.setText(finalText);
                messageViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(i).getMessage()));
                        messageViewHolder.itemView.getContext().startActivity(intent);
                    }
                });

            }else
            {

                messageViewHolder.fileIn.setVisibility(View.VISIBLE);
                messageViewHolder.fileR.setVisibility(View.VISIBLE);
                messageViewHolder.fileR.setBackgroundResource(R.drawable.doc);

                messageViewHolder.fileRT.setTextColor(Color.BLACK);

                String[] arrOfStr = messages.getName().split("/");
                SpannableString ss1=  new SpannableString(arrOfStr[arrOfStr.length-1]);
                ss1.setSpan(new AbsoluteSizeSpan(15,true), 0, ss1.length(), SPAN_INCLUSIVE_INCLUSIVE); // set size

                SpannableString ss2=  new SpannableString(messages.getTime() + " - " + messages.getDate());
                ss2.setSpan(new AbsoluteSizeSpan(10,true), 0, ss2.length(), SPAN_INCLUSIVE_INCLUSIVE); // set size

                CharSequence finalText = TextUtils.concat(ss1, "\n \n" , ss2);

                messageViewHolder.fileRT.setText(finalText);
                messageViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(i).getMessage()));
                        messageViewHolder.itemView.getContext().startActivity(intent);
                    }
                });

            }
        }
        else if (fromMessageType.equals("pptx")){
            if(fromUserID.equals(messageSenderId))
            {
                messageViewHolder.sender_file_seen.setVisibility(View.VISIBLE);

                    if(messages.isIsseen() )
                    {
                        messageViewHolder.sender_file_seen.setText("Seen");
                    }
                    else
                    {
                        messageViewHolder.sender_file_seen.setText("Delivered");
                    }


                messageViewHolder.fileOut.setVisibility(View.VISIBLE);
                messageViewHolder.fileS.setVisibility(View.VISIBLE);
                messageViewHolder.fileS.setBackgroundResource(R.drawable.ppt);

                messageViewHolder.fileST.setTextColor(Color.WHITE);

                String[] arrOfStr = messages.getName().split("/");
                SpannableString ss1=  new SpannableString(arrOfStr[arrOfStr.length-1]);
                ss1.setSpan(new AbsoluteSizeSpan(15,true), 0, ss1.length(), SPAN_INCLUSIVE_INCLUSIVE); // set size

                SpannableString ss2=  new SpannableString(messages.getTime() + " - " + messages.getDate());
                ss2.setSpan(new AbsoluteSizeSpan(10,true), 0, ss2.length(), SPAN_INCLUSIVE_INCLUSIVE); // set size

                CharSequence finalText = TextUtils.concat(ss1, "\n \n" , ss2);

                messageViewHolder.fileST.setText(finalText);

                messageViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(i).getMessage()));
                        messageViewHolder.itemView.getContext().startActivity(intent);
                    }
                });

            }else
            {

                messageViewHolder.fileIn.setVisibility(View.VISIBLE);
                messageViewHolder.fileR.setVisibility(View.VISIBLE);
                messageViewHolder.fileR.setBackgroundResource(R.drawable.ppt);

                messageViewHolder.fileRT.setTextColor(Color.BLACK);

                String[] arrOfStr = messages.getName().split("/");
                SpannableString ss1=  new SpannableString(arrOfStr[arrOfStr.length-1]);
                ss1.setSpan(new AbsoluteSizeSpan(15,true), 0, ss1.length(), SPAN_INCLUSIVE_INCLUSIVE); // set size

                SpannableString ss2=  new SpannableString(messages.getTime() + " - " + messages.getDate());
                ss2.setSpan(new AbsoluteSizeSpan(10,true), 0, ss2.length(), SPAN_INCLUSIVE_INCLUSIVE); // set size

                CharSequence finalText = TextUtils.concat(ss1, "\n \n" , ss2);

                messageViewHolder.fileRT.setText(finalText);

                messageViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(i).getMessage()));
                        messageViewHolder.itemView.getContext().startActivity(intent);
                    }
                });

            }
        }
        else if (fromMessageType.equals("txt")){
            if(fromUserID.equals(messageSenderId))
            {

                messageViewHolder.sender_file_seen.setVisibility(View.VISIBLE);

                    if(messages.isIsseen() )
                    {
                        messageViewHolder.sender_file_seen.setText("Seen");
                    }
                    else
                    {
                        messageViewHolder.sender_file_seen.setText("Delivered");
                    }

                messageViewHolder.fileOut.setVisibility(View.VISIBLE);
                messageViewHolder.fileS.setVisibility(View.VISIBLE);
                messageViewHolder.fileS.setBackgroundResource(R.drawable.txt);

                messageViewHolder.fileST.setTextColor(Color.WHITE);

                String[] arrOfStr = messages.getName().split("/");
                SpannableString ss1=  new SpannableString(arrOfStr[arrOfStr.length-1]);
                ss1.setSpan(new AbsoluteSizeSpan(15,true), 0, messages.getName().length(), SPAN_INCLUSIVE_INCLUSIVE); // set size

                SpannableString ss2=  new SpannableString(messages.getTime() + " - " + messages.getDate());
                ss2.setSpan(new AbsoluteSizeSpan(10,true), 0, ss2.length(), SPAN_INCLUSIVE_INCLUSIVE); // set size

                CharSequence finalText = TextUtils.concat(ss1, "\n \n" , ss2);

                messageViewHolder.fileST.setText(finalText);

                messageViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(i).getMessage()));
                        messageViewHolder.itemView.getContext().startActivity(intent);
                    }
                });

            }else
            {

                messageViewHolder.fileIn.setVisibility(View.VISIBLE);
                messageViewHolder.fileR.setVisibility(View.VISIBLE);
                messageViewHolder.fileR.setBackgroundResource(R.drawable.txt);

                messageViewHolder.fileRT.setTextColor(Color.BLACK);

                String[] arrOfStr = messages.getName().split("/");
                SpannableString ss1=  new SpannableString(arrOfStr[arrOfStr.length-1]);
                ss1.setSpan(new AbsoluteSizeSpan(15,true), 0, messages.getName().length(), SPAN_INCLUSIVE_INCLUSIVE); // set size

                SpannableString ss2=  new SpannableString(messages.getTime() + " - " + messages.getDate());
                ss2.setSpan(new AbsoluteSizeSpan(10,true), 0, ss2.length(), SPAN_INCLUSIVE_INCLUSIVE); // set size

                CharSequence finalText = TextUtils.concat(ss1, "\n \n" , ss2);

                messageViewHolder.fileRT.setText(finalText);

                messageViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(i).getMessage()));
                        messageViewHolder.itemView.getContext().startActivity(intent);
                    }
                });

            }

        }
        else if (fromMessageType.equals("mp3")){
            if(fromUserID.equals(messageSenderId))
            {

                messageViewHolder.sender_seen.setVisibility(View.VISIBLE);

                    if(messages.isIsseen() )
                    {
                        messageViewHolder.sender_seen.setText("Seen");
                    }
                    else
                    {
                        messageViewHolder.sender_seen.setText("Delivered");
                    }


                messageViewHolder.audioOut.setVisibility(View.VISIBLE);

                SpannableString ss2=  new SpannableString(messages.getTime() + " - " + messages.getDate());
                ss2.setSpan(new AbsoluteSizeSpan(10,true), 0, ss2.length(), SPAN_INCLUSIVE_INCLUSIVE);
                messageViewHolder.dateS.setText(ss2);

                messageViewHolder.playS.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {


                        if(isFileExists(userMessagesList.get(i).getName())){
                            if(player==null) {
                                try {
                                     player = new MediaPlayer();
                                    player.setAudioStreamType(AudioManager.STREAM_MUSIC);
                                    player.setDataSource(messageViewHolder.itemView.getContext(), Uri.parse(Environment.getExternalStorageDirectory().getAbsolutePath() +
                                            "/ChatApp/" + userMessagesList.get(i).getName().replaceAll(":", "_")+".mp3"));
                                    player.prepare();
                                    player.start();

                                    String s = String.format("%02d:%02d",
                                            TimeUnit.MILLISECONDS.toMinutes(player.getDuration()),
                                            TimeUnit.MILLISECONDS.toSeconds(player.getDuration()) -
                                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(player.getDuration()))
                                    );
                                    messageViewHolder.audioTS.setText(s);

                                    Timer timer = new Timer();
                                    final Handler handler = new Handler() {
                                        public void handleMessage(Message msg) {

                                                messageViewHolder.playS.setBackgroundResource(R.drawable.ic_play);
                                                messageViewHolder.seekS.setProgress(0);
                                                player = null;
                                                timer.cancel();


                                        }
                                    };



                                    timer.scheduleAtFixedRate(new TimerTask() {
                                        @Override
                                        public void run() {
                                            Integer p = (100 * player.getCurrentPosition()) / player.getDuration();
                                            messageViewHolder.seekS.setProgress(p);
                                            //do stuff like remove view etc
                                            if((player.getDuration()-player.getCurrentPosition())<=0){
                                                Message msg = handler.obtainMessage();
                                                msg.arg1 = player.getCurrentPosition();
                                                handler.sendMessage(msg);

                                            }



                                        }
                                    },0,1000);



                                    messageViewHolder.playS.setBackgroundResource(R.drawable.ic_pause);
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                        messageViewHolder.playS.setBackgroundTintList(ContextCompat.getColorStateList(messageViewHolder.itemView.getContext(),R.color.colorAccent));
                                    }
                                } catch (Exception e) {
                                    // TODO: handle exception
                                }

                            }else{
                                if(player.isPlaying()){
                                    messageViewHolder.playS.setBackgroundResource(R.drawable.ic_play);
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                        messageViewHolder.playS.setBackgroundTintList(ContextCompat.getColorStateList(messageViewHolder.itemView.getContext(),R.color.colorAccent));
                                    }
                                    player.pause();
                                    length=player.getCurrentPosition();

                                }else {
                                    messageViewHolder.playS.setBackgroundResource(R.drawable.ic_pause);
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                        messageViewHolder.playS.setBackgroundTintList(ContextCompat.getColorStateList(messageViewHolder.itemView.getContext(),R.color.colorAccent));
                                    }
                                    player.seekTo(length);
                                    player.start();

                                }
                                messageViewHolder.seekS.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                                    @Override
                                    public void onStopTrackingTouch(SeekBar seekBar) {

                                    }

                                    @Override
                                    public void onStartTrackingTouch(SeekBar seekBar) {

                                    }

                                    @Override
                                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                        if(player != null && fromUser){
                                            player.seekTo(progress*player.getDuration() / 100);
                                            messageViewHolder.seekS.setProgress(progress);
                                        }
                                    }
                                });
                            }
                        }
                        else{
                            downloadFileq(messageViewHolder.itemView.getContext(),userMessagesList.get(i).getMessage(),i);
                        }
                    }
                });

                messageViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                });

            }
            else
            {

                messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                messageViewHolder.audioIn.setVisibility(View.VISIBLE);

                SpannableString ss2=  new SpannableString(messages.getTime() + " - " + messages.getDate());
                ss2.setSpan(new AbsoluteSizeSpan(10,true), 0, ss2.length(), SPAN_INCLUSIVE_INCLUSIVE);
                messageViewHolder.dateR.setText(ss2);

                messageViewHolder.playR.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {


                        if (isFileExists(userMessagesList.get(i).getName())) {
                            if (player == null) {
                                try {
                                     player = new MediaPlayer();
                                    player.setAudioStreamType(AudioManager.STREAM_MUSIC);
                                    player.setDataSource(messageViewHolder.itemView.getContext(), Uri.parse(Environment.getExternalStorageDirectory().getAbsolutePath() +
                                            "/ChatApp/" + userMessagesList.get(i).getName().replaceAll(":", "_")+".mp3"));
                                    player.prepare();
                                    player.start();

                                    String s = String.format("%02d:%02d",
                                            TimeUnit.MILLISECONDS.toMinutes(player.getDuration()),
                                            TimeUnit.MILLISECONDS.toSeconds(player.getDuration()) -
                                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(player.getDuration()))
                                    );
                                    messageViewHolder.audioTR.setText(s);

                                    Timer timer = new Timer();

                                    final Handler handler = new Handler() {
                                        public void handleMessage(Message msg) {
                                                messageViewHolder.playR.setBackgroundResource(R.drawable.ic_play);
                                                messageViewHolder.seekR.setProgress(0);
                                                player = null;
                                                timer.cancel();

                                            }



                                    };



                                    timer.scheduleAtFixedRate(new TimerTask() {
                                        @Override
                                        public void run() {
                                            Integer p = (100 * player.getCurrentPosition()) / player.getDuration();
                                            messageViewHolder.seekR.setProgress(p);
                                            //do stuff like remove view etc

                                            if((player.getDuration()-player.getCurrentPosition())<=0){
                                                Message msg = handler.obtainMessage();
                                                msg.arg1 = player.getCurrentPosition();
                                                handler.sendMessage(msg);

                                            }



                                        }
                                    },0,1000);



                                    messageViewHolder.playR.setBackgroundResource(R.drawable.ic_pause);

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                        messageViewHolder.playS.setBackgroundTintList(ContextCompat.getColorStateList(messageViewHolder.itemView.getContext(), R.color.colorAccent));
                                    }
                                } catch (Exception e) {
                                    // TODO: handle exception
                                }
                            } else {
                                if (player.isPlaying()) {
                                    messageViewHolder.playR.setBackgroundResource(R.drawable.ic_play);
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                        messageViewHolder.playS.setBackgroundTintList(ContextCompat.getColorStateList(messageViewHolder.itemView.getContext(), R.color.colorAccent));
                                    }
                                    player.pause();
                                    length=player.getCurrentPosition();

                                } else {
                                    messageViewHolder.playR.setBackgroundResource(R.drawable.ic_pause);
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                        messageViewHolder.playS.setBackgroundTintList(ContextCompat.getColorStateList(messageViewHolder.itemView.getContext(), R.color.colorAccent));
                                    }
                                    player.seekTo(length);
                                    player.start();

                                }
                                messageViewHolder.seekR.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                                    @Override
                                    public void onStopTrackingTouch(SeekBar seekBar) {

                                    }

                                    @Override
                                    public void onStartTrackingTouch(SeekBar seekBar) {

                                    }

                                    @Override
                                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                        if(player != null && fromUser){
                                            player.seekTo(progress*player.getDuration() / 100);
                                            length = player.getCurrentPosition();
                                            messageViewHolder.seekR.setProgress(progress);
                                        }
                                    }
                                });
                            }


                        }
                        else{
                            downloadFileq(messageViewHolder.itemView.getContext(),userMessagesList.get(i).getMessage(),i);
                        }
                    }
                });

                messageViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                });

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


    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            // Log exception
            return null;
        }
    }

}