package com.codesses.e_serve.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.codesses.e_serve.R;
import com.codesses.e_serve.model.Chat;
import com.codesses.e_serve.utils.FirebaseRef;
import com.codesses.e_serve.viewolder.ViewHolderMessage;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<ViewHolderMessage> {


    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;

    Context mContext;
    List<Chat> mChat;
    FirebaseUser fuser;
    View view;

    public MessageAdapter(Context mContext, List<Chat> mChat) {
        this.mChat = mChat;
        this.mContext = mContext;
    }


    @NonNull
    @Override
    public ViewHolderMessage onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT) {
            view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_sender, parent, false);
        } else if (viewType == MSG_TYPE_LEFT) {
            view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_receiver, parent, false);
        }
        return new ViewHolderMessage(view, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderMessage holder, final int position) {
        Chat chat = mChat.get(position);
        holder.onBind(chat, mContext);
//        holder.setIsRecyclable(false);


    }

    @Override
    public int getItemCount() {
        return mChat.size();
    }

    @Override
    public int getItemViewType(int position) {
        fuser = FirebaseRef.getCurrentUser();
        if (mChat.get(position).getSender_id().equals(fuser.getUid())) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }

//    @Override
//    public void onViewAttachedToWindow(final ViewHolderMessage holder) {
//        if (holder instanceof ViewHolderMessage) {
//            holder.setIsRecyclable(false);
//        }
//        super.onViewAttachedToWindow(holder);
//    }
//
//    @Override
//    public void onViewDetachedFromWindow(final ViewHolderMessage holder) {
//        if (holder instanceof ViewHolderMessage){
//            holder.setIsRecyclable(true);
//        }
//        super.onViewDetachedFromWindow(holder);
//    }
}