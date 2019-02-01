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

                        if (response != null && response.body() != null) {

                            for (int i = 0; i < response.body().size(); i++) {

                                PraktikanModel praktikanModel = response.body().get(i);
                                CreateQRCode(praktikanModel.getNim().toString(), praktikanModel.getNama(), praktikanModel.getKelas(), path);
                                //just testing purpose
                                Log.d("RESPONSE_CONTENT", response.body().get(i).toString());
                            }

                            progressDialog.dismiss();
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

    public void CreateQRCode(String qrCodeData, String nim, String kelas, String newPath){

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

            newPath = newPath+"/"+kelas;
            saveImage(addBorderToBitmap(mergeBitmaps(overlay,bitmap), 10, BLACK), qrCodeData, newPath, nim);

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

        int centreX = (int) ((canvasWidth  - overlay.getWidth()*0.22) /2);
        int centreY = (int) ((canvasHeight - overlay.getHeight()*0.22) /2);
        canvas.drawBitmap(Bitmap.createScaledBitmap(overlay,(int)(overlay.getWidth()*0.22), (int)(overlay.getHeight()*0.22), true), centreX, centreY, null);

        return combined;
    }

    void saveImage(Bitmap originalBitmap, String nim, String path, String nama) {
        File myDir = new File(path);
        if (!myDir.exists()) {
            myDir.mkdir();
        }
        String fname = nama.toUpperCase() + "-" + nim + ".jpg";
        File file = new File (myDir, fname);
        if (!file.exists ()){
            try {
                FileOutputStream out = new FileOutputStream(file);

                // NEWLY ADDED CODE STARTS HERE [
                Canvas canvas = new Canvas(originalBitmap);

                Paint paint = new Paint();
                paint.setColor(Color.RED); // Text Color
                paint.setTextSize(12); // Text Size
                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)); // Text Overlapping Pattern
                paint.setAntiAlias(true);

                canvas.drawBitmap(originalBitmap, 0, 0, paint);
                // NEWLY ADDED CODE ENDS HERE ]

                originalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                out.flush();
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    protected Bitmap addBorderToBitmap(Bitmap srcBitmap, int borderWidth, int borderColor){
        // Initialize a new Bitmap to make it bordered bitmap
        Bitmap dstBitmap = Bitmap.createBitmap(
                srcBitmap.getWidth() + borderWidth*2, // Width
                srcBitmap.getHeight() + borderWidth*2, // Height
                Bitmap.Config.ARGB_8888 // Config
        );

        /*
            Canvas
                The Canvas class holds the "draw" calls. To draw something, you need 4 basic
                components: A Bitmap to hold the pixels, a Canvas to host the draw calls (writing
                into the bitmap), a drawing primitive (e.g. Rect, Path, text, Bitmap), and a paint
                (to describe the colors and styles for the drawing).
        */
        // Initialize a new Canvas instance
        Canvas canvas = new Canvas(dstBitmap);

        // Initialize a new Paint instance to draw border
        Paint paint = new Paint();
        paint.setColor(borderColor);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(borderWidth);
        paint.setAntiAlias(true);

        Rect rect = new Rect(
                borderWidth / 2,
                borderWidth / 2,
                canvas.getWidth() - borderWidth / 2,
                canvas.getHeight() - borderWidth / 2
        );

        canvas.drawRect(rect,paint);

        // Draw source bitmap to canvas
        canvas.drawBitmap(srcBitmap, borderWidth, borderWidth, null);

        srcBitmap.recycle();

        // Return the bordered circular bitmap
        return dstBitmap;
    }
}
