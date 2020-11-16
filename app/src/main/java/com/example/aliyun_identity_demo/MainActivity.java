package com.example.aliyun_identity_demo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.aliyun.identity.platform.api.IdentityCallback;
import com.aliyun.identity.platform.api.IdentityOcrInfo;
import com.aliyun.identity.platform.api.IdentityPlatform;
import com.aliyun.identity.platform.api.IdentityResponse;
import com.aliyun.identity.platform.api.IdentityResponseCode;
import com.aliyun.identity.platform.log.xLogger;
import com.example.aliyun_identity_demo.net.NormalNetUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import okhttp3.Call;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private static String LOG_TAG = "AliyunIdentity";
    private static final int IDENTITY_PERMISSION_REQUEST_CODE = 1024;

    private static String[] identityPermissions = {
            Manifest.permission.CAMERA,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
    };

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

        // 获取摄像头等权限
        handlePermissions();

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

        Map<String,String> parmas =new HashMap<>();
//
//        NormalNetUtils.getInstance().postDataAsynToNet("", parmas, new NormalNetUtils.MyNetCall() {
//            @Override
//            public void success(Call call, Response response) throws IOException {
//                Log.i("whcTag", "success: ");
//            }
//
//            @Override
//            public void failed(Call call, IOException e) {
//                Log.i("whcTag", "failed: ");
//            }
//        });
//

        NormalNetUtils.getInstance().getDataAsynFromNet("https://www.baidu.com", new NormalNetUtils.MyNetCall() {
            @Override
            public void success(Call call, Response response) throws IOException {
                Log.i("whcTag", "success: " + response.body().string());

            }

            @Override
            public void failed(Call call, IOException e) {
                Log.i("whcTag", "failed: " + e.toString());

            }
        });







    }
}