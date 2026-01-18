package com.example.Signal.components.charts;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class EChartsScatterChartBuilder {

    private static final Map<Integer, String> SCORE_COLORS = Map.ofEntries(Map.entry(1, "#00d66a"),
                                                                           Map.entry(2, "#4cd964"),
                                                                           Map.entry(3, "#a8e063"),
                                                                           Map.entry(4, "#ffc857"),
                                                                           Map.entry(5, "#ff9a3c"),
                                                                           Map.entry(6, "#ff6b6b"),
                                                                           Map.entry(7, "#c92a2a"));

    private final String chartElementId;
    private final Map<LocalDate, Integer> data;
    private final LocalDate minDate;
    private final LocalDate maxDate;

    public EChartsScatterChartBuilder(String chartElementId,
                                      Map<LocalDate, Integer> data,
                                      LocalDate minDate,
                                      LocalDate maxDate) {
        this.chartElementId = chartElementId;
        this.data = data;
        this.minDate = minDate;
        this.maxDate = maxDate;
    }

    public String buildScript() {
        List<Map.Entry<LocalDate, Integer>> sortedData = data.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .toList();

        String dataPointsJson = buildDataPointsJson(sortedData);
        String colorsJson = buildColorsJson(sortedData);

        return String.format("""
                                     (function() {
                                         const chartDom = document.getElementById('%s');
                                         if (!chartDom) return;
                                     
                                         const myChart = echarts.init(chartDom);
                                         const dataPoints = %s;
                                         const colors = %s;
                                     
                                         const option = %s;
                                     
                                         myChart.setOption(option);
                                     
                                         window.addEventListener('resize', function() {
                                             myChart.resize();
                                         });
                                     })();
                                     """, chartElementId, dataPointsJson, colorsJson, buildChartOptions());
    }

    private String buildDataPointsJson(List<Map.Entry<LocalDate, Integer>> sortedData) {
        StringBuilder dataPoints = new StringBuilder("[");

        for (int i = 0; i < sortedData.size(); i++) {
            Map.Entry<LocalDate, Integer> entry = sortedData.get(i);
            if (i > 0) {
                dataPoints.append(",");
            }
            dataPoints.append(String.format("['%s', %d]", entry.getKey().toString(), entry.getValue()));
        }

        dataPoints.append("]");
        return dataPoints.toString();
    }

    private String buildColorsJson(List<Map.Entry<LocalDate, Integer>> sortedData) {
        StringBuilder colors = new StringBuilder("[");

        for (int i = 0; i < sortedData.size(); i++) {
            if (i > 0) {
                colors.append(",");
            }
            colors.append(String.format("'%s'", SCORE_COLORS.get(sortedData.get(i).getValue())));
        }

        colors.append("]");
        return colors.toString();
    }

    private String buildChartOptions() {
        return String.format("""
                                     {
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
                                     }
                                     """, minDate.toString(), maxDate.toString());
    }
}
