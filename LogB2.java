import java.util.HashMap;
import java.util.Map;


public class LogB2 {
	public static double logOf2 = Math.log(2);
	public static Map<Integer, Map<Integer, Double>> logTable;
	public static boolean tableGen = false;
	
	public static double getLog(int numerator, int denominator){
		if(!tableGen){
			generateTable();
			tableGen = true;
		}
		return logTable.get(denominator).get(numerator);
	}

	private static void generateTable() {
		logTable = new HashMap<Integer, Map<Integer, Double>>();
		for(int i = 4952; i > 0; i--){
			System.out.println("Calculating "+i);
			for(int j = i; j > 0; j--){
				//System.out.println("\t"+j);
				if(logTable.containsKey(i)){
					Map<Integer, Double> m = logTable.get(i);
					m.put(j, Math.log((1.0*j)/i)/logOf2 );
				}else{
					Map<Integer, Double> m = new HashMap<Integer, Double>();
					m.put(j, Math.log((1.0*j)/i)/logOf2 );
					logTable.put(i, m);
				}
			}
		}
	}
}
