package com.sandriver.apptools.webkit;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipData;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.fragment.app.FragmentActivity;

import com.luck.picture.lib.PictureSelectionModel;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.sandriver.apptools.webkit.rxpermissions2.RxPermissions;

import java.io.File;
import java.util.List;

/**
 * @author zook
 */
public class WebKitHelper {
    private final static int FILE_CHOOSER_RESULT_CODE = 10000;
    private final static int FILE_SELECT_CODEB = 1;

    private Activity activity;
    private LollipopFixedWebView webView;
    private ValueCallback<Uri> mUploadMsg;
    private ValueCallback<Uri[]> mValueCallback;
    private ViewGroup mWindowContainer;
    private RxPermissions rxPermissions;

    public void embedWebKit(Activity activity, ViewGroup container) {
        if (null != container) {
            if (null == webView) {
                this.activity = activity;
                rxPermissions = new RxPermissions((FragmentActivity) activity);
                mWindowContainer = (ViewGroup) activity.getWindow().getDecorView();
                webView = new LollipopFixedWebView(container.getContext());
                webView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                WebSettings webSetting = webView.getSettings();
                webSetting.setSavePassword(false);
                webSetting.setSaveFormData(false);
                webSetting.setJavaScriptEnabled(true);
                webSetting.setDomStorageEnabled(true);
                webSetting.setAllowFileAccess(true);
                webSetting.setSupportZoom(true);
                webSetting.setBuiltInZoomControls(false);
                webSetting.setDisplayZoomControls(false); //隐藏原生的缩放控件
                webSetting.setUseWideViewPort(true);
                webSetting.setLoadWithOverviewMode(true);
                webSetting.setJavaScriptCanOpenWindowsAutomatically(true);
                webSetting.setLoadsImagesAutomatically(true); //支持自动加载图片
                webSetting.setDefaultTextEncodingName("utf-8");//设置编码格式
                webSetting.setPluginState(WebSettings.PluginState.ON);
                webSetting.setSupportMultipleWindows(true);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    webSetting.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
                }
                String cachePath = container.getContext().getFilesDir().getAbsolutePath() + "appcache";
                Log.d("GameInfoBean_debug", cachePath);
                webSetting.setAppCachePath(cachePath);
                webSetting.setDatabasePath(cachePath);
                webSetting.setAppCacheEnabled(true);
                webSetting.setDatabaseEnabled(true);
                webSetting.setCacheMode(WebSettings.LOAD_NO_CACHE);
                webSetting.setAppCacheMaxSize(8 * 1024 * 1024);
                webView.addJavascriptInterface(new WebKitInterface(activity), "suporIotInjected");
                webView.setWebChromeClient(new WebChromeClient() {
                    private CustomViewCallback mCustomViewCallback;
                    //  横屏时，显示视频的view
                    private View mCustomView;

                    @Override
                    public void onShowCustomView(View view, CustomViewCallback callback) {
                        super.onShowCustomView(view, callback);
                        if (mCustomView != null) {
                            callback.onCustomViewHidden();
                            return;
                        }

                        mCustomView = view;
                        mCustomView.setVisibility(View.VISIBLE);
                        mCustomViewCallback = callback;
                        mWindowContainer.addView(mCustomView);
                        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    }

                    @Override
                    public void onHideCustomView() {
                        super.onHideCustomView();
                        if (mCustomView == null) {
                            return;
                        }
                        mCustomView.setVisibility(View.GONE);
                        mWindowContainer.removeView(mCustomView);
                        mCustomView = null;
                        try {
                            mCustomViewCallback.onCustomViewHidden();
                        } catch (Exception e) {
                        }
                        mWindowContainer.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//竖屏

                            }
                        }, 100);
                    }

                    // For Android < 3.0
                    public void openFileChooser(ValueCallback<Uri> valueCallback) {
                        mUploadMsg = valueCallback;
                        checkMedia("");
                    }

                    // For Android  >= 3.0
                    public void openFileChooser(ValueCallback valueCallback, String acceptType) {
                        mUploadMsg = valueCallback;
                        checkMedia(acceptType);
                    }

                    //For Android  >= 4.1
                    public void openFileChooser(ValueCallback<Uri> valueCallback, String acceptType, String capture) {
                        mUploadMsg = valueCallback;
                        checkMedia(acceptType);
                    }

                    @Override
                    public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                        mValueCallback = filePathCallback;
//                openFileChooserActivity();
                        checkMedia(fileChooserParams.getAcceptTypes()[0]);
//                        openImageChooserActivity(fileChooserParams.getAcceptTypes()[0]);
                        return true;
                    }
                });
            }
            container.addView(webView);
        }
    }

    public void loadUrl(String url) {
        if (!TextUtils.isEmpty(url) && null != webView) {
            webView.loadUrl(url);
        }
    }

    public void sendLoginToWeb(int state, String token) {
//        webView.loadUrl("javascript:sendLoginToWeb()");
//        webView.loadUrl("javascript:window.sendLoginToWeb");
        String functionFormat = String.format("javascript:%s(%d,\"%s\");", "window.sendLoginToWeb", state, token);
        webView.loadUrl(functionFormat);
    }

    public void sendPageStateToWeb(int state) {
        webView.loadUrl("javascript:sendPageStateToWeb(" + state + ")");
    }

    public void destoryKit() {
        if (null != webView && null != webView.getParent()) {
            ((ViewGroup) webView.getParent()).removeAllViews();
        }
        activity = null;
    }

    private void checkMedia(String type) {
        rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(aBoolean -> {
                    if (aBoolean) {
                        PictureSelectionModel pictureSelector;
                        if ("image/*".equals(type)) {
                            pictureSelector = PictureSelector.create(activity).openGallery(PictureMimeType.ofImage())
                                    .maxSelectNum(9);
                        } else if ("video/*".equals(type)) {
                            pictureSelector = PictureSelector.create(activity).openGallery(PictureMimeType.ofVideo())
                                    .maxSelectNum(1);
                        } else {
                            return;
                        }
                        pictureSelector
                                .imageEngine(GlideEngine.createGlideEngine()) // 请参考Demo GlideEngine.java
                                .forResult(PictureConfig.CHOOSE_REQUEST);
                    } else {
                        Logc.i("permission request: failed");
//                        showDialog();
                    }
                });


    }

    private void openImageChooserActivity(String type) {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType(type);
        activity.startActivityForResult(Intent.createChooser(i, "Media Chooser"), FILE_CHOOSER_RESULT_CODE);
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode == FILE_CHOOSER_RESULT_CODE) {
//            if (null == mUploadMsg && null == mValueCallback) return;
//            Uri result = data == null || resultCode != Activity.RESULT_OK ? null : data.getData();
//            if (mValueCallback != null) {
//                onActivityResultAboveL(requestCode, resultCode, data);
//            } else if (mUploadMsg != null) {
//                mUploadMsg.onReceiveValue(result);
//                mUploadMsg = null;
//            }
//        }
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case PictureConfig.CHOOSE_REQUEST:
                    // 结果回调
                    List<LocalMedia> selectList = PictureSelector.obtainMultipleResult(data);
                    try {
                        if (mUploadMsg != null) {

                            String path;
                            if (selectList.get(0).isCompressed()) {
                                Logc.e("getCompressPath） =" + selectList.get(0).getCompressPath());
                                path = selectList.get(0).getCompressPath();
                            } else {
                                path = selectList.get(0).getPath();
                            }
                            if (!new File(path).exists()) {
                                Logc.e("sourcePath empty or not exists.");
                                break;
                            }
                            Uri uri = Uri.parse(path);

                            mUploadMsg.onReceiveValue(uri);
                            mUploadMsg = null;
                        } else if (mValueCallback != null) {
                            Uri[] uris = new Uri[selectList.size()];
                            for (int i = 0; i < selectList.size(); i++) {
//                                String path;
//                                if (selectList.get(i).isCompressed()) {
//                                    Logc.e("getCompressPath） =" + selectList.get(i).getCompressPath());
//                                    path = selectList.get(i).getRealPath();
//                                } else {
//                                    path = selectList.get(i).getRealPath();
//                                }
//                                File file = new File(path);
                                uris[i] = Uri.parse(selectList.get(i).getPath());
                            }
                            if (uris.length > 0) {
                                mValueCallback.onReceiveValue(uris);
                            } else {
                                Logc.e("sourcePath empty or not exists.");
                            }
                            break;

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    break;
            }
        } else {
            if (null != mUploadMsg) {
                mUploadMsg.onReceiveValue(null);
            }
            if (null != mValueCallback) {
                mValueCallback.onReceiveValue(null);
            }
        }
    }

    public Uri getMediaContentUri(String filePath) {
//        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = activity.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID}, MediaStore.Images.Media.DATA + "=? ",
                new String[]{filePath}, null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/images/media");
            return Uri.withAppendedPath(baseUri, "" + id);
        } else {
            if (new File(filePath).exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                return activity.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void onActivityResultAboveL(int requestCode, int resultCode, Intent intent) {
        if (requestCode != FILE_CHOOSER_RESULT_CODE || mValueCallback == null)
            return;
        Uri[] results = null;
        if (resultCode == Activity.RESULT_OK) {
            if (intent != null) {
                String dataString = intent.getDataString();
                ClipData clipData = intent.getClipData();
                if (clipData != null) {
                    results = new Uri[clipData.getItemCount()];
                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        ClipData.Item item = clipData.getItemAt(i);
                        results[i] = item.getUri();
                    }
                }
                if (dataString != null)
                    results = new Uri[]{Uri.parse(dataString)};
            }
        }
        mValueCallback.onReceiveValue(results);
        mValueCallback = null;
    }

    public static class WebKitInterface {
        private Activity activity;

        public WebKitInterface(Activity activity) {
            this.activity = activity;
        }


        @JavascriptInterface
        public void sendLoginToNative(String json) {

        }

    }

    public void goBack() {
        if (null != webView) {
            webView.goBack();//返回上个页面
        }
    }

    public boolean canGoBack() {
        return null != webView && webView.canGoBack();
    }

    public LollipopFixedWebView getWebView() {
        return webView;
    }
}