package com.example.Signal.components;

import com.example.Signal.models.EvaluationTimeframe;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class StatisticsPanel extends VerticalLayout {

    private final Select<EvaluationTimeframe> selectTimeframe;
    private final IntegerField rollingAverageWindowField;
    private final DatePicker datePickerFrom;
    private final DatePicker datePickerTo;
    private final HorizontalLayout hlCustomDateRange;
    private final HorizontalLayout hlEvaluation;

    public StatisticsPanel(Runnable updateEvaluation) {
        setPadding(false);
        setSpacing(true);
        addClassName("statistics-panel");

        H2 headline = new H2("Statistics");
        headline.addClassName("statistics-headline");

        selectTimeframe = new Select<>();
        selectTimeframe.setLabel("Timeframe");
        selectTimeframe.setItems(EvaluationTimeframe.values());
        selectTimeframe.setValue(EvaluationTimeframe.ALL_TIME);
        selectTimeframe.addValueChangeListener(event -> {
            if (event.isFromClient()) {
                updateEvaluation.run();
            }
        });
        selectTimeframe.setWidth("200px");

        rollingAverageWindowField = new IntegerField();
        rollingAverageWindowField.setLabel("Rolling Average");
        rollingAverageWindowField.setValue(7);
        rollingAverageWindowField.setMin(1);
        rollingAverageWindowField.setMax(30);
        rollingAverageWindowField.setStepButtonsVisible(true);
        rollingAverageWindowField.addValueChangeListener(event -> {
            if (event.isFromClient() && event.getValue() != null) {
                updateEvaluation.run();
            }
        });
        rollingAverageWindowField.setWidth("150px");

        HorizontalLayout timeframeRow = new HorizontalLayout(selectTimeframe, rollingAverageWindowField);
        timeframeRow.setSpacing(true);
        timeframeRow.setAlignItems(Alignment.END);

        datePickerFrom = new DatePicker("From");
        datePickerFrom.setValue(LocalDate.now().minusDays(30));
        datePickerFrom.addValueChangeListener(event -> {
            if (event.isFromClient()) {
                updateEvaluation.run();
            }
        });
        datePickerFrom.setWidth("150px");

        datePickerTo = new DatePicker("To");
        datePickerTo.setValue(LocalDate.now());
        datePickerTo.addValueChangeListener(event -> {
            if (event.isFromClient()) {
                updateEvaluation.run();
            }
        });
        datePickerTo.setWidth("150px");

        hlCustomDateRange = new HorizontalLayout(datePickerFrom, datePickerTo);
        hlCustomDateRange.setVisible(false);
        hlCustomDateRange.setSpacing(true);

        hlEvaluation = new HorizontalLayout();
        hlEvaluation.addClassName("evaluation-container");
        hlEvaluation.setWidth("100%");

        add(headline, timeframeRow, hlCustomDateRange, hlEvaluation);
    }
}
