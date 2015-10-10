package PLM;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.FSDirectory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by Zohreh on 10/7/2015.
 */
public class Popularity {
    PLM p;
    Utility u;

    public static void main(String args[]) {
        PrintStream stdout = System.out;
        try
        {
            PrintStream out = new PrintStream(new FileOutputStream("popularity.txt"));
            System.setOut(out);
        }
        catch(IOException e)
        {
            System.out.println("\n\n\n\nSorry!\n\n\n\n");
        }

        Popularity pop = new Popularity();

        long start = System.currentTimeMillis();
        pop.start();
        long end = System.currentTimeMillis();
        System.out.println("Total RunTime: "+(end-start));
    }

    private void start() {

        for (int i = 2008; i < 2016; i++) {
            Map<Integer, String> unsortMap = new HashMap<Integer, String>();

            HashSet<String> Tags= p.getTags(i);
            for(String tag:Tags) {
                Integer N_t = u.getDocCount(u.BooleanQueryAnd(u.SearchTag(tag), u.SearchCreationDate(i)));
                //System.out.println(tag+","+i+","+u.getDocCount(u.BooleanQueryAnd(u.SearchTag(tag),u.SearchCreationDate(i))));
                unsortMap.put(N_t, tag+","+i+",");
            }
            Map<Integer, String> treeMap = new TreeMap<Integer, String>(unsortMap);

            for(Map.Entry<Integer,String> entry : treeMap.entrySet()) {
                Integer key = entry.getKey();
                String value = entry.getValue();
                System.out.println(value  + key);
            }
            System.out.println("***************");
        }
    }

    public Popularity() {
        p = new PLM();
        u = new Utility("JavaQAIndex");
    }
}
