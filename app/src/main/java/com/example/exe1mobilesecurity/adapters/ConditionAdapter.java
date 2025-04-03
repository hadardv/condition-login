package com.example.exe1mobilesecurity.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.exe1mobilesecurity.R;
import com.example.exe1mobilesecurity.models.Condition;
import java.util.List;


public class ConditionAdapter extends RecyclerView.Adapter<ConditionAdapter.ViewHolder>  {

    private final List<Condition> conditions;

    public ConditionAdapter(List<Condition> conditions) {
        this.conditions = conditions;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        TextView conditionText;

        public ViewHolder(View view) {
            super(view);
            checkBox = view.findViewById(R.id.checkBox);
            conditionText = view.findViewById(R.id.conditionText);
        }
    }


    @NonNull
    public ConditionAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.condition_item, parent, false);
        return new ViewHolder(view);
    }

    public void onBindViewHolder(ViewHolder holder, int position) {
        Condition condition = conditions.get(position);
        holder.conditionText.setText(condition.getDescription());
        holder.checkBox.setChecked(condition.isMet());
    }

    public int getItemCount() {
        return conditions.size();
    }

    // This method updates the state of a condition
    public void updateCondition(int index, boolean isMet) {
        conditions.get(index).setIsMet(isMet);
        notifyItemChanged(index);
    }
}
