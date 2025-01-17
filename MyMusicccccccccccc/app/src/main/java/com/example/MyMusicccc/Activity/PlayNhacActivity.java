package com.example.MyMusicccc.Activity;

import static android.net.wifi.WifiConfiguration.Status.strings;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewpager.widget.ViewPager;

import com.example.MyMusicccc.Adapter.BangXepHangAdapter;
import com.example.MyMusicccc.Adapter.ViewPagerDiaNhac;
import com.example.MyMusicccc.Fragment.Fragment_dia_nhac;
import com.example.MyMusicccc.Model.BaiHatModel;
import com.example.MyMusicccc.Model.BaiHatThuVienPlayListModel;
import com.example.MyMusicccc.Model.BaiHatYeuThichModel;
import com.example.MyMusicccc.Model.BangXepHangModel;
import com.example.MyMusicccc.Model.ResponseModel;
import com.example.MyMusicccc.Model.ThuVienPlayListModel;
import com.example.MyMusicccc.R;
import com.example.MyMusicccc.Service_API.APIService;
import com.example.MyMusicccc.Service_API.Dataservice;
import com.example.MyMusicccc.Service_Local.ForegroundServiceControl;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlayNhacActivity extends AppCompatActivity {
    private SQLiteDatabase db;
    private androidx.appcompat.widget.Toolbar toolbarplaynhac;
    private SeekBar seekBarnhac;
    private ImageView imageViewtim, nutshare;
    private TextView textViewtennhac, textViewcasi, textViewrunrime, textViewtatoltime;
    private ImageButton imageButtontronnhac, imageButtonpreviewnhac, imageButtonplaypausenhac, imageButtonnexnhac, imageButtonlapnhac;
    private int dem = 0, position = 0, duration = 0, timeValue = 0, durationToService = 0;
    private boolean repeat = false, checkrandom = false, isplaying;
    private static ArrayList<BaiHatModel> mangbaihat = new ArrayList<>();
    private static ArrayList<BaiHatThuVienPlayListModel> mangbaihetthuvienplaylist = new ArrayList<>();
    private static final ArrayList<BaiHatYeuThichModel> mangbaihatyeuthich = new ArrayList<>();
    private String taikhoan;
    private Fragment_dia_nhac fragment_dia_nhac;
    private static ViewPagerDiaNhac adapternhac;


    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        if (intent != null){
            isplaying = intent.getBooleanExtra("status_player", false);
            int action = intent.getIntExtra("action_music", 0);
            duration = intent.getIntExtra("duration_music", 0);
            timeValue = intent.getIntExtra("seektomusic", 0);
            position = intent.getIntExtra("position_music", 0);
            seekBarnhac.setProgress(timeValue);
            @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss");
            textViewrunrime.setText(simpleDateFormat.format(timeValue));
            handleMusic(action);
            TimeSong();
        }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_nhac);
        db = openOrCreateDatabase("NguoiDung.db", MODE_PRIVATE, null);
        GetDataFromIntent();
        getDataSQLite();
        AnhXa();
        enventClick();
        setViewStart();
        StartService();
        overridePendingTransition(R.anim.anim_intent_in, R.anim.anim_intent_out);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,
                new IntentFilter("send_data_to_activity"));
    }

    private void StartService() {
        Intent intent =  new Intent(this, ForegroundServiceControl.class);
        if (mangbaihat.size() > 0){
            intent.putExtra("obj_song_baihat", mangbaihat);
        }else if (mangbaihatyeuthich.size() > 0){
            intent.putExtra("obj_song_yeuthich", mangbaihatyeuthich);
        }else if (mangbaihetthuvienplaylist.size() > 0){
            intent.putExtra("obj_song_thuvien", mangbaihetthuvienplaylist);
        }
        startService(intent);
    }
    private void insertYeuThich(String un, int idbh, String tbh, String tcs, String hbh, String lbh) {
        Dataservice dataservice = APIService.getService();
        Call<ResponseModel> callback = dataservice.insertyeuthich(un, idbh, tbh, tcs, hbh, lbh);
        callback.enqueue(new Callback<ResponseModel>() {
            @Override
            public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) {
            }

            @Override
            public void onFailure(Call<ResponseModel> call, Throwable t) {
            }
        });
    }
    private void deleteYeuThich(String un, int idbh) {
        Dataservice dataservice = APIService.getService();
        Call<ResponseModel> callback = dataservice.deleteyeuthich(un, idbh);
        callback.enqueue(new Callback<ResponseModel>() {
            @Override
            public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) {
            }
            @Override
            public void onFailure(Call<ResponseModel> call, Throwable t) {
            }
        });
    }
    private void checkYeuThich(String un, int idbh) {
        Dataservice dataservice = APIService.getService();
        Call<ResponseModel> callback = dataservice.checkyeuthich(un, idbh);
        callback.enqueue(new Callback<ResponseModel>() {
            @Override
            public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) {
                ResponseModel responseBody = response.body();
                if (responseBody != null) {
                    if (responseBody.getSuccess().equals("1")) {
                        dem = 1;
                        imageViewtim.setImageResource(R.drawable.iconloved);
                    } else {
                        dem = 0;
                        imageViewtim.setImageResource(R.drawable.iconlove);
                    }
                }
            }
            @Override
            public void onFailure(Call<ResponseModel> call, Throwable t) {
            }
        });
    }
    private void enventClick() {
        imageViewtim.setOnClickListener(view -> {
            if (dem == 0){
                Animation animation = AnimationUtils.loadAnimation(PlayNhacActivity.this, R.anim.anim_timclick);
                imageViewtim.setImageResource(R.drawable.iconloved);
                view.startAnimation(animation);
                if (mangbaihat.size() > 0){
                    insertYeuThich(taikhoan, mangbaihat.get(position).getIdBaiHat(), mangbaihat.get(position).getTenBaiHat(),
                            mangbaihat.get(position).getTenCaSi(), mangbaihat.get(position).getHinhBaiHat(), mangbaihat.get(position).getLinkBaiHat());
                }else if (mangbaihetthuvienplaylist.size() > 0){
                    insertYeuThich(taikhoan, mangbaihetthuvienplaylist.get(position).getIdBaiHat(), mangbaihetthuvienplaylist.get(position).getTenBaiHat(),
                            mangbaihetthuvienplaylist.get(position).getTenCaSi(), mangbaihetthuvienplaylist.get(position).getHinhBaiHat(), mangbaihetthuvienplaylist.get(position).getLinkBaiHat());
                }else if (mangbaihatyeuthich.size() > 0){
                    insertYeuThich(taikhoan, mangbaihatyeuthich.get(position).getIdBaiHat(), mangbaihatyeuthich.get(position).getTenBaiHat(),
                            mangbaihatyeuthich.get(position).getTenCaSi(), mangbaihatyeuthich.get(position).getHinhBaiHat(), mangbaihatyeuthich.get(position).getLinkBaiHat());
                }
                dem++;
            }else {
                imageViewtim.setImageResource(R.drawable.iconlove);
                if (mangbaihat.size() > 0){
                    deleteYeuThich(taikhoan, mangbaihat.get(position).getIdBaiHat());
                }else if (mangbaihetthuvienplaylist.size() > 0){
                    deleteYeuThich(taikhoan, mangbaihetthuvienplaylist.get(position).getIdBaiHat());
                }else if (mangbaihatyeuthich.size() > 0){
                    deleteYeuThich(taikhoan, mangbaihatyeuthich.get(position).getIdBaiHat());
                }
                dem--;
            }
        });
        imageButtonplaypausenhac.setOnClickListener(view -> {
            if (isplaying){
                sendActionToService(ForegroundServiceControl.ACTION_PAUSE);
                imageButtonplaypausenhac.setImageResource(R.drawable.nutpause);
            }else {
                sendActionToService(ForegroundServiceControl.ACTION_RESUME);
                imageButtonplaypausenhac.setImageResource(R.drawable.nutplay);
            }
        });
        imageButtonlapnhac.setOnClickListener(this::onClick);
        imageButtontronnhac.setOnClickListener(view -> {
            if (!checkrandom){
                if (repeat){
                    repeat = false;
                    imageButtontronnhac.setImageResource(R.drawable.iconshuffled);
                    imageButtonlapnhac.setImageResource(R.drawable.iconrepeat);
                }else {
                    imageButtontronnhac.setImageResource(R.drawable.iconshuffled);
                }
                checkrandom = true;
            }else {
                imageButtontronnhac.setImageResource(R.drawable.iconsuffle);
                checkrandom = false;
            }
            sendActionToService(ForegroundServiceControl.ACTION_RANDOM);
        });
        seekBarnhac.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                durationToService = seekBar.getProgress();
                sendActionToService(ForegroundServiceControl.ACTION_DURATION);
            }
        });
        imageButtonnexnhac.setOnClickListener(view -> sendActionToService(ForegroundServiceControl.ACTION_NEXT));
        imageButtonpreviewnhac.setOnClickListener(view -> sendActionToService(ForegroundServiceControl.ACTION_PREVIOUS));
        toolbarplaynhac.setNavigationOnClickListener(view -> {
            mangbaihat.clear();
            mangbaihetthuvienplaylist.clear();
            mangbaihatyeuthich.clear();
            finish();
        });





        nutshare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_SEND);
               intent.setType("text/plain");
                String Body = "Chia sẻ bài này";
                String Sub2 = mangbaihat.get(position).getHinhBaiHat() ;
                String Sub3 = mangbaihat.get(position).getLinkBaiHat() ;

                intent.putExtra(Intent.EXTRA_TEXT, Body);
                intent.putExtra(Intent.EXTRA_TEXT, Sub3);
                startActivity(Intent.createChooser(intent, "Chia sẻ với"));

            }
        });


    }

   



    @SuppressWarnings("deprecation")
    private void setViewStart(){
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mangbaihat.size() > 0){
                    setView(taikhoan, mangbaihat.get(position).getIdBaiHat(),
                            mangbaihat.get(position).getHinhBaiHat(), mangbaihat.get(position).getTenBaiHat(), mangbaihat.get(position).getTenCaSi());
                }else if (mangbaihetthuvienplaylist.size() > 0){
                    setView(taikhoan, mangbaihetthuvienplaylist.get(position).getIdBaiHat(),
                            mangbaihetthuvienplaylist.get(position).getHinhBaiHat(), mangbaihetthuvienplaylist.get(position).getTenBaiHat()
                            , mangbaihetthuvienplaylist.get(position).getTenCaSi());
                }else if (mangbaihatyeuthich.size() > 0){
                    setView(taikhoan, mangbaihatyeuthich.get(position).getIdBaiHat(),
                            mangbaihatyeuthich.get(position).getHinhBaiHat(), mangbaihatyeuthich.get(position).getTenBaiHat(), mangbaihatyeuthich.get(position).getTenCaSi());
                }else {
                    handler.postDelayed(this, 300);
                }
            }
        }, 500);
    }
    private void NextMusic(){
        imageButtonplaypausenhac.setImageResource(R.drawable.nutplay);
        timeValue = 0;
    }
    private void completeNextMusic() {
        if (mangbaihat.size() > 0){
            NextMusic();
            setView(taikhoan, mangbaihat.get(position).getIdBaiHat(),
                    mangbaihat.get(position).getHinhBaiHat(), mangbaihat.get(position).getTenBaiHat(), mangbaihat.get(position).getTenCaSi());
        }else if (mangbaihetthuvienplaylist.size() > 0){
            NextMusic();
            setView(taikhoan, mangbaihetthuvienplaylist.get(position).getIdBaiHat(),
                    mangbaihetthuvienplaylist.get(position).getHinhBaiHat(), mangbaihetthuvienplaylist.get(position).getTenBaiHat()
                    , mangbaihetthuvienplaylist.get(position).getTenCaSi());
        }else if (mangbaihatyeuthich.size() > 0){
            NextMusic();
            setView(taikhoan, mangbaihatyeuthich.get(position).getIdBaiHat(),
                    mangbaihatyeuthich.get(position).getHinhBaiHat(), mangbaihatyeuthich.get(position).getTenBaiHat(), mangbaihatyeuthich.get(position).getTenCaSi());
        }
    }
    private void PreviousMusic(){
        imageButtonplaypausenhac.setImageResource(R.drawable.nutplay);
        timeValue = 0;
    }
    private void completePreviousMusic() {
        if (mangbaihat.size() > 0){
            PreviousMusic();
            setView(taikhoan, mangbaihat.get(position).getIdBaiHat(),
                    mangbaihat.get(position).getHinhBaiHat(), mangbaihat.get(position).getTenBaiHat(), mangbaihat.get(position).getTenCaSi());
        }else if (mangbaihetthuvienplaylist.size() > 0){
            PreviousMusic();
            setView(taikhoan, mangbaihetthuvienplaylist.get(position).getIdBaiHat(),
                    mangbaihetthuvienplaylist.get(position).getHinhBaiHat(), mangbaihetthuvienplaylist.get(position).getTenBaiHat(),
                    mangbaihetthuvienplaylist.get(position).getTenCaSi());
        }else if (mangbaihatyeuthich.size() > 0){
            PreviousMusic();
            setView(taikhoan, mangbaihatyeuthich.get(position).getIdBaiHat(),
                    mangbaihatyeuthich.get(position).getHinhBaiHat(), mangbaihatyeuthich.get(position).getTenBaiHat(), mangbaihatyeuthich.get(position).getTenCaSi());
        }
    }
    private void GetDataFromIntent() {
        Intent intent = getIntent();
        mangbaihat.clear();
        mangbaihetthuvienplaylist.clear();
        mangbaihatyeuthich.clear();
        if (intent != null){
            if (intent.hasExtra("cakhuc")){
                BaiHatModel baiHat = intent.getParcelableExtra("cakhuc");
                mangbaihat.add(baiHat);
            }else if (intent.hasExtra("cacbaihat")){
                mangbaihat = intent.getParcelableArrayListExtra("cacbaihat");
            }else if (intent.hasExtra("cakhucthuvien")){
                BaiHatThuVienPlayListModel baiHatThuVienPlayList = intent.getParcelableExtra("cakhucthuvien");
                mangbaihetthuvienplaylist.add(baiHatThuVienPlayList);
            }else if (intent.hasExtra("cacbaihatthuvien")){
                mangbaihetthuvienplaylist = intent.getParcelableArrayListExtra("cacbaihatthuvien");
            }else if (intent.hasExtra(("cakhucyeuthich"))){
                BaiHatYeuThichModel baiHatYeuThichModel = intent.getParcelableExtra("cakhucyeuthich");
                mangbaihatyeuthich.add(baiHatYeuThichModel);
            }
        }
    }
    private void getDataSQLite() {
        String sql = "SELECT * FROM tbNguoiDung";
        @SuppressLint("Recycle") Cursor cursor = db.rawQuery(sql, null);
        cursor.moveToLast();
        taikhoan = cursor.getString(1);
    }
    private void AnhXa() {
        nutshare = findViewById(R.id.iconsharenhac);
        toolbarplaynhac = findViewById(R.id.toolbarplaynhac);
        seekBarnhac = findViewById(R.id.seekBartime);
        ViewPager viewPagerplaynhac = findViewById(R.id.viewPagerdianhac);

        imageViewtim = findViewById(R.id.imageViewtimplaynhac);
        imageButtontronnhac = findViewById(R.id.imageButtontron);
        imageButtonpreviewnhac = findViewById(R.id.imageButtonpreview);
        imageButtonplaypausenhac = findViewById(R.id.imageButtonplaypause);
        imageButtonnexnhac = findViewById(R.id.imageButtonnext);
        imageButtonlapnhac = findViewById(R.id.imageButtonlap);
        textViewtatoltime = findViewById(R.id.textViewtimetotal);
        textViewcasi = findViewById(R.id.textViewtencasiplaynhac);
        textViewtennhac = findViewById(R.id.textViewtenbaihatplaynhac);
        textViewrunrime = findViewById(R.id.textViewruntime);

        fragment_dia_nhac = new Fragment_dia_nhac();
        adapternhac = new ViewPagerDiaNhac(getSupportFragmentManager());
        adapternhac.AddFragment(fragment_dia_nhac);
        viewPagerplaynhac.setAdapter(adapternhac);
        setSupportActionBar(toolbarplaynhac);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbarplaynhac.setTitleTextColor(Color.BLACK);
        fragment_dia_nhac = (Fragment_dia_nhac) adapternhac.getItem(position);
    }
    private void TimeSong(){
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss");
        textViewtatoltime.setText(simpleDateFormat.format(duration));
        seekBarnhac.setMax(duration);
    }
    private void handleMusic(int action){
        switch (action){
            case ForegroundServiceControl.ACTION_PAUSE:
                imageButtonplaypausenhac.setImageResource(R.drawable.nutpause);
                break;
            case ForegroundServiceControl.ACTION_RESUME:
                imageButtonplaypausenhac.setImageResource(R.drawable.nutplay);
                break;
            case ForegroundServiceControl.ACTION_NEXT:
                completeNextMusic();
                break;
            case ForegroundServiceControl.ACTION_PREVIOUS:
                completePreviousMusic();
                break;
        }
    }
    private void sendActionToService(int action){
        Intent intent = new Intent(this, ForegroundServiceControl.class);
        intent.putExtra("action_music_service", action);
        intent.putExtra("duration", durationToService);
        intent.putExtra("repeat_music", repeat);
        intent.putExtra("random_music", checkrandom);
        startService(intent);
    }
    private void setView(String taikhoan, int idBaiHat, String hinhBaiHat, String tenBaiHat, String tenCaSi){
        setGradient(hinhBaiHat);
        fragment_dia_nhac.PlayNhac(hinhBaiHat);
        Objects.requireNonNull(getSupportActionBar()).setTitle(tenBaiHat);
        textViewcasi.setText(tenCaSi);
        textViewtennhac.setText(tenBaiHat);
        checkYeuThich(taikhoan, idBaiHat);
    }
    private void setGradient(String urlImage){
        Picasso.get().load(urlImage)
                .into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        Palette.from(bitmap).generate(palette -> {
                            assert palette != null;
                            Palette.Swatch swatch = palette.getDominantSwatch();
                            RelativeLayout mContaier = findViewById(R.id.mContainer);
                            mContaier.setBackgroundResource(R.drawable.custom_back_ground_playnhac);
                            assert swatch != null;
                            GradientDrawable gradientDrawableBg = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                                    new int[]{swatch.getRgb(), swatch.getRgb()});
                            mContaier.setBackground(gradientDrawableBg);
                        });
                    }
                    @Override
                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                    }
                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {
                    }
                });
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }
    private void onClick(View view) {
        if (!repeat) {
            if (checkrandom) {
                checkrandom = false;
                imageButtonlapnhac.setImageResource(R.drawable.iconsyned);
                imageButtontronnhac.setImageResource(R.drawable.iconsuffle);
            } else {
                imageButtonlapnhac.setImageResource(R.drawable.iconsyned);
            }
            repeat = true;
        } else {
            imageButtonlapnhac.setImageResource(R.drawable.iconrepeat);
            repeat = false;
        }
        sendActionToService(ForegroundServiceControl.ACTION_REPEAT);
    }
}
