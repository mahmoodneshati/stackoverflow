package PLM;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by Zohreh on 10/8/2015.
 */
public class Conservativeness {
    Utility u;
    PLM p;

    public static void main(String args[]) {
        PrintStream stdout = System.out;
        try {
            PrintStream out = new PrintStream(new FileOutputStream("conservativeness.txt"));
            System.setOut(out);
        }
        catch(IOException e) {
            System.out.println("\n\n\n\nSorry!\n\n\n\n");
        }

        Conservativeness c = new Conservativeness();

        long start = System.currentTimeMillis();
        c.start("JavaQAIndex", "*:*");
        long end = System.currentTimeMillis();
        System.out.println("Total RunTime: "+(end-start));
    }

    public Conservativeness() {
        u = new Utility("JavaQAIndex");
        p = new PLM();
    }

    public void start(String indexDir, String q) {
        try {
            IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexDir)));
            Analyzer analyzer = new StandardAnalyzer();
            HashSet<Integer> ExpertIDs = new HashSet<Integer>();
            int c = 0;
            for (int i = 0; i < reader.maxDoc(); i++) {
                Document doc = reader.document(i);
                ExpertIDs.add(Integer.parseInt(doc.get("OwnerUserId")));
                ExpertIDs.add(Integer.parseInt(doc.get("LastEditorUserId")));
            }
            reader.close();
            System.out.println("Expert Count: " + ExpertIDs.size());
            for(Integer e: ExpertIDs)
                getConservativenessProbability(e);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getConservativenessProbability(Integer eid) {
        double output = 0;
        String out = ""+eid+",";
        ArrayList<Integer> activityYears = u.getActivityYearsByExpertID(eid);

        HashMap<Integer,HashSet> TagMap = new HashMap<Integer,HashSet>();
        for (int i = 0; i < activityYears.size(); i++) {
            int year = activityYears.get(i);
            HashSet<String> A = p.getTagsByAuthorAndYear(year, eid);
            TagMap.put(year, A);
        }

        int count = 0;
        for (int i = 1; i < activityYears.size(); i++) {
            double c = getConservativenessProbabilityByYear(eid, activityYears.get(i),activityYears.get(i-1),TagMap);
            out = out + c + ",";
            output += c;
            count++;
        }
        output = (count == 0 ? 0.5 : output / count);
        System.out.println(out + output);
    }

    private double getConservativenessProbabilityByYear(Integer eid, int year, int lastYear, HashMap<Integer, HashSet> TagMap) {
        double output;

        HashSet<String> A_t = TagMap.get(year);
        HashSet<String> A_t_1 = TagMap.get(lastYear);

        HashSet<String> IntersectionSet = new HashSet<>();
        IntersectionSet.addAll(A_t);
        IntersectionSet.retainAll(A_t_1);

        A_t_1.addAll(A_t);

        output = (A_t_1.size() == 0 ? 0 : (IntersectionSet.size() * 1.0) / A_t_1.size());
        return output;
    }

}
