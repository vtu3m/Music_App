package com.example.MyMusicccc.Activity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.example.MyMusicccc.Adapter.MainViewPagerAdapter;
import com.example.MyMusicccc.Fragment.Fragment_Profile;
import com.example.MyMusicccc.Fragment.Fragment_Thu_Vien;
import com.example.MyMusicccc.Fragment.Fragment_Tim_Kiem;
import com.example.MyMusicccc.Fragment.Fragment_Trang_Chu;
import com.example.MyMusicccc.Fragment.LoadingDialog;
import com.example.MyMusicccc.R;
import com.example.MyMusicccc.Service_Local.ForegroundServiceControl;
import com.google.android.material.tabs.TabLayout;

public class HomeActivity extends AppCompatActivity {
    private SQLiteDatabase db;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private String taikhoan, matkhau, name, email, url;
    private long backPressTime;
    private Toast mToast;
    static int checkkhach = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        db = openOrCreateDatabase("NguoiDung.db", MODE_PRIVATE, null);
        getData();
        AnhXa();
        final LoadingDialog loadingDialog = new LoadingDialog(HomeActivity.this);
        loadingDialog.show();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadingDialog.dismiss();
            }
        }, 7500);
        init();
        overridePendingTransition(R.anim.anim_intent_in_home, R.anim.anim_intent_out);
    }

    @Override
    public void onBackPressed() {
        if (backPressTime + 2000 > System.currentTimeMillis()){
            mToast.cancel();
            Intent intent = new Intent(getApplicationContext(), KhoiDongActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("EXIT", true);
            startActivity(intent);
            finish();
            System.exit(0);
        }else {
            mToast = Toast.makeText(HomeActivity.this, "Ấn lần nữa để thoát", Toast.LENGTH_SHORT);
            mToast.show();
        }
        backPressTime = System.currentTimeMillis();
    }
    public void StopService(){
        Intent intent = new Intent(this, ForegroundServiceControl.class);
        stopService(intent);
    }
    private void init() {
        if (checkkhach ==1) {
            MainViewPagerAdapter mainViewPagerAdapter = new MainViewPagerAdapter(getSupportFragmentManager());
            mainViewPagerAdapter.addFragment(new Fragment_Trang_Chu(), "");
            mainViewPagerAdapter.addFragment(new Fragment_Tim_Kiem(), "");
            mainViewPagerAdapter.addFragment(new Fragment_Thu_Vien(), "");
            mainViewPagerAdapter.addFragment(new Fragment_Profile(), "");
            viewPager.setAdapter(mainViewPagerAdapter);
            tabLayout.setupWithViewPager(viewPager);
            tabLayout.getTabAt(0).setIcon(R.drawable.icontrangchu);
            tabLayout.getTabAt(1).setIcon(R.drawable.icontimkiem);
            tabLayout.getTabAt(2).setIcon(R.drawable.iconthuvien);
            tabLayout.getTabAt(3).setIcon(R.drawable.iconlogo);
        }else if (checkkhach == 0){
            MainViewPagerAdapter mainViewPagerAdapter = new MainViewPagerAdapter(getSupportFragmentManager());
            mainViewPagerAdapter.addFragment(new Fragment_Trang_Chu(), "");
            mainViewPagerAdapter.addFragment(new Fragment_Tim_Kiem(), "");
            //mainViewPagerAdapter.addFragment(new Fragment_Thu_Vien(), "");
            mainViewPagerAdapter.addFragment(new Fragment_Profile(), "");
            viewPager.setAdapter(mainViewPagerAdapter);
            tabLayout.setupWithViewPager(viewPager);
            tabLayout.getTabAt(0).setIcon(R.drawable.icontrangchu);
            tabLayout.getTabAt(1).setIcon(R.drawable.icontimkiem);
           // tabLayout.getTabAt(2).setIcon(R.drawable.iconthuvien);
            tabLayout.getTabAt(2).setIcon(R.drawable.iconlogo);

        }
    }
    private void AnhXa() {
        tabLayout = findViewById(R.id.myTabLayout);
        viewPager = findViewById(R.id.myViewPager);
    }
    public void getData() {
        String sql = "SELECT * FROM tbNguoiDung";
        Cursor cursor = db.rawQuery(sql, null);
        cursor.moveToLast();
        if (!cursor.isAfterLast() && !(cursor.getString(1).equals("khach"))){
            checkkhach = 1;
            taikhoan = cursor.getString(1);
            matkhau = cursor.getString(2);
            name = cursor.getString(3);
            email = cursor.getString(4);
            url = cursor.getString(5);
        } else {
            checkkhach = 0;
            taikhoan = cursor.getString(1);
            matkhau = cursor.getString(2);
            name = cursor.getString(3);
            email = cursor.getString(4);
            url = cursor.getString(5);

        }
    }
    public void updateHome(){
        getData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        StopService();
        finish();
    }

    public String getTaikhoan() {
        return taikhoan;
    }

    public String getMatkhau() {
        return matkhau;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getUrl() {
        return url;
    }
}