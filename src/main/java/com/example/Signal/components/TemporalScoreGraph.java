package com.example.Signal.components;

import com.example.Signal.components.charts.EChartsScatterChartBuilder;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

public class TemporalScoreGraph extends VerticalLayout {

    public TemporalScoreGraph(String personName,
                              Map<LocalDate, Integer> scoresOverTime,
                              LocalDate globalStartDate,
                              LocalDate globalEndDate,
                              int rollingAverageWindow) {
        addClassName("temporal-score-graph");
        setPadding(true);
        setSpacing(true);
        setWidth("600px");

        if (scoresOverTime.isEmpty()) {
            add(new Span("No data available for " + personName));
            return;
        }

        add(createStatsSpan(personName, scoresOverTime));
        add(createChartDiv(scoresOverTime, globalStartDate, globalEndDate, rollingAverageWindow));
    }

    private Span createStatsSpan(String personName, Map<LocalDate, Integer> scoresOverTime) {
        double avgScore = scoresOverTime.values()
                .stream()
                .filter(s -> s != 7)
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0);

        Span statsSpan = new Span(String.format("%s - Games: %d | Avg: %.2f",
                personName,
                scoresOverTime.size(),
                avgScore));
        statsSpan.getStyle()
                .set("font-weight", "600")
                .set("margin-bottom", "8px");
        return statsSpan;
    }

    private Div createChartDiv(Map<LocalDate, Integer> scoresOverTime,
                               LocalDate globalStartDate,
                               LocalDate globalEndDate,
                               int rollingAverageWindow) {
        Div chartDiv = new Div();
        chartDiv.setId("chart-" + UUID.randomUUID());
        chartDiv.setWidth("100%");
        chartDiv.setHeight("300px");

        EChartsScatterChartBuilder chartBuilder = new EChartsScatterChartBuilder(
                chartDiv.getId().orElseThrow(),
                scoresOverTime,
                globalStartDate,
                globalEndDate,
                rollingAverageWindow
        );

        chartDiv.getElement().executeJs(chartBuilder.buildScript());
        return chartDiv;
    }
}
