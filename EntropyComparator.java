import java.util.Comparator;


public class EntropyComparator implements Comparator<Column>{

	@Override
	public int compare(Column c1, Column c2) {
		double entropyDiff = c1.calcEntropy() - c2.calcEntropy();
		if(entropyDiff != 0){
			return entropyDiff < 0 ? -1 : 1;
		}else{
			return c1.attrName.compareTo(c2.attrName);
		}
	}

}
