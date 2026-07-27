package com.kite.mnemoai.ui.main;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.viewmodel.ViewModelInitializer;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationBarView;
import com.kite.mnemoai.R;
import com.kite.mnemoai.databinding.ActivityMainBinding;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private MaterialToolbar toolbar;
    private NavigationBarView navigationBarView;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        MainViewModel viewmodel = new ViewModelProvider(this).get(MainViewModel.class);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(
                binding.toolbar, (v, insets) -> {
                    Insets bars = insets.getInsets(WindowInsetsCompat.Type.statusBars());
                    v.setPadding(0, bars.top, 0, 0
                    );
                    return insets;
                });
        ViewCompat.setOnApplyWindowInsetsListener(
                binding.bottomNavigation, (v, insets) -> {
                    Insets bars = insets.getInsets(WindowInsetsCompat.Type.navigationBars());
                    v.setPadding(bars.left, 0, bars.right, -80
                    );
                    return insets;
                });
        toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navController.navigateUp();
            }
        });
        viewmodel.getUiState().observe(this, new Observer<MainUIState>() {
            @Override
            public void onChanged(MainUIState mainUIState) {
                if(mainUIState == null) return;
                Objects.requireNonNull(getSupportActionBar()).setTitle(mainUIState.getTitle());
                getSupportActionBar().setDisplayHomeAsUpEnabled(mainUIState.isShowNavIcon());
            }
        });

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
                binding.bottomNavigation.setVisibility(View.VISIBLE);
            }else {
                binding.bottomNavigation.setVisibility(View.GONE);
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
}