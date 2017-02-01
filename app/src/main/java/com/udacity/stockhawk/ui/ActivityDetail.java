package com.udacity.stockhawk.ui;

import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;


public class ActivityDetail extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    @BindView(R.id.tvName)
    TextView tvStockName;
    @BindView(R.id.tv_stock_price)
    TextView tvStockPrice;
    @BindView(R.id.tv_stock_change)
    TextView tvStockChange;
    @BindView(R.id.tv_day_high)
    TextView tvStockHigh;
    @BindView(R.id.tv_day_low)
    TextView tvStockLow;
    @BindView(R.id.chartStock)
    LineChart stockChart;
    private Uri uriStock;
    @BindColor(R.color.white)
    public int white;
    @BindColor(R.color.colorPrimary)
    public int colorPrimary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);
        uriStock = getIntent().getData();
        getSupportLoaderManager().initLoader(0,null,this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (uriStock != null)
            return new CursorLoader(this, uriStock, Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}), null, null, null);
        return null;
    }
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(data.moveToFirst()) {
            String stockName = data.getString(Contract.Quote.POSITION_NAME);
            Float stockPrice = data.getFloat(Contract.Quote.POSITION_PRICE);
            Float stockChange = data.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
            Float high = data.getFloat(Contract.Quote.POSITION_DAY_HIGH);
            Float low = data.getFloat(Contract.Quote.POSITION_DAY_LOW);
            String historyData = data.getString(Contract.Quote.POSITION_HISTORY);

            getWindow().getDecorView().setContentDescription(
                    String.format(getString(R.string.cd_detail_activity), stockName));

            DecimalFormat dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.getDefault());
            DecimalFormat dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.getDefault());
            dollarFormatWithPlus.setPositivePrefix("+$");

            tvStockName.setText(stockName);
            tvStockPrice.setText(dollarFormat.format(stockPrice));
            tvStockPrice.setContentDescription(String.format(getString(R.string.cd_stock_price), tvStockPrice.getText()));

            tvStockChange.setText(dollarFormatWithPlus.format(stockChange));
            if (stockChange > 0) {
                tvStockChange.setBackgroundResource(R.drawable.percent_change_pill_green);
                tvStockChange.setContentDescription(
                        String.format(getString(R.string.cd_stock_increment), tvStockChange.getText()));
            } else {
                tvStockChange.setBackgroundResource(R.drawable.percent_change_pill_red);
                tvStockChange.setContentDescription(
                        String.format(getString(R.string.cd_stock_decrement), tvStockChange.getText()));
            }

            if(low != -1) {
                tvStockHigh.setText(dollarFormat.format(high));
                tvStockHigh.setContentDescription(String.format(getString(R.string.cd_day_highest), tvStockHigh.getText()));
                tvStockHigh.setVisibility(View.VISIBLE);
                tvStockLow.setText(dollarFormat.format(low));
                tvStockLow.setContentDescription(String.format(getString(R.string.cd_day_lowest), tvStockLow.getText()));
                tvStockLow.setVisibility(View.VISIBLE);
            } else {
                tvStockHigh.setVisibility(View.GONE);
                tvStockLow.setVisibility(View.GONE);
            }

            drawChart(historyData);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private void drawChart(String historyData) {
        Pair<Long, List<Entry>> result = getFormattedStockHistory(historyData);
        List<Entry> dataPairs = result.second;
        Long referenceTime = result.first;
        LineDataSet dataSet = new LineDataSet(dataPairs, "");

        dataSet.setColor(white);
        dataSet.setDrawHighlightIndicators(false);
        dataSet.setCircleColor(colorPrimary);
        dataSet.setHighLightColor(white);
        dataSet.setValueTextColor(white);
        dataSet.setDrawValues(true);

        LineData lineData = new LineData(dataSet);
        stockChart.setData(lineData);

        XAxis xAxis = stockChart.getXAxis();
        // TODO: Create a formatter implementing IValueFormatter
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return getDate((long)value, "dd/MM/yyyy");
            }
        });
        xAxis.setEnabled(true);

        YAxis yAxisLeft = stockChart.getAxisLeft();
        yAxisLeft.setEnabled(true);

        YAxis yAxisRight = stockChart.getAxisRight();
        yAxisRight.setEnabled(false);

        Legend legend = stockChart.getLegend();
        legend.setEnabled(false);


       /*
        stockChart.getXAxis().setGridColor(white);
        stockChart.getAxisLeft().setGridColor(white);
        stockChart.getAxisRight().setGridColor(white);
        stockChart.getXAxis().setTextColor(white);
        */
        stockChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        Description description = new Description();
        description.setText("");
        stockChart.setDescription(description);
        stockChart.setExtraOffsets(10, 0, 0, 10);
        stockChart.animateX(1500, Easing.EasingOption.Linear);
    }

    public Pair<Long, List<Entry>> getFormattedStockHistory(String history) {
        List<Entry> entries = new ArrayList<>();
        List<Long> timeData = new ArrayList<>();
        List<Float> stockPrice = new ArrayList<>();
        String[] dataPairs = history.split("\n");

        for (String pair : dataPairs) {
            String[] entry = pair.split(",");
            timeData.add(Long.valueOf(entry[0]));
            stockPrice.add(Float.valueOf(entry[1]));
        }
        Collections.reverse(timeData);

        Collections.reverse(stockPrice);
        Long referenceTime = 0l; // Set Reference to 0 just to later convert it directly into Date
        for (int i = 0; i < timeData.size(); i++) {
            entries.add(new Entry(timeData.get(i) - referenceTime, stockPrice.get(i)));
        }
        return new Pair<>(referenceTime, entries);
    }

    public static String getDate(long milliSeconds, String dateFormat) {
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }
}
