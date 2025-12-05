package com.example.offdutycallfilter;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class WhitelistActivity extends AppCompatActivity {

    private WhitelistAdapter adapter;
    private ConfigManager configManager;
    private List<WhitelistedContact> whitelist;

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
        if (isGranted) {
            launchContactPicker();
        } else {
            Toast.makeText(this, "Read contacts permission is required to add contacts.", Toast.LENGTH_LONG).show();
        }
    });

    private final ActivityResultLauncher<Intent> pickContactLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
            Uri contactUri = result.getData().getData();
            if (contactUri != null) {
                Cursor cursor = getContentResolver().query(contactUri, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                    int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

                    if (nameIndex == -1 || numberIndex == -1) {
                        Toast.makeText(this, "Failed to retrieve contact information.", Toast.LENGTH_SHORT).show();
                        cursor.close();
                        return;
                    }

                    String name = cursor.getString(nameIndex);
                    String number = cursor.getString(numberIndex);

                    String normalizedNumber = number.replaceAll("[\\s\\-()]", "");

                    WhitelistedContact newContact = new WhitelistedContact(name, normalizedNumber);
                    whitelist.add(newContact);
                    configManager.setWhitelist(whitelist);
                    adapter.notifyItemInserted(whitelist.size() - 1);

                    cursor.close();
                }
            }
        }
    });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_whitelist);

        configManager = new ConfigManager(this);
        RecyclerView recyclerView = findViewById(R.id.recycler_view_whitelist);
        Button addFromContactsButton = findViewById(R.id.button_add_from_contacts);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        whitelist = configManager.getWhitelist();
        if (whitelist == null) {
            whitelist = new ArrayList<>();
        }

        adapter = new WhitelistAdapter(whitelist, this::removeContact);
        recyclerView.setAdapter(adapter);

        addFromContactsButton.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS);
            } else {
                launchContactPicker();
            }
        });
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
            Toast.makeText(this, "Contact removed.", Toast.LENGTH_SHORT).show();
        }
    }
}
