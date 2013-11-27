/**
 * 
 */
package sc.fiji.CMP_BIA.segmentation;

import org.junit.Test;

import sc.fiji.CMP_BIA.segmentation.tools.MutInfo;
import sc.fiji.CMP_BIA.tools.MatrixTools;
import sc.fiji.CMP_BIA.tools.Prints;

/**
 * @author borovji3
 *
 */
public class SimSegTest {

	@Test
	public void test_MIgrad() {
		Prints.printTitle("Mutual Information - gradient");
		
		float[][] a = new float[][]{{10,1,2}, {3,5,5}, {0,0,0}, {7,2,6}};
		a = MatrixTools.normMatrix(a);
				
	    MutInfo m = new MutInfo(a);
	    System.out.println( m.crit() );
	    Prints.printMatrix( m.grad() );
	    
	    double h = 1e-12;
	    
	    for (int i = 0; i < a.length; i++) {
			for (int j = 0; j < a[0].length; j++) {
				a[i][j] += h;
	            MutInfo m2 = new MutInfo(a);
	            double tmp = (m2.crit()-m.crit()) / h;
	            System.out.println(" -> analg="+Double.toString(m.grad()[i][j])+" numgr="+Double.toString(tmp) );
			}
		}
	}

}
