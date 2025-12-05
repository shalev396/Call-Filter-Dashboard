package com.example.offdutycallfilter;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class WhitelistFragment extends Fragment {

    private WhitelistAdapter adapter;
    private ConfigManager configManager;
    private List<WhitelistedContact> whitelist;

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
        if (isGranted) {
            launchContactPicker();
        } else {
            Toast.makeText(getContext(), "Read contacts permission is required to add contacts.", Toast.LENGTH_LONG).show();
        }
    });

    private final ActivityResultLauncher<Intent> pickContactLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == -1 && result.getData() != null) {
            Uri contactUri = result.getData().getData();
            if (contactUri != null) {
                Cursor cursor = requireContext().getContentResolver().query(contactUri, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                    int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);                    if (nameIndex != -1 && numberIndex != -1) {
                        String name = cursor.getString(nameIndex);
                        String number = cursor.getString(numberIndex);
                        String normalizedNumber = number.replaceAll("[\\s\\-()]", "");

                        WhitelistedContact newContact = new WhitelistedContact(name, normalizedNumber);
                        whitelist.add(newContact);
                        configManager.setWhitelist(whitelist);
                        adapter.notifyItemInserted(whitelist.size() - 1);
                    }
                    cursor.close();
                }
            }
        }
    });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_whitelist, container, false);

        configManager = new ConfigManager(requireContext());
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view_whitelist);
        EditText searchEditText = view.findViewById(R.id.edit_text_search);
        FloatingActionButton fab = view.findViewById(R.id.fab_add_contact);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        whitelist = configManager.getWhitelist();
        if (whitelist == null) {
            whitelist = new ArrayList<>();
        }

        adapter = new WhitelistAdapter(whitelist, this::removeContact);
        recyclerView.setAdapter(adapter);

        fab.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS);
            } else {
                launchContactPicker();
            }
        });

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        return view;
    }

    private void launchContactPicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        pickContactLauncher.launch(intent);
    }

    private void removeContact(WhitelistedContact contact) {
        int position = whitelist.indexOf(contact);
        if (position >= 0) {
            whitelist.remove(position);
            configManager.setWhitelist(whitelist);
            adapter.notifyItemRemoved(position);
            adapter.notifyItemRangeChanged(position, whitelist.size());
            Toast.makeText(getContext(), "Contact removed.", Toast.LENGTH_SHORT).show();
        }
    }

    private void filter(String text) {
        List<WhitelistedContact> filteredList = new ArrayList<>();
        for (WhitelistedContact item : whitelist) {
            if (item.getName().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(item);
            }
        }
        adapter.filterList(filteredList);
    }
}
