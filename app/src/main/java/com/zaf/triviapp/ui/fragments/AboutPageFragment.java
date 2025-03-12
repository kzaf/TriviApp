package com.zaf.triviapp.ui.fragments;

import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.zaf.triviapp.R;
import com.zaf.triviapp.databinding.ActivityAboutPageBinding;
import com.zaf.triviapp.ui.MainActivity;

public class AboutPageFragment extends Fragment {

    private MainActivity mainActivity;
    private ActivityAboutPageBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        binding = ActivityAboutPageBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        mainActivity = ((MainActivity)getActivity());
        if (mainActivity != null) {
            mainActivity.setBackButtonVisibility(true);
        }

        binding.aboutTv.setText(
                Html.fromHtml(mainActivity.getResources().getString(R.string.about_description),
                        Html.FROM_HTML_MODE_COMPACT));


        return view;
    }

}
