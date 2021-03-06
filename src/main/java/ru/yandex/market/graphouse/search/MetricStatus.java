package ru.yandex.market.graphouse.search;

import java.util.*;

/**
 * @author Dmitry Andreev <a href="mailto:AndreevDm@yandex-team.ru"></a>
 * @date 08/06/15
 */
public enum MetricStatus {
    /**
     * Статус по умолчанию при создании директории/метрики.
     */
    SIMPLE,
    /**
     * Если директория (метрика) забанена, то
     * - директория и все метрики в ней (метрика) перестаёт находиться в поиске (а следовательно и в графите)
     * - значения метрик в директории (метрики) перестают приниматься и писаться в графит
     * <p/
     * Чтобы открыть деректорию(метрику), необходимо явно перевести в {@link #APPROVED}
     */
    BAN,
    APPROVED,
    /**
     * Если директория(метрика) скрыта, то
     * - директория и все метрики в ней (метрика) перестаёт находиться в поиске (а следовательно и в графите)
     * - как только появится новое значение метрика и все родительские директории будут открыты
     * <p/>
     */
    HIDDEN,
    /**
     * Директория автоматически скрывается, если все её дочерние директории и метрики не видимы {@link #visible}
     * Как только появится новое значение для дочерней метрики, директория будет открыта {@link #SIMPLE}
     * <p/>
     * Метрика может быть автоматически скрыта в {@link ru.yandex.market.graphouse.AutoHideService}
     * Аналогично, при появлении новых значений будет открыта {@link #SIMPLE}
     */
    AUTO_HIDDEN;


    public static final Map<MetricStatus, List<MetricStatus>> RESTRICTED_GRAPH_EDGES = new EnumMap<>(
        MetricStatus.class
    );

    static {
        RESTRICTED_GRAPH_EDGES.put(MetricStatus.BAN, Arrays.asList(MetricStatus.SIMPLE, MetricStatus.AUTO_HIDDEN));
        RESTRICTED_GRAPH_EDGES.put(MetricStatus.HIDDEN, Collections.singletonList(MetricStatus.AUTO_HIDDEN));
        RESTRICTED_GRAPH_EDGES.put(MetricStatus.APPROVED, Arrays.asList(MetricStatus.SIMPLE, MetricStatus.AUTO_HIDDEN));
    }

    /**
     * Если <code>false</code>, то в поиске не будет отдаваться данная метрика (ни одна метрика из данной директории).
     */
    public boolean visible() {
        switch (this) {
            case SIMPLE:
            case APPROVED:
                return true;
            case BAN:
            case HIDDEN:
            case AUTO_HIDDEN:
                return false;
            default:
                throw new IllegalStateException();
        }
    }

    public boolean handmade() {
        switch (this) {
            case APPROVED:
            case BAN:
            case HIDDEN:
                return true;
            case SIMPLE:
            case AUTO_HIDDEN:
                return false;
            default:
                throw new IllegalStateException();
        }
    }
}
