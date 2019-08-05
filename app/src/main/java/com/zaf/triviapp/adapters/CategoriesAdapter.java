package com.zaf.triviapp.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zaf.triviapp.R;
import com.zaf.triviapp.models.Category;

import java.util.List;

public class CategoriesAdapter extends RecyclerView.Adapter<CategoriesAdapter.CategoriesViewHolder>{

    final private CategoriesAdapterListItemClickListener mOnClickListener;
    private List<Category> categoriesList;

    public CategoriesAdapter(CategoriesAdapterListItemClickListener mOnClickListener, List<Category> categoriesList) {
        this.mOnClickListener = mOnClickListener;
        this.categoriesList = categoriesList;
    }

    @NonNull
    @Override
    public CategoriesViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.categories_item, viewGroup, false);
        return new CategoriesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoriesViewHolder categoriesViewHolder, int position) {
        categoriesViewHolder.categoryName.setText(categoriesList.get(position).getName());
    }

    @Override
    public int getItemCount() {
        if (null == categoriesList) return 0;
        return categoriesList.size();
    }

    public interface CategoriesAdapterListItemClickListener {
        void onListItemClick(int item);
    }

    public class CategoriesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView categoryName;

        private CategoriesViewHolder(View itemView) {
            super(itemView);

            categoryName = itemView.findViewById(R.id.category_name);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            mOnClickListener.onListItemClick(adapterPosition);
        }
    }
}
