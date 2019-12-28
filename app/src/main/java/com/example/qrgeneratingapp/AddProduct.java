package com.example.qrgeneratingapp;

import android.app.DatePickerDialog;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.print.PrintHelper;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.zxing.WriterException;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import javax.crypto.spec.SecretKeySpec;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;
import androidmads.library.qrgenearator.QRGSaver;

public class AddProduct extends AppCompatActivity {

    EditText txtName,txtPrice,txtDetails,txtDate;
    Button btnAdd,btnDownload;
    ImageView img;
    Bitmap bitmap;
    static String TAG = "GenerateQRCode";
    String savePath = Environment.getExternalStorageDirectory().getPath() + "/QRCode/";
    String ProductId = "";
    DatabaseReference databaseReference;
    FirebaseAuth firebaseAuth;

    TextToSpeech ttobj;

    // Date Stuff

    final Calendar myCalendar = Calendar.getInstance();
    DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            // TODO Auto-generated method stub
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabel();
        }

    };

    // Encryption Stuff

    private static SecretKeySpec secretKey;
    private static byte[] key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        getSupportActionBar().setTitle("Add Product"); // for set actionbar title
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // for add back arrow in action bar

        txtName = findViewById(R.id.txtProductName);
        txtPrice = findViewById(R.id.txtProductPrice);
        txtDetails = findViewById(R.id.txtProductDetails);
        txtDate = findViewById(R.id.txtExpiryDate);
        btnAdd = findViewById(R.id.btnAdd);
        btnDownload = findViewById(R.id.btnDownload);
        img = findViewById(R.id.imgQrCode);

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
    }

    public void setDate(View v) {
        // TODO Auto-generated method stub
        new DatePickerDialog(this, date, myCalendar
                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)).show();

    }

    private void updateLabel() {
        String myFormat = "dd-MMM-yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        txtDate.setText(sdf.format(myCalendar.getTime()));
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

    public void Add(View v) throws Exception {
        boolean flag = true;

        String Name = txtName.getText().toString();
        String Price = txtPrice.getText().toString();
        String Details = txtDetails.getText().toString();
        String ExpiryDate = txtDate.getText().toString();
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        String ts = df.format(c);

        String ManufactureId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if(TextUtils.isEmpty(Name) || TextUtils.isEmpty(Price) || TextUtils.isEmpty(Details) || TextUtils.isEmpty(ExpiryDate)){
            Toast.makeText(this, "All fields are required!",
                    Toast.LENGTH_LONG).show();

            //Playing audio
            ttobj.speak("All fields are required!", TextToSpeech.QUEUE_FLUSH, null);
            flag = false;
        }

        if(flag){

            Product pro = new Product(Name,Price,Details,ManufactureId,ts,ExpiryDate);
            ProductId = databaseReference.push().getKey();
            databaseReference.child(ProductId).setValue(pro);

            WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
            Display display = manager.getDefaultDisplay();
            Point point = new Point();
            display.getSize(point);
            int width = point.x;
            int height = point.y;
            int smallerDimension = width < height ? width : height;
            smallerDimension = smallerDimension * 3 / 4;

            QRGEncoder qrgEncoder = new QRGEncoder(ProductId , null, QRGContents.Type.TEXT, smallerDimension);
            try {
                bitmap = qrgEncoder.encodeAsBitmap();
                img.setImageBitmap(bitmap);
                btnDownload.setVisibility(View.VISIBLE);
                img.setVisibility(View.VISIBLE);
            } catch (WriterException e) {
                Log.v(TAG, e.toString());
            }
            //Playing audio
            ttobj.speak("Product Added and QR code is generated!", TextToSpeech.QUEUE_FLUSH, null);
            Toast.makeText(AddProduct.this, "Product Added and QR code is generated!", Toast.LENGTH_SHORT).show();
        }



    }

    public void Save(View v){
        boolean save;
        String result;
        try{
            save = QRGSaver.save(savePath, ProductId, bitmap, QRGContents.ImageType.IMAGE_JPEG);
            result = save ? "QrCode Saved!" : "QrCode Not Saved!";
            Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
            finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void PrintIt(View v){


        PrintHelper photoPrinter = new PrintHelper(AddProduct.this);
        photoPrinter.setScaleMode(PrintHelper.SCALE_MODE_FIT);
        Bitmap bitmap = ((BitmapDrawable) img.getDrawable()).getBitmap();
        photoPrinter.printBitmap("QR_" + ProductId,bitmap);
    }


}
