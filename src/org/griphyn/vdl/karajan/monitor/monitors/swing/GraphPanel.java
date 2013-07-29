//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 24, 2013
 */
package org.griphyn.vdl.karajan.monitor.monitors.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimerTask;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.UIManager;

import org.griphyn.vdl.karajan.monitor.SystemState;
import org.griphyn.vdl.karajan.monitor.items.StatefulItemClass;
import org.griphyn.vdl.karajan.monitor.items.SummaryItem;
import org.griphyn.vdl.karajan.monitor.items.SummaryItem.State;
import org.griphyn.vdl.karajan.monitor.monitors.ansi.GlobalTimer;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

public class GraphPanel extends JPanel {
    private SystemState state;
    private JFreeChart chart;
    private final List<SummaryItem.State> enabled;
    private TimeSeriesCollection col;
    private JPanel legend;
    private GraphsPanel gp;

    public GraphPanel(SystemState state, GraphsPanel gp) {
        this.state = state;
        this.gp = gp;
        this.setLayout(new BorderLayout());
        enabled = new ArrayList<SummaryItem.State>();
        createChart();
        
        GlobalTimer.getTimer().schedule(new TimerTask() {
            public void run() {
                update();
            }
        }, 1000, 1000);
    }
    
    private void createChart() {
        col = new TimeSeriesCollection();
        for (SummaryItem.State s : enabled) {
            TimeSeries ts = new TimeSeries(s.getName());
            col.addSeries(ts);
        }
        chart = ChartFactory.createTimeSeriesChart(null, "Time", "Count", col, false, true, false);
        ChartPanel cp = new ChartPanel(chart);
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(UIManager.getColor("TextField.background"));
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());
        p.add(cp, BorderLayout.CENTER);
        legend = new JPanel();
        legend.setLayout(new FlowLayout());
        
        rebuildLegend();
        
        this.add(p, BorderLayout.CENTER);
        this.add(legend, BorderLayout.SOUTH);
    }

    private void rebuildLegend() {
        legend.removeAll();
        for (int i = 0; i < enabled.size(); i++) {
            makeLegendEntry(legend, chart, enabled.get(i).getShortName(), i);
        }
        legend.add(new JLabel());
        legend.add(new JLabel());
        JButton newb = GraphsPanel.makeButton("gui/icons/plus.png", "New...");
        
        newb.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                displayAddPopup((JButton) e.getSource());
            } 
        });
        
        legend.add(newb);         
    }

    protected void displayAddPopup(JButton src) {
        JPopupMenu p = new JPopupMenu();
        for (final SummaryItem.State s : SummaryItem.STATES) {
            if (!enabled.contains(s)) {
                JMenuItem mi = new JMenuItem(s.getName());
                
                mi.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        enable(s);
                    } 
                });
                p.add(mi);
            }
        }
        
        p.show(src, 4, 4);
    }
    

    private void makeLegendEntry(JPanel l, final JFreeChart chart, final String label, final int series) {
        Color color = (Color) chart.getPlot().getLegendItems().get(series).getLinePaint();
        final ColorButton cb = new ColorButton(color);
        cb.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                displaySeriesPopup(chart, label, series, cb);
            }
        });
        l.add(cb);
        l.add(new JLabel(label));
    }

    protected void removeSeries(JFreeChart chart, int series) {
        enabled.remove(series);
        col.removeSeries(series);
        rebuildLegend();
        repaint();
        gp.saveLayout();
    }
    
    protected void displaySeriesPopup(final JFreeChart chart, final String label, final int series, final ColorButton button) {
        JPopupMenu p = new JPopupMenu();
        JMenuItem color = new JMenuItem("Color...");
        p.add(color);
        color.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                displayColorPicker(chart, label, series, button);
            }
        });
        
        JMenuItem remove = new JMenuItem("Remove");
        p.add(remove);
        remove.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeSeries(chart, series);
            } 
        });
        
        p.show(button, 4, 4);
    }

    protected void displayColorPicker(JFreeChart chart, String label, int series, ColorButton button) {
        Color color = (Color) chart.getPlot().getLegendItems().get(series).getLinePaint();
        Color newColor = JColorChooser.showDialog(this, "Pick a color for " + label, color);
        button.setColor(newColor);
        ((XYPlot) chart.getPlot()).getRenderer().setSeriesPaint(series, newColor);
        gp.saveLayout();
    }
    
    public void setColor(State s, Color color) {
        int series = enabled.indexOf(s);
        ((XYPlot) chart.getPlot()).getRenderer().setSeriesPaint(series, color);
        int i = 0;
        for (Component c : legend.getComponents()) {
            if (c instanceof JButton) {
                i++;
                if (i == series + 1) {
                    ((ColorButton) c).setColor(color);
                }
            }
        }
    }

    private void update() {
        SummaryItem summary = (SummaryItem) state.getItemByID(SummaryItem.ID, StatefulItemClass.WORKFLOW);
        if (summary == null) {
            return;
        }
        for (SummaryItem.State s : enabled) {
            TimeSeries ts = col.getSeries(s.getName());
            ts.add(new Second(new Date()), summary.getCount(s));
        }
    }
    
    public void enable(State s) {
        enable(s, true);
    }

    public void enable(State s, boolean save) {
        TimeSeries ts = new TimeSeries(s.getName());
        col.addSeries(ts);
        enabled.add(s);
        rebuildLegend();
        repaint();
        if (save) {
            gp.saveLayout();
        }
    }

    public void store(Preferences p) {
        p.putInt("enabledCount", enabled.size());
        for (int i = 0; i < enabled.size(); i++) {
            Preferences gp = p.node("series" + i);
            gp.put("key", enabled.get(i).getName());
            Color color = (Color) chart.getPlot().getLegendItems().get(i).getLinePaint();
            gp.putInt("color.r", color.getRed());
            gp.putInt("color.g", color.getGreen());
            gp.putInt("color.b", color.getBlue());
        }
    }
    
    public static GraphPanel load(Preferences p, SystemState state, GraphsPanel gps) {
        GraphPanel g = new GraphPanel(state, gps);
        int ec = p.getInt("enabledCount", 0);
        for (int i = 0; i < ec; i++) {
            Preferences gp = p.node("series" + i);
            String key = gp.get("key", null);
            if (key == null) {
                throw new RuntimeException("Null series key");
            }
            int cr = gp.getInt("color.r", 255);
            int cg = gp.getInt("color.g", 0);
            int cb = gp.getInt("color.b", 0);
            SummaryItem.State s = SummaryItem.getStateByKey(key);
            g.enable(s, false);
            g.setColor(s, new Color(cr, cg, cb));
        }
        return g;
    }
}