package com.tabuadaquest.app;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            openGame();
        } catch (Throwable error) {
            showStartupError(error);
        }
    }

    private void openGame() {
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.WHITE);

        webView = new WebView(getApplicationContext());
        webView.setBackgroundColor(Color.WHITE);
        webView.setFocusable(true);
        webView.setFocusableInTouchMode(true);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setSupportZoom(false);
        settings.setTextZoom(100);
        settings.setDefaultTextEncodingName("utf-8");
        settings.setMediaPlaybackRequiresUserGesture(false);

        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());

        root.addView(webView, new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ));

        setContentView(root);

        // Caminho mais simples e compatível para um aplicativo totalmente offline.
        webView.loadUrl("file:///android_asset/index.html");
        webView.requestFocus(View.FOCUS_DOWN);
    }

    private void showStartupError(Throwable error) {
        TextView message = new TextView(this);
        message.setTextColor(Color.rgb(35, 42, 63));
        message.setBackgroundColor(Color.WHITE);
        message.setTextSize(16f);
        message.setGravity(Gravity.CENTER);
        message.setPadding(32, 32, 32, 32);

        String detail = error == null ? "erro desconhecido" : error.getClass().getSimpleName() + ": " + String.valueOf(error.getMessage());
        message.setText("O Tabuada Quest não conseguiu iniciar.\n\n" + detail + "\n\nAtualize o Android System WebView/Google Chrome e abra o aplicativo novamente.");
        setContentView(message);
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            try {
                webView.stopLoading();
                webView.destroy();
            } catch (Throwable ignored) {
            }
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
