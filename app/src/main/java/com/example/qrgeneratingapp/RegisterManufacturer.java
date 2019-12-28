package com.example.qrgeneratingapp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.qrgeneratingapp.Extra.LoadingDialog;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;

public class RegisterManufacturer extends AppCompatActivity {
    private LoadingDialog loadingDialog;
    EditText txtName,txtEmail,txtPassword,txtCnic,txtCompId;
    Button btnRegister;

    DatabaseReference databaseReference;
    FirebaseAuth firebaseAuth;
    String Name , CNIC , Email, Password, CompanyID;
    private Uri imageUri=null;
    ImageView imageView;

    TextToSpeech ttobj;

    private static final int SELECT_PICTURE = 100;

    private static int RESULT_LOAD_IMAGE = 1;
    private String FullName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_manufacturer);
        loadingDialog = new LoadingDialog(this, R.style.DialogLoadingTheme);

        getSupportActionBar().setTitle("Register Manufacturer"); // for set actionbar title
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // for add back arrow in action bar

        txtName = findViewById(R.id.txtName);
        txtEmail = findViewById(R.id.txtEmail);
        txtCnic = findViewById(R.id.txtCNIC);
        txtCompId = findViewById(R.id.txtCompId);
        txtPassword = findViewById(R.id.txtPassword);
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

        databaseReference = FirebaseDatabase.getInstance().getReference("Manufacturer");
        firebaseAuth = FirebaseAuth.getInstance();
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

    public void LoginActivity(View v){
        Intent intent = new Intent(this,LoginManufacturer.class);
        startActivity(intent);
    }


    public void setImage(View v) {

        handlePermission();

        final CharSequence[] options = { "Camera Click", "From Gallery","Cancel" };



        AlertDialog.Builder builder = new AlertDialog.Builder(RegisterManufacturer.this);

        builder.setTitle("Upload Photo :)");

        builder.setItems(options, new DialogInterface.OnClickListener() {

            @Override

            public void onClick(DialogInterface dialog, int item) {

                if (options[item].equals("Camera Click"))

                {
                    //Playing audio
                    ttobj.speak("Opening Camera", TextToSpeech.QUEUE_FLUSH, null);
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                    File f = new File(android.os.Environment.getExternalStorageDirectory(), "temp.jpg");

                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                    //pic = f;

                    startActivityForResult(intent, 1);


                }

                else if (options[item].equals("From Gallery"))

                {
                    //Playing audio
                    ttobj.speak("Opening gallery", TextToSpeech.QUEUE_FLUSH, null);

                    Intent intent = new   Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                    startActivityForResult(intent, 2);



                }

                else if (options[item].equals("Cancel")) {

                    dialog.dismiss();

                }

            }

        });

        builder.show();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            if (requestCode == 1) {
                //h=0;
                File f = new File(Environment.getExternalStorageDirectory().toString());

                for (File temp : f.listFiles()) {

                    if (temp.getName().equals("temp.jpg")) {

                        f = temp;
                        File photo = new File(Environment.getExternalStorageDirectory(), "temp.jpg");
                        //pic = photo;
                        break;

                    }

                }

                try {

                    Bitmap bitmap;

                    BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();


                    bitmap = BitmapFactory.decodeFile(f.getAbsolutePath(),

                            bitmapOptions);

                    imageUri = getImageUri(this,bitmap);
                    Picasso.get().load(imageUri).transform(new CircleTransform()).into(imageView);



                    String path = android.os.Environment

                            .getExternalStorageDirectory()

                            + File.separator

                            + "Phoenix" + File.separator + "default";
                    //p = path;

                    f.delete();

                    OutputStream outFile = null;

                    File file = new File(path, String.valueOf(System.currentTimeMillis()) + ".jpg");

                    try {

                        outFile = new FileOutputStream(file);

                        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outFile);
                        //pic=file;
                        outFile.flush();

                        outFile.close();


                    } catch (FileNotFoundException e) {

                        e.printStackTrace();

                    } catch (IOException e) {

                        e.printStackTrace();

                    } catch (Exception e) {

                        e.printStackTrace();

                    }

                } catch (Exception e) {

                    e.printStackTrace();

                }

            } else if (requestCode == 2) {


                Uri selectedImage = data.getData();
                // h=1;
                //imgui = selectedImage;
                String[] filePath = {MediaStore.Images.Media.DATA};

                Cursor c = getContentResolver().query(selectedImage, filePath, null, null, null);

                c.moveToFirst();

                int columnIndex = c.getColumnIndex(filePath[0]);

                String picturePath = c.getString(columnIndex);

                c.close();

                Bitmap thumbnail = (BitmapFactory.decodeFile(picturePath));

                imageUri = getImageUri(this,thumbnail);
                Picasso.get().load(imageUri).transform(new CircleTransform()).into(imageView);

            }

        }
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
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
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case SELECT_PICTURE:
                for (int i = 0; i < permissions.length; i++) {
                    String permission = permissions[i];
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        boolean showRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, permission);
                        if (showRationale) {
                            //  Show your own message here
                        } else {
                            showSettingsAlert();
                        }
                    }
                }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    private void showSettingsAlert() {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Alert");
        alertDialog.setMessage("App needs to access the Camera.");
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "DONT ALLOW",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        //finish();
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "SETTINGS",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        openAppSettings(RegisterManufacturer.this);
                    }
                });
        alertDialog.show();
    }
    public static void openAppSettings(final Activity context) {
        if (context == null) {
            return;
        }
        final Intent i = new Intent();
        i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        i.addCategory(Intent.CATEGORY_DEFAULT);
        i.setData(Uri.parse("package:" + context.getPackageName()));
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        context.startActivity(i);
    }

    public void RegisterUserButton(View v){
        FullName = txtName.getText().toString();
        final String Email = txtEmail.getText().toString();
        String Password = txtPassword.getText().toString();
        CompanyID = txtCompId.getText().toString();
        CNIC = txtCnic.getText().toString();
        boolean flag = true;


        if(TextUtils.isEmpty(Email) || TextUtils.isEmpty(FullName) || TextUtils.isEmpty(Password) || TextUtils.isEmpty(CompanyID) || TextUtils.isEmpty(CNIC)){
                if(imageUri == null){
                    Toast.makeText(this, "All fields and image is required!",
                            Toast.LENGTH_LONG).show();

                    //Playing audio
                    ttobj.speak("All fields and image is required!", TextToSpeech.QUEUE_FLUSH, null);
                    flag = false;
                }
                else{
                    Toast.makeText(this, "All fields are required!",
                            Toast.LENGTH_LONG).show();

                    //Playing audio
                    ttobj.speak("All fields are required!", TextToSpeech.QUEUE_FLUSH, null);
                    flag = false;
                }


            }
            else{
                if(imageUri == null){
                    Toast.makeText(this, "Image is required!",
                            Toast.LENGTH_LONG).show();

                    //Playing audio
                    ttobj.speak("Image is required!", TextToSpeech.QUEUE_FLUSH, null);
                    flag = false;
                }
                else if(CNIC.length()!=13 || !CNIC.matches("[0-9]+") ){
                    Toast.makeText(this, "CNIC should be 13 digits!",
                            Toast.LENGTH_LONG).show();

                    //Playing audio
                    ttobj.speak("CNIC should be 13 digits!", TextToSpeech.QUEUE_FLUSH, null);
                    flag = false;
                }
                else{
                    boolean chk = isStringOnlyAlphabet(FullName);
                    if(chk == false){
                        Toast.makeText(this, "Name should be in Alpahbets",
                                Toast.LENGTH_LONG).show();

                        //Playing audio
                        ttobj.speak("Name should be in Alpahbets", TextToSpeech.QUEUE_FLUSH, null);
                        flag = false;
                    }
                }
            }







        if(flag){
            loadingDialog.show();
            firebaseAuth.createUserWithEmailAndPassword(Email, Password)
                    .addOnCompleteListener(RegisterManufacturer.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                UploadDataWithPic(user);
                            } else {
                                Toast.makeText(RegisterManufacturer.this, "Person already registered with this email",
                                        Toast.LENGTH_LONG).show();

                                //Playing audio
                                ttobj.speak("Person already registered with this email", TextToSpeech.QUEUE_FLUSH, null);
                                Log.i("failed", "createUserWithEmail:failure", task.getException());
                                loadingDialog.dismiss();

                            }

                            // ...
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    loadingDialog.dismiss();
                }
            });
        }

    }

    // Function to check String for only Alphabets
    public static boolean isStringOnlyAlphabet(String str)
    {
        if (str == null || str.equals("")) {
            return false;
        }
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            if ((!(ch >= 'A' && ch <= 'Z'))
                    && (!(ch >= 'a' && ch <= 'z'))) {
                return false;
            }
        }
        return true;
    }

    private void UploadDataWithPic(final FirebaseUser firebaseUser){
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
                Toast.makeText(RegisterManufacturer.this, "Manufacturer Registration Failed Image Upload error", Toast.LENGTH_SHORT).show();
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
                    Uri downUri = task.getResult();
                    DatabaseReference submitted = FirebaseDatabase.getInstance().getReference("Manufacturer");
                    Manufacturer user=new Manufacturer(FullName,firebaseUser.getEmail(),downUri.toString(),CNIC,CompanyID);
                    submitted.child(firebaseUser.getUid()).setValue(user);
                    //Playing audio
                    ttobj.speak("Welcome ", TextToSpeech.QUEUE_FLUSH, null);
                    //Playing audio
                    ttobj.speak("You are registered " + FullName, TextToSpeech.QUEUE_FLUSH, null);
                    Toast.makeText(RegisterManufacturer.this, "You are registered " + FullName, Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(RegisterManufacturer.this,LoginManufacturer.class));
                }
            }
        });
    }


}
