/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package JakedUp.AppDrawer;

import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * @author Josep
 */
public class Folder extends DrawerItem{
    private String id;
    private String iconURI;
    private ArrayList<Application> apps;
    
    private FolderLayout layout;

    public Folder(String name) {
        super(null, name);
        apps = new ArrayList<Application>();
        layout = new FolderLayout();
    }

    public ArrayList<Application> getApps() {
        return apps;
    }

    public void setApps(ArrayList<Application> apps) {
        this.apps = apps;
    }
    
    public void addApp(Application app)
    {
        apps.add(app);
    }
    
    public void sortApps()
    {
        Collections.sort(apps);
    }
    
    public void removeApp(Application app)
    {
        apps.remove(app);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIconURI() {
        return iconURI;
    }

    public void setIconURI(String iconURI) {
        this.iconURI = iconURI;
    }
    
    public void open(Main main){
        
        main.setOpenedFolder(this);
        layout.open(main, this);

    }
    
    public void close()
    {
        layout.close();
    }

    
}
