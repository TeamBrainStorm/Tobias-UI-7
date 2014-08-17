package JakedUp.AppDrawer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.d4a.tobias.R;

public class Main extends Activity {

    public static final int ICON_SELECT = 25;
    public static int LIST_STYLE = 2, GRID_STYLE = 1, PAGED_STYLE = 0;
    private SharedPreferences sp;
    private final Context context = this;
    public OnItemClickListener itemClickListener;
    private HorizontalPager hp;
    private ArrayList<GridView> pages = new ArrayList<GridView>();
    private ArrayList<ArrayList<DrawerItem>> appsPages = new ArrayList<ArrayList<DrawerItem>>();
    public static PackageManager pm;
    private GridView gv;
    private ListView lv;
    public CustomViewAdapter cva;
    public static ArrayList<DrawerItem> apps;
    public static SQLiteDatabase db;
    private SQLManager dbm;
    private Application uninstalledApp;
    private HashMap<String, String> hiddenApps;
    private HashMap<String, String[]> editedApps;
    private HashMap<String, Integer> appUses;
    public HashMap<String, Folder> folders;
    private HashMap<String, Folder> appsInFolders;
    public static ArrayList<Application> checkedItems;
    public String newIconURI;
    private ImageView homeButton;
    public ImageButton editAppIcon;
//    public static boolean multiMode = false;
    public static int mode = 0;
    public static final int NORMAL = 0;
    public static final int MULTI_SELECT = 1;
    public static final int FOLDER_EDIT = 2;
    public static final int FOLDER_VIEW = 3;
    public static boolean isDragging = false;
    public static final int ANIMATION_SPEED = 200;
    public static final int FOLDER_ANIMATION_SPEED = 300;
	private static final Resources Resources = null;
    private FolderManager fManager;
    private Folder openedFolder;
    private long time;
//    private ServiceConnection servConn = new ServiceConnection() {
//
//        @Override
//        public void onServiceDisconnected(ComponentName name) {
//        }
//
//        @Override
//        public void onServiceConnected(ComponentName name, IBinder service) {
//
//            System.out.println("SERVICE READY");
//
//        }
//    };
    private static Boolean firstTime;
    private int appsPerPage;
    private int orientation;

    class AppInstalledReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //			
            //			if(intent.getAction().equals("android.intent.action.BOOT_COMPLETED"))
            //			{
            //				Intent startupIntent = new Intent(context, Main.class); // substitute with your launcher class
            //				startupIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //				context.startActivity(startupIntent);
            //			}
            if (intent.getAction().equals("android.intent.action.PACKAGE_ADDED")) {
                String packageName = intent.getDataString();
                packageName = packageName.split(":")[1];

                Application app = getAppByPackageName(packageName);
                if (app != null && !containsApp(app)) {
                    addApp(app);
                } else if (app == null) {
                    listApps();
                }


            } else if (intent.getAction().equals("android.intent.action.PACKAGE_REMOVED")) {

                if (uninstalledApp != null) {
                    Folder f;
                    if ((f = uninstalledApp.getContainingFolder()) != null) {
                        f.removeApp(uninstalledApp);
                        addFolderToDB(f);//Actualizamos el folder sin la app
                    } else {
                        removeApp(uninstalledApp);
                    }
                    uninstalledApp = null;
                } else {
                    String packageName = intent.getDataString();
                    packageName = packageName.split(":")[1];

                    Application app = findAppByPackageName(packageName);
                    if (app != null) {
                        removeApp(app);
                    }
                }
                notifyDataSetChanged();
//				doLayout();
            }


        }
    }

    /**
     * Called when the activity is first created. 
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {

//        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
//
//            public void uncaughtException(Thread th, Throwable ex) {
//                Log.e("AppDrawer", "Uncaught exception at "+th.toString(), ex);
//            }   
//        });

        pm = getPackageManager();

        sp = getSharedPreferences("AppDrawer_Prefs", Activity.MODE_PRIVATE);
        Settings.getPrefs(sp);
        if (Settings.bg_type == 2) {
            setTheme(android.R.style.Theme_Wallpaper);
        }
        super.onCreate(savedInstanceState);
        //		long ini = System.nanoTime();


//        Intent service = new Intent(this, AppDrawerService.class);
//        startService(service);

//        service = new AppDrawerService();
//        service.startForeground(1, new Notification());

        dbm = new SQLManager(context);

        db = dbm.getWritableDatabase();
        dbm.onCreate(db);

        checkedItems = new ArrayList<Application>();


//        bindService(service, servConn, 0);


        if (Settings.fullscreen) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }


        try {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
        } catch (Exception e) {

            e.printStackTrace();
        }

        if (Settings.screen_orientation == 1) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else if (Settings.screen_orientation == 2) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }


        if ((apps = AppDrawerService.getApps()) == null || (firstTime == null || firstTime)) {

            time = System.currentTimeMillis();

            apps = new ArrayList<DrawerItem>();

            getEditedAppsFromDB();

            getHiddenAppsFromDB();

            getAppUsesFromDB();

            getFoldersFromDB();

            Log.d("TIMESTAMP LEER DB", (System.currentTimeMillis() - time) + "");
            time = System.currentTimeMillis();

            listApps();

        } else {
            Log.d("APPDRAWERSERVICE", "Apps restauradas desde el servicio");
        }

//        if (Settings.list_style != PAGED_STYLE) {
        doLayout();
//        }





        onConfigurationChanged(new Configuration());



        IntentFilter newAppFilter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
        newAppFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);


//        newAppFilter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE);
//        newAppFilter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE);
//        newAppFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
//        newAppFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);

        newAppFilter.addDataScheme("package");
        getApplicationContext().registerReceiver(new AppInstalledReceiver(), newAppFilter);

        //		long fin = System.nanoTime();
        //		
        //		Toast.makeText(getApplicationContext(), String.valueOf((fin-ini)/1000000000.0),
        //				Toast.LENGTH_LONG).show();



        firstTime = false;

    }

    private void getFoldersFromDB() {

        folders = new HashMap<String, Folder>();
        appsInFolders = new HashMap<String, Folder>();

        Cursor c = db.query("folders", new String[]{"_id", "name", "icon", "apps"}, null, null, null, null, null);
        c.moveToNext();
        Folder f = null;
        for (int i = 0; i < c.getCount(); i++) {
            f = new Folder(c.getString(1));
            f.setId(c.getString(0));
            f.setIconURI(c.getString(2));
            if ("default".equals(f.getIconURI()) || "".equals(f.getIconURI())) {
                f.setIcon(getResources().getDrawable(R.drawable.folder));
            } else {
                Uri editedIcon = Uri.parse(f.getIconURI());
                f.setIcon(createDrawableFromURI(editedIcon));
            }

            String[] apps = c.getString(3).split(", ");
            for (String activityName : apps) {
                appsInFolders.put(activityName, f);
            }

            folders.put(c.getString(0), f);
            c.moveToNext();

        }

    }

    private void getHiddenAppsFromDB() {

        hiddenApps = new HashMap<String, String>();

        Cursor c = db.query("hiddenApps", new String[]{"packagename", "activityname"}, null, null, null, null, null);
        c.moveToNext();
        for (int i = 0; i < c.getCount(); i++) {
            hiddenApps.put(c.getString(1), c.getString(0));
            c.moveToNext();

        }

    }

    private void getEditedAppsFromDB() {

        editedApps = new HashMap<String, String[]>();

        Cursor c = db.query("editedApps", new String[]{"activityname", "icon", "name"}, null, null, null, null, null);
        c.moveToNext();
        for (int i = 0; i < c.getCount(); i++) {
            String icon;
            if (c.getString(1).equals("")) {
                icon = "default";
            } else {
                icon = c.getString(1);
            }

            editedApps.put(c.getString(0), new String[]{icon, c.getString(2)});
            c.moveToNext();

        }

    }

    private void getAppUsesFromDB() {

        appUses = new HashMap<String, Integer>();

        Cursor c = db.query("appUses", new String[]{"activityname", "uses"}, null, null, null, null, null);
        c.moveToNext();
        for (int i = 0; i < c.getCount(); i++) {

            appUses.put(c.getString(0), c.getInt(1));
            c.moveToNext();

        }

    }

    private void addHiddenAppToDB(String packagename, String activityname) {
        ContentValues cv = new ContentValues();
        cv.put("packagename", packagename);
        cv.put("activityname", activityname);

        db.insert("hiddenApps", null, cv);
    }

    public void addFolderToDB(Folder f) {
        ContentValues cv = new ContentValues();
//        cv.putNull("_id");
        cv.put("name", f.getName());
        cv.put("icon", f.getIconURI());
        ArrayList<Application> appsL = f.getApps();
        String apps = "";
        for (Application app : appsL) {
            apps += app.getActivityName() + ", ";
        }
        cv.put("apps", apps);

        if (f.getId() != null && !f.getId().equals("")) {
            db.update("folders", cv, "_id='" + f.getId() + "'", null);
        } else {
            f.setId(String.valueOf(db.insert("folders", null, cv)));
        }
    }

    public void deleteFolderFromDB(Folder f) {

        db.delete("folders", "_id = '" + f.getId() + "'", null);
    }

    private void addEditedAppToDB(String activityname, String icon, String name) {
        ContentValues cv = new ContentValues();

        cv.put("activityname", activityname);
        cv.put("icon", icon);
        cv.put("name", name);
        db.insert("editedApps", null, cv);
    }

    private void updateEditedAppToDB(String activityname, String icon, String name) {
        ContentValues cv = new ContentValues();

        cv.put("activityname", activityname);
        if (icon != "") {
            cv.put("icon", icon);
        }
        cv.put("name", name);
        db.update("editedApps", cv, "activityname='" + activityname + "'", null);
    }

    private void addAppUseToDB(String activityname) {
        ContentValues cv = new ContentValues();

        cv.put("activityname", activityname);
        cv.put("uses", 1);
        db.insert("appUses", null, cv);
    }

    private void updateAppUseToDB(String activityname) {
        ContentValues cv = new ContentValues();

        cv.put("activityname", activityname);
        cv.put("uses", appUses.get(activityname));
        db.update("appUses", cv, "activityname='" + activityname + "'", null);
    }

    @Override
    protected void onResume() {

        if (!Settings.remember_position) {
            doLayout();

            if (Settings.list_style == PAGED_STYLE) {
                hp.setCurrentScreen(0, false);
            }
        }
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        super.onResume();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        //		if(Settings.list_style==1)
        //		{
        //			int orient = newConfig.orientation;
        //			if(orient==Configuration.ORIENTATION_LANDSCAPE)
        //				gv.setNumColumns(num_columns_h);
        //			if(orient==Configuration.ORIENTATION_PORTRAIT)
        //				gv.setNumColumns(num_columns_v);	
        //		}

        if (Settings.list_style == LIST_STYLE) {
            setContentView(R.layout.main);
        } else if (Settings.list_style == GRID_STYLE) {
            setContentView(R.layout.main_grid);
        } else if (Settings.list_style == PAGED_STYLE) {
            setContentView(R.layout.main_paged);
        }

        doLayout();


        super.onConfigurationChanged(newConfig);

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();

        menu.clear();
        if (mode == NORMAL) {
            inflater.inflate(R.menu.menu, menu);
            SubMenu sm = menu.addSubMenu(R.string.sort);
            sm.setIcon(android.R.drawable.ic_menu_sort_by_size);
            OnMenuItemClickListener menuItemClickListener = new OnMenuItemClickListener() {

                @Override
                public boolean onMenuItemClick(MenuItem item) {

                    Settings.sortBy = item.getItemId();
                    sp.edit().putInt("sortBy", item.getItemId()).commit();

                    sortApps();

                    notifyDataSetChanged();

                    return false;
                }
            };
            sm.add(Menu.NONE, 0, Menu.NONE, R.string.sortByName).setOnMenuItemClickListener(menuItemClickListener);
            sm.add(Menu.NONE, 1, Menu.NONE, R.string.sortByDate).setOnMenuItemClickListener(menuItemClickListener);
            sm.add(Menu.NONE, 2, Menu.NONE, R.string.sortByDate2).setOnMenuItemClickListener(menuItemClickListener);
            sm.add(Menu.NONE, 3, Menu.NONE, R.string.sortByUses).setOnMenuItemClickListener(menuItemClickListener);
            MenuItem foldersFirst = sm.add(Menu.NONE, 4, Menu.NONE, R.string.sortFoldersFirst);
            foldersFirst.setCheckable(true);
            foldersFirst.setChecked(Settings.sort_folders_first);
            foldersFirst.setOnMenuItemClickListener(new OnMenuItemClickListener() {

                public boolean onMenuItemClick(MenuItem item) {
                    Settings.sort_folders_first = !item.isChecked();
                    sp.edit().putBoolean("sort_folders_first", !item.isChecked()).commit();

                    sortApps();

                    notifyDataSetChanged();

                    return true;

                }
            });
        } else if (mode == MULTI_SELECT) {
            inflater.inflate(R.menu.menu_multi, menu);
            menu.findItem(R.id.hide).setTitle(getResources().getStringArray(R.array.app_actions)[0]);
            menu.findItem(R.id.uninstall).setTitle(getResources().getStringArray(R.array.app_actions)[2]);
            menu.findItem(R.id.edit).setTitle(getResources().getStringArray(R.array.app_actions)[4]);
            menu.findItem(R.id.add_to_folder).setTitle(getResources().getStringArray(R.array.app_actions)[5]);
//            menu.findItem(R.id.new_folder).setTitle(getResources().getStringArray(R.array.app_actions)[6]);

        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.refresh:

                time = System.currentTimeMillis();

                getEditedAppsFromDB();

                getHiddenAppsFromDB();

                getAppUsesFromDB();

                getFoldersFromDB();


                Log.d("TIMESTAMP LEER DB", (System.currentTimeMillis() - time) + "");
                time = System.currentTimeMillis();


                if (Settings.list_style == PAGED_STYLE) {
                    clearHorizontalPages();
                }

                listApps();
                //apps = ListAppsService.getApps();
//			doLayout();

                notifyDataSetChanged();

                return true;
            case R.id.settings:
                startActivityForResult(new Intent(this, JakedUp.AppDrawer.Settings.class), 1);
                return true;
            case R.id.hidden_apps:
                startActivityForResult(new Intent(this, JakedUp.AppDrawer.HiddenApps.class), 1);
                return true;
            case R.id.manage:
                startActivity(new Intent(android.provider.Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS));
                return true;
            case R.id.multi:
                mode = MULTI_SELECT;
                checkedItems = new ArrayList<Application>();
//                doLayout();
                notifyDataSetChanged();
                return true;
            case R.id.hide:
                for (DrawerItem app : checkedItems) {
                    hideApp(app);
                }
//                multiMode = false;
                mode = NORMAL;
                checkedItems.clear();
                notifyDataSetChanged();
//                doLayout();
                return true;
            case R.id.uninstall:
                for (DrawerItem app : checkedItems) {
                    uninstallApp(app);
                }
                mode = NORMAL;
                checkedItems.clear();
                notifyDataSetChanged();
//                doLayout();
                return true;
            case R.id.edit:
                for (DrawerItem app : checkedItems) {
                    editApp(app);
                }
                mode = NORMAL;
                checkedItems.clear();
                notifyDataSetChanged();
//                doLayout();
                return true;
            case R.id.add_to_folder:
                Application[] checks = checkedItems.toArray(new Application[checkedItems.size()]);
                showExistingFolders(checks);
                checkedItems.clear();
                mode = NORMAL;
                notifyDataSetChanged();
                return true;
//            case R.id.new_folder:
//                mode = NORMAL;
//                notifyDataSetChanged();
//                Application[] checks2 = checkedItems.toArray(new Application[checkedItems.size()]);
//                createNewFolder(checks2);
//                checkedItems.clear();
//                notifyDataSetChanged();
//                return true;
            case R.id.select_all:
                if (checkedItems.size() < apps.size()) {
                    if (Settings.list_style == LIST_STYLE) {
                        for (int i = 0; i < apps.size(); i++) {
                            if (!(lv.getItemAtPosition(i) instanceof Application)) {
                                continue;
                            }
                            Application app = (Application) lv.getItemAtPosition(i);
                            checkedItems.add(app);
                            CheckBox check = (CheckBox) lv.getAdapter().getView(i, null, null).findViewById(R.id.appCheck);
                            check.setChecked(true);

                        }
                    } else if (Settings.list_style == GRID_STYLE) {
                        for (int i = 0; i < apps.size(); i++) {
                            if (!(lv.getItemAtPosition(i) instanceof Application)) {
                                continue;
                            }
                            Application app = (Application) gv.getItemAtPosition(i);
                            checkedItems.add(app);
                            CheckBox check = (CheckBox) gv.getAdapter().getView(i, null, null).findViewById(R.id.appCheck);
                            check.setChecked(true);
                        }
                    } else if (Settings.list_style == PAGED_STYLE) {
//					for(int i=0;i<apps.size();i++)
//					{
//						Application app = (Application)hp.getItemAtPosition(i);
//						checkedApps.add(app);
//						CheckBox check = (CheckBox) hp.getAdapter().getView(i, null,null).findViewById(R.id.appCheck);
//						check.setChecked(true);
//					}
                    }

                    notifyDataSetChanged();

                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Settings.getPrefs(sp);
                doLayout();

            } else if (resultCode == Settings.RESULT_NEED_RESTART) {

                restartApp();

            } else if (resultCode == Settings.RESULT_NEED_RESTART_AND_CLEAR_DB) {
                dbm.resetDB(db);
                db.close();
                restartApp();
            } else if (resultCode == Settings.RESULT_UNHIDE_APP) {
                //getHiddenApps();
                //listApps();
                Settings.getPrefs(sp);
                final ArrayList<String> appsToShow = data.getStringArrayListExtra("appsToShow");
                new AsyncTask<Void, Application, Void>() {

                    @Override
                    protected Void doInBackground(Void... paramss) {
                        for (String appInfo : appsToShow) {
                            String[] packageAndActivity = appInfo.split("\\#");
                            ActivityInfo ai;
                            try {
                                ai = pm.getActivityInfo(new ComponentName(packageAndActivity[0], packageAndActivity[1]), 0);
                                File f = new File(ai.applicationInfo.sourceDir);
                                Application app = new Application(ai.loadIcon(pm), ai.loadLabel(pm).toString(), ai.packageName, ai.name, f.lastModified());

                                publishProgress(app);

                            } catch (NameNotFoundException e) {

                                e.printStackTrace();
                            }

                        }

                        return (null);
                    }

                    @Override
                    protected void onProgressUpdate(Application... app) {
                        if (!containsApp(app[0])) {
                            addApp(app[0]);
                            Log.d("UNHIDE_APP", "Added " + app[0].getName() + " application.");
                        } else {
                            Log.d("UNHIDE_APP", "Application " + app[0].getName() + " already exists.");
                        }
                        hiddenApps.remove(app[0].getActivityName());
                    }
                }.execute();

            }
        } else if (requestCode == ICON_SELECT) {

            try {
                Uri selectedIcon = data.getData();
                newIconURI = selectedIcon.toString();
                //Bitmap selectedBitmap = createBitmapFromURI(selectedIcon);
                Drawable selectedDrawable = createDrawableFromURI(selectedIcon);
                //editAppIcon.setImageBitmap(selectedBitmap);
                try {
                    editAppIcon.setImageDrawable(selectedDrawable);
                } catch (Exception e) {

                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private Drawable createDrawableFromURI(Uri selectedIcon) {
        InputStream imageStream = null;
        try {
            imageStream = getContentResolver().openInputStream(selectedIcon);

        } catch (FileNotFoundException e) {

            e.printStackTrace();
        }
        Drawable selectedDrawable;
        try {
            selectedDrawable = Drawable.createFromStream(imageStream, "");
            imageStream.close();
        } catch (Exception e) {

            e.printStackTrace();
            return null;
        }

        return selectedDrawable;
    }

    private void restartApp() {
        Intent intent = getIntent();
        overridePendingTransition(0, 0);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        firstTime = true;

        finish();

        overridePendingTransition(0, 0);
        startActivity(intent);

    }

    public void showHomeButton() {


        homeButton = (ImageView) findViewById(R.id.homeButton);

        if (!Settings.show_home_button) {
            homeButton.setVisibility(ImageView.GONE);
        } else {
            homeButton.setVisibility(ImageView.VISIBLE);

            ViewGroup.LayoutParams lp;
            if (Settings.list_style == PAGED_STYLE) {
                lp = (LinearLayout.LayoutParams) homeButton.getLayoutParams();
            } else {
                lp = (FrameLayout.LayoutParams) homeButton.getLayoutParams();


                switch (getResources().getConfiguration().orientation) {
                    case 1:
                        switch (Settings.home_position) {
                            case 0:
                                ((FrameLayout.LayoutParams) lp).gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
                                break;
                            case 1:
                                ((FrameLayout.LayoutParams) lp).gravity = Gravity.LEFT | Gravity.BOTTOM;
                                ;
                                break;
                            case 2:
                                ((FrameLayout.LayoutParams) lp).gravity = Gravity.RIGHT | Gravity.BOTTOM;
                                break;
                        }
                        break;
                    case 2:
                        switch (Settings.home_position) {
                            case 0:
                                ((FrameLayout.LayoutParams) lp).gravity = Gravity.CENTER_VERTICAL | Gravity.RIGHT;
                                break;
                            case 1:
                                ((FrameLayout.LayoutParams) lp).gravity = Gravity.TOP | Gravity.RIGHT;
                                break;
                            case 2:
                                ((FrameLayout.LayoutParams) lp).gravity = Gravity.BOTTOM | Gravity.RIGHT;
                                break;
                        }
                        break;
                }
            }


            homeButton.setLayoutParams(lp);


            homeButton.setOnTouchListener(new OnTouchListener() {

                public boolean onTouch(View v, MotionEvent event) {

                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        ((ImageView) v).setImageResource(R.drawable.home_button_pressed);
                    }
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        ((ImageView) v).setImageResource(R.drawable.home_button);
                    }
                    return false;
                }
            });


            homeButton.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {

                    goDesktop();
                    //					openOptionsMenu();
                }
            });



        }

    }

    public void hideHomeButton() {
        homeButton = (ImageView) findViewById(R.id.homeButton);
        homeButton.setVisibility(ImageView.GONE);
    }

//    private void listApps() {
//
//        apps.clear();
//
//
//        Intent intent = new Intent(Intent.ACTION_MAIN, null);
//        intent.addCategory(Intent.CATEGORY_LAUNCHER);
//
//
//        final ArrayList<ResolveInfo> listaRI = (ArrayList<ResolveInfo>) pm.queryIntentActivities(intent, PackageManager.PERMISSION_GRANTED);
//
//        Log.d("TIMESTAMP OBTENER APPS", (System.currentTimeMillis() - time) + "");
//        time = System.currentTimeMillis();
//
//        for (ResolveInfo app : listaRI) {
//            String activityName = app.activityInfo.name;
//            if (!hiddenApps.containsKey(activityName))// && app.loadLabel(pm).toString().matches(".*[Ww].*"))
//            {
//                String name = "";
//                Drawable icon = null;
//                boolean edited = false;
//                String[] editedArray = editedApps.get(activityName);
//                if (editedArray == null) {
//                    icon = app.loadIcon(pm);
//                    name = app.loadLabel(pm).toString();
//                } else {
//
//
//                    if (!editedArray[0].equals("default")) {
//                        Uri editedIcon = Uri.parse(editedApps.get(activityName)[0]);
//                        icon = createDrawableFromURI(editedIcon);
//
//                    } else {
//                        icon = app.loadIcon(pm);
//
//                    }
//
//                    name = editedArray[1];
//                    edited = true;
//                }
//                ApplicationInfo ai = null;
//                try {
//                    ai = pm.getApplicationInfo(app.activityInfo.packageName, 0);
//
//                } catch (NameNotFoundException e) {
//
//                    e.printStackTrace();
//                }
//                File f = new File(ai.sourceDir);
//                Application appItem = new Application(icon, name, app.activityInfo.packageName, activityName, f.lastModified());
//                Integer appuses = appUses.get(activityName);
//                if (appuses != null) {
//                    appItem.setUses(appuses);
//                }
//
//                Folder folder = null;
//                if ((folder = appsInFolders.get(activityName)) != null) {
//                    folder.addApp(appItem);
//                    appItem.setContainingFolder(folder);
//                    if (!containsApp(folder)) {
//                        addApp(folder);
////                                sortApps();
//                    }
//                    continue;
//                }
//
//                if (!containsApp(appItem)) {
//                    addApp(appItem);
//                }
//
//            }
//        }
//
//        Log.d("TIMESTAMP PREPARAR APPS", (System.currentTimeMillis() - time) + "");
//        time = System.currentTimeMillis();
//
//
//        sortApps();
//        Log.d("TIMESTAMP ORDENAR APPS", (System.currentTimeMillis() - time) + "");
//
//
//        Log.d("TIMESTAMP", "---------------------");
//        
//    }
    private void listApps() {

        apps.clear();


//        notifyDataSetChanged();

        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);


        final ArrayList<ResolveInfo> listaRI = (ArrayList<ResolveInfo>) pm.queryIntentActivities(intent, PackageManager.PERMISSION_GRANTED);

        Log.d("TIMESTAMP OBTENER APPS", (System.currentTimeMillis() - time) + "");
        time = System.currentTimeMillis();

//        Collections.sort(listaRI, new ResolveInfo.DisplayNameComparator(pm));
        /*new Comparator<ResolveInfo>() {
        
        public int compare(ResolveInfo t, ResolveInfo t1) {
        //                return t.loadLabel(pm).toString().compareToIgnoreCase(t1.loadLabel(pm).toString());
        return Collator.getInstance().compare(t.loadLabel(pm).toString(), t1.loadLabel(pm).toString());
        }
        });*/

        Log.d("TIMESTAMP ORDENAR APPS", (System.currentTimeMillis() - time) + "");
        time = System.currentTimeMillis();

//        for (Folder f : folders.values())
//        {
//            addApp(f);
//        }
//        
//        sortApps();
//        
//        Log.d("TIMESTAMP SET FOLDERS", (System.currentTimeMillis() - time) + "");
//        time = System.currentTimeMillis();

        new AsyncTask<Void, Object, Void>() {

            @SuppressLint("NewApi")
			@Override
            protected Void doInBackground(Void... paramss) {
                for (ResolveInfo app : listaRI) {
                    String activityName = app.activityInfo.name;
                    if (!hiddenApps.containsKey(activityName))// && app.loadLabel(pm).toString().matches(".*[Ww].*"))
                    {
                        String name = "";
                        Drawable icon = null;
                        boolean edited = false;
                        String[] editedArray = editedApps.get(activityName);
                        if (editedArray == null) {
                            icon = app.loadIcon(pm);
                            name = app.loadLabel(pm).toString();
                        } else {


                            if (!editedArray[0].equals("default")) {
                                Uri editedIcon = Uri.parse(editedApps.get(activityName)[0]);
                                icon = createDrawableFromURI(editedIcon);

                            } else {
                                icon = app.loadIcon(pm);

                            }

                            name = editedArray[1];
                            edited = true;
                        }
                        PackageInfo pi = null;
                        try {
                            pi = pm.getPackageInfo(app.activityInfo.packageName, 0);
                        } catch (NameNotFoundException ex) {
                            Log.e("APPDRAWER", "PackageInfo for installation date not found", ex);
                        }

                        Application appItem = new Application(icon, name, app.activityInfo.packageName, activityName, pi.firstInstallTime);
                        Integer uses = appUses.get(activityName);
                        if (uses != null) {
                            appItem.setUses(uses);
                        }

                        Folder folder = null;
                        if ((folder = appsInFolders.get(activityName)) != null) {
                            Integer folderUses = appUses.get(folder.getId());
                            if (folderUses != null) {
                                folder.setUses(folderUses);
                            }
                            folder.addApp(appItem);
                            appItem.setContainingFolder(folder);
                            publishProgress(folder, true);
                            continue;
                        }

                        publishProgress(appItem, edited);

                    }
                }

                return (null);
            }

            @Override
            protected void onProgressUpdate(Object... obj) {
                DrawerItem item = (DrawerItem) obj[0];
                Boolean sort = (Boolean) obj[1];
                if (!containsApp(item)) {
                    addApp(item);
//                    if (sort) {
//                    sortApps();
//                    }
                } else {
                    Log.d("LIST_ITEMS", item.getClass().getName() + " " + item.getName() + " already exists.");
                }

            }

            @Override
            protected void onPostExecute(Void result) {
                Log.d("TIMESTAMP PINTAR APPS", (System.currentTimeMillis() - time) + "");

                AppDrawerService.setApps(apps);

//                if (Settings.list_style == PAGED_STYLE) {
//                    doLayout();
//                }

                final String[] latestVersion;
                if ((latestVersion = Settings.checkNewVersion()) != null) {

                    AlertDialog.Builder adb = new Builder(context);

                    adb.setCancelable(true).setNegativeButton(R.string.button_cancel, null).setPositiveButton(R.string.button_OK, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            Intent downloadIntent = new Intent("android.intent.action.VIEW", Uri.parse(latestVersion[1]));
                            startActivity(downloadIntent);
                        }
                    }).setMessage(getResources().getString(R.string.new_version) + " " + latestVersion[0] + "?");

                    adb.create().show();

                }
            }
        }.execute();



        Log.d("TIMESTAMP PREPARAR APPS", (System.currentTimeMillis() - time) + "");
        time = System.currentTimeMillis();

//
//        sortApps();
//        Log.d("TIMESTAMP ORDENAR APPS", (System.currentTimeMillis() - time) + "");
//

        Log.d("TIMESTAMP", "---------------------");
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

        if (mode == FOLDER_VIEW) {
            mode = NORMAL;
            doLayout();
        }
    }

    public void doLayout() {

        //TextView numApps = (TextView)findViewById(R.id.numApps);
        //numApps.setText("Numero de apps instaladas: "+apps.size());

        //System.gc();

        if (mode == FOLDER_EDIT || mode == FOLDER_VIEW) {
            mode = NORMAL;
        }

        if (Settings.list_style == LIST_STYLE) {
            setContentView(R.layout.main);
        } else if (Settings.list_style == GRID_STYLE) {
            setContentView(R.layout.main_grid);
        } else if (Settings.list_style == PAGED_STYLE) {
            setContentView(R.layout.main_paged);
        }

        ImageView background = (ImageView) findViewById(R.id.background);
        //ImageView background_protector = (ImageView)findViewById(R.id.background_protector);
        background.setDrawingCacheEnabled(true);
        if (Settings.low_quality) {
            background.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_LOW);
        } else {
            background.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_AUTO);
        }

        switch (Settings.bg_type) {

            case 1:
                //BitmapDrawable bg = new BitmapDrawable(Settings.bg_image);
                BitmapDrawable bg = new BitmapDrawable(Resources, Settings.bg_image);

                bg.setDither(false);
                //bg.setAntiAlias(false);
                bg.setAlpha(Settings.bg_alpha);
                background.setScaleType(ScaleType.FIT_XY);
                background.setImageDrawable(bg);
                //background_protector.setBackgroundColor(Color.BLACK);

                break;

            case 2:
                //Drawable wallpaper = WallpaperManager.getInstance(this).getDrawable();
                //wallpaper.setAlpha(bg_alpha);
                //wallpaper.setDither(false);
                //background.setScaleType(ScaleType.CENTER);
                //background.setImageDrawable(wallpaper);

                String bg_alpha_hex = Integer.toHexString(255 - Settings.bg_alpha);
                if (bg_alpha_hex.length() == 1) {
                    bg_alpha_hex = "0" + bg_alpha_hex;
                }

                background.setBackgroundColor(Color.parseColor("#" + bg_alpha_hex + "000000"));
                //background_protector.setBackgroundColor(android.R.color.transparent);

                break;
            default:
            case 0:
                background.setImageDrawable(null);
                background.setBackgroundColor(Settings.bg_color);
                //background_protector.setBackgroundColor(Color.BLACK);
                break;
        }



        View v = null;

        cva = new CustomViewAdapter(this, 0, apps, Settings.font, Settings.font_color, Settings.font_size, Settings.icon_size, Settings.list_style, mode);

        orientation = getResources().getConfiguration().orientation;
        if (Settings.list_style == LIST_STYLE) {

            v = (ListView) findViewById(R.id.list);
            lv = (ListView) v;
            lv.setAdapter(cva);
            lv.setDivider(null);
            lv.setDividerHeight(Settings.row_spacing);

            ((AdapterView<ListAdapter>) v).setOnItemClickListener(itemClickListener);

            DragListener drag = new DragListener(this);

            lv.setOnItemLongClickListener(drag);

            lv.setOnTouchListener(drag);

            lv.setTextFilterEnabled(true);

            lv.setFadingEdgeLength(50);

        } else if (Settings.list_style == GRID_STYLE) {
            v = (GridView) findViewById(R.id.grid);
            gv = (GridView) v;
            gv.setAdapter(cva);
            gv.setVerticalSpacing(Settings.row_spacing);
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                gv.setNumColumns(Settings.num_columns_h);
            } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                gv.setNumColumns(Settings.num_columns_v);
            }

            gv.setOnItemClickListener(itemClickListener);

            DragListener drag = new DragListener(this);

            gv.setOnItemLongClickListener(drag);

            gv.setOnTouchListener(drag);

            gv.setTextFilterEnabled(true);

            gv.setFadingEdgeLength(50);

        } else if (Settings.list_style == PAGED_STYLE) {
            hp = (HorizontalPager) findViewById(R.id.horizontalPager1);

            hp.removeAllViews();

            hp.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

            appsPerPage = 0;
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                appsPerPage = Settings.num_columns_h * Settings.num_rows_h;
            } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                appsPerPage = Settings.num_columns_v * Settings.num_rows_v;
            }



//            int pagesCount = (int) Math.ceil(apps.size() / (double) appsPerPage);
//            pages = new GridView[pagesCount];
//            appsPages = new ArrayList[pagesCount];
//
//            for (int i = 0; i < pagesCount; i++) {
//
//                pages[i] = new GridView(context);
//
//                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
//                    pages[i].setNumColumns(Settings.num_columns_h);
//                } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
//                    pages[i].setNumColumns(Settings.num_columns_v);
//                }
//                pages[i].setVerticalSpacing(Settings.row_spacing);
//
//                try {
//                    appsPages[i] = new ArrayList<DrawerItem>(apps.subList(i * appsPerPage, (i * appsPerPage) + appsPerPage));
//                } catch (IndexOutOfBoundsException e) {
//                    appsPages[i] = new ArrayList<DrawerItem>(apps.subList(i * appsPerPage, apps.size()));
//                }
//                cva = new CustomViewAdapter(this, R.layout.approw, appsPages[i], Settings.font, Settings.font_color, Settings.font_size, Settings.icon_size, Settings.list_style, mode);
//                pages[i].setAdapter(cva);
//
//                pages[i].setOnTouchListener(new OnTouchListener() {
//
//                    @Override
//                    public boolean onTouch(View view, MotionEvent event) {
//                        switch (event.getAction() & MotionEvent.ACTION_MASK) {
//                            case MotionEvent.ACTION_MOVE:
//                                event.setAction(MotionEvent.ACTION_CANCEL);
//                                return true;
//                            default:
//                                return false;
//                        }
//
//                    }
//                });
//
//                pages[i].setOnItemClickListener(itemClickListener);
//
//                DragListener drag = new DragListener(this);
//
//                pages[i].setOnItemLongClickListener(drag);
//
//                pages[i].setOnTouchListener(drag);
//
//                hp.addView(pages[i]);
//
//
//            }

            LinearLayout indicators = (LinearLayout) findViewById(R.id.pageIndicator);

            if (Settings.show_indicators) {
                indicators.setVisibility(View.VISIBLE);
                hp.setIndicators(indicators);
            } else {
                indicators.setVisibility(View.GONE);
            }

//            addPage();

        }


        showHomeButton();


        itemClickListener = new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, final View arg1, int arg2,
                    long arg3) {

                DrawerItem item = (DrawerItem) arg0.getItemAtPosition(arg2);
                if (item instanceof Folder) {
                    if (mode == NORMAL) {
                        Folder f = ((Folder) item);
                        f.addUse();
                        f.open(Main.this);
                        if (appUses.put(f.getId(), f.getUses()) == null) {
                            addAppUseToDB(f.getId());
                        } else {
                            updateAppUseToDB(f.getId());
                        }
                        if (Settings.sortBy == Settings.SORT_BY_MOST_USED) {
                            sortApps();

                            notifyDataSetChanged();
                            //						doLayout();

                        }
                    }
                } else if (item instanceof Application) {
                    Application selectedApp = (Application) item;
                    if (mode == NORMAL || (mode == FOLDER_VIEW && selectedApp.getContainingFolder() != null)) {
                        try {
                            Intent appToOpen = selectedApp.getIntent(pm);
                            startActivity(appToOpen);
                            selectedApp.addUse();
                            String activityName = selectedApp.getActivityName();
                            if (appUses.put(activityName, selectedApp.getUses()) == null) {
                                addAppUseToDB(activityName);
                            } else {
                                updateAppUseToDB(activityName);
                            }
                            if (Settings.sortBy == Settings.SORT_BY_MOST_USED) {
                                sortApps();

                                notifyDataSetChanged();
                                //doLayout();

                            }
                            //finish();
                            moveTaskToBack(true);
                        } catch (Exception e) {
                            //                        Toast.makeText(context, "Error when opening app. Try again.", Toast.LENGTH_SHORT).show();
                            Log.e("APPLAUNCH", "Error al abrir una aplicacion", e);
                            //                        e.printStackTrace();
                        }
                    } else if (mode == MULTI_SELECT) {
                        CheckBox check = (CheckBox) arg1.findViewById(R.id.appCheck);
                        ImageView icon = (ImageView) arg1.findViewById(R.id.appIcon);
                        TextView name = (TextView) arg1.findViewById(R.id.appName);
                        check.toggle();
                        if (check.isChecked()) {
                            checkedItems.add(selectedApp);
                            if (Settings.list_style != LIST_STYLE) {
                                arg1.setBackgroundResource(R.drawable.rounded);
                            }

                        } else {
                            checkedItems.remove(selectedApp);
                            if (Settings.list_style != LIST_STYLE) {
                                arg1.setBackgroundDrawable(null);
                            }
                        }


                    } else if (mode == FOLDER_EDIT && fManager != null) {
                        if (checkedItems.contains(selectedApp)) {
                            fManager.remove(selectedApp);
                        } else {
                            fManager.add(selectedApp);
                        }
                    }
                }

            }
        };

    }

    public void createShortcutOnDesktop(Application app) {

        Intent intent = new Intent();
        intent.setClassName(app.getPackageName(), app.getActivityName());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        Intent shortcutIntent = new Intent();
        shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent);
        shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, app.getName());

        Drawable icon = app.getIcon();
        if (icon instanceof BitmapDrawable) {
            BitmapDrawable bd = (BitmapDrawable) icon;
            shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON, bd.getBitmap());
        }

        //shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(context, R.drawable.home_button));
        shortcutIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
        sendBroadcast(shortcutIntent);

        goDesktop();

    }

    private void goDesktop() {
        Intent i = new Intent(Intent.ACTION_MAIN);
        i.addCategory(Intent.CATEGORY_HOME);
        startActivity(i);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    public void onBackPressed() {
        if (mode == MULTI_SELECT) {
            mode = NORMAL;
//            doLayout();
            notifyDataSetChanged();
        } else if (mode == FOLDER_EDIT) {
            if (fManager != null) {
                fManager.closeFolderManager();
            }

        } else if (mode == FOLDER_VIEW) {
            if (openedFolder != null) {
                openedFolder.close();
            }
        } else {
            goDesktop();
        }
    }

    public void uninstallApp(DrawerItem item) {
        if (item instanceof Folder) {
            return;
        }

        Application app = (Application) item;
        uninstalledApp = app;
        Uri packageURI = Uri.parse("package:" + app.getPackageName());
        Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
        startActivityForResult(uninstallIntent, ICON_SELECT);
    }

    public void showAppDetails(Application app) {
        Uri packageURI = Uri.parse("package:" + app.getPackageName());
        Intent showInfo = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        showInfo.setData(packageURI);
        startActivity(showInfo);
    }

    public void hideApp(DrawerItem item) {
        if (item instanceof Folder) {
            return;
        }

        Application app = (Application) item;
        addHiddenAppToDB(app.getPackageName(), app.getActivityName());
        hiddenApps.put(app.getActivityName(), app.getPackageName());
        //listApps();
        Folder f;
        if ((f = app.getContainingFolder()) != null) {
            f.removeApp(app);
        } else {
            removeApp(app);
            notifyDataSetChanged();
        }
    }

    public void editApp(DrawerItem item) {
        if (item instanceof Folder) {
            return;
        }

        final Application app = (Application) item;

        LayoutInflater inflater = LayoutInflater.from(context);
        View editAppView = inflater.inflate(R.layout.edit_app, null);

        final AlertDialog.Builder adb = new Builder(context);

        editAppIcon = (ImageButton) editAppView.findViewById(R.id.editAppIcon);

        final EditText name = (EditText) editAppView.findViewById(R.id.editAppName);

        editAppIcon.setImageDrawable(app.getIcon());

        name.setText(app.getName());

        name.setSelection(name.getText().length());

        editAppIcon.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent iconPickerIntent = new Intent(Intent.ACTION_PICK);

                iconPickerIntent.setType("image/*");
                //iconPickerIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
                //iconPickerIntent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
                startActivityForResult(iconPickerIntent, ICON_SELECT);
            }
        });

        adb.setTitle(getResources().getStringArray(R.array.app_actions)[4]).setCancelable(true).setView(editAppView).setPositiveButton(R.string.button_OK, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                String newName = name.getText().toString().trim();
                Drawable newIcon = editAppIcon.getDrawable();
                if (!app.getName().trim().equals(newName) || !app.getIcon().equals(newIcon)) {
                    Folder f;
                    if ((f = app.getContainingFolder()) != null) {
                        f.removeApp(app);
                    } else {
                        removeApp(app);
                    }

                    if (newIconURI != null) {
                        app.setIcon(editAppIcon.getDrawable());
                    }
                    app.setName(newName);
                    String icon = (newIconURI != null) ? newIconURI : "";
                    if (editedApps.put(app.getActivityName(), new String[]{icon, newName}) == null) {
                        addEditedAppToDB(app.getActivityName(), icon, newName);
                    } else {
                        updateEditedAppToDB(app.getActivityName(), icon, newName);
                    }

                    newIconURI = null;

                    if (f != null) {
                        f.addApp(app);
                    } else {
                        addApp(app);
                    }

//					doLayout();

                }
                return;
            }
        }).setNegativeButton(R.string.button_cancel, null).setNeutralButton(R.string.button_restore, new android.content.DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                Folder f;
                if ((f = app.getContainingFolder()) != null) {
                    f.removeApp(app);
                } else {
                    removeApp(app);
                }

                ResolveInfo ri = pm.resolveActivity(app.getIntent(pm), 0);
                app.setIcon(ri.loadIcon(pm));
                app.setName(ri.loadLabel(pm).toString());
                db.delete("editedApps", "activityname='" + app.getActivityName() + "'", null);
                editedApps.remove(app.getActivityName());
                if (f != null) {
                    f.addApp(app);
                } else {
                    addApp(app);
                }
//				doLayout();

                return;
            }
        });


        AlertDialog d = adb.create();



        d.show();


    }

    public void createNewFolder(Application... defaultApps) {
        Folder f = new Folder(getResources().getString(R.string.folder_name));
        f.setIcon(getResources().getDrawable(R.drawable.folder));

        for (Application app : defaultApps) {
            f.addApp(app);
        }

        fManager = new FolderManager(f, Main.this, true);
        setFolderManager(fManager);
        fManager.getHorizontalScrollView().setBackgroundDrawable(null);
    }

    public void showExistingFolders(final Application... apps) {
        final ArrayList<DrawerItem> folders = new ArrayList<DrawerItem>();
        for (Folder f : getFolders().values()) {
            folders.add(f);
        }

        if (folders.isEmpty()) {
            AlertDialog.Builder dBuilder = new AlertDialog.Builder(this);
            dBuilder.setTitle(R.string.no_folders_title).setMessage(R.string.no_folders).setCancelable(true).setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface di, int i) {
                    di.dismiss();
                }
            }).setPositiveButton(R.string.button_OK, new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface di, int i) {
                    Folder currentFolder = apps[0].getContainingFolder();
                    if (currentFolder != null) //Moving apps between folders
                    {
                        currentFolder.removeApp(apps[0]);
                        addFolderToDB(currentFolder);
                    }
                    createNewFolder(apps);
                }
            }).create().show();


        } else {
            final CheckBox checkEdit = new CheckBox(this);
            AlertDialog.Builder builder = new Builder(this);
            CustomViewAdapter cva = new CustomViewAdapter(this, R.layout.approw, folders, Main.LIST_STYLE);
            AlertDialog dialog = builder.setAdapter(cva, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface di, int i) {
                    Folder currentFolder = apps[0].getContainingFolder();
                    if (currentFolder != null) {
                        currentFolder.removeApp(apps[0]);
                        addFolderToDB(currentFolder);
                    }
                    Folder f = (Folder) folders.get(i);
                    if (!checkEdit.isChecked()) {
                        for (Application app : apps) {
                            f.addApp(app);
                            app.setContainingFolder(f);
                            Main.this.removeApp(app);
                        }
                        addFolderToDB(f);

                        Main.this.notifyDataSetChanged();
                    } else {
                        FolderManager m = new FolderManager(f, Main.this, false);
                        for (Application app : apps) {
                            m.add(app);
                        }
                    }

                }
            }).setTitle(R.string.choose_folder).setCancelable(true).setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface di, int i) {
                    di.dismiss();
                }
            }).setNeutralButton(R.string.new2, new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface di, int i) {
                    Folder currentFolder = apps[0].getContainingFolder();
                    if (currentFolder != null) {
                        currentFolder.removeApp(apps[0]);
                        addFolderToDB(currentFolder);
                    }
                    createNewFolder(apps);
                }
            }).create();
            dialog.getListView().setDividerHeight(10);
            dialog.getListView().setPadding(0, 10, 0, 10);

            checkEdit.setText(R.string.keep_editing);
            checkEdit.setTextColor(Color.BLACK);
            dialog.getListView().addFooterView(checkEdit);
            dialog.show();
        }
    }

    private Application getAppByPackageName(String packageName) {
        try {

            ApplicationInfo app = pm.getApplicationInfo(packageName, 0);
            Intent i = pm.getLaunchIntentForPackage(packageName);
            String activityname = pm.getActivityInfo(i.getComponent(), 0).name;
            File f = new File(app.sourceDir);
            Application appAux = new Application(app.loadIcon(pm), app.loadLabel(pm).toString(), app.packageName, activityname, f.lastModified());
            return appAux;

        } catch (Exception e) {
            e.printStackTrace();
            return null;

        }
    }

    private Application findAppByPackageName(String packageName) {

        Application app = null;
        for (DrawerItem item : apps) {
            if (item instanceof Application) {
                app = (Application) item;
            } else {
                continue;
            }
            if (app.getPackageName().equals(packageName)) {
                return app;
            }
        }

        return null;
    }

    public boolean isFullscreen() {
        return Settings.fullscreen;
    }

    public FolderManager getFolderManager() {
        return fManager;
    }

    public void setFolderManager(FolderManager fManager) {
        this.fManager = fManager;
    }

    public Folder getOpenedFolder() {
        return openedFolder;
    }

    public void setOpenedFolder(Folder openedFolder) {
        this.openedFolder = openedFolder;
    }

    public void scrollToTop() {
        if (Settings.list_style == LIST_STYLE) {
            lv.scrollTo(0, 0);
        } else if (Settings.list_style == GRID_STYLE) {
            gv.scrollTo(0, 0);
        }
    }

    public HashMap<String, Folder> getFolders() {
        return folders;
    }

    @Override
    public boolean onSearchRequested() {
        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.toggleSoftInput(0, 0);

        return false;
    }

    public void addApp(DrawerItem it) {
        if (Settings.list_style == PAGED_STYLE) {
//
            if (pages.isEmpty()) {
                addPage();
            }

            ArrayList<DrawerItem> first = appsPages.get(0);


            first.add(it);
            Collections.sort(first);

            if (first.size() > appsPerPage) {

                if (appsPages.get(appsPages.size() - 1).size() >= appsPerPage) //Esta al limite
                {
                    addPage();
                }

                DrawerItem sobrante = first.remove(first.size() - 1);

                for (int i = 1; i < appsPages.size(); i++) {

                    ArrayList<DrawerItem> list = appsPages.get(i);
                    list.add(sobrante);
                    Collections.sort(list);
                    if (list.size() > appsPerPage) {
                        sobrante = list.remove(list.size() - 1);
                    }
                }

            }


        }
//        else 
//        {
        apps.add(it);
        Collections.sort(apps);
        notifyDataSetChanged();
//        }
    }

    public void removeApp(DrawerItem it) {
        if (Settings.list_style == PAGED_STYLE) {
            for (int i = 0; i < appsPages.size(); i++) {

                if (appsPages.get(i).remove(it)) {
                    if (appsPages.get(i).size() < appsPerPage) //Se ha quedado un hueco
                    {
                        for (int j = i; j < appsPages.size() - 1; j++) {
                            DrawerItem item = appsPages.get(j + 1).remove(0);
                            if (appsPages.get(j + 1).isEmpty()) {
                                removePage();
                            }
                            appsPages.get(j).add(item);
                        }
                    }
                    break;
                }
            }
        }
//        else {
        apps.remove(it);
        
        notifyDataSetChanged();
//        }
    }

    public void sortApps() {
        Collections.sort(apps);

        if (Settings.list_style == PAGED_STYLE) {

            int pagesCount = (int) Math.ceil(apps.size() / (double) appsPerPage);

            int i = 0;
            for (; i < pagesCount - 1; i++) {
                ArrayList<DrawerItem> unsorted = appsPages.get(i);
                unsorted.clear();
                unsorted.addAll(apps.subList(i * appsPerPage, (i * appsPerPage) + appsPerPage));
//                appsPages.set(i, new ArrayList<DrawerItem>(apps.subList(i * appsPerPage, (i * appsPerPage) + appsPerPage)));
            }

            ArrayList<DrawerItem> unsorted = appsPages.get(i);
                unsorted.clear();
                unsorted.addAll(apps.subList(i * appsPerPage, apps.size()));
            
        }
        
        notifyDataSetChanged();

    }

    public boolean containsApp(DrawerItem it) {
        return apps.contains(it);
    }

    public void addPage() {
        GridView page = new GridView(context);

        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            page.setNumColumns(Settings.num_columns_h);
        } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            page.setNumColumns(Settings.num_columns_v);
        }
        page.setVerticalSpacing(Settings.row_spacing);


        ArrayList<DrawerItem> appsForThePage = new ArrayList<DrawerItem>();

//        cva = new CustomViewAdapter(this, R.layout.approw, appsForThePage, Settings.font, Settings.font_color, Settings.font_size, Settings.icon_size, Settings.list_style, mode);
        page.setAdapter(new CustomViewAdapter(this, R.layout.approw, appsForThePage, Settings.font, Settings.font_color, Settings.font_size, Settings.icon_size, Settings.list_style, mode));

        page.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_MOVE:
                        event.setAction(MotionEvent.ACTION_CANCEL);
                        return true;
                    default:
                        return false;
                }

            }
        });

        page.setOnItemClickListener(itemClickListener);

        DragListener drag = new DragListener(this);

        page.setOnItemLongClickListener(drag);

        page.setOnTouchListener(drag);

        hp.addPage(page);

        pages.add(page);
        appsPages.add(appsForThePage);
    }

    public void removePage() {
        int pos = hp.removePage();
        pages.remove(pos);
        appsPages.remove(pos);

    }

    public void clearHorizontalPages() {
        appsPages.clear();
        pages.clear();
        hp.clearPages();
    }

    public void notifyDataSetChanged() {
        if (Settings.list_style == PAGED_STYLE) {
            for (GridView v : pages) {
                ((CustomViewAdapter) v.getAdapter()).notifyDataSetChanged();
            }
        } else {
            cva.notifyDataSetChanged();
        }
    }
}