package com.shalev396.offdutycallfilter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LogAdapter extends RecyclerView.Adapter<LogAdapter.LogViewHolder> {

    private final List<BlockedCall> blockedCalls;
    private final Context context;

    public LogAdapter(List<BlockedCall> blockedCalls, Context context) {
        this.blockedCalls = blockedCalls;
        this.context = context;
    }

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_blocked_call, parent, false);
        return new LogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        BlockedCall call = blockedCalls.get(position);
        holder.bind(call);
    }

    @Override
    public int getItemCount() {
        return blockedCalls.size();
    }

    public class LogViewHolder extends RecyclerView.ViewHolder {

        private final TextView callerName;
        private final TextView callerNumber;
        private final TextView callTime;
        private final ImageButton callButton;

        public LogViewHolder(@NonNull View itemView) {
            super(itemView);
            callerName = itemView.findViewById(R.id.text_caller_name);
            callerNumber = itemView.findViewById(R.id.text_caller_number);
            callTime = itemView.findViewById(R.id.text_call_time);
            callButton = itemView.findViewById(R.id.button_call);
        }

        public void bind(BlockedCall call) {
            String name = getContactName(call.getNumber());
            callerName.setText(name != null ? name : "Unknown");
            callerNumber.setText(call.getNumber());

            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());
            callTime.setText(sdf.format(new Date(call.getTimestamp())));

            callButton.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", call.getNumber(), null));
                context.startActivity(intent);
            });
        }

        private String getContactName(String number) {
            try (android.database.Cursor cursor = context.getContentResolver().query(
                    android.net.Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, android.net.Uri.encode(number)),
                    new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME);
                    if(nameIndex > -1) {
                        return cursor.getString(nameIndex);
                    }
                }
            }
            return null;
        }
    }
}
