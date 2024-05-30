package com.aceburgundy.criminalintent;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.aceburgundy.criminalintent.crimefragmentview.CrimeFragment;
import com.aceburgundy.criminalintent.utilities.Helper;

import java.util.List;
import java.util.UUID;

public class CrimePagerActivity extends AppCompatActivity implements CrimeFragment.Callbacks {
    private static final String EXTRA_CRIME_ID = "com.enfranchiser.android.criminal-intent.crime_id";
    private List<Crime> crimeList;

    @Override
    public void onCrimeUpdated(Crime crime) {
        // Document why this method is empty
    }

    public static Intent newIntent(Context packageContext, UUID crimeId) {
        Intent intent = new Intent(packageContext, CrimePagerActivity.class);
        intent.putExtra(EXTRA_CRIME_ID, crimeId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        ViewPager2 viewPager;
        Button jumpToFirstButton;
        Button jumpToLastButton;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crime_pager);

        final UUID crimeId = (UUID) Serializable.getSerializable(getIntent(), EXTRA_CRIME_ID);

        viewPager = findViewById(R.id.crime_view_pager);
        crimeList = CrimeLab.get(this).getCrimes();

        FragmentManager fragmentManager = getSupportFragmentManager();

        viewPager.setAdapter(new FragmentStateAdapter(fragmentManager, getLifecycle()) {

            @Override
            public int getItemCount() {
                return crimeList.size();
            }

            @NonNull
            @Override
            public Fragment createFragment(int position) {
                Crime crime = crimeList.get(position);
                return CrimeFragment.newInstance(crime.getId());
            }

        });

        for (int i = 0; i < crimeList.size(); i++) {
            if (crimeList.get(i).getId().equals(crimeId)) {
                viewPager.setCurrentItem(i);
                break;
            }
        }

        jumpToFirstButton = findViewById(R.id.jump_to_first_button);
        jumpToLastButton = findViewById(R.id.jump_to_last_button);

        jumpToFirstButton.setOnClickListener(view -> viewPager.setCurrentItem(0));
        jumpToLastButton.setOnClickListener(view -> viewPager.setCurrentItem(crimeList.size() - 1));

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                if (crimeList.size() == 1) {
                    jumpToFirstButton.setVisibility(View.GONE);
                    jumpToLastButton.setVisibility(View.GONE);
                }

                // if on first page
                if (position == 0) {
                    Helper.disableElement(jumpToFirstButton);
                    Helper.enableElement(jumpToLastButton);
                    return;
                }

                // if on last page
                if (position == crimeList.size() - 1) {
                    Helper.enableElement(jumpToFirstButton);
                    Helper.disableElement(jumpToLastButton);
                    return;
                }

                // otherwise
                Helper.enableElement(jumpToFirstButton);
                Helper.enableElement(jumpToLastButton);

            }
        });

    }

}