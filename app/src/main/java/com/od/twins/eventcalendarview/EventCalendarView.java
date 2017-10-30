package com.od.twins.eventcalendarview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;

/**
 * Created by user on 10/27/2017.
 */

public class EventCalendarView extends LinearLayout {
    // how many days to show, defaults to six weeks, 42 days
    private static final int DAYS_COUNT = 42;
    // default date format
    private static final String DATE_FORMAT = "MMM yyyy";
    // date format
    private String dateFormat;
    // current displayed month
    private Calendar currentDate;
    //event handling
    private EventHandler eventHandler = null;
    // internal components
    private GridView grid;

    public EventCalendarView(Context context) {
        super(context);
    }

    public EventCalendarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initControl(context, attrs);
    }

    public EventCalendarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initControl(context, attrs);
    }

    /**
     * Load control xml layout
     */
    private void initControl(Context context, AttributeSet attrs) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.control_calendar, this);

        loadDateFormat(attrs);
        grid = (GridView) findViewById(R.id.calendar_grid);
        assignClickHandlers();

        addEvent();
    }

    private void loadDateFormat(AttributeSet attrs) {
        TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.EventCalendarView);

        try {
            // try to load provided date format, and fallback to default otherwise
            dateFormat = ta.getString(R.styleable.EventCalendarView_dateFormat);
            if (dateFormat == null)
                dateFormat = DATE_FORMAT;
        } finally {
            ta.recycle();
        }
    }

    private void assignClickHandlers() {
//         long-pressing a day
        grid.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> view, View cell, int position, long id) {
                // handle long-press
                if (eventHandler == null)
                    return false;

                eventHandler.onDayLongPress((Date) view.getItemAtPosition(position));
                return true;
            }
        });

        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d("CalendarView", "Date click : " + i);
                // handle long-press
                if (eventHandler != null)
                    eventHandler.onDayPress((Date) adapterView.getItemAtPosition(i));
            }
        });
    }

    public void setCurrentDate(Calendar currentDate) {
        this.currentDate = currentDate;
    }

    /**
     * Display dates correctly in grid
     */
    public void addEvent() {
        addEvent(null);
    }

    /**
     * Display dates correctly in grid
     */
    public void addEvent(HashSet<Date> events) {
        if (currentDate == null)
            currentDate = Calendar.getInstance();
        ArrayList<Date> cells = new ArrayList<>();
        Calendar calendar = (Calendar) currentDate.clone();

        // determine the cell for current month's beginning
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        int monthBeginningCell = calendar.get(Calendar.DAY_OF_WEEK) - 1;

        // move calendar backwards to the beginning of the week
        calendar.add(Calendar.DAY_OF_MONTH, -monthBeginningCell);

        // fill cells
        while (cells.size() < DAYS_COUNT) {
            cells.add(calendar.getTime());
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        // update grid
        grid.setAdapter(new CalendarAdapter(getContext(), cells, events));

//        // update title
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        if (eventHandler != null)
            eventHandler.changeTitle(sdf.format(currentDate.getTime()));
    }

    private class CalendarAdapter extends ArrayAdapter<Date> {
        // days with events
        private HashSet<Date> eventDays;

        // for view inflation
        private LayoutInflater inflater;

        public CalendarAdapter(Context context, ArrayList<Date> days, HashSet<Date> eventDays) {
            super(context, R.layout.control_calendar_day, days);
            this.eventDays = eventDays;
            inflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(int position, View viewParent, ViewGroup parent) {

            // day in question
            Date date = getItem(position);
            int day = date.getDate();
            int month = date.getMonth();
            int year = date.getYear();

            // today
            Date today = new Date();

            // inflate item if it does not exist yet
            if (viewParent == null)
                viewParent = inflater.inflate(R.layout.control_calendar_day, parent, false);

            TextView numberView = viewParent.findViewById(R.id.number_view);
            // if this day has an event, specify event image
            numberView.setBackgroundResource(0);
            // clear styling
            numberView.setTypeface(null, Typeface.NORMAL);
            numberView.setTextColor(Color.BLACK);

            if (month != currentDate.get(Calendar.MONTH) && year != currentDate.get(Calendar.YEAR)) {
                // if this day is outside current month, grey it out
                numberView.setTextColor(getResources().getColor(R.color.greyed_out));
            }

            if (day == today.getDate() && month == today.getMonth() && year == today.getYear()) {
                // if it is today, set it to blue/bold
                numberView.setTypeface(null, Typeface.BOLD);
                numberView.setTextColor(getResources().getColor(R.color.today));
                numberView.setBackgroundResource(R.drawable.background_today);
            }

            if (eventDays != null) {
                for (Date eventDate : eventDays) {
                    if (eventDate.getDate() == day && eventDate.getMonth() == month && eventDate.getYear() == year) {
                        // mark this day for event
                        if (eventDate.getDate() == today.getDate()) {
                            numberView.setBackgroundResource(R.drawable.background_event_today);
                            numberView.setTextColor(getResources().getColor(R.color.today));
                        } else
                            numberView.setBackgroundResource(R.drawable.background_event);
                        break;
                    }
                }
            }
            // set text
            numberView.setText(String.valueOf(date.getDate()));

            return numberView;
        }
    }

    /**
     * Assign event handler to be passed needed events
     */
    public void setEventHandler(EventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }

    /**
     * This interface defines what events to be reported to
     * the outside world
     */
    public interface EventHandler {
        void onDayLongPress(Date date);

        void onDayPress(Date date);

        void changeTitle(String month);
    }
}
