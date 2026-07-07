package com.kite.mnemoai;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import androidx.core.splashscreen.SplashScreen;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationBarView;
import com.kite.mnemoai.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private MaterialToolbar toolbar;
    private NavigationBarView navigationBarView;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        EdgeToEdge.enable(this);
        setContentView(view);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        toolbar = binding.toolbar.toolbar;
        setSupportActionBar(toolbar);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.navHostFragment);
        assert navHostFragment != null;
        navController = navHostFragment.getNavController();

        navigationBarView = binding.appBottomNavigationView;
        NavigationUI.setupWithNavController(navigationBarView, navController);
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if(destination.getId() == R.id.reciteWordFragment ||
                    destination.getId() == R.id.vocabularyFragment ||
                    destination.getId() == R.id.statisticFragment ||
                    destination.getId() == R.id.mineFragment){
                navigationBarView.setVisibility(View.VISIBLE);
            }else {
                navigationBarView.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public boolean onNavigateUp() {
        boolean isSuccess = navController.navigateUp();
        if(isSuccess) Log.d("Navigationaaa", "返回成功: " + true);
        else Log.d("Navigationaaa", "返回失败: " + false);
        return isSuccess;
    }

    public void showBackIV(){
        binding.toolbar.backIV.setVisibility(View.VISIBLE);
    }
    public void hideBackIV(){
        binding.toolbar.backIV.setVisibility(View.GONE);
    }

    public void setTitleText(int titleResource){
        binding.toolbar.titleTV.setText(titleResource);
    }

    public void setTitleText(String title){
        binding.toolbar.titleTV.setText(title);
    }
}