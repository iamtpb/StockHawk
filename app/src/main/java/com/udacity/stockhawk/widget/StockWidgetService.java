package com.udacity.stockhawk.widget;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.util.TypedValue;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;


public class StockWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ListRemoteViewFactory();
    }

    public class ListRemoteViewFactory implements RemoteViewsService.RemoteViewsFactory {

        private Cursor data = null;

        @Override
        public void onCreate() {
            //No action needed
        }

        @Override
        public void onDestroy() {
            if (data != null) {
                data.close();
                data = null;
            }

        }

        @Override
        public void onDataSetChanged() {
            if (data != null) data.close();

            final long identityToken = Binder.clearCallingIdentity();
            data = getContentResolver().query(Contract.Quote.URI,
                    Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                    null,
                    null,
                    Contract.Quote.COLUMN_SYMBOL);
            Binder.restoreCallingIdentity(identityToken);
        }

        @Override
        public int getCount() {
            return data == null ? 0 : data.getCount();
        }

        @SuppressLint("PrivateResource")
        @Override
        public RemoteViews getViewAt(int position) {
            if (position == AdapterView.INVALID_POSITION || data == null
                    || !data.moveToPosition(position)) {
                return null;
            }

            RemoteViews remoteViews = new RemoteViews(getPackageName(),
                    R.layout.list_item_quote);

            String stockSymbol = data.getString(Contract.Quote.POSITION_SYMBOL);
            Float stockPrice = data.getFloat(Contract.Quote.POSITION_PRICE);
            Float absoluteChange = data.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
            DecimalFormat dollarFormat =
                    (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.getDefault());
            DecimalFormat dollarFormatWithPlus =
                    (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.getDefault());

            dollarFormatWithPlus.setPositivePrefix("+");
            dollarFormatWithPlus.setMaximumFractionDigits(2);
            dollarFormat.setMaximumFractionDigits(2);
            dollarFormat.setMinimumFractionDigits(2);
            dollarFormatWithPlus.setMinimumFractionDigits(2);

            int backgroundDrawable = absoluteChange > 0 ?
                    R.drawable.percent_change_pill_green : R.drawable.percent_change_pill_red;

            remoteViews.setTextViewText(R.id.symbol, stockSymbol);
            remoteViews.setTextViewTextSize(R.id.symbol, TypedValue.COMPLEX_UNIT_DIP, 14);
            remoteViews.setTextViewText(R.id.price, dollarFormat.format(stockPrice));
            remoteViews.setTextViewTextSize(R.id.price, TypedValue.COMPLEX_UNIT_DIP, 14);
            remoteViews.setTextViewText(R.id.change, dollarFormatWithPlus.format(absoluteChange));
            remoteViews.setTextViewTextSize(R.id.change, TypedValue.COMPLEX_UNIT_DIP, 14);
            remoteViews.setInt(R.id.change, "setBackgroundResource", backgroundDrawable);

            Uri stockUri = Contract.Quote.makeUriForStock(stockSymbol);
            final Intent fillInIntent = new Intent()
                    .setData(stockUri);
            remoteViews.setOnClickFillInIntent(R.id.list_item_quote, fillInIntent);
            return remoteViews;

        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int i) {
            return data.moveToPosition(i) ? data.getLong(Contract.Quote.POSITION_ID) : i;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }
}