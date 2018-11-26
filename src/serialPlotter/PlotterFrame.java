package serialPlotter;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RefineryUtilities;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

public class PlotterFrame extends JFrame implements ActionListener {
	
	private JPanel upperPanel;
	private JPanel middlePanel;
	private JPanel lowerPanel;
	
	private JComboBox<String> ports;
	private JButton connectButton;
	
	private SerialPort currentPort;
	private Thread reader;
	private Thread writer;
	private InputStream in;
	private OutputStream out;
	
	private JButton startButton;
	private JButton stopButton;
	private static ArrayList<float[]> storedData = new ArrayList<>();
	
	private static boolean started = false;
	
	private ChartPanel xAccelChartPanel;
	private ChartPanel yAccelChartPanel;
	private ChartPanel zAccelChartPanel;
	
	
	private ChartPanel xNoiseChartPanel;
	private ChartPanel yNoiseChartPanel;
	private ChartPanel zNoiseChartPanel;
	
	private JTextField xIntervalTextField =  new JTextField("0.001");
	private JTextField yIntervalTextField =  new JTextField("0.001");
	private JTextField zIntervalTextField =  new JTextField("0.001");
	
	private JButton xNoiseRefreshButton = new JButton("refresh");
	private JButton yNoiseRefreshButton = new JButton("refresh"); 
	private JButton zNoiseRefreshButton = new JButton("refresh");
	
	
	
	public PlotterFrame() {
		
		init();
	}
	
	private void init() {
		this.setMinimumSize(new Dimension(1280, 720));
		this.setPreferredSize(new Dimension(1280, 720));
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setLayout(new GridBagLayout());
		
		createUpperPanel();
		createMiddlePanel();
		createLowerPanel();
		createCharts();
		RefineryUtilities.centerFrameOnScreen(this);
		this.setVisible(true);
		
		//petit test
	}
	
	private void createUpperPanel() {
		upperPanel = new JPanel();
		GridBagConstraints constraint = new GridBagConstraints();
		constraint.anchor = GridBagConstraints.WEST;
		constraint.fill = GridBagConstraints.BOTH;
		constraint.gridx = 0;
		constraint.gridy = 0;
		constraint.gridheight = 1;
		constraint.gridwidth = 1;
		constraint.weightx = 1.0f;
		constraint.weighty= 0.02f;
		this.add(upperPanel, constraint);
		
		this.upperPanel.setBackground(Color.darkGray);
		
		upperPanel.setLayout(new GridBagLayout());
		
		
		constraint.fill = GridBagConstraints.HORIZONTAL;
		constraint.weightx = 1.0f;
		constraint.weighty = 1.0f;
		
		ports = new JComboBox<>();
		ports.setMinimumSize(new Dimension(100, 20));
		
		java.util.Enumeration<CommPortIdentifier> portEnum = CommPortIdentifier.getPortIdentifiers();
        while ( portEnum.hasMoreElements() ) 
        {
            CommPortIdentifier portIdentifier = portEnum.nextElement();
            ports.addItem(portIdentifier.getName());
            
        }    
		constraint.gridx = 0;
		constraint.gridy = 0;
		upperPanel.add(ports, constraint);
		
		connectButton = new JButton("Connecter");
		connectButton.setName("C");
		connectButton.setMinimumSize(new Dimension(50,20));
		constraint.gridx = 1;
		constraint.gridy = 0;
		upperPanel.add(connectButton, constraint);
		connectButton.addActionListener(this);
		
		
		
	}
	
	
	private void createMiddlePanel() {
		middlePanel = new JPanel();
		GridBagConstraints constraint = new GridBagConstraints();
		constraint.anchor = GridBagConstraints.WEST;
		constraint.fill = GridBagConstraints.BOTH;
		constraint.gridx = 0;
		constraint.gridy = 1;
		constraint.gridheight = 1;
		constraint.gridwidth = 1;
		constraint.weightx = 1.0f;
		constraint.weighty= 0.96f;
		this.add(middlePanel, constraint);
	}
	
	private void createLowerPanel() {
		lowerPanel = new JPanel();
		GridBagConstraints constraint = new GridBagConstraints();
		constraint.anchor = GridBagConstraints.WEST;
		constraint.fill = GridBagConstraints.BOTH;
		constraint.gridx = 0;
		constraint.gridy = 2;
		constraint.gridheight = 1;
		constraint.gridwidth = 1;
		constraint.weightx = 1.0f;
		constraint.weighty= 0.02f;
		this.add(lowerPanel, constraint);
		
		constraint.anchor = GridBagConstraints.WEST;
		constraint.fill = GridBagConstraints.HORIZONTAL;
		constraint.gridheight = 1;
		constraint.gridwidth = 1;
		constraint.weightx= 1.0f;
		constraint.weighty= 1.0f;
		
		startButton = new JButton("Start");
		constraint.gridx = 0;
		constraint.gridy = 0;
		lowerPanel.add(startButton, constraint);
		startButton.addActionListener(this);
		startButton.setEnabled(false);
		
		stopButton = new JButton("Stop");
		constraint.gridx = 1;
		constraint.gridy = 0;
		lowerPanel.add(stopButton, constraint);
		stopButton.addActionListener(this);
		stopButton.setEnabled(false);
		
		
	}
	
	private void createCharts() {
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		gbc.weightx = 0.5f;
		gbc.weighty= 0.33f;
		middlePanel.setLayout(new GridBagLayout());
		
		xAccelChartPanel = new ChartPanel(null);
		gbc.gridx = 0;
		gbc.gridy = 0;
		middlePanel.add(xAccelChartPanel, gbc);
		
		yAccelChartPanel = new ChartPanel(null);
		gbc.gridx = 0;
		gbc.gridy = 1;
		middlePanel.add(yAccelChartPanel, gbc);
		
		zAccelChartPanel = new ChartPanel(null);
		gbc.gridx = 0;
		gbc.gridy = 2;
		middlePanel.add(zAccelChartPanel, gbc);
		
		xNoiseChartPanel = new ChartPanel(null);
		xNoiseRefreshButton.addActionListener(this);
		gbc.gridx = 1;
		gbc.gridy = 0;
		middlePanel.add(getNoiseChartPanel(xNoiseChartPanel, xIntervalTextField, xNoiseRefreshButton), gbc);
		
		yNoiseChartPanel = new ChartPanel(null);
		yNoiseRefreshButton.addActionListener(this);
		gbc.gridx = 1;
		gbc.gridy = 1;
		middlePanel.add(getNoiseChartPanel(yNoiseChartPanel, yIntervalTextField, yNoiseRefreshButton), gbc);
		
		zNoiseChartPanel = new ChartPanel(null);
		zNoiseRefreshButton.addActionListener(this);
		gbc.gridx = 1;
		gbc.gridy = 2;
		middlePanel.add(getNoiseChartPanel(zNoiseChartPanel, zIntervalTextField, zNoiseRefreshButton), gbc);
		
	}
	
	
	JPanel getNoiseChartPanel(ChartPanel chartPan, JTextField textField, JButton button) {
		JPanel pan = new JPanel();
		pan.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		gbc.weightx = 1.0f;
		gbc.weighty= 0.98f;
		
		gbc.gridx = 0;
		gbc.gridy = 0;
		pan.add(chartPan, gbc);
		
		JPanel subPan = new JPanel();
		gbc.weightx = 0.5f;
		gbc.weighty= 1.0f;
		subPan.setLayout(new GridBagLayout());
		gbc.gridx = 0;
		gbc.gridy = 0;
		subPan.add(textField, gbc);
		gbc.gridx = 1;
		gbc.gridy = 0;
		subPan.add(button, gbc);
		
		
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		gbc.weightx = 1.0f;
		gbc.weighty= 0.02f;
		gbc.gridx = 0;
		gbc.gridy = 1;
		pan.add(subPan, gbc);
		
		return pan;
	}
	
	
	void generateRawCharts() {

		xAccelChartPanel.removeAll();
		yAccelChartPanel.removeAll();
		zAccelChartPanel.removeAll();

		XYSeriesCollection xDataset = new XYSeriesCollection();
		XYSeriesCollection yDataset = new XYSeriesCollection();
		XYSeriesCollection zDataset = new XYSeriesCollection();
		XYSeries xData = new XYSeries("x-accel");
		XYSeries yData = new XYSeries("y-accel");
		XYSeries zData = new XYSeries("z-accel");
		for(float[] stored : storedData) {
			xData.add(stored[0], stored[1]);
			yData.add(stored[0], stored[2]);
			zData.add(stored[0], stored[3]);
		}
		xDataset.addSeries(xData);
		yDataset.addSeries(yData);
		zDataset.addSeries(zData);

		
		
		JFreeChart xylineChart = ChartFactory.createXYLineChart("x-accel brut","t(s)","acceleration(g)",xDataset,PlotOrientation.VERTICAL,true , true , false);
		XYPlot plot = xylineChart.getXYPlot();
		XYLineAndShapeRenderer renderer1 = new XYLineAndShapeRenderer();
		renderer1.setSeriesShapesVisible(0, false);
		renderer1.setSeriesPaint(0, Color.red);
		plot.setRenderer(renderer1);
		xAccelChartPanel.setChart(xylineChart);
		
		xylineChart = ChartFactory.createXYLineChart("y-accel brut","t(s)","acceleration(g)",yDataset,PlotOrientation.VERTICAL,true , true , false);
		plot = xylineChart.getXYPlot();
		XYLineAndShapeRenderer renderer2 = new XYLineAndShapeRenderer();
		renderer2.setSeriesShapesVisible(0, false);
		renderer2.setSeriesPaint(0, Color.yellow);
		plot.setRenderer(renderer2);
		yAccelChartPanel.setChart(xylineChart);
		
		xylineChart = ChartFactory.createXYLineChart("z-accel brut","t(s)","acceleration(g)",zDataset,PlotOrientation.VERTICAL,true , true , false);
		plot = xylineChart.getXYPlot();
		XYLineAndShapeRenderer renderer3 = new XYLineAndShapeRenderer();
		renderer3.setSeriesShapesVisible(0, false);
		renderer3.setSeriesPaint(0, Color.green);
		plot.setRenderer(renderer3);
		zAccelChartPanel.setChart(xylineChart);
	}
	
	int[] getDataForNoiseChart(float data[], float pitch) {
		float min = 20.0f;
		float max = -20.0f;
		
		for(int i=0; i < data.length; i++) {
			if(data[i]<min) min = data[i];
			if(data[i]>max) max = data[i];
		}
		
		int nbOfIntervals = (int)(Math.abs(max - min)/pitch);
		int[] pointsPerInterval = new int[nbOfIntervals+1];
		
		for(int i=0; i < pointsPerInterval.length; i++) pointsPerInterval[i]= 0;
		
		for(int i=0; i < data.length; i++) {
			for(int j=0; j < pointsPerInterval.length; j++) {
				if(data[i] <= j*pitch+min) {
					pointsPerInterval[j]++;
					break;
				}
			}
		}
		
		return pointsPerInterval;
	}
	
	void refreshNoiseChart(int chartIdx, float pitch) {

		float[] data = new float[storedData.size()];
		float min = 100.0f;
		for(int i=0; i < storedData.size(); i++) {
			data[i] = storedData.get(i)[chartIdx+1];
			if(data[i] < min) min = data[i];
		}
		int[] pointsPerInterval = getDataForNoiseChart(data, pitch);
		
		XYSeriesCollection dataSet = new XYSeriesCollection();
		XYSeries serie = new XYSeries("intervals");
		for(int i=0; i< pointsPerInterval.length; i++) {
			serie.add(i*pitch+min, pointsPerInterval[i]);
		}
		dataSet.addSeries(serie);

		JFreeChart barChart = ChartFactory.createScatterPlot("test", "x", "y", dataSet);
		barChart.removeLegend();
		XYPlot plot = barChart.getXYPlot();
		XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
		plot.setRenderer(renderer);
		if(chartIdx == 0) {
			 renderer.setSeriesPaint(0, Color.red);
			xNoiseChartPanel.setChart(barChart);
		}
		else if(chartIdx == 1) {
			renderer.setSeriesPaint(0, Color.yellow);
			yNoiseChartPanel.setChart(barChart);
		}
		else if(chartIdx == 2) {
			renderer.setSeriesPaint(0, Color.green);
			zNoiseChartPanel.setChart(barChart);
		}
	}
	
	void disconnect() {
		try {
			if(currentPort != null) {
				reader.stop();
				out.close();
				in.close();
				currentPort.close();
				connectButton.setName("C");
				connectButton.setText("Connecter");
			}
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	void connect (String name) throws Exception {
		CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(name);
        if ( portIdentifier.isCurrentlyOwned() )
        {
            System.out.println("Error: Port is currently in use");
        }
        else
        {
            CommPort commPort = portIdentifier.open(this.getClass().getName(),2000);
            
            if ( commPort instanceof SerialPort )
            {
                currentPort = (SerialPort) commPort;
                currentPort.setSerialPortParams(115200,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
                
                in = currentPort.getInputStream();
                out = currentPort.getOutputStream();
                
                reader = new Thread(new SerialReader(in));
                writer = new Thread(new SerialWriter(out));
                
                reader.start();
                
                connectButton.setText("Déconnecter");
                connectButton.setName("D");

            }
            else
            {
                System.out.println("Error: Only serial ports are handled by this example.");
            }
        }     
    }
    
    public static class SerialReader implements Runnable 
    {
        InputStream in;
        String output = new String("");
        private volatile boolean exit = false;
        
        public SerialReader ( InputStream in )
        {
            this.in = in;
        }
        
        public void run ()
        {
            try
            {
            	String result = new String("");
            	int off = 0;
            	while(!exit) {
            		while(in.available() > -1) {
            			byte[] buffer = new byte[1024];
            			int len = in.read(buffer, 0, 1024);
            			result += new String(buffer, 0, len);
            			while(result.contains("\r\n")) {
            				String line = "";
            				int index = 0;
            				try {
            					index = result.indexOf("\r\n");
            					line = result.substring(0, index);

            					String[] parsed = line.split(" ");
            					float[] data = new float[6];
            					for(int i=4; i<parsed.length-1; i++) {
            						data[i-4] = Float.valueOf(parsed[i]);
            					}
            					if(index+2 < result.length()-1) {
            						result = result.substring(index+2, result.length()-1);
            					}
            					else result = "";
            					if(started) {
            						storedData.add(data);
            					}
            				}
            				catch(NumberFormatException ex) {
            					//It's a dead line remove it
            					result = "";
            				}
            			}
            		}
            	}
            }
            catch ( IOException e )
            {
            	e.printStackTrace();
            }            
        }

        public void stop() {
        	exit = true;
        }
    }

    /** */
    public static class SerialWriter implements Runnable 
    {
        OutputStream out;
        private volatile boolean exit = false;
        
        public SerialWriter ( OutputStream out )
        {
            this.out = out;
        }
        
        public void run ()
        {
            try
            {                
                int c = 0;
                while ( ( c = System.in.read()) > -1 )
                {
                    this.out.write(c);
                }                
            }
            catch ( IOException e )
            {
                e.printStackTrace();
            }            
        }
    }

	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if(source == connectButton) {
			try {
				if(connectButton.getName().equals("C")) {
					connect((String)ports.getSelectedItem());
					startButton.setEnabled(true);
				}
				else {
					disconnect();
					startButton.setEnabled(false);
					stopButton.setEnabled(false);
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
		if(source == startButton) {
			startButton.setEnabled(false);
			stopButton.setEnabled(true);
			storedData = new ArrayList<>();
			started = true;
		}
		if(source == stopButton) {
			stopButton.setEnabled(false);
			startButton.setEnabled(true);
			started = false;
			generateRawCharts();
			refreshNoiseChart(0,Float.valueOf(xIntervalTextField.getText()));
			refreshNoiseChart(1,Float.valueOf(yIntervalTextField.getText()));
			refreshNoiseChart(2,Float.valueOf(zIntervalTextField.getText()));
			revalidate();
		}
		if(source == xNoiseRefreshButton) {
			refreshNoiseChart(0,Float.valueOf(xIntervalTextField.getText()));
			revalidate();
		}
		if(source == yNoiseRefreshButton) {
			refreshNoiseChart(1,Float.valueOf(yIntervalTextField.getText()));
			revalidate();
		}
		if(source == zNoiseRefreshButton) {
			refreshNoiseChart(2,Float.valueOf(zIntervalTextField.getText()));
			revalidate();
		}
		
	}
}
