package mfs.ese.scotlandyard;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;

/**
 * Created by Benjamin on 09.05.2014.
 */
public class NumberPickerDialog extends DialogFragment {
    public interface NumberPickerDialogListener {
        public void onDialogPositiveClick(NumberPickerDialog dialog);
        public void onDialogNegativeClick(NumberPickerDialog dialog);
    }

    View mView;
    NumberPickerDialogListener mListener;
    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (NumberPickerDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NumberPickerDialogListener");
        }
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        mView = inflater.inflate(R.layout.fragment_dialog_number, null);
        builder.setView(mView)
                // Add action buttons
                .setTitle(R.string.dialog_number_title)
                .setMessage(R.string.dialog_number_description)
                .setPositiveButton(R.string.dialog_number_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onDialogPositiveClick(NumberPickerDialog.this);
                    }
                })
                .setNegativeButton(R.string.dialog_number_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        NumberPickerDialog.this.getDialog().cancel();
                        mListener.onDialogNegativeClick(NumberPickerDialog.this);
                    }
                });
        NumberPicker nb = (NumberPicker) mView.findViewById(R.id.numberPicker);
        nb.setMinValue(1);
        nb.setMaxValue(30);
        return builder.create();
    }

    public int getResult()
    {
        return ((NumberPicker) mView.findViewById(R.id.numberPicker)).getValue();
    }
    /*public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.dialog_number_description)
                .setPositiveButton(R.string.dialog_number_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // FIRE ZE MISSILES!
                    }
                })
                .setNegativeButton(R.string.dialog_number_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }*/
}
