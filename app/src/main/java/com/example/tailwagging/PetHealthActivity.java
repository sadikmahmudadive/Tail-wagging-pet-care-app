package com.example.tailwagging;

import android.os.Bundle;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class PetHealthActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private ImageButton btnBack;
    private Pet selectedPet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pet_health);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.petHealthRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        selectedPet = getIntent().getParcelableExtra("SELECTED_PET");

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        HealthPagerAdapter adapter = new HealthPagerAdapter(this, selectedPet);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == 0) {
                tab.setText(getString(R.string.wellness_tab));
            } else {
                tab.setText(getString(R.string.medical_records_tab));
            }
        }).attach();

        NavbarHelper.setupNavbar(this);
    }

    private static class HealthPagerAdapter extends FragmentStateAdapter {
        private final Pet pet;

        public HealthPagerAdapter(@NonNull FragmentActivity fragmentActivity, Pet pet) {
            super(fragmentActivity);
            this.pet = pet;
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            Fragment fragment;
            if (position == 0) {
                fragment = new WellnessFragment();
            } else {
                fragment = new MedicalRecordsFragment();
            }

            if (pet != null) {
                Bundle args = new Bundle();
                args.putParcelable("SELECTED_PET", pet);
                fragment.setArguments(args);
            }
            return fragment;
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }
}