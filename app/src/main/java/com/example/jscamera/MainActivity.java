package com.example.jscamera;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private final static String TAG = "MainActivity";
    private final int REQUEST_CODE_ASK_PERMISSIONS =123;

    private Button bt;
    private WebView wv;
    public String fileFullName;//照相后的照片的全整路径
    private boolean fromTakePhoto; //是否是从摄像界面返回的webview
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private void initViews() {

        bt = (Button) findViewById(R.id.bt);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("----------------");
                takePhoto(Math.random() * 1000 + 1 + ".jpg");
            }
        });

        wv = (WebView) findViewById(R.id.wv);
        WebSettings setting = wv.getSettings();
        setting.setJavaScriptEnabled(true);
        wv.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return super.shouldOverrideUrlLoading(view, url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }

        });

        wv.setWebChromeClient(new WebChromeClient() {
            @Override//实现js中的alert弹窗在Activity中显示
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                Log.d(TAG, message);
                result.confirm();
                return true;
            }
        });

        wv.loadUrl("file:///android_asset/index.html");
        final Handler mHandler = new Handler();
        //webview增加javascript接口，监听html页面中的js点击事件
        wv.addJavascriptInterface(new Object() {
            public String clickOnAndroid() {//将被js调用
                mHandler.post(new Runnable() {
                    public void run() {
                        fromTakePhoto = true;
                        //调用 启用摄像头的自定义方法
                        takePhoto("testimg" + Math.random() * 1000 + 1 + ".jpg");
                        System.out.println("========fileFullName: " + fileFullName);

                    }
                });
                return fileFullName;
            }
        }, "demo");
    }

    /*
     * 调用摄像头的方法
     */
    public void takePhoto(String filename) {
        System.out.println("----start to take photo2 ----");

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            int status = checkSelfPermission(Manifest.permission.CAMERA);
            if (status != PackageManager.PERMISSION_GRANTED) {
                if (!shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                    showMessageOKCancel("You need to allow access to Camera",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                                        requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CODE_ASK_PERMISSIONS);
                                    };
                                }
                            });
                    return;
                }
                requestPermissions(new String[] {Manifest.permission.CAMERA}, REQUEST_CODE_ASK_PERMISSIONS);
                return;
            }
        }


        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_MEDIA_TITLE, "TakePhoto");

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            int status = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (status != PackageManager.PERMISSION_GRANTED) {
                if (!shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    showMessageOKCancel("You need to allow access to SDCard",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_ASK_PERMISSIONS);
                                    };
                                }
                            });
                    return;
                }
                requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_ASK_PERMISSIONS);
                return;
            }
        }

        //判断是否有SD卡
        String sdDir = null;
        boolean isSDcardExist = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        if (isSDcardExist) {
            sdDir = Environment.getExternalStorageDirectory().getAbsolutePath();
        } else {
            sdDir = Environment.getRootDirectory().getAbsolutePath();
        }
        //确定相片保存路径
        String targetDir = sdDir + "/" + "webview_camera";
        File file = new File(targetDir);
        if (!file.exists()) {
            file.mkdirs();
        }
        fileFullName = targetDir + "/" + filename;
        System.out.println("----taking photo fileFullName: " + fileFullName);
        //初始化并调用摄像头
        intent.putExtra(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(fileFullName)));
        startActivityForResult(intent, 1);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 0:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, 1001);

                } else {
                    //Tips
                }
        }
    }

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
     * 重写些方法，判断是否从摄像Activity返回的webview activity
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        System.out.println("----requestCode: " + requestCode + "; resultCode " + resultCode + "; fileFullName: " + fileFullName);
        if (fromTakePhoto && requestCode == 1 && resultCode == -1) {
            wv.loadUrl("javascript:wave2('" + fileFullName + "')");
        } else {
            wv.loadUrl("javascript:wave2('Please take your photo')");
        }
        fromTakePhoto = false;
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.example.jscamera/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.example.jscamera/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }


    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }
}
