package ar.rulosoft.mimanganu;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

import com.melnykov.fab.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import ar.rulosoft.mimanganu.componentes.MasMangasPageTransformer;
import ar.rulosoft.mimanganu.utils.ThemeColors;

public class ActivityMisMangas extends ActionBarActivity implements OnClickListener {

    public static final String SERVER_ID = "server_id";
    public static final String MANGA_ID = "manga_id";
    public static final String MOSTRAR_EN_GALERIA = "mostrarengaleria";

    SectionsPagerAdapter mSectionsPagerAdapter;

    ViewPager mViewPager;
    public int[] colors;
    FragmentMisMangas fragmentMisMangas;
    FragmentAddManga fragmentAddManga;
    SharedPreferences pm;
    FloatingActionButton button_add;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_mis_mangas);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        fragmentAddManga = new FragmentAddManga();
        fragmentMisMangas = new FragmentMisMangas();

        fragmentAddManga.setRetainInstance(true);

        mSectionsPagerAdapter.add(fragmentMisMangas);
        mSectionsPagerAdapter.add(fragmentAddManga);

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setPageTransformer(false, new MasMangasPageTransformer());

        button_add = (FloatingActionButton) findViewById(R.id.button_add);
        button_add.setOnClickListener(this);
        pm = PreferenceManager.getDefaultSharedPreferences(ActivityMisMangas.this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mis_mangas, menu);
        MenuItem menuEsconderSinLectura = menu.findItem(R.id.action_esconder_leidos);
        boolean checkedLeidos = pm.getInt(FragmentMisMangas.SELECTOR_MODO, FragmentMisMangas.MODO_ULTIMA_LECTURA_Y_NUEVOS) > 0;
        menuEsconderSinLectura.setChecked(checkedLeidos);
        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.descargas) {
            Intent intent = new Intent(this, ActivityDescargas.class);
            startActivity(intent);
        } else if (id == R.id.licencia) {
            Intent intent = new Intent(this, ActivityLicencia.class);
            startActivity(intent);
        } else if (id == R.id.action_esconder_leidos) {
            if (item.isChecked()) {
                item.setChecked(false);
                pm.edit().putInt(FragmentMisMangas.SELECTOR_MODO, FragmentMisMangas.MODO_ULTIMA_LECTURA_Y_NUEVOS).commit();
            } else {
                item.setChecked(true);
                pm.edit().putInt(FragmentMisMangas.SELECTOR_MODO, FragmentMisMangas.MODO_SIN_LEER).commit();
            }
            try {
                fragmentMisMangas.cargarMangas();
            } catch (Exception e) {
                e.printStackTrace();
                // TODO
            }
        } else if (id == R.id.action_configurar) {
            startActivity(new Intent(ActivityMisMangas.this, OpcionesActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mViewPager.getCurrentItem() == 1) {
            onClick(button_add);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        colors = ThemeColors.getColors(pm, getApplicationContext());
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(colors[0]));
        button_add.setColorNormal(colors[1]);
        button_add.setColorPressed(colors[3]);
        button_add.setColorRipple(colors[0]);
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        if (mViewPager.getCurrentItem() == 0) {
            ObjectAnimator anim = ObjectAnimator.ofFloat(v, "rotation", 360.0f, 315.0f);
            anim.setDuration(200);
            anim.start();
            mViewPager.setCurrentItem(1);
        } else {
            ObjectAnimator anim = ObjectAnimator.ofFloat(v, "rotation", 315.0f, 360.0f);
            anim.setDuration(200);
            anim.start();
            mViewPager.setCurrentItem(0);
        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        List<Fragment> fragments;
        private String tabs[] = new String[]{getResources().getString(R.string.mismangas), getResources().getString(R.string.masmangas)};

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            this.fragments = new ArrayList<Fragment>();
        }

        public void add(Fragment f) {
            fragments.add(f);
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public float getPageWidth(int position) {
            return 1f;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabs[position];
        }
    }

    public interface OnFinishTask {
        void onFinish();
    }

}
