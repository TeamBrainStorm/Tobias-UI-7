package JakedUp.AppDrawer;

import java.io.File;
import java.util.ArrayList;


import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import com.d4a.tobias.R;

public class CustomViewAdapter extends ArrayAdapter<DrawerItem> {

//        private ArrayList<Application> appList;
    private int fontColor, fontSize, iconSize, style;
    String font;
    private Typeface typeface;
    private LayoutInflater vi;
    private int mode;

    public CustomViewAdapter(Context context, int textViewResourceId, ArrayList<DrawerItem> items, int style) {
        this(context, R.layout.approw, items, "Default", Color.BLACK, 25, 40, style, Main.NORMAL);
    }

    public CustomViewAdapter(Context context, int textViewResourceId, ArrayList<DrawerItem> items, String font, int fontColor, int fontSize, int iconSize, int style, int mode) {
        super(context, textViewResourceId, items);
//                this.appList = items;
        setNotifyOnChange(true);
        this.font = font;
        this.fontColor = fontColor;
        this.fontSize = fontSize;
        this.iconSize = iconSize;
        this.style = style;
        this.mode = mode;
        try {
            if (font != "Default") {
                if (font.startsWith(".")) {
                    this.typeface = Typeface.createFromAsset(getContext().getAssets(), "fonts/" + font.substring(1));
                } else if (font.startsWith("+")) {
                    File sdCard = Environment.getExternalStorageDirectory();
                    File fontsFolder = new File(sdCard.getAbsolutePath() + Settings.FONTS_DIR);
                    this.typeface = Typeface.createFromFile(new File(fontsFolder.getAbsolutePath() + "/" + font.substring(1)));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;
        ViewHolder holder;
        float SCALE = getContext().getResources().getDisplayMetrics().density;

        if (v == null) {

            holder = new ViewHolder();

            if (Main.mode == Main.MULTI_SELECT || mode == Main.MULTI_SELECT) {
                if (style == Main.GRID_STYLE || style == Main.PAGED_STYLE) {
                    v = vi.inflate(R.layout.appgrid_multi, null);
                } else {
                    v = vi.inflate(R.layout.approw_multi, null);
                }

                holder.check = (CheckBox) v.findViewById(R.id.appCheck);

            } else {
                if (style == Main.GRID_STYLE || style == Main.PAGED_STYLE) {
                    v = vi.inflate(R.layout.appgrid, null);
                } else {
                    v = vi.inflate(R.layout.approw, null);
                }
            }
            v.setDrawingCacheEnabled(true);


            holder.text = (TextView) v.findViewById(R.id.appName);
            holder.icon = (ImageView) v.findViewById(R.id.appIcon);

//                    holder.icon.setDrawingCacheEnabled(true);
//					holder.icon.setDrawingCacheQuality(524288);
            holder.icon.getDrawable().setDither(false);

            holder.text.setTextColor(fontColor);
            holder.text.setTextSize((int) (fontSize * SCALE + 0.5f));
            if (font != "Default") {
                holder.text.setTypeface(typeface);
            }

            holder.icon.getLayoutParams().height = (int) (iconSize * SCALE + 0.5f);
            holder.icon.getLayoutParams().width = (int) (iconSize * SCALE + 0.5f);

            v.setTag(holder);

        } else {
            holder = (ViewHolder) v.getTag();

        }

        if ((Main.mode == Main.MULTI_SELECT && holder.check == null)
                || (holder.check != null && Main.mode == Main.NORMAL && mode != Main.MULTI_SELECT)) {
            return getView(position, null, parent);
        }



        DrawerItem item = getItem(position);

        if (fontSize > 0) {
            holder.text.setText(item.getName());
        } else {
            holder.text.setText("");
        }

//					if(!holder.icon.getDrawable().equals(app.getIcon()))
        holder.icon.setImageDrawable(item.getIcon());
        if (Main.mode == Main.MULTI_SELECT) {
            boolean b = Main.checkedItems.contains(item);
            if (item instanceof Folder) {
                holder.check.setVisibility(View.INVISIBLE);
            } else {
                holder.check.setVisibility(View.VISIBLE);
            }

            holder.check.setChecked(b);

            if (b && style != Main.LIST_STYLE) {
                v.setBackgroundResource(R.drawable.rounded);
            } else {
                v.setBackgroundDrawable(null);
            }
        } else if (mode == Main.MULTI_SELECT) {
            holder.check.setChecked(HiddenApps.checkedApps.contains(item));
        }

        if (Main.mode == Main.FOLDER_EDIT) {
            if (Main.checkedItems.contains(item)) {
                AlphaAnimation a = new AlphaAnimation(1.0f, 0.5f);
                a.setFillAfter(true);
                v.startAnimation(a);
            } else {
                AlphaAnimation a = new AlphaAnimation(0.5f, 1.0f);
                a.setFillAfter(true);
                v.startAnimation(a);
            }

        }

        return v;

    }

    static class ViewHolder {

        TextView text;
        ImageView icon;
        CheckBox check;
    }

//    @Override
//    public Filter getFilter() {
//        if (filter == null) {
//            filter = new AppFiltering();
//        }
//
//        return filter;
//    }
//
//    private class AppFiltering extends Filter {
//
//        @Override
//        protected FilterResults performFiltering(CharSequence constraint) {
//            FilterResults result = new FilterResults();
//
//            constraint = constraint.toString().toLowerCase();
//            ArrayList<DrawerItem> filteredApps = new ArrayList<DrawerItem>();
//
//            if (constraint != null && !constraint.toString().trim().isEmpty()) {
//                for (DrawerItem it : Main.apps) {
//                    if(it.getName().toLowerCase().contains(constraint))
//                    {
//                        filteredApps.add(it);
//                    }
//                    
//                }
//
//                result.values = filteredApps;
//                result.count = filteredApps.size();
//            } else {
//                result.values = Main.apps;
//                result.count = Main.apps.size();
//            }
//
//            return result;
//        }
//
//        @Override
//        protected void publishResults(CharSequence constraint, FilterResults results) {
//            clear();
//            
//            for (DrawerItem it : (ArrayList<DrawerItem>)results.values)
//            {
//                add(it);
//            }
//            
//            notifyDataSetChanged();
//            notifyDataSetInvalidated();
//        }
//    }
}
