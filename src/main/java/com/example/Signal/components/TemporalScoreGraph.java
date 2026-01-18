package com.example.Signal.components;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TemporalScoreGraph extends VerticalLayout {

    private static final Map<Integer, String> SCORE_COLORS = Map.of(1,
                                                                    "#00d66a",
                                                                    2,
                                                                    "#4cd964",
                                                                    3,
                                                                    "#a8e063",
                                                                    4,
                                                                    "#ffc857",
                                                                    5,
                                                                    "#ff9a3c",
                                                                    6,
                                                                    "#ff6b6b",
                                                                    7,
                                                                    "#c92a2a");

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    public TemporalScoreGraph(String personName,
                              Map<LocalDate, Integer> scoresOverTime,
                              LocalDate globalStartDate,
                              LocalDate globalEndDate) {
        addClassName("temporal-score-graph");
        setPadding(true);
        setSpacing(true);
        setWidth("600px");

        if (scoresOverTime.isEmpty()) {
            Span noData = new Span("No data available for " + personName);
            add(noData);
            return;
        }

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
        statsSpan.getStyle().set("font-weight", "600").set("margin-bottom", "8px");
        add(statsSpan);

        Div chartDiv = new Div();
        chartDiv.setId("chart-" + UUID.randomUUID());
        chartDiv.setWidth("100%");
        chartDiv.setHeight("300px");
        add(chartDiv);

        List<Map.Entry<LocalDate, Integer>> sortedData = scoresOverTime.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .toList();

        StringBuilder dataPoints = new StringBuilder("[");
        StringBuilder colors = new StringBuilder("[");

        for (int i = 0; i < sortedData.size(); i++) {
            Map.Entry<LocalDate, Integer> entry = sortedData.get(i);
            LocalDate date = entry.getKey();
            int score = entry.getValue();

            if (i > 0) {
                dataPoints.append(",");
                colors.append(",");
            }

            dataPoints.append(String.format("['%s', %d]", date.toString(), score));
            colors.append(String.format("'%s'", SCORE_COLORS.get(score)));
        }

        dataPoints.append("]");
        colors.append("]");

        String script = String.format("""
                                              (function() {
                                                  const chartDom = document.getElementById('%s');
                                                  if (!chartDom) return;
                                              
                                                  const myChart = echarts.init(chartDom);
                                                  const dataPoints = %s;
                                                  const colors = %s;
                                              
                                                  const option = {
                                                      tooltip: {
                                                          trigger: 'item',
                                                          formatter: function(params) {
                                                              const score = params.value[1] === 7 ? 'X' : params.value[1];
                                                              const date = new Date(params.value[0]).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
                                                              return date + '<br/>Score: ' + score;
                                                          }
                                                      },
                                                      grid: {
                                                          left: '50px',
                                                          right: '20px',
                                                          top: '20px',
                                                          bottom: '50px',
                                                          containLabel: true
                                                      },
                                                      xAxis: {
                                                          type: 'time',
                                                          min: '%s',
                                                          max: '%s',
                                                          splitNumber: 5,
                                                          axisLine: {
                                                              show: true
                                                          },
                                                          axisTick: {
                                                              show: true
                                                          },
                                                          axisLabel: {
                                                              show: true,
                                                              formatter: function(value) {
                                                                  const date = new Date(value);
                                                                  const month = date.toLocaleDateString('en-US', { month: 'short' });
                                                                  const day = date.getDate();
                                                                  return month + ' ' + day;
                                                              },
                                                              interval: 0,
                                                              fontSize: 11,
                                                              color: '#666'
                                                          }
                                                      },
                                                      yAxis: {
                                                          type: 'value',
                                                          inverse: true,
                                                          min: 1,
                                                          max: 7,
                                                          interval: 1,
                                                          splitLine: {
                                                              show: true,
                                                              lineStyle: {
                                                                  color: '#e0e0e0'
                                                              }
                                                          },
                                                          axisLabel: {
                                                              formatter: function(value) {
                                                                  return value === 7 ? 'X' : value.toString();
                                                              }
                                                          }
                                                      },
                                                      series: [{
                                                          name: 'Score',
                                                          type: 'scatter',
                                                          data: dataPoints,
                                                          itemStyle: {
                                                              color: function(params) {
                                                                  return colors[params.dataIndex];
                                                              }
                                                          },
                                                          symbolSize: 10
                                                      }]
                                                  };
                                              
                                                  myChart.setOption(option);
                                              
                                                  window.addEventListener('resize', function() {
                                                      myChart.resize();
                                                  });
                                              })();
                                              """,
                                      chartDiv.getId().orElse(""), dataPoints, colors,
                                      globalStartDate.toString(),
                                      globalEndDate.toString());

        chartDiv.getElement().executeJs(script);
    }
}
