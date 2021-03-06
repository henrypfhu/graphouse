package ru.yandex.market.graphouse;

import ru.yandex.market.graphouse.search.tree.MetricDescription;

import java.util.Date;

/**
 * @author Dmitry Andreev <a href="mailto:AndreevDm@yandex-team.ru"></a>
 * @date 06/04/15
 */
public class Metric {
    private final MetricDescription metricDescription;
    private final Date time;
    private final double value;
    private final int updated;

    public Metric(MetricDescription metricDescription, Date time, double value, int updated) {
        this.metricDescription = metricDescription;
        this.time = time;
        this.value = value;
        this.updated = updated;
    }

    public MetricDescription getMetricDescription() {
        return metricDescription;
    }

    public Date getTime() {
        return time;
    }

    public int getTimeSeconds() {
        return (int) (time.getTime() / 1000);
    }

    public double getValue() {
        return value;
    }

    public int getUpdated() {
        return updated;
    }
}
