package in.notyouraveragedev.tensor_image_classification;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.PathUtils;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.ml.common.FirebaseMLException;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.common.modeldownload.FirebaseModelManager;
import com.google.firebase.ml.custom.FirebaseCustomRemoteModel;
import com.google.firebase.ml.custom.FirebaseModelDataType;
import com.google.firebase.ml.custom.FirebaseModelInputOutputOptions;
import com.google.firebase.ml.custom.FirebaseModelInputs;
import com.google.firebase.ml.custom.FirebaseModelInterpreter;
import com.google.firebase.ml.custom.FirebaseModelInterpreterOptions;
import com.google.firebase.ml.custom.FirebaseModelOutputs;
import com.google.firebase.ml.modeldownloader.CustomModel;
import com.google.firebase.ml.modeldownloader.CustomModelDownloadConditions;
import com.google.firebase.ml.modeldownloader.DownloadType;
import com.google.firebase.ml.modeldownloader.FirebaseModelDownloader;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

import in.notyouraveragedev.tensor_image_classification.classifier.Classifier;
import in.notyouraveragedev.tensor_image_classification.classifier.ImageClassifier;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

/**
 * The Main Activity Class
 * <p>
 * Created by A Anand on 11-05-2020
 */
public class MainActivity extends AppCompatActivity{

    /**
     * Requests Codes to identify camera and permission requests
     */
    private static final int CAMERA_REQUEST_CODE = 10001;
    private static final int REQUEST_CODE = 123;

    /**
     * UI Elements
     */
    //private ImageClassifier imageClassifier;
    private Classifier classifier;
    private boolean capturedByCamera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_layout);
        Toast.makeText(MainActivity.this,"Firebase connection success",Toast.LENGTH_SHORT).show();
        // initalizing ui elements
        initializeUIElements();
    }

    /**
     * Method to initalize UI Elements. this method adds the on click
     */
    private void initializeUIElements() {
        Button uploadpicture = findViewById(R.id.upload_pic);
        Button takepicture = findViewById(R.id.take_pic);
        Button help = findViewById(R.id.help);

        /*
         * Creating an instance of our tensor image classifier
         */
        try {
            //imageClassifier = new ImageClassifier(this);
            classifier = Classifier.create(this, Classifier.Model.FLOAT, Classifier.Device.CPU,3);
        } catch (IOException e) {
            Log.e("Image Classifier Error", "ERROR: " + e);
        }

        // adding on click listener to button
        takepicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // checking whether camera permissions are available.
                // if permission is available, open the camera intent to get picture
                // otherwise request for permissions
                grantingPermissions();
            }
        });
        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent helpActivity = new Intent(MainActivity.this,Help.class);
                startActivity(helpActivity);
            }
        });
        uploadpicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent chooseImg = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                //chooseImg.setType("image/*");
                //chooseImg.setAction(Intent.ACTION_PICK);
                startActivityForResult(chooseImg,1);
                //startActivityForResult(Intent.createChooser(chooseImg, "Select Picture"),1);
            }
        });
    }

    public void grantingPermissions(){
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)
        + ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            //when permission is not granted
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.CAMERA) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this, WRITE_EXTERNAL_STORAGE)){
                //Create alert dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Grant those permission");
                builder.setMessage("Camera and Write Storage");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(
                                MainActivity.this,
                                new String[]{
                                        Manifest.permission.CAMERA,
                                        WRITE_EXTERNAL_STORAGE
                                },
                                REQUEST_CODE
                        );
                    }
                });
                builder.setNegativeButton("Cancel",null);
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }else{
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{
                                Manifest.permission.CAMERA,
                                WRITE_EXTERNAL_STORAGE
                        },
                        REQUEST_CODE
                        );
            }
        }else{
            //when permissions are already granted
            openCamera();
        }
    }

    String extension =null ;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // if this is the result of our camera image request
        try {
            if (requestCode == CAMERA_REQUEST_CODE) {
                // getting bitmap of the image
                Bitmap photo = (Bitmap) Objects.requireNonNull(Objects.requireNonNull(data).getExtras()).get("data");
                capturedByCamera = true;
                processImage(photo, 1);
            }
            if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "The selection is cancelled", Toast.LENGTH_LONG).show();
            }
            if (resultCode == RESULT_OK && requestCode == 1) {
                Uri selectedImage;
                try {
                    selectedImage = data.getData();
                    //convert the uri data to bitmap image
                    extension = getFileExtension(selectedImage);
                    Bitmap img = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                    Log.d("MainActivity", extension);
                    //perform the tensorflow image-classification on the selected image
                    processImage(img, 0);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }catch(Exception ex){ex.printStackTrace();}
        super.onActivityResult(requestCode, resultCode, data);
    }

    //retreive the extension of the image file
    private String getFileExtension(Uri uri) {
        String extension;
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        extension= mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
        return extension;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void processImage(Bitmap photo, int process_code) {
        if(process_code == 1) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);
            Date now = new Date();
            String fname = formatter.format(now) + ".jpg";
            MediaStore.Images.Media.insertImage(this.getContentResolver(), photo, fname, "Crop Disease ScreenShot");
        }

        // converting the bitmap image to byte to pass to the next intent:
        //referenced from: https://stackoverflow.com/questions/4989182/converting-java-bitmap-to-byte-array
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        if(capturedByCamera) {
            //compress the file in JPEG format
            photo.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        }else {
            //if the image extension is jpeg, then compress it into jpeg format
            if(extension.equals("jpg")) {
                photo.compress(Bitmap.CompressFormat.JPEG, 25, stream);
            }else if(extension.equals("png")) {
                photo.compress(Bitmap.CompressFormat.PNG, 25, stream);
            }
        }

        capturedByCamera = false;
        byte[] byteArray = stream.toByteArray();
        String img_encode = Base64.encodeToString(byteArray,Base64.DEFAULT);

        // pass this bitmap to classifier to make prediction

        List<Classifier.Recognition> predictions = classifier.recognizeImage(photo,0);

            // creating a list of string to display in list view
            final List<String> predictionsList = new ArrayList<>();
            final List<String> labels = new ArrayList<>();
            String p = "";
            int i = 0;
            for (Classifier.Recognition recog : predictions) {
                labels.add(recog.getTitle());
                predictionsList.add(recog.getTitle() + "  ::::::::::  " + recog.getConfidence());
                p += predictionsList.get(i) + "\n";
                i++;
            }

            String highLabel = formatLabel(labels.get(0));

            Data data = new Data(highLabel,img_encode,p);

            Bundle bundle = new Bundle();
            bundle.putSerializable("data",data);


            Intent showRes = new Intent(MainActivity.this,ShowResult.class);
            showRes.putExtras(bundle);

            startActivity(showRes);
    }

    private String formatLabel(String s) {
        String r="";
        for(int i = 0; i < s.length(); i++){
            //if the character is not a digit and is not a spacing
            if(!(Character.isDigit(s.charAt(i)) || s.charAt(i) == ' ')){
                r += s.charAt(i);
            }
        }
        return r;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_CODE){
            if((grantResults.length>0) &&
                    (grantResults[0] + grantResults[1] == PackageManager.PERMISSION_GRANTED)){
                openCamera();
            }else{
                grantingPermissions();
            }
        }
    }

    /**
     * creates and starts an intent to get a picture from camera
     */
    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
    }
}
