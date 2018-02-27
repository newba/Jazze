package br.com.darksite.jazze.adapteur;


import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.util.List;

import br.com.darksite.jazze.R;
import br.com.darksite.jazze.application.ConfigurationFirebase;
import br.com.darksite.jazze.model.Messages;
import de.hdodenhof.circleimageview.CircleImageView;

public class MessagesAdapter extends RecyclerView.Adapter <MessagesAdapter.MessageViewHolder> {

    private List<Messages> userMessagesList;

    private FirebaseAuth mAuth;

    public MessagesAdapter (List<Messages> userMessagesList){
        this.userMessagesList = userMessagesList;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.messages_layout_of_users, parent, false);

        mAuth = ConfigurationFirebase.getFirebasejazzeAuth();

        return new MessageViewHolder(view);

    }

    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {

        String messageSenderID = mAuth.getCurrentUser().getUid();

        Messages messages = userMessagesList.get(position);

        String fromUserID = messages.getFrom();

        if(messageSenderID.equals(fromUserID)){
            holder.messageText.setBackgroundResource(R.drawable.message_text_from_user);
            holder.messageText.setTextColor(Color.WHITE);
            holder.messageText.setGravity(Gravity.RIGHT);
            //Picasso.with(holder.userProfileImage.getContext().load(user))
        }
        else{
            holder.messageText.setBackgroundResource(R.drawable.message_text_background);
            holder.messageText.setTextColor(Color.BLACK);
            holder.messageText.setGravity(Gravity.LEFT);
        }

        holder.messageText.setText(messages.getMessage());
    }

    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder{
        /*
        https://developer.android.com/training/material/lists-cards.html
        https://www.androidhive.info/2016/01/android-working-with-recycler-view/
        https://github.com/newba/apk/tree/master/ListEpicerie
        https://github.com/newba/apk/tree/master/ListaDeCursos
        https://github.com/newba/apk/tree/master/ListExample
         */
        public TextView messageText;
        public CircleImageView userProfileImage;


        public MessageViewHolder(View itemView) {
            super(itemView);
            messageText = (TextView) itemView.findViewById(R.id.message_text);

            //userProfileImage = (CircleImageView) itemView.findViewById(R.id.messages_profile_image);
        }
    }

}
