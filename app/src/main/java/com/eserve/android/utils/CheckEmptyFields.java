
package com.eserve.android.utils;

import android.content.Context;
import android.widget.EditText;

import com.eserve.android.R;


public class CheckEmptyFields {
    public static boolean isEditText(Context context, String string, EditText editText) {
        if (string.isEmpty()) {
            editText.requestFocus();
            editText.setError(context.getString(R.string.enter) + " " + editText.getHint());
            return false;
        } else {
            return true;
        }
    }



    public static boolean isPassMatch(Context context, String password, String confirmPass, EditText R_Cnfm_Pass) {
        if (!password.equals(confirmPass)) {
            R_Cnfm_Pass.requestFocus();
            R_Cnfm_Pass.setError(context.getString(R.string.pass_not_match));
            return false;
        } else {
            return true;
        }
    }

    public static boolean isPassNotMatch(Context context, String password, String confirmPass, EditText R_Cnfm_Pass) {
        if (password.equals(confirmPass)) {
            R_Cnfm_Pass.requestFocus();
            R_Cnfm_Pass.setError(context.getString(R.string.pass_must_match));
            return false;
        } else {
            return true;
        }
    }


}
