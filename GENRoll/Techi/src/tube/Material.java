package tube;


//
// this class represents a mechanical material
// with properties filled in for use in GENRoll program
// in US Customary units
//
// example: new Material("CuNi 90/10",1.8E7,1.5E4,9.5E-6)
//
public class Material
{
	private String name;			//name. e.g. Cu-Ni 70:30
	private double elasticity;		//elasticity in psi at ambient;
	private double yield;			//yield strength at ambient; 
	private double alpha;			//thermal conductivity in BTU/hr.ft.F
	
	public Material(String name, double elasticity, double yield, double alpha)
	{
		super();
		this.name = name;
		this.elasticity = elasticity;
		this.yield = yield;
		this.alpha = alpha;
	}

	public String toString()
	{
		return name;
	}
	
	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public double getElasticity()
	{
		return elasticity;
	}

	public void setElasticity(double elasticity)
	{
		this.elasticity = elasticity;
	}

	public double getYield()
	{
		return yield;
	}

	public void setYield(double yield)
	{
		this.yield = yield;
	}

	public double getAlpha()
	{
		return alpha;
	}

	public void setAlpha(double alpha)
	{
		this.alpha = alpha;
	}
	
	

}
