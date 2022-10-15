package com.codesses.e_serve.viewolder;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.codesses.e_serve.R;
import com.codesses.e_serve.model.Chat;

import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.Locale;

public class ViewHolderMessage extends RecyclerView.ViewHolder {

    TextView showMessage;
    TextView timeStamp;
    int viewType;
    ConstraintLayout constraintLayout;


    public ViewHolderMessage(@NonNull @NotNull View itemView, int viewType) {
        super(itemView);

        this.viewType = viewType;
        showMessage = itemView.findViewById(R.id.show_message);
        timeStamp = itemView.findViewById(R.id.timestamp);
        constraintLayout = itemView.findViewById(R.id.parent);


    }

    public void onBind(Chat chat, Context mContext) {
//            if (chat.isSeen().equals("delivered"))
//                seenImage.setImageResource(R.drawable.ic_done);
//            else if (chat.isSeen().equals("received"))
//                seenImage.setImageResource(R.drawable.ic_rceived);
//            else if (chat.isSeen().equals("read"))
//                seenImage.setImageResource(R.drawable.ic_done_all);


        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        try {
            cal.setTimeInMillis(Long.parseLong(chat.getTimestamp()));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
//
//        if (!chat.isPicked())
//            constraintLayout.setBackgroundColor(mContext.getResources().getColor(R.color.white));
//        else
//            constraintLayout.setBackgroundColor(mContext.getResources().getColor(R.color.transparent_green));
        String dateTime = DateFormat.format("hh:mm ", cal).toString();
        timeStamp.setText(dateTime);
        showMessage.setText(chat.getMessage());


    }
}
