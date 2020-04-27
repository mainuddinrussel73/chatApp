package com.example.mainuddin.doapp;

import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mainuddin.doapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class GroupChatActivity extends AppCompatActivity implements ExampleBottomSheetDialog.BottomSheetListener
{
    private Toolbar mToolbar;
    private ImageButton SendMessageButton,SendLikeButton;
    private EditText userMessageInput;

    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef, GroupNameRef, GroupMessageKeyRef;

    public static String currentGroupName, currentUserID, currentUserName, currentDate, currentTime;
    private final List<GroupMessage> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private GroupMessageAdapter messageAdapter;
    public static RecyclerView userMessagesList;
    private DatabaseReference GroupRef;
    public static  String msgKey;
    boolean canclick = false;
    CircleImageView user1,user2;
    TextView tagg;
    Set<String> set = new HashSet<>();
    String User1 = "",User2="";


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_group_chat);



        currentGroupName = getIntent().getExtras().get("groupName").toString();
        Toast.makeText(GroupChatActivity.this, currentGroupName, Toast.LENGTH_SHORT).show();


        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        GroupNameRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName);

        GroupRef = FirebaseDatabase.getInstance().getReference().child("Groups");


        InitializeFields();


        GetUserInfo();

        GroupNameRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Iterator iterator = dataSnapshot.getChildren().iterator();
                int i = 2;
                while (iterator.hasNext()) {

                    DataSnapshot snapshot = ((DataSnapshot) iterator.next());

                    if (snapshot.getValue().equals("member")) {
                        set.add(snapshot.getKey());

                        if(i==2){
                            User1 =  snapshot.getKey();
                        }else if(i==1){
                            User2 = snapshot.getKey();
                        }
                        i--;



                    }


                }
                if(set.size()<=2){
                    tagg.setText("+"+(set.size())+"Users ");
                }
                else {tagg.setText("+"+(set.size()-2)+"Users ");}
                FirebaseDatabase.getInstance().getReference().child("Users").child(User1).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        if (dataSnapshot.hasChild("image"))
                        {
                            String receiverImage = dataSnapshot.child("image").getValue().toString();

                            Picasso.get().load(receiverImage).placeholder(R.drawable.profile_image).into(user1);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                FirebaseDatabase.getInstance().getReference().child("Users").child(User2).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        if (dataSnapshot.hasChild("image"))
                        {
                            String receiverImage = dataSnapshot.child("image").getValue().toString();

                            Picasso.get().load(receiverImage).placeholder(R.drawable.profile_image).into(user2);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });



            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });









        SendMessageButton.setVisibility(View.GONE);
        SendLikeButton.setVisibility(View.VISIBLE);


        userMessageInput.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
                if(!userMessageInput.getText().toString().equals("")){
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

        SendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                SaveMessageInfoToDatabase();

                userMessageInput.setText("");

            }
        });

        SendLikeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SaveLikeInfoToDatabase();
            }
        });

        ImageButton buttonOpenBottomSheet = findViewById(R.id.send_options_button);
        buttonOpenBottomSheet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ExampleBottomSheetDialog bottomSheet = new ExampleBottomSheetDialog();
                bottomSheet.show(getSupportFragmentManager(), "exampleBottomSheet");
            }
        });

        GroupNameRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue(String.class).equals("leader")){
                    canclick = true;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //Log.w(TAG, "onCancelled", databaseError.toException());
            }
        });

    }



    @Override
    protected void onStart()
    {
        super.onStart();

        messagesList.clear();

        messageAdapter.notifyDataSetChanged();



        GroupNameRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Iterator iterator = dataSnapshot.getChildren().iterator();
                while (iterator.hasNext()) {

                    DataSnapshot snapshot = ((DataSnapshot) iterator.next());

                    if (snapshot.getValue().equals("member")) {
                        set.add(snapshot.getKey());



                    }


                }




            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        GroupNameRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s)
            {
                if (dataSnapshot.exists())
                {
                    DisplayMessages(dataSnapshot);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s)
            {
                if (dataSnapshot.exists())
                {
                    DisplayMessages(dataSnapshot);
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


    private void InitializeFields()
    {
        mToolbar = (Toolbar) findViewById(R.id.group_chat_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(currentGroupName);

        mToolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_arrow_back_black_24dp));
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //What to do on back clicked
                finish();
            }
        });

        mToolbar.setBackgroundDrawable(ContextCompat.getDrawable(this,R.drawable.message_edit));

        SendMessageButton = (ImageButton) findViewById(R.id.send_message_button);
        SendLikeButton = findViewById(R.id.send_like_button);
        userMessageInput = (EditText) findViewById(R.id.input_message);
        user1 = findViewById(R.id.remove_user);
        user2 = findViewById(R.id.remove_use1r);
        tagg = findViewById(R.id.use_count);

        messageAdapter = new GroupMessageAdapter(messagesList);
        userMessagesList = (RecyclerView) findViewById(R.id.group_messages_list_of_users);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(messageAdapter);


    }



    private void GetUserInfo()
    {
        UsersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists())
                {
                    currentUserName = dataSnapshot.child("name").getValue().toString();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }




    private void SaveMessageInfoToDatabase()
    {
        String message = userMessageInput.getText().toString();
        String messagekEY = GroupNameRef.push().getKey();
        msgKey = messagekEY;

        if (TextUtils.isEmpty(message))
        {
            Toast.makeText(this, "Please write message first...", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Calendar calForDate = Calendar.getInstance();
            SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM dd, yyyy");
            currentDate = currentDateFormat.format(calForDate.getTime());

            Calendar calForTime = Calendar.getInstance();
            SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
            currentTime = currentTimeFormat.format(calForTime.getTime());


            HashMap<String, Object> groupMessageKey = new HashMap<>();
            GroupNameRef.updateChildren(groupMessageKey);

            GroupMessageKeyRef = GroupNameRef.child(messagekEY);

            HashMap<String, Object> messageInfoMap = new HashMap<>();
            messageInfoMap.put("name", currentUserName);
            messageInfoMap.put("message", message);
            messageInfoMap.put("messageKey", messagekEY);
            messageInfoMap.put("groupName",currentGroupName);
            messageInfoMap.put("date", currentDate);
            messageInfoMap.put("time", currentTime);
            messageInfoMap.put("from",currentUserID);
            messageInfoMap.put("countY", 0);
            messageInfoMap.put("countN", 0);
            messageInfoMap.put("type", "text");
            GroupMessageKeyRef.updateChildren(messageInfoMap);
        }
    }

    private void SaveLikeInfoToDatabase()
    {
        String message = userMessageInput.getText().toString();
        String messagekEY = GroupNameRef.push().getKey();
        msgKey = messagekEY;


        {
            Calendar calForDate = Calendar.getInstance();
            SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM dd, yyyy");
            currentDate = currentDateFormat.format(calForDate.getTime());

            Calendar calForTime = Calendar.getInstance();
            SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
            currentTime = currentTimeFormat.format(calForTime.getTime());


            HashMap<String, Object> groupMessageKey = new HashMap<>();
            GroupNameRef.updateChildren(groupMessageKey);

            GroupMessageKeyRef = GroupNameRef.child(messagekEY);

            HashMap<String, Object> messageInfoMap = new HashMap<>();
            messageInfoMap.put("name", "like");
            messageInfoMap.put("message", "");
            messageInfoMap.put("messageKey", messagekEY);
            messageInfoMap.put("groupName",currentGroupName);
            messageInfoMap.put("date", currentDate);
            messageInfoMap.put("time", currentTime);
            messageInfoMap.put("from",currentUserID);
            messageInfoMap.put("countY", 0);
            messageInfoMap.put("countN", 0);
            messageInfoMap.put("type", "emoji");
            GroupMessageKeyRef.updateChildren(messageInfoMap);
        }
    }



    private void DisplayMessages(DataSnapshot dataSnapshot)
    {

        GroupMessage messages = new GroupMessage();;

        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {

            System.out.println(postSnapshot.getValue());
            if(postSnapshot.getKey().equals("countN")){
                messages = new GroupMessage();
                messages.setPollN(Integer.parseInt(postSnapshot.getValue().toString()));
            }else if(postSnapshot.getKey().equals("countY")){
                messages.setPollY(Integer.parseInt(postSnapshot.getValue().toString()));
            }else if(postSnapshot.getKey().equals("groupName")){
                messages.setGroupName(postSnapshot.getValue().toString());
            } else if(postSnapshot.getKey().equals("messageKey")){
                messages.setMessageKey(postSnapshot.getValue().toString());
            } else if(postSnapshot.getKey().equals("date")){
                messages.setDate(postSnapshot.getValue().toString());
            }else if(postSnapshot.getKey().equals("name")){
                messages.setName(postSnapshot.getValue().toString());
            }else if(postSnapshot.getKey().equals("message")){
                messages.setMessage(postSnapshot.getValue().toString());
            }else if(postSnapshot.getKey().equals("time")){
                messages.setTime(postSnapshot.getValue().toString());
            }else if(postSnapshot.getKey().equals("from")){
                messages.setFrom(postSnapshot.getValue().toString());
            }else if(postSnapshot.getKey().equals("type")){
                messages.setType(postSnapshot.getValue().toString());
                messagesList.add(messages);
            }

        }

        messageAdapter.notifyDataSetChanged();
        userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());

    }
    private void SendPoll(String text)
    {

        String messagekEY = GroupNameRef.push().getKey();
        msgKey = messagekEY;

        if (TextUtils.isEmpty(text))
        {
            Toast.makeText(this, "Please write topic first...", Toast.LENGTH_SHORT).show();
        }
        else {


            Calendar calForDate = Calendar.getInstance();
            SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM dd, yyyy");
            currentDate = currentDateFormat.format(calForDate.getTime());

            Calendar calForTime = Calendar.getInstance();
            SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
            currentTime = currentTimeFormat.format(calForTime.getTime());


            HashMap<String, Object> groupMessageKey = new HashMap<>();
            GroupNameRef.updateChildren(groupMessageKey);

            GroupMessageKeyRef = GroupNameRef.child(messagekEY);

            HashMap<String, Object> messageInfoMap = new HashMap<>();
            messageInfoMap.put("name", currentUserName);
            messageInfoMap.put("message", text);
            messageInfoMap.put("messageKey", messagekEY);
            messageInfoMap.put("groupName",currentGroupName);
            messageInfoMap.put("date", currentDate);
            messageInfoMap.put("time", currentTime);
            messageInfoMap.put("from", currentUserID);
            messageInfoMap.put("countY", 0);
            messageInfoMap.put("countN", 0);
            messageInfoMap.put("type", "poll");
            GroupMessageKeyRef.updateChildren(messageInfoMap);


        }


    }


    @Override
    public void onButtonClicked(String text) {

        if(text.equals("poll")){

            LayoutInflater layoutInflaterAndroid = LayoutInflater.from(GroupChatActivity.this);
            View mView = layoutInflaterAndroid.inflate(R.layout.options, null);
            AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(GroupChatActivity.this,R.style.RoundedDialog);
            alertDialogBuilderUserInput.setView(mView);

            final EditText userInputDialogEditText = (EditText) mView.findViewById(R.id.userInputDialog);
            alertDialogBuilderUserInput
                    .setCancelable(false)
                    .setPositiveButton("Send", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogBox, int id) {
                            // ToDo get user input here
                            System.out.println(userInputDialogEditText.getText().toString());
                            SendPoll(userInputDialogEditText.getText().toString());
                            userMessageInput.setText("");
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
        else if(text.equals("remove")){
            if(canclick) {


                AlertDialog.Builder builderSingle = new AlertDialog.Builder(GroupChatActivity.this, R.style.RoundedDialog);
                builderSingle.setIcon(R.drawable.chat);
                builderSingle.setTitle("Select One Name:-");

                final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(GroupChatActivity.this, android.R.layout.select_dialog_singlechoice);

                HashMap<String, String> map = new HashMap<>();
                for (String sa :
                        set) {
                    FirebaseDatabase.getInstance().getReference().child("Users").child(sa).addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                            if (dataSnapshot.getKey().equals("name")) {
                                System.out.println(dataSnapshot.getValue().toString());
                                String name = dataSnapshot.getValue().toString();
                                map.put(name, sa);
                                arrayAdapter.add(name);
                            }


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


                builderSingle.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String strName = arrayAdapter.getItem(which);
                        AlertDialog.Builder builderInner = new AlertDialog.Builder(GroupChatActivity.this, R.style.RoundedDialog);
                        builderInner.setMessage(strName);
                        builderInner.setTitle("Your Selected Item is");
                        builderInner.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();

                                System.out.println(map.get(strName) + "val");
                                System.out.println(GroupNameRef.child(map.get(strName)).removeValue().isSuccessful());


                            }
                        });
                        builderInner.show();
                    }
                });
                builderSingle.show();

            }else{
                Toast.makeText(GroupChatActivity.this,"You are not an admin.",Toast.LENGTH_LONG).show();
            }
        }

    }
}