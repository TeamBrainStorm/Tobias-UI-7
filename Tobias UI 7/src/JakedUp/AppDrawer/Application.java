package JakedUp.AppDrawer;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

public class Application extends DrawerItem {

    private String packageName;
    private String activityName;
    private long installationDate;
    private Folder containingFolder;

    public Application(Drawable icon, String name, String packageName, String activityName, long installationDate) {
        super(icon, name);
        this.packageName = packageName;
        this.activityName = activityName;
        this.installationDate = installationDate;

    }

    public String getPackageName() {
        return packageName;
    }

    public String getActivityName() {
        return activityName;
    }

    public long getInstallationDate() {
        return installationDate;
    }

//    @Override
//    public int compareTo(DrawerItem a) {
//        switch (Main.sortBy) {
//            case 0:
//                return getName().compareToIgnoreCase(a.getName());
//            case 1:
//                return String.valueOf(getInstallationDate()).compareTo(String.valueOf(a.getInstallationDate()));
//            case 2:
//                return String.valueOf(a.getInstallationDate()).compareTo(String.valueOf(getInstallationDate()));
//            case 3:
//                int result = a.getUses() - getUses();
//                if (result == 0) {
//                    return getName().compareToIgnoreCase(a.getName());
//                } else {
//                    return result;
//                }
//            default:
//                return 0;
//        }
//
//    }
    public Intent getIntent(PackageManager pm) {

        Intent i = pm.getLaunchIntentForPackage(packageName);
        i.setClassName(packageName, activityName);

        i.addCategory(Intent.CATEGORY_LAUNCHER);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

        return i;
    }

    /**
     * Something like this:
     * --------------------
     * Name:
     * PackageName:
     * ActivityName:
     * --------------------
     * @return 
     */
    public String toStringDetail() {
        return "Name:" + getName() + "\n" + "PackageName: " + packageName + "\n" + "ActivityName: " + activityName + "\n--------------------\n";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Application other = (Application) obj;
        if ((this.packageName == null) ? (other.packageName != null) : !this.packageName.equals(other.packageName)) {
            return false;
        }
        if ((this.activityName == null) ? (other.activityName != null) : !this.activityName.equals(other.activityName)) {
            return false;
        }
        return true;
    }

    public Folder getContainingFolder() {
        return containingFolder;
    }

    public void setContainingFolder(Folder containingFolder) {
        this.containingFolder = containingFolder;
    }
}
