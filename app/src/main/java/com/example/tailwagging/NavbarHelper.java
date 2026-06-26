package com.example.tailwagging;

import android.animation.ObjectAnimator;
import android.animation.AnimatorSet;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

/**
 * Modern Helper for the persistent floating navigation bar.
 *
 * Active-state design:
 *   • Chip background fades in  (alpha 0 → 1) behind the icon
 *   • Icon tints to primary color and scales up slightly (0.95 → 1.1)
 *   • Label tints to primary color at full opacity
 *   • Inactive items stay dimmed at 0.55 alpha
 *
 * Zero-flicker transitions are preserved via overridePendingTransition(0,0).
 */
public class NavbarHelper {

    /* ─── animation constants ─────────────────────────────────── */
    private static final long ANIM_DURATION  = 220L;
    private static final float SCALE_ACTIVE   = 1.10f;
    private static final float SCALE_INACTIVE = 1.00f;
    private static final float ALPHA_ACTIVE   = 1.00f;
    private static final float ALPHA_INACTIVE = 0.55f;
    private static final float CHIP_ACTIVE    = 1.00f;
    private static final float CHIP_INACTIVE  = 0.00f;

    /* ─── public setup ────────────────────────────────────────── */

    public static void setupNavbar(Activity activity) {
        android.view.ViewGroup container = activity.findViewById(R.id.bottomNavContainer);
        String rolePref = activity.getSharedPreferences("UserPrefs", Activity.MODE_PRIVATE).getString("user_role", "Pet Owner");
        String role = (rolePref != null) ? rolePref.trim() : "Pet Owner";
        boolean isProvider = "Veterinarian".equalsIgnoreCase(role) || "Grooming".equalsIgnoreCase(role) || "Boarding".equalsIgnoreCase(role);

        if (container != null) {
            // Check if we need to inflate or re-inflate the correct navbar layout
            boolean hasProviderNav = activity.findViewById(R.id.navProviderHome) != null;
            boolean hasOwnerNav = activity.findViewById(R.id.navVet) != null;

            if ((isProvider && !hasProviderNav) || (!isProvider && !hasOwnerNav)) {
                container.removeAllViews();
                activity.getLayoutInflater().inflate(isProvider ? R.layout.layout_navigation_bar_provider : R.layout.layout_navigation_bar, container, true);
                container.setClickable(false);
                container.setFocusable(false);
            }
        }

        // ── Owner / Pet-owner tabs ───────────────────────────────
        setupItem(activity,
                R.id.navVet,         MainActivity.class,
                R.id.chipBgHome,     R.id.ivHome,     R.id.tvHome);

        setupItem(activity,
                R.id.navManage,      PetServicesActivity.class,
                R.id.chipBgSearch,   R.id.ivSearch,   R.id.tvSearch);

        // Centre FAB — no chip, handled separately (always gradient)
        setupFabItem(activity,
                R.id.navAddPet,      AddEditPet.class,
                R.id.ivAdd,          R.id.tvAdd);

        setupItem(activity,
                R.id.navCalendar,    Calendar.class,
                R.id.chipBgCalendar, R.id.ivCalendar, R.id.tvCalendar);

        setupItem(activity,
                R.id.navProfile,     Profile.class,
                R.id.chipBgProfile,  R.id.ivProfile,  R.id.tvProfile);

        // ── Provider / Vet tabs ──────────────────────────────────
        setupItem(activity,
                R.id.navProviderHome,     VetDashboardActivity.class,
                R.id.chipBgProHome,       R.id.ivProHome,     R.id.tvProHome);

        setupItem(activity,
                R.id.navProviderCalendar, Calendar.class,
                R.id.chipBgProCalendar,   R.id.ivProCalendar, R.id.tvProCalendar);

        // Provider centre FAB
        setupFabItem(activity,
                R.id.navProviderAdd,  null /* no destination, caller sets listener */,
                R.id.ivProAdd,        R.id.tvProAdd);

        setupItem(activity,
                R.id.navProviderPatients, ClientListActivity.class,
                R.id.chipBgProPatients,   R.id.ivProPatients, R.id.tvProPatients);

        setupItem(activity,
                R.id.navProviderProfile,  Profile.class,
                R.id.chipBgProProfile,    R.id.ivProProfile,  R.id.tvProProfile);

        // Hide the opposite side's views to be doubly sure (though usually handled by inflation)
        if (isProvider) {
            int[] userIds = {R.id.navVet, R.id.navManage, R.id.navCalendar, R.id.navProfile, R.id.navAddPet};
            for (int id : userIds) {
                View v = activity.findViewById(id);
                if (v != null) v.setVisibility(View.GONE);
            }
        } else {
            int[] providerIds = {R.id.navProviderHome, R.id.navProviderCalendar, R.id.navProviderPatients, R.id.navProviderProfile, R.id.navProviderAdd};
            for (int id : providerIds) {
                View v = activity.findViewById(id);
                if (v != null) v.setVisibility(View.GONE);
            }
        }
    }

    /**
     * Convenience method to refresh the navbar on lifecycle changes (e.g., after role switch).
     * Currently just delegates to setupNavbar, but kept for future extensibility.
     */
    public static void refresh(Activity activity) {
        setupNavbar(activity);
    }

    /* ─── regular nav item ────────────────────────────────────── */

    private static void setupItem(Activity activity,
                                  int layoutId, Class<?> targetClass,
                                  int chipBgId,
                                  int iconId,  int textId) {

        View layout = activity.findViewById(layoutId);
        if (layout == null) return;

        layout.setVisibility(View.VISIBLE);

        View     chipBg = activity.findViewById(chipBgId);
        ImageView icon  = activity.findViewById(iconId);
        TextView  text  = activity.findViewById(textId);

        boolean isActive = activity.getClass().equals(targetClass);

        applyActiveState(activity, chipBg, icon, text, isActive, /* animate= */ false);

        layout.setOnClickListener(v -> {
            if (isActive) return;

            Intent intent = new Intent(activity, targetClass);
            boolean isHome = targetClass.equals(MainActivity.class)
                          || targetClass.equals(VetDashboardActivity.class);
            if (isHome) {
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                              | Intent.FLAG_ACTIVITY_NEW_TASK);
            }

            activity.startActivity(intent);
            // Zero-flicker: suppress system transition animations
            activity.overridePendingTransition(0, 0);
        });
    }

    /* ─── centre FAB item (gradient circle, no chip) ─────────── */

    private static void setupFabItem(Activity activity,
                                     int layoutId, Class<?> targetClass,
                                     int iconId,   int textId) {

        View layout = activity.findViewById(layoutId);
        if (layout == null) return;

        layout.setVisibility(View.VISIBLE);

        // FAB icon is always white (set in XML); just wire click if target given
        if (targetClass == null) return;

        layout.setOnClickListener(v -> {
            // Tiny spring-pulse on tap for satisfying feel
            animatePulse(layout);

            Intent intent = new Intent(activity, targetClass);
            activity.startActivity(intent);
            activity.overridePendingTransition(0, 0);
        });
    }

    /* ─── active-state applicator ─────────────────────────────── */

    private static void applyActiveState(Activity activity,
                                         View chipBg, ImageView icon, TextView text,
                                         boolean isActive, boolean animate) {

        int activeColor   = ContextCompat.getColor(activity, R.color.md_theme_light_primary);
        int inactiveColor = ContextCompat.getColor(activity, R.color.dark_blue);

        float targetChipAlpha = isActive ? CHIP_ACTIVE   : CHIP_INACTIVE;
        float targetAlpha     = isActive ? ALPHA_ACTIVE  : ALPHA_INACTIVE;
        float targetScale     = isActive ? SCALE_ACTIVE  : SCALE_INACTIVE;
        int   targetColor     = isActive ? activeColor   : inactiveColor;

        if (animate && chipBg != null && icon != null && text != null) {
            // Animate chip fade
            ObjectAnimator chipFade = ObjectAnimator.ofFloat(chipBg, "alpha",
                    chipBg.getAlpha(), targetChipAlpha);
            chipFade.setDuration(ANIM_DURATION);
            chipFade.setInterpolator(new DecelerateInterpolator());

            // Animate icon scale + alpha
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(icon, "scaleX",
                    icon.getScaleX(), targetScale);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(icon, "scaleY",
                    icon.getScaleY(), targetScale);
            ObjectAnimator iconAlpha = ObjectAnimator.ofFloat(icon, "alpha",
                    icon.getAlpha(), targetAlpha);

            // Animate label alpha
            ObjectAnimator textAlpha = ObjectAnimator.ofFloat(text, "alpha",
                    text.getAlpha(), targetAlpha);

            AnimatorSet set = new AnimatorSet();
            set.playTogether(chipFade, scaleX, scaleY, iconAlpha, textAlpha);
            set.setDuration(ANIM_DURATION);
            set.setInterpolator(new DecelerateInterpolator());
            set.start();

        } else {
            // Instant apply (on first bind — no animation needed)
            if (chipBg != null) chipBg.setAlpha(targetChipAlpha);
            if (icon != null) {
                icon.setAlpha(targetAlpha);
                icon.setScaleX(targetScale);
                icon.setScaleY(targetScale);
            }
            if (text != null) text.setAlpha(targetAlpha);
        }

        // Color tint (always instant — tint doesn't distract)
        if (icon != null) icon.setColorFilter(targetColor);
        if (text != null) text.setTextColor(targetColor);
    }

    /* ─── micro-interaction: spring pulse on FAB tap ──────────── */

    private static void animatePulse(View view) {
        AnimatorSet pulse = new AnimatorSet();
        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.88f);
        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.88f);
        ObjectAnimator scaleUpX   = ObjectAnimator.ofFloat(view, "scaleX", 0.88f, 1f);
        ObjectAnimator scaleUpY   = ObjectAnimator.ofFloat(view, "scaleY", 0.88f, 1f);

        AnimatorSet down = new AnimatorSet();
        down.playTogether(scaleDownX, scaleDownY);
        down.setDuration(90);

        AnimatorSet up = new AnimatorSet();
        up.playTogether(scaleUpX, scaleUpY);
        up.setDuration(160);
        up.setInterpolator(new DecelerateInterpolator(1.5f));

        pulse.playSequentially(down, up);
        pulse.start();
    }
}