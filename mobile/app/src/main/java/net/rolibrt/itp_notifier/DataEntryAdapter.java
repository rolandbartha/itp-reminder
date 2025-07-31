package net.rolibrt.itp_notifier;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DataEntryAdapter extends RecyclerView.Adapter<DataEntryAdapter.ViewHolder> {
    private final List<DataEntry> dataEntries;

    public DataEntryAdapter(List<DataEntry> dataEntries) {
        this.dataEntries = dataEntries;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView idText, tagText, dateText, expiryText, phoneText;
        CheckBox checkBox;

        public ViewHolder(View view) {
            super(view);
            idText = view.findViewById(R.id.idText);
            phoneText = view.findViewById(R.id.phoneText);
            tagText = view.findViewById(R.id.tagText);
            dateText = view.findViewById(R.id.dateText);
            expiryText = view.findViewById(R.id.expiryText);
            checkBox = view.findViewById(R.id.checkBox);
        }

        public void bind(DataEntry entry) {
            idText.setText(String.format("Id: %s", entry.getId()));
            phoneText.setText(String.format("Telefon: %s", entry.getPhone()));
            tagText.setText(entry.getTag());
            dateText.setText(String.format("Data verificării: %s", Utils.formatter.format(entry.getDate())));
            expiryText.setText(String.format("Data expirării: %s", Utils.formatter.format(entry.getExpiry())));
            if (entry.isExpired())
                expiryText.setTextColor(Color.parseColor("#FF5722"));
            checkBox.setChecked(entry.isSelected());
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> entry.setSelected(isChecked));
        }
    }

    @NonNull
    @Override
    public DataEntryAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_dataentry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(dataEntries.get(position));
    }

    @Override
    public int getItemCount() {
        return dataEntries.size();
    }
}
