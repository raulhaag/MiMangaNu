package ar.rulosoft.mimanganu.componentes;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import ar.rulosoft.mimanganu.MangaFragment;
import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.ReaderFragment;
import ar.rulosoft.mimanganu.componentes.readers.Reader;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase;

import static ar.rulosoft.mimanganu.ReaderFragment.ADJUST_KEY;
import static ar.rulosoft.mimanganu.ReaderFragment.ORIENTATION;


/**
 * Created by Raúl on 11/03/2018.
 */

public class ReaderOptions extends FrameLayout {

    public static final String BLUE_FILTER_CHECK = "blue_filter_switch_";
    public static final String BLUE_FILTER_LEVEL = "blue_filter_level_";
    public static final String BRIGHT_CHECK = "bright_filter_switch__";
    public static final String BRIGHT_LEVEL = "bright_filter_level__";

    Reader.Direction mDirection;
    ImageViewTouchBase.DisplayType mScreenFit;
    LinearLayout optionsRoot;
    Activity mActivity;
    Reader reader;
    Manga manga;
    int d = -1;
    Button type, direction, adjust, keep_screen, rotate;
    CheckBox blueCheckbox, brightCheckbox;
    RelativeLayout root;
    SeekBar blueLevel, brightLevel;
    SharedPreferences pm;
    private int readerType;
    private boolean mKeepOn;
    private int mOrientation;
    private OptionListener optionListener;

    public ReaderOptions(Context context) {
        super(context);
        initialize();
    }

    public ReaderOptions(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public ReaderOptions(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public ReaderOptions(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initialize();
    }

    private void initialize() {
        pm = PreferenceManager.getDefaultSharedPreferences(getContext());
        LayoutInflater li = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (li != null) {
            li.inflate(R.layout.reader_options, this, true);

            root = findViewById(R.id.root_reader_options);
            optionsRoot = findViewById(R.id.option_root);
            type = findViewById(R.id.reader_type);
            direction = findViewById(R.id.reader_direction);
            adjust = findViewById(R.id.reader_ajust);
            rotate = findViewById(R.id.reader_rotate);
            keep_screen = findViewById(R.id.reader_stay_awake);
            blueCheckbox = findViewById(R.id.blue_cb);
            blueLevel = findViewById(R.id.blue_sb);
            brightCheckbox = findViewById(R.id.ux_cb);
            brightLevel = findViewById(R.id.ux_sb);
            ImageButton back = findViewById(R.id.reader_options_close);

            type.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (manga != null)
                        if (manga.getReaderType() == 2) {
                            manga.setReaderType(1);
                            readerType = 1;
                            type.setCompoundDrawablesWithIntrinsicBounds(null, ContextCompat.getDrawable(getContext(), R.drawable.ic_action_paged), null, null);
                            type.setText(R.string.paged_reader);
                        } else {
                            manga.setReaderType(2);
                            readerType = 2;
                            type.setCompoundDrawablesWithIntrinsicBounds(null, ContextCompat.getDrawable(getContext(), R.drawable.ic_action_continuous), null, null);
                            type.setText(R.string.continuous_reader);
                        }
                    if (manga != null)
                        Database.updateManga(getContext(), manga, false);
                    if (optionListener != null) {
                        optionListener.onOptionChange(OptionType.TYPE);
                    }
                    if (readerType == 2) {
                        adjust.setEnabled(false);
                        adjust.setAlpha(0.5f);
                    } else {
                        adjust.setEnabled(true);
                        adjust.setAlpha(1.f);
                    }
                }
            });

            direction.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    int readDirection;
                    if (manga != null && manga.getReadingDirection() != -1) {
                        readDirection = manga.getReadingDirection();
                    } else {
                        readDirection = Integer.parseInt(pm.getString(MangaFragment.DIRECTION, "" + Reader.Direction.L2R.ordinal()));
                    }
                    if (readDirection == Reader.Direction.R2L.ordinal()) {
                        direction.setCompoundDrawablesWithIntrinsicBounds(null, ContextCompat.getDrawable(getContext(), R.drawable.ic_action_inverso), null, null);
                        mDirection = Reader.Direction.L2R;
                    } else if (readDirection == Reader.Direction.L2R.ordinal()) {
                        direction.setCompoundDrawablesWithIntrinsicBounds(null, ContextCompat.getDrawable(getContext(), R.drawable.ic_action_verical), null, null);
                        mDirection = Reader.Direction.VERTICAL;
                    } else {
                        direction.setCompoundDrawablesWithIntrinsicBounds(null, ContextCompat.getDrawable(getContext(), R.drawable.ic_action_clasico), null, null);
                        mDirection = Reader.Direction.R2L;
                    }
                    if (manga != null) {
                        manga.setReadingDirection(mDirection.ordinal());
                        Database.updateReadOrder(getContext(), mDirection.ordinal(), manga.getId());
                    }
                    if (optionListener != null) {
                        optionListener.onOptionChange(OptionType.DIRECTION);
                    }
                }
            });

            adjust.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mScreenFit = mScreenFit.getNext();
                    SharedPreferences.Editor editor = pm.edit();
                    editor.putString(ADJUST_KEY, mScreenFit.toString());
                    editor.apply();
                    updateIconAdjust(mScreenFit);
                    if (optionListener != null) {
                        optionListener.onOptionChange(OptionType.ADJUST);
                    }
                }
            });

            keep_screen.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!mKeepOn) {
                        keep_screen.setCompoundDrawablesWithIntrinsicBounds(null, ContextCompat.getDrawable(getContext(), R.drawable.ic_action_mantain_screen_on), null, null);
                        if (mActivity != null && optionListener != null)
                            mActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                        keep_screen.setText(R.string.stay_awake_on);
                    } else {
                        keep_screen.setCompoundDrawablesWithIntrinsicBounds(null, ContextCompat.getDrawable(getContext(), R.drawable.ic_action_mantain_screen_off), null, null);
                        if (mActivity != null && optionListener != null)
                            mActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                        keep_screen.setText(R.string.stay_awake_off);
                    }
                    mKeepOn = !mKeepOn;
                    if (optionListener != null) {
                        optionListener.onOptionChange(OptionType.KEEP_SCREEN);
                    }
                    SharedPreferences.Editor editor = pm.edit();
                    editor.putBoolean(ReaderFragment.KEEP_SCREEN_ON, mKeepOn);
                    editor.apply();
                }
            });

            rotate.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOrientation = (mOrientation + 1) % 3;
                    updateIconOrientation();
                    if (optionListener != null) {
                        optionListener.onOptionChange(OptionType.ROTATE);
                    }
                    SharedPreferences.Editor editor = pm.edit();
                    editor.putInt(ReaderFragment.ORIENTATION, mOrientation);
                    editor.apply();
                }
            });

            updateValues();
            blueCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        if (reader != null)
                            reader.setBlueFilter((100f - blueLevel.getProgress()) / 100f);
                        blueLevel.setEnabled(true);
                    } else {
                        if (reader != null)
                            reader.setBlueFilter(1f);
                        blueLevel.setEnabled(false);
                    }
                }
            });

            blueLevel.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (reader != null && blueCheckbox.isChecked())
                        reader.setBlueFilter((100f - progress) / 100f);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    if (reader != null)
                        reader.setBlueFilter((100f - seekBar.getProgress()) / 100f);
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

            brightCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    startStopBrightLevel(isChecked);
                    if (isChecked)
                        setBrightLevel(brightLevel.getProgress());

                    brightLevel.setEnabled(isChecked);
                }
            });

            brightLevel.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    setBrightLevel(brightLevel.getProgress());
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    setBrightLevel(brightLevel.getProgress());
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
            back.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    switchOptions();
                }
            });
        }
    }

    public void setManga(Manga manga) {
        this.manga = manga;
        setValues();
    }

    public void setValues() {
        //   Direction
        if (manga != null && manga.getReadingDirection() != -1) {
            d = manga.getReadingDirection();
        } else {
            d = Integer.parseInt(pm.getString(MangaFragment.DIRECTION, "" + Reader.Direction.L2R.ordinal()));
        }
        if (d == Reader.Direction.R2L.ordinal()) {
            this.mDirection = Reader.Direction.R2L;
            direction.setCompoundDrawablesWithIntrinsicBounds(null, ContextCompat.getDrawable(getContext(), R.drawable.ic_action_clasico), null, null);
        } else if (d == Reader.Direction.L2R.ordinal()) {
            this.mDirection = Reader.Direction.L2R;
            direction.setCompoundDrawablesWithIntrinsicBounds(null, ContextCompat.getDrawable(getContext(), R.drawable.ic_action_inverso), null, null);
        } else {
            this.mDirection = Reader.Direction.VERTICAL;
            direction.setCompoundDrawablesWithIntrinsicBounds(null, ContextCompat.getDrawable(getContext(), R.drawable.ic_action_verical), null, null);
        }

        // Type
        readerType = pm.getBoolean("reader_type", true) ? 1 : 2;
        if (manga != null && manga.getReaderType() != 0) {
            readerType = manga.getReaderType();
        }
        if (readerType == 2) {
            type.setCompoundDrawablesWithIntrinsicBounds(null, ContextCompat.getDrawable(getContext(), R.drawable.ic_action_continuous), null, null);
            type.setText(R.string.continuous_reader);
        } else {
            type.setCompoundDrawablesWithIntrinsicBounds(null, ContextCompat.getDrawable(getContext(), R.drawable.ic_action_paged), null, null);
            type.setText(R.string.paged_reader);
        }
        if (readerType == 2) {
            adjust.setEnabled(false);
            adjust.setAlpha(0.5f);
        }

        // Ajust
        mScreenFit = ImageViewTouchBase.DisplayType.valueOf(pm.getString(ADJUST_KEY, ImageViewTouchBase.DisplayType.FIT_TO_WIDTH.toString()));
        updateIconAdjust(mScreenFit);

        // KeepOn
        mKeepOn = pm.getBoolean(ReaderFragment.KEEP_SCREEN_ON, false);
        if (mKeepOn) {
            keep_screen.setCompoundDrawablesWithIntrinsicBounds(null, ContextCompat.getDrawable(getContext(), R.drawable.ic_action_mantain_screen_on), null, null);
            if (mActivity != null)
                mActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        // Orientation
        mOrientation = pm.getInt(ORIENTATION, 2);
        updateIconOrientation();
        updateValues();
    }

    private void updateIconAdjust(ImageViewTouchBase.DisplayType displayType) {
        switch (displayType) {
            case NONE:
                adjust.setCompoundDrawablesWithIntrinsicBounds(null, ContextCompat.getDrawable(getContext(), R.drawable.ic_action_original), null, null);
                adjust.setText(R.string.no_scale);
                break;
            case FIT_TO_HEIGHT:
                adjust.setCompoundDrawablesWithIntrinsicBounds(null, ContextCompat.getDrawable(getContext(), R.drawable.ic_action_ajustar_alto), null, null);
                adjust.setText(R.string.ajuste_alto);
                break;
            case FIT_TO_WIDTH:
                adjust.setCompoundDrawablesWithIntrinsicBounds(null, ContextCompat.getDrawable(getContext(), R.drawable.ic_action_ajustar_ancho), null, null);
                adjust.setText(R.string.ajuste_ancho);
                break;
            case FIT_TO_SCREEN:
                adjust.setCompoundDrawablesWithIntrinsicBounds(null, ContextCompat.getDrawable(getContext(), R.drawable.ic_action_ajustar_diagonal), null, null);
                adjust.setText(R.string.mejor_ajuste);
                break;
            default:
                break;

        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    private void updateIconOrientation() {
        if (mOrientation == 0) {
            if (mActivity != null)
                mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            rotate.setCompoundDrawablesWithIntrinsicBounds(null, ContextCompat.getDrawable(getContext(), R.drawable.ic_action_screen_landscape), null, null);
            rotate.setText(R.string.lock_on_landscape);
        } else if (mOrientation == 1) {
            if (mActivity != null)
                mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            rotate.setCompoundDrawablesWithIntrinsicBounds(null, ContextCompat.getDrawable(getContext(), R.drawable.ic_action_screen_portrait), null, null);
            rotate.setText(R.string.lock_on_portrait);
        } else if (mOrientation == 2) {
            if (mActivity != null)
                mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            rotate.setCompoundDrawablesWithIntrinsicBounds(null, ContextCompat.getDrawable(getContext(), R.drawable.ic_action_screen_free), null, null);
            rotate.setText(R.string.rotation_no_locked);
        }
        SharedPreferences.Editor editor = pm.edit();
        editor.putInt(ORIENTATION, mOrientation);
        editor.apply();
    }

    private void saveValues() {
        pm.edit().putInt(BLUE_FILTER_LEVEL, blueLevel.getProgress()).apply();
        pm.edit().putBoolean(BLUE_FILTER_CHECK, blueCheckbox.isChecked()).apply();

        pm.edit().putInt(BRIGHT_LEVEL, brightLevel.getProgress()).apply();
        pm.edit().putBoolean(BRIGHT_CHECK, brightCheckbox.isChecked()).apply();
    }

    public void updateValues() {
        blueCheckbox.setChecked(pm.getBoolean(BLUE_FILTER_CHECK, false));
        blueLevel.setProgress(pm.getInt(BLUE_FILTER_LEVEL, 30));
        blueLevel.setEnabled(blueCheckbox.isChecked());

        brightCheckbox.setChecked(pm.getBoolean(BRIGHT_CHECK, false));
        brightLevel.setProgress(pm.getInt(BRIGHT_LEVEL, 125));
        brightLevel.setEnabled(brightCheckbox.isChecked());
    }

    public void switchOptions() {
        ValueAnimator vA = ValueAnimator.ofFloat(0f, 1f);
        vA.setInterpolator(new DecelerateInterpolator());
        vA.setDuration(300);
        vA.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                optionsRoot.setAlpha(animation.getAnimatedFraction());
            }
        });
        if (root.getVisibility() == GONE) {
            optionsRoot.setAlpha(0f);
            root.setVisibility(VISIBLE);
            updateValues();
            vA.start();
        } else {
            saveValues();
            vA.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    root.setVisibility(GONE);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            vA.reverse();
        }
    }

    @Override
    public void setBackgroundColor(int color) {
        optionsRoot.setBackgroundColor(color);
    }

    public void setActivity(Activity mActivity) {
        this.mActivity = mActivity;
    }

    public void setReader(Reader reader) {
        if (reader != null) {
            this.reader = reader;
            if (blueCheckbox.isChecked()) {
                reader.setBlueFilter((100f - blueLevel.getProgress()) / 100f);
            }
            if (brightCheckbox.isChecked()) {
                startStopBrightLevel(true);
                setBrightLevel(brightLevel.getProgress());
            }
        }
    }

    public void setOptionListener(OptionListener optionListener) {
        this.optionListener = optionListener;
    }

    public ImageViewTouchBase.DisplayType getScreenFit() {
        return mScreenFit;
    }

    public Reader.Type getReaderType() {
        if (readerType == 2)
            return Reader.Type.CONTINUOUS;
        else
            return Reader.Type.PAGED;
    }

    public Reader.Direction getDirection() {
        return mDirection;
    }

    public boolean isVisible() {
        return root.getVisibility() == VISIBLE;
    }

    public void setBrightLevel(int level) {
        if (mActivity == null) return;
        WindowManager.LayoutParams layoutParams = mActivity.getWindow().getAttributes();
        layoutParams.screenBrightness = ((float) level / 255.0f);
        mActivity.getWindow().setAttributes(layoutParams);
    }

    public void startStopBrightLevel(boolean start) {
        if (mActivity == null) return;
        WindowManager.LayoutParams layoutParams = mActivity.getWindow().getAttributes(); // Get Params
        if (start) {
            layoutParams.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL;
        } else {
            layoutParams.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
        }
        mActivity.getWindow().setAttributes(layoutParams); // Set params
    }


    public enum OptionType {
        TYPE,
        DIRECTION,
        ADJUST,
        KEEP_SCREEN,
        ROTATE
    }

    public interface OptionListener {
        void onOptionChange(OptionType optionType);
    }
}