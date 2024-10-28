package org.example;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.ui.ApplicationFrame;
import org.jfree.chart.ui.UIUtils;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jtransforms.fft.DoubleFFT_1D;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class SignalPlotter extends ApplicationFrame {

    public SignalPlotter(String title, List<Double> frequencies) {
        super(title);
        JTabbedPane tabbedPane = new JTabbedPane();

        for (double freq : frequencies) {
            // Создание сигналов
            XYSeries harmonicSeries = createHarmonicSeries(freq);
            XYSeries digitalSeries = createDigitalSeries(freq);

            // Создание спектров
            XYSeries harmonicSpectrum = createSpectrum(harmonicSeries);
            XYSeries digitalSpectrum = createSpectrum(digitalSeries);

            // Создание графиков сигналов
            XYSeriesCollection harmonicDataset = new XYSeriesCollection(harmonicSeries);
            XYSeriesCollection digitalDataset = new XYSeriesCollection(digitalSeries);
            JFreeChart harmonicChart = createChart(harmonicDataset, "Harmonic Signal (Frequency: " + freq + " Hz)");
            JFreeChart digitalChart = createChart(digitalDataset, "Digital Signal (Frequency: " + freq + " Hz)");

            // Создание графиков спектров
            XYSeriesCollection harmonicSpectrumDataset = new XYSeriesCollection(harmonicSpectrum);
            XYSeriesCollection digitalSpectrumDataset = new XYSeriesCollection(digitalSpectrum);
            JFreeChart harmonicSpectrumChart = createChart(harmonicSpectrumDataset, "Harmonic Spectrum (Frequency: " + freq + " Hz)");
            JFreeChart digitalSpectrumChart = createChart(digitalSpectrumDataset, "Digital Spectrum (Frequency: " + freq + " Hz)");

            // Добавление графиков на вкладки
            JPanel harmonicPanel = new ChartPanel(harmonicChart);
            JPanel digitalPanel = new ChartPanel(digitalChart);
            JPanel harmonicSpectrumPanel = new ChartPanel(harmonicSpectrumChart);
            JPanel digitalSpectrumPanel = new ChartPanel(digitalSpectrumChart);

            tabbedPane.add("Harmonic " + freq + " Hz", harmonicPanel);
            tabbedPane.add("Digital " + freq + " Hz", digitalPanel);
            tabbedPane.add("Harmonic Spectrum " + freq + " Hz", harmonicSpectrumPanel);
            tabbedPane.add("Digital Spectrum " + freq + " Hz", digitalSpectrumPanel);
        }

        setContentPane(tabbedPane);
    }

    private XYSeries createHarmonicSeries(double frequency) {
        XYSeries series = new XYSeries("Harmonic Signal");
        double samplingRate = 1000;  // Частота дискретизации (в Гц)
        int points = (int) samplingRate;  // 1000 точек для 1 секунды

        for (int i = 0; i < points; i++) {
            double t = i / samplingRate;  // Время в секундах
            double value = Math.sin(2 * Math.PI * frequency * t);
            series.add(t, value);
        }
        return series;
    }

    private XYSeries createDigitalSeries(double frequency) {
        XYSeries series = new XYSeries("Digital Signal");
        double samplingRate = 1000;  // Частота дискретизации (в Гц)
        int points = (int) samplingRate;  // 1000 точек для 1 секунды

        for (int i = 0; i < points; i++) {
            double t = i / samplingRate;  // Время в секундах
            double value = (i % (samplingRate / (2 * frequency)) < (samplingRate / (4 * frequency))) ? 1 : 0;
            series.add(t, value);
        }
        return series;
    }

    private XYSeries createSpectrum(XYSeries signal) {
        int n = signal.getItemCount();
        double[] fftData = new double[n * 2];  // Двойной массив для комплексных данных
        for (int i = 0; i < n; i++) {
            fftData[2 * i] = signal.getY(i).doubleValue(); // Реальная часть
            fftData[2 * i + 1] = 0; // Мнимая часть
        }

        DoubleFFT_1D fft = new DoubleFFT_1D(n);
        fft.realForward(fftData); // Выполнение FFT

        XYSeries spectrum = new XYSeries("Spectrum");
        for (int i = 0; i < n / 2; i++) { // Используем только первую половину спектра
            double magnitude = Math.sqrt(fftData[2 * i] * fftData[2 * i] + fftData[2 * i + 1] * fftData[2 * i + 1]);
            spectrum.add(i, magnitude); // Добавляем амплитуду к спектру
        }
        return spectrum;
    }

    private JFreeChart createChart(XYSeriesCollection dataset, String title) {
        JFreeChart chart = ChartFactory.createXYLineChart(
                title,
                "Frequency (Hz)",
                "Amplitude",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );
        XYPlot plot = chart.getXYPlot();
        plot.setDomainPannable(true);
        plot.setRangePannable(true);
        return chart;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        List<Double> frequencies = new ArrayList<>();

        System.out.println("Введите частоты через запятую (например: 1,2,4,8):");
        String input = scanner.nextLine();

        String[] inputs = input.split(","); // Разделяем строку по запятой
        for (String freq : inputs) {
            try {
                frequencies.add(Double.parseDouble(freq.trim())); // Убираем пробелы и конвертируем в double
            } catch (NumberFormatException e) {
                System.out.println("Некорректный формат числа: " + freq);
            }
        }
        scanner.close();

        SignalPlotter plotter = new SignalPlotter("Signal Plotter", frequencies);
        plotter.setSize(800, 600);
        UIUtils.centerFrameOnScreen(plotter);
        plotter.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        plotter.setVisible(true);
    }
}
