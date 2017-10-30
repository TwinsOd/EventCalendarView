package com.od.twins.eventcalendarview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        HashSet<Date> events = new HashSet<>();
        events.add(new Date());
        Date date1 = new Date();
        date1.setDate(23);
        events.add(date1);
        Date date2 = new Date();
        date2.setDate(15);
        events.add(date2);
        Date date3 = new Date();
        date3.setDate(6);
        events.add(date3);

        EventCalendarView eventCalendarView = findViewById(R.id.calendar_view);
        Calendar calendar = Calendar.getInstance();
        // assign event handler
        eventCalendarView.setEventHandler(new EventCalendarView.EventHandler() {
            @Override
            public void onDayLongPress(Date date) {
                // show returned day
                DateFormat df = SimpleDateFormat.getDateInstance();
                Toast.makeText(MainActivity.this, df.format(date), Toast.LENGTH_SHORT).show();
                Log.d("MainActivity", "Date long click : " + df.format(date));
            }

            @Override
            public void onDayPress(Date date) {
                DateFormat df = SimpleDateFormat.getDateInstance();
                Toast.makeText(MainActivity.this, df.format(date), Toast.LENGTH_SHORT).show();
                Log.d("MainActivity", "Date click : " + df.format(date));
            }

            @Override
            public void changeTitle(String month) {
                if (!TextUtils.isEmpty(month))
                    setTitle(month);
            }
        });
        calendar.add(Calendar.MONTH, 0);
        eventCalendarView.setCurrentDate(calendar);
        eventCalendarView.addEvent(events);
    }
}
