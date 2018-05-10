package com.sdsuchatapp.chatitout;

import android.graphics.Color;
import android.support.annotation.NonNull;
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
        View view;
        if(viewType == 1){
             view = LayoutInflater.from(parent.getContext()).inflate(R.layout.messages_layout_right, parent,false);
        }
        else{
             view = LayoutInflater.from(parent.getContext()).inflate(R.layout.messages_layout_left, parent,false);
        }
        return new MessageViewHolder(view);
    }
    //Reference: https://stackoverflow.com/questions/37677520/how-to-do-threaded-message-view-with-send-and-received-messages-on-two-sides-wit?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa
    //Edit by: Priyanka Deshmukh
    @Override
    public int getItemViewType(int position) {
        auth = FirebaseAuth.getInstance();
        String currentUserId = auth.getCurrentUser().getUid();
        MessageBean currentMessage = messageList.get(position);
        String fromUserId = currentMessage.getFrom();
        if (fromUserId.equalsIgnoreCase(currentUserId)) {
            return 1;
        }
        else{
            return 2;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.MessageViewHolder holder, int position) {

        auth = FirebaseAuth.getInstance();
        String currentUserId = auth.getCurrentUser().getUid();
        MessageBean currentMessage = messageList.get(position);
        String fromUserId = currentMessage.getFrom();
        if(fromUserId.equalsIgnoreCase(currentUserId)){
            holder.userMessage.setBackgroundResource(R.drawable.message_background_sent);
            holder.userMessage.setTextColor(Color.BLACK);



        }
        else{
            holder.userMessage.setBackgroundResource(R.drawable.message_background_received);
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
