package com.Mbuntu.MbuntuMobile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.Mbuntu.MbuntuMobile.ui.main.SectionsPagerAdapter;
import com.Mbuntu.MbuntuMobile.utils.TokenManager;
import com.Mbuntu.MbuntuMobile.websocket.WebSocketManager;
import com.google.android.material.bottomnavigation.BottomNavigationView; // Nouvel import
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {

    private TokenManager tokenManager;
    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private NavigationView navigationView;
    private ViewPager2 viewPager;
    private BottomNavigationView bottomNavigationView; // Remplacement de TabLayout

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tokenManager = new TokenManager(this);

        if (tokenManager.getToken() == null) {
            navigateToLogin();
            return;
        }

        setContentView(R.layout.activity_main);

        // Liaison des vues
        toolbar = findViewById(R.id.toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        viewPager = findViewById(R.id.view_pager);
        bottomNavigationView = findViewById(R.id.bottom_navigation); // Nouvelle liaison

        // Configuration
        setSupportActionBar(toolbar);
        setupDrawer();
        setupViewPagerAndBottomNav(); // On combine les deux pour une meilleure synchro
        connectWebSocket();
    }

    private void setupDrawer() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
            } else if (itemId == R.id.nav_settings) {
                Toast.makeText(this, "Écran des paramètres à implémenter", Toast.LENGTH_SHORT).show();
            } else if (itemId == R.id.nav_logout) {
                tokenManager.clear();
                WebSocketManager.getInstance().disconnect();
                navigateToLogin();
            }
            drawerLayout.closeDrawers();
            return true;
        });
    }

    private void setupViewPagerAndBottomNav() {
        // 1. Configurer le ViewPager avec son adapter
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this);
        viewPager.setAdapter(sectionsPagerAdapter);
        viewPager.setUserInputEnabled(false); // Optionnel: désactive le swipe entre les pages

        // 2. Lier le ViewPager à la BottomNavigationView (le swipe met à jour le bouton sélectionné)
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                switch (position) {
                    case 0:
                        bottomNavigationView.setSelectedItemId(R.id.nav_bottom_chats);
                        break;
                    case 1:
                        bottomNavigationView.setSelectedItemId(R.id.nav_bottom_status);
                        break;
                    case 2:
                        bottomNavigationView.setSelectedItemId(R.id.nav_bottom_tara);
                        break;
                    case 3:
                        bottomNavigationView.setSelectedItemId(R.id.nav_bottom_calls);
                        break;
                }
            }
        });

        // 3. Lier la BottomNavigationView au ViewPager (le clic sur un bouton change la page)
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_bottom_chats) {
                viewPager.setCurrentItem(0, false); // false pour une transition instantanée
                return true;
            } else if (itemId == R.id.nav_bottom_status) {
                viewPager.setCurrentItem(1, false);
                return true;
            } else if (itemId == R.id.nav_bottom_tara) {
                viewPager.setCurrentItem(2, false);
                return true;
            } else if (itemId == R.id.nav_bottom_calls) {
                viewPager.setCurrentItem(3, false);
                return true;}
            return false;
        });
    }

    private void connectWebSocket() {
        Long userId = tokenManager.getUserId();
        if (userId != -1L) {
            WebSocketManager.getInstance().connect(this,userId);
        } else {
            Toast.makeText(this, "Erreur critique: ID utilisateur introuvable.", Toast.LENGTH_LONG).show();
            tokenManager.clear();
            navigateToLogin();
        }
    }

    private void navigateToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}