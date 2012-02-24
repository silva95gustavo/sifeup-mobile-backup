package pt.up.fe.mobile.ui.webclient;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import pt.up.fe.mobile.R;
import pt.up.fe.mobile.ui.BaseFragment;
import pt.up.fe.mobile.ui.DownloaderFragment;
import pt.up.fe.mobile.ui.LoginActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

public class WebviewFragment extends BaseFragment {

    public final static String URL_INTENT = "pt.up.fe.mobile.ui.webclient.URL";
    private WebView mWebView;
    private ProgressBar progressWebView;
    private String url;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        url = getArguments().getString(URL_INTENT);
        if (url == null)
            url = savedInstanceState.getString(URL_INTENT);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.webview, getParentContainer(),
                true);
        mWebView = (WebView) root.findViewById(R.id.webview);
        progressWebView = (ProgressBar) root
                .findViewById(R.id.webview_progress);
        final WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            webSettings.setDisplayZoomControls(false);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setSavePassword(false);
        webSettings.setSaveFormData(false);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        mWebView.setWebViewClient(new FeupWebViewClient());
        mWebView.setDownloadListener(new DownloadListener() {
            public void onDownloadStart(String url, String userAgent,
                    String contentDisposition, String mimetype,
                    long contentLength) {
                String filename = getFilename(contentDisposition);
                if (filename == null)
                    filename = url;
                DownloaderFragment.newInstance("Downloader", url, filename)
                        .show(getFragmentManager(), "Downloader");

            }

        });

        mWebView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                progressWebView.setProgress(progress);
            }
        });

        mWebView.loadUrl(url);
        showFastMainScreen();
        return getParentContainer(); // mandatory
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.webclient_menu_items, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_browser) {
            final Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse(url));
            startActivity(browserIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private String getFilename(String contentDisposition) {
        final String pattern = "filename=\"";
        int filenamePos = contentDisposition.indexOf(pattern);
        if (filenamePos == -1)
            return null;
        int filenameFinal = contentDisposition.indexOf("\"", filenamePos
                + pattern.length());
        return contentDisposition.substring(filenamePos + pattern.length(),
                filenameFinal);
    }

    private class FeupWebViewClient extends WebViewClient {

        private boolean loggedIn = false;

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if ( url.equals("https://www.fe.up.pt/si/inqueritos_geral.inqueritos_list") )
            {//trying to stop stupid redirections
                view.loadUrl(WebviewFragment.this.url);
                return true;
            }
            return false;
        }

        @Override
        public void onReceivedError(WebView view, int errorCode,
                String description, String failingUrl) {
            if (getActivity() == null)
                return;
            Toast.makeText(getActivity(), description, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            progressWebView.setVisibility(View.VISIBLE);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            if (getActivity() == null)
                return;
            if (!loggedIn) {
                // This is a ugly hack to login
                // This is needed because some devices are very slow
                // first we fill the form
                // then we reload the page
                // Logging in through javascript
                final SharedPreferences loginSettings = getActivity()
                        .getSharedPreferences(LoginActivity.class.getName(),
                                Context.MODE_PRIVATE);
                final String user = loginSettings.getString(
                        LoginActivity.PREF_USERNAME, "");
                final String pass = loginSettings.getString(
                        LoginActivity.PREF_PASSWORD, "");
                mWebView.loadUrl("javascript: {"
                        + "document.getElementById('user').value = '" + user
                        + "';" + "var  pass = document.getElementById('pass');"
                        + "pass.value = '" + pass + "';"
                        + "var frms = pass.form; " + "frms.submit(); };");
                loggedIn = true;
            } else
                WebviewFragment.this.url = url;
            progressWebView.setVisibility(View.GONE);
            progressWebView.setProgress(0);
        }
    }

    public void onBackPressed() {
        if (getActivity() == null)
            return;
        if (mWebView != null && mWebView.canGoBack()) {
            mWebView.goBack();
        } else
        {
            getActivity().finish();
            getActivity().overridePendingTransition(R.anim.home_enter, R.anim.home_exit);
        }
    }

}