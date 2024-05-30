package com.aceburgundy.criminalintent;

import static com.aceburgundy.criminalintent.utilities.Helper.onPhone;
import static com.aceburgundy.criminalintent.utilities.Helper.onTablet;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class CrimeListFragment extends Fragment {
    private static final String SAVED_SUBTITLE_VISIBILITY_KEY = "subtitle";
    private static final int VIEW_TYPE_NORMAL = 0;
    private static final int VIEW_TYPE_POLICE = 1;
    private static final int VIEW_TYPE_EMPTY = 2;
    private LinearLayout noCrimesPlaceholderItem;
    private RecyclerView crimeRecyclerView;
    private boolean subtitleVisible;
    private int clickedItemPosition;
    private CrimeAdapter adapter;
    private Callbacks callbacks;

    /**
     * Required interface for hosting activities
     */
    public interface Callbacks {
        void onCrimeSelected(Crime crime);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        callbacks = (Callbacks) context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    private class CrimeHolder extends RecyclerView.ViewHolder {
        private final ImageView solvedImageView;
        private final TextView titleTextView;
        private final TextView dateTextView;
        private Button contactPoliceButton;
        private Crime mCrime;
        public CrimeHolder(View itemView, int viewType) {
            super(itemView);

            solvedImageView = itemView.findViewById(R.id.crime_solved);
            titleTextView = itemView.findViewById(R.id.crime_title);
            dateTextView = itemView.findViewById(R.id.crime_date);

            switch (viewType) {
                case VIEW_TYPE_NORMAL:
                    contactPoliceButton = null;
                    break;
                case VIEW_TYPE_POLICE:
                    contactPoliceButton = itemView.findViewById(R.id.contact_police_button);
                    break;
                default:
                    break;
            }

            if (viewType != VIEW_TYPE_EMPTY) {
                itemView.setOnClickListener(view -> {
                    clickedItemPosition = getAdapterPosition();
                    callbacks.onCrimeSelected(mCrime);
                });
            }

        }

        public void bind(@NonNull Crime crime) {
            mCrime = crime;

            if (contactPoliceButton != null && crime.isSolved()) {
                contactPoliceButton.setVisibility(View.GONE);
            }

            if (contactPoliceButton != null && !crime.isSolved()) {
                contactPoliceButton.setOnClickListener(buttonView -> {
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:911"));
                    requireActivity().startActivity(intent);
                });
            }

            titleTextView.setText(crime.getTitle());
            dateTextView.setText(crime.getFormattedDate());
            solvedImageView.setVisibility(crime.isSolved() ? View.VISIBLE : View.GONE);
            itemView.setContentDescription(getShortenedCrimeDescription());
        }

        @NonNull
        private String getShortenedCrimeDescription() {
            boolean hasTitle = !titleTextView.getText().toString().trim().isEmpty();
            String titleIntroduction = getString(R.string.crime_no_title);
            if (hasTitle) titleIntroduction = getString(R.string.crime_introduction) + titleTextView.getText();
            String dateOccurred = getString(R.string.crime_date_introduction) + dateTextView.getText();
            String requiresIntervention = mCrime.requiresIntervention() ? getString(R.string.crime_requires_police_introduction) : "";
            String status = mCrime.isSolved() ? getString(R.string.crime_solved_introduction) : getString(R.string.crime_not_solved_introduction);

            return String.format("%s %s %s %s %s",
                titleIntroduction,
                dateOccurred,
                mCrime.getFormattedTime(),
                requiresIntervention,
                status
            );
        }
    }

    public int getCrimePositionById(UUID crimeId) {
        for (int position = 0; position < adapter.crimes.size(); position++) {
            UUID currentCrimeId = adapter.crimes.get(position).getId();
            if (crimeId.equals(currentCrimeId)) return position;
        }
        return -1;
    }

    public void navigateAndToggleClosestCrime(int position) {
        int numberOfCrimes = adapter.getItemCount();

        if (numberOfCrimes <= 0) return;

        int nextClosestPosition = -1;
        int nextPosition = position + 1;
        int previousPosition = position - 1;

        if (nextPosition < numberOfCrimes) {
            nextClosestPosition = nextPosition;
        } else if (previousPosition >= 0) {
            nextClosestPosition = previousPosition;
        }

        if (nextClosestPosition > -1) clickCrimeListFragmentItem(previousPosition);
    }

    public void removeCrimeAndNavigateToClosestCrime(Crime crime) {
        int position = getCrimePositionById(crime.getId());
        if (position >= 0) {
            adapter.onItemDismiss(position);
        } else {
            Log.e("No Crime position found", "received -1");
        }
    }

    private void clickCrimeListFragmentItem(int position) {
        /*
         * Problem:
         *   The original code "callbacks.onCrimeUpdated(Crime crime);" faced an issue where the
         *   CrimeFragment updates didn't immediately reflect in the corresponding list item fragment
         *   when creating a new crime. The list item would update only after clicking the item,
         *   excluding cases involving checkboxes.
         *
         * Solution:
         *   Introducing a delay of 0 milliseconds using postDelayed after notifyItemInserted ensures
         *   that the search for the ViewHolder executes after the RecyclerView has handled the layout
         *   update. Simulating a click on the newly added item immediately after ensures that the
         *   CrimeFragment updates are reflected in the list item without requiring a manual click.
         *
         * Result:
         *   Updates to the CrimeFragment now seamlessly propagate to its respective list item without
         *   the need for manual clicks, providing a smoother user experience.
         * */
        Looper looper = Objects.requireNonNull(Looper.myLooper());
        new Handler(looper).postDelayed(() -> {
            RecyclerView.ViewHolder newCrime = crimeRecyclerView.findViewHolderForAdapterPosition(position);
            if (newCrime != null) {
                View newCrimeItemView = newCrime.itemView;
                newCrimeItemView.performClick();
            }
        }, 0);
    }

    public void updateCrimeListFragmentUI(Crime crime) {
        int position = adapter.crimes.indexOf(crime);
        adapter.crimes.remove(crime);
        adapter.notifyItemRemoved(position);
        updateUI();
    }

    private class CrimeAdapter extends RecyclerView.Adapter<CrimeHolder> {
        private List<Crime> crimes;

        public CrimeAdapter(List<Crime> crimes) {
            this.crimes = crimes;
        }

        @NonNull
        @Override
        public CrimeHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            View view;

            switch (viewType) {
                case VIEW_TYPE_NORMAL:
                    view = layoutInflater.inflate(R.layout.list_item_crime, parent, false);
                    break;
                case VIEW_TYPE_POLICE:
                    view = layoutInflater.inflate(R.layout.list_item_crime_with_intervention, parent, false);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid view type");
            }

            return new CrimeHolder(view, viewType);
        }

        @Override
        public int getItemViewType(int position) {
            if (crimes.isEmpty()) return VIEW_TYPE_EMPTY;
            boolean requiresPoliceIntervention = crimes.get(position).requiresIntervention();
            return requiresPoliceIntervention ? VIEW_TYPE_POLICE : VIEW_TYPE_NORMAL;
        }

        @Override
        public void onBindViewHolder(@NonNull CrimeHolder holder, int position) {
            Crime currentCrime = crimes.get(position);
            holder.bind(currentCrime);
        }

        public void onItemDismiss(int position) {
            Crime crime = crimes.get(position);
            CrimeLab.get(getActivity()).deleteCrime(crime);

            if (onTablet(requireActivity())) {
                clearCrimeFragmentLayout(crime);
                navigateAndToggleClosestCrime(position);
            }

            updateCrimeListFragmentUI(crime);
        }

        public void setCrimes(List<Crime> crimes) {
            this.crimes = crimes;
        }

        @Override
        public int getItemCount() {
            return crimes.size();
        }
    }

    public void clearCrimeFragmentLayout(@NonNull Crime crime) {
        FragmentManager manager = requireActivity().getSupportFragmentManager();
        Fragment crimeFragmentLayout = manager.findFragmentById(R.id.detail_fragment_container);

        // gets the title of the current crime title inside the crime fragment view
        assert crimeFragmentLayout != null;
        View fragmentView = crimeFragmentLayout.getView();

        assert fragmentView != null;
        TextView crimeTitleTextView = fragmentView.findViewById(R.id.crime_title);

        String crimeTitle = crimeTitleTextView.getText().toString();

        // clears the crime fragment view if both crime.title and crimeTitle in view are the same
        if (Objects.equals(crimeTitle, crime.getTitle())) {
            manager.beginTransaction()
                    .remove(Objects.requireNonNull(crimeFragmentLayout))
                    .commit();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Button newCrimeButton;

        if (savedInstanceState != null) subtitleVisible = savedInstanceState.getBoolean(SAVED_SUBTITLE_VISIBILITY_KEY);

        View view = inflater.inflate(R.layout.fragment_crime_list, container, false);
        noCrimesPlaceholderItem = view.findViewById(R.id.no_crime_placeholder_item);

        crimeRecyclerView = view.findViewById(R.id.crime_list_recycler_view);
        crimeRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        newCrimeButton = view.findViewById(R.id.new_crime_button);
        newCrimeButton.setOnClickListener(newCrimeView -> startNewCrime());

        updateUI();

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,@NonNull RecyclerView.ViewHolder target) {
                return false;
            }
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                adapter.onItemDismiss(viewHolder.getAdapterPosition());
            }
        });

        itemTouchHelper.attachToRecyclerView(crimeRecyclerView);

        // click the first crime on load if onTablet
        if (onTablet(requireActivity()) && adapter.getItemCount() > 0) {
            clickCrimeListFragmentItem(0);
        }

        return view;
    }

    void updateUI() {
        CrimeLab crimeLab = CrimeLab.get(requireActivity());
        List<Crime> crimes = crimeLab.getCrimes();

        if (noCrimesPlaceholderItem != null) {
            noCrimesPlaceholderItem.setVisibility(crimes.isEmpty() ? View.VISIBLE : View.GONE);
        }

        if (adapter == null) {
            adapter = new CrimeAdapter(crimes);
            if (crimeRecyclerView != null) {
                crimeRecyclerView.setAdapter(adapter);
            }
        } else {
            adapter.setCrimes(crimes);
            adapter.notifyItemChanged(clickedItemPosition);
        }

        updateSubtitle();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SAVED_SUBTITLE_VISIBILITY_KEY, subtitleVisible);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callbacks = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime_list, menu);

        MenuItem subtitleItem = menu.findItem(R.id.show_subtitle);
        subtitleItem.setTitle(subtitleVisible ? R.string.hide_subtitle : R.string.show_subtitle);
    }

    private void startNewCrime() {
        Crime crime = new Crime();
        CrimeLab crimeLab = CrimeLab.get(getActivity());
        crimeLab.addCrime(crime);

        int latestCrimePosition = crimeLab.getCrimes().size() - 1;
        adapter.notifyItemInserted(latestCrimePosition);

        if (onPhone(requireActivity())) {
            Intent intent = CrimePagerActivity.newIntent(getActivity(), crime.getId());
            startActivity(intent);
            return;
        }

        startNewCrimeOnTablets(crime);
        updateUI();
    }

    private void startNewCrimeOnTablets(Crime crime) {
        adapter.crimes.add(crime);
        int newPosition = adapter.crimes.indexOf(crime);
        adapter.notifyItemInserted(newPosition);
        clickCrimeListFragmentItem(newPosition);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int newCrimeId = R.id.new_crime;
        int showSubtitleId = R.id.show_subtitle;

        if (item.getItemId() == newCrimeId) {
            startNewCrime();
            return true;
        }

        if (item.getItemId() == showSubtitleId) {
            subtitleVisible = !subtitleVisible;
            requireActivity().invalidateOptionsMenu();
            updateSubtitle();
            return true;
        }

        return super.onOptionsItemSelected(item);

    }

    private void updateSubtitle() {
        CrimeLab crimeLab = CrimeLab.get(getActivity());
        int crimeCount = crimeLab.getCrimes().size();
        String subtitle = getResources().getQuantityString(R.plurals.subtitle_plural, crimeCount, crimeCount);

        if (!subtitleVisible) subtitle = null;

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        assert activity != null;

        androidx.appcompat.app.ActionBar actionBar = activity.getSupportActionBar();

        if (actionBar != null) {
            actionBar.setSubtitle(subtitle);
        }

    }


}
