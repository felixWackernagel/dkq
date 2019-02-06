package de.wackernagel.dkq.utils;

import android.content.ContentUris;
import android.content.Intent;
import android.provider.CalendarContract;

public class CalendarBuilder {
    private final Intent calendar;

    /**
     * Constructor for a new Event
     */
    public CalendarBuilder() {
        calendar = new Intent(Intent.ACTION_INSERT);
        calendar.setData(CalendarContract.Events.CONTENT_URI);
    }

    /**
     * Constructor for existing Event
     */
    public CalendarBuilder(final long eventID) {
        calendar = new Intent(Intent.ACTION_EDIT);
        calendar.setData(ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventID));
    }

    public CalendarBuilder beginTime(final long beginTimeInMillis) {
        calendar.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTimeInMillis);
        return this;
    }

    public CalendarBuilder endTime(final long endTimeInMillis) {
        calendar.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTimeInMillis);
        return this;
    }

    public CalendarBuilder allDay(final boolean isAllDayEvent) {
        calendar.putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, isAllDayEvent);
        return this;
    }

    public CalendarBuilder title(final String title) {
        calendar.putExtra(CalendarContract.Events.TITLE, title);
        return this;
    }

    public CalendarBuilder description(final String description) {
        calendar.putExtra(CalendarContract.Events.DESCRIPTION, description);
        return this;
    }

    public CalendarBuilder location(final String location) {
        calendar.putExtra(CalendarContract.Events.EVENT_LOCATION, location);
        return this;
    }

    /**
     * Look at Spec 'http://tools.ietf.org/html/rfc5545#section-3.8.5.3'
     *
     * @param recurrenceRule
     * @return builder
     */
    public CalendarBuilder recurrence(final String recurrenceRule) {
        calendar.putExtra(CalendarContract.Events.RRULE, recurrenceRule);
        return this;
    }

    public CalendarBuilder access(final int accessLevel) {
        calendar.putExtra(CalendarContract.Events.ACCESS_LEVEL, accessLevel);
        return this;
    }

    public CalendarBuilder availability(final int availabilityConstant) {
        calendar.putExtra(CalendarContract.Events.AVAILABILITY, availabilityConstant);
        return this;
    }

    public CalendarBuilder invitee(final String email) {
        String invitees = email;
        if (calendar.hasExtra(Intent.EXTRA_EMAIL)) {
            invitees = calendar.getStringExtra(Intent.EXTRA_EMAIL) + "," + email;
        }
        calendar.putExtra(Intent.EXTRA_EMAIL, invitees);
        return this;
    }

    public Intent build() {
        return calendar;
    }
}
