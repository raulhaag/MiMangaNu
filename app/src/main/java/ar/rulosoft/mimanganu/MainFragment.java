package ar.rulosoft.mimanganu;

import android.animation.ObjectAnimator;
import android.support.v4.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.melnykov.fab.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import ar.rulosoft.mimanganu.componentes.MoreMangasPageTransformer;

/**
 * Created by Raul on 09/04/2016.
 */
public class MainFragment extends Fragment implements View.OnClickListener {

    public static final String SERVER_ID = "server_id";
    public static final String MANGA_ID = "manga_id";
    Menu menu;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private MisMangasFragment misMangasFragment;
    private AddMangaFragment addMangaFragment;
    private ViewPager mViewPager;
    FloatingActionButton button_add;
    private SharedPreferences pm;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.activity_mis_mangas,container,false);
    }

    @Override
    public void onStart() {
        super.onStart();
        mSectionsPagerAdapter = new SectionsPagerAdapter(getChildFragmentManager());
        addMangaFragment = new AddMangaFragment();
        misMangasFragment = new MisMangasFragment();

        addMangaFragment.setRetainInstance(true);
        misMangasFragment.setRetainInstance(true);
        mSectionsPagerAdapter.add(misMangasFragment);
        mSectionsPagerAdapter.add(addMangaFragment);

        mViewPager = (ViewPager) getView().findViewById(R.id.pager);
        button_add = (FloatingActionButton) getView().findViewById(R.id.button_add);
        button_add.setOnClickListener(this);
        pm = PreferenceManager.getDefaultSharedPreferences(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setPageTransformer(false, new MoreMangasPageTransformer());
        ((MainActivity)getActivity()).enableHomeButton(false);
        ((MainActivity)getActivity()).setTitle(getString(R.string.app_name));
    }

    @Override
    public void onClick(View v) {
        if (mViewPager.getCurrentItem() == 0) {
            ObjectAnimator anim =
                    ObjectAnimator.ofFloat(v, "rotation", 360.0f, 315.0f);
            anim.setDuration(200);
            anim.start();
            mViewPager.setCurrentItem(1);
        } else {
            ObjectAnimator anim =
                    ObjectAnimator.ofFloat(v, "rotation", 315.0f, 360.0f);
            anim.setDuration(200);
            anim.start();
            mViewPager.setCurrentItem(0);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.view_mismangas, menu);

        /** Set hide/unhide checkbox */
        boolean checkedRead = pm.getInt(MisMangasFragment.SELECT_MODE, MisMangasFragment.MODE_SHOW_ALL) > 0;
        menu.findItem(R.id.action_hide_read).setChecked(checkedRead);

        /** Set sort mode */
        int sortList[] = {
                R.id.sort_last_read, R.id.sort_last_read_asc,
                R.id.sort_name, R.id.sort_name_asc,
                R.id.sort_author, R.id.sort_author_asc,
                R.id.sort_finished, R.id.sort_finished_asc
        };
        menu.findItem(sortList[pm.getInt("manga_view_sort_by", 0)]).setChecked(true);
        this.menu = menu;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_view_download: {
                ((MainActivity)getActivity()).replaceFragment(new DownloadsFragment(),"DownloadFragment");
                break;
            }
            case R.id.action_hide_read: {
                item.setChecked(!item.isChecked());
                pm.edit().putInt(MisMangasFragment.SELECT_MODE,
                        item.isChecked() ?
                                MisMangasFragment.MODE_HIDE_READ : MisMangasFragment.MODE_SHOW_ALL
                ).apply();
                if (misMangasFragment != null)
                    misMangasFragment.setListManga(true);
                break;
            }
            case R.id.action_settings: {
                //TODO((MainActivity)getActivity()).replaceFragment(new PreferencesFragment(),"PreferencesFragment");
                break;
            }
            case R.id.sort_last_read: {
                item.setChecked(true);
                pm.edit().putInt("manga_view_sort_by", 0).apply();
                misMangasFragment.setListManga(true);
                break;
            }
            case R.id.sort_last_read_asc: {
                item.setChecked(true);
                pm.edit().putInt("manga_view_sort_by", 1).apply();
                misMangasFragment.setListManga(true);
                break;
            }
            case R.id.sort_name: {
                item.setChecked(true);
                pm.edit().putInt("manga_view_sort_by", 2).apply();
                misMangasFragment.setListManga(true);
                break;
            }
            case R.id.sort_name_asc: {
                item.setChecked(true);
                pm.edit().putInt("manga_view_sort_by", 3).apply();
                misMangasFragment.setListManga(true);
                break;
            }
            case R.id.sort_author: {
                item.setChecked(true);
                pm.edit().putInt("manga_view_sort_by", 4).apply();
                misMangasFragment.setListManga(true);
                break;
            }
            case R.id.sort_author_asc: {
                item.setChecked(true);
                pm.edit().putInt("manga_view_sort_by", 5).apply();
                misMangasFragment.setListManga(true);
                break;
            }
            case R.id.sort_finished: {
                item.setChecked(true);
                pm.edit().putInt("manga_view_sort_by", 6).apply();
                misMangasFragment.setListManga(true);
                break;
            }
            case R.id.sort_finished_asc: {
                item.setChecked(true);
                pm.edit().putInt("manga_view_sort_by", 7).apply();
                misMangasFragment.setListManga(true);
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }



    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        List<android.support.v4.app.Fragment> fragments;

        public SectionsPagerAdapter(android.support.v4.app.FragmentManager fm) {
            super(fm);
            this.fragments = new ArrayList<>();
        }

        public void add(android.support.v4.app.Fragment f) {
            fragments.add(f);
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }
    }
}
