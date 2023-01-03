package com.eserve.android.utils;

import android.content.Context;

public class ProgressDialog {

    /******************
     * Data Members
     */
    private static Context context;
    private static android.app.ProgressDialog progressDialog;


    /*************************************
     * Member Functions Of Progress Dialog
     * Initialize Progress Dialog
     * Show Progress Dialog
     * Dismiss Dialog
     * @param
     */

    //  Initialization
    public static void initialize(Context ctx) {
        context = ctx;
        progressDialog = new android.app.ProgressDialog(context);
    }


    //   Show progress dialog
    public static void show(Context ctx, int title, int msg) {

        ProgressDialog.initialize(ctx);
        if (title != 0)
            progressDialog.setTitle(context.getString(title));
        if (msg != 0)
            progressDialog.setMessage(context.getString(msg));
        progressDialog.setCancelable(false);
        progressDialog.show();
    }


    //   Dismiss progress dialog
    public static void dismiss() {
        progressDialog.dismiss();
    }

}
