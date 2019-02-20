package de.wackernagel.dkq.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import de.wackernagel.dkq.R;
import de.wackernagel.dkq.viewmodels.QuizzerRole;

public class QuizzersViewPagerFragment extends Fragment {

    static QuizzersViewPagerFragment newInstance() {
        final QuizzersViewPagerFragment fragment = new QuizzersViewPagerFragment();
        fragment.setArguments( new Bundle(0) );
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_quizzers_pager, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final ViewPager pager = view.findViewById(R.id.viewpager);
        pager.setAdapter( new QuizzersFragmentPagerAdapter( getContext(), getChildFragmentManager() ) );
        final TabLayout tabsLayout = view.findViewById(R.id.tabs);
        tabsLayout.setupWithViewPager(pager);
    }

    static class QuizzersFragmentPagerAdapter extends FragmentPagerAdapter {
        private final Context context;

        QuizzersFragmentPagerAdapter( final Context context, @NonNull final FragmentManager fm ) {
            super(fm);
            this.context = context;
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return QuizzersListFragment.newInstance(QuizzerRole.values()[position]);
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            switch ( QuizzerRole.values()[ position ] ) {
                case WINNER:
                    return context.getString( R.string.quizzers_tab_winners);

                case QUIZMASTER:
                default:
                    return context.getString( R.string.quizzers_tab_quiz_masters);
            }
        }
    }
}
