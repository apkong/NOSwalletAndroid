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

public class HistoryViewHolder extends RecyclerView.ViewHolder {

    ImageView icon;

    TextView balance, account;

    public HistoryViewHolder(View itemView) {
        super(itemView);
        icon = itemView.findViewById(R.id.item_send_receive_icon);
        balance = itemView.findViewById(R.id.item_history_balance);
        account = itemView.findViewById(R.id.item_history_account);
    }

    public void bind(AccountHistory accountHistory) {
        icon.setImageResource(accountHistory.isSend() ? R.drawable.ic_send : R.drawable.ic_receive);
        balance.setText(readableAmount(accountHistory.amount));
        account.setText(createSpannable(accountHistory.account));
    }

    private String readableAmount(String amount) {
        if (amount.length() > 1) {
            String part1 = amount.substring(0, 1);
            String part2 = amount.substring(1, Math.min(amount.length(), 5));

            return "~" + part1 + "." + part2 + " NOS";
        } else return amount;
    }

    private SpannableString createSpannable(String amount) {
        String bluePath = amount.substring(0, 8);
        int len = amount.length();
        String orangePath = amount.substring(len - 6, len);

        String totalPath = bluePath + orangePath;

        SpannableString spannableString = new SpannableString(totalPath);

        ColorStateList blue = new ColorStateList(new int[][]{new int[]{}}, new int[]{0xff4a90e2});
        ColorStateList orange = new ColorStateList(new int[][]{new int[]{}}, new int[]{0xffea6232});

        TextAppearanceSpan blueSpan = colorSpan(blue);
        TextAppearanceSpan orangeSpan = colorSpan(orange);

        spannableString.setSpan(blueSpan, 0, 8, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(orangeSpan, 8, totalPath.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableString;
    }

    TextAppearanceSpan colorSpan(ColorStateList color) {
        TextAppearanceSpan span = new TextAppearanceSpan(null, Typeface.NORMAL, -1, color, null);
        return span;
    }

}
