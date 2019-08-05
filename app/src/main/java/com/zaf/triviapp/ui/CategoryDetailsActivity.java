package com.zaf.triviapp.ui;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.zaf.triviapp.R;
import com.zaf.triviapp.models.Category;

public class CategoryDetailsActivity extends AppCompatActivity {

    public static final String SELECTED_CATEGORY = "selected_category";
    Toolbar toolbar;
    TextView categoryName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_details);

        toolbarOptions();

        Category selectedCategory = getIntent().getParcelableExtra(SELECTED_CATEGORY);

        categoryName = findViewById(R.id.selected_category_name);
        categoryName.setText(selectedCategory.getName());

    }

    private void toolbarOptions() {
        toolbar = findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.category_details_menu_items);

        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarTitle.setText(Html.fromHtml("<font color=#0031AA>Trivi</font><font color=#AD0000>App</font>"));

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if(menuItem.getItemId()==R.id.categories_menu_scores)
                    Toast.makeText(CategoryDetailsActivity.this, "Scores", Toast.LENGTH_SHORT).show();
                else if(menuItem.getItemId()== R.id.categories_menu_settings)
                    Toast.makeText(CategoryDetailsActivity.this, "Categories", Toast.LENGTH_SHORT).show();
                else{
                    if (FirebaseAuth.getInstance().getCurrentUser() == null){
                        Intent intent = new Intent(CategoryDetailsActivity.this, LoginAuth.class);
                        startActivity(intent);
                    }else{
                        AuthUI.getInstance()
                                .signOut(CategoryDetailsActivity.this)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        //showSignInOptions();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(CategoryDetailsActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                }

                return false;
            }
        });
    }
}
