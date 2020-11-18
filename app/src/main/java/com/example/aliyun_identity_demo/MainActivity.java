package com.example.aliyun_identity_demo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.os.EnvironmentCompat;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.aliyun.identity.platform.api.IdentityCallback;
import com.aliyun.identity.platform.api.IdentityOcrInfo;
import com.aliyun.identity.platform.api.IdentityPlatform;
import com.aliyun.identity.platform.api.IdentityResponse;
import com.aliyun.identity.platform.api.IdentityResponseCode;
import com.aliyun.identity.platform.log.xLogger;
import com.example.aliyun_identity_demo.net.NormalNetUtils;
import com.example.aliyun_identity_demo.util.ImageUtil;
import com.example.aliyun_identity_demo.util.PhotoBitmapUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

import okhttp3.Call;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private ImageView ivPhoto;
    private TextView textView;

    private final static int  CAMERA_REQUEST_CODE = 1024;

    private static String LOG_TAG = "AliyunIdentity";
    private static final int IDENTITY_PERMISSION_REQUEST_CODE = 1024;

    private static String[] identityPermissions = {
            Manifest.permission.CAMERA,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
    };

    String mCameraImagePath = "";

    String metaInfo;

    public       String     certifyId = "";

    // 将metaInfo发送到业务自己的服务器端，服务器端调用阿里云相关接口拿到certifyId，再返回给客户端。
    // 注意：一个certifyId只能调用一次认证服务。
    private String getCertifyId(String metaInfo) {
        String certifyId = "27a1faac60368957f2e8a9490b873072";
        // sendToCustomerServer(metaInfo);
        // certifyId = parseResponse();
        return certifyId;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 获取metaInfo，发送到业务自己服务器端换取certifyId
         metaInfo = IdentityPlatform.getMetaInfo(MainActivity.this);
        // 获取摄像头等权限
        handlePermissions();


        ivPhoto = findViewById(R.id.iv_current);
        textView = findViewById(R.id.tv_current);
        setImageView();

        // 初始化SDK
        IdentityPlatform.getInstance().install(MainActivity.this);

        Button btnAliyunIdentity = findViewById(R.id.btnAliyunIdentity);
        if (null != btnAliyunIdentity) {
            btnAliyunIdentity.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // 获取metaInfo，发送到业务自己服务器端换取certifyId
                    String metaInfo = IdentityPlatform.getMetaInfo(MainActivity.this);

                    // 将metaInfo发送到业务自己服务器端换取certifyId
                    String certifyId = getCertifyId(metaInfo);

                    // 开始验证
                    IdentityPlatform.getInstance().faceDetect(certifyId, null, new IdentityCallback() {
                        @Override
                        public boolean response(final IdentityResponse response) {
                            if (IdentityResponseCode.IDENTITY_SUCCESS == response.code) {
                                Toast.makeText(MainActivity.this,
                                        "认证通过",
                                        Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(MainActivity.this,
                                        "认证失败([" + response.code + "]" + response.message + ")",
                                        Toast.LENGTH_LONG).show();
                            }

                            return true;
                        }
                    });
                }
            });
        }
    }


    private void setImageView( ){
        try {
            File photoFile = createImageFile();
            if (photoFile != null) {
                mCameraImagePath = photoFile.getAbsolutePath();
                ivPhoto.setImageBitmap(BitmapFactory.decodeFile(mCameraImagePath));
            }else {
                textView.setText("请先进行拍照操作");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 获取当前需要的权限里面还没有取得用户授权的权限列表
     *
     * @return
     */
    private List<String> genUnGrantedToygerPermissions() {
        List<String> requestPerms = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String permission : identityPermissions) {
                if (PackageManager.PERMISSION_GRANTED != checkSelfPermission(permission)) {
                    requestPerms.add(permission);
                }
            }
        }

        return requestPerms;
    }

    /**
     * 处理权限问题
     */
    private void handlePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> requestPerms = genUnGrantedToygerPermissions();
            if (requestPerms.size() > 0) {
                requestPermissions(requestPerms.toArray(new String[0]),
                        IDENTITY_PERMISSION_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        List<String> perms = genUnGrantedToygerPermissions();
        if ((requestCode == IDENTITY_PERMISSION_REQUEST_CODE) && (perms.size() <= 0)) {
            Log.d(LOG_TAG, "Ppermissions are OK.");
        } else {
            StringBuilder unGrantedPermissions = new StringBuilder();
            for(String permission:perms){
                unGrantedPermissions.append(permission);
                unGrantedPermissions.append(",");
            }

            Log.d(LOG_TAG, "Ppermissions not granted: " + unGrantedPermissions.toString());
        }
    }




    public void OnNetClick(View view) {


        if (TextUtils.isEmpty(mCameraImagePath)){
            Toast.makeText(MainActivity.this,"请先进行拍照操作",Toast.LENGTH_SHORT).show();
            return;
        }

        ///common/aliyun/initSmartVerify（初始接口）
        //  参数：String metaInfo
        //
        //
        //  /common/aliyun/describeSmartVerify（获取是否成功接口）
        //  参数：String certifyId


        Log.i("whcTag", "OnNetClick: 准备获取 base64 ");


        String url = "http://10.10.130.151:8590/common/aliyun/initSmartVerify" ;
        String imgStr = ImageUtil.imageToBase64(mCameraImagePath);

        Map<String,String> parmas =new HashMap<>();
        parmas.put("metaInfo",metaInfo);
        parmas.put("facePictureBase64",imgStr);

        Log.i("whcTag", "OnNetClick: 准备开始网络请求 ");

        String result = "        {\"ret\":\"5805a520448100a20473ab48372560b6\",\"message\":\"\",\"code\":0}\n";
        // 将metaInfo发送到业务自己服务器端换取certifyId

        try {
            JSONObject jsonObject = new JSONObject(result);

            certifyId = jsonObject.getString("ret");

        } catch (JSONException e) {
            e.printStackTrace();
        }


        NormalNetUtils.getInstance().postDataAsynToNet(url, parmas, new NormalNetUtils.MyNetCall() {
            @Override
            public void success(Call call, Response response) throws IOException {

                final String result = response.body().string();
                //{"ret":"5805a520448100a20473ab48372560b6","message":"","code":0}
                // 将metaInfo发送到业务自己服务器端换取certifyId

                try {
                    JSONObject jsonObject = new JSONObject(result);

                    certifyId = jsonObject.getString("ret");

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (TextUtils.isEmpty(certifyId)){
                    return;
                }

                Log.i("whcTag", "success:  获取的 id 是 ：" + certifyId);
                // 开始验证
                IdentityPlatform.getInstance().faceDetect(certifyId, null, new IdentityCallback() {
                    @Override
                    public boolean response(final IdentityResponse response) {
                        if (IdentityResponseCode.IDENTITY_SUCCESS == response.code) {
                            Toast.makeText(MainActivity.this,
                                    "认证通过",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(MainActivity.this,
                                    "认证失败([" + response.code + "]" + response.message + ")",
                                    Toast.LENGTH_LONG).show();

                            Log.i("whcTag", "response:  code :" + response.code +  " msg:" + response.message);
                        }

                        return true;
                    }
                });


            }

            @Override
            public void failed(Call call, IOException e) {
                Log.i("whcTag", "failed: ");
            }
        });





    }



    /**
     * 调起相机拍照
     */
    private void openCamera() {
        Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // 判断是否有相机
        if (captureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            Uri photoUri = null;

                try {
                    photoFile = createImageFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (photoFile != null) {
                    mCameraImagePath = photoFile.getAbsolutePath();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        //适配Android 7.0文件权限，通过FileProvider创建一个content类型的Uri
                        photoUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", photoFile);
                    } else {
                        photoUri = Uri.fromFile(photoFile);
                    }
                }

                if (photoUri != null) {
                captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                captureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                startActivityForResult(captureIntent, CAMERA_REQUEST_CODE);
            }
        }
    }

    /**
     * 创建保存图片的文件
     * @return
     * @throws IOException
     */
    private File createImageFile() throws IOException {
    //    String imageName = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageName = "current";
        ///storage/sdcard0/Pictures
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (!storageDir.exists()) {
            storageDir.mkdir();
        }
        File tempFile = new File(storageDir, imageName);
        if (!Environment.MEDIA_MOUNTED.equals(EnvironmentCompat.getStorageState(tempFile))) {
            return null;
        }

        return tempFile;
    }


    public void OnTakePic(View view) {
        openCamera();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {

                Bitmap bm = PhotoBitmapUtil.rotaingImageView(mCameraImagePath, BitmapFactory.decodeFile(mCameraImagePath)) ;

                // 使用图片路径加载
                ivPhoto.setImageBitmap(bm);


                //通知相册更新
                MediaStore.Images.Media.insertImage(MainActivity.this.getContentResolver(), bm, mCameraImagePath, null);
                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri uri = Uri.fromFile(new File(mCameraImagePath));
                intent.setData(uri);
                MainActivity.this.sendBroadcast(intent);


            } else {
                Toast.makeText(this,"取消",Toast.LENGTH_LONG).show();
            }



        }
    }

    public void OnNetVerify(View view) {

        ///common/aliyun/initSmartVerify（初始接口）
        //  参数：String metaInfo
        //
        //
        //  /common/aliyun/describeSmartVerify（获取是否成功接口）
        //  参数：String certifyId



        if (TextUtils.isEmpty(certifyId)){
            Toast.makeText(MainActivity.this,"ceritfyId 不能为空",Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "http://10.10.130.151:8590/common/aliyun/describeSmartVerify" ;

        Map<String,String> parmas =new HashMap<>();
        parmas.put("certifyId",certifyId);


        NormalNetUtils.getInstance().postDataAsynToNet(url, parmas, new NormalNetUtils.MyNetCall() {
            @Override
            public void success(Call call, Response response) throws IOException {

                final String result = response.body().string();


                Log.i("whcTag", " 查询认证结果  success: result: " + result);


            }

            @Override
            public void failed(Call call, IOException e) {
                Log.i("whcTag", "查询认证结果 ： failed: ");
            }
        });



    }
}