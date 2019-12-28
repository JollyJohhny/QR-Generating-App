package com.example.qrgeneratingapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.qrgeneratingapp.Extra.LoadingDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Locale;

public class ViewAllProducts extends AppCompatActivity {

    ListView listView ;
    public static ArrayList<ListType> AllProducts;
    ProductAdapter myAdapter;
    String ManuId;
    private LoadingDialog loadingDialog;

    DatabaseReference databaseReference;
    FirebaseAuth firebaseAuth;
    int i = 0;

    TextToSpeech ttobj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_all_products);

        getSupportActionBar().setTitle("All Products List"); // for set actionbar title
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // for add back arrow in action bar

        loadingDialog = new LoadingDialog(this, R.style.DialogLoadingTheme);
        loadingDialog.show();


        listView = findViewById(R.id.ListId);


        databaseReference = FirebaseDatabase.getInstance().getReference("Products");
        firebaseAuth = FirebaseAuth.getInstance();
        ManuId= FirebaseAuth.getInstance().getCurrentUser().getUid();


        //Initializing Text to Speech
        ttobj=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    ttobj.setLanguage(Locale.UK);
                }
            }
        });

        ShowProducts();
    }

    public void ShowProducts(){
        AllProducts = new ArrayList<ListType>();
        AddProductsToList();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showPopup(view,position);
            }
        });
    }

    public void AddProductsToList(){
        AllProducts = new ArrayList<ListType>();

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String ManuIdGet = snapshot.child("manufactureId").getValue(String.class);
                    if(ManuIdGet.equals(ManuId)){
                        String name = snapshot.child("name").getValue(String.class);
                        String time = snapshot.child("timeStamp").getValue(String.class);
                        String proId = snapshot.getKey();

                        ListType Product = new ListType(name,proId,time);
                        AllProducts.add(Product);


                    }
                }
                Show();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("The read failed: " ,databaseError.getMessage());
            }

        });
    }

    public void Show(){
        myAdapter = new ProductAdapter(this, AllProducts);
        listView.setAdapter(myAdapter);

        if(AllProducts.size() == 0 ){
            //Playing audio
            ttobj.speak("No product added yet!", TextToSpeech.QUEUE_FLUSH, null);
            Toast.makeText(ViewAllProducts.this, "No product added yet!", Toast.LENGTH_SHORT).show();

        }

        loadingDialog.dismiss();
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

    public void AddProduct(View v){
        Intent intent = new Intent(this, AddProduct.class);
        startActivity(intent);
    }

    public void showPopup(View v,final int i){

        final String ProductId = AllProducts.get(i).getId();
        PopupMenu popup= new PopupMenu(this,v);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.edit:
                        Intent intent1 = new Intent(getBaseContext(), UpdateProduct.class);
                        intent1.putExtra("PRODUCTID", ProductId);
                        startActivity(intent1);
                        return true;
                    case R.id.del:
                        //Playing audio
                        ttobj.speak("Do you really want to delete this product?", TextToSpeech.QUEUE_FLUSH, null);
                        AlertDialog.Builder builder = new AlertDialog.Builder(ViewAllProducts.this);
                        builder.setCancelable(true);
                        builder.setTitle("Confirmation");
                        builder.setMessage("Do you really want to delete this product?");
                        builder.setPositiveButton("Confirm",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        delete(ProductId);
                                    }

                                });
                        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                        return true;
                    case R.id.view:
                        Intent intent2 = new Intent(getBaseContext(), ViewProduct.class);
                        intent2.putExtra("PRODUCTID", ProductId);
                        startActivity(intent2);
                        return true;
                    default:
                        return false;

                }
            }
        });
        popup.inflate(R.menu.popup_menu);
        popup.show();


    }

    public void delete(String id){
        databaseReference.child(id).removeValue();
        //Playing audio
        ttobj.speak("Product deleted!", TextToSpeech.QUEUE_FLUSH, null);
        Toast.makeText(ViewAllProducts.this, "Product deleted!", Toast.LENGTH_SHORT).show();
        Intent intent2 = new Intent(getBaseContext(), ViewAllProducts.class);
        startActivity(intent2);

    }


}
