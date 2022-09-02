package com.example.product;

import static com.example.product.RemoteService.BASE_URL;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    Retrofit retrofit;
    RemoteService service;
    List<ProductVO> array=new ArrayList<>();
    ProductAdapter adapter=new ProductAdapter();
    DecimalFormat df=new DecimalFormat("#,###원");
    ArrayList<String> arrDelete=new ArrayList<>();
    String word="";
    String order="recently";

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(@NonNull Menu menu) {
        switch (order){
            case "recently":
                menu.findItem(R.id.recently).setChecked(true);
                break;
            case "high":
                menu.findItem(R.id.high).setChecked(true);
                break;
            case "low":
                menu.findItem(R.id.low).setChecked(true);
                break;
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        AlertDialog.Builder box=new AlertDialog.Builder(this);
        switch (item.getItemId()){
            case R.id.recently: //최근상품순 정렬
                order="recently";
                onRestart();
                break;
            case R.id.low: //낮은가격순 정렬
                order="low";
                onRestart();
                break;
            case R.id.high: //높은 가격순 정렬
                order="high";
                onRestart();
                break;
            case R.id.delete:
                if(arrDelete.size()==0){
                    box.setTitle("메시지");
                    box.setMessage("삭제할 상품들을 선택하세요!");
                    box.setPositiveButton("확인", null);
                    box.show();
                }else{
                    box.setTitle("질의");
                    box.setMessage(arrDelete.size() + "개 상품을 삭제하실래요?");
                    box.setPositiveButton("예", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            for(String strCode:arrDelete){
                                int code=Integer.parseInt(strCode);
                                Call<Void> call=service.delete(code);
                                call.enqueue(new Callback<Void>() {
                                    @Override
                                    public void onResponse(Call<Void> call, Response<Void> response) {
                                        onRestart();
                                    }

                                    @Override
                                    public void onFailure(Call<Void> call, Throwable t) {

                                    }
                                });
                            }
                        }
                    });
                    box.setNegativeButton("아니오", null);
                    box.show();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        arrDelete.clear();
        Call<List<ProductVO>> call=service.list(word, order);
        call.enqueue(new Callback<List<ProductVO>>() {
            @Override
            public void onResponse(Call<List<ProductVO>> call, Response<List<ProductVO>> response) {
                array=response.body();
                System.out.println("데이터갯수:" + array.size());
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<List<ProductVO>> call, Throwable t) {
                System.out.println("오류................." + t.toString());
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermission();

        retrofit=new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        service=retrofit.create(RemoteService.class);
        onRestart();

        getSupportActionBar().setTitle("상품관리");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_home);

        RecyclerView list=findViewById(R.id.list);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(adapter);

        FloatingActionButton add=findViewById(R.id.add);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,
                        InsertActivity.class);
                startActivity(intent);
            }
        });

        ImageView search=findViewById(R.id.search);
        EditText edtWord=findViewById(R.id.word);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                word=edtWord.getText().toString();
                onRestart();
            }
        });

        edtWord.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                word=s.toString();
                onRestart();
            }
        });
    }

    //권한설정
    public void checkPermission() {
        String[] permissions= { Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA };
        ArrayList<String> noPermissions=new ArrayList<>();
        for(String permission:permissions){
            if(ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED){
                noPermissions.add(permission);
            }
        }

        if(noPermissions.size() > 0){
            String[] reqPermissions=noPermissions.toArray(new String[noPermissions.size()]);
            ActivityCompat.requestPermissions(this, reqPermissions, 100);
        }
    }

    //어댑터정의
    class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder>{
        @NonNull
        @Override
        public ProductAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view=getLayoutInflater().inflate(
                    R.layout.item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ProductAdapter.ViewHolder holder, int position) {
            ProductVO vo=array.get(position);
            holder.name.setText(vo.getName());
            holder.price.setText(df.format(vo.getPrice()));
            holder.chk.setChecked(false);

            String image=vo.getImage();
            if(image!=null && !image.equals("")){
                Picasso.with(MainActivity.this)
                        .load(BASE_URL + "/upload/" + vo.getImage())
                        .into(holder.image);
            }else{
                holder.image.setImageResource(R.drawable.ic_home);
            }

            holder.item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent(MainActivity.this,
                            ReadActivity.class);
                    intent.putExtra("code", vo.getCode());
                    startActivity(intent);
                }
            });

            holder.chk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String strCode=vo.getCode() + "";

                    if(holder.chk.isChecked()){
                        arrDelete.add(strCode);
                    }else{
                        arrDelete.remove(strCode);
                    }

                    System.out.println("선택상품수:" + arrDelete.size());
                }
            });
        }

        @Override
        public int getItemCount() {
            return array.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            ImageView image;
            TextView name, price;
            RelativeLayout item;
            CheckBox chk;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                image=itemView.findViewById(R.id.image);
                name=itemView.findViewById(R.id.name);
                price=itemView.findViewById(R.id.price);
                item=itemView.findViewById(R.id.item);
                chk=itemView.findViewById(R.id.chk);
            }
        }
    }
}