package JakedUp.AppDrawer;

import java.io.File;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.d4a.tobias.R;

public class FontChooserAdapter extends ArrayAdapter<String> {

    private LayoutInflater vi;

    public FontChooserAdapter(Context context, int textViewResourceId, ArrayList<String> items) {
        super(context, textViewResourceId, items);

        vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;


        String f = getItem(position);


        v = vi.inflate(R.layout.font_item, null);

        TextView tv = (TextView) v.findViewById(R.id.fontItem);
        TextView tv2 = (TextView) v.findViewById(R.id.fontName);
        if (f != "Default") {
            try {
                if (f.startsWith(".")) {
                    tv.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "fonts/" + f.substring(1)));
                } else if (f.startsWith("+")) {
                    File sdCard = Environment.getExternalStorageDirectory();
                    File fontsFolder = new File(sdCard.getAbsolutePath() + Settings.FONTS_DIR);
                    tv.setTypeface(Typeface.createFromFile(new File(fontsFolder.getAbsolutePath() + "/" + f.substring(1))));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            tv.setText("Default");
        }

        tv2.setText(f);

        return v;

    }
}
