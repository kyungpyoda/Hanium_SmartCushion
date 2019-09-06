package org.techtown.SmartCushion;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.techtown.SmartCushion.MainActivity.USERID;

public class Fragment1 extends Fragment {
    BarChart barChart;
    ArrayList<BarEntry> barEntryArrayList;
    ArrayList<String> labelsNames;
    ArrayList<DailyStatistics> dailyStatisticsArrayList = new ArrayList<>();
    String pValue;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment1, container, false);

        initUI(rootView);

        return rootView;
    }
    private void initUI(ViewGroup rootView) {
        //실시간 자세 정보에 따라 현상태 이미지 변경(리스너로 해야하나???)
        //ImageView status_img = rootView.findViewById(R.id.status_img);
        //status_img.setImageDrawable(getResources().getDrawable(R.drawable.cushion_bad));
        //status_img.setImageDrawable(getResources().getDrawable(R.drawable.cushion_bad, getActivity().getTheme()));
        //같은 명령어지만 api level에 맞춰서 둘 중 하나 골라서 ㄱㄱ

        Calendar c = Calendar.getInstance();
        SimpleDateFormat mdformat = new SimpleDateFormat("yyyyMMdd");
        String year = mdformat.format(c.getTime()).substring(0,4);
        String month = mdformat.format(c.getTime()).substring(4,6);
        String day = mdformat.format(c.getTime()).substring(6);

        FetchData fetchData = new FetchData();
        String temp = "";
        try {
            temp = fetchData.execute(
                    USERID,
                    year + month + day
            ).get();
            pValue = temp;
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Toast.makeText(getActivity(), temp ,Toast.LENGTH_LONG).show();


        //
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
    private void tempData() {
        dailyStatisticsArrayList.clear();
        for(int i = 0;i<24;i++){
            dailyStatisticsArrayList.add(
                    new DailyStatistics(Integer.toString(i),(pValue.charAt(i)-'0'))
            );
        }
    }
}
