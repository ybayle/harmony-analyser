package org.harmony_analyser.application;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import java.awt.*;
import java.util.*;

@SuppressWarnings({"SameParameterValue", "UnusedParameters"})

public class ComplexityChartDrawPanel extends DrawPanel {
	private final double[] descriptorValues;
	private final String[] descriptorDescriptions;
	private final String[] descriptorShortcuts;

	public ComplexityChartDrawPanel(Map<Float, String> data) {
		super();
		descriptorValues = new double[3];
		descriptorDescriptions = new String[3];
		descriptorShortcuts = new String[3];
		int i = 0;
		for(Map.Entry<Float, String> entry : data.entrySet()) {
			descriptorValues[i] = (double) Float.parseFloat(entry.getValue());
			i++;
		}
	}

	/* Public / Package methods */

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		drawComplexityColumnGraph(g, descriptorValues[0], descriptorValues[1], descriptorValues[2]);
	}

	/* Private methods */

	/* Complet analysis */

	private void drawComplexityColumnGraph(Graphics g, double atc, double ahc, double rtc) {
		descriptorValues[0] = atc;
		descriptorShortcuts[0] = "ATC";
		descriptorDescriptions[0] = "Avg. Transition Compl.";
		descriptorValues[1] = ahc;
		descriptorShortcuts[1] = "ACC";
		descriptorDescriptions[1] = "Avg. Chord Compl.";
		descriptorValues[2] = rtc;
		descriptorShortcuts[2] = "RTC";
		descriptorDescriptions[2] = "Rel. Transition Compl.";

		drawColumnChart(g, descriptorValues, descriptorShortcuts, descriptorDescriptions, "Complexity results", "", "", Color.BLACK);
	}

	/* Analysis components */

	/**
	 * Draws simple column chart using JFreeChart
	 * @param g [Graphics] main Graphics object
	 * @param descriptorValues [int[]] descriptor values to plot
	 * @param descriptorDescriptions [String[]] descriptions of descriptors
	 * @param descriptorShortcuts [String[]] names of descriptors
	 * @param chartTitle [String] name of the chart
	 * @param xAxisTitle [String] name of X axis
	 * @param yAxisTitle [String] name of Y axis
	 * @param gridLinePaint [Color] color of the line paint
	 */
	private void drawColumnChart(Graphics g, double[] descriptorValues, String[] descriptorShortcuts, String[] descriptorDescriptions, String chartTitle, String xAxisTitle, String yAxisTitle, Color gridLinePaint) {
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		for (int i = 0; i < descriptorValues.length; i++) {
			dataset.setValue(descriptorValues[i], descriptorDescriptions[i], descriptorShortcuts[i]);
		}
		JFreeChart chart = ChartFactory.createBarChart(chartTitle, xAxisTitle, yAxisTitle,dataset, PlotOrientation.VERTICAL, true, true, true);
		CategoryPlot categoryPlot = chart.getCategoryPlot();
		categoryPlot.setRangeGridlinePaint(gridLinePaint);
		categoryPlot.setBackgroundPaint(Color.WHITE);
		chart.getTitle().setFont(new Font("Sans", Font.PLAIN, 15));
		this.removeAll();
		ChartPanel chartPanel = new ChartPanel(chart, this.getWidth(), this.getHeight(), this.getWidth(), this.getHeight(), this.getWidth(), this.getHeight(), false, true, true, true, true, true);
		this.add(chartPanel);
		this.validate();
	}
}
