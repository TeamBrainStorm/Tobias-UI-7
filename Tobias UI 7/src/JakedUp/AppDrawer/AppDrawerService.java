package JakedUp.AppDrawer;

import android.app.Notification;
import android.app.PendingIntent;
import java.util.ArrayList;


import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class AppDrawerService extends Service {

    private static ArrayList<DrawerItem> apps;
    private boolean ready = false;

    @Override
    public void onCreate() {
       
        super.onCreate();
        
        apps = new ArrayList<DrawerItem>();
        
        ready = true;
//        stopSelf();
    }
//
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        int myID = 1234;
//
//        //This constructor is deprecated. Use Notification.Builder instead
//        Notification notice = new Notification(R.drawable.add_to_folder, "Ticker text", System.currentTimeMillis());
//        notice.setLatestEventInfo(this, "Prueba", "Bla bla bla", PendingIntent.getActivity(this, 0, intent, 0));
//
//        startForeground(myID, notice);
//
//        return super.onStartCommand(intent, flags, startId);
//    }

    

    
    public boolean isReady() {
        return ready;
    }

    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    public static ArrayList<DrawerItem> getApps() {

        return apps;
    }

    public static void setApps(ArrayList<DrawerItem> apps) {
        AppDrawerService.apps = apps;
    }
    
    public static void addApp(DrawerItem app)
    {
        apps.add(app);
    }
    
    public static void removeApp(DrawerItem app)
    {
        apps.remove(app);
    }
    
}
