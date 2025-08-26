package com.Mbuntu.MbuntuMobile.ui.main;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.Mbuntu.MbuntuMobile.ui.conversations.ConversationsFragment;
import com.Mbuntu.MbuntuMobile.ui.placeholder.PlaceholderFragment; // On va le créer

public class SectionsPagerAdapter extends FragmentStateAdapter {

    public SectionsPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 1:
                return PlaceholderFragment.newInstance("Écran Statut"); // Fragment temporaire
            case 2:
                return PlaceholderFragment.newInstance("Écran Tara"); // Fragment temporaire
            case 3:
                return PlaceholderFragment.newInstance("Écran Appels"); // Fragment temporaire
            case 0:
            default:
                return new ConversationsFragment(); // Notre vrai fragment
        }
    }

    @Override
    public int getItemCount() {
        return 3; // On gère maintenant 3 pages
    }
}