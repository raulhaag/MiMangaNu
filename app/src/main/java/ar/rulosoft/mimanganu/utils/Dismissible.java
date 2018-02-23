package ar.rulosoft.mimanganu.utils;

//We use this to remove the Fragment only when the animation finished
public interface Dismissible {
    void dismiss(OnDismissedListener listener);

    interface OnDismissedListener {
        void onDismissed();
    }
}
