package com.zaf.triviapp.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zaf.triviapp.R;
import com.zaf.triviapp.database.tables.Scores;
import com.zaf.triviapp.models.Category;

import java.util.List;

public class CategoriesProfileAdapter extends RecyclerView.Adapter<CategoriesProfileAdapter.CategoriesProfileViewHolder> {

    final private CategoriesProfileAdapter.CategoriesProfileAdapterListItemClickListener mOnClickListener;
    private List<Scores> scoresList;

    public CategoriesProfileAdapter(CategoriesProfileAdapter.CategoriesProfileAdapterListItemClickListener mOnClickListener, List<Scores> scoresList) {
        this.mOnClickListener = mOnClickListener;
        this.scoresList = scoresList;
    }

    @NonNull
    @Override
    public CategoriesProfileAdapter.CategoriesProfileViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.categories_profile_item, viewGroup, false);
        return new CategoriesProfileAdapter.CategoriesProfileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoriesProfileAdapter.CategoriesProfileViewHolder categoriesProfileViewHolder, int position) {
        categoriesProfileViewHolder.categoryName.setText(scoresList.get(position).getCategoryName());
        categoriesProfileViewHolder.categoryScore.setText(scoresList.get(position).getCategoryScore() * 10 + "%");
    }

    @Override
    public int getItemCount() {
        if (null == scoresList) return 0;
        return scoresList.size();
    }

    public interface CategoriesProfileAdapterListItemClickListener {
        void onListItemClick(int item);
    }

    public class CategoriesProfileViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView categoryName;
        TextView categoryScore;

        private CategoriesProfileViewHolder(View itemView) {
            super(itemView);

            categoryName = itemView.findViewById(R.id.category_profile_name);
            categoryScore = itemView.findViewById(R.id.category_profile_score);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            mOnClickListener.onListItemClick(adapterPosition);
        }
    }
}
