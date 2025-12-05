package com.example.offdutycallfilter;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class WhitelistViewHolder extends RecyclerView.ViewHolder {

    private final TextView textContactName;
    private final TextView textPhoneNumber;
    private final ImageButton buttonRemoveContact;

    public interface OnRemoveClickListener {
        void onRemoveClick();
    }

    public WhitelistViewHolder(@NonNull View itemView) {
        super(itemView);
        textContactName = itemView.findViewById(R.id.text_contact_name);
        textPhoneNumber = itemView.findViewById(R.id.text_phone_number);
        buttonRemoveContact = itemView.findViewById(R.id.button_remove_contact);
    }

    public void bind(WhitelistedContact contact, final OnRemoveClickListener removeListener) {
        textContactName.setText(contact.getName());
        textPhoneNumber.setText(contact.getPhoneE164());

        buttonRemoveContact.setOnClickListener(v -> removeListener.onRemoveClick());
    }
}
