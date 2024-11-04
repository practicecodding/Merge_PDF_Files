package com.hamidul.mergepdffiles;

import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfReader;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_PICK_PDF_FILES = 1;
    Button btnSelectPdf,btnMargePDF;
    List<Uri> mSelectedPdfs = new ArrayList<>();
    Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSelectPdf = findViewById(R.id.btnSelectPdf);
        btnMargePDF = findViewById(R.id.btnMargePDF);

        btnSelectPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // open the user gallery
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.setType("application/pdf");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);
                startActivityForResult(intent,REQUEST_PICK_PDF_FILES);

            }
        });

        btnMargePDF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mSelectedPdfs.isEmpty()){
                    setToast("Please select at least one PDF File");
                    return;
                }

                try {

                    String fileName = new SimpleDateFormat("HH:mm:ss dd-MMMM-yyy").format(new Date()) + ".pdf";

                    File mergePdfFile = new File(MainActivity.this.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName);

                    FileOutputStream fileOutputStream = new FileOutputStream(mergePdfFile);

                    Document document = new Document();
                    PdfCopy pdfCopy = new PdfCopy(document,fileOutputStream);
                    document.open();

                    for (Uri pdfUri : mSelectedPdfs){
                        PdfReader pdfReader = new PdfReader(getContentResolver().openInputStream(pdfUri));
                        pdfCopy.addDocument(pdfReader);
                        pdfReader.close();
                    }

                    document.close();
                    fileOutputStream.close();
                    setToast("PDF Files merge successfully");

                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    Uri fileUri = FileProvider.getUriForFile(MainActivity.this, getPackageName() + ".provider", mergePdfFile);
                    intent.setDataAndType(fileUri,"application/pdf");
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(intent);
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    setToast("Something Wrong");
                }

            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_PICK_PDF_FILES && resultCode == RESULT_OK){
            mSelectedPdfs.clear();

            if (data.getData() != null){
                mSelectedPdfs.add(data.getData());
            }
            else if (data.getClipData() != null){

                for (int i = 0; i<data.getClipData().getItemCount(); i++){
                    mSelectedPdfs.add(data.getClipData().getItemAt(i).getUri());
                }

            }

            setToast("Selected "+mSelectedPdfs.size()+" PDF Files");

        }


    }

    private void setToast(String text){
        if (toast!=null) toast.cancel();
        toast = Toast.makeText(MainActivity.this,text,Toast.LENGTH_SHORT);
        toast.show();
    }



}