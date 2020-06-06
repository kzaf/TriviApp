package com.zaf.triviapp.ui.fragments;

import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.zaf.triviapp.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AboutPageFragment extends Fragment {

    @BindView(R.id.mail_tv) TextView mailTextView;
    @BindView(R.id.about_text) TextView aboutTextView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_about_page, container, false);
        ButterKnife.bind(this, view);

        aboutTextView.setText(
                Html.fromHtml(getResources().getString(R.string.about_description),
                        Html.FROM_HTML_MODE_COMPACT));


        return view;
    }

}
