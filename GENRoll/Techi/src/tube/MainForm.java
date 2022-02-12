package tube;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JComboBox;
import javax.swing.JTextArea;
import java.awt.Font;
import javax.swing.JScrollPane;

public class MainForm
{

	private JFrame frmGenroll;
	private JTextField textDOUT;
	private JTextField textT0;
	private JTextField textC;
	private JTextField textP00;
	private JTextField textHS;
	private JTextField textET;
	private JTextField textSYT;
	private JTextField textALPHAT;
	private JTextField textES;
	private JTextField textSYS;
	private JTextField textALPHAS;
	
	private JTextArea textArea1;
	// the calculation objects
	//
	private GENRoll G;
	private boolean bResult;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args)
	{
		EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				try
				{
					MainForm window = new MainForm();
					window.frmGenroll.setVisible(true);
				} catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public MainForm()
	{
		//initialise GUI form
		initialize();

		
		//create calculation object 
		G = new GENRoll();

		//prepare input
		G.ReadInput();
		printString("Generating input");
		
		setTextFields(G);
		
		
		
	}

	
	//process the calculate button
	//calculate button pressed
    //perform the calculation
    //and fill the textarea with results
	public void calculateForm()
	{
		
		getTextFields(G);
		
		bResult = G.Calculate();
		
		textArea1.setText(null);
		
		//print results in the textArea
        if (bResult)
        {
            printInput(G);
            printMaterial(G);
            printResults(G);

        }
        else
        {
            textArea1.append("No results available\n");
            printString("\n"+ G.message);
        }
		
	}
	
	//clear button pressed
    //clear the text area
	public void clearForm()
	{
		textArea1.setText(null);
		
	}
	
	public void setTextFields(GENRoll R)
	{
		if (R != null)
        {
            textDOUT.setText(Double.toString(R.DOUT));
            textT0.setText(Double.toString(R.T0));
            textC.setText(Double.toString(R.C));
            textP00.setText(Double.toString(R.P00));
            textHS.setText(Double.toString(R.HS));
            textET.setText(Double.toString(R.getET()));
            textSYT.setText(Double.toString(R.getSYT()));
            textALPHAT.setText(Double.toString(R.getALPHAT()));
            textES.setText(String.valueOf(R.getES()));
            textSYS.setText(String.valueOf(R.getSYS()));
            textALPHAS.setText(String.valueOf(R.getALPHAS()));
        }	
	}
	
	public void getTextFields(GENRoll R)
	{
		 if (R != null)
	        {
			 //TODO: check for Exceptions
	            R.DOUT = Double.parseDouble(textDOUT.getText());
	            R.T0 = Double.parseDouble(textT0.getText());
	            R.C = Double.parseDouble(textC.getText());
	            R.P00 = Double.parseDouble(textP00.getText());
	            R.HS = Double.parseDouble(textHS.getText());
	            R.setET(Double.parseDouble(textET.getText()));
	            R.setSYT(Double.parseDouble(textSYT.getText()));
	            R.setALPHAT(Double.parseDouble(textALPHAT.getText()));
	            R.setES(Double.parseDouble(textES.getText()));
	            R.setSYS(Double.parseDouble(textSYS.getText()));
	            R.setALPHAS(Double.parseDouble(textALPHAS.getText()));

	        }
	}
	
	public void printInput(GENRoll R)
	{
		// print the input variables
        textArea1.append("\n**** INPUT DATA ****\n");
        textArea1.append(String.format( "Tube outside diameter DOUT= %6.3f (in) \n",R.DOUT  ));
        textArea1.append(String.format("Tube wall thickness T0= %6.3f (in) \n",R.T0 ));
        textArea1.append(String.format("Poisson ratio ANU= %6.3f (-) \n",R.ANU));
        textArea1.append(String.format("Radius clearance C= %6.3f (in) \n",R.C));
        textArea1.append(String.format("Maximum rolling pressure P00= %6.3f (psi) \n",R.P00));

        printString(String.format("Rolling temperature TROLL= %6.3f (degs.F) \n",R.TROLL));
        printString(String.format("Operating temperature TOP= %6.3f (degs.F) \n",R.TOP));
        printString(String.format("Step size of rolling pressure increment DP00= %6.3f (psi) \n",R.DP00));
        printString(String.format("Step size of temperature increment DT0= %6.3f (degs.F) \n",R.DT0));
        printString(String.format("Layer thickness assumed HS= %6.3f (in) \n",R.HS));
        printString(String.format("Printout step size for rolling increment PSTEP= %6.3f \n",R.PSTEP));
        printString(String.format("Printout step size for temperature TSTEP= %6.3f \n",R.TSTEP));
        printString(String.format("Printout options NPR= %b \n", R.NPR));
        printString(String.format("Temperature return options NRETUN= %b \n", R.NRETUN));	
	}
	
	public void printMaterial(GENRoll G)
	{
		
	}
	public void printResults(GENRoll R)
	{
		
		printString("\n**** CALCULATION RESULTS \n\n");
	    printString("\n"+ R.message + "\n");
	        //TODO: print more results
	        printString(String.format("Critical Rolling pressure P0= %6.3f [psi] \n",R.P0));

	        printString("   (1) LOADING STAGE.\n");
	        printString("Rolling pressure   Contact Pressure  Plastic Layers         Pressures between layers \n");
	        printString("    (psi)                (psi)       1 2 3 4 5 6 7 8 9 10    2-3   3-4   4-5   5-6   6-7   7-8   8-9   9-10\n");

	        if (R.P0 >= (R.KK*R.PSTEP))
	        {
	            printString(String.format("   %8.1f          %8.1f        ",R.P0,R.P[0]));
	            for (int i = 0; i < 10; i++)
	            {
	                if (R.KJ1[i])				//KJ1
	                    printString("1 ");
	                else
	                    printString("0 ");
	            }
	            for (int i = 1; i <= 8; i++)
	            {
	                printString(String.format("%6.0f",R.P[i]));
	            }


	        }
	}
	 //prints a string to textArea1
    private void printString(String S)
    {
    	textArea1.append(S);
    }
	
	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize()
	{
		frmGenroll = new JFrame();
		frmGenroll.setResizable(false);
		frmGenroll.setTitle("GENROLL");
		frmGenroll.setBounds(100, 100, 807, 597);
		frmGenroll.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JPanel genPanel = new JPanel();
		frmGenroll.getContentPane().add(genPanel, BorderLayout.CENTER);
		genPanel.setLayout(null);

		JLabel lblNewLabel = new JLabel("Tube outside diameter [in] DOUT:");
		lblNewLabel.setBounds(10, 14, 266, 14);
		genPanel.add(lblNewLabel);

		JLabel lblNewLabel_1 = new JLabel("Thermal expansion tube [in/in.F] ALPHAT");
		lblNewLabel_1.setBounds(10, 226, 266, 14);
		genPanel.add(lblNewLabel_1);

		JLabel lblNewLabel_2 = new JLabel("Tube wall thickness [in] T0:");
		lblNewLabel_2.setBounds(10, 39, 266, 14);
		genPanel.add(lblNewLabel_2);

		JLabel lblNewLabel_3 = new JLabel("Radial clearance [in] C:");
		lblNewLabel_3.setBounds(10, 64, 266, 14);
		genPanel.add(lblNewLabel_3);

		JLabel lblNewLabel_4 = new JLabel("Maximum rolling pressure [psi] P00:");
		lblNewLabel_4.setBounds(10, 89, 266, 14);
		genPanel.add(lblNewLabel_4);

		JLabel lblNewLabel_5 = new JLabel("Tubesheet layer thickness [in] HS:");
		lblNewLabel_5.setBounds(10, 114, 266, 14);
		genPanel.add(lblNewLabel_5);

		JLabel lblNewLabel_6 = new JLabel("Youngs modulus tube [psi] ET:");
		lblNewLabel_6.setBounds(10, 172, 266, 14);
		genPanel.add(lblNewLabel_6);

		JLabel lblNewLabel_7 = new JLabel("Yield strength tube [psi] SYT:");
		lblNewLabel_7.setBounds(10, 197, 266, 14);
		genPanel.add(lblNewLabel_7);

		textDOUT = new JTextField();
		textDOUT.setBounds(278, 14, 86, 20);
		genPanel.add(textDOUT);
		textDOUT.setColumns(10);

		textT0 = new JTextField();
		textT0.setBounds(278, 39, 86, 20);
		genPanel.add(textT0);
		textT0.setColumns(10);

		textC = new JTextField();
		textC.setBounds(278, 64, 86, 20);
		genPanel.add(textC);
		textC.setColumns(10);

		textP00 = new JTextField();
		textP00.setBounds(278, 89, 86, 20);
		genPanel.add(textP00);
		textP00.setColumns(10);

		textHS = new JTextField();
		textHS.setBounds(278, 114, 86, 20);
		genPanel.add(textHS);
		textHS.setColumns(10);

		textET = new JTextField();
		textET.setBounds(278, 172, 86, 20);
		genPanel.add(textET);
		textET.setColumns(10);

		textSYT = new JTextField();
		textSYT.setBounds(278, 197, 86, 20);
		genPanel.add(textSYT);
		textSYT.setColumns(10);

		textALPHAT = new JTextField();
		textALPHAT.setBounds(278, 226, 86, 20);
		genPanel.add(textALPHAT);
		textALPHAT.setColumns(10);

		JButton btnCalculate = new JButton("Calculate");
		btnCalculate.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				calculateForm();
			}
		});
		btnCalculate.setBounds(25, 257, 89, 23);
		genPanel.add(btnCalculate);

		JButton btnClear = new JButton("Clear");
		btnClear.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				clearForm();
			}
		});
		btnClear.setBounds(227, 257, 89, 23);
		genPanel.add(btnClear);

		JComboBox comboTubeMaterial = new JComboBox();
		comboTubeMaterial.setBounds(218, 142, 146, 22);
		genPanel.add(comboTubeMaterial);

		JComboBox comboTubesheetMaterial = new JComboBox();
		comboTubesheetMaterial.setBounds(614, 142, 146, 22);
		genPanel.add(comboTubesheetMaterial);
		
		JLabel lblNewLabel_1_1 = new JLabel("Thermal expansion tubesheet [in/in.F] ALPHAS");
		lblNewLabel_1_1.setBounds(386, 232, 266, 14);
		genPanel.add(lblNewLabel_1_1);
		
		JLabel lblNewLabel_6_1 = new JLabel("Youngs modulus tubesheet [psi] ES:");
		lblNewLabel_6_1.setBounds(386, 178, 266, 14);
		genPanel.add(lblNewLabel_6_1);
		
		JLabel lblNewLabel_7_1 = new JLabel("Yield strength tubesheet [psi] SYS:");
		lblNewLabel_7_1.setBounds(386, 203, 266, 14);
		genPanel.add(lblNewLabel_7_1);
		
		textES = new JTextField();
		textES.setColumns(10);
		textES.setBounds(674, 175, 86, 20);
		genPanel.add(textES);
		
		textSYS = new JTextField();
		textSYS.setColumns(10);
		textSYS.setBounds(674, 200, 86, 20);
		genPanel.add(textSYS);
		
		textALPHAS = new JTextField();
		textALPHAS.setColumns(10);
		textALPHAS.setBounds(674, 229, 86, 20);
		genPanel.add(textALPHAS);
		
		JLabel lblNewLabel_8 = new JLabel("progam made with Java - Eclipse");
		lblNewLabel_8.setFont(new Font("Arial", Font.BOLD, 12));
		lblNewLabel_8.setBounds(426, 14, 334, 14);
		genPanel.add(lblNewLabel_8);
		
		JLabel lblNewLabel_9 = new JLabel("tube material:");
		lblNewLabel_9.setBounds(10, 147, 186, 14);
		genPanel.add(lblNewLabel_9);
		
		JLabel lblNewLabel_10 = new JLabel("tubesheet material:");
		lblNewLabel_10.setBounds(386, 146, 192, 14);
		genPanel.add(lblNewLabel_10);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(25, 303, 735, 244);
		genPanel.add(scrollPane);
		
		textArea1 = new JTextArea();
		scrollPane.setViewportView(textArea1);
		textArea1.setFont(new Font("Consolas", Font.BOLD, 12));
		textArea1.setEditable(false);
	}
}
