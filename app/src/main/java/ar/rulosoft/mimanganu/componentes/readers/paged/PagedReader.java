package ar.rulosoft.mimanganu.componentes.readers.paged;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import java.io.File;
import java.util.List;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.readers.Reader;
import it.sephiroth.android.library.TapListener;
import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase.InitialPosition;
import it.sephiroth.android.library.imagezoom.graphics.FastBitmapDrawable;

/**
 * Created by Raul on 24/06/2016.
 */

public abstract class PagedReader extends Reader implements TapListener {

    private static ImageViewTouchBase.DisplayType mScreenFit;
    protected PageAdapter mPageAdapter;
    List<String> paths;
    int currentPage = 0;
    private InitialPosition iniPosition = InitialPosition.LEFT_UP;

    public PagedReader(Context context) {
        super(context);
    }

    public abstract void setPagerAdapter(PageAdapter mPageAdapter);

    @Override
    public void setScreenFit(ImageViewTouchBase.DisplayType displayType) {
        mScreenFit = displayType;
        if (mPageAdapter != null)
            mPageAdapter.updateDisplayType();
    }

    @Override
    public void setPaths(List<String> paths) {
        this.paths = paths;
        setPagerAdapter(new PageAdapter());
    }

    @Override
    public void freeMemory() {
        setPagerAdapter(null);
    }

    @Override
    public void freePage(int idx) {
        if(idx == 0){
            if (mPageAdapter != null && mPageAdapter.pages[idx] != null) {
                mPageAdapter.pages[idx].unloadImage();
            }
        } else {
            if (mPageAdapter != null && mPageAdapter.pages[idx - 1] != null) {
                mPageAdapter.pages[idx - 1].unloadImage();
            }
        }
    }

    @Override
    public String getPath(int idx) {
        if (paths != null) {
            if (idx == 0)
                return paths.get(idx);
            else
                return paths.get(idx - 1);
        } else
            return "";
    }

    @Override
    public void reset() {
        setPagerAdapter(null);
        currentPage = 0;
    }

    @Override
    public void reloadImage(int idx) {
        //Log.d("PR", "idx: " + idx);
        if (mPageAdapter != null) {
            if(idx > mPageAdapter.pages.length){
                Log.e("PagedReader","idx > mPageAdapter.pages.length !");
            } else {
                if (idx == 0) {
                    if (mPageAdapter.pages[idx] != null) {
                        mPageAdapter.pages[idx].setImage();
                    }
                } else {
                    if (mPageAdapter.pages[idx - 1] != null) {
                        mPageAdapter.pages[idx - 1].setImage();
                    }
                }
            }
        }
    }

    @Override
    public void setScrollSensitive(float mScrollSensitive) {
        this.mScrollSensitive = mScrollSensitive;
        if (mPageAdapter != null)
            mPageAdapter.setPageScroll(mScrollSensitive);
    }

    @Override
    public boolean hasFitFeature() {
        return true;
    }

    public class PageAdapter extends PagerAdapter {
        private Page[] pages;

        PageAdapter(){
            pages = new Page[paths.size()];
        }

        public Page getCurrentPage() {
            return pages[currentPage];
        }

        public void setCurrentPage(int nCurrentPage) {
            if (mDirection == Direction.L2R)
                nCurrentPage = paths.size() - nCurrentPage;
            currentPage = nCurrentPage;
            for (int i = 0; i < pages.length; i++) {
                if (pages[i] != null) {
                    if (Math.abs(i - nCurrentPage) <= 1 && !pages[i].imageLoaded) {
                        pages[i].setImage();
                    } else if (Math.abs(i - nCurrentPage) > 1 && pages[i].imageLoaded) {
                        pages[i] = null;
                    }
                }
            }
        }

        Page getPage(int idx){
            if (idx < 0)
                idx = 0;
            else if (idx >= pages.length)
                idx = pages.length - 1;
            return pages[idx];
        }

        @Override
        public int getCount() {
            if (pages != null)
                return pages.length;
            else return 0;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            if (mDirection == Direction.L2R)
                position = getCount() - position;
            Page page = pages[position];
            if (pages[position] != null) {
                container.addView(page, 0);
            } else {
                Context context = getContext();
                page = new Page(context);
                page.setImage(paths.get(position));
                container.addView(page, 0);
                page.index = position;
                pages[position] = page;
            }
            return page;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            try {
                container.removeView((Page) object);
            } catch (Exception ignore) {

            }
        }

        public void updateDisplayType() {
            for (int i = 0; i < pages.length; i++) {
                if (pages[i] != null) {
                    pages[i].visor.setDisplayType(mScreenFit);
                }
            }
        }

        public void setPageScroll(float pageScroll) {
            if (pages != null)
                for (int i = 0; i < pages.length; i++) {
                    if (pages[i] != null) {
                        pages[i].visor.setScrollFactor(pageScroll);
                    }
                }
        }
    }

    public class Page extends RelativeLayout {
        public ImageViewTouch visor;
        ProgressBar loading;
        boolean loadingImage = false;
        boolean imageLoaded = false;
        int index = 0;
        private String path = null;

        public Page(Context context) {
            super(context);
            init();
        }

        public void init() {
            String infService = Context.LAYOUT_INFLATER_SERVICE;
            LayoutInflater li = (LayoutInflater) getContext().getSystemService(infService);
            li.inflate(R.layout.view_reader_page, this, true);
            visor = (ImageViewTouch) findViewById(R.id.visor);
            visor.setDisplayType(mScreenFit);
            visor.setTapListener(PagedReader.this);
            visor.setScaleEnabled(false);
            loading = (ProgressBar) findViewById(R.id.loading);
            loading.bringToFront();
            visor.setScrollFactor(mScrollSensitive);
        }

        public void unloadImage() {
            if (visor != null) {
                if (visor.getDrawable() != null)
                    ((FastBitmapDrawable) visor.getDrawable()).getBitmap().recycle();
                visor.setImageDrawable(null);
                visor.setImageBitmap(null);
            }
            imageLoaded = false;
            loadingImage = false;
        }

        public void setImage() {
            if (!imageLoaded && visor != null && !loadingImage)
                new SetImageTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

        public void setImage(String path) {
            this.path = path;
            setImage();
        }

        public boolean canScroll(int dx) {
            return visor == null || visor.canScroll(dx);
        }

        public boolean canScrollV(int dx) {
            return visor == null || visor.canScrollV(dx);
        }

        public class SetImageTask extends AsyncTask<Void, Void, Bitmap> {

            @Override
            protected void onPreExecute() {
                loadingImage = true;
                if (loading != null)
                    loading.setVisibility(ProgressBar.VISIBLE);
                super.onPreExecute();
            }

            @Override
            protected Bitmap doInBackground(Void... params) {
                if (new File(path).exists()) {
                    boolean notLoaded = true;
                    int retry = 5;
                    Bitmap bitmap = null;
                    BitmapFactory.Options opts = new BitmapFactory.Options();
                    opts.inPreferredConfig = Bitmap.Config.RGB_565;
                    while (notLoaded && retry > 0) {
                        try {
                            bitmap = BitmapFactory.decodeFile(path, opts);
                            notLoaded = false;
                        } catch (OutOfMemoryError oom) {
                            retry--;
                            try {
                                Thread.sleep(3000);//time to free memory
                            } catch (InterruptedException ignored) {
                            }
                        }
                    }
                    return bitmap;
                } else {
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Bitmap result) {
                if (result != null && visor != null) {
                    imageLoaded = true;
                    visor.setScaleEnabled(true);
                    if (mDirection == Direction.VERTICAL)
                        visor.setInitialPosition(iniPosition);
                    else visor.setInitialPosition(ImageViewTouchBase.InitialPosition.LEFT_UP);
                    if ((result.getHeight() > mTextureMax ||
                            result.getWidth() > mTextureMax) &&
                            Build.VERSION.SDK_INT >= 11) {
                        visor.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                    }
                    visor.setAlpha(0f);
                    visor.setImageBitmap(result);
                    if (index == getCurrentPage()) {
                        ObjectAnimator.ofFloat(visor, "alpha", 1f).setDuration(500).start();
                    } else {
                        visor.setAlpha(1f);
                    }
                    loading.setVisibility(ProgressBar.INVISIBLE);
                }
                loadingImage = false;
                super.onPostExecute(result);
            }
        }
    }
}
