package com.codesses.e_serve.dialogs;


import android.app.Dialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;


import com.codesses.e_serve.R;
import com.codesses.e_serve.interfaces.OnChangePassClick;
import com.codesses.e_serve.utils.CheckEmptyFields;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChangePassDialog extends DialogFragment {

    //    TODO: Widgets
    @BindView(R.id.old_pass)
    EditText Old_Pass;
    @BindView(R.id.new_pass)
    EditText New_Pass;
    @BindView(R.id.update_pass)
    Button Update_Pass;


    //    TODO: Variables
    private boolean isOldPassVisible = false,
            isNewPassVisible = false;


    //    TODO: Interface
    OnChangePassClick onChangePassClick;


    //    TODO: Constructor
    public ChangePassDialog(OnChangePassClick onChangePassClick) {
        this.onChangePassClick = onChangePassClick;
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.change_pass_layout, null);
        ButterKnife.bind(this, view);

        alertDialog.setView(view)
                .setTitle(getString(R.string.change_pass));


//        TODO: Click Listeners
        Update_Pass.setOnClickListener(this::updatePass);


        return alertDialog.show();
    }


    @Override
    public void onDetach() {
        onChangePassClick = null;
        super.onDetach();
    }


    /*****************************************
     * Methods Call In Current Fragment Dialog
     */

    private void updatePass(View view) {
        String oldPass = Old_Pass.getText().toString().trim();
        String newPass = New_Pass.getText().toString().trim();

        if (CheckEmptyFields.isEditText(getActivity(), oldPass, Old_Pass) &&
                CheckEmptyFields.isEditText(getActivity(), newPass, New_Pass) &&
                CheckEmptyFields.isPassNotMatch(getActivity(), oldPass, newPass, New_Pass)) {

            onChangePassClick.onClick(oldPass, newPass);
            dismiss();
        }

    }


}
