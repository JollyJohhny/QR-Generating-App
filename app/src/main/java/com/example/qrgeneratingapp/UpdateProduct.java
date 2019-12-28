package com.example.qrgeneratingapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;

public class UpdateProduct extends AppCompatActivity {
    EditText txtName,txtPrice,txtDetails;
    Button btnSave;
    ImageView img;
    Bitmap bitmap;
    String TAG = "GenerateQRCode";
    String ProductId = "";
    DatabaseReference databaseReference;
    FirebaseAuth firebaseAuth;

    TextToSpeech ttobj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_product);

        getSupportActionBar().setTitle("Update Product"); // for set actionbar title
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // for add back arrow in action bar

        txtName = findViewById(R.id.txtProductName);
        txtPrice = findViewById(R.id.txtProductPrice);
        txtDetails = findViewById(R.id.txtProductDetails);
        btnSave = findViewById(R.id.btnAdd);
        img = findViewById(R.id.imgQrCode);

        Intent intent = getIntent();
        ProductId= intent.getStringExtra("PRODUCTID");

        databaseReference = FirebaseDatabase.getInstance().getReference("Products");
        firebaseAuth = FirebaseAuth.getInstance();

        //Initializing Text to Speech
        ttobj=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    ttobj.setLanguage(Locale.UK);
                }
            }
        });


        databaseReference.child(ProductId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue(String.class);
                String price = dataSnapshot.child("price").getValue(String.class);
                String details = dataSnapshot.child("details").getValue(String.class);


                txtName.setText(name);
                txtPrice.setText(price);
                txtDetails.setText(details);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void Update(View v){


        try {
            databaseReference.child(ProductId).child("name").setValue(txtName.getText().toString());
            databaseReference.child(ProductId).child("price").setValue(txtPrice.getText().toString());
            databaseReference.child(ProductId).child("details").setValue(txtDetails.getText().toString());

            //Playing audio
            ttobj.speak("Product Updated!", TextToSpeech.QUEUE_FLUSH, null);
            Toast.makeText(UpdateProduct.this, "Product Updated!", Toast.LENGTH_SHORT).show();

            finish();




        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
