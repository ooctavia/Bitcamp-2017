package com.example.osbertoctavianus.tutor;

import android.Manifest;
import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.android.volley.Response;

import java.io.File;
import java.io.IOException;

import static android.R.id.progress;

public class DocumentGetter extends AppCompatActivity {

    private Button button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document_getter);

        button = (Button) findViewById(R.id.button);

         if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
             if(ActivityCompat.checkSelfPermission(this, Manifest.permission
                     .READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                 requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},100);
                 return;
             }
         }

        enable_button();/*This grants user permission*/
    }
       private void enable_button(){
           button.setOnClickListener(new View.OnClickListener(){
               @Override
                       public void onClick(View Object view;
               view){
                   new MaterialFilePicker() /*3:11*/
                           .withActivity(MainActivity.this)
                           .withRequestCode(10)
                           .start(); }

           } );
       }

       /*Requesting permission*/
       @Override
     public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
           if (requestCode == 100 && (grantResults[0]) == PackageManager.PERMISSION_GRANTED) {
               enable_button();
           } else {
               if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {/*Ask for permission again*/
                   requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
               }

           }
       }

       ProgressDialog progress ;/*this a spinning icon for wait to upload*/

       @Override
       protected void onActivityResult(int requestCode, int resultCode, final Intent data){
           if(requestCode == 10 && resultCode == RESULT_OK){

               progress = new ProgressDialog(MainActivity.this);
               progress.setTitle("Uploading");
               progress.setMessage("Please wait...");
               progress.show();;

               Thread t = new Thread(new Runnable(){
                   @Override
                   public void run(){
                       File f = new File(data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH));
                       String content_type = getMimeType(f.getPath());

                       String file_path = f.getAbsolutePath();
                       OkHttpClient client = new OkHttpClient(); /*Network request*/
                       RequestBody file_body = RequestBody.create(MediaType.parse(content_type),f);/*allows to upload a file regardless of size?*/

                       RequestBody request_body = new MultipartBody.Builder() /*Multipart request*/
                               .setType(MultipartBody.FORM)
                               .addFormDataPart("type",content_type)
                               .addFormDataPart("uploaded_file",file_path.substring(file_path.lastIndexOf("/")+1),file_body)/*To select the part we need*/
                               .build();
                       Request request = new Request.Builder()
                               .url("http://192.168.168.26/testing/save_file.php") /*url to the php script that will save the file on the web server*/
                               .post(request_body)
                               .build();


                       try{
                           Response response = client.newCall(request).execute();

                           if(!response.isSuccessful()){
                               throw new IOException("Error : "+response);
                           }
                              progress.dismiss();
                       } catch (IOException e){
                           e.printStackTrace();
                       }

                   }
               });
               t.start();
           }
       }

       private String getMimeType(String path){
           String extension = MimeTypeMap.getFileExtensionFromUrl(path);
           return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
       }
}
