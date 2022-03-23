package com.de.mucify.ui;

import android.os.Bundle;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.de.mucify.R;

public class ActivitySearch extends AppCompatActivity {

    private RecyclerView mRvSearchResult;
    private EditText mEditSearchQuery;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        mRvSearchResult = findViewById(R.id.rvSearchResult);
        mEditSearchQuery = findViewById(R.id.editSearchQuery);



    }
}
