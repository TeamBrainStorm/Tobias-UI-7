/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package JakedUp.AppDrawer;

import android.graphics.drawable.Drawable;
import java.text.Collator;

/**
 *
 * @author Josep
 */
public class DrawerItem implements Comparable<DrawerItem> {

    private Drawable icon;
    private String name;
    private int uses;

    public DrawerItem(Drawable icon, String name) {
        this.icon = icon;
        this.name = name;
        this.uses = 0;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getUses() {
        return uses;
    }

    public void setUses(int uses) {
        this.uses = uses;
    }

    public void addUse() {
        this.uses++;
    }

    @Override
    public String toString() {
        return name;
    }

    public int compareTo(DrawerItem t) {
        if (this instanceof Folder && t instanceof Application && Settings.sort_folders_first) //Folder - App
        {
            return -1;
        }
        if (this instanceof Application && t instanceof Folder && Settings.sort_folders_first) {
            return 1;
        }

        switch (Settings.sortBy) {
            case Settings.SORT_BY_NAME:
                return Collator.getInstance().compare(getName(), t.getName());
            case Settings.SORT_BY_MOST_USED:
                int result = t.getUses() - getUses();
                if (result == 0) {
                    return Collator.getInstance().compare(getName(), t.getName());
                } else {
                    return result;
                }
            case Settings.SORT_BY_LATEST_INSTALLED_FIRST:
                if (this instanceof Application && t instanceof Application) {
                    return String.valueOf(((Application) t).getInstallationDate()).compareTo(String.valueOf((((Application) this).getInstallationDate())));
                }
                return Collator.getInstance().compare(getName(), t.getName());
            case Settings.SORT_BY_INSTALLATION_DATE:
                if (this instanceof Application && t instanceof Application) {
                    return String.valueOf(((Application) this).getInstallationDate()).compareTo(String.valueOf((((Application) t).getInstallationDate())));
                }
                return Collator.getInstance().compare(getName(), t.getName());
            default:
                return 0;

        }

    }
}
