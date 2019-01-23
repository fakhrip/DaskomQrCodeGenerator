package com.faitechno.www.daskomqrcodegenerator;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.codekidlabs.storagechooser.StorageChooser;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.WHITE;

public class MainActivity extends AppCompatActivity {

    EditText ipAddr;
    Button makeData, chooseDir;
    ProgressDialog progressDialog;
    String path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ipAddr = findViewById(R.id.ipaddr);
        chooseDir = findViewById(R.id.chooseDir);
        makeData = findViewById(R.id.make_data_button);

        makeData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                progressDialog = new ProgressDialog(MainActivity.this);
                progressDialog.setMessage("Loading....");
                progressDialog.show();

                GetDataService service = RetrofitClientInstance.getRetrofitInstance(ipAddr.getText().toString()).create(GetDataService.class);
                Call<List<PraktikanModel>> call = service.getAllPraktikan();
                call.enqueue(new Callback<List<PraktikanModel>>() {
                    @Override
                    public void onResponse(@Nullable Call<List<PraktikanModel>> call, @Nullable Response<List<PraktikanModel>> response) {
                        progressDialog.dismiss();
                        if (response != null && response.body() != null) {

                            CreateQRCode(response.body().get(0).getNim().toString());
//                            for (int i = 0; i < response.body().size(); i++) {
//
//                                //just testing purpose
//                                Log.d("RESPONSE_CONTENT", response.body().get(i).toString());
//                            }

                            Toast.makeText(MainActivity.this, "All Barcode Successfully Created", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@Nullable Call<List<PraktikanModel>> call, @Nullable Throwable t) {
                        progressDialog.dismiss();
                        Toast.makeText(MainActivity.this, "Something went wrong...Please try later!", Toast.LENGTH_SHORT).show();
                        Log.d("RESPONSE_CONTENT", t != null ? t.getLocalizedMessage() : null);
                    }
                });
            }
        });

        chooseDir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StorageChooser chooser = new StorageChooser.Builder()
                        .withActivity(MainActivity.this)
                        .withFragmentManager(getFragmentManager())
                        .withMemoryBar(false)
                        .allowCustomPath(true)
                        .setType(StorageChooser.DIRECTORY_CHOOSER)
                        .build();

                chooser.show();

                chooser.setOnSelectListener(new StorageChooser.OnSelectListener() {
                    @Override
                    public void onSelect(String newPath) {
                        path = newPath;
                        Toast.makeText(MainActivity.this, "Path changed to = "+path, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    public void CreateQRCode(String qrCodeData){

        int size = 1000;

        Map<EncodeHintType, ErrorCorrectionLevel> hintMap = new HashMap<>();
        hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);

        try {
            //generating qr code in bitmatrix type
            BitMatrix matrix = new MultiFormatWriter().encode(qrCodeData,
                    BarcodeFormat.QR_CODE, size, size, hintMap);
            //converting bitmatrix to bitmap
            int width = matrix.getWidth();
            int height = matrix.getHeight();
            int[] pixels = new int[width * height];
            // All are 0, or black, by default
            for (int y = 0; y < height; y++) {
                int offset = y * width;
                for (int x = 0; x < width; x++) {
                    //pixels[offset + x] = matrix.get(x, y) ? BLACK : WHITE;
                    pixels[offset + x] = matrix.get(x, y) ? BLACK : WHITE;
                }
            }

            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, size, 0, 0, width, height);
            //setting bitmap to image view

            Bitmap overlay = BitmapFactory.decodeResource(getResources(), R.drawable.daskom);

            saveImage(mergeBitmaps(overlay,bitmap), qrCodeData, path);

        } catch (Exception er){
            Log.e("QrGenerate",er.getMessage());
        }
    }

    public Bitmap mergeBitmaps(Bitmap overlay, Bitmap bitmap) {

        int height = bitmap.getHeight();
        int width = bitmap.getWidth();

        Bitmap combined = Bitmap.createBitmap(width, height, bitmap.getConfig());
        Canvas canvas = new Canvas(combined);
        int canvasWidth = canvas.getWidth();
        int canvasHeight = canvas.getHeight();

        canvas.drawBitmap(bitmap, new Matrix(), null);

        int centreX = (int) ((canvasWidth  - overlay.getWidth()*0.2) /2);
        int centreY = (int) ((canvasHeight - overlay.getHeight()*0.2) /2);
        canvas.drawBitmap(Bitmap.createScaledBitmap(overlay,(int)(overlay.getWidth()*0.2), (int)(overlay.getHeight()*0.2), true), centreX, centreY, null);

        return combined;
    }

    void saveImage(Bitmap originalBitmap, String nim, String path) {
        File myDir = new File(path);
        String fname = "Image-"+ nim +".jpg";
        File file = new File (myDir, fname);
        if (file.exists ()) file.delete ();
        try {
            FileOutputStream out = new FileOutputStream(file);

            // NEWLY ADDED CODE STARTS HERE [
            Canvas canvas = new Canvas(originalBitmap);

            Paint paint = new Paint();
            paint.setColor(Color.RED); // Text Color
            paint.setTextSize(12); // Text Size
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)); // Text Overlapping Pattern
            paint.setAntiAlias(true);
            // some more settings...

            canvas.drawBitmap(originalBitmap, 0, 0, paint);
            canvas.drawText(nim, 10, 10, paint);
            // NEWLY ADDED CODE ENDS HERE ]

            originalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
