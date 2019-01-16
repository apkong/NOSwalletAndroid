package co.nos.noswallet.ui.settings.addressBook;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import co.nos.noswallet.R;
import co.nos.noswallet.network.websockets.currencyFormatter.CryptoCurrencyFormatter;
import co.nos.noswallet.persistance.currency.CryptoCurrency;

public class AddressesAdapter extends RecyclerView.Adapter<AddressesAdapter.ViewHolder> {

    public interface Listener {
        void onClicked(AddressBookEntry entry);
    }

    private final CryptoCurrencyFormatter currencyFormatter;

    @Nullable
    public Listener listener = null;

    @NonNull
    public CryptoCurrency cryptoCurrencyApplied = CryptoCurrency.NOLLAR;

    public List<AddressBookEntry> items = new ArrayList<>();

    @Inject
    public AddressesAdapter(CryptoCurrencyFormatter formatter) {
        this.currencyFormatter = formatter;
    }

    public void refreshCurrency(CryptoCurrency currency) {
        this.cryptoCurrencyApplied = currency;
        notifyDataSetChanged();
    }

    public void refresh(List<AddressBookEntry> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_addressbook_entry, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        AddressBookEntry entry = items.get(position);

        holder.bind(entry, currencyFormatter, cryptoCurrencyApplied);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onClicked(entry);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView item_name, item_account;

        ViewHolder(View itemView) {
            super(itemView);
            item_name = itemView.findViewById(R.id.item_name);
            item_account = itemView.findViewById(R.id.item_account);
        }

        public void bind(AddressBookEntry entry,
                         CryptoCurrencyFormatter currencyFormatter,
                         CryptoCurrency currencyApplied) {
            item_name.setText(entry.name);

            String preferredAddress = entry.addressesMap.get(currencyApplied);
            if (preferredAddress == null) {
                preferredAddress = firstAddress(entry.addressesMap);
            }

            if (preferredAddress != null && preferredAddress.length() == 64) {
                item_account.setText(currencyFormatter.createSpannable(preferredAddress));
            }
        }

        @Nullable
        private static String firstAddress(Map<CryptoCurrency, String> map) {
            for (CryptoCurrency cryptoCurrency : map.keySet()) {
                String value = map.get(cryptoCurrency);
                if (value != null) {
                    return value;
                }
            }
            return null;
        }
    }
}
