package co.nos.noswallet.ui.home.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import co.nos.noswallet.R;
import co.nos.noswallet.network.nosModel.AccountHistory;
import co.nos.noswallet.network.websockets.currencyFormatter.CryptoCurrencyFormatter;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryViewHolder> {

    public List<AccountHistory> history = new ArrayList<>();

    private final CryptoCurrencyFormatter currencyFormatter;

    public void refresh(List<AccountHistory> history) {
        this.history = history;
        notifyDataSetChanged();
    }

    @Inject
    public HistoryAdapter(CryptoCurrencyFormatter currencyFormatter) {
        this.currencyFormatter = currencyFormatter;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        holder.bind(history.get(position), currencyFormatter);
    }

    @Override
    public int getItemCount() {
        return history.size();
    }
}
