package cc.framextv.tv;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final String HOME_URL = "https://framextv.cc/";
    private WebView webView;
    private FrameLayout root;
    private View loading;
    private View customView;
    private WebChromeClient.CustomViewCallback customViewCallback;
    private boolean pageFailed = false;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        applyImmersiveMode();

        root = new FrameLayout(this);
        root.setBackgroundColor(Color.rgb(8, 11, 18));
        setContentView(root);

        webView = new WebView(this);
        webView.setBackgroundColor(Color.rgb(8, 11, 18));
        webView.setFocusable(true);
        webView.setFocusableInTouchMode(true);
        webView.setOverScrollMode(View.OVER_SCROLL_NEVER);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true); // Required for modern web apps.
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setLoadsImagesAutomatically(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(false);
        settings.setSupportMultipleWindows(false);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setUserAgentString(settings.getUserAgentString() + " FrameXTVAndroidTV/1.0");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) settings.setSafeBrowsingEnabled(true);

        CookieManager cookies = CookieManager.getInstance();
        cookies.setAcceptCookie(true);
        cookies.setAcceptThirdPartyCookies(webView, true);

        loading = makeLoadingView();
        root.addView(webView, new FrameLayout.LayoutParams(-1, -1));
        root.addView(loading, new FrameLayout.LayoutParams(-1, -1));

        webView.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                String scheme = uri.getScheme() == null ? "" : uri.getScheme();
                if ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme)) return false;
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, uri));
                } catch (ActivityNotFoundException ignored) {
                    Toast.makeText(MainActivity.this, "No app can open this link", Toast.LENGTH_SHORT).show();
                }
                return true;
            }

            @Override public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                pageFailed = false;
                loading.setVisibility(View.VISIBLE);
            }

            @Override public void onPageFinished(WebView view, String url) {
                loading.setVisibility(View.GONE);
                view.requestFocus();
            }

            @Override public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (request.isForMainFrame()) {
                    pageFailed = true;
                    loading.setVisibility(View.VISIBLE);
                    showLoadError(error.getDescription() == null ? "Unable to load website" : error.getDescription().toString());
                }
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override public void onShowCustomView(View view, CustomViewCallback callback) {
                if (customView != null) { callback.onCustomViewHidden(); return; }
                customView = view;
                customViewCallback = callback;
                root.addView(customView, new FrameLayout.LayoutParams(-1, -1));
                webView.setVisibility(View.GONE);
                loading.setVisibility(View.GONE);
                applyImmersiveMode();
            }

            @Override public void onHideCustomView() { hideCustomView(); }

            @Override public void onProgressChanged(WebView view, int progress) {
                if (progress >= 100 && !pageFailed) loading.setVisibility(View.GONE);
            }
        });

        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState);
        } else {
            webView.loadUrl(HOME_URL);
        }
        webView.requestFocus();
    }

    private View makeLoadingView() {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setGravity(Gravity.CENTER);
        panel.setBackgroundColor(Color.rgb(8, 11, 18));
        TextView title = new TextView(this);
        title.setText("FrameXTV");
        title.setTextColor(Color.WHITE);
        title.setTextSize(30);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        TextView subtitle = new TextView(this);
        subtitle.setText("Loading…");
        subtitle.setTextColor(Color.rgb(160, 174, 192));
        subtitle.setTextSize(16);
        subtitle.setGravity(Gravity.CENTER);
        panel.addView(title);
        panel.addView(subtitle);
        return panel;
    }

    private void showLoadError(String detail) {
        if (!(loading instanceof ViewGroup)) return;
        ViewGroup panel = (ViewGroup) loading;
        panel.removeAllViews();
        TextView title = new TextView(this);
        title.setText("Can't load FrameXTV");
        title.setTextColor(Color.WHITE);
        title.setTextSize(25);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        TextView message = new TextView(this);
        message.setText(detail + "\n\nCheck your TV's internet connection, then press OK to retry.");
        message.setTextColor(Color.LTGRAY);
        message.setTextSize(16);
        message.setGravity(Gravity.CENTER);
        message.setFocusable(true);
        message.setFocusableInTouchMode(true);
        message.setOnClickListener(v -> retry());
        message.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_UP && (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER)) { retry(); return true; }
            return false;
        });
        panel.addView(title);
        panel.addView(message);
        message.requestFocus();
    }

    private void retry() {
        if (webView != null) {
            pageFailed = false;
            loading.setVisibility(View.VISIBLE);
            webView.loadUrl(HOME_URL);
        }
    }

    private void hideCustomView() {
        if (customView == null) return;
        root.removeView(customView);
        customView = null;
        webView.setVisibility(View.VISIBLE);
        if (customViewCallback != null) customViewCallback.onCustomViewHidden();
        customViewCallback = null;
        webView.requestFocus();
        applyImmersiveMode();
    }

    private void applyImmersiveMode() {
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }

    @Override public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
            if (customView != null) { hideCustomView(); return true; }
            if (webView != null && webView.canGoBack()) { webView.goBack(); return true; }
            return super.dispatchKeyEvent(event);
        }
        return super.dispatchKeyEvent(event);
    }

    @Override protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (webView != null) webView.saveState(outState);
    }

    @Override protected void onPause() {
        if (webView != null) webView.onPause();
        super.onPause();
    }

    @Override protected void onResume() {
        super.onResume();
        if (webView != null) webView.onResume();
        applyImmersiveMode();
    }

    @Override protected void onDestroy() {
        if (webView != null) {
            webView.stopLoading();
            webView.setWebChromeClient(null);
            webView.setWebViewClient(null);
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }
}
