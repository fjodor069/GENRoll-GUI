package tube;

//test how to use simultaneous equation solver
//
// involving a lot of matrix functions (matrix = a two dimensional array)
//
public class MatrixEquations
{
	public static final double EPSILON = 1e-10;
	
	
	public static double[][] A1 = {
										{5,1,8},
										{4,-2,0},
										{6,7,4}
									  };
	public static double[] B1 = {46,12,50};
	
	public static double[][] C1 = {
										{1.0,2.0,3.0},
										{4.0,5.0,6.0},
										{7.0,8.0,9.0}
									};

	public static double[][] D1 = {
										{0.0,2.0,4.0},
										{1.0,4.5,2.2},
										{1.1,4.3,5.2}
									};
	
	// method to solve simultaneous linear equations
	// for N equations with N unknown variables
	// Q is the NxN matrix with N coefficients from N equation
	// F is the N matrix with results
	// returns the solution in matrix F
	public static double[] Leqt(double[][] Q, double[] F, int N)
	{
		double[][] A = new double[N][N+1];
		double[] X = new double[N];
		int JJ;
		
		
		//copy Q into A
		for (int i = 0; i < N; i++)
			for (int j = 0; j < N; j++)
				A[i][j] = Q[i][j];
		
		//copy F into A
		for (int i = 0; i < N; i++)
			A[i][N] = F[i];
		
		//
		//printDoubleMatrix(A,A.length,A[1].length);
		//
		for (int k = 0; k < N-1; k++)
		{
			JJ = k;
			double BIG = Math.abs(A[k][k]);
			int kp1 = k + 1;
			for (int i = kp1; i < N; i++)
			{
				
				double AB = Math.abs(A[i][k]);
				if ((BIG - AB) < 0)
				  JJ = 1;
			}
			if ((JJ- k) < 0)
				for (int j = k; j <= N; j++)
				{
					double temp = A[JJ][j];
					A[k][j] = temp;
				}
			else
				for (int i = kp1; i < N; i++)
				{
					double Quot = A[i][k] / A[k][k];
					for (int j = kp1; j <= N; j++)
						A[i][j] = A[i][j] - Quot*A[k][j];
				}
			for (int i = kp1; i < N; i++)
				A[i][k] = 0.0;
			X[N-1] = A[N-1][N] / A[N-1][N];
			for (int NN = 0; NN <= N- 1; NN++)
			{
				double Sum = 0.0;
				int i = N - NN - 1;
				int IP1 = i + 1 ;
				for (int j = IP1; j < N; j++)
					Sum = Sum + A[i][j]*X[j];
				//System.err.printf("i = %d  M  = %d \n",i,M ); 
				X[i] = (A[i][N] - Sum)/A[i][i];
			}
			for (int i = 0; i < N; i++)
				F[i] = X[i];
		}
		return X;
		
	}

	//method to add two matrices
	//
	public static double[][] addMatrix(double[][] a, double[][] b)
	{
		double[][] c = new double[a.length][a[1].length];
		
		for (int i = 0; i < a.length; i++)
		{
			for (int j = 0; j < a[i].length; j++)
				c[i][j] = a[i][j] + b[i][j];
		}
		return c;
	}

	//method to multiply two matrices of arbitrary but same size
	//number of rows and columns must be equal
	// the result is returned as a new matrix
	// it works !!
	public static double[][] multiplyMatrix(double[][] a, double[][] b)
	{
		double[][] c = new double[a.length][a[1].length];
		
		for (int i = 0; i < a.length; i++)
		{
			for (int j = 0; j < a[i].length; j++)
			{
				for (int k = 0; k < a.length; k++)
					c[i][j] += a[i][k]*b[k][j];
			}
		}
		return c;
	}
	
	//overload to multiply with b as unequal size
	// return is a single dimensional array or one column in the matrix
	public static double[][] multiplyMatrix(double[][] a, double[] b)
	{
		double[][] c = new double[a.length][a[1].length];
		
		for (int i = 0; i < a.length; i++)
		{
			for (int j = 0; j < a[i].length; j++)
			{
				for (int k = 0; k < a[i].length; k++)
				  c[i][j] += a[i][k]*b[k];
			}
		}
		return c;
	}
	//method to find the inverse of a matrix
	public static double[][] invertMatrix(double[][] a)
	{
		int size = a.length;
		double[][] m_inverse = new double[size][size];
		double[][] m_transpose;
		
		double det;
		
		System.out.printf("invert Matrix with %d elements ",size);
		det = determinant(a, size );
		
		if (det > 0)
		{
						
			m_transpose = transpose(cofactor(a));
			
			for (int i = 0; i < size; i++)
				for (int j = 0; j < size; j++)
					m_inverse[i][j] = m_transpose[i][j] / det;
		}
		
		return m_inverse;
	}
	
	
	// method to find the determinant of a matrix
	// note this method is recursive
	// and it works !!!
	public static double determinant(double[][] Mx, int size)
	{
		double det = 0;
		double s = 1;
		double[][] m_minor = new double[size][size];
		int i,j,m,n,c;
		
		
		if (size == 1)
			return Mx[0][0];
		else
		{
			det = 0;
			for (c = 0; c < size; c++)
			{
				m = 0;
				n = 0;
				for (i = 0; i < size; i++)
				{
					for (j = 0; j < size; j++)
					{
						m_minor[i][j] = 0;
						if (i != 0 && j != c)
						{
							m_minor[m][n] = Mx[i][j];
							if ( n < (size - 2))
								n++;
							else
							{
								n = 0;
								m++;
							}
						}
					}
				}
				det = det + s *(Mx[0][c] * determinant(m_minor,size-1));
				s = -1 * s;
			}
		}
		return det;
	}
	
	//method to find the cofactor of a matrix
	public static double[][] cofactor(double[][] Mx)
	{
		int size = Mx.length;
		
		double[][] m_cofactor = new double[size][size];
		double[][] Matrix_cofactor = new double[size][size];
		
		int p,q,m,n,i,j;
		
		for (q = 0; q < size; q++)
		{
			for (p = 0; p < size; p++)
			{
				m = 0;
				n = 0;
				for (i = 0; i < size; i++)
				{
					for (j = 0; j < size; j++)
					{
						if (i != q && j != p)
						{
							m_cofactor[m][n] = Mx[i][j];
							if (n < (size-2))
								n++;
							else
							{
								n = 0;
								m++;
							}
						}
					}
				}
				Matrix_cofactor[q][p] = Math.pow(-1, q+p) * determinant(m_cofactor,size-1);
				
			}
		}
		return Matrix_cofactor;
	}
	
	
	//method to transpose a matrix
	public static double[][] transpose(double[][] Mx)
	{
		int size = Mx.length;
		
		double[][] m_transpose = new double[size][size];
		
		for (int i = 0; i < size; i++)
			for (int j = 0; j < size; j++)
				m_transpose[i][j] = Mx[j][i];
		
		return m_transpose;
	}
	
	//print a twodimensional matrix
	// M and N is the size and can also be read as Mx.length
	public static void printDoubleMatrix(double[][] Mx, int M, int N)
	{
		for (int i = 0; i < N; i++)
		{
			for (int j = 0; j < M; j++)
				System.out.printf(" %5.4f ",Mx[i][j]) ;
			System.out.println();
		}
				
	}
	
	public static void printSingleMatrix(double[] Mx, int N)
	{
		for (int i = 0; i < N; i++)
		{
			System.out.printf(" %5.4f ",Mx[i]) ;
			System.out.println();
		}
				
	}
	
	// lsolve
	


	// Gaussian elimination with partial pivoting
	public static double[] lsolve(double[][] A, double[] b)
	{
		int n = b.length;

		for (int p = 0; p < n; p++)
		{

			// find pivot row and swap
			int max = p;
			for (int i = p + 1; i < n; i++)
			{
				if (Math.abs(A[i][p]) > Math.abs(A[max][p]))
				{
					max = i;
				}
			}
			double[] temp = A[p];
			A[p] = A[max];
			A[max] = temp;
			double t = b[p];
			b[p] = b[max];
			b[max] = t;

			// singular or nearly singular
			if (Math.abs(A[p][p]) <= EPSILON)
			{
				throw new ArithmeticException(
						"Matrix is singular or nearly singular");
			}

			// pivot within A and b
			for (int i = p + 1; i < n; i++)
			{
				double alpha = A[i][p] / A[p][p];

				b[i] -= alpha * b[p];
				for (int j = p; j < n; j++)
				{
					A[i][j] -= alpha * A[p][j];
				}
			}
		}

		// back substitution
		double[] x = new double[n];
		for (int i = n - 1; i >= 0; i--)
		{
			double sum = 0.0;
			for (int j = i + 1; j < n; j++)
			{
				sum += A[i][j] * x[j];
			}
			x[i] = (b[i] - sum) / A[i][i];
		}
		return x;
	}

	
	

} //end class
