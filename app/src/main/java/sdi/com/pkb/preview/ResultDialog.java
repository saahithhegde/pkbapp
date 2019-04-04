package sdi.com.pkb.preview;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import sdi.com.pkb.R;

/**
 * Created by Kartik on 05-Apr-19.
 */
public class ResultDialog extends DialogFragment {
    private TextView ownerName,ownerAddress,regNo,chassisNo,color;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        Bundle args = getArguments();
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View v = inflater.inflate(R.layout.result_dialog, null);
        builder.setView(v)
                .setPositiveButton("Check Violations", (dialog, which) -> {

                })
                .setNegativeButton("Dismiss",((dialog, which) -> {

                }));
        ownerName = v.findViewById(R.id.name);
        regNo = v.findViewById(R.id.reg);
        chassisNo = v.findViewById(R.id.chassis_number);
        color = v.findViewById(R.id.color_value);
        ownerAddress = v.findViewById(R.id.address_value);
        ownerName.setText((String) args.get("name"));
        ownerAddress.setText((String) args.get("address"));
        regNo.setText((String) args.get("reg"));
        chassisNo.setText((String) args.get("chassis"));
        color.setText((String) args.get("color"));
        return builder.create();
    }
}
