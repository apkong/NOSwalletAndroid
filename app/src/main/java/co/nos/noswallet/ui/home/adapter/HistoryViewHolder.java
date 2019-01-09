package co.nos.noswallet.ui.home.adapter;

import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.TextAppearanceSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import co.nos.noswallet.R;
import co.nos.noswallet.network.nosModel.AccountHistory;
import co.nos.noswallet.network.websockets.currencyFormatter.CryptoCurrencyFormatter;

public class HistoryViewHolder extends RecyclerView.ViewHolder {

    ImageView icon;

    TextView balance, account;

    public HistoryViewHolder(View itemView) {
        super(itemView);
        icon = itemView.findViewById(R.id.item_send_receive_icon);
        balance = itemView.findViewById(R.id.item_history_balance);
        account = itemView.findViewById(R.id.item_history_account);
    }

    public void bind(AccountHistory accountHistory, CryptoCurrencyFormatter currencyFormatter) {
        icon.setImageResource(accountHistory.isSend() ? R.drawable.ic_send : R.drawable.ic_receive);
        balance.setText(currencyFormatter.rawtoUi(accountHistory.amount));
        account.setText(currencyFormatter.createSpannable(accountHistory.account));
    }

}
