package com.example.qrgeneratingapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.net.Uri;
import android.os.Build;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.qrgeneratingapp.Extra.LoadingDialog;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.util.Locale;

public class UpdateProfile extends AppCompatActivity {

    String UserId;
    EditText txtName,txtEmail,txtPassword,txtCompId,txtCnic;
    Button btnRegister;

    private LoadingDialog loadingDialog;

    DatabaseReference databaseReference;
    FirebaseAuth firebaseAuth;
    private Uri imageUri=null;
    ImageView imageView;
    boolean imgChk = false;
    TextToSpeech ttobj;


    Uri downUri;

    private static final int SELECT_PICTURE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);



        loadingDialog = new LoadingDialog(this, R.style.DialogLoadingTheme);
        UserId = getIntent().getStringExtra("USERID");

        getSupportActionBar().setTitle("Update Profile"); // for set actionbar title
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // for add back arrow in action bar

        txtName = findViewById(R.id.txtName);
        txtCompId = findViewById(R.id.txtCompId);
        txtCnic = findViewById(R.id.txtCnic);
        btnRegister = findViewById(R.id.btnLogin);
        imageView = findViewById(R.id.imageView);

        //Initializing Text to Speech
        ttobj=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    ttobj.setLanguage(Locale.UK);
                }
            }
        });


        FirebaseDatabase.getInstance().getReference("Manufacturer/"+FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("ima")){
//                    Picasso.get().load(dataSnapshot.child("ima").getValue(String.class)).into((ImageView) findViewById(R.id.UserImage));

                    Picasso.get().load(dataSnapshot.child("ima").getValue(String.class)).transform(new CircleTransform()).into(imageView);

                }
                String name = dataSnapshot.child("fullName").getValue(String.class);
                String cnic = dataSnapshot.child("cnic").getValue(String.class);
                String compId = dataSnapshot.child("companyId").getValue(String.class);


                txtName.setText(name);
                txtCnic.setText(cnic);
                txtCompId.setText(compId);


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


    public class CircleTransform implements Transformation {
        @Override
        public Bitmap transform(Bitmap source) {
            int size = Math.min(source.getWidth(), source.getHeight());

            int x = (source.getWidth() - size) / 2;
            int y = (source.getHeight() - size) / 2;

            Bitmap squaredBitmap = Bitmap.createBitmap(source, x, y, size, size);
            if (squaredBitmap != source) {
                source.recycle();
            }

            Bitmap bitmap = Bitmap.createBitmap(size, size, source.getConfig());

            Canvas canvas = new Canvas(bitmap);
            Paint paint = new Paint();
            BitmapShader shader = new BitmapShader(squaredBitmap,
                    Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            paint.setShader(shader);
            paint.setAntiAlias(true);

            float r = size / 2f;
            canvas.drawCircle(r, r, r, paint);

            squaredBitmap.recycle();
            return bitmap;
        }

        @Override
        public String key() {
            return "circle";
        }
    }

    public void Update(View v){

        databaseReference = FirebaseDatabase.getInstance().getReference("Manufacturer");

        try {
            databaseReference.child(UserId).child("fullName").setValue(txtName.getText().toString());
            databaseReference.child(UserId).child("cnic").setValue(txtCnic.getText().toString());
            databaseReference.child(UserId).child("companyId").setValue(txtCompId.getText().toString());

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if(imgChk == true){
                UploadPic(user);
            }
            else {

                //Playing audio
                ttobj.speak("Profile Updated!", TextToSpeech.QUEUE_FLUSH, null);
                Toast.makeText(UpdateProfile.this, "Profile Updated!", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(UpdateProfile.this,ManufacturerProfile.class);
                startActivity(intent);
            }



        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void UploadPic(final FirebaseUser firebaseUser){
        loadingDialog.show();
        final StorageReference mStorageRef = FirebaseStorage.getInstance().getReference("ManufacturerImages/"+ System.currentTimeMillis());
        final StorageReference riversRef = mStorageRef.child("profile");
        riversRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                loadingDialog.dismiss();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                loadingDialog.dismiss();
                Toast.makeText(UpdateProfile.this, "Manufacturer Registration Failed Image Upload error", Toast.LENGTH_SHORT).show();
            }
        }).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()){
                    throw task.getException();
                }
                return riversRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()){
                    downUri = task.getResult();

                    databaseReference.child(UserId).child("ima").setValue(downUri.toString());
                    loadingDialog.dismiss();

                    //Playing audio
                    ttobj.speak("Profile Updated", TextToSpeech.QUEUE_FLUSH, null);
                    Toast.makeText(UpdateProfile.this, "Profile Updated!", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(UpdateProfile.this,ManufacturerProfile.class);
                    startActivity(intent);
                }
            }
        });
    }

    private void handlePermission() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //ask for permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    SELECT_PICTURE);
        }
    }


    public void setImage(View v){
        imgChk = true;
        handlePermission();
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && null != data) {
            Uri selectedImage = data.getData();
            imageUri=selectedImage;
//            Toast.makeText(this, selectedImage.toString(),
//                    Toast.LENGTH_LONG).show();

//            Picasso.get().load(selectedImage).into(imageView);
            Picasso.get().load(selectedImage).transform(new CircleTransform()).into(imageView);

        }


    }


}
