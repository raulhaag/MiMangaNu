package ar.rulosoft.mimanganu.componentes;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.GridView;

public class UnScrolledGridView extends GridView {

    public UnScrolledGridView(Context context) {
        super(context);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(MEASURED_SIZE_MASK, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
        ViewGroup.LayoutParams params = getLayoutParams();
        params.height = getMeasuredHeight();
    }
}

