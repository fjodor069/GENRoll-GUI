package tube;

import java.util.Arrays;
import java.util.Locale;
import java.util.Scanner;
import javax.swing.JOptionPane;

//import static members: 
import static java.lang.Math.*;

// GENROLL
// this program is an elastic-plastic analysis of 
// rolling-expanding tubes into tubesheet holes
//
// the method is based on Singh & Soler, "Mechanical Design of Heatexchangers", app 7D, p.368
//
// this book gives a listing of a program in Fortran66 for the TRS80 (1982) 
//
// the original Fortran code was translated to Java 
// and tested with Eclipse and jre 1.8 /jdk 14.0.1
//
// there is limited input checking or exception handling !!!
//
//  note: - in Java all array indexes begin with 0 instead of 1
//  	 - notation is not Java standard
//
// 15-3-2015		basic translation is complete	
// 23-9-2015		start checking and validating
// 12-10-2015		some bugs found in Coeff()
// 3-11-2020		new check
//					note all variables are public in this package
//					removed some static statements
//					added global constant NUM_LAYERS
// 11-11-2020		created class MatrixEquations for array methods
//					and new GaussianElimination solver lsolve 					
//
// 15-11-2020		first time run with clean solve !!!
//
// 2-1-2021			finally solved the bug in Coeff
//
//
// 8-2-2022			add GUI in eclipse
// 9-2-2022			make get/set properties for input
//

public class GENRoll
{
	private Locale locale;
	private final int NUM_LAYERS = 10;
	
	//====input variables====
	private double ET,SYT,ALPHAT;			//tube material data
	private double ES,SYS,ALPHAS; 			//tubesheet material data
	double DOUT;					//outside diameter of tube
	double T0;						//wall thickness of tube
	double ANU;						//poisson ratio of tube material
	double C;						//radial clearance between tube and tube hole
	double P00;						//assumed maximum rolling pressure in F
	double TOP;						//tubesheet operating temperature  in F
	double TROLL;					//assembly temperature when rolling takes place (ambient)
	double DP00;					//value of rolling pressure increment
	double DT0;						//value of temperature increment
	double HS;						//tubesheet layer thickness
	double PSTEP;					//printout step size during mechanical loading 
	double TSTEP;					//printout step size during thermal loading
	boolean NPR;					//normal or expanded printout
	boolean NRETUN;					//return to assembly temperature after thermal loading
	
	//=====================
	// calculated variables
	double P0;
	double DP0;

	double DT;							//delta T temperature increment in loadcase temperature
	double[] R = new double[10];		//radius
	double[][] A = new double[19][19];	//holds coefficients of linear equations
	double[] B = new double[19];		//result of linear equations
	double[] P = new double[10];		//contact pressure
	double[] DIS = new double[10];		//DIS = interface displacement between layer i and i+1 
	double[] ALAMD = new double[10];
	double[] TTH = new double[10];		//TTH = current value of layer thickness of layer i
	boolean[] K = new boolean[10];		//check if layer yields or not
	boolean[] KJ1 = new boolean[10];	//layer yield or not
	double[] SR = new double[10];		//radial stress
	double[] ST = new double[10];		//tangential stress
	double[] DPJM1 = new double[10];
	double[] DPJ = new double[10];
	double[] DPIM1 = new double[11];
	double[] DPI = new double[11];
	double[] ERI = new double[10];
	double[] ETI = new double[10];
	double[] STR = new double[10];
	double[] ETJ = new double[10];
	double[] ERJ = new double[10];
	double[] SRT = new double[10];
	double[] B1 = new double[10];
	double[] B2 = new double[10];
	double[] DEPSR = new double[10];
	double[] DEPST = new double[10];
	double[] DP = new double[9];
	double[] DUUP = new double[10];
	double[] EQUI = new double[10];			//equivalent stress in layer
	int 	NN;								//telt iets
	
	double R0;
	boolean NPLAS;		//tube deforms plastic or not 
	double T0R;			//ratio of tube thickness/radius
	double SQT;			//help factor
	double PP;
	double U;
	double DTE;
	double DTP = 0.0;
	
	double CCC;
	
	double T;

	double GT;
	double GS;

	double GGG1;

	double GGG2;
	double REDT = 0.0, REDS = 0.0, REDC = 0.0, REDA = 0.0;
	double OMEGA = 0.0, RSTA = 0.0,TSTA = 0.0, RE, RHOLE;
	double TEMP;
	double PCRIT, PCRIS;
	double PFINAL;
	double PFF;
	double PPP;
	double P0A;
	double P0MAX;
	double PIRMT;
	long KK;
	
	int NSTAGE;

	
	//1 = loading , 2 = unloading, 3 = temperature change up/down
	private enum Stage {loading,unloading,temperature};
	
	private Stage myStage; 
	boolean bStageExit = false;
	
	String message;
	
	//
	// constructor code
	// 
	// not used anymore in GUI app
	public GENRoll()
	{
		locale = Locale.US;
			
		
		ReadInput();
		
//		PrintInput();
//		PrintMaterial();
//		
//
//		Calculate();
		
				
	}
	
	
	//get / set methods for private fields
	public void setLocale(Locale loc)
	{
		this.locale = loc;
	}
	public Locale getLocale()
	{
		return this.locale;
		
	}
	
	public void ReadKey()
	{
		//om te debuggen
		JOptionPane.showInputDialog("Enter something to continue");
		
	}
	
	public void ReadInput()
	{
		Scanner input = new Scanner(System.in);
		
		input.useLocale(locale);
		//prompt the user to enter three numbers
		// TOD = tube outer diameter [inch]
		// TT = tube nominal wall thickness [inch]
		// C = radial clearance between tube and hole [inch]
	
		System.out.println("Generating input");
		// prompt the user to enter four numbers
		// ET = youngs modulus of tube material [psi]
		// ES = youngs modulus of tubesheet material [psi]
		// SYT = yield stress of tube material [psi]
		// SYS = yield stress of tubesheet material [psi]
		//
		//example CuNi 70:30 tube in CuNi 90:10 tubesheet
		ET = 2.2E7;
		SYT = 1.8E4;
		ALPHAT = 9E-6;
		DOUT = 0.75;
		T0 = 0.049;
		ES = 1.8E7;
		SYS = 1.5E4;
		ALPHAS = 9.5E-6;
		C = 0.005;
		HS = 0.085;
		TROLL = 70;
		TOP = 300;
		P00 = 19660;
		DP00 = 10;
		DT0 = 10;
		
		ANU = 0.3;
		
		PSTEP = 100;
		TSTEP = 10;
		
		NPR = false;
		//NPR = true;
		//System.out.print("Enter ET, ES, SYT, SYS : ");
		//ET = input.nextDouble();
		//ES = input.nextDouble();
		//SYT = input.nextDouble();
		//SYS = input.nextDouble();
		System.out.println();
		input.close();
	
	}
	
	
	public double getET()
	{
		return ET;
	}


	public void setET(double eT)
	{
		ET = eT;
	}


	public double getSYT()
	{
		return SYT;
	}


	public void setSYT(double sYT)
	{
		SYT = sYT;
	}


	public double getALPHAT()
	{
		return ALPHAT;
	}


	public void setALPHAT(double aLPHAT)
	{
		ALPHAT = aLPHAT;
	}


	public double getES()
	{
		return ES;
	}


	public void setES(double eS)
	{
		ES = eS;
	}


	public double getSYS()
	{
		return SYS;
	}


	public void setSYS(double sYS)
	{
		SYS = sYS;
	}


	public double getALPHAS()
	{
		return ALPHAS;
	}


	public void setALPHAS(double aLPHAS)
	{
		ALPHAS = aLPHAS;
	}


	//
	// this is the complete calculation ....
	//
	public boolean Calculate()
	{
		
		System.out.println("****CALCULATION****");
		//determine initial contact state of the tube
		R0 = 0.5*(DOUT-T0);
		NPLAS = false;
		T0R = T0/R0;
		
		SQT = sqrt(1.0 - 0.5*T0R + 0.25*T0R*T0R);
		
		PP = T0R*SYT/SQT;
		U = R0*PP*(1.0/T0R - 0.5*ANU)/ET;
		DTE = T0*PP*(-0.5 - ANU/T0R + 0.5*ANU)/ET;
		
		if ( U + 0.5*DTE > C  )
		{
			//line 100
			//tube remains elastic when contact begins
			System.out.println("Tube remains elastic when contact begins.");
			//initial contact state for elastic tube case:
			CCC = T0*(ANU/T0R + 0.5 - 0.5*ANU)-2.0*R0*(1.0/T0R - 0.5 + 0.5*ANU);
			P0 = -2.0*ET*C/CCC;
			T = T0*(1.0 + 2.0*C*(ANU/T0R + 0.5 - 0.5*ANU)/CCC);
		}
		else
		{
			NPLAS = true;
			//tube yields prior to contact; (that is good)
			System.out.println("Tube yields prior to contact.");
			//initial contact state for tube yield case:
			P0 = PP;
			GT = ET/(2.0*(1.0 + ANU));
			GS = ES/(2.0*(1.0 + ANU));
			GGG1 = R0*PP/(4.0*GT*T0) - 0.5 + 1.0/T0R;
			GGG2 = 1.0 - R0*PP/(2.0*GT*T0);
			REDT = -(0.5 + ANU*GGG1/GGG2)/ET;
			REDS = ANU*PP*PP/(T0R*T0R*GGG2*ET) + PP*(0.5 + 1.0/T0R)/3.0;
			REDC = -(2.0/T0R - 0.5)/(T0R*T0R*GGG2*((2.0/T0R - 0.5)*GGG1/GGG2 + 0.5/T0R + 0.25));
			REDA = ((GGG1*REDC + 1.0/(T0R*T0R))/GGG2 + 0.5*ANU*REDC)*PP*PP/ET + PP*(2.0/T0R - 0.5)/3.0;
			OMEGA = (REDT*REDC*PP*PP - REDS)/REDA;
			RSTA = R0*(1.0 + (1.0/T0R - 0.5 + 0.5*ANU)*PP/ET);
			TSTA = T0*(1.0 + (0.5*ANU - 0.5 - ANU/T0R)*PP/ET);
			RE = RSTA + 0.5*TSTA;
			RHOLE = 0.5*DOUT + C;
			DTP = OMEGA*TSTA/RSTA*(RHOLE - RE)/(1.0 + 0.5*TSTA/RSTA*OMEGA);
			T = T0 + DTE + DTP;
		}

		//line 200
		//some initial values and constants
		R[0] = 0.5*DOUT + C - 0.5*T;
		T0R = T/R[0];
		SQT = sqrt(1.0 - 0.5*T0R + 0.25*T0R*T0R);
		
		//recalculate P0 at the point of initial contact
		if (NPLAS) 
			P0 = T0R*SYT/SQT;
		
		//System.out.printf("R[0] = %6.4f T0R = %6.4f SQT = %6.4f OMEGA = %6.4f Rhole = %6.4f Dtp = %6.4f \n",R[0],T0R,SQT,OMEGA,RHOLE,DTP);
		//System.out.printf("GT = %6.1f GS = %6.1f \n", GT,GS);
		
		for (int i = 0; i <= 8; i++)
		{
			int j = i + 1;
			R[j] = 0.5*DOUT + C + (0.5 + i)*HS;
			P[i] = 0.0;
			DIS[i] = 0.0;
			ALAMD[i] = 0.0;
			if (i == 0) 
				TTH[i] = T;
			else
			    TTH[i] = HS;
		}
			
		//PrintSingleMatrix(R);
		
		if (NPLAS)
			ALAMD[0] = DTP/(OMEGA*TSTA*REDA);
		
		P[9] = 0.0;
		DIS[9] = 0.0;
		ALAMD[9] = 0.0;
		TTH[9] = HS;
		TEMP = TROLL;
		
		//limiting values for roll and contact pressure
		//as function of the uniaxial yield stress of tube and tubesheet
		PCRIT = SYT * sqrt(4.0/3.0);
		PCRIS = SYS * sqrt(4.0/3.0);
		

		//set state of layers to false; not yielded
		Arrays.fill(K, false);
		
		//line 350
		if (NPLAS)
		{
			K[0] = true;
			KJ1[0] = true;
		}
		
		//check if maximum rolling pressure is not too small
		if (P00 <= P0)
		{
			
			System.out.printf(locale,"P00= %6.3f (psi) P0 = %6.3f (psi) \n",P00,P0);
			System.out.println("Maximum rolling pressure too small. Initial contact can not occur.");
			// exit the calculation
			return false;
		}
		
		//line 500
		myStage = Stage.loading;		//must be the same as NSTAGE = 1
		NSTAGE = 1;
		
		PPP = P0/PSTEP;
		
		//KK is het aantal stappen van rolling pressure
		KK = (int) (PPP) + 1;			//in Fortran: KK = IFIX(PPP) + 1

		
		DP0 = DP00;
		DT = 0.0;
		
		//een teller....
		NN = 1;
		
		ComputeStressesForEachLayer();
		//line 600
		// begin van een loop
		
		do //until bStageExit
		{
		
			
			if (NSTAGE != 1 || NN != 1)
			{
				//compute interface displacements and layer thicknesses
				//System.out.println("compute displacements");
				ComputeDisplacementsForEachLayer();
				
				//line 5300
				//compute stresses for each layer
				ComputeStressesForEachLayer();
			}
		
			//line 10000
			//check all layers for yielding
			//set results in array K[]
			DetermineYield(myStage);
	
						
			//line 8000
			if (NN == 1)
			{
				PrintInitialStage(myStage);
				if (NSTAGE > 1)
				  NN = 2;
			}
			
			PrintStage(myStage);
			
			//System.out.println("now checking the stage");
			if (CheckStage(myStage))
			{
				//go to the next stage
				
				//line 840
				NSTAGE = NSTAGE + 1;
				//System.out.printf(locale," Now into stage NSTAGE = %d ",NSTAGE);
				//System.out.println();
				
				if (NSTAGE > 4)
					bStageExit = true;
				switch (NSTAGE)
				{
					case 1: 
						myStage = Stage.loading;
						break;
					case 2: 
						myStage = Stage.unloading;
						PFINAL = P0;
						PFF = (double) PSTEP *((int) PFINAL/PSTEP);	//FLOAT(IFIX(PFINAL/PSTEP))
						
						KK = 1;
						NN = 1;
						DP0 = -DP00;
						DT = 0.0;
							
					
						//
						break;
					case 3 : 
						myStage = Stage.temperature;
						KK = 1;
						NN = 1;
						DP0 = 0.0;
						if (DT0 == 0.0)
							bStageExit = true;
						if (TOP == TROLL)
							bStageExit = true;
						if (TOP < TROLL)
							DT = -DT0;
						else
							DT = DT0;
						
					
						
						
						break;
					case 4:  
						myStage = Stage.temperature;
						break;
					
				}
				
				
			}
			else
			{
				//==== SOLVING ====
				//System.out.println("beginning solving");
				//System.out.printf(locale," NSTAGE = %d  NN = %d     bStageExit = %b \n",NSTAGE,NN,bStageExit);
				//line 1600
				// 
				if (NSTAGE == 1 && NN == 1)
				{
				//	System.out.println("NSTAGE == 1 && NN == 1 ");
					P0A = P0;
					P0 = DP0 * (int) P0/DP0 + 1;
					DP0 = P0 - P0A;
					NN = 2;
					P0 = P0A;
					
				}		
				//line 1650
								
				
				//fill in coefficients for linear equations
				Coeff(A,B);
							
			
				//=====================
				//System.out.println("solve linear equations");
				//solve the linear equations met Leqt
				
				
				B = MatrixEquations.lsolve(A, B);
				
				
				//line 1700
				//compute pressure between layers
				
				
				//fill in the results from the equation solving
				//System.out.println("compute pressure between layers");
				for (int i = 0; i < P.length-1; i++)
				{
					int j = 2*i;
					DP[i] = B[j];
					P[i] = P[i] + DP[i];
					ALAMD[i] = B[j+1];
				}
				ALAMD[9] = B[18];

				//=============
			
				P0 = P0 + DP0;
				TEMP = TEMP + DT;
				

				
				
			} // == END SOLVING ===
			
//		
			
		} while (bStageExit != true);
		
		//loop to 600
		
		return true;
	}//====END CALCULATE =========
	
	//print a onedimensional matrix
	// utility function for debugging contents
	public static void PrintSingleMatrix(double[] Mx)
	{
		for (int i = 0; i < Mx.length; i++)
		{
			System.out.printf(" %5.10f", Mx[i]);
		}
		System.out.println();
	}
	
	//print a twodimensional matrix
		// M and N is the size and can also be read as Mx.length
		public static void PrintDoubleMatrix(double[][] Mx, int M, int N)
		{
			for (int i = 0; i < N; i++)
			{
				for (int j = 0; j < M; j++)
					System.out.printf(" %5.4f ",Mx[i][j]) ;
				System.out.println();
			}
					
		}
	
	//
	// replaces lines 10000, 20000, 30000 with one subroutine
	// for stages loading, unloading and temperature change state 
	// this routine checks each layer if it yields or remains yield
	// returns: K[i] filled with booleans
	// if it yields then set K[i] = 1(true) else K = 0(false)
	//			EQUI[i] equivalent stress in the layer
	//
	public void DetermineYield(Stage st)
	{
		switch (st)
		{
		case loading:
			DetermineYieldLoading();
			break;
			
		case unloading:
			DetermineYieldUnloading();
			break;
			
		case temperature:
			DetermineYieldTemperature();
			break;
		}
		
	
		
	}
	
	public void DetermineYieldLoading()
	{
		for (int i = 0; i < K.length; i++)
		{
			//is layer not yielded already, do the check
			if (K[i] == false)
			{
				//calculate equivalent stress in the layer (10050)
				EQUI[i] = sqrt(ST[i]*ST[i]-ST[i]*SR[i]+SR[i]*SR[i]);
				//for first layer, max yield is the tube yield stress SYT (10200)
				if (i == 0)
				{
					if (EQUI[i] < SYT)
					{
					  //calculated, no yielding
					  K[i] = false;
					}
					else
					{
						//there is yield, it must be equal to tube yield
						K[i] = true;
						EQUI[i] = SYT;
					}
				}
				else // for other layers, max yield is tubesheet yield stress SYS 
				{
					if (EQUI[i] < SYS)
					{
						K[i] = false;
					}
					else
					{
						K[i] = true;
						EQUI[i] = SYS;
					}
				}
				
				
			}
			else //layer is already yielded
			{
				K[i] = true;
				if (i == 0)
					EQUI[i] = SYT;
				else
					EQUI[i] = SYS;
						
			}
		}//end check for layer yield==============	
		
	
//		
	}
	
	public void DetermineYieldUnloading()
	{
		for (int i = 0; i < K.length; i++)
		{
			EQUI[i] = sqrt(ST[i]*ST[i]-ST[i]*SR[i]+SR[i]*SR[i]);
			
			if (ST[i] < SR[i])
			{
				if (i == 0)
				{
					if (EQUI[i] < SYT)
					{
						K[i] = true;
						EQUI[i] = SYT;
					}
					else
					{
						K[i] = false;
					}
						
				}
				else
				{
					if (EQUI[i] < SYS)
					{
						K[i] = false;
					
					}
					else
					{
						K[i] = true;
						EQUI[i] = SYS;
					}
				}
				
			}
			else
			{
				K[i] = false;
				
				if (i == 0)
				{
					if (EQUI[i] > SYT)
						EQUI[i] = SYT;
				}
				else
				{
					if (EQUI[i] > SYS)
						EQUI[i] = SYS;
				}
								
			}
			
		}
		
	}
	
	//lines 30000 - 33000
	public void DetermineYieldTemperature()
	{
		for (int i = 0; i < K.length; i++)
		{
			EQUI[i] = sqrt(ST[i]*ST[i]-ST[i]*SR[i]+SR[i]*SR[i]);
			
			if (((ALPHAT - ALPHAS)*DT) >= 0.0)
			{
				//thermal expansion
				if (i == 0)
				{
					if (EQUI[i] < SYT)
					{
						K[i] = false;
					}
					else
					{
						EQUI[i] = SYT;
						K[i] = true;
					}
				}
				else
				{
					if (EQUI[i] < SYS)
					{
						K[i] = false;
					}
					else
					{
						EQUI[i] = SYS;
						K[i] = true;
						if (EQUI[i] == SYS && ALAMD[i] < 0.0)
						{
							K[i] = false;
						}
					}
				}
				
				
			}
			else
			{
				//thermal contraction
				//for first layer
				if (i == 0)
				{
					if (EQUI[i] > SYT)
					{
						EQUI[i] = SYT;
					}
						K[i] = false;
					
				}
				else
				{
					if (EQUI[i] > SYS)
						EQUI[i] = SYS;
					K[i] = false;
					if (EQUI[i] == SYS)
						K[i] = true;
					if (EQUI[i] == SYS && ALAMD[i] <= 0.0)
					  	K[i] = false;
					
				}
				
				
			}
			
			
		}
		
		
		
		
		//throw new UnsupportedOperationException("method not implemented");
	}
	
	
	//replace lines 550 and 5300 with one routine
	public void ComputeStressesForEachLayer()
	{
		// calculate the radial and circumferential stress in each layer 
		// dependent on the rolling pressure in that layer
		//
		SR[0] = -0.5*(P0 + P[0]);
		ST[0] = (P0 - P[0])*R[0]/TTH[0] + SR[0];
				
		for (int i = 1; i < SR.length; i++)
		{
			SR[i] = -0.5*(P[i-1] + P[i]);
			ST[i] = (P[i-1] - P[i])*R[i]/TTH[i] + SR[i];
		}
		

	}
	
	//replace lines 3500 to 5300 with one routine
	public void ComputeDisplacementsForEachLayer()
	{
		//
		//System.out.println("compute displacements for each layer ");
		//line 3500
		// if NSTAGE > 1 OR NN > 1
		//compute current values of DIS[i] and TTT
		for (int i = 0; i < NUM_LAYERS; i++)
		{
			double E;
				
			if (ALAMD[i] == 0)
				KJ1[i] = false;
			else
			{
				//KJ1[i] = (1 == 2);
				//System.out.printf("ALAMD[i] = %8.2f",ALAMD[i]);

				//System.out.println();
				KJ1[i] =  (  (int) ( ALAMD[i]/ abs(ALAMD[i])) > 0)  ;
			}		
			if (i == 0)
				E = ET;
			else 
				E = ES;
					
			DPJM1[i] = -(0.5 + ANU*R[i]/TTH[i] - 0.5*ANU)/E;
			DPJ[i] = (-0.5 + ANU*R[i]/TTH[i] + 0.5*ANU)/E;
			DPIM1[i] = (R[i]/TTH[i] - 0.5 + 0.5*ANU)/E;
			DPI[i] = (-R[i]/TTH[i] - 0.5 + 0.5*ANU)/E;
			ERI[i] = (ST[i] - SR[i])/E;
			ETI[i] = 1.0 - ERI[i];
			STR[i] = (2.0*ST[i] - SR[i])/3.0;
			ETJ[i] = ANU*ERI[i];
			ERJ[i] = 1.0 - ETJ[i];
			SRT[i] = (2.0*SR[i] - ST[i])/3.0;
					
		}
				
		B1[0] = DPJM1[0]*DP0 + DPJ[0]*DP[0] + SRT[0]*ALAMD[0] + ALPHAT*DT;
		B2[0] = DPIM1[0]*DP0 + DPI[0]*DP[0] + SRT[0]*ALAMD[0] + ALPHAT*DT;
				
		for (int i = 1; i < NUM_LAYERS-1; i++)
		{
			B1[i] = DPJM1[i]*DP[i-1] + DPJ[i]*DP[i] + SRT[i]*ALAMD[i] + ALPHAT*DT;
			B2[i] = DPIM1[i]*DP[i-1] + DPI[i]*DP[i] + SRT[i]*ALAMD[i] + ALPHAT*DT;
		}
		B1[9] = DPJM1[9]*DP[8] + SRT[9]*ALAMD[9] + ALPHAT*DT;
		B2[9] = DPIM1[9]*DP[8] + SRT[9]*ALAMD[9] + ALPHAT*DT;
				
		for (int i = 0; i < NUM_LAYERS; i++)
		{
			DEPSR[i] = (ETI[i]*B1[i] - ETJ[i]*B2[i])/(ERJ[i]*ETI[i] - ETJ[i]*ERI[i]);
			DEPST[i] = (B2[i] - ERI[i]*DEPSR[i])/ETI[i];
			DUUP[i] = R[i]*DEPST[i] + 0.5*TTH[i]*DEPSR[i];
			//DIS[i] is the interface displacement between layer i and i+1
			DIS[i] = DIS[i] + DUUP[i];
			//TTH[i] is the layer thickness
			TTH[i] = TTH[i]*(1.0 + DEPSR[i]);
			R[i] = R[i]*(1.0  + DEPST[i]);
		}
		
	}
	
	
	public void PrintInput()
	{
		// print the input variables
		System.out.println();
		System.out.println("****INPUT DATA****");
		System.out.printf(locale,"Tube outside diameter DOUT= %6.3f (in) \n",DOUT );
		System.out.printf(locale,"Tube wall thickness T0= %6.3f (in) \n",T0 );
		System.out.printf(locale,"Poisson ratio ANU= %6.3f (-) \n",ANU);
		System.out.printf(locale,"Radius clearance C= %6.3f (in) \n",C);
		System.out.printf(locale,"Maximum rolling pressure P00= %6.3f (psi) \n",P00);
		System.out.printf(locale,"Rolling temperature TROLL= %6.3f (degs.F) \n",TROLL);
		System.out.printf(locale,"Operating temperature TOP= %6.3f (degs.F) \n",TOP);
		
		System.out.printf(locale,"Step size of rolling pressure increment DP00= %6.3f (psi) \n",DP00);
		System.out.printf(locale,"Step size of temperature increment DT0= %6.3f (degs.F) \n",DT0);
		System.out.printf(locale,"Layer thickness assumed HS= %6.3f (in) \n",HS);
		System.out.printf(locale,"Printout step size for rolling increment PSTEP= %6.3f \n",PSTEP);
		System.out.printf(locale,"Printout step size for temperature TSTEP= %6.3f \n",TSTEP);
		System.out.println("Printout options NPR= " + NPR);
		System.out.println("Temperature return options NRETUN= " + NRETUN);
		System.out.println();
		
	}
	
	public void PrintMaterial()
	{
		//print the output
		System.out.println();
		System.out.println("****RESULT PRINTOUT****");
		System.out.println("Following results are for: ");
		System.out.printf(locale,"Youngs Modulus of tube ET= %6.3f (psi) \n",ET );
		System.out.printf(locale,"Yield strength of tube SYT= %6.3f (psi) \n",SYT );
		System.out.printf(locale,"Thermal expansion coefficient of tube ALPHAT= %6.3e \n",ALPHAT );
		System.out.printf(locale,"Youngs Modulus of tubesheet ES= %6.3f (psi) \n",ES );
		System.out.printf(locale,"Yield strength of tubesheet SYS= %6.3f (psi) \n",SYS );
		System.out.printf(locale,"Thermal expansion coefficient of tubesheet ALPHAS= %6.3e \n",ALPHAS );
		
		
		
		System.out.println("**********************************");	
		System.out.println();
	}
	
	public void PrintInitialStage(Stage st)
	{
		System.out.println();
		switch (st)
		{
		case loading :
			System.out.println("   (1) LOADING STAGE.");
			System.out.println("Rolling pressure   Contact Pressure  Plastic Layers         Pressures between layers ");
			System.out.println("    (psi)                (psi)       1 2 3 4 5 6 7 8 9 10   2-3   3-4   4-5   5-6   6-7   7-8   8-9   9-10");
			
			break;
		case unloading:
			System.out.println("   (2) UNLOADING STAGE.");
			System.out.println("Rolling pressure   Contact Pressure  Plastic Layers         Pressures between layers ");
			System.out.println("(psi)              (psi)             1 2 3 4 5 6 7 8 9 10   2-3   3-4   4-5   5-6   6-7   7-8   8-9   9-10");
			
			break;
			
		case temperature:
			System.out.println("   (3) TEMPERATURE CHANGE STAGE.");
			System.out.println("Temperature        Contact Pressure  Plastic Layers ");
			System.out.println("(degs.F)           (psi)             1 2 3 4 5 6 7 8 9 10   2-3   3-4   4-5   5-6   6-7   7-8   8-9   9-10");
			
			break;
		}
		
		//line 8000 etc.
		if (st == Stage.loading || st == Stage.unloading)
		{
			//System.out.println("now in PrintInitialStage");
			System.out.printf(locale,"   %8.1f          %8.1f        ",P0,P[0]);
			for (int i = 0; i <= 9; i++)
			{
				
				if (KJ1[i])
				
				  System.out.print("1 ");
				else
				  System.out.print("0 ");
			}
			for (int i = 1; i <= 8; i++)
			{
				  System.out.printf(locale,"%6.0f",P[i]);
			}
			System.out.printf(locale,"     %7.6f",TTH[0]);
			
						
			System.out.println();
			
		
		
		}
		else if (st == Stage.temperature )
		{
			System.out.printf(locale,"   %8.1f          %8.1f        ",TEMP,P[0]);
			for (int i = 0; i <= 9; i++)
			{
				if (KJ1[i])
				  System.out.print("1 ");
				else
				  System.out.print("0 ");
			}
			for (int i = 1; i <= 8; i++)
			{
				  System.out.printf(locale,"%6.0f",P[i]);
			}
			System.out.printf(locale,"     %7.6f",TTH[0]);
			
			System.out.println();
			//
			
		}
		if (NPR) //(expanded output)
		  PrintDetailed();		

		
	}
	
	public void PrintStage(Stage st)
	{
		//printout
		//line 750 , 900, 1400, 1570
		//System.out.println("now in PrintStage");
		//System.out.printf(locale,"rolling pressure P0 = %8.1f contact pressure P[0] = %8.1f KK = %d  PSTEP = %8.1f   \n",P0,P[0],KK,PSTEP);
		
		switch(st)
		{
		case loading:
			//---------------
			//System.out.println("case loading");
			//System.out.printf(locale," P0 = %8.1f  P[0] = %8.1f   ",P0,P[0]);
			//System.out.println();
			//--------------------
			//System.out.printf("P0 = %8.1f KK = %d PSTEP = %8.1f \n", P0,KK,PSTEP);
			//KK is calculated as P0/PSTEP
			if (P0 >= (double)(KK*PSTEP))
			{
				//printout of result
				System.out.printf(locale,"   %8.1f          %8.1f        ",P0,P[0]);
				for (int i = 0; i <= 9; i++)
				{
					if (KJ1[i])				//KJ1
					//if (K[i])
					  System.out.print("1 ");
					else
					  System.out.print("0 ");
				}
				for (int i = 1; i <= 8; i++)
				{
					  System.out.printf(locale,"%6.0f",P[i]);
				}
				System.out.printf(locale,"     %7.6f",TTH[0]);
				System.out.println();

				KK++;
			}
			else //debugg
			{
				System.out.printf(locale,"   %8.1f          %8.1f        ",P0,P[0]);
				for (int i = 0; i <= 9; i++)
				{
					if (KJ1[i])				
				
					  System.out.print("1 ");
					else
					  System.out.print("0 ");
				}
				for (int i = 1; i <= 8; i++)
				{
					  System.out.printf(locale,"%6.0f",P[i]);
				}
				System.out.printf(locale,"     %7.6f",TTH[0]);
				System.out.println();
			}
			break;
			
		case unloading:
			if (KK == 1)
			{
				if (P0 > PFF)
					return;
			}
			else
				if (P0 > (PFF - PSTEP*(KK-1)))
					return;
			
			System.out.printf(locale,"   %8.1f          %8.1f        ",P0,P[0]);
			for (int i = 0; i <= 9; i++)
			{
				if (KJ1[i])
				  System.out.print("1 ");
				else
				  System.out.print("0 ");
			}
			for (int i = 1; i <= 8; i++)
			{
				  System.out.printf(locale,"%6.0f",P[i]);
			}
			System.out.printf(locale,"     %7.6f",TTH[0]);
			System.out.println();

			KK++;
			
			break;
			
		case temperature:
			
			//line 1400
			if (TOP >= TROLL)
			{
				if (TEMP < (TROLL + KK*TSTEP))
				
					return;
			}	
			else
			{
					if (TEMP > (TROLL -KK*TSTEP))
						return;
			}
			
			System.out.printf(locale,"   %8.1f          %8.1f        ",TEMP,P[0]);
			for (int i = 0; i <= 9; i++)
			{
				if (KJ1[i])
				  System.out.print("1 ");
				else
				  System.out.print("0 ");
			}
			for (int i = 1; i <= 8; i++)
			{
				  System.out.printf(locale,"%6.0f",P[i]);
			}
			System.out.printf(locale,"     %7.6f",TTH[0]);
			System.out.println();
			
			KK++;
			
			break;
		}
		
		
		
		if (NPR) //  == 1 (expanded output)
		  PrintDetailed();
					
		
		
	}

	public void PrintDetailed()
	{
		//detailed printout
		//System.out.println();
		System.out.print("Interface displacements :");
		for (int i = 0; i <= 8; i++)
		{
			System.out.printf(locale,"%9.7f ",DIS[i]);
		}
		System.out.println();
		System.out.print("Layer thickness         :");
		for (int i = 1; i <= 9; i++)
		{
			System.out.printf(locale,"%9.7f ",TTH[i]);
		}
		System.out.println();
		
		System.out.print("                         ");
		for (int i = 0; i <= 9; i++)
		{
			System.out.printf(locale,"%8.1f ",ST[i]);
		}
		System.out.println();
		
		System.out.print("                         ");
		for (int i = 0; i <= 9; i++)
		{
			System.out.printf(locale,"%8.1f ",SR[i]);
		}
		System.out.println();
		
		System.out.print("                         ");
		for (int i = 0; i <= 9; i++)
		{
			System.out.printf(locale,"%8.1f ",EQUI[i]);
		}
		System.out.println();
		
		System.out.print("ALAMD                    :");
		for (int i = 1; i <= 9; i++)
		{
			System.out.printf(locale,"%9.7f ",ALAMD[i]);
		}
		System.out.println();
		
		
		
		
	}
	
	// 
	// check when to go to the next stage 
	// line 800
	// returns true: when to go to the next stage
	// if false: keep incrementing in current stage
	public boolean CheckStage(Stage st)
	{
		//System.out.println("CheckStage");
		switch (st)
		{
		case loading:
			//check for loading stage
			if ( (PCRIT-P0) <= DP00)
			{
				System.out.println("Rolling pressure reaches tube strength control critical value.");
				return true;
			}
			else if ( (PCRIS - P[0]) <= DP00)
			{
				System.out.println("Contact pressure reaches tubesheet strength control critical value.");
				return true;
			}
			else if (K[8] == true)
			{
				
				System.out.println("All layers except the outermost layer yield.");
				P0MAX = P0;
				return true;
			}
			else if ( P0 >= P00)
			{
				System.out.println("Rolling pressure reaches pre-specified value.");
				return true;
			}
			break;
			
		case unloading:
			
			if (P[0] <= 0.0)
			{
				System.out.println("No contact between tube and tubesheet.");
				//end the analysis
				bStageExit = true;
				return true;
			}
			if (P0 <= DP00)
			{
				if (P0 < 0.0001)
				{
					PIRMT = P[0];
					return true;
				}
				else
				{
				  DP0 = -P0 + 0.0001;
				  return false;
				}
			}
			break;
			
		case temperature:
			
			//check temperature change state
			//line 1480
			if (P[0] <= 0.0)
			{
				System.out.println("No contact between tube and tubesheet. Joint fails");
				//end the analysis
				bStageExit = true;
				return true;
			}
			//line 1500
			if (TOP < TROLL)
			{
				if (TEMP > TOP)
					return true;
			}	
			else
			{
				if (TEMP < TOP)
					return true;
			}
			
			
			
			break;
		
		
		}
		return false;
	}
	
	//
	// this method computes the coefficients of the simultaneous equations
	// to prepare for solving with the simultaneous equation solver
	// 
	// the twodimensional 19x19 array A is filled with the coefficients
	// and the one dimensional array B holds the equation results
	//  use as example: 	Coeff(A,B)
	//
	// note: there are N-1 equations for N layers acc. eq. 7.C.12:
	//       Ai*Pi + Bi*Pi ....  = Ri*T
	// 	and additional N equations acc. eq. 7.C.15
	//       ki*(Fi*Pi-1 + ..) = 0
	// in total 2N-1 equations
	//
	public void Coeff(double[][] A, double[] B)
	{
		//alle arrays beginnen met 0 ipv 1
		double E, G, ALPHA;
		
		double[] SOG = new double[10];
		double[] OMSOG = new double[10];
		double[] TERM1 = new double[10];
		double[] TERM2 = new double[10];
		double[] TERM3 = new double[10];
		double[] TERM4 = new double[10];
		double[] TERM5 = new double[10];
		double[] TERM6 = new double[10];
		double[] AA = new double[9];
		double[] BB = new double[9];
		double[] C = new double[9];
		double[] D = new double[9];
		double[] FF = new double[9];
		double[] CPJM1 = new double[9];
		double[] RTJ = new double[9];
		double[] CPJP1 = new double[9];
		double[] CPJ = new double[9];
		double[] CLJP1 = new double[9];
		double[] CLJ = new double[9];
		double[] RTJP1 = new double[9];
		double[] CKPJ = new double[10];
		double[] CKPJM1 = new double[10];
		double[] CKLJ = new double[10];
		double[] STMSR = new double[10];
		
		//int size = A.length;
		
		for (int i = 0; i < NUM_LAYERS; i++)
		{
			if (i == 0)
			{
				E = ET;
				G = GT;
			}
			else
			{
				E = ES;
				G = GS;
			}
			STMSR[i] = ST[i] - SR[i];
			SOG[i] = STMSR[i] / G;
			OMSOG[i] = 1.0 - 0.5*SOG[i];
			TERM1[i] = (0.25*SOG[i] - 0.5 + R[i]/TTH[i])/OMSOG[i] + 0.5*ANU;
			TERM2[i] = (0.25*SOG[i] - 0.5 - R[i]/TTH[i])/OMSOG[i] + 0.5*ANU;
			TERM3[i] = STMSR[i] / OMSOG[i] + E;
			TERM4[i] = 0.5 + ANU*(0.25*SOG[i] - 0.5 + R[i]/TTH[i])/OMSOG[i];
			TERM5[i] = 0.5 + ANU*(0.25*SOG[i] - 0.5 - R[i]/TTH[i])/OMSOG[i];
			TERM6[i] = E + ANU*STMSR[i]/OMSOG[i];
		}
		
		//formula 7.C.12 (9x)
		//
		for (int i = 0; i < NUM_LAYERS-1; i++)
		{
			AA[i] = (R[i+1]*TERM3[i+1] + 0.5*TTH[i+1]*ANU*STMSR[i+1]/OMSOG[i+1])/ES;
			BB[i] = (0.5*TTH[i+1]*TERM6[i+1] + R[i+1]*STMSR[i+1]/OMSOG[i+1])/ES;
		}
		
		
		for(int i = 0; i < NUM_LAYERS-1; i++)
		{
			if (i == 0)
			{
				E = ET;
				ALPHA = ALPHAT;
			}
			else
			{
				E = ES;
				ALPHA = ALPHAS;
			}
			C[i] = (R[i]*TERM3[i] - 0.5*TTH[i]*ANU*STMSR[i]/OMSOG[i])/E;
			D[i] = (0.5*TTH[i]*TERM6[i] - R[i]*STMSR[i]/OMSOG[i])/E;
			FF[i] = (R[i]*TERM2[i] - 0.5*TTH[i]*TERM5[i])/E;
			CPJM1[i] = -(R[i]*TERM1[i] - 0.5*TTH[i]*TERM4[i])/E;
			RTJ[i] = (R[i] + 0.5*TTH[i])*ALPHA;								//hier zat een fout
		}
		for (int i = 0; i < NUM_LAYERS-1; i++)
		{
			CPJP1[i] = (R[i+1]*TERM2[i+1] + 0.5*TTH[i+1]*TERM5[i+1])/ES;
			CPJ[i] = (R[i+1]*TERM1[i+1] + 0.5*TTH[i+1]*TERM4[i+1])/ES - FF[i];
			CLJP1[i] = -((AA[i] + 2.0*BB[i])*SR[i+1]-(2.0*AA[i] + BB[i])*ST[i+1])/3.0;
			CLJ[i] = -((2.0*C[i]-D[i])*ST[i] - (C[i] - 2.0*D[i])*SR[i])/3.0;
			RTJP1[i] = -(R[i+1] - 0.5*TTH[i+1])*ALPHAS;
		}
		//formula 7.C.15 (10x)
		for (int i = 0; i < NUM_LAYERS; i++)
		{
			CKPJ[i] = (2.0*ST[i] - SR[i]) * (0.25*SOG[i] - 0.5 - R[i]/TTH[i])/OMSOG[i] - 0.5*(2.0*SR[i] - ST[i]);
			CKPJM1[i] = (2.0*ST[i] - SR[i]) * (0.25*SOG[i] - 0.5 + R[i]/TTH[i])/OMSOG[i] - 0.5*(2.0*SR[i] - ST[i]);
			CKLJ[i] = (2.0*ST[i] - SR[i]) * (ST[i] - SR[i])*(ST[i]-SR[i])/OMSOG[i];
//				
			if (CKLJ[i] <= MatrixEquations.EPSILON)
				CKLJ[i] = 1.0;
		}
		
		
		//--------------------------------------------
		//clear array A
		for (int j = 0; j < 19; j++ )
			for (int i = 0; i < 19; i++)
				A[i][j] = 0.0;
		
		
		// now fill array A with coefficients 
		//eq. 7.C.12 (9x)
		A[0][0] = CPJ[0];
		if (K[0]) A[0][1] = CLJ[0]; 
		A[0][2] = CPJP1[0];
		if (K[1])  A[0][3] = CLJP1[0];  
		A[1][0] = CPJM1[1];
		A[1][2] = CPJ[1];
		if (K[1]) A[1][3] = CLJ[1]; 
		A[1][4] = CPJP1[1];
		if (K[2]) A[1][5] = CLJP1[1]; 
		A[2][2] = CPJM1[2];
		A[2][4] = CPJ[2];
		if (K[2]) A[2][5] = CLJ[2];
		A[2][6] = CPJP1[2];
		if (K[3]) A[2][7] = CLJP1[2];
		A[3][4] = CPJM1[3];
		A[3][6] = CPJ[3];
		if (K[3]) A[3][7] = CLJ[3];
		A[3][8] = CPJP1[3];
		if (K[4]) A[3][9] = CLJP1[3];
		A[4][6] = CPJM1[4];
		A[4][8] = CPJ[4];
		if (K[4]) A[4][9] = CLJ[4];
		A[4][10] = CPJP1[4];
		if (K[5]) A[4][11] = CLJP1[4];
		A[5][8] = CPJM1[5];
		A[5][10] = CPJ[5];
		if (K[5]) A[5][11] = CLJ[5];
		A[5][12] = CPJP1[5];
		if (K[6]) A[5][13] = CLJP1[5];
		A[6][10] = CPJM1[6];
		A[6][12] = CPJ[6];
		if (K[6]) A[6][13] = CLJ[6];
		A[6][14] = CPJP1[6];
		if (K[7]) A[6][15] = CLJP1[6];
		A[7][12] = CPJM1[7];
		A[7][14] = CPJ[7];
		if (K[7]) A[7][15] = CLJ[7];
		A[7][16] = CPJP1[7];
		if (K[8]) A[7][17] = CLJP1[7];
		A[8][14] = CPJM1[8];
		A[8][16] = CPJ[8];
		if (K[8]) A[8][17] = CLJ[8];
		if (K[9]) A[8][18] = CLJP1[8];
		//-----------------------------------------
		//eq. 7.C.15 (10 x)
		if (K[0]) A[9][0] = CKPJ[0];
		A[9][1] = CKLJ[0];
		if (K[1]) A[10][0] = CKPJM1[1];
		if (K[1]) A[10][2] = CKPJ[1];
		A[10][3] = CKLJ[1];
		if (K[2]) A[11][2] = CKPJM1[2];
		if (K[2]) A[11][4] = CKPJ[2];
		A[11][5] = CKLJ[2];
		if (K[3]) A[12][4] = CKPJM1[3];
		if (K[3]) A[12][6] = CKPJ[3];
		A[12][7] = CKLJ[3];
		if (K[4]) A[13][6] = CKPJM1[4];
		if (K[4]) A[13][8] = CKPJ[4];
		A[13][9] = CKLJ[4];
		if (K[5]) A[14][8] = CKPJM1[5];
		if (K[5]) A[14][10] = CKPJ[5];
		A[14][11] = CKLJ[5];
		if (K[6]) A[15][10] = CKPJM1[6];
		if (K[6]) A[15][12] = CKPJ[6];
		A[15][13] = CKLJ[6];
		if (K[7]) A[16][12] = CKPJM1[7];
		if (K[7]) A[16][14] = CKPJ[7];
		A[16][15] = CKLJ[7];
		if (K[8]) A[17][14] = CKPJM1[8];
		if (K[8]) A[17][16] = CKPJ[8];
		A[17][17] = CKLJ[8];
		if (K[9]) A[18][16] = CKPJM1[9];
		A[18][18] = CKLJ[9];
		
		
		//..and array B
		//
		Arrays.fill(B, 0.0);
		
		
		B[0] = -CPJM1[0]*DP0 + (RTJ[0]  + RTJP1[0])*DT;
		for (int i = 1; i < NUM_LAYERS-1; i++)
		{
			B[i] = (RTJ[i] + RTJP1[i])*DT;
		}
		if (K[0]) B[9] = -CKPJM1[0]*DP0;		//hier zat een fout !!

		
	}
	
	
	
	
	
//	public static void main(String[] args)
//	{
//			
//		System.out.println("GENROLL elastic/plastic calculation of tube tubesheet rolling");
//		System.out.println("use inch and psi and decimal .\n");
//		System.out.println();
//		
//		new GENRoll();
//
//	}
	
	

} //end class GENROLL
