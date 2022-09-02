package com.example.product;

import static com.example.product.RemoteService.BASE_URL;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class InsertActivity extends AppCompatActivity {
    Retrofit retrofit;
    RemoteService service;
    ImageView image;
    String strImage="";
    EditText name,price;
    @SuppressLint("Range")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //앨범에서 사진 선택 후 해당 이미지 경로 및 파일명 읽어오기
        Cursor cursor=getContentResolver().query(data.getData(),null,null,null,null);
        cursor.moveToFirst();
        strImage=cursor.getString(
                cursor.getColumnIndex(MediaStore.Images.Media.DATA));
        image.setImageBitmap(BitmapFactory.decodeFile(strImage));
        System.out.println("11123123123................"+strImage);



    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert);

        getSupportActionBar().setTitle("상품등록");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        image=findViewById(R.id.image);
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent,1);
            }
        });
        name=findViewById(R.id.name);
        price=findViewById(R.id.price);
        //node mysql과 연결하는것
        retrofit=new Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create()).build();
        service=retrofit.create(RemoteService.class);

        //등록하기 버튼을 클릭한 경우
        Button save = findViewById(R.id.save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              String strName=name.getText().toString();
                String strPrice=price.getText().toString();

                if(strName=="" || strPrice==""|| strImage =="" ){
                    Toast.makeText(InsertActivity.this,"내용을 모두 넣어주세요",Toast.LENGTH_SHORT).show();
                    return;
                }

                RequestBody reqName= RequestBody.create(
                        MediaType.parse("multipart/form-data"), strName);
                RequestBody reqPrice= RequestBody.create(
                        MediaType.parse("multipart/form-data"), strPrice);
                //이미지 업로드
                File file=new File(strImage);
                RequestBody reqimage=RequestBody.create(
                        MediaType.parse("multipart/form-data"),file);

                MultipartBody.Part partImage=MultipartBody.Part.createFormData("image",file.getName(),reqimage);



                Call<Void> call=service.insert(reqName,reqPrice,partImage);
                call.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        finish();
                    }
                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {

                    }
                });



            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
        }
        return super.onOptionsItemSelected(item);
    }
}