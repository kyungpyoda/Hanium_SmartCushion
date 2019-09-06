package org.techtown.SmartCushion;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.techtown.SmartCushion.MainActivity.USERID;

public class Fragment3 extends Fragment {

    BarChart barChart;
    ArrayList<BarEntry> barEntryArrayList;
    ArrayList<String> labelsNames;
    ArrayList<DailyStatistics> dailyStatisticsArrayList = new ArrayList<>();
    PieChart pieChart;
    TextView displayDate;
    int[] DATE = new int[3];
    DatePickerDialog.OnDateSetListener dateSetListener;
    String pValue;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment3, container, false);

        initUI(rootView);

        return rootView;
    }
    private void initUI(final ViewGroup rootView) {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat mdformat = new SimpleDateFormat("yyyyMMdd");
        DATE[0]=Integer.parseInt(mdformat.format(c.getTime()).substring(0,4));
        DATE[1]=Integer.parseInt(mdformat.format(c.getTime()).substring(4,6));
        DATE[2]=Integer.parseInt(mdformat.format(c.getTime()).substring(6));
        String date = DATE[0] + "년 " + DATE[1] + "월 " + DATE[2] + "일";

        displayDate = rootView.findViewById(R.id.tvDate);
        displayDate.setText(date);
        displayDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(
                        getActivity(),
                        android.R.style.Theme_Holo_Dialog_MinWidth,
                        dateSetListener,
                        year, month, day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.getDatePicker().setMaxDate(System.currentTimeMillis()-1000);
                dialog.show();
            }

        });
        dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month = month + 1;
                String date = year + "년 " + month + "월 " + dayOfMonth + "일";
                DATE[0]=year;
                DATE[1]=month;
                DATE[2]=dayOfMonth;

                displayDate.setText(date);

                FetchData fetchData = new FetchData();
                String temp = "";
                try {
                    temp = fetchData.execute(
                            USERID,
                            Integer.toString(DATE[0])+
                            Integer.toString(DATE[1])+
                            Integer.toString(DATE[2])
                    ).get();
                    pValue = temp;
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Toast.makeText(getActivity(), temp ,Toast.LENGTH_LONG).show();
                updateChart(rootView);
            }
        };

        FetchData fetchData = new FetchData();
        String temp = "";
        try {
            temp = fetchData.execute(
                    USERID,
                    Integer.toString(DATE[0])+
                            Integer.toString(DATE[1])+
                            Integer.toString(DATE[2])
            ).get();
            pValue = temp;
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Toast.makeText(getActivity(), temp ,Toast.LENGTH_LONG).show();
        updateChart(rootView);

    }

    private void tempData() {
        dailyStatisticsArrayList.clear();
        for(int i = 0;i<24;i++){
            dailyStatisticsArrayList.add(
                    new DailyStatistics(Integer.toString(i),(pValue.charAt(i)-'0'))
            );
        }
    }

    private class MyBarDataSet extends BarDataSet {
        public MyBarDataSet(List<BarEntry> yVals, String label) {
            super(yVals, label);
        }

        @Override
        public int getColor(int index) {
            if(getEntryForIndex(index).getY() == 1)
                return mColors.get(0);
            else
                return mColors.get(1);
        }
    }
    private class MyPieDataSet extends PieDataSet {
        public MyPieDataSet(List<PieEntry> yVals, String label) { super(yVals, label); }

        @Override
        public int getColor(int index) {
            if(getEntryForIndex(index).getLabel().equals("GOOD"))
                return mColors.get(0);
            else
                return mColors.get(1);
        }
    }

    private void updateChart(final ViewGroup rootView){
        barChart = rootView.findViewById(R.id.barchart);


        tempData();
        barEntryArrayList = new ArrayList<>();
        labelsNames = new ArrayList<>();
        barEntryArrayList.clear();
        labelsNames.clear();

        for(int i = 0; i < dailyStatisticsArrayList.size(); i++) {
            String hour = dailyStatisticsArrayList.get(i).getHour();
            int status = dailyStatisticsArrayList.get(i).getStatus();
            barEntryArrayList.add(new BarEntry(i, status));

            labelsNames.add(hour);
        }
        //BarDataSet barDataSet = new BarDataSet(barEntryArrayList, "Daily statistics");
        MyBarDataSet myBarDataSet = new MyBarDataSet(barEntryArrayList, "");
        //barDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        myBarDataSet.setColors(new int[]{
                ContextCompat.getColor(barChart.getContext(), R.color.barB),
                ContextCompat.getColor(barChart.getContext(), R.color.barR)});
        myBarDataSet.setDrawValues(false); //값 표시 제거
        Description description = new Description();
        description.setText("Hours");
        barChart.setDescription(description);
        BarData barData = new BarData(myBarDataSet);
        barChart.setData(barData);
        XAxis xAxis = barChart.getXAxis();
        //xAxis.setValueFormatter(new IndexAxisValueFormatter(labelsNames));
        //xAxis.setPosition(XAxis.XAxisPosition.TOP);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(labelsNames.size());
        barChart.getAxisLeft().setAxisMaximum(1f);
        barChart.getAxisRight().setAxisMaximum(1f);
        barChart.getAxisLeft().setDrawGridLines(false);
        barChart.getAxisRight().setDrawGridLines(false);
        barChart.getAxisLeft().setDrawLabels(false);
        barChart.getAxisRight().setDrawLabels(false);
        barChart.getAxisLeft().setDrawZeroLine(true);
        barChart.getAxisRight().setDrawZeroLine(true);
        barChart.setDrawMarkers(false);
        barChart.setDrawBorders(false);
        barChart.invalidate();

        pieChart = rootView.findViewById(R.id.piechart);
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setExtraOffsets(5,10,5,5);
        pieChart.setDragDecelerationFrictionCoef(0.95f);
        pieChart.setDrawHoleEnabled(false);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setTransparentCircleRadius(61f);
        ArrayList<PieEntry> yValues = new ArrayList<PieEntry>();
        int pieBad = 0, pieGood = 0;
        for(DailyStatistics d : dailyStatisticsArrayList) {
            if(d.status != 0) {
                if(d.status == 1) pieGood++;
                else pieBad++;
            }
        }
        yValues.add(new PieEntry(pieBad, "BAD"));
        yValues.add(new PieEntry(pieGood, "GOOD"));
        Description description2 = new Description();
        description2.setText("pie");
        description2.setTextSize(15);
        pieChart.setDescription(description2);
        MyPieDataSet myPieDataSet = new MyPieDataSet(yValues, "pie");
        myPieDataSet.setSliceSpace(3f);
        myPieDataSet.setSelectionShift(5f);
        myPieDataSet.setColors(new int[]{
                ContextCompat.getColor(pieChart.getContext(), R.color.barB),
                ContextCompat.getColor(pieChart.getContext(), R.color.barR)});
        PieData pieData = new PieData(myPieDataSet);
        pieData.setValueTextSize(10f);
        pieData.setValueTextColor(Color.YELLOW);
        pieChart.setData(pieData);
        pieChart.invalidate();
    }
}
