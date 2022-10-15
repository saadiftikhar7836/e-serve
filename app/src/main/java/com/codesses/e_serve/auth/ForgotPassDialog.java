package com.codesses.e_serve.auth;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import com.codesses.e_serve.R;
import com.codesses.e_serve.interfaces.OnForgotPassClick;


public class ForgotPassDialog extends DialogFragment {

    // Tag
    public static final String TAG = "FORGOT_PASSWORD_DIALOG";


    // Context
    private FragmentActivity mContext;

    // Widgets
    EditText etEmail;
    TextView btnSendEmail;
    TextView btnCancel;


    //    Variables
    private boolean isEmail = false;

    //     Interface
    private OnForgotPassClick onForgotPassClick;

    public ForgotPassDialog() {

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        mContext = getActivity();

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        LayoutInflater inflater = mContext.getLayoutInflater();

        View view = inflater.inflate(R.layout.forgot_pass_layout, null);

        etEmail = view.findViewById(R.id.et_email);
        btnSendEmail = view.findViewById(R.id.btn_send_email);
        btnCancel = view.findViewById(R.id.btn_cancel);



        builder.setView(view);


//        Click listeners
        btnSendEmail.setOnClickListener(v -> sendEmail());
        btnCancel.setOnClickListener(v -> dismiss());


        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            onForgotPassClick = (OnForgotPassClick) context;
        } catch (ClassCastException e) {
            Log.e(TAG, e.getMessage());
            throw new ClassCastException(context.toString() +
                    "Must Implement ForgotDialogListener");
        }

    }


    @Override
    public void onDetach() {
        onForgotPassClick = null;
        super.onDetach();
    }


    private void sendEmail() {
        String email = etEmail.getText().toString().trim();

        if (!TextUtils.isEmpty(email)) {
            onForgotPassClick.onApply(email);
            dismiss();
        }
    }


}
