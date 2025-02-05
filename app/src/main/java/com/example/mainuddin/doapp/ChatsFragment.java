package com.example.mainuddin.doapp;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mainuddin.doapp.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class ChatsFragment extends Fragment
{
    private View PrivateChatsView;
    private RecyclerView chatsList;

    private DatabaseReference ChatsRef, UsersRef;
    private FirebaseAuth mAuth;
    private String currentUserID="";
    private String LastMessage="";
    private String messageReceiverID, messageReceiverName, messageReceiverImage, messageSenderID;


    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        PrivateChatsView = inflater.inflate(R.layout.fragment_chats, container, false);


        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        ChatsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");


        chatsList = (RecyclerView) PrivateChatsView.findViewById(R.id.chats_list);
        chatsList.setLayoutManager(new LinearLayoutManager(getContext()));


        return PrivateChatsView;
    }


    @Override
    public void onStart()
    {
        super.onStart();


        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                        .setQuery(ChatsRef, Contacts.class)
                        .build();


        FirebaseRecyclerAdapter<Contacts, ChatsViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, ChatsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final ChatsViewHolder holder, int position, @NonNull Contacts model)
                    {
                        holder.itemView.findViewById(R.id.last_msg).setVisibility(View.VISIBLE);
                        final String usersIDs = getRef(position).getKey();
                        messageReceiverID = currentUserID;
                        final String[] retImage = {"default_image"};
                        LastMessage = "default";
                        messageSenderID = usersIDs;
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Messages").child(messageReceiverID).child(messageSenderID);
                        reference.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                                    if (snapshot.getValue() != null){
                                        Messages messages = snapshot.getValue(Messages.class);
                                        {
                                            LastMessage = messages.getMessage();
                                            if(LastMessage.equals("default")){
                                                holder.last_msg.setText("No Message");
                                            }
                                            else {

                                                if(messages.getType().equals("emoji")){
                                                    holder.last_msg.setText("\uD83D\uDC4D");
                                                }else if(messages.getType().equals("image")){
                                                    holder.last_msg.setText("\uD83C\uDF05");
                                                }else if(messages.getType().equals("pdf") || messages.getType().equals("txt") || messages.getType().equals("pptx") ||
                                                        messages.getType().equals("docx")){
                                                    holder.last_msg.setText("\uD83D\uDCC3");
                                                }else if(messages.getType().equals("mp3")){
                                                    holder.last_msg.setText("\uD83C\uDFA4");
                                                }else if(messages.getType().equals("location")){
                                                    holder.last_msg.setText("\uD83D\uDCCD");
                                                }
                                                else{
                                                    if(!messages.isIsseen()){

                                                        holder.last_msg.setText(LastMessage);
                                                        holder.last_msg.setTypeface(Typeface.DEFAULT_BOLD);

                                                    }else{

                                                        holder.last_msg.setText(LastMessage);
                                                        holder.last_msg.setTypeface(Typeface.DEFAULT);

                                                    }
                                                }

                                            }

                                            LastMessage = "default";
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                        UsersRef.child(usersIDs).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot)
                            {
                                if (dataSnapshot.exists())
                                {
                                    if (dataSnapshot.hasChild("image"))
                                    {
                                        retImage[0] = dataSnapshot.child("image").getValue().toString();
                                        Picasso.get().load(retImage[0]).into(holder.profileImage);
                                    }

                                    final String retName = dataSnapshot.child("name").getValue().toString();
                                    final String retStatus = dataSnapshot.child("status").getValue().toString();

                                    holder.userName.setText(retName);


                                    if (dataSnapshot.child("userState").hasChild("state"))
                                    {
                                        String state = dataSnapshot.child("userState").child("state").getValue().toString();
                                        String date = dataSnapshot.child("userState").child("date").getValue().toString();
                                        String time = dataSnapshot.child("userState").child("time").getValue().toString();

                                        if (state.equals("online"))
                                        {
                                            holder.userStatus.setText("online");
                                            holder.onlineIcon.setVisibility(View.VISIBLE);
                                        }
                                        else if (state.equals("offline"))
                                        {
                                            holder.userStatus.setText("Last Seen: " + date + " " + time);
                                            holder.onlineIcon.setVisibility(View.INVISIBLE);
                                        }


                                    }
                                    else
                                    {
                                        holder.userStatus.setText("offline");
                                        holder.onlineIcon.setVisibility(View.INVISIBLE);

                                    }


                                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view)
                                        {

                                            messageReceiverName = retName;
                                            messageReceiverImage = retImage[0];

                                            Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                            chatIntent.putExtra("visit_user_id", usersIDs);
                                            chatIntent.putExtra("visit_user_name", retName);
                                            chatIntent.putExtra("visit_image", retImage[0]);
                                            startActivity(chatIntent);
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });


                    }

                    @NonNull
                    @Override
                    public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
                    {
                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.user_display_chat, viewGroup, false);
                        return new ChatsViewHolder(view);
                    }
                };

        chatsList.setAdapter(adapter);
        adapter.startListening();
    }




    public static class  ChatsViewHolder extends RecyclerView.ViewHolder
    {
        CircleImageView profileImage;
        TextView userStatus, userName,last_msg;
        ImageView onlineIcon;


        public ChatsViewHolder(@NonNull View itemView)
        {
            super(itemView);

            profileImage = itemView.findViewById(R.id.users_profile_image);
            userStatus = itemView.findViewById(R.id.user_status);
            userName = itemView.findViewById(R.id.user_profile_name);
            last_msg = itemView.findViewById(R.id.last_msg);
            onlineIcon = (ImageView) itemView.findViewById(R.id.user_online_status);
        }
    }


}