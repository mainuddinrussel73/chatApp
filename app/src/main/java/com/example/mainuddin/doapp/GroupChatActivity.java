package com.example.mainuddin.doapp;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mainuddin.doapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class GroupChatActivity extends AppCompatActivity
{
    private Toolbar mToolbar;
    private ImageButton SendMessageButton;
    private EditText userMessageInput;

    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef, GroupNameRef, GroupMessageKeyRef;

    private String currentGroupName, currentUserID, currentUserName, currentDate, currentTime;
    private final List<GroupMessage> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private GroupMessageAdapter messageAdapter;
    private RecyclerView userMessagesList;
    private DatabaseReference GroupRef;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
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


        SendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                SaveMessageInfoToDatabase();

                userMessageInput.setText("");

            }
        });

        ImageButton remove_user = findViewById(R.id.remove_user);
        remove_user.setVisibility(View.GONE);
        GroupNameRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue(String.class).equals("leader")){
                    remove_user.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //Log.w(TAG, "onCancelled", databaseError.toException());
            }
        });

        remove_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GroupNameRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        Set<String> set = new HashSet<>();
                        Iterator iterator = dataSnapshot.getChildren().iterator();

                        while (iterator.hasNext())
                        {

                            DataSnapshot snapshot = ((DataSnapshot)iterator.next());
                            System.out.println(snapshot.getValue());

                            if(snapshot.getValue().equals("member")){
                                set.add(snapshot.getKey());



                            }



                        }


                        AlertDialog.Builder builderSingle = new AlertDialog.Builder(GroupChatActivity.this);
                        builderSingle.setIcon(R.drawable.chat);
                        builderSingle.setTitle("Select One Name:-");

                        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(GroupChatActivity.this, android.R.layout.select_dialog_singlechoice);

                        HashMap<String,String> map = new HashMap<>();
                        for (String sa:
                             set) {
                            FirebaseDatabase.getInstance().getReference().child("Users").child(sa).addChildEventListener(new ChildEventListener() {
                                @Override
                                public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                                    if(dataSnapshot.getKey().equals("name")){
                                        System.out.println(dataSnapshot.getValue().toString());
                                        String name = dataSnapshot.getValue().toString();
                                        map.put(name,sa);
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
                                AlertDialog.Builder builderInner = new AlertDialog.Builder(GroupChatActivity.this);
                                builderInner.setMessage(strName);
                                builderInner.setTitle("Your Selected Item is");
                                builderInner.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,int which) {
                                        dialog.dismiss();

                                        System.out.println(map.get(strName)+"val");
                                        System.out.println(GroupNameRef.child(map.get(strName)).removeValue().isSuccessful());


                                    }
                                });
                                builderInner.show();
                            }
                        });
                        builderSingle.show();

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });




            }
        });
    }



    @Override
    protected void onStart()
    {
        super.onStart();

        messagesList.clear();

        messageAdapter.notifyDataSetChanged();

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

        SendMessageButton = (ImageButton) findViewById(R.id.send_message_button);
        userMessageInput = (EditText) findViewById(R.id.input_group_message);

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
            messageInfoMap.put("date", currentDate);
            messageInfoMap.put("time", currentTime);
            messageInfoMap.put("from",currentUserID);
            messageInfoMap.put("type", "text");
            GroupMessageKeyRef.updateChildren(messageInfoMap);
        }
    }



    private void DisplayMessages(DataSnapshot dataSnapshot)
    {

        GroupMessage messages = new GroupMessage();;

        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {

            System.out.println(postSnapshot.getValue());
            if(postSnapshot.getKey().equals("date")){
                messages = new GroupMessage();
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


}