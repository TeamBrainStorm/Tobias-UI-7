/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package JakedUp.AppDrawer;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.d4a.tobias.R;
import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * @author Josep
 */
public class FolderManager implements View.OnClickListener {

    private Main main;
    private LinearLayout fmView;
    private Folder folder;
    private LinearLayout appsListLayout;
    private LinearLayout mainLayout;
    private TextView nameView;
    private ImageView iconView;
    private ArrayList<Application> appsFolder;
    private ImageButton iconEdit;
    private LinearLayout buttonsWrap;
    public boolean saving = false;
    public static boolean changes = true;

    public FolderManager(Folder folder, Main main, boolean newFolder) {

        this.folder = folder;
        this.main = main;

        mainLayout = (LinearLayout) main.findViewById(R.id.linearLayout1);
        fmView = (LinearLayout) main.findViewById(R.id.folder_manager_include);
        fmView.requestLayout();
        appsListLayout = (LinearLayout) fmView.findViewById(R.id.folderSelectedApps);
        appsListLayout.removeAllViews();
        appsFolder = new ArrayList<Application>();
        fillTitleBar();

        showFolderManager();

        folder.sortApps();

        for (Application app : folder.getApps()) {
            add(app);
        }

        main.setFolderManager(this);

        changes = newFolder;
    }

    public void showFolderManager() {

        Main.mode = Main.FOLDER_EDIT;
        fmView.setVisibility(View.VISIBLE);
        final float heightF = fmView.getHeight();
        Animation slideUp = new TranslateAnimation(0, 0, heightF, 0);
        slideUp.setDuration(Main.FOLDER_ANIMATION_SPEED);
        slideUp.setAnimationListener(new Animation.AnimationListener() {

            public void onAnimationStart(Animation anmtn) {
            }

            public void onAnimationEnd(Animation anmtn) {
                main.hideHomeButton();
                mainLayout.getLayoutParams().height = (int) (mainLayout.getHeight() - fmView.getHeight());
                mainLayout.requestLayout();
            }

            public void onAnimationRepeat(Animation anmtn) {
            }
        });
//        Animation resizeUp = new ScaleAnimation(1, 1, 1, (heightM-heightF)/heightM);
//        resizeUp.setDuration(Main.ANIMATION_SPEED);
//        resizeUp.setFillBefore(true);
//        mainLayout.startAnimation(resizeUp);
        fmView.startAnimation(slideUp);

    }

    private void hideFolderManager() {
        fmView.setVisibility(View.INVISIBLE);
        float heightF = fmView.getHeight();
        Animation slideDown = new TranslateAnimation(0, 0, 0, heightF);
        slideDown.setDuration(Main.FOLDER_ANIMATION_SPEED);
        fmView.startAnimation(slideDown);
        main.showHomeButton();
        Main.checkedItems.clear();
        
        if (folder.getId() != null) //Si estamos editando
            //Eliminar las apps añadidas temporalmente (quitadas de la carpeta) a la lista de apps
            for (Application app : folder.getApps()) {
                main.removeApp(app);
            }
        
        main.cva.notifyDataSetChanged();
        mainLayout.getLayoutParams().height = -1;
        mainLayout.requestLayout();
        fmView.removeView(buttonsWrap);
        mainLayout.post(new Runnable() {

            public void run() {
                Main.mode = Main.NORMAL;
            }
        });

    }

    public void add(Application app) {
        if (appsFolder.contains(app)) {
            return;
        }
        View v = View.inflate(main, R.layout.appgrid, null);
        ImageView appIcon = (ImageView) v.findViewById(R.id.appIcon);
        TextView appName = (TextView) v.findViewById(R.id.appName);
        appIcon.setImageDrawable(app.getIcon());
        appIcon.setMaxWidth(60);
        appName.setText(app.getName());
        v.setPadding(0, 10, 10, 10);
        v.setOnClickListener(this);
        v.setTag(app);
        appsListLayout.addView(v);
        appsFolder.add(app);
        Main.checkedItems.add(app);
        main.cva.notifyDataSetChanged();
        appsListLayout.post(new Runnable() {

            public void run() {
                ((HorizontalScrollView) appsListLayout.getParent()).scrollTo(appsListLayout.getWidth(), 0);
            }
        });

        changes = true;
    }

    public void remove(View view, Application app) {
        appsListLayout.removeView(view);
        appsFolder.remove(app);
        Main.checkedItems.remove(app);
        if (!main.containsApp(app)) {
            main.addApp(app);
        }
        changes = true;
    }

    public void remove(Application app) {
        for (View v : appsListLayout.getTouchables()) {
            if (v.getTag() != null && v.getTag().equals(app)) {
                appsListLayout.removeView(v);
                appsFolder.remove(app);
                Main.checkedItems.remove(app);
                main.cva.notifyDataSetChanged();
                break;
            }
        }

        changes = true;
    }

    private void fillTitleBar() {

        nameView = (TextView) fmView.findViewById(R.id.titleFolder);

        nameView.setText(folder.getName());

        iconView = (ImageView) fmView.findViewById(R.id.iconFolder);

        iconView.setImageDrawable(folder.getIcon());

        View.OnClickListener ontitlebarClick = new View.OnClickListener() {

            public void onClick(View view) {

                LayoutInflater inflater = LayoutInflater.from(main);
                View editAppView = inflater.inflate(R.layout.edit_app, null);

                final AlertDialog.Builder adb = new Builder(main);

                iconEdit = (ImageButton) editAppView.findViewById(R.id.editAppIcon);

                final EditText nameEdit = (EditText) editAppView.findViewById(R.id.editAppName);

                iconEdit.setImageDrawable(iconView.getDrawable());

                nameEdit.setText(nameView.getText());

                nameEdit.setSelection(nameEdit.getText().length());

                iconEdit.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        Intent iconPickerIntent = new Intent(Intent.ACTION_PICK);

                        iconPickerIntent.setType("image/*");
                        //iconPickerIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
                        //iconPickerIntent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
                        main.editAppIcon = iconEdit;
                        //main.startActivityForResult(iconPickerIntent, main.ICON_SELECT);
                        main.startActivityForResult(iconPickerIntent, Main.ICON_SELECT);
                    }
                });

                adb.setTitle(main.getResources().getStringArray(R.array.app_actions)[4]).setCancelable(true).setView(editAppView).setPositiveButton(R.string.button_OK, new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        nameView.setText(nameEdit.getText().toString().trim());
                        iconView.setImageDrawable(iconEdit.getDrawable());
                        changes = true;
                        dialog.dismiss();
                        return;
                    }
                }).setNegativeButton(R.string.button_cancel, null);

                adb.show();
            }
        };

        nameView.setOnClickListener(ontitlebarClick);

        iconView.setOnClickListener(ontitlebarClick);

        TextView closeFolder = (TextView) main.findViewById(R.id.closeFolder);
        closeFolder.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                if (!changes) {
                    hideFolderManager();
                } else {
                    closeFolderManager();
                }
            }
        });

    }

    public LinearLayout getAppsListLayout() {
        return appsListLayout;
    }

    public HorizontalScrollView getHorizontalScrollView() {
        return (HorizontalScrollView) appsListLayout.getParent();
    }

    public void onClick(View view) {
        Application app = (Application) view.getTag();
        if (app != null) {
            remove(view, app);
        }
    }

    public void closeFolderManager() {

        if (!saving) {
            if (appsFolder.isEmpty()) {
                hideFolderManager();
                return;
            }
            LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1.0f);
            buttonsWrap = new LinearLayout(main);
            buttonsWrap.setOrientation(LinearLayout.HORIZONTAL);
            buttonsWrap.setLayoutParams(params);

            params.width = LayoutParams.MATCH_PARENT;

            Button btSave = new Button(main);
            btSave.setText(R.string.folder_save);
            btSave.setLayoutParams(params);
            btSave.setOnClickListener(new View.OnClickListener() {

                public void onClick(View view) {
                    saveCurrentFolder();
                    hideFolderManager();
                }
            });

            Button btDiscard = new Button(main);
            btDiscard.setText(R.string.folder_discard);
            btDiscard.setLayoutParams(params);
            btDiscard.setOnClickListener(new View.OnClickListener() {

                public void onClick(View view) {
                    hideFolderManager();
                }
            });

            buttonsWrap.addView(btSave);
            buttonsWrap.addView(btDiscard);

            fmView.addView(buttonsWrap);

            saving = true;
        } else {
            fmView.removeView(buttonsWrap);
            saving = false;
        }

    }

    private void saveCurrentFolder() {



        if (main.newIconURI == null || "".equals(main.newIconURI)) {
            if (folder.getIconURI() == null || "".equals(folder.getIconURI())) {
                folder.setIconURI("default");
                folder.setIcon(main.getResources().getDrawable(R.drawable.folder));
            }
        } else {
            folder.setIcon(iconView.getDrawable());
            folder.setIconURI(main.newIconURI);
            main.newIconURI = null;
        }
        folder.setName(nameView.getText().toString());
        if (folder.getId() == null) {
            main.addApp(folder);
        } else {
            for (Application app : folder.getApps()) {
                app.setContainingFolder(null);
                if (!main.containsApp(app)) {
                    main.addApp(app);
                }
            }
        }
        folder.setApps(appsFolder);
        main.scrollToTop();
        main.addFolderToDB(folder);
        for (Application app : appsFolder) {
            app.setContainingFolder(folder);
            main.removeApp(app);
        }
        main.folders.put(folder.getId(), folder);
    }
}
