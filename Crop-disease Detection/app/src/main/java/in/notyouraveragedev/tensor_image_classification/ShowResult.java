package in.notyouraveragedev.tensor_image_classification;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;

public class ShowResult extends AppCompatActivity implements ValueEventListener {

    private ImageView imageView;
    private ListView listView;
    public static String image_encode;
    private Bundle bundle;
    String result;
    String prob;
    private TextView disease_info,produce_info,def_and_cause,solution,probability;
    FirebaseDatabase database;
    DatabaseReference dbref;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // initalizing ui elements
        database = FirebaseDatabase.getInstance();
        dbref = database.getReference("disease");
        initializeUIElements();
    }

    Data data;

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void initializeUIElements() {
        imageView = findViewById(R.id.iv_capture);
        disease_info = findViewById(R.id.disease_name);
        produce_info = findViewById(R.id.produce);
        def_and_cause = findViewById(R.id.def_cause);
        solution = findViewById(R.id.solution);
        probability = findViewById(R.id.probability);
        bundle = getIntent().getExtras();
        data = (Data) bundle.getSerializable("data");
        prob = data.getProbability();

        byte[] decodedByte = Base64.decode(data.getImage(),0);
        Bitmap img = BitmapFactory.decodeByteArray(decodedByte,0,decodedByte.length);
        imageView.setImageBitmap(img);

        probability.setText(prob);

        Toast.makeText(this,image_encode,Toast.LENGTH_SHORT).show();
        result = data.getName();
        result = result.toLowerCase();
        Toast.makeText(this,result,Toast.LENGTH_SHORT).show();
        //decode the string encoded data into image
        dbref.addValueEventListener(this);
        if(result != null) {
            disease_info.setText(result);
        }else{
            disease_info.setText("null");
        }
    }

    @Override
    public void onDataChange(@NonNull DataSnapshot snapshot) {
        Disease disease = snapshot.child(result).getValue(Disease.class);
        disease_info.setText(disease.getDisease_name());
        produce_info.setText(disease.getProduce_name());
        def_and_cause.setText(disease.getDef_and_cause());
        solution.setText(disease.getSolution());
    }

    @Override
    public void onCancelled(@NonNull DatabaseError error) {

    }
}
