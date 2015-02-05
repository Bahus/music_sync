package ru.bahusoff.console;

import org.fusesource.jansi.Ansi;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Draws progress bar for multiple tasks
 *
 * All console related methods must be synchronized to work properly and
 * avoid graphical glitches
 */
public class ProgressReporter {
    private static ProgressReporter instance = null;
    private ConcurrentMap<Reportable, TaskInfo> tasks = null;
    private final int capacity;

    private static final char barStart = '[';
    private static final char barEnd = ']';
    private static final char barProgress = '#';
    private static final char barEmpty = '-';

    public static byte PROGRESS_LENGTH = 10;

    private ProgressReporter(int capacity) {
        this.tasks = new ConcurrentHashMap<Reportable, TaskInfo>(capacity);
        this.capacity = capacity;
    }

    public static ProgressReporter getInstance(int capacity) {
        if (instance != null) {
            return instance;
        }

        instance = new ProgressReporter(capacity);
        instance.init();
        return instance;
    }

    public static ProgressReporter getInstance() {
        return instance;
    }

    private synchronized void init() {

        Console.println(
            Ansi.ansi()
                .cursorLeft(1000)
                .format("Synchronizing %d track(s).", capacity)
        );

        Console.print(Ansi.ansi().saveCursorPosition());

    }

    public void drawProgress(Reportable task, int percent) {
        assert (percent >= 0 && percent <= 100) : "Percent should be between 0 and 100";

        TaskInfo taskInfo = getTaskInfo(task);
        byte current_position = getPosition(percent);

        // do not re-draw progressbar, since it was not changed
        if (current_position == getPosition(taskInfo.getPercent()) & percent > 0) {
            return;
        } else {
            taskInfo.setPercent(percent);
        }

        drawProgressBar(taskInfo, getProgressBar(percent));
    }

    private synchronized void drawProgressBar(TaskInfo taskInfo, String progress) {
        int offset = taskInfo.getNumber();

        Console.print(Ansi.ansi().restorCursorPosition());

        Console.print(
            Ansi.ansi()
                .cursorDown(offset)
                .fgBright(Ansi.Color.GREEN)
                .format("%3d.", taskInfo.getNumber())
                .reset()
                .a(" ").fg(Ansi.Color.WHITE)
                .a(progress)
                .reset()
                .a(" ")
                .a(taskInfo.getTitle())
        );
    }

    private byte getPosition(int percent) {
        return (byte) (percent * PROGRESS_LENGTH / 100);
    }

    private String getProgressBarFromPosition(int position) {

        StringBuilder bar = new StringBuilder(2 + PROGRESS_LENGTH);
        bar.append(barStart);

        for (int i = 0; i < PROGRESS_LENGTH; i++) {
            if (i < position | position == PROGRESS_LENGTH) {
                bar.append(barProgress);
            } else {
                bar.append(barEmpty);
            }
        }

        bar.append(barEnd);
        return bar.toString();
    }

    private String getProgressBar(int percent) {
        return getProgressBarFromPosition(getPosition(percent));
    }

    private TaskInfo getTaskInfo(Reportable task) {
        if (tasks.containsKey(task)) {
            return tasks.get(task);
        }

        return registerTask(task);
    }

    public synchronized void setTaskCompleted(Reportable task) {
        drawProgress(task, 100);
    }

    public synchronized void setTaskFailed(Reportable task) {
        TaskInfo taskInfo = getTaskInfo(task);

        Ansi failed_progress = (
            Ansi.ansi()
                .fgBright(Ansi.Color.RED)
                .a(getProgressBar(taskInfo.getPercent()))
                .reset()
        );

        drawProgressBar(taskInfo, failed_progress.toString());
    }

    public synchronized void close() {
        tasks.clear();

        Console.print(Ansi.ansi().restorCursorPosition());
        Console.print(
            Ansi.ansi()
                .cursorDown(capacity)
                .newline()
                .newline()
                .reset()
        );

        instance = null;
    }

    public synchronized TaskInfo registerTask(Reportable task) {
        if (tasks.containsKey(task)) {
            return tasks.get(task);
        }

        Number new_count = tasks.size() + 1;
        String title = task.getTitle();

        TaskInfo info = new TaskInfo(title, 0, new_count.intValue());

        tasks.put(task, info);
        drawProgress(task, 0);

        return info;
    }

}


class TaskInfo {

    private final String title;
    private int percent;
    private final int number;

    TaskInfo(String title, int percent, int number) {
        this.title = title;
        this.percent = percent;
        this.number = number;
    }

    public String getTitle() {
        return title;
    }

    public int getPercent() {
        return percent;
    }

    public void setPercent(int percent) {
        this.percent = percent;
    }

    public int getNumber() {
        return number;
    }
}