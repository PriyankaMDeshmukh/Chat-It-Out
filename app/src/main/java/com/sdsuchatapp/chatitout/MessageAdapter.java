package com.sdsuchatapp.chatitout;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>{
    private FirebaseAuth auth;
    private List<MessageBean> messageList;
    public MessageAdapter(List<MessageBean> messageList) {
        this.messageList = messageList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.messages_layout, parent,false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.MessageViewHolder holder, int position) {

        auth = FirebaseAuth.getInstance();
        String currentUserId = auth.getCurrentUser().getUid();
        MessageBean currentMessage = messageList.get(position);
        String fromUserId = currentMessage.getFrom();
        if(fromUserId.equalsIgnoreCase(currentUserId)){
            holder.userMessage.setBackgroundColor(Color.WHITE);
            holder.userMessage.setTextColor(Color.BLACK);
        }
        else{
            holder.userMessage.setBackgroundResource(R.drawable.message_background);
            holder.userMessage.setTextColor(Color.WHITE);

        }
        holder.userMessage.setText(currentMessage.getMessage());
    }


    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        private CircleImageView userImage;
        private TextView userMessage;

        public MessageViewHolder(View itemView) {
            super(itemView);

            userImage = itemView.findViewById(R.id.userImage);
            userMessage = itemView.findViewById(R.id.singleMessageView);
        }

    }
}
