package com.example.callfilter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class WhitelistAdapter extends RecyclerView.Adapter<WhitelistViewHolder> {

    // Define the listener interface within the adapter
    public interface OnContactRemoveListener {
        void onRemove(int position);
    }

    private List<WhitelistedContact> whitelist;
    private final OnContactRemoveListener removeListener;

    public WhitelistAdapter(List<WhitelistedContact> whitelist, OnContactRemoveListener removeListener) {
        this.whitelist = whitelist;
        this.removeListener = removeListener;
    }

    @NonNull
    @Override
    public WhitelistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_whitelist_contact, parent, false);
        return new WhitelistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WhitelistViewHolder holder, int position) {
        WhitelistedContact contact = whitelist.get(position);
        holder.bind(contact, () -> {
            int bindingAdapterPosition = holder.getBindingAdapterPosition();
            if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                removeListener.onRemove(bindingAdapterPosition);
            }
        });
    }

    @Override
    public int getItemCount() {
        return whitelist.size();
    }

    public void filterList(List<WhitelistedContact> filteredList) {
        final int oldSize = whitelist.size();
        whitelist = filteredList;
        final int newSize = whitelist.size();
        if (newSize > oldSize) {
            notifyItemRangeInserted(oldSize, newSize - oldSize);
        } else if (newSize < oldSize) {
            notifyItemRangeRemoved(newSize, oldSize - newSize);
        }
        notifyItemRangeChanged(0, Math.min(newSize, oldSize));
    }
}
