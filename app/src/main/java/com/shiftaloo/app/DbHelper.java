package com.shiftaloo.app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public final class DbHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "shiftaloo.db";
    private static final int DB_VERSION = 1;

    public DbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE hospitals (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL," +
                "ward TEXT NOT NULL DEFAULT ''," +
                "base_salary INTEGER NOT NULL DEFAULT 0," +
                "day_rate INTEGER NOT NULL DEFAULT 0," +
                "evening_rate INTEGER NOT NULL DEFAULT 0," +
                "night_rate INTEGER NOT NULL DEFAULT 0)");
        db.execSQL("CREATE TABLE shifts (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "hospital_id INTEGER NOT NULL," +
                "date_key TEXT NOT NULL," +
                "type TEXT NOT NULL," +
                "start_millis INTEGER NOT NULL," +
                "end_millis INTEGER NOT NULL," +
                "custom_amount INTEGER NOT NULL DEFAULT 0," +
                "reminder_minutes INTEGER NOT NULL DEFAULT 0," +
                "alarm_enabled INTEGER NOT NULL DEFAULT 0," +
                "paid INTEGER NOT NULL DEFAULT 0," +
                "note TEXT NOT NULL DEFAULT ''," +
                "FOREIGN KEY(hospital_id) REFERENCES hospitals(id))");
        db.execSQL("CREATE INDEX idx_shifts_time ON shifts(start_millis,end_millis)");
        db.execSQL("CREATE INDEX idx_shifts_hospital ON shifts(hospital_id)");
    }

    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}

    public long saveHospital(Hospital hospital) {
        ContentValues values = new ContentValues();
        values.put("name", hospital.name.trim());
        values.put("ward", hospital.ward.trim());
        values.put("base_salary", hospital.baseSalary);
        values.put("day_rate", hospital.dayRate);
        values.put("evening_rate", hospital.eveningRate);
        values.put("night_rate", hospital.nightRate);
        if (hospital.id > 0) {
            getWritableDatabase().update("hospitals", values, "id=?", new String[]{Long.toString(hospital.id)});
            return hospital.id;
        }
        return getWritableDatabase().insertOrThrow("hospitals", null, values);
    }

    public List<Hospital> hospitals() {
        ArrayList<Hospital> result = new ArrayList<>();
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT id,name,ward,base_salary,day_rate,evening_rate,night_rate FROM hospitals ORDER BY name", null)) {
            while (c.moveToNext()) result.add(readHospital(c));
        }
        return result;
    }

    public Hospital hospital(long id) {
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT id,name,ward,base_salary,day_rate,evening_rate,night_rate FROM hospitals WHERE id=?",
                new String[]{Long.toString(id)})) {
            return c.moveToFirst() ? readHospital(c) : null;
        }
    }

    private Hospital readHospital(Cursor c) {
        Hospital h = new Hospital();
        h.id = c.getLong(0);
        h.name = c.getString(1);
        h.ward = c.getString(2);
        h.baseSalary = c.getLong(3);
        h.dayRate = c.getLong(4);
        h.eveningRate = c.getLong(5);
        h.nightRate = c.getLong(6);
        return h;
    }

    public long saveShift(Shift shift) {
        ContentValues values = new ContentValues();
        values.put("hospital_id", shift.hospitalId);
        values.put("date_key", shift.dateKey);
        values.put("type", shift.type);
        values.put("start_millis", shift.startMillis);
        values.put("end_millis", shift.endMillis);
        values.put("custom_amount", shift.customAmount);
        values.put("reminder_minutes", shift.reminderMinutes);
        values.put("alarm_enabled", shift.alarmEnabled ? 1 : 0);
        values.put("paid", shift.paid ? 1 : 0);
        values.put("note", shift.note == null ? "" : shift.note.trim());
        if (shift.id > 0) {
            getWritableDatabase().update("shifts", values, "id=?", new String[]{Long.toString(shift.id)});
            return shift.id;
        }
        return getWritableDatabase().insertOrThrow("shifts", null, values);
    }

    public void deleteShift(long id) {
        getWritableDatabase().delete("shifts", "id=?", new String[]{Long.toString(id)});
    }

    public Shift shift(long id) {
        List<Shift> rows = queryShifts("s.id=?", new String[]{Long.toString(id)}, "s.start_millis ASC");
        return rows.isEmpty() ? null : rows.get(0);
    }

    public List<Shift> allShifts() {
        return queryShifts(null, null, "s.start_millis ASC");
    }

    public List<Shift> futureShifts(long fromMillis, int limit) {
        return queryShifts("s.end_millis>=?", new String[]{Long.toString(fromMillis)}, "s.start_millis ASC LIMIT " + limit);
    }

    public List<Shift> filteredShifts(long fromMillis, long toMillis, long hospitalId, String type, int paidFilter) {
        StringBuilder where = new StringBuilder("s.start_millis>=? AND s.start_millis<?");
        ArrayList<String> args = new ArrayList<>();
        args.add(Long.toString(fromMillis));
        args.add(Long.toString(toMillis));
        if (hospitalId > 0) {
            where.append(" AND s.hospital_id=?");
            args.add(Long.toString(hospitalId));
        }
        if (type != null && !type.isEmpty()) {
            where.append(" AND s.type=?");
            args.add(type);
        }
        if (paidFilter >= 0) {
            where.append(" AND s.paid=?");
            args.add(Integer.toString(paidFilter));
        }
        return queryShifts(where.toString(), args.toArray(new String[0]), "s.start_millis ASC");
    }

    public List<Shift> conflicts(long shiftId, long startMillis, long endMillis) {
        return queryShifts(
                "s.id<>? AND s.start_millis<? AND s.end_millis>?",
                new String[]{Long.toString(shiftId), Long.toString(endMillis), Long.toString(startMillis)},
                "s.start_millis ASC");
    }

    private List<Shift> queryShifts(String where, String[] args, String order) {
        ArrayList<Shift> result = new ArrayList<>();
        String sql = "SELECT s.id,s.hospital_id,h.name,s.date_key,s.type,s.start_millis,s.end_millis," +
                "s.custom_amount,s.reminder_minutes,s.alarm_enabled,s.paid,s.note " +
                "FROM shifts s JOIN hospitals h ON h.id=s.hospital_id" +
                (where == null ? "" : " WHERE " + where) + " ORDER BY " + order;
        try (Cursor c = getReadableDatabase().rawQuery(sql, args)) {
            while (c.moveToNext()) {
                Shift s = new Shift();
                s.id = c.getLong(0);
                s.hospitalId = c.getLong(1);
                s.hospitalName = c.getString(2);
                s.dateKey = c.getString(3);
                s.type = c.getString(4);
                s.startMillis = c.getLong(5);
                s.endMillis = c.getLong(6);
                s.customAmount = c.getLong(7);
                s.reminderMinutes = c.getInt(8);
                s.alarmEnabled = c.getInt(9) == 1;
                s.paid = c.getInt(10) == 1;
                s.note = c.getString(11);
                result.add(s);
            }
        }
        return result;
    }

    public long estimatedAmount(Shift shift) {
        if (shift.customAmount > 0) return shift.customAmount;
        Hospital h = hospital(shift.hospitalId);
        return h == null ? 0 : h.rateFor(shift.type);
    }

    public long monthlyShiftIncome(long hospitalId, int jy, int jm) {
        PersianDate first = new PersianDate(jy, jm, 1);
        PersianDate next = jm == 12 ? new PersianDate(jy + 1, 1, 1) : new PersianDate(jy, jm + 1, 1);
        long total = 0;
        for (Shift s : filteredShifts(first.atTimeMillis(0, 0), next.atTimeMillis(0, 0), hospitalId, null, -1)) {
            total += estimatedAmount(s);
        }
        return total;
    }

    public long monthlyTotal(long hospitalId, int jy, int jm) {
        long shiftIncome = monthlyShiftIncome(hospitalId, jy, jm);
        if (hospitalId > 0) {
            Hospital h = hospital(hospitalId);
            return shiftIncome + (h == null ? 0 : h.baseSalary);
        }
        long bases = 0;
        for (Hospital h : hospitals()) bases += h.baseSalary;
        return shiftIncome + bases;
    }
}
