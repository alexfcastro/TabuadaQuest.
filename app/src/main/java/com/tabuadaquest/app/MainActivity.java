package com.tabuadaquest.app;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class MainActivity extends Activity {

    private WebView webView;
    private TextView errorBanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        enableImmersiveMode();

        FrameLayout root = new FrameLayout(this);

        webView = new WebView(this);
        webView.setBackgroundColor(Color.WHITE);
        root.addView(webView, new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ));

        errorBanner = new TextView(this);
        errorBanner.setVisibility(View.GONE);
        errorBanner.setTextColor(Color.WHITE);
        errorBanner.setBackgroundColor(Color.rgb(176, 35, 35));
        errorBanner.setTextSize(12f);
        errorBanner.setPadding(18, 12, 18, 12);
        errorBanner.setGravity(Gravity.CENTER_VERTICAL);
        FrameLayout.LayoutParams errorParams = new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
            Gravity.TOP
        );
        root.addView(errorBanner, errorParams);

        setContentView(root);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setSupportZoom(false);
        settings.setTextZoom(100);
        settings.setDefaultTextEncodingName("utf-8");
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setJavaScriptCanOpenWindowsAutomatically(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                if (request != null && request.isForMainFrame()) {
                    showError("Erro ao carregar o aplicativo: " + error.getDescription());
                }
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                if (consoleMessage != null) {
                    String message = consoleMessage.message();
                    if (consoleMessage.messageLevel() == ConsoleMessage.MessageLevel.ERROR ||
                        (message != null && message.startsWith("TQ_JS_ERROR"))) {
                        showError("Erro interno: " + message + " (linha " + consoleMessage.lineNumber() + ")");
                    }
                }
                return true;
            }
        });

        webView.requestFocus(View.FOCUS_DOWN);
        webView.setFocusableInTouchMode(true);
        loadApp();
    }

    private void loadApp() {
        try {
            String html = readAsset("index.html");
            String diagnostics =
                "<script>" +
                "window.addEventListener('error',function(e){" +
                "console.error('TQ_JS_ERROR: '+(e.message||'erro desconhecido')+' @ '+(e.filename||'')+':'+(e.lineno||0));" +
                "});" +
                "window.addEventListener('unhandledrejection',function(e){" +
                "var r=e.reason; console.error('TQ_JS_ERROR: Promise rejeitada: '+(r&&r.message?r.message:String(r)));" +
                "});" +
                "</script>";

            html = html.replace("<head>", "<head>" + diagnostics);

            // Usar uma origem HTTPS local evita os comportamentos inconsistentes do file://
            // no WebView e torna localStorage/DOM storage muito mais confiáveis.
            webView.loadDataWithBaseURL(
                "https://tabuadaquest.local/",
                html,
                "text/html",
                "UTF-8",
                "https://tabuadaquest.local/"
            );
        } catch (Exception e) {
            showError("Não foi possível abrir o Tabuada Quest: " + e.getMessage());
        }
    }

    private String readAsset(String fileName) throws IOException {
        InputStream input = getAssets().open(fileName);
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(input, StandardCharsets.UTF_8)
        );
        StringBuilder builder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line).append('\n');
        }
        reader.close();
        return builder.toString();
    }

    private void showError(final String message) {
        runOnUiThread(() -> {
            if (errorBanner != null) {
                errorBanner.setText(message);
                errorBanner.setVisibility(View.VISIBLE);
            }
        });
    }

    private void enableImmersiveMode() {
        if (android.os.Build.VERSION.SDK_INT >= 30) {
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                controller.setSystemBarsBehavior(
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                );
            }
        } else {
            getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            );
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            enableImmersiveMode();
        }
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
