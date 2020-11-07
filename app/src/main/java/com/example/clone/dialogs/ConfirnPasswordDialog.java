package com.example.clone.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.clone.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class ConfirnPasswordDialog extends DialogFragment {

    private static final String TAG = "ConfirnPasswordDialog";

    public interface OnConfirmPasswordListener{
        public void onConfirmPassword(String password);
    }
    OnConfirmPasswordListener mOnConfirmPasswordListener;

    // vers
    TextView mPassword;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_confrm_password,container,false);
        mPassword=(TextView)view.findViewById(R.id.confirm_password);

        Log.d(TAG, "onCreateView: started");

        TextView confirmDialog =(TextView)view.findViewById(R.id.dialogCcofirm);
        confirmDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: capture password and confirming");

                String password=mPassword.getText().toString();
                if (!password.equals("")) {
                    mOnConfirmPasswordListener.onConfirmPassword(password);
                    getDialog().dismiss();
                }else {
                    Toast.makeText(getActivity(),"you must enter a password",Toast.LENGTH_SHORT).show();
                }
            }
        });

        TextView cancelDialog =(TextView)view.findViewById(R.id.dialogCancel);
        cancelDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: closing the dialog");
                getDialog().dismiss();
            }
        });
        
        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            mOnConfirmPasswordListener= (OnConfirmPasswordListener)getTargetFragment();
        }
        catch (ClassCastException e){
            Log.e(TAG, "onAttach: Class cast exception" + e.getMessage() );
        }
    }
}
