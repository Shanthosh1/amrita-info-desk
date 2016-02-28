package com.njlabs.amrita.aid.landing;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.PeriodicTask;
import com.google.android.gms.gcm.Task;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.accountswitcher.AccountHeader;
import com.mikepenz.materialdrawer.accountswitcher.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.njlabs.amrita.aid.BaseActivity;
import com.njlabs.amrita.aid.BuildConfig;
import com.njlabs.amrita.aid.R;
import com.njlabs.amrita.aid.about.Amrita;
import com.njlabs.amrita.aid.about.App;
import com.njlabs.amrita.aid.aums.AumsActivity;
import com.njlabs.amrita.aid.gpms.GpmsActivity;
import com.njlabs.amrita.aid.info.Calender;
import com.njlabs.amrita.aid.info.Curriculum;
import com.njlabs.amrita.aid.info.TrainBusInfo;
import com.njlabs.amrita.aid.news.NewsActivity;
import com.njlabs.amrita.aid.news.NewsUpdateService;
import com.njlabs.amrita.aid.settings.SettingsActivity;
import com.njlabs.amrita.aid.util.PersistentCookieStore;
import com.njlabs.amrita.aid.util.ark.logging.Ln;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class Landing extends BaseActivity {

    @Override
    public void init(Bundle savedInstanceState) {
        setupLayout(R.layout.activity_landing, "Amrita Info Desk");
        toolbar.setBackgroundColor(getResources().getColor(R.color.white));
        setRecentHeaderColor(getResources().getColor(R.color.white));
        checkForUpdates();

        AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.header)
                .addProfiles(
                        new ProfileDrawerItem().withName("Amrita Info Desk").withEmail("Version " + BuildConfig.VERSION_NAME).setSelectable(false)
                )
                .withSelectionListEnabledForSingleProfile(false)
                .build();

        headerResult.getHeaderBackgroundView().setColorFilter(Color.rgb(170, 170, 170), android.graphics.PorterDuff.Mode.MULTIPLY);
        Drawer result = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withAccountHeader(headerResult)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName("Home").withIcon(R.drawable.ic_action_home).withCheckable(false),
                        new PrimaryDrawerItem().withName("News").withIcon(R.drawable.ic_action_speaker_notes).withCheckable(false),
                        new DividerDrawerItem(),
                        new PrimaryDrawerItem().withName("About the app").withIcon(R.drawable.ic_action_info).withCheckable(false),
                        new PrimaryDrawerItem().withName("Settings").withIcon(R.drawable.ic_action_settings).withCheckable(false)
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(AdapterView<?> parent, View view, int position, long id, IDrawerItem drawerItem) {
                        switch (position) {
                            case 1:
                                startActivity(new Intent(baseContext, NewsActivity.class));
                                break;
                            case 3:
                                startActivity(new Intent(baseContext, App.class));
                                break;
                            case 4:
                                startActivity(new Intent(baseContext, SettingsActivity.class));
                                break;
                        }
                        return false;
                    }
                })
                .withCloseOnClick(true)
                .build();

        setupGrid();

        long periodSecs = 21600L;
        long flexSecs = 30L;
        String tag = "periodic  | NewsUpdateService: " + periodSecs + "s, f:" + flexSecs;
        PeriodicTask periodic = new PeriodicTask.Builder()
                .setService(NewsUpdateService.class)
                .setPeriod(periodSecs)
                .setFlex(flexSecs)
                .setTag(tag)
                .setPersisted(true)
                .setRequiredNetwork(Task.NETWORK_STATE_CONNECTED)
                .setRequiresCharging(false)
                .build();

        GcmNetworkManager.getInstance(this).schedule(periodic);


        File aumsCookieFile = new File(getApplicationContext().getFilesDir().getParent()+"/shared_prefs/" + PersistentCookieStore.AUMS_COOKIE_PREFS+ ".xml" );
        if(aumsCookieFile.exists()) {
            aumsCookieFile.delete();
        }

        File gpmsCookieFile = new File(getApplicationContext().getFilesDir().getParent()+"/shared_prefs/" + PersistentCookieStore.GPMS_COOKIE_PREFS+ ".xml" );
        if(gpmsCookieFile.exists()) {
            gpmsCookieFile.delete();
        }

    }

    private void setupGrid() {

        GridView gridView = (GridView) findViewById(R.id.landing_grid);
        gridView.setAdapter(new LandingAdapter(baseContext, 1));
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String name = ((TextView) view.getTag(R.id.landing_text)).getText().toString();
                switch (name) {
                    case "About Amrita":
                        // ABOUT AMRITA
                        startActivity(new Intent(baseContext, Amrita.class));
                        break;
                    case "Amrita Explorer":
                        Snackbar.make(parentView, "Amrita Explorer is under maintenance", Snackbar.LENGTH_SHORT).show();
                        break;
                    case "Academic Calender":
                        // ACADEMIC CALENDER
                        startActivity(new Intent(baseContext, Calender.class));
                        break;
                    case "Amrita UMS Login":
                        // AUMS
                        startActivity(new Intent(baseContext, AumsActivity.class));
                        break;
                    case "Train & Bus Timings":
                        // TRAIN & BUS INFO
                        final CharSequence[] transportationOptions = {"Trains from Coimbatore", "Trains from Palghat", "Trains to Coimbatore", "Trains to Palghat", "Buses from Coimbatore", "Buses to Coimbatore"};
                        AlertDialog.Builder transportationDialogBuilder = new AlertDialog.Builder(baseContext);
                        transportationDialogBuilder.setTitle("View timings of ?");
                        transportationDialogBuilder.setItems(transportationOptions, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int item) {
                                // Showing Alert Message
                                Intent trainBusOpen = new Intent(baseContext, TrainBusInfo.class);
                                trainBusOpen.putExtra("type", transportationOptions[item]);
                                startActivity(trainBusOpen);
                            }
                        });
                        AlertDialog transportationDialog = transportationDialogBuilder.create();
                        transportationDialog.show();
                        break;

                    case "GPMS Login":
                        // GPMS LOGIN
                        startActivity(new Intent(baseContext, GpmsActivity.class));
                        break;
                    case "Curriculum Info":
                        // CURRICULUM INFO
                        final CharSequence[] items_c = {"Aerospace Engineering", "Civil Engineering", "Chemical Engineering", "Computer Science Engineering", "Electrical & Electronics Engineering", "Electronics & Communication Engineering", "Electronics & Instrumentation Engineering", "Mechanical Engineering"};
                        AlertDialog.Builder departmentDialogBuilder = new AlertDialog.Builder(baseContext);
                        departmentDialogBuilder.setTitle("Select your Department");
                        departmentDialogBuilder.setItems(items_c, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int item) {
                                // Showing Alert Message
                                Intent curriculum_open = new Intent(baseContext, Curriculum.class);
                                curriculum_open.putExtra("department", items_c[item]);
                                startActivity(curriculum_open);
                            }
                        });
                        AlertDialog departmentDialog = departmentDialogBuilder.create();
                        departmentDialog.show();
                        break;

                    case "News":
                        // NEWS
                        startActivity(new Intent(baseContext, NewsActivity.class));
                        break;
                    default:
                        Toast.makeText(baseContext, String.valueOf(i), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void checkForUpdates() {

        OkHttpClient client = new OkHttpClient.Builder()
                .followRedirects(true)
                .followSslRedirects(true)
                .build();

        Request.Builder request = new Request.Builder()
                .url("https://api.codezero.xyz/aid/latest");

        client.newCall(request.build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Ln.e(e);
            }

            @Override
            public void onResponse(Call call, final Response rawResponse) throws IOException {
                final String responseString = rawResponse.body().string();
                ((Activity) baseContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        JSONObject response;

                        try {
                            response = new JSONObject(responseString);

                            String status = "";
                            status = response.getString("status");

                            if (status.equals("ok")) {
                                Double Latest = 0.0;
                                String Description = null;
                                try {
                                    Latest = response.getDouble("version");
                                    Description = response.getString("description");
                                } catch (JSONException e) {
                                    Crashlytics.logException(e);
                                }
                                if (Latest > BuildConfig.VERSION_CODE) {

                                    AlertDialog.Builder updateDialogBuilder = new AlertDialog.Builder(Landing.this);

                                    LayoutInflater factory = LayoutInflater.from(Landing.this);
                                    final View changelogView = factory.inflate(R.layout.webview_dialog, null);
                                    LinearLayout WebViewDialogLayout = (LinearLayout) changelogView.findViewById(R.id.WebViewDialogLayout);
                                    WebViewDialogLayout.setPadding(5, 5, 5, 5);
                                    WebView changelogWebView = (WebView) changelogView.findViewById(R.id.LicensesView);
                                    changelogWebView.loadData(String.format("%s", Description), "text/html", "utf-8");
                                    changelogWebView.setPadding(5,5,5,5);
                                    changelogWebView.setBackgroundColor(0);
                                    changelogWebView.setOnLongClickListener(new View.OnLongClickListener() {
                                        @Override
                                        public boolean onLongClick(View v) {
                                            return true;
                                        }
                                    });
                                    changelogWebView.setLongClickable(false);
                                    updateDialogBuilder.setView(changelogView).setCancelable(true)
                                            .setCancelable(false)
                                            .setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    dialog.cancel();
                                                }
                                            })
                                            .setPositiveButton("Update Now", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    Uri uri = Uri.parse("market://details?id=com.njlabs.amrita.aid");
                                                    Intent it = new Intent(Intent.ACTION_VIEW, uri);
                                                    startActivity(it);
                                                }
                                            });
                                    AlertDialog alert = updateDialogBuilder.create();
                                    alert.setTitle("Update Available");
                                    alert.setIcon(R.mipmap.ic_launcher);
                                    alert.show();
                                }
                            }

                        } catch (Exception e) {
                            Ln.e(e);
                        }
                    }
                });
            }
        });
    }

}
