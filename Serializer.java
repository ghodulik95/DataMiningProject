import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Serializer {
	public void serializeClusters(List<Cluster> clus){
		
		try{
			int i = 1;
			for(Cluster c : clus){
				FileOutputStream fout = new FileOutputStream("Clusters"+(i++)+".ser");
				ObjectOutputStream oos = new ObjectOutputStream(fout);   
				oos.writeObject(c);
				oos.close();
			}
			System.out.println("Done");
	 
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	public List< Cluster> deserializeClusters(){
		List<Cluster> clus = new ArrayList<Cluster>();
		try{
			 int i = 1;
			 while(true){
			   FileInputStream fin = new FileInputStream("Clusters"+(i++)+".ser");
			   ObjectInputStream ois = new ObjectInputStream(fin);
			   clus.add( (Cluster) ois.readObject());
			   ois.close();
			 }
	 
		   }catch(FileNotFoundException ex){
			   return clus;
		   }catch(Exception ex){
			   ex.printStackTrace();
			   return null;
		   }
	}
}
