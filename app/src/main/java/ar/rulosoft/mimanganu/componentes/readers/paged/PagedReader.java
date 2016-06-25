package ar.rulosoft.mimanganu.componentes.readers.paged;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import java.util.List;

import ar.rulosoft.mimanganu.componentes.readers.Reader;

/**
 * Created by Raul on 24/06/2016.
 */

public class PagedReader extends Reader {

    public PagedReader(Context context) {
        super(context);
    }

    public PagedReader(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PagedReader(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void goToPage(int aPage) {

    }

    @Override
    public void reset() {

    }

    @Override
    public void seekPage(int index) {

    }

    @Override
    public void setPaths(List<String> paths) {

    }

    @Override
    public void changePath(int idx, String path) {

    }

    @Override
    public void freeMemory() {

    }

    @Override
    public void freePage(int idx) {

    }

    @Override
    public String getPath(int idx) {
        return null;
    }

    @Override
    public void reloadImage(int idx) {

    }

    @Override
    public boolean isLastPageVisible() {
        return false;
    }

    @Override
    public void setScrollSensitive(float mScrollSensitive) {

    }

    @Override
    public void setMaxTexture(int mTextureMax) {

    }

    @Override
    public int getCurrentPage() {
        return 0;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }
}
