package com.shiftaloo.app;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public final class MainActivity extends Activity {
    private DbHelper db;
    private FrameLayout content;
    private int viewYear;
    private int viewMonth;
    private int activeTab = 0;
    private int pendingQuickType = -1;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_main);
        getWindow().setStatusBarColor(getColor(R.color.cream));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        db = new DbHelper(this);
        content = findViewById(R.id.content);
        PersianDate today = PersianDate.today();
        viewYear = today.year;
        viewMonth = today.month;
        ((TextView) findViewById(R.id.todayLabel)).setText(today.longText());

        findViewById(R.id.addShiftButton).setOnClickListener(v -> showShiftDialog(null, null));
        findViewById(R.id.tabAgenda).setOnClickListener(v -> showTab(0));
        findViewById(R.id.tabCalendar).setOnClickListener(v -> showTab(1));
        findViewById(R.id.tabReports).setOnClickListener(v -> showTab(2));
        findViewById(R.id.tabHospitals).setOnClickListener(v -> showTab(3));
        requestNotificationPermission();
        showTab(0);
    }

    @Override protected void onResume() {
        super.onResume();
        if (content != null) showTab(activeTab);
    }

    private void showTab(int tab) {
        activeTab = tab;
        int[] tabs = {R.id.tabAgenda, R.id.tabCalendar, R.id.tabReports, R.id.tabHospitals};
        for (int i = 0; i < tabs.length; i++) findViewById(tabs[i]).setSelected(i == tab);
        content.removeAllViews();
        if (tab == 1) content.addView(calendarPage());
        else if (tab == 2) content.addView(reportPage());
        else if (tab == 3) content.addView(hospitalsPage());
        else content.addView(agendaPage());
    }

    private View agendaPage() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        LinearLayout root = column();
        root.setPadding(dp(16), dp(4), dp(16), dp(28));
        scroll.addView(root);

        List<Shift> future = db.futureShifts(System.currentTimeMillis(), 1);
        LinearLayout nextCard = column();
        nextCard.setBackgroundResource(R.drawable.bg_next_shift);
        nextCard.setElevation(dp(5));
        if (future.isEmpty()) {
            nextCard.addView(kicker("شیفت بعدی"));
            nextCard.addView(heading("فعلاً شیفتی در راه نیست 🌱", 20));
            nextCard.addView(muted("از دکمه‌های پایین یک شیفت تازه بساز."));
            nextCard.setOnClickListener(v -> showShiftDialog(null, null));
        } else {
            Shift upcoming = future.get(0);
            nextCard.addView(kicker("شیفت بعدی"));
            nextCard.addView(heading(PersianDate.fromMillis(upcoming.startMillis).longText(), 22));
            nextCard.addView(heading(upcoming.typeName() + " · " + upcoming.hospitalName, 16));
            nextCard.addView(muted(timeRange(upcoming) + (upcoming.alarmEnabled ? " · زنگ فعال" : "")));
            nextCard.setOnClickListener(v -> showShiftDialog(upcoming, null));
        }
        root.addView(nextCard);

        LinearLayout shortcutCard = card();
        shortcutCard.addView(kicker("ثبت سریع"));
        shortcutCard.addView(heading("چه شیفتی داری؟", 17));
        LinearLayout shortcuts = row();
        Button dayQuick = shortcut("☀\nروزکار\n۷ تا ۱۵", R.drawable.bg_day, R.color.mint_ink);
        Button eveningQuick = shortcut("◒\nعصرکار\n۱۵ تا ۲۳", R.drawable.bg_evening, R.color.peach_dark);
        Button nightQuick = shortcut("☾\nشب‌کار\n۱۹ تا ۷", R.drawable.bg_night, R.color.lilac_ink);
        dayQuick.setOnClickListener(v -> quickShift(0)); eveningQuick.setOnClickListener(v -> quickShift(1)); nightQuick.setOnClickListener(v -> quickShift(2));
        shortcuts.addView(dayQuick, weightHeight(dp(102))); shortcuts.addView(eveningQuick, weightHeight(dp(102))); shortcuts.addView(nightQuick, weightHeight(dp(102)));
        shortcutCard.addView(shortcuts, marginTop(10));
        root.addView(shortcutCard, marginTop(14));

        LinearLayout listCard = card();
        LinearLayout monthBar = row();
        Button next = smallButton("‹");
        TextView title = heading(monthTitle(), 18); title.setGravity(Gravity.CENTER);
        Button previous = smallButton("›");
        monthBar.addView(next); monthBar.addView(title, weight()); monthBar.addView(previous);
        listCard.addView(monthBar);

        List<Hospital> hospitals = db.hospitals();
        Spinner hospital = spinner(hospitalNames(hospitals, true));
        Spinner type = spinner(new String[]{"همه نوع‌ها", "روزکار", "عصرکار", "شب‌کار"});
        Spinner paid = spinner(new String[]{"پرداخت: همه", "پرداخت‌شده", "پرداخت‌نشده"});
        LinearLayout filter1 = row(); filter1.setPadding(0, dp(8), 0, dp(6));
        filter1.addView(hospital, weight()); filter1.addView(type, weight());
        listCard.addView(filter1); listCard.addView(paid, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(48)));
        LinearLayout list = column(); listCard.addView(list, marginTop(8));
        root.addView(listCard, marginTop(14));

        Runnable refresh = () -> {
            PersianDate first = new PersianDate(viewYear, viewMonth, 1);
            long hospitalId = hospital.getSelectedItemPosition() <= 0 ? 0 : hospitals.get(hospital.getSelectedItemPosition() - 1).id;
            String shiftType = typeCode(type.getSelectedItemPosition());
            int paidFilter = paid.getSelectedItemPosition() == 0 ? -1 : paid.getSelectedItemPosition() == 1 ? 1 : 0;
            List<Shift> shifts = db.filteredShifts(first.atTimeMillis(0, 0), nextMonth(first).atTimeMillis(0, 0), hospitalId, shiftType, paidFilter);
            title.setText(monthTitle() + " · " + Fa.n(shifts.size()) + " شیفت");
            list.removeAllViews();
            if (shifts.isEmpty()) list.addView(empty("برای این فیلتر شیفتی پیدا نشد 🍑"));
            for (int i = 0; i < shifts.size(); i++) {
                Shift shift = shifts.get(i);
                View item = new ShiftAdapter(shifts).getView(i, null, list);
                item.setOnClickListener(v -> showShiftDialog(shift, null));
                item.setOnLongClickListener(v -> { confirmDelete(shift); return true; });
                list.addView(item);
            }
        };
        AdapterView.OnItemSelectedListener changed = new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) { refresh.run(); }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        };
        hospital.setOnItemSelectedListener(changed);
        type.setOnItemSelectedListener(changed);
        paid.setOnItemSelectedListener(changed);
        previous.setOnClickListener(v -> { moveMonth(-1); showTab(0); });
        next.setOnClickListener(v -> { moveMonth(1); showTab(0); });
        refresh.run();
        return scroll;
    }

    private void quickShift(int type) { pendingQuickType = type; showShiftDialog(null, null); }

    private void confirmDelete(Shift shift) {
        new AlertDialog.Builder(this).setTitle("حذف شیفت؟").setMessage(shift.typeName() + " در " + shift.hospitalName)
                .setNegativeButton("نه", null).setPositiveButton("حذف", (d, w) -> {
                    AlarmScheduler.cancel(this, shift.id); db.deleteShift(shift.id); ShiftWidgetProvider.refresh(this); showTab(0);
                }).show();
    }

    private View calendarPage() {
        ScrollView scroll = new ScrollView(this);
        LinearLayout root = column();
        root.setPadding(dp(14), dp(4), dp(14), dp(24));
        scroll.addView(root);

        LinearLayout monthBar = row();
        Button next = smallButton("ماه بعد");
        TextView title = heading(monthTitle(), 20);
        title.setGravity(Gravity.CENTER);
        Button previous = smallButton("ماه قبل");
        monthBar.addView(next);
        monthBar.addView(title, weight());
        monthBar.addView(previous);
        root.addView(monthBar);
        next.setOnClickListener(v -> { moveMonth(1); showTab(1); });
        previous.setOnClickListener(v -> { moveMonth(-1); showTab(1); });

        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(7);
        grid.setAlignmentMode(GridLayout.ALIGN_BOUNDS);
        for (String day : new String[]{"ش", "ی", "د", "س", "چ", "پ", "ج"}) {
            TextView h = label(day, 13, true);
            h.setGravity(Gravity.CENTER);
            h.setTextColor(day.equals("ج") ? getColor(R.color.danger) : getColor(R.color.muted));
            grid.addView(h, gridCell());
        }
        PersianDate first = new PersianDate(viewYear, viewMonth, 1);
        for (int i = 0; i < first.weekdayIndex(); i++) grid.addView(new TextView(this), gridCell());
        List<Shift> monthShifts = db.filteredShifts(first.atTimeMillis(0, 0), nextMonth(first).atTimeMillis(0, 0), 0, null, -1);
        for (int day = 1; day <= PersianDate.monthLength(viewYear, viewMonth); day++) {
            PersianDate date = new PersianDate(viewYear, viewMonth, day);
            int count = 0;
            for (Shift s : monthShifts) if (s.dateKey.equals(date.key())) count++;
            String holiday = HolidayRepository.title(date);
            Button cell = new Button(this);
            cell.setAllCaps(false);
            cell.setMinHeight(0); cell.setMinWidth(0);
            cell.setPadding(2, 2, 2, 2);
            cell.setText(Fa.n(day) + (count > 0 ? "\n● " + Fa.n(count) : ""));
            cell.setTextSize(13);
            cell.setTextColor(HolidayRepository.isHoliday(date) ? getColor(R.color.danger) : getColor(R.color.ink));
            cell.setBackgroundResource(R.drawable.bg_chip);
            cell.setContentDescription(date.longText() + (holiday == null ? "" : "، " + holiday));
            cell.setOnClickListener(v -> showDay(date));
            grid.addView(cell, gridCell());
        }
        root.addView(grid, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView info = label("روزهای قرمز تعطیل رسمی یا جمعه‌اند. روی هر روز بزن تا شیفت‌ها و عنوان تعطیلی را ببینی.", 13, false);
        info.setTextColor(getColor(R.color.muted));
        info.setPadding(4, dp(14), 4, 4);
        root.addView(info);
        return scroll;
    }

    private void showDay(PersianDate date) {
        long from = date.atTimeMillis(0, 0);
        long to = date.plusDays(1).atTimeMillis(0, 0);
        List<Shift> shifts = db.filteredShifts(from, to, 0, null, -1);
        StringBuilder text = new StringBuilder();
        String holiday = HolidayRepository.title(date);
        if (holiday != null) text.append("تعطیل رسمی: ").append(holiday).append("\n\n");
        if (date.isFriday() && holiday == null) text.append("تعطیل هفتگی جمعه\n\n");
        if (shifts.isEmpty()) text.append("شیفتی برای این روز ثبت نشده.");
        for (Shift s : shifts) text.append("• ").append(s.typeName()).append(" در ").append(s.hospitalName)
                .append("، ").append(timeRange(s)).append("\n");
        new AlertDialog.Builder(this).setTitle(date.longText()).setMessage(text.toString())
                .setNegativeButton("بستن", null)
                .setPositiveButton("افزودن شیفت", (d, w) -> showShiftDialog(null, date)).show();
    }

    private View hospitalsPage() {
        ScrollView scroll = new ScrollView(this);
        LinearLayout root = column();
        root.setPadding(dp(14), dp(4), dp(14), dp(24));
        LinearLayout bar = row();
        bar.addView(heading("بیمارستان‌های من", 20), weight());
        Button add = smallButton("+ بیمارستان");
        add.setBackgroundResource(R.drawable.bg_primary);
        add.setTextColor(Color.WHITE);
        bar.addView(add);
        root.addView(bar);
        add.setOnClickListener(v -> showHospitalDialog(null));

        List<Hospital> hospitals = db.hospitals();
        if (hospitals.isEmpty()) {
            TextView empty = label("اولین بیمارستان را بساز؛ حقوق پایه و نرخ روزکار، عصرکار و شب‌کار همین‌جا تعریف می‌شود.", 16, false);
            empty.setBackgroundResource(R.drawable.bg_card);
            empty.setPadding(dp(18), dp(24), dp(18), dp(24));
            root.addView(empty, marginTop(14));
        }
        for (Hospital h : hospitals) {
            LinearLayout card = column();
            card.setBackgroundResource(R.drawable.bg_card);
            card.setPadding(dp(16), dp(14), dp(16), dp(14));
            TextView name = heading(h.name + (h.ward.isEmpty() ? "" : " • " + h.ward), 18);
            card.addView(name);
            card.addView(label("حقوق پایه: " + Fa.money(h.baseSalary), 14, false));
            card.addView(label("روزکار " + Fa.money(h.dayRate) + "  |  عصرکار " + Fa.money(h.eveningRate) + "  |  شب‌کار " + Fa.money(h.nightRate), 13, false));
            Button edit = smallButton("ویرایش نرخ‌ها");
            edit.setOnClickListener(v -> showHospitalDialog(h));
            card.addView(edit, marginTop(8));
            root.addView(card, marginTop(10));
        }
        scroll.addView(root);
        return scroll;
    }

    private View reportPage() {
        ScrollView scroll = new ScrollView(this);
        LinearLayout root = column();
        root.setPadding(dp(14), dp(4), dp(14), dp(24));

        LinearLayout monthBar = row();
        Button next = smallButton("ماه بعد");
        TextView title = heading("گزارش " + monthTitle(), 20);
        title.setGravity(Gravity.CENTER);
        Button previous = smallButton("ماه قبل");
        monthBar.addView(next); monthBar.addView(title, weight()); monthBar.addView(previous);
        root.addView(monthBar);
        next.setOnClickListener(v -> { moveMonth(1); showTab(2); });
        previous.setOnClickListener(v -> { moveMonth(-1); showTab(2); });

        List<Hospital> hospitals = db.hospitals();
        Spinner hospitalFilter = spinner(hospitalNames(hospitals, true));
        root.addView(hospitalFilter, marginTop(10));

        LinearLayout dynamic = column();
        root.addView(dynamic);
        Runnable refresh = () -> {
            dynamic.removeAllViews();
            long selected = hospitalFilter.getSelectedItemPosition() <= 0 ? 0 : hospitals.get(hospitalFilter.getSelectedItemPosition() - 1).id;
            PersianDate first = new PersianDate(viewYear, viewMonth, 1);
            List<Shift> current = db.filteredShifts(first.atTimeMillis(0, 0), nextMonth(first).atTimeMillis(0, 0), selected, null, -1);
            long total = db.monthlyTotal(selected, viewYear, viewMonth);
            TextView big = heading("حقوق تقریبی: " + Fa.money(total), 23);
            big.setTextColor(getColor(R.color.peach_dark));
            big.setBackgroundResource(R.drawable.bg_card);
            big.setPadding(dp(18), dp(18), dp(18), dp(18));
            dynamic.addView(big, marginTop(10));
            dynamic.addView(label("تعداد شیفت‌ها: " + Fa.n(current.size()) + "  •  جمع نرخ شیفت‌ها: " + Fa.money(shiftIncome(current)), 15, false), marginTop(8));

            for (Hospital h : hospitals) {
                if (selected > 0 && h.id != selected) continue;
                long shiftPay = db.monthlyShiftIncome(h.id, viewYear, viewMonth);
                long hospitalTotal = h.baseSalary + shiftPay;
                TextView row = label(h.name + "\nحقوق پایه " + Fa.money(h.baseSalary) + " + شیفت‌ها " + Fa.money(shiftPay) + " = " + Fa.money(hospitalTotal), 15, true);
                row.setBackgroundResource(R.drawable.bg_card);
                row.setPadding(dp(14), dp(12), dp(14), dp(12));
                dynamic.addView(row, marginTop(8));
            }

            dynamic.addView(heading("نمودار شش ماه از ماه انتخاب‌شده", 17), marginTop(18));
            IncomeChartView chart = new IncomeChartView(this);
            chart.setBackgroundResource(R.drawable.bg_card);
            long[] incomes = new long[6]; int[] counts = new int[6]; String[] labels = new String[6];
            PersianDate cursor = first;
            for (int i = 0; i < 6; i++) {
                PersianDate end = nextMonth(cursor);
                List<Shift> shifts = db.filteredShifts(cursor.atTimeMillis(0,0), end.atTimeMillis(0,0), selected, null, -1);
                incomes[i] = db.monthlyTotal(selected, cursor.year, cursor.month);
                counts[i] = shifts.size();
                labels[i] = PersianDate.MONTHS[cursor.month - 1];
                cursor = end;
            }
            chart.setData(incomes, counts, labels);
            dynamic.addView(chart, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(260)));
        };
        hospitalFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) { refresh.run(); }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
        refresh.run();
        scroll.addView(root);
        return scroll;
    }

    private long shiftIncome(List<Shift> rows) {
        long total = 0;
        for (Shift s : rows) total += db.estimatedAmount(s);
        return total;
    }

    private void showHospitalDialog(Hospital existing) {
        Hospital hospital = existing == null ? new Hospital() : existing;
        LinearLayout form = form();
        EditText name = input("نام بیمارستان", hospital.name, false);
        EditText ward = input("بخش یا توضیح کوتاه", hospital.ward, false);
        EditText base = input("حقوق پایه ماهانه (تومان)", Long.toString(hospital.baseSalary), true);
        EditText day = input("نرخ تقریبی روزکار", Long.toString(hospital.dayRate), true);
        EditText evening = input("نرخ تقریبی عصرکار", Long.toString(hospital.eveningRate), true);
        EditText night = input("نرخ تقریبی شب‌کار", Long.toString(hospital.nightRate), true);
        form.addView(name); form.addView(ward); form.addView(base); form.addView(day); form.addView(evening); form.addView(night);
        AlertDialog dialog = new AlertDialog.Builder(this).setTitle(existing == null ? "بیمارستان تازه" : "ویرایش بیمارستان")
                .setView(form).setNegativeButton("انصراف", null).setPositiveButton("ذخیره", null).create();
        dialog.setOnShowListener(x -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            if (name.getText().toString().trim().isEmpty()) { name.setError("نام بیمارستان لازم است"); return; }
            hospital.name = name.getText().toString().trim();
            hospital.ward = ward.getText().toString().trim();
            hospital.baseSalary = number(base);
            hospital.dayRate = number(day);
            hospital.eveningRate = number(evening);
            hospital.nightRate = number(night);
            db.saveHospital(hospital);
            dialog.dismiss();
            showTab(3);
        }));
        dialog.show();
    }

    private void showShiftDialog(Shift existing, PersianDate presetDate) {
        List<Hospital> hospitals = db.hospitals();
        if (hospitals.isEmpty()) {
            new AlertDialog.Builder(this).setTitle("اول بیمارستان را بساز")
                    .setMessage("برای ثبت شیفت باید حداقل یک بیمارستان همراه نرخ‌های تقریبی داشته باشی.")
                    .setNegativeButton("بعداً", null)
                    .setPositiveButton("ساخت بیمارستان", (d, w) -> showHospitalDialog(null)).show();
            return;
        }
        Shift shift = existing == null ? new Shift() : existing;
        PersianDate date = existing == null ? (presetDate == null ? PersianDate.today() : presetDate) : PersianDate.parse(existing.dateKey);
        LinearLayout form = form();
        Spinner hospital = spinner(hospitalNames(hospitals, false));
        Spinner type = spinner(new String[]{"روزکار", "عصرکار", "شب‌کار"});
        Spinner year = spinner(range(date.year - 1, date.year + 3));
        Spinner month = spinner(PersianDate.MONTHS);
        Spinner day = spinner(days(date.year, date.month));
        year.setSelection(1); month.setSelection(date.month - 1); day.setSelection(date.day - 1);
        if (existing != null) {
            for (int i = 0; i < hospitals.size(); i++) if (hospitals.get(i).id == existing.hospitalId) hospital.setSelection(i);
            type.setSelection(existing.type.equals(Shift.TYPE_EVENING) ? 1 : existing.type.equals(Shift.TYPE_NIGHT) ? 2 : 0);
        }

        LinearLayout dateRow = row(); dateRow.addView(year, weight()); dateRow.addView(month, weight()); dateRow.addView(day, weight());
        Spinner startHour = spinner(range(0, 23)); Spinner startMinute = spinner(new String[]{"۰۰", "۱۵", "۳۰", "۴۵"});
        Spinner endHour = spinner(range(0, 23)); Spinner endMinute = spinner(new String[]{"۰۰", "۱۵", "۳۰", "۴۵"});
        int sh = 7, sm = 0, eh = 15, em = 0;
        if (existing != null) {
            java.time.ZonedDateTime st = Instant.ofEpochMilli(existing.startMillis).atZone(ZoneId.systemDefault());
            java.time.ZonedDateTime et = Instant.ofEpochMilli(existing.endMillis).atZone(ZoneId.systemDefault());
            sh = st.getHour(); sm = st.getMinute(); eh = et.getHour(); em = et.getMinute();
        }
        startHour.setSelection(sh); startMinute.setSelection(sm / 15); endHour.setSelection(eh); endMinute.setSelection(em / 15);
        LinearLayout timeRow = row();
        timeRow.addView(label("از", 14, true)); timeRow.addView(startHour, weight()); timeRow.addView(startMinute, weight());
        timeRow.addView(label("تا", 14, true)); timeRow.addView(endHour, weight()); timeRow.addView(endMinute, weight());
        EditText amount = input("مبلغ این شیفت؛ خالی = نرخ بیمارستان", existing == null || existing.customAmount == 0 ? "" : Long.toString(existing.customAmount), true);
        Spinner reminder = spinner(new String[]{"بدون زنگ", "هنگام شروع", "۳۰ دقیقه قبل", "۶۰ دقیقه قبل", "۱۲۰ دقیقه قبل"});
        if (existing != null && existing.alarmEnabled) reminder.setSelection(existing.reminderMinutes == 0 ? 1 : existing.reminderMinutes == 30 ? 2 : existing.reminderMinutes == 60 ? 3 : 4);
        CheckBox paid = new CheckBox(this); paid.setText("پرداخت شده"); paid.setChecked(existing != null && existing.paid);
        EditText note = input("یادداشت", existing == null ? "" : existing.note, false);
        form.addView(label("بیمارستان", 13, true)); form.addView(hospital);
        form.addView(label("نوع شیفت", 13, true)); form.addView(type);
        form.addView(label("تاریخ شمسی", 13, true)); form.addView(dateRow);
        form.addView(label("ساعت شروع و پایان", 13, true)); form.addView(timeRow);
        form.addView(amount); form.addView(reminder); form.addView(paid); form.addView(note);

        AdapterView.OnItemSelectedListener dateChanged = new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                int y = date.year - 1 + year.getSelectedItemPosition();
                int m = month.getSelectedItemPosition() + 1;
                int old = day.getSelectedItemPosition();
                day.setAdapter(new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_dropdown_item, days(y, m)));
                day.setSelection(Math.min(old, PersianDate.monthLength(y, m) - 1));
            }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        };
        year.setOnItemSelectedListener(dateChanged); month.setOnItemSelectedListener(dateChanged);
        type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                if (existing != null) return;
                if (pos == 0) { startHour.setSelection(7); endHour.setSelection(15); }
                else if (pos == 1) { startHour.setSelection(15); endHour.setSelection(23); }
                else { startHour.setSelection(19); endHour.setSelection(7); }
            }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });
        if (existing == null && pendingQuickType >= 0) {
            int quick = pendingQuickType;
            pendingQuickType = -1;
            type.setSelection(quick);
        }

        ScrollView wrapper = new ScrollView(this); wrapper.addView(form);
        AlertDialog dialog = new AlertDialog.Builder(this).setTitle(existing == null ? "شیفت تازه" : "ویرایش شیفت")
                .setView(wrapper).setNegativeButton("انصراف", null).setPositiveButton("بررسی و ذخیره", null).create();
        dialog.setOnShowListener(x -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            int y = date.year - 1 + year.getSelectedItemPosition();
            int m = month.getSelectedItemPosition() + 1;
            int d = day.getSelectedItemPosition() + 1;
            PersianDate chosen = new PersianDate(y, m, d);
            int startH = startHour.getSelectedItemPosition(), startM = startMinute.getSelectedItemPosition() * 15;
            int endH = endHour.getSelectedItemPosition(), endM = endMinute.getSelectedItemPosition() * 15;
            shift.hospitalId = hospitals.get(hospital.getSelectedItemPosition()).id;
            shift.hospitalName = hospitals.get(hospital.getSelectedItemPosition()).name;
            shift.type = typeCode(type.getSelectedItemPosition() + 1);
            shift.dateKey = chosen.key();
            shift.startMillis = chosen.atTimeMillis(startH, startM);
            shift.endMillis = chosen.atTimeMillis(endH, endM);
            if (shift.endMillis <= shift.startMillis) shift.endMillis = chosen.plusDays(1).atTimeMillis(endH, endM);
            shift.customAmount = number(amount);
            int rp = reminder.getSelectedItemPosition();
            shift.alarmEnabled = rp > 0;
            shift.reminderMinutes = rp <= 1 ? 0 : rp == 2 ? 30 : rp == 3 ? 60 : 120;
            shift.paid = paid.isChecked();
            shift.note = note.getText().toString();
            List<Shift> conflicts = db.conflicts(shift.id, shift.startMillis, shift.endMillis);
            if (!conflicts.isEmpty()) {
                Shift c = conflicts.get(0);
                String message = "این شیفت با «" + c.typeName() + "» در بیمارستان «" + c.hospitalName + "» تداخل دارد.\n\n" +
                        PersianDate.fromMillis(c.startMillis).longText() + "، " + timeRange(c) + "\n\nبا این حال ذخیره شود؟";
                new AlertDialog.Builder(this).setTitle("تداخل دقیق پیدا شد")
                        .setMessage(message).setNegativeButton("اصلاح می‌کنم", null)
                        .setPositiveButton("ذخیره با تداخل", (dd, ww) -> { persistShift(shift); dialog.dismiss(); }).show();
            } else {
                persistShift(shift); dialog.dismiss();
            }
        }));
        dialog.show();
    }

    private void persistShift(Shift shift) {
        shift.id = db.saveShift(shift);
        if (shift.alarmEnabled) ensureExactAlarmPermission();
        AlarmScheduler.schedule(this, shift);
        ShiftWidgetProvider.refresh(this);
        Toast.makeText(this, "شیفت ذخیره شد", Toast.LENGTH_SHORT).show();
        showTab(activeTab);
    }

    private void ensureExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= 31) {
            AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
            if (!am.canScheduleExactAlarms()) {
                try { startActivity(new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:" + getPackageName()))); }
                catch (Exception ignored) {}
            }
        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 41);
        }
    }

    private final class ShiftAdapter extends BaseAdapter {
        private final List<Shift> rows;
        ShiftAdapter(List<Shift> rows) { this.rows = rows; }
        @Override public int getCount() { return rows.size(); }
        @Override public Object getItem(int position) { return rows.get(position); }
        @Override public long getItemId(int position) { return rows.get(position).id; }
        @Override public View getView(int position, View convert, ViewGroup parent) {
            View view = convert == null ? LayoutInflater.from(MainActivity.this).inflate(R.layout.item_shift, parent, false) : convert;
            Shift shift = rows.get(position);
            PersianDate date = PersianDate.fromMillis(shift.startMillis);
            boolean newDay = position == 0 || !rows.get(position - 1).dateKey.equals(shift.dateKey);
            String holiday = HolidayRepository.title(date);
            ((TextView) view.findViewById(R.id.shiftType)).setText((newDay ? date.longText() + (holiday == null ? "" : " • " + holiday) + "\n" : "") + shift.typeName());
            ((TextView) view.findViewById(R.id.shiftAmount)).setText(Fa.money(db.estimatedAmount(shift)));
            ((TextView) view.findViewById(R.id.shiftDetails)).setText(shift.hospitalName + " • " + timeRange(shift) + (shift.paid ? " • پرداخت‌شده" : " • پرداخت‌نشده"));
            ((TextView) view.findViewById(R.id.shiftReminder)).setText(shift.alarmEnabled ? "زنگ: " + (shift.reminderMinutes == 0 ? "هنگام شروع" : Fa.n(shift.reminderMinutes) + " دقیقه قبل") : "بدون زنگ");
            return view;
        }
    }

    private String timeRange(Shift shift) {
        java.time.ZonedDateTime start = Instant.ofEpochMilli(shift.startMillis).atZone(ZoneId.systemDefault());
        java.time.ZonedDateTime end = Instant.ofEpochMilli(shift.endMillis).atZone(ZoneId.systemDefault());
        String text = "ساعت " + Fa.time(start.getHour(), start.getMinute()) + " تا " + Fa.time(end.getHour(), end.getMinute());
        if (!start.toLocalDate().equals(end.toLocalDate())) text += " روز بعد";
        return text;
    }

    private LinearLayout column() { LinearLayout v = new LinearLayout(this); v.setOrientation(LinearLayout.VERTICAL); v.setLayoutDirection(View.LAYOUT_DIRECTION_RTL); return v; }
    private LinearLayout row() { LinearLayout v = new LinearLayout(this); v.setOrientation(LinearLayout.HORIZONTAL); v.setGravity(Gravity.CENTER_VERTICAL); v.setLayoutDirection(View.LAYOUT_DIRECTION_RTL); return v; }
    private LinearLayout form() { LinearLayout v = column(); v.setPadding(dp(20), dp(8), dp(20), dp(18)); return v; }
    private TextView heading(String text, int size) { return label(text, size, true); }
    private TextView label(String text, int size, boolean bold) { TextView v = new TextView(this); v.setText(text); v.setTextSize(size); v.setTextColor(getColor(R.color.ink)); v.setPadding(dp(5), dp(6), dp(5), dp(6)); if (bold) v.setTypeface(Typeface.DEFAULT, Typeface.BOLD); return v; }
    private TextView kicker(String text) { TextView v = label(text, 12, true); v.setTextColor(getColor(R.color.raspberry)); return v; }
    private TextView muted(String text) { TextView v = label(text, 13, false); v.setTextColor(getColor(R.color.muted)); return v; }
    private TextView empty(String text) { TextView v = muted(text); v.setGravity(Gravity.CENTER); v.setPadding(dp(8), dp(25), dp(8), dp(25)); return v; }
    private LinearLayout card() { LinearLayout v = column(); v.setBackgroundResource(R.drawable.bg_card); v.setPadding(dp(16), dp(14), dp(16), dp(16)); v.setElevation(dp(3)); return v; }
    private Button shortcut(String text, int background, int color) { Button b = new Button(this); b.setText(text); b.setTextSize(14); b.setTextColor(getColor(color)); b.setTypeface(Typeface.DEFAULT, Typeface.BOLD); b.setAllCaps(false); b.setGravity(Gravity.CENTER); b.setMinWidth(0); b.setMinHeight(0); b.setBackgroundResource(background); LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, dp(102), 1); p.setMargins(dp(4), 0, dp(4), 0); b.setLayoutParams(p); return b; }
    private Button smallButton(String text) { Button b = new Button(this); b.setText(text); b.setTextSize(12); b.setTextColor(getColor(R.color.ink)); b.setMinHeight(0); b.setMinWidth(0); b.setAllCaps(false); b.setBackgroundResource(R.drawable.bg_chip); return b; }
    private Spinner spinner(String[] values) { Spinner s = new Spinner(this); s.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, values)); s.setPadding(dp(8), 0, dp(8), 0); s.setBackgroundResource(R.drawable.bg_input); return s; }
    private EditText input(String hint, String value, boolean number) { EditText e = new EditText(this); e.setHint(hint); e.setText(value); e.setTextSize(15); e.setSingleLine(); e.setTextColor(getColor(R.color.ink)); e.setHintTextColor(getColor(R.color.muted)); e.setBackgroundResource(R.drawable.bg_input); if (number) e.setInputType(InputType.TYPE_CLASS_NUMBER); LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(55)); p.setMargins(0, dp(5), 0, dp(6)); e.setLayoutParams(p); return e; }
    private LinearLayout.LayoutParams weight() { return new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1); }
    private LinearLayout.LayoutParams weightHeight(int height) { LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, height, 1); p.setMargins(dp(4), 0, dp(4), 0); return p; }
    private LinearLayout.LayoutParams marginTop(int dp) { LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT); p.setMargins(0, dp(dp), 0, 0); return p; }
    private GridLayout.LayoutParams gridCell() { GridLayout.LayoutParams p = new GridLayout.LayoutParams(); p.width = 0; p.height = dp(68); p.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f); p.setMargins(dp(2), dp(2), dp(2), dp(2)); return p; }
    private int dp(int v) { return Math.round(v * getResources().getDisplayMetrics().density); }
    private String monthTitle() { return PersianDate.MONTHS[viewMonth - 1] + " " + Fa.n(viewYear); }
    private void moveMonth(int delta) { viewMonth += delta; if (viewMonth == 13) { viewMonth = 1; viewYear++; } else if (viewMonth == 0) { viewMonth = 12; viewYear--; } }
    private PersianDate nextMonth(PersianDate date) { return date.month == 12 ? new PersianDate(date.year + 1, 1, 1) : new PersianDate(date.year, date.month + 1, 1); }
    private String typeCode(int position) { if (position == 1) return Shift.TYPE_DAY; if (position == 2) return Shift.TYPE_EVENING; if (position == 3) return Shift.TYPE_NIGHT; return null; }
    private String[] hospitalNames(List<Hospital> hospitals, boolean all) { ArrayList<String> names = new ArrayList<>(); if (all) names.add("همه بیمارستان‌ها"); for (Hospital h : hospitals) names.add(h.name); return names.toArray(new String[0]); }
    private String[] range(int first, int last) { String[] r = new String[last - first + 1]; for (int i = 0; i < r.length; i++) r[i] = Fa.n(first + i); return r; }
    private String[] days(int year, int month) { return range(1, PersianDate.monthLength(year, month)); }
    private long number(EditText input) { String s = input.getText().toString().trim().replace(",", "").replace("٬", ""); s = s.replace('۰','0').replace('۱','1').replace('۲','2').replace('۳','3').replace('۴','4').replace('۵','5').replace('۶','6').replace('۷','7').replace('۸','8').replace('۹','9'); try { return s.isEmpty() ? 0 : Long.parseLong(s); } catch (Exception e) { return 0; } }
}
