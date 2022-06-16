package com.example.firebase_image_recycalerview;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class show_images extends AppCompatActivity implements ImageAdapter.OnItemClickListener {
    RecyclerView recyclerView;
    ImageAdapter adapter;
    DatabaseReference databaseReference;
    FirebaseStorage storage;
    List<update_model> uploads;

    ValueEventListener DBListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_images);

        recyclerView = findViewById(R.id.recy);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        uploads = new ArrayList<>();
        adapter = new ImageAdapter(show_images.this, uploads);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(show_images.this);

        storage = FirebaseStorage.getInstance(); //get storage Referance

        databaseReference = FirebaseDatabase.getInstance().getReference("Uploads");  //get realtime Referance
      DBListener = databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                uploads.clear();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    update_model up_load = postSnapshot.getValue(update_model.class);
                    up_load.setKey(postSnapshot.getKey());
                    uploads.add(up_load);
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(show_images.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onItemClick(int position) {
        Toast.makeText(this, "Normal click at position: " + position, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeleteClick(int position) {
        update_model selectedItem = uploads.get(position);
        final String slectedkey = selectedItem.getKey();

        StorageReference imageRef = storage.getReferenceFromUrl(selectedItem.getImageUrl());
        imageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                databaseReference.child(slectedkey).removeValue();
                Toast.makeText(show_images.this, "Item Deleted", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onWhatEverClick(int position) {
        Toast.makeText(this, "Whatever click at position: " + position, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        databaseReference.removeEventListener(DBListener);
    }
}
