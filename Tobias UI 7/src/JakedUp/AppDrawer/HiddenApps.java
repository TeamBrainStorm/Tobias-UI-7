package JakedUp.AppDrawer;

import java.io.File;
import java.util.ArrayList;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;
import com.d4a.tobias.R;

public class HiddenApps extends Activity {

    private SQLiteDatabase db;
    private SQLManager dbm;
    private PackageManager pm;
    private Context context = this;
    private ArrayList<DrawerItem> apps;
    private ArrayList<String> appsToShow;
    public static ArrayList<Application> checkedApps;
    private Intent result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.hidden_apps_list);

        appsToShow = new ArrayList<String>();

        checkedApps = new ArrayList<Application>();

        result = new Intent();

        listHiddenApps();

        draw();

        result.putStringArrayListExtra("appsToShow", appsToShow);
        setResult(Settings.RESULT_UNHIDE_APP, result);

        try {
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void listHiddenApps() {

        pm = getPackageManager();

        dbm = new SQLManager(this);

        db = dbm.getWritableDatabase();


        apps = new ArrayList<DrawerItem>();


        Cursor c = db.query("hiddenApps", new String[]{"packagename", "activityname"}, null, null, null, null, null);
        c.moveToNext();

        for (int i = 0; i < c.getCount(); i++) {
            ActivityInfo app;
            try {
                app = pm.getActivityInfo(new ComponentName(c.getString(0), c.getString(1)), 0);

                File f = new File(app.applicationInfo.sourceDir);
                Application appAux = new Application(app.loadIcon(pm), app.loadLabel(pm).toString(), app.packageName, c.getString(1), f.lastModified());
                if (!apps.contains(appAux)) {
                    apps.add(appAux);
                }
                c.moveToNext();

            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }
        }


        if (apps.isEmpty()) {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.no_hidden_apps),
                    Toast.LENGTH_LONG).show();

            finish();
        }


    }

    private void draw() {
        CustomViewAdapter cva = new CustomViewAdapter(this, R.layout.approw, apps, "Default", Color.WHITE, 22, 50, Main.LIST_STYLE, Main.MULTI_SELECT);

        final ListView lv = ((ListView) findViewById(R.id.hiddenappslist));
        lv.setAdapter(cva);
        lv.setDivider(null);
        lv.setDividerHeight(10);

        lv.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(final AdapterView<?> arg0, View arg1, final int arg2,
                    long arg3) {
                Application selectedApp = (Application) arg0.getItemAtPosition(arg2);

                CheckBox check = (CheckBox) arg1.findViewById(R.id.appCheck);

                check.toggle();
                if (check.isChecked()) {
                    checkedApps.add(selectedApp);
                } else {
                    checkedApps.remove(selectedApp);
                }


            }
        });

        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            public boolean onItemLongClick(AdapterView<?> av, View view, int i, long l) {
                final Application selectedApp = (Application) av.getItemAtPosition(i);


                AlertDialog.Builder builder = new AlertDialog.Builder(HiddenApps.this);

                builder.setItems(new CharSequence[]{getResources().getString(R.string.open)}, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        Intent appToOpen = selectedApp.getIntent(pm);
                        startActivity(appToOpen);
                        moveTaskToBack(true);
                    } 
                });
                
                builder.create().show();
                
                return false;
            }
        });

        Button unhide = (Button) findViewById(R.id.button_unhide);

        unhide.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!checkedApps.isEmpty()) {
                    final AlertDialog.Builder adb = new Builder(context);
                    adb.setCancelable(true).setPositiveButton(R.string.button_OK,
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(
                                        DialogInterface dialog,
                                        int which) {

                                    db = dbm.getWritableDatabase();

                                    for (Application app : checkedApps) {
                                        appsToShow.add(app.getPackageName() + "#" + app.getActivityName());

                                        //db.execSQL("delete from hiddenApps where ")
                                        db.delete(
                                                "hiddenApps",
                                                "activityname='"
                                                + app.getActivityName()
                                                + "'", null);
                                        listHiddenApps();

                                    }

                                    try {
                                        db.close();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                    finish();
                                }
                            }).setNegativeButton(R.string.button_cancel,
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(
                                        DialogInterface dialog,
                                        int which) {
                                    return;
                                }
                            }) //.setMessage(R.string.unhide_confirmation)
                            .setTitle(R.string.unhide_confirmation).show();
                }



            }
        });


        Button select_all = (Button) findViewById(R.id.button_select_all);

        select_all.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                if (checkedApps.size() < apps.size()) {
                    for (int i = 0; i < lv.getCount(); i++) {
                        Application app = (Application) lv.getItemAtPosition(i);
                        checkedApps.add(app);
                        CheckBox check = (CheckBox) lv.getAdapter().getView(i, null, null).findViewById(R.id.appCheck);
                        check.setChecked(true);
                    }

                    draw();

                }
            }
        });

    }
}
