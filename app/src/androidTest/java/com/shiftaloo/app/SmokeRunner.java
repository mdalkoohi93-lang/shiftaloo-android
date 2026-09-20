package com.shiftaloo.app;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.EditText;
import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;

/** Runs only in the test APK on an isolated emulator. No demo data in the app. */
public final class SmokeRunner extends Instrumentation {
  MainActivity activity;
  int checks = 0;
  Bundle args;

  @Override
  public void onCreate(Bundle b) {
    super.onCreate(b);
    args = b;
    start();
  }

  void check(boolean value, String why) {
    checks++;
    if (!value) throw new AssertionError(why);
  }

  @Override
  public void onStart() {
    Bundle result = new Bundle();
    try {
      // Connect before launching the activity so window-focus events are observed.
      android.accessibilityservice.AccessibilityServiceInfo automationInfo = getUiAutomation().getServiceInfo();
      automationInfo.flags |= android.accessibilityservice.AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS;
      getUiAutomation().setServiceInfo(automationInfo);
      if ("large".equals(args.getString("mode"))) {
        activity =
            (MainActivity)
                startActivitySync(
                    new Intent(getTargetContext(), MainActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        capture("08-large-font-home");
        runOnMainSync(() -> activity.showTab(1));
        capture("09-large-font-calendar");
        runOnMainSync(() -> activity.editShift(null, PersianDate.today().plusDays(7), 0));
        capture("10-large-font-form");
        runOnMainSync(
            () -> {
              View button =
                  activity.activeSheet.getWindow().getDecorView().findViewWithTag("saveShift");
              button.requestRectangleOnScreen(
                  new android.graphics.Rect(0, 0, button.getWidth(), button.getHeight()), true);
            });
        capture("11-large-font-save");
        runOnMainSync(
            () -> {
              View button =
                  activity.activeSheet.getWindow().getDecorView().findViewWithTag("saveShift");
              android.graphics.Rect bounds = new android.graphics.Rect();
              check(
                  button.getGlobalVisibleRect(bounds) && bounds.height() >= button.getHeight() * .9,
                  "save reachable with large font");
            });
        result.putString("stream", "SHIFTALOO_OK large-font\n");
        finish(Activity.RESULT_OK, result);
        return;
      }
      getTargetContext().deleteDatabase("shiftaloo.db");
      activity =
          (MainActivity)
              startActivitySync(
                  new Intent(getTargetContext(), MainActivity.class)
                      .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
      capture("00-empty");
      check(PersianDate.fromGregorian(2026, 9, 19).key().equals("1405-06-28"), "Jalali conversion");
      check(new PersianDate(1405, 1, 1).toGregorian().equals(LocalDate.of(2026, 3, 21)), "Nowruz");
      check(
          PersianDate.monthLength(1403, 12) == 30 && PersianDate.monthLength(1404, 12) == 29,
          "leap years");
      for (int y = 1390; y <= 1420; y++)
        for (int m = 1; m <= 12; m++) {
          PersianDate p = new PersianDate(y, m, PersianDate.monthLength(y, m));
          LocalDate g = p.toGregorian();
          check(
              PersianDate.fromGregorian(g.getYear(), g.getMonthValue(), g.getDayOfMonth())
                  .equals(p),
              "round trip");
        }
      Hospital a = new Hospital();
      a.name = "بیمارستان امام خمینی";
      a.ward = "اورژانس";
      a.baseSalary = 12000000;
      a.dayRate = 2400000;
      a.nightRate = 3200000;
      a.id = activity.db.saveHospital(a);
      check(a.rateFor(Shift.TYPE_EVENING) == 2800000, "fallback mean");
      check(new Hospital().rateFor(Shift.TYPE_DAY) == 0, "no invented fee");
      Hospital b = new Hospital();
      b.name = "بیمارستان شهید بهشتی";
      b.dayRate = 2200000;
      b.baseSalary = 8000000;
      b.id = activity.db.saveHospital(b);
      Hospital c = new Hospital();
      c.name = "بیمارستان میلاد";
      c.dayRate = 2600000;
      c.id = activity.db.saveHospital(c);
      PersianDate today = PersianDate.today(), date = today.plusDays(1);
      Shift day = shift(a.id, date, Shift.TYPE_DAY, 7, 15, false);
      Shift evening = shift(b.id, date, Shift.TYPE_EVENING, 15, 23, true);
      Shift night = shift(c.id, date.plusDays(1), Shift.TYPE_NIGHT, 23, 7, false);
      check(
          activity.db.conflicts(0, date.atTimeMillis(14, 0), date.atTimeMillis(16, 0)).size() == 2,
          "all overlaps");
      check(
          activity.db.conflicts(day.id, day.startMillis, day.endMillis).isEmpty(),
          "self excluded and adjacent accepted");
      check(
          activity
                  .db
                  .conflicts(
                      0, date.plusDays(2).atTimeMillis(6, 30), date.plusDays(2).atTimeMillis(8, 0))
                  .size()
              == 1,
          "overnight overlap");
      PersianDate first = new PersianDate(date.year, date.month, 1),
          next = MainActivity.nextMonth(first);
      check(
          activity
                  .db
                  .filteredShifts(
                      first.atTimeMillis(0, 0),
                      next.atTimeMillis(0, 0),
                      b.id,
                      Shift.TYPE_EVENING,
                      1)
                  .size()
              == 1,
          "combined filters");
      check(
          activity.db.monthlyTotal(a.id, date.year, date.month) == 14400000,
          "base salary plus shifts");
      day.alarmEnabled = true;
      day.reminderMinutes = 30;
      activity.db.saveShift(day);
      AlarmScheduler.schedule(activity, day);
      android.app.AlarmManager alarms =
          (android.app.AlarmManager)
              activity.getSystemService(android.content.Context.ALARM_SERVICE);
      check(
          alarms.getNextAlarmClock() != null
              && alarms.getNextAlarmClock().getTriggerTime() == day.startMillis - 1800000L,
          "exact alarm scheduled");
      AlarmScheduler.cancel(activity, day.id);
      check(alarms.getNextAlarmClock() == null, "alarm cancelled");
      day.alarmEnabled = false;
      activity.db.saveShift(day);
      activity
          .getSharedPreferences("widgets", 0)
          .edit()
          .putLong("73hospital", b.id)
          .putString("73type", Shift.TYPE_EVENING)
          .commit();
      ShiftWidgetFactory widget = new ShiftWidgetFactory(activity, 73);
      widget.onCreate();
      check(widget.getCount() == 1, "widget combined filters");
      check(widget.getViewAt(0) != null, "widget native row");
      widget.onDestroy();
      for (int i = 1; i < 6; i++) {
        first = MainActivity.nextMonth(first);
        for (int j = 0; j < i + 1; j++)
          shift(
              a.id,
              first.plusDays(j * 2),
              j % 2 == 0 ? Shift.TYPE_DAY : Shift.TYPE_NIGHT,
              j % 2 == 0 ? 7 : 23,
              j % 2 == 0 ? 15 : 7,
              false);
      }
      runOnMainSync(
          () -> {
            activity.year = date.year;
            activity.month = date.month;
            activity.showTab(0);
          });
      capture("01-home");
      runOnMainSync(
          () -> {
            activity.selected = date;
            activity.root.findViewWithTag("tab1").performClick();
          });
      capture("02-calendar");
      check(activity.tab == 1, "calendar navigation");
      runOnMainSync(() -> activity.root.findViewWithTag("tab2").performClick());
      capture("03-reports");
      check(activity.tab == 2, "report navigation");
      runOnMainSync(
          () -> {
            activity.showTab(0);
            activity.editShift(null, new PersianDate(date.year + 1, 1, 10), 0);
          });
      capture("04-new-shift");
      runOnMainSync(
          () -> {
            View decor = activity.activeSheet.getWindow().getDecorView();
            decor.findViewWithTag("type2").performClick();
            ((EditText) decor.findViewWithTag("shiftAmount")).setText("۳۰۰۰۰۰۰");
            ((EditText) decor.findViewWithTag("shiftNote")).setText("آزمون ثبت شیفت");
            decor.findViewWithTag("saveShift").performClick();
          });
      waitForIdleSync();
      check(activity.activeSheet == null, "shift saved by actual button");
      Shift saved = null;
      for (Shift s : activity.db.allShifts()) if (s.note.equals("آزمون ثبت شیفت")) saved = s;
      check(saved != null, "persisted UI draft");
      check(saved.endMillis - saved.startMillis == 8 * 3600000L, "night default 8 hours");
      check(saved.customAmount == 3000000, "Persian number entry");
      final Shift edit = saved;
      runOnMainSync(
          () -> {
            activity.editShift(edit, null, 0);
            ((EditText)
                    activity.activeSheet.getWindow().getDecorView().findViewWithTag("shiftNote"))
                .setText("ویرایش موفق");
            activity
                .activeSheet
                .getWindow()
                .getDecorView()
                .findViewWithTag("saveShift")
                .performClick();
          });
      check(activity.db.shift(edit.id).note.equals("ویرایش موفق"), "edit saved");
      runOnMainSync(() -> activity.showTab(3));
      capture("05-hospitals");
      runOnMainSync(() -> activity.filters());
      capture("06-filters");
      runOnMainSync(() -> activity.activeSheet.dismiss());
      runOnMainSync(() -> activity.pickDate(date, d -> {}));
      capture("07-jalali-picker");
      getUiAutomation()
          .performGlobalAction(
              android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK);
      SystemClock.sleep(400);
      runOnMainSync(() -> activity.showTab(0));
      check(
          getTargetContext().checkSelfPermission("android.permission.INTERNET")
              != android.content.pm.PackageManager.PERMISSION_GRANTED,
          "no internet permission");
      result.putString("stream", "SHIFTALOO_OK checks=" + checks + "\n");
      finish(Activity.RESULT_OK, result);
    } catch (Throwable t) {
      result.putString("stream", "SHIFTALOO_FAILED " + android.util.Log.getStackTraceString(t));
      finish(Activity.RESULT_CANCELED, result);
    }
  }

  Shift shift(long hospital, PersianDate day, String type, int from, int to, boolean paid) {
    Shift s = new Shift();
    s.hospitalId = hospital;
    s.dateKey = day.key();
    s.type = type;
    s.startMillis = day.atTimeMillis(from, 0);
    s.endMillis = (to < from ? day.plusDays(1) : day).atTimeMillis(to, 0);
    s.paid = paid;
    s.id = activity.db.saveShift(s);
    return s;
  }

  void capture(String name) throws Exception {
    waitForIdleSync();
    SystemClock.sleep(900);
    // An occasional emulator Pixel Launcher ANR is unrelated to the target app.
    // Close only that named launcher dialog; never suppress a Shiftaloo failure.
    android.view.accessibility.AccessibilityNodeInfo root =
        getUiAutomation().getRootInActiveWindow();
    if (root != null && !root.findAccessibilityNodeInfosByText("Pixel Launcher").isEmpty()) {
      for (android.view.accessibility.AccessibilityNodeInfo node :
          root.findAccessibilityNodeInfosByText("Close app")) {
        android.view.accessibility.AccessibilityNodeInfo clickable = node;
        while (clickable != null && !clickable.isClickable()) clickable = clickable.getParent();
        if (clickable != null)
          clickable.performAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_CLICK);
      }
      SystemClock.sleep(1200);
      root = getUiAutomation().getRootInActiveWindow();
    }
    Bitmap image = getUiAutomation().takeScreenshot();
    check(image != null, "screenshot " + name);
    File directory = new File(getTargetContext().getExternalFilesDir(null), "screenshots");
    directory.mkdirs();
    try (FileOutputStream out = new FileOutputStream(new File(directory, name + ".png"))) {
      image.compress(Bitmap.CompressFormat.PNG, 100, out);
    }
    image.recycle();
    // Preserve the actual image even if a system overlay fails the capture gate.
    if (root != null) check("com.shiftaloo.app".contentEquals(root.getPackageName()),
        "unexpected overlay package " + root.getPackageName() + ": " + name);
  }
}
