package PLM;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

/**
 * Created by Zohreh on 10/10/2015.
 */
public class Similarity {
    PLM p;
    Utility u;
    HashMap<String,HashSet<Integer>> map;

    public Similarity() {
        p = new PLM();
        u = new Utility("JavaQAIndex");
        map = new HashMap<String,HashSet<Integer>>();

    }

    public static void main(String args[]) {
        PrintStream stdout = System.out;
        try
        {
            PrintStream out = new PrintStream(new FileOutputStream("similarity2010.txt"));
            System.setOut(out);
        }
        catch(IOException e)
        {
            System.out.println("\n\n\n\nSorry!\n\n\n\n");
        }

        Similarity s = new Similarity();
        long start = System.currentTimeMillis();
        s.start(2010);
        long end = System.currentTimeMillis();
        System.out.println("Total RunTime: " + (end - start));

    }

    private void start(int year) {
        HashSet<String> Tags = p.getTags(year);
        for(String tag: Tags){
            map.put(tag,u.getExpertsBYTagandYear(tag,year));
        }
        ArrayList<String> TagList = new ArrayList<String>(Tags);
        Collections.sort(TagList);

        for (int i = 0; i < TagList.size(); i++) {
            for (int j = i + 1; j < TagList.size(); j++) {
                    String tag1 = TagList.get(i);
                    String tag2 = TagList.get(j);
                    double sim = tagSimilarity(tag1, tag2, year);
                    if (sim != 0.0)
                        System.out.println(tag1 + "," + tag2 + "," + year + "," + sim);
                }
            }
    }

    private double tagSimilarity(String futureTag, String currentTag, int CurrentYear) {
        HashSet<Integer> IntersectionSet = new HashSet<Integer>();
        IntersectionSet.addAll(map.get(currentTag));
        IntersectionSet.retainAll(map.get(futureTag));
        int sorat = IntersectionSet.size();

        HashSet<Integer> UnionSet = new HashSet<Integer>();
        int makhraj=1;
        if (sorat != 0) {
            UnionSet.addAll(map.get(currentTag));
            UnionSet.addAll(map.get(futureTag));
            makhraj = UnionSet.size();
        }
        return 1.0*sorat / makhraj;
    }
}
