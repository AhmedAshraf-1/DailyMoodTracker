package com.dailymoodtracker.service;

import com.dailymoodtracker.model.Reminder;
import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.time.LocalTime;
import java.time.LocalDateTime;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;
import java.util.logging.Level;

public class ReminderService {
    private static final Logger LOGGER = Logger.getLogger(ReminderService.class.getName());
    private final List<Reminder> reminders = new CopyOnWriteArrayList<>();
    private final Timer timer;

    public ReminderService() {
        this.timer = new Timer(true); // Daemon timer
        startReminderChecker();
    }

    private void startReminderChecker() {
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                checkReminders();
            }
        }, 0, 60000); // Check every minute
    }

    private void checkReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalTime currentTime = now.toLocalTime();
        
        for (Reminder reminder : reminders) {
            if (reminder.isEnabled() && shouldTrigger(reminder, now)) {
                triggerReminder(reminder);
                updateReminderAfterTrigger(reminder);
            }
        }
    }

    private boolean shouldTrigger(Reminder reminder, LocalDateTime now) {
        if (reminder.isRead()) return false;

        LocalTime reminderTime = reminder.getTime();
        LocalTime currentTime = now.toLocalTime();
        
        if (reminderTime.getHour() != currentTime.getHour() || 
            reminderTime.getMinute() != currentTime.getMinute()) {
            return false;
        }

        switch (reminder.getFrequency().toLowerCase()) {
            case "daily":
                return true;
            case "weekly":
                // Check if today is the correct day of the week
                return now.getDayOfWeek() == DayOfWeek.MONDAY; // You can adjust this to the desired day
            case "custom":
                // Custom frequency logic can be implemented here
                return true;
            default:
                return false;
        }
    }

    private void triggerReminder(Reminder reminder) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Reminder");
            alert.setHeaderText(null);
            alert.setContentText(reminder.getMessage());
            alert.show();
        });
    }

    private void updateReminderAfterTrigger(Reminder reminder) {
        reminder.setRead(true);
        // For daily reminders, reset the read flag after the day changes
        if ("daily".equalsIgnoreCase(reminder.getFrequency())) {
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    reminder.setRead(false);
                }
            }, 24 * 60 * 60 * 1000); // Reset after 24 hours
        }
    }

    public void createReminder(Reminder reminder) {
        reminders.add(reminder);
        LOGGER.log(Level.INFO, "Created reminder: {0}", reminder.getMessage());
    }

    public void deleteReminder(Reminder reminder) {
        reminders.remove(reminder);
        LOGGER.log(Level.INFO, "Deleted reminder: {0}", reminder.getMessage());
    }

    public List<Reminder> getUserReminders(int userId) {
        return reminders.stream()
                .filter(r -> r.getUserId() == userId)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    public void updateReminder(Reminder reminder) {
        int index = -1;
        for (int i = 0; i < reminders.size(); i++) {
            if (reminders.get(i).getReminderId() == reminder.getReminderId()) {
                index = i;
                break;
            }
        }
        if (index != -1) {
            reminders.set(index, reminder);
            LOGGER.log(Level.INFO, "Updated reminder: {0}", reminder.getMessage());
        }
    }

    // Method to clean up resources
    public void shutdown() {
        if (timer != null) {
            timer.cancel();
            LOGGER.log(Level.INFO, "ReminderService shutdown completed");
        }
    }
} 