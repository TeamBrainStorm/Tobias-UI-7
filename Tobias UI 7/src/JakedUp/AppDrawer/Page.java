package JakedUp.AppDrawer;

import android.content.Context;
import android.widget.GridView;

public class Page extends GridView {

	public Page(Context context) {
		super(context);
	}
	
	@Override
	public void setOnScrollListener(OnScrollListener l) {
		
		super.setOnScrollListener(l);
	}

}
