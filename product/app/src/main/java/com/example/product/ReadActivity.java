package com.example.product;

import static com.example.product.RemoteService.BASE_URL;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ReadActivity extends AppCompatActivity {
    int code;
    Retrofit retrofit;
    RemoteService service;
    ImageView image;
    EditText name, price;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert);
        Intent intent=getIntent();
        code=intent.getIntExtra("code", 0);

        getSupportActionBar().setTitle(code + ": 상품정보");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        image=findViewById(R.id.image);
        name=findViewById(R.id.name);
        price=findViewById(R.id.price);

        retrofit=new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        service=retrofit.create(RemoteService.class);
        Call<ProductVO> call=service.read(code);
        call.enqueue(new Callback<ProductVO>() {
            @Override
            public void onResponse(Call<ProductVO> call, Response<ProductVO> response) {
                ProductVO vo= response.body();
                name.setText(vo.getName());
                price.setText(vo.getPrice()+"");
                if(vo.getImage()!=null && !vo.getImage().equals("")){
                    Picasso.with(ReadActivity.this)
                            .load(BASE_URL + "/upload/" + vo.getImage())
                            .into(image);
                }else{
                    image.setImageResource(R.drawable.ic_home);
                }
            }

            @Override
            public void onFailure(Call<ProductVO> call, Throwable t) {

            }
        });

        Button save=findViewById(R.id.save);
        save.setText("수정하기");
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder box=new AlertDialog.Builder(ReadActivity.this);
                box.setTitle("질의");
                box.setMessage(code + "번 상품을 수정하실래요?");
                box.setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ProductVO vo=new ProductVO();
                        vo.setCode(code);
                        vo.setName(name.getText().toString());
                        int intPrice=Integer.parseInt(price.getText().toString());
                        vo.setPrice(intPrice);
                        Call<Void> call=service.update(vo);
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
                box.setNegativeButton("아니오", null);
                box.show();
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