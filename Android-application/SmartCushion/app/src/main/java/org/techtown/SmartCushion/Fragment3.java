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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.Chart;
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
    ArrayList<Integer> pValue;
    LinearLayout layoutForNull2;
    LinearLayout layoutForNull3;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment3, container, false);

        initUI(rootView);

        return rootView;
    }
    private void initUI(final ViewGroup rootView) {
        ////날짜를 선택하여 통계조회
        Calendar c = Calendar.getInstance();
        SimpleDateFormat mdformat = new SimpleDateFormat("yyyyMMdd");
        DATE[0]=Integer.parseInt(mdformat.format(c.getTime()).substring(0,4));
        DATE[1]=Integer.parseInt(mdformat.format(c.getTime()).substring(4,6));
        DATE[2]=Integer.parseInt(mdformat.format(c.getTime()).substring(6));
        String date = DATE[0] + "년 " + DATE[1] + "월 " + DATE[2] + "일";
        ////DatePicker dialog 생성
        displayDate = rootView.findViewById(R.id.tvDate);
        displayDate.setText(" "+date+" ");
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
        ////날짜를 선택하면 USERID와 선택한 날짜를 담아서 fetchData를 실행하여 http 요청
        dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month = month + 1;
                String date = year + "년 " + month + "월 " + dayOfMonth + "일";
                DATE[0]=year;
                DATE[1]=month;
                DATE[2]=dayOfMonth;

                displayDate.setText(" "+date+" ");

                FetchData fetchData = new FetchData();
                ////pValue는 파싱된 24개의 정수배열(0~23시)
                try {
                    pValue = fetchData.execute(
                            USERID,
                            Integer.toString(DATE[0])+':'+
                            Integer.toString(DATE[1])+':'+
                            Integer.toString(DATE[2])
                    ).get();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //Toast.makeText(getActivity(), temp ,Toast.LENGTH_LONG).show();
                updateChart(rootView);
            }
        };

        ////위의 소스와 중복되는 이유는
        ////위의 소스는 datepicker dialog 의 날짜 선택에 따른 리스너 설정
        ////아래 소스는 현재 통계 탭(Fragment3)을 띄우자마자 바로 그래프로 보여주기 위함
        FetchData fetchData = new FetchData();
        try {
            pValue = fetchData.execute(
                    USERID,
                    Integer.toString(DATE[0])+':'+
                            Integer.toString(DATE[1])+':'+
                            Integer.toString(DATE[2])
            ).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        barChart = rootView.findViewById(R.id.barchart);
        pieChart = rootView.findViewById(R.id.piechart);
        layoutForNull2 = (LinearLayout)rootView.findViewById(R.id.layoutForNull2);
        layoutForNull3 = (LinearLayout)rootView.findViewById(R.id.layoutForNull3);


        updateChart(rootView);
    }

    ////pValue에 파싱되어 있는 데이터를 그래프로 보여주기 위해 다시 변환
    private void tempData() {
        dailyStatisticsArrayList.clear();
        for(int i = 0;i<pValue.size();i++){
            dailyStatisticsArrayList.add(
                    new DailyStatistics(Integer.toString(i),(pValue.get(i)))
            );
        }
    }

    private class MyBarDataSet extends BarDataSet {
        public MyBarDataSet(List<BarEntry> yVals, String label) {
            super(yVals, label);
        }

        @Override
        public int getColor(int index) {
            ////그래프의 자세값이 0은 정상, 1,2,3,4는 비정상
            if((Integer) getEntryForIndex(index).getData() == 0)
                return mColors.get(0); ////get(0)은 초록색
            else
                return mColors.get(1); ////get(1)은 빨간색
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

    ////변환해놓은 값들을 이용해서 막대그래프, 원그래프로 시각화
    private void updateChart(final ViewGroup rootView){
        //barChart = rootView.findViewById(R.id.barchart);
        if (pValue.isEmpty()) {
            layoutForNull2.setVisibility(View.VISIBLE);
            layoutForNull3.setVisibility(View.VISIBLE);
            barChart.setNoDataText("");
            pieChart.setNoDataText("");
            barChart.invalidate();
            pieChart.invalidate();
            return;
        }
        layoutForNull2.setVisibility(View.INVISIBLE);
        layoutForNull3.setVisibility(View.INVISIBLE);

        tempData();
        barEntryArrayList = new ArrayList<>();
        labelsNames = new ArrayList<>();
        barEntryArrayList.clear();
        labelsNames.clear();

        for(int i = 0; i < dailyStatisticsArrayList.size(); i++) {
            String hour = dailyStatisticsArrayList.get(i).getHour();
            int status = dailyStatisticsArrayList.get(i).getStatus();
            barEntryArrayList.add(new BarEntry(i, (status == -1) ? 0 : 1 , status));

            labelsNames.add(hour);
        }
        //BarDataSet barDataSet = new BarDataSet(barEntryArrayList, "Daily statistics");
        MyBarDataSet myBarDataSet = new MyBarDataSet(barEntryArrayList, "");
        //barDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        myBarDataSet.setColors(new int[]{
                ContextCompat.getColor(barChart.getContext(), R.color.goodc),
                ContextCompat.getColor(barChart.getContext(), R.color.badc)});
        myBarDataSet.setDrawValues(false); //값 표시 제거
        Description description = new Description();
        description.setText("");
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

        barChart.setNoDataText("표시할 데이터가 없습니다.");
        barChart.getPaint(Chart.PAINT_INFO).setTextSize(40f);

        barChart.invalidate();

        //pieChart = rootView.findViewById(R.id.piechart);
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setExtraOffsets(5,10,5,5);
        pieChart.setDragDecelerationFrictionCoef(0.95f);
        pieChart.setDrawHoleEnabled(false);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setTransparentCircleRadius(61f);
        pieChart.setDrawEntryLabels(false);
        pieChart.setDrawMarkers(false);
        ArrayList<PieEntry> yValues = new ArrayList<PieEntry>();
        int pieBad = 0, pieGood = 0;
        for(DailyStatistics d : dailyStatisticsArrayList) {
            if(d.status != -1) {
                if(d.status == 0) pieGood++;
                else pieBad++;
            }
        }
        yValues.add(new PieEntry(pieGood, "GOOD"));
        yValues.add(new PieEntry(pieBad, "BAD"));
        Description description2 = new Description();
        description2.setText("");
        description2.setTextSize(15);
        pieChart.setDescription(description2);
        MyPieDataSet myPieDataSet = new MyPieDataSet(yValues, "");
        myPieDataSet.setSliceSpace(3f);
        myPieDataSet.setSelectionShift(5f);
        myPieDataSet.setColors(new int[]{
                ContextCompat.getColor(pieChart.getContext(), R.color.goodc),
                ContextCompat.getColor(pieChart.getContext(), R.color.badc)});
        PieData pieData = new PieData(myPieDataSet);
        pieData.setValueTextSize(10f);
        pieData.setValueTextColor(Color.YELLOW);
        pieChart.setData(pieData);
        pieChart.setNoDataText("표시할 데이터가 없습니다.");
        pieChart.getPaint(Chart.PAINT_INFO).setTextSize(40f);
        pieChart.invalidate();
    }
}
