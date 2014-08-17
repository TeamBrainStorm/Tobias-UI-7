/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package JakedUp.AppDrawer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.GridView;
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
public class FolderLayout {
    
    private CustomViewAdapter adapter;
    private LinearLayout fLayout;
    private static float lastPosition;
    private int direction = DOWN;
    private static int DOWN = 0;
    private static int UP = 1;
    private Main main;
    private Folder folder;
    
    public void open(Main m, Folder f)
    {
        this.main = m;
        this.folder = f;
        Main.mode = Main.FOLDER_VIEW;
        fLayout = (LinearLayout) main.findViewById(R.id.folder_include);
        GridView gv = (GridView) fLayout.findViewById(R.id.grid_folder);
        final TextView title = (TextView) fLayout.findViewById(R.id.titleFolder);
        title.setText(f.getName());
        
        ArrayList<DrawerItem> items = new ArrayList<DrawerItem>();
        
        f.sortApps();
        
        for (DrawerItem i : f.getApps())
        {
            items.add(i);
        }
                
        int orient = main.getResources().getConfiguration().orientation;
        gv.setVerticalSpacing(Settings.row_spacing);
            if (orient == Configuration.ORIENTATION_LANDSCAPE) {
                gv.setNumColumns(Settings.num_columns_h);
            } else if (orient == Configuration.ORIENTATION_PORTRAIT) {
                gv.setNumColumns(Settings.num_columns_v);
            }
        
        adapter = new CustomViewAdapter(main, R.layout.appgrid, items, "Default", Color.WHITE, 14, 45, Main.GRID_STYLE, Main.NORMAL);
        gv.setOnItemClickListener(main.itemClickListener);
        DragListener drag = new DragListener(main);
        gv.setOnItemLongClickListener(drag);
        gv.setOnTouchListener(drag);
        gv.setAdapter(adapter);
        
        
        final float heightF = fLayout.getHeight();
        lastPosition = 0;
        Animation slideUp = new TranslateAnimation(0, 0, heightF, 0);
        slideUp.setDuration(Main.FOLDER_ANIMATION_SPEED);
        fLayout.setVisibility(View.VISIBLE);
        fLayout.startAnimation(slideUp);
        
        final LinearLayout fWrap = (LinearLayout) fLayout.findViewById(R.id.outTheFolder);
        
        ImageView editButton = (ImageView) fWrap.findViewById(R.id.edit_folder);
        ImageView deleteButton = (ImageView) fWrap.findViewById(R.id.delete_folder);
        
        editButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                close();
                new FolderManager(folder, main, false).showFolderManager();
            }
        });
        
        deleteButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                

                AlertDialog.Builder dBuilder = new AlertDialog.Builder(main);
                dBuilder.setTitle(R.string.delete_folder).setMessage(R.string.delete_folder_confirm)
                        .setCancelable(true).setNegativeButton(R.string.button_cancel, new OnClickListener() {

                    public void onClick(DialogInterface di, int i) {
                        di.dismiss();
                    }
                }).setPositiveButton(R.string.button_OK, new OnClickListener() {

                    public void onClick(DialogInterface di, int i) {
                        close();
                        
                        for (Application app : folder.getApps()) {
                            app.setContainingFolder(null);
                            main.addApp(app);
                        }
                        main.removeApp(folder);
                        main.cva.notifyDataSetChanged();
                        main.deleteFolderFromDB(folder);
                        main.folders.remove(folder.getId());
                    }
                }).create().show();
                
                
            }
        });
        
        fWrap.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                close();
            }
        });
        
        fWrap.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View view, MotionEvent me) {
//                if (me.getAction() == MotionEvent.ACTION_DOWN)
//                {
//                    FrameLayout vistaActual = (FrameLayout) main.findViewById(android.R.id.content);
//                    draggingImage = new ImageView(main);
//                    fLayout.buildDrawingCache();
//                    draggingImage.setImageBitmap(fLayout.getDrawingCache());
//                    vistaActual.addView(draggingImage);
//                    fLayout.setVisibility(View.INVISIBLE);
//                }
//                else 
                if (me.getAction() == MotionEvent.ACTION_MOVE)
                {
//                    moveBar = new TranslateAnimation(0, 0, me.getY(), me.getY());
//                    moveBar.setDuration(0);
//                    fLayout.startAnimation(moveBar);  
                    
                	//fLayout.getLayoutParams().height = (int)(main.getWindowManager().getDefaultDisplay().getHeight() - me.getRawY());
                	Display display = main.getWindowManager().getDefaultDisplay();
                	Point size = new Point();
                	display.getSize(size);
                	int theWidth = size.x;
                	int theHeight = size.y;
                	fLayout.getLayoutParams().height = (int) (theHeight - me.getRawY());
                    fLayout.requestLayout();
                    
//                    draggingImage.setVisibility(View.VISIBLE);
//                    Matrix m = new Matrix();
//                    m.reset();
//                    m.postTranslate(0, me.getY());
//                    draggingImage.setScaleType(ScaleType.MATRIX);
//                    draggingImage.setImageMatrix(m);
//                    draggingImage.invalidate();
                    
                    if (me.getRawY() <= fWrap.getHeight()/2)
                        direction = DOWN;
                    else if (me.getRawY() < lastPosition)
                        direction = UP;
                    else if (me.getRawY() == lastPosition)
                    {
                        if (me.getRawY() < heightF/3)
                            direction = UP;
                        else
                            direction = DOWN;
                    }
                    else direction = DOWN;
                    
                    lastPosition = me.getRawY();
                }
                else if (me.getAction() == MotionEvent.ACTION_UP) {
                    if (direction == DOWN)
                        close();
                    else
                    {
//                        float heightF = draggingImage.getHeight();
//                        float values[] = new float[9];
//                        draggingImage.getImageMatrix().getValues(values);
//                        Animation slideUp = new TranslateAnimation(0, 0, values[Matrix.MTRANS_Y]-60, 0);
//                        slideUp.setAnimationListener(new Animation.AnimationListener() {
//
//                            public void onAnimationStart(Animation anmtn) {
//                            }
//
//                            public void onAnimationEnd(Animation anmtn) {
//                                fLayout.setVisibility(View.VISIBLE);
//                                draggingImage.setImageDrawable(null);
//                            }
//
//                            public void onAnimationRepeat(Animation anmtn) {
//                            }
//                        });
                        

                        //fLayout.getLayoutParams().height = main.getWindowManager().getDefaultDisplay().getHeight();
                    	Display display = main.getWindowManager().getDefaultDisplay();
                    	Point size = new Point();
                    	display.getSize(size);
                    	int theWidth = size.x;
                    	int theHeight = size.y;
                    	fLayout.getLayoutParams().height = (int) (theHeight);                        
                        fLayout.requestLayout();
//                        Animation slideUp = new ScaleAnimation(1, 1, 1, (heightF)/fLayout.getHeight());
                        Animation slideUp = new TranslateAnimation(0, 0, lastPosition, 0);
                        slideUp.setDuration(Main.FOLDER_ANIMATION_SPEED);
                        fLayout.startAnimation(slideUp);
                        lastPosition = 0;
                    }
                }
                return true;
            }
        });
    }
    
    public void close()
    {
        Main.mode = Main.NORMAL;
        fLayout.setVisibility(View.INVISIBLE);
        //fLayout.getLayoutParams().height = (int)(main.getWindowManager().getDefaultDisplay().getHeight());
    	Display display = main.getWindowManager().getDefaultDisplay();
    	Point size = new Point();
    	display.getSize(size);
    	int theWidth = size.x;
    	int theHeight = size.y;
    	fLayout.getLayoutParams().height = (int) (theHeight);
//        fLayout.requestLayout();
//        float heightF = fLayout.getHeight();
        Animation slideDown = new TranslateAnimation(0, 0, lastPosition, (int) (theHeight));
        slideDown.setDuration(Main.FOLDER_ANIMATION_SPEED);

//        if (draggingImage != null)
//        {
//            slideDown.setAnimationListener(new Animation.AnimationListener() {
//
//                public void onAnimationStart(Animation anmtn) {
//                }
//
//                public void onAnimationEnd(Animation anmtn) {
//                    draggingImage.setImageDrawable(null);
//                }
//
//                public void onAnimationRepeat(Animation anmtn) {
//                }
//            });
//            draggingImage.startAnimation(slideDown);
//        }
//        else 
        {
//            slideDown = new TranslateAnimation(0, 0, 0, heightF);
//            slideDown.setDuration(Main.ANIMATION_SPEED);
            fLayout.startAnimation(slideDown);
        }
        
    }
    
}
