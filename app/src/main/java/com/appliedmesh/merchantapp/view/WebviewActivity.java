package com.appliedmesh.merchantapp.view;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.appliedmesh.merchantapp.R;

/**
 * Created by Home on 2015/4/6.
 */
public class WebviewActivity extends Activity{
    WebView mWebView;
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webview);
        mWebView = (WebView) findViewById(R.id.webView);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.addJavascriptInterface(new JavaScriptFasttrack(), "Fasttrack");
        mWebView.setWebViewClient(new WebViewClient());
        mWebView.setWebChromeClient(new WebChromeClient());
        mWebView.loadUrl(getIntent().getStringExtra("url"));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                this.finish();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()){
            mWebView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    final class JavaScriptFasttrack {
        @JavascriptInterface
        public void onAction(String action) {
            if (action.equals("close")) {
                WebviewActivity.this.finish();
            }
        }
        @JavascriptInterface
        public void onPaymentResult(String result) {
            WebviewActivity.this.finish();
        }
        @JavascriptInterface
        public void onReceiptPageLoad() {
        }
    }
}
