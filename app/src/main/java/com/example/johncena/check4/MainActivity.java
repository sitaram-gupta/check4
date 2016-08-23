package com.example.johncena.check4;


import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;

import android.widget.TextView;
import android.widget.Toast;

import android.graphics.drawable.Drawable;

import java.util.ArrayList;
import java.util.List;

import android.widget.AdapterView.OnItemSelectedListener;

public class MainActivity extends Activity implements AdapterView.OnItemSelectedListener {
    Spinner spinner1, spinner2;
    private static final int RESULT_LOAD_IMG = 1;
    public Button b1;
    ImageButton imgButton;
    Button bc;
    public Button b;
    EditText editText;
    TextView textout;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private static int ACTIVITY_START_CAMERA_APP = 0;
    private final static int PICK_IMAGE = 12345;
    public static Bitmap yourSelectedImage;
    String imgDecodableString, ret;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spinner1 = (Spinner) findViewById(R.id.spin1);
        spinner1.setOnItemSelectedListener(this);
        List<String> categories = new ArrayList<String>();
        categories.add("ALBANIAN");
        categories.add("ARMENIAN");
        categories.add("AZERBAIJANI");
        categories.add("BELARUSIAN");
        categories.add("BULGARIAN");
        categories.add("CATALAN");
        categories.add("CROATIAN");
        categories.add("CZECH");
        categories.add("DANISH");
        categories.add("DUTCH");
        categories.add("ENGLISH");
        categories.add("ESTONIAN");
        categories.add("FINNISH");
        categories.add("FRENCH");
        categories.add("GERMAN");
        categories.add("GREEK");
        categories.add("HINDI");
        categories.add("HUNGARIAN");
        categories.add("ITALIAN");
        categories.add("LATVIAN");
        categories.add("LITHUANIAN");
        categories.add("MACEDONIAN");
        categories.add("NORWEGIAN");
        categories.add("POLISH");
        categories.add("PORTUGUESE");
        categories.add("ROMANIAN");
        categories.add("RUSSIAN");
        categories.add("SERBIAN");
        categories.add("SLOVAK");
        categories.add("SLOVENIAN");
        categories.add("SPANISH");
        categories.add("SWEDISH");
        categories.add("TURKISH");
        categories.add("UKRAINIAN");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner1.setAdapter(dataAdapter);


        spinner2 = (Spinner) findViewById(R.id.spin2);
        spinner2.setOnItemSelectedListener(this);
        List<String> categories1 = new ArrayList<String>();
        categories1.add("ALBANIAN");
        categories1.add("ARMENIAN");
        categories1.add("AZERBAIJANI");
        categories1.add("BELARUSIAN");
        categories1.add("BULGARIAN");
        categories1.add("CATALAN");
        categories1.add("CROATIAN");
        categories1.add("CZECH");
        categories1.add("DANISH");
        categories1.add("DUTCH");
        categories1.add("ENGLISH");
        categories1.add("ESTONIAN");
        categories1.add("FINNISH");
        categories1.add("FRENCH");
        categories1.add("GERMAN");
        categories1.add("GREEK");
        categories1.add("HINDI");
        categories1.add("HUNGARIAN");
        categories1.add("ITALIAN");
        categories1.add("LATVIAN");
        categories1.add("LITHUANIAN");
        categories1.add("MACEDONIAN");
        categories1.add("NORWEGIAN");
        categories1.add("POLISH");
        categories1.add("PORTUGUESE");
        categories1.add("ROMANIAN");
        categories1.add("RUSSIAN");
        categories1.add("SERBIAN");
        categories1.add("SLOVAK");
        categories1.add("SLOVENIAN");
        categories1.add("SPANISH");
        categories1.add("SWEDISH");
        categories1.add("TURKISH");
        categories1.add("UKRAINIAN");
        ArrayAdapter<String> dataAdapter1 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories1);
        dataAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner2.setAdapter(dataAdapter1);


        b1 = (Button) findViewById(R.id.b1);
        b1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                startActivityForResult(gallery, PICK_IMAGE);

            }


        });


        imgButton = (ImageButton) findViewById(R.id.imageButton);

        imgButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                Intent callCameraApplicationIntent = new Intent();
                callCameraApplicationIntent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(callCameraApplicationIntent, ACTIVITY_START_CAMERA_APP);


            }
        });


        b = (Button) findViewById(R.id.button);
        b.setOnClickListener(new View.OnClickListener() {

            public final String TAG = MainActivity.class.getSimpleName();
            ;

            @Override
            public void onClick(View view) {
                editText = (EditText) findViewById(R.id.ed1);
                textout = (TextView) findViewById(R.id.textView);
                final String input = editText.getText().toString();
                System.out.println("edittext: " + input);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Translate.setKey(ApiKeys.YANDEX_API_KEY);
                            String translation = Translate.execute(input, Language.ENGLISH, Language.HINDI);

                            // String translation = Translate.execute("Hello i am student.", Language.ENGLISH, Language.FRENCH);
                            System.out.println("Translation: " + translation);
                            textout.setText(translation);

                            // Log.i(TAG, "ON", );

                            System.out.println("output: " + translation);
                        } catch (Exception e) {
                            System.out.println("exception found");
                            // e.printStackTrace();
                            System.out.println(e.getMessage());
                            System.out.println("loccalized msg found");
                            System.out.println(e.getLocalizedMessage());
                            System.out.println("generic error");
                            System.out.println(e);
                        }
                    }
                }).start();
            }


        });


    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {


        if (requestCode == ACTIVITY_START_CAMERA_APP && resultCode == RESULT_OK) {
            Toast.makeText(this, "picture taken successfully", Toast.LENGTH_SHORT).show();

            super.onActivityResult(requestCode, resultCode, data);

        } else if (resultCode == RESULT_OK) {
            Uri uri = data.getData();
            String[] projection = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(projection[0]);
            String filePath = cursor.getString(columnIndex);
            cursor.close();

            yourSelectedImage = BitmapFactory.decodeFile(filePath);
            //Drawable d = new BitmapDrawable(yourSelectedImage);

            Toast.makeText(this, "Image picking Successful",
                    Toast.LENGTH_SHORT).show();
//                        super.onActivityResult(requestCode, resultCode, data);
            Intent i = new Intent(getApplicationContext(), CropActivity.class);
            startActivity(i);

        } else {
            Toast.makeText(this, "You haven't picked Image",
                    Toast.LENGTH_SHORT).show();
        }


    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
      /*  TextView myText= (TextView) view;
      Toast.makeText(this,"You Selected "+myText.getText(), Toast.LENGTH_SHORT).show();
      */
        if (view == spinner1) {
            String item1 = parent.getItemAtPosition(position).toString();

            Toast.makeText(parent.getContext(), "Selected: " + item1, Toast.LENGTH_LONG).show();


        }
        if (view == spinner2) {

            String item2 = parent.getItemAtPosition(position).toString();

            Toast.makeText(parent.getContext(), "Selected: " + item2, Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {


    }
}
