package com.example.johncena.check4;

/**
 * Created by John Cena on 8/23/2016.
 */


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;

import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;

import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.ImageView;

import android.widget.Toast;

import com.example.johncena.interfaces.GrayScale;
import com.example.johncena.interfaces.Rotate;
import com.example.johncena.interfaces.SkewChecker;
import com.example.johncena.interfaces.Threshold;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;


public class CropActivity extends Activity implements View.OnClickListener, View.OnLongClickListener {

    private ImageView cropImView;
    private ClippingWindow clippingWindow;
    private ImageButton redoButton, cropButton;
    private Bitmap image;
    private Bitmap croppedImage;
    private Rect imageCoordinates;
    private ProgressDialog progressDialog;

    private  static NNMatrix weights_at_layer2, weights_at_layer3;
    private  static NNMatrix bias_at_layer2, bias_at_layer3;

    private ArrayList<Bitmap> componentBitmaps = new ArrayList<>();
   // private final ArrayList<NNMatrix> weightsArr = Splash.getWeights();

    private Bitmap bmResult;
    private String ocrResult;

    private final int BLACK = -16777216;
    private final int WHITE = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);
        instantiate();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cropBtn:
                crop();
                //changeIntent();
                break;

            case R.id.redoFromCrop:
                onBackPressed();
                break;


        }
    }

    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()) {
            case R.id.cropBtn:
                Toast.makeText(this, "Crop", Toast.LENGTH_LONG).show();
                break;
            case R.id.redoFromCrop:
                Toast.makeText(this, "Redo", Toast.LENGTH_LONG).show();
                break;

        }
        return true;
    }


    private void instantiate() {
        redoButton = (ImageButton) findViewById(R.id.redoFromCrop);
        cropButton = (ImageButton) findViewById(R.id.cropBtn);

        redoButton.setOnClickListener(this);
        cropButton.setOnClickListener(this);

        //image = BitmapFactory.decodeResource(getResources(), R.drawable.ntc_target_test);
        image = MainActivity.yourSelectedImage;
//        image = BitmapFactory.decodeResource(getResources(),R.drawable.test);
        cropImView = (ImageView) findViewById(R.id.imageView);
        cropImView.setImageBitmap(image);

        clippingWindow = (ClippingWindow) findViewById(R.id.clipping);


        ViewTreeObserver vto = cropImView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                Coordinates imageLocation = new ImageLocatorInImageview(cropImView);
                imageCoordinates = imageLocation.getCoordinates();
//                Log.d("abhishek", "abhishek");
                //clippingWindow = new ClippingWindow(getApplicationContext(), imageCoordinates);
                clippingWindow.initializeBoundary(imageCoordinates);
            }
        });


    }

    private void crop() {
        Coordinates bitmapCoordinates = new CoordinateLocatorInBitmap(cropImView, clippingWindow.getClippingWindowCoordinates());
        Rect bitmapClippingCoordinates = bitmapCoordinates.getCoordinates();
//        cropImView.setImageBitmap(croppedImage);
        Bitmap originalBitmap = ((BitmapDrawable) cropImView.getDrawable()).getBitmap();
        croppedImage = Bitmap.createBitmap(originalBitmap, bitmapClippingCoordinates.left, bitmapClippingCoordinates.top, bitmapClippingCoordinates.right - bitmapClippingCoordinates.left, bitmapClippingCoordinates.bottom - bitmapClippingCoordinates.top);
        cropImView.setImageBitmap(croppedImage);
        process();
    }

    public void process() {
        progressDialog = ProgressDialog.show(this, "", "Scanning", true);


//        progressBar = (ProgressBar) findViewById(R.id.progressBar);


        new MyTask().execute();
    }

    class MyTask extends AsyncTask<Void, Void, Void> {

        //private Bitmap thresholdedImage;

        @Override
        protected Void doInBackground(Void... params) {
            processImage();
            return null;
        }


        @Override
        protected void onPostExecute(Void params) {
//            progressBar.setVisibility(View.INVISIBLE);
            progressDialog.dismiss();
            Log.d("OcrResult", ocrResult);
            Intent mainIntent = new Intent(CropActivity.this, MainActivity.class).putExtra("ocrResult", ocrResult);
            setResult(RESULT_OK, mainIntent);
            CropActivity.this.finish();
        }

        public void processImage() {

            //Bitmap sourceBitmap = Bitmap.createBitmap(CropActivity.croppedImage);
            bmResult = gammaCorrect(croppedImage);
            //imageWriter.writeImage(bmResult, false, "aftergamma", "01_gamma");


            bmResult = grayScale(bmResult);
            //imageWriter.writeImage(bmResult, false, "aftergrayscale", "02_grayscale");
            bmResult = skewCorrect(bmResult);
            //imageWriter.writeImage(bmResult, false, "afterrotate", "03_rotate");

            //bmResult = medianFilter(bmResult);
            //imageWriter.writeImage(bmResult, false, "aftermedianfilter", "04_medianfilter");

            bmResult = threshold(bmResult);
            //imageWriter.writeImage(bmResult, false, "afterthreshold", "05_threshold");

            //thresholdedImage = bmResult;

            componentBitmaps = getSegmentArray(bmResult);

            List<double[][]> binarySegmentList = BinaryArray.CreateBinaryArray(componentBitmaps);
            //generateBinarySegmentedImages();

            ocrResult = generateOutput(binarySegmentList);
        }

        private Bitmap gammaCorrect(Bitmap bmp) {
            GammaCorrection gc = new GammaCorrection(1.0);
            bmp = gc.correctGamma(bmp);
            return bmp;
        }

        private Bitmap grayScale(Bitmap bmp) {
            GrayScale grayScale = new ITURGrayScale(bmp);
            bmp = grayScale.grayScale();
            return bmp;
        }

        private Bitmap skewCorrect(Bitmap bmp) {
            SkewChecker skewChecker = new HoughLineSkewChecker();
            double angle = skewChecker.getSkewAngle(bmp);

            Rotate rotator = new RotateNearestNeighbor(angle);
            bmp = rotator.rotateImage(bmp);
            return bmp;
        }

/*
        private Bitmap medianFilter(Bitmap bmp) {
            MedianFilter medianFilter = new NonLocalMedianFilter(3);
            bmp = medianFilter.applyMedianFilter(bmp);
            return bmp;
        }
*/

        private Bitmap threshold(Bitmap bmp) {
            Threshold threshold = new BradleyThreshold();
            bmp = threshold.threshold(bmp);
            return bmp;
        }

        private ArrayList<Bitmap> getSegmentArray(Bitmap bmp) {
            ArrayList<Bitmap> segments;
            bmp = PrepareImage.addBackgroundPixels(bmp);
            int height = bmp.getHeight();
            int width = bmp.getWidth();

//                        get value of pixels from binary image
            int[] pixels = createPixelArray(width, height, bmp);

//                       Create a binary array called booleanImage using pixel values in threshold bitmap
            boolean[] booleanImage = new boolean[width * height];


//                      false if pixel is a background pixel, else true
            int index = 0;
            for (int pixel : pixels) {
                if (pixel == BLACK) {
                    booleanImage[index] = true;
                }
                index++;
            }

            CcLabeling ccLabeling = new CcLabeling();
            ComponentImages componentImages = new ComponentImages(CropActivity.this);
            segments = componentImages.CreateComponentImages(ccLabeling.CcLabels(booleanImage, width));
            return segments;
        }

        private void generateBinarySegmentedImages() {
            List<int[]> binarySegmentList1D = BinaryArray.CreateBinaryArrayOneD(componentBitmaps);

            int counter = 0;
            for (int[] segment : binarySegmentList1D) {
                for (int i = 0; i < 256; i++) {
                    if (segment[i] == 1) segment[i] = WHITE;
                    else segment[i] = BLACK;
                }
                Bitmap bitmap = Bitmap.createBitmap(segment, 16, 16, Bitmap.Config.RGB_565);

            }

        }


        private int[] createPixelArray(int width, int height, Bitmap thresholdImage) {

            int[] pixels = new int[width * height];
            thresholdImage.getPixels(pixels, 0, width, 0, 0, width, height);
            return pixels;
        }


        private String generateOutput(List<double[][]> binarySegmentList) {
            JsonContentReader jsonContentReader = new JsonContentReader();
            String jsonContent = jsonContentReader.getJsonString(getApplicationContext());

            WeightReader weightReader = new WeightReader();
            bias_at_layer2 = new NNMatrix(weightReader.getWeights(jsonContent, "layer_1_bias"));
            weights_at_layer2 = new NNMatrix(weightReader.getWeights(jsonContent, "layer_1_weight"));
            weights_at_layer3 = new NNMatrix(weightReader.getWeights(jsonContent, "layer_2_weight"));
            bias_at_layer3 = new NNMatrix(weightReader.getWeights(jsonContent, "layer_2_bias"));

            String ocrString = " ";
            NeuralNetwork net = new NeuralNetwork(bias_at_layer2, bias_at_layer3, weights_at_layer2, weights_at_layer3);
            List<Integer> recognizedList = new ArrayList<Integer>();
            for (double[][] binarySegment : binarySegmentList) {
                NNMatrix input = new NNMatrix(binarySegment);
                NNMatrix output = net.FeedForward(input);
                //output.showOutputArray();
                int filteredOutput = output.filterOutput();

                if (filteredOutput != -1)
                    recognizedList.add(filteredOutput);


            }

            for (int a : recognizedList) {
                ocrString = ocrString + Integer.toString(a);
            }

            return ocrString;

        }

    }
}


