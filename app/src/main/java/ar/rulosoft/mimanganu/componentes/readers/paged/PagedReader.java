package ar.rulosoft.mimanganu.componentes.readers.paged;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.viewpager.widget.PagerAdapter;

import java.io.FileNotFoundException;
import java.io.InputStream;
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
    int currentPage = 1;  //keep the value from 1..n for externally view
    private InitialPosition iniPosition = InitialPosition.LEFT_UP;
    ColorFilter savedCf;

    public PagedReader(Context context) {
        super(context);
    }

    public abstract void setPagerAdapter(PageAdapter mPageAdapter);

    protected abstract int getCurrentPosition();

    @Override
    public void setScreenFit(ImageViewTouchBase.DisplayType displayType) {
        mScreenFit = displayType;
        if (mPageAdapter != null)
            mPageAdapter.updateDisplayType();
    }

    @Override
    public int getPages() {
        return paths.size();
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
        int iIdx = idx - 1;
        if (mPageAdapter != null && mPageAdapter.pages[iIdx] != null) {
            mPageAdapter.pages[iIdx].unloadImage();
        }
    }

    @Override
    public String getPath(int idx) {
        if (paths != null) {
            return paths.get(idx - 1);
        }
        return "";
    }

    @Override
    public void reset() {
        setPagerAdapter(null);
        currentPage = 1;
    }

    @Override
    public void reloadImage(int idx) {
        if (mPageAdapter != null && mPageAdapter.pages[idx - 1] != null) {
            mPageAdapter.pages[idx - 1].setImage();
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

    @Override
    public void setBlueFilter(float bf) {
        ColorMatrix cm = new ColorMatrix();
        cm.set(new float[]{1, 0, 0, 0, 0,
                0, (0.6f + 0.4f * bf), 0, 0, 0,
                0f, 0f, (0.1f + 0.9f * bf), 0, 0,
                0, 0, 0, 1f, 0});
        savedCf = new ColorMatrixColorFilter(cm);
        if (mPageAdapter != null)
            mPageAdapter.updateBlueFilter(savedCf);
    }

    public class PageAdapter extends PagerAdapter {
        private Page[] pages;
        private ColorFilter cf;

        PageAdapter() {
            if (savedCf == null) {
                ColorMatrix cm = new ColorMatrix();
                cm.set(new float[]{1f, 0, 0, 0, 0,
                        0, 1f, 0, 0, 0,
                        0, 0, 1f, 0, 0,
                        0, 0, 0, 1f, 0});
                cf = new ColorMatrixColorFilter(cm);
                savedCf = cf;
            } else {
                cf = savedCf;
            }
            pages = new Page[paths.size()];
        }

        public Page getCurrentPage() {
            return pages[getCurrentPosition()];
        }

        public void setCurrentPage(int nCurrentPage) {
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

        Page getPage(int idx) {
            return pages[idx - 1];
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
            if (mDirection == Direction.L2R) {
                position = getCount() - position;
            }

            Page page = pages[position];
            if (page == null) {
                page = new Page(getContext());
                page.visor.setColorFilter(cf);
                page.setImage(paths.get(position));
                page.index = position;
                pages[position] = page;
            }

            container.addView(page, 0);
            return page;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            try {
                container.removeView((Page) object);
            } catch (Exception ignore) {

            }
        }

        public void updateBlueFilter(ColorFilter cf) {
            this.cf = cf;
            for (Page page : pages) {
                if (page != null) {
                    page.visor.setColorFilter(cf);
                }
            }
        }

        void updateDisplayType() {
            for (Page page : pages) {
                if (page != null) {
                    page.visor.setDisplayType(mScreenFit);
                }
            }
        }

        void setPageScroll(float pageScroll) {
            if (pages != null)
                for (Page page : pages) {
                    if (page != null) {
                        page.visor.setScrollFactor(pageScroll);
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
            assert li != null;
            li.inflate(R.layout.view_reader_page, this, true);
            visor = findViewById(R.id.visor);
            visor.setDisplayType(mScreenFit);
            visor.setTapListener(PagedReader.this);
            visor.setScaleEnabled(false);
            loading = findViewById(R.id.loading);
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
            if (!imageLoaded && visor != null && !loadingImage) {
                new SetImageTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
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

        @SuppressLint("StaticFieldLeak")
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
                if (fileExist(path)) {
                    boolean notLoaded = true;
                    int retry = 5;
                    Bitmap bitmap = null;
                    BitmapFactory.Options opts = new BitmapFactory.Options();
                    opts.inPreferredConfig = Bitmap.Config.RGB_565;
                    while (notLoaded && retry > 0) {
                        try {
                            try (InputStream inputStream = getInputStream(path)) {
                                bitmap = BitmapFactory.decodeStream(inputStream, null, opts);
                                notLoaded = false;
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
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
                            result.getWidth() > mTextureMax)) {
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
