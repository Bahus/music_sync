package ru.bahusoff.tests;

import ru.bahusoff.console.ProgressReporter;
import ru.bahusoff.console.Reportable;


public class SimpleTask implements Runnable, Reportable {

    private final String title;
    private final int timeout;

    public SimpleTask(String title, int timeout) {
        this.title = title;
        this.timeout = timeout;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void run() {
        ProgressReporter reporter = ProgressReporter.getInstance();

        for (int i = 0; i < 10; i++) {
            try {
                Thread.sleep(timeout);
                reporter.drawProgress(this, i * 10 + 2);
            } catch (InterruptedException e) {
                reporter.setTaskFailed(this);
                e.printStackTrace();
            }
        }

        if (timeout == 200) {
            reporter.setTaskFailed(this);
        } else {
            reporter.setTaskCompleted(this);
        }
    }
}
