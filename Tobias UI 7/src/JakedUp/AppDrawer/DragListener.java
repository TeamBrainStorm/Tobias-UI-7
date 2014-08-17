/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package JakedUp.AppDrawer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.d4a.tobias.R;
import java.util.Collections;

/**
 *
 * @author Josep
 */
public class DragListener implements OnTouchListener, OnItemLongClickListener {

    private Main main;
    private LinearLayout dropMenu;
    private LinearLayout dropMenuFolders;
    private View appView;
    private ImageView draggingImage;
    private DrawerItem draggingItem;
    private float oldX;
    private float oldY;
    private TextView actionTitle;
    private LinearLayout actionIcons;
    private String titles[];// = new String[5];
    private ImageView iconHide;
    private Rect rectHide;
    private ImageView iconDetails;
    private Rect rectDetails;
//    private ImageView iconUninstall;
//    private Rect rectUninstall;
    private ImageView iconShortcut;
    private Rect rectShortcut;
    private ImageView iconEdit;
    private Rect rectEdit;
    private ImageView iconAddToFolder;
    private Rect rectAddToFolder;
    private ImageView iconNewFolder;
    private Rect rectNewFolder;
    private static final int STOP = -2;
    private static final int NONE = -1;
    private static final int HIDE = 0;
    private static final int DETAILS = 1;
    private static final int UNINSTALL = 2;
    private static final int SHORTCUT = 3;
    private static final int EDIT = 4;
    private static final int EXISTING_FOLDER = 5;
    private static final int EXISTING_FOLDER_CHANGE = 10;
    private static final int NEW_FOLDER = 6;
    private static final int DELETE_FOLDER = 7;
    private static final int EDIT_FOLDER = 8;
    private static final int DELETE_FROM_FOLDER = 9;
    private static final int ADD_TO_FOLDER = 20;
    private int status;
    private Handler uninstallDelay;
    private Runnable uninstallRunnable;
    private FolderManager fManager;
    private Rect rectFManager;
    private boolean draggingFromFolder;

    public DragListener(Main mainAct) {
        this.main = mainAct;

        dropMenu = (LinearLayout) main.findViewById(R.id.long_click_menu_include);
        dropMenu.setVisibility(View.INVISIBLE);
        actionIcons = (LinearLayout) dropMenu.findViewById(R.id.long_click_icons);

        dropMenuFolders = (LinearLayout) main.findViewById(R.id.long_click_folders_include);
        dropMenuFolders.setVisibility(View.INVISIBLE);

        iconHide = (ImageView) main.findViewById(R.id.hide_drag);
        iconDetails = (ImageView) main.findViewById(R.id.details_drag);
//        iconUninstall = (ImageView) main.findViewById(R.id.uninstall_drag);
        iconShortcut = (ImageView) main.findViewById(R.id.shortcut_drag);
        iconEdit = (ImageView) main.findViewById(R.id.edit_drag);
        iconAddToFolder = (ImageView) main.findViewById(R.id.add_to_folder_drag);
        iconNewFolder = (ImageView) main.findViewById(R.id.new_folder_drag);

        actionTitle = (TextView) main.findViewById(R.id.long_click_title);
        status = STOP;

        titles = main.getResources().getStringArray(R.array.app_actions);

        uninstallDelay = new Handler();
        uninstallRunnable = new Runnable() {

            public void run() {
                iconHide.setImageResource(R.drawable.uninstall);
                actionTitle.setText(titles[UNINSTALL]);
                status = UNINSTALL;
            }
        };

    }

    public boolean onItemLongClick(AdapterView<?> av, View view, int i, long l) {

        if (Main.mode == Main.MULTI_SELECT) {
            return false;
        }
        
        

        draggingItem = (DrawerItem) av.getItemAtPosition((int) l);

        if (Main.mode == Main.NORMAL || (Main.mode == Main.FOLDER_VIEW && draggingItem instanceof Application && ((Application) draggingItem).getContainingFolder() != null)) {

            if (Main.mode == Main.FOLDER_VIEW) {
                draggingFromFolder = true;
                ((Application) draggingItem).getContainingFolder().close();
            } else {
                draggingFromFolder = false;
            }
            
            Main.isDragging = true;

            if (draggingItem instanceof Folder) {
                actionIcons.setVisibility(View.GONE);
            } else {
                actionIcons.setVisibility(View.VISIBLE);
            }

            dropMenu.setVisibility(View.VISIBLE);
            float height = dropMenu.getHeight();
            Animation slideDown = new TranslateAnimation(0, 0, -height, 0);
            slideDown.setDuration(Main.ANIMATION_SPEED);
            dropMenu.startAnimation(slideDown);


            if (draggingItem instanceof Folder) {
                iconAddToFolder.setImageResource(R.drawable.edit);
                iconNewFolder.setImageResource(R.drawable.uninstall);
            } else {
                iconAddToFolder.setImageResource(R.drawable.add_to_folder);
                if (draggingFromFolder) {
                    iconNewFolder.setImageResource(R.drawable.uninstall);
                } else {
                    iconNewFolder.setImageResource(R.drawable.new_folder);
                }

            }
            dropMenuFolders.setVisibility(View.VISIBLE);
            float heightF = dropMenuFolders.getHeight();
            Animation slideUp = new TranslateAnimation(0, 0, heightF, 0);
            slideUp.setDuration(Main.ANIMATION_SPEED);
            dropMenuFolders.startAnimation(slideUp);



        }

        FrameLayout vistaActual = (FrameLayout) main.findViewById(android.R.id.content);
        draggingImage = new ImageView(main);
        view.buildDrawingCache();
        draggingImage.setImageBitmap(view.getDrawingCache());
        draggingImage.setVisibility(View.INVISIBLE);
        vistaActual.addView(draggingImage);

        appView = view;

        AlphaAnimation a = new AlphaAnimation(1.0f, 0.5f);
        a.setFillAfter(true);
        appView.startAnimation(a);

        int oldLocation[] = new int[2];
        view.getLocationInWindow(oldLocation);
        oldX = oldLocation[0];
        oldY = oldLocation[1];

        findRects();

        return true;
    }

    @Override
    public boolean onTouch(View view, MotionEvent me) {

        //DRAGGING
        if (me.getAction() == MotionEvent.ACTION_MOVE && draggingImage != null) {
            draggingImage.setVisibility(View.VISIBLE);
            Matrix m = new Matrix();
            m.reset();
            int offset = 0;
            if (draggingItem instanceof Application && ((Application) draggingItem).getContainingFolder() != null) {
                offset = 50;
            }
            m.postTranslate(me.getX() - draggingImage.getDrawable().getMinimumWidth() / 2, offset + me.getY() - draggingImage.getDrawable().getMinimumHeight() / 2);
            draggingImage.setScaleType(ScaleType.MATRIX);
            draggingImage.setImageMatrix(m);
            draggingImage.invalidate();

//            if (rDetails == null)
//                findRects();

            int x = (int) me.getX();
            int y = (int) me.getY();

            if (draggingFromFolder) {
                y += 50;
            }

            if (draggingItem instanceof Application && rectHide != null && rectHide.contains(x, y)) {

                if (status != UNINSTALL) {
                    actionTitle.setText(titles[HIDE]);
                    status = HIDE;
                    iconHide.setImageResource(R.drawable.hide);
                    uninstallDelay.postDelayed(uninstallRunnable, 1000);
                }
                //SWITCH TO UNINSTALL

            } else if (draggingItem instanceof Application && rectDetails != null && rectDetails.contains(x, y)) {
                actionTitle.setText(titles[DETAILS]);
                status = DETAILS;
                removeUninstallCallback();
//            } else if (rectUninstall != null && rectUninstall.contains(x, y)) {
//                actionTitle.setText(titles[UNINSTALL]);
//                status = UNINSTALL;
            } else if (draggingItem instanceof Application && rectShortcut != null && rectShortcut.contains(x, y)) {
                actionTitle.setText(titles[SHORTCUT]);
                status = SHORTCUT;
                removeUninstallCallback();
            } else if (draggingItem instanceof Application && rectEdit != null && rectEdit.contains(x, y)) {
                actionTitle.setText(titles[EDIT]);
                status = EDIT;
                removeUninstallCallback();
            } else if (rectAddToFolder != null && rectAddToFolder.contains(x, y)) {
                if (draggingItem instanceof Application) {
                    if (draggingFromFolder) {
                        actionTitle.setText(titles[EXISTING_FOLDER_CHANGE]);
                        status = EXISTING_FOLDER;
                    } else {
                        actionTitle.setText(titles[EXISTING_FOLDER]);
                        status = EXISTING_FOLDER;
                    }
                    removeUninstallCallback();
                } else {
                    actionTitle.setText(titles[EDIT_FOLDER]);
                    status = EDIT_FOLDER;
                }
            } else if (rectNewFolder != null && rectNewFolder.contains(x, y)) {
                if (draggingItem instanceof Application) {
                    if (draggingFromFolder) {
                        actionTitle.setText(titles[DELETE_FROM_FOLDER]);
                        status = DELETE_FROM_FOLDER;
                    } else {
                        actionTitle.setText(titles[NEW_FOLDER]);
                        status = NEW_FOLDER;
                    }
                    removeUninstallCallback();
                } else {
                    actionTitle.setText(titles[DELETE_FOLDER]);
                    status = DELETE_FOLDER;

                }
            } else if (rectFManager != null && rectFManager.contains(x, y)) {
                fManager.getHorizontalScrollView().setBackgroundResource(R.drawable.border);
                status = ADD_TO_FOLDER;
            } else {
                if (draggingItem != null) {
                    actionTitle.setText(draggingItem.getName());
                } else {
                    actionTitle.setText("");
                }
                status = NONE;
                removeUninstallCallback();

                if (fManager != null) {
                    fManager.getHorizontalScrollView().setBackgroundDrawable(null);
                }
            }

            return true;
        }

        //APPLICATION GOES BACK TO ITS OLD POSITION
        Animation goBackAnim = null;
        if (me.getAction() == MotionEvent.ACTION_UP && draggingImage != null) {
            float[] currentLocation = new float[9];
            draggingImage.getImageMatrix().getValues(currentLocation);

            float currentX = currentLocation[2];
            float currentY = currentLocation[5] - statusBarHeight();

            float diffX = oldX - currentX;
            float diffY = oldY - currentY;

            goBackAnim = new TranslateAnimation(0, diffX,
                    0, diffY); //to
            goBackAnim.setDuration(Main.ANIMATION_SPEED);
            goBackAnim.setAnimationListener(new Animation.AnimationListener() {

                public void onAnimationStart(Animation anmtn) {
                }

                public void onAnimationEnd(Animation anmtn) {
                    cleanUpTheMess();
                }

                public void onAnimationRepeat(Animation anmtn) {
                }
            });

        }

        boolean goBack = false;
        //RELEASE DRAGGING
        if (me.getAction() == MotionEvent.ACTION_UP && dropMenu != null && status != STOP && status != ADD_TO_FOLDER) {

            Main.isDragging = false;
            
            float height = dropMenu.getHeight();
            Animation slideUp = new TranslateAnimation(0, 0, 0, -height);
            slideUp.setDuration(Main.ANIMATION_SPEED);
            slideUp.setFillAfter(true);
            dropMenu.startAnimation(slideUp);
            dropMenu.setVisibility(View.INVISIBLE);

            float heightF = dropMenuFolders.getHeight();
            Animation slideDown = new TranslateAnimation(0, 0, 0, heightF);
            slideDown.setDuration(Main.ANIMATION_SPEED);
            slideDown.setFillAfter(true);
            dropMenuFolders.startAnimation(slideDown);
            dropMenuFolders.setVisibility(View.INVISIBLE);

            if (draggingItem != null) {
                AlphaAnimation a = new AlphaAnimation(0.5f, 1.0f);
                a.setFillAfter(true);

                switch (status) {
                    case HIDE:
                        removeUninstallCallback();
                        cleanUpTheMess();
                        main.hideApp((Application) draggingItem);
                        break;
                    case DETAILS:
                        cleanUpTheMess();
                        main.showAppDetails((Application) draggingItem);
                        break;
                    case UNINSTALL:
                        cleanUpTheMess();
                        main.uninstallApp((Application) draggingItem);
                        break;
                    case SHORTCUT:
                        cleanUpTheMess();
                        main.createShortcutOnDesktop((Application) draggingItem);
                        break;
                    case EDIT:
                        //Clear image to prevent "Canvas: trying to use a recycled bitmap" error.
                        cleanUpTheMess();
                        main.editApp((Application) draggingItem);
                        break;
                    case EXISTING_FOLDER:
                        cleanUpTheMess();
                        main.showExistingFolders((Application) draggingItem);
                        break;
                    case NEW_FOLDER:
                        cleanUpTheMess();
                        main.createNewFolder((Application) draggingItem);
                        break;
                    case DELETE_FOLDER:
                        cleanUpTheMess();
                        deleteFolder();
                        break;
                    case EDIT_FOLDER:
                        cleanUpTheMess();
                        fManager = new FolderManager((Folder) draggingItem, main, false);
                        main.setFolderManager(fManager);
                        fManager.getHorizontalScrollView().setBackgroundDrawable(null);
                        break;
                    case DELETE_FROM_FOLDER:
                        cleanUpTheMess();
                        Application app = (Application) draggingItem;
                        Folder f = app.getContainingFolder();
                        f.removeApp(app);
                        main.addFolderToDB(f);
                        app.setContainingFolder(null);
                        main.addApp(app);
                        break;
                    default:
                        goBack = true;
                }
            } else {
                goBack = true;
            }

            draggingItem = null;
            status = STOP;
            if (goBack && goBackAnim != null && draggingImage != null) {
                draggingImage.startAnimation(goBackAnim);
            }
            return true;

        }

        //RELEASE DRAGGING AT FOLDER
        if (me.getAction() == MotionEvent.ACTION_UP && fManager != null && status == ADD_TO_FOLDER) {

            if (draggingItem != null && draggingItem instanceof Application) {
                cleanUpTheMess();
                fManager.add((Application) draggingItem);
            } else if (goBackAnim != null) {
                draggingImage.startAnimation(goBackAnim);
            }

            fManager.getHorizontalScrollView().setBackgroundDrawable(null);

            status = STOP;

            return true;

        }

        if (goBack && goBackAnim != null) {
            draggingImage.startAnimation(goBackAnim);
        }
        
        return false;
    }

    //Looks for the Rect of every icon.
    private void findRects() {

        int[] location = new int[2];
        int x = 0;
        int y = 0;
        if (Main.mode == Main.NORMAL) {
            iconHide.getLocationInWindow(location);
            x = location[0];
            y = location[1];
            rectHide = new Rect(x, y, x + iconHide.getWidth(), y + iconHide.getHeight());

            iconDetails.getLocationInWindow(location);
            x = location[0];
            y = location[1];
            rectDetails = new Rect(x, y, x + iconDetails.getWidth(), y + iconDetails.getHeight());

            //        iconUninstall.getLocationInWindow(location);
            //        x = location[0];
            //        y = location[1];
            //        rectUninstall = new Rect(x, y, x + iconUninstall.getWidth(), y + iconUninstall.getHeight());

            iconShortcut.getLocationInWindow(location);
            x = location[0];
            y = location[1];
            rectShortcut = new Rect(x, y, x + iconShortcut.getWidth(), y + iconShortcut.getHeight());

            iconEdit.getLocationInWindow(location);
            x = location[0];
            y = location[1];
            rectEdit = new Rect(x, y, x + iconEdit.getWidth(), y + iconEdit.getHeight());

            iconAddToFolder.getLocationInWindow(location);
            x = location[0];
            y = location[1];
            rectAddToFolder = new Rect(x, y, x + iconAddToFolder.getWidth(), y + iconAddToFolder.getHeight());

            iconNewFolder.getLocationInWindow(location);
            x = location[0];
            y = location[1];
            rectNewFolder = new Rect(x, y, x + iconNewFolder.getWidth(), y + iconNewFolder.getHeight());
        } else if (Main.mode == Main.FOLDER_EDIT && fManager != null) {
            fManager.getHorizontalScrollView().getLocationInWindow(location);
            x = location[0];
            y = location[1];
            rectFManager = new Rect(x, y, x + fManager.getHorizontalScrollView().getWidth(), y + fManager.getHorizontalScrollView().getHeight());
            rectNewFolder = null;
            rectAddToFolder = null;
        }

    }

    //Gets back to the old alpha and clear the dragged image
    private void cleanUpTheMess() {
        if (draggingImage != null) {
            draggingImage.setImageDrawable(null);
        }
        draggingImage = null;
        AlphaAnimation a = new AlphaAnimation(0.5f, 1.0f);
        a.setFillAfter(true);
        appView.startAnimation(a);

    }

    //Returns the height of the status bar. Needed when no fullscreen.
    private int statusBarHeight() {
        if (main.isFullscreen()) {
            return 0;
        }

        Rect rect = new Rect();
        Window window = main.getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rect);
        int statusBarHeight = rect.top;
        int contentViewTop = window.findViewById(Window.ID_ANDROID_CONTENT).getTop();
        return contentViewTop - statusBarHeight;
    }

    private void deleteFolder() {
        if (draggingItem != null && draggingItem instanceof Folder) {

            final Folder f = (Folder) draggingItem;

            AlertDialog.Builder dBuilder = new AlertDialog.Builder(main);
            dBuilder.setTitle(R.string.delete_folder).setMessage(R.string.delete_folder_confirm).setCancelable(true).setNegativeButton(R.string.button_cancel, new OnClickListener() {

                public void onClick(DialogInterface di, int i) {
                    di.dismiss();
                }
            }).setPositiveButton(R.string.button_OK, new OnClickListener() {

                public void onClick(DialogInterface di, int i) {

                    for (Application app : f.getApps()) {
                        app.setContainingFolder(null);
                        main.addApp(app);
                    }
                    main.removeApp(f);
                    main.cva.notifyDataSetChanged();
                    main.deleteFolderFromDB(f);
                    main.folders.remove(f.getId());
                }
            }).create().show();

        }
    }

    private void removeUninstallCallback() {
        uninstallDelay.removeCallbacks(uninstallRunnable);
        iconHide.setImageResource(R.drawable.hide);
    }
}
