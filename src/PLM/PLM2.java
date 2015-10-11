package PLM;

import com.sun.xml.internal.bind.v2.TODO;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by Zohreh on 10/10/2015.
 */
public class PLM2 {
    Utility u;
    IndexReader reader;
    IndexSearcher searcher;
    String IndexDir;

    Integer N_e_t;// number of documents writen by eid in specified year
    double beta = 0.5;
    double conservativeness;// conservativeness of specified expert
    HashSet<String> currentYearTags; // expert uses these tags in current year
    HashMap<String,Double> P_at_e;// key= tags from currentYearTags  ,  values = Probability of having documnets written by eid with specified tags in year t

    public PLM2() {
        try {
            IndexDir = "JavaQAIndex";
            reader = DirectoryReader.open(FSDirectory.open(Paths.get(IndexDir)));
            searcher = new IndexSearcher(reader);
            u = new Utility(IndexDir);
            P_at_e = new HashMap<String,Double>();
            //beta = 0.5;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]) {

        PrintStream stdout = System.out;
        try {
            PrintStream out = new PrintStream(new FileOutputStream("output.txt"));
            System.setOut(out);
        } catch (IOException e) {
            System.out.println("\n\n\n\nSorry!\n\n\n\n");
        }
        PLM2 p = new PLM2();

        long start = System.currentTimeMillis();
        //p.start("java");

        long end = System.currentTimeMillis();
        System.out.println("Total RunTime: " + (end - start));

    }

    /**
     * Perform some preparing task
     * @param word
     * @param eid
     * @param futureYear
     */
    public int start(String word, Integer eid, int futureYear) {

        //caculating tags which eid use in current year
        currentYearTags = getTags(futureYear - 1,eid);
        if (currentYearTags == null) {
            System.err.println("No tags found for year = " + (futureYear-1));
            return 0;
        }

        //calculating number of documents writen by eid in specified year
        N_e_t = getN_e_t(eid,futureYear - 1);

        //calculating Probability of having documents written by eid with specified tags in year t for each tag incurrentYearTags
        for (String currentTag : currentYearTags)
            P_at_e.put(currentTag,getCurrentYearTagProbabilityByExpertMLE(currentTag, eid, (futureYear-1),N_e_t));

        //calculating conservativeness of specified expert
        conservativeness = getConservativenessProbability(eid, (futureYear-1));

        return 0;
    }

    //Start from here
    public double getWordProbabilityByExpertAndYear(String word, Integer eid, int futureYear) {

        HashSet<String> tags1 = getTopTagsSimilar(futureYear, 25);
        HashSet<String> tags2 = getTopTagsPopular(futureYear, 25);

        HashSet<String> futureTags = new HashSet<>();
        futureTags.addAll(tags1);
        futureTags.addAll(tags2);
        if (futureTags == null) {
            System.err.println("No tags found for year = " + futureYear);
            return 0;
        }


        double output = 0;
        for (String tag : futureTags) {
            double P_W_A = getFutureWordProbabilityByTagAndYearMLE(futureYear, tag, word);
            double P_A_E = getFutureTagProbabilityByExpertAndYear(tag, eid, futureYear);
            output += P_W_A * P_A_E;
        }
        return output;
    }

    private double getCurrentYearTagProbabilityByExpertMLE(String currentTag, Integer eid, int currentYear,Integer N_e_t) {
        Integer N_at_e = u.getDocCount(
                u.BooleanQueryAnd(
                        u.BooleanQueryAnd(
                                u.SearchOwnerUserId(eid), u.SearchCreationDate(currentYear)), u.SearchTag(currentTag)));

        double output = (N_e_t == 0 ? 0 : (N_at_e * 1.0) / N_e_t);
        return output;
    }

    private double getFutureWordProbabilityByTagAndYearMLE(int futureYear, String tag, String word) {
        //  Assumption : P(w_(t+1)|a_(t+1)) = P(w_t|a_t) replace year by (year-1)
        Query Q_date_tag = u.BooleanQueryAnd(u.SearchCreationDate(futureYear - 1), u.SearchTag(tag));
        Query Q_date_tag_word = u.BooleanQueryAnd(u.SearchCreationDate(futureYear - 1),
                u.BooleanQueryAnd(u.SearchBody(word), u.SearchTag(tag)));

        Integer docCountByTag = u.getDocCount(Q_date_tag);

        Integer docCountByTagAndWordInBody = u.getDocCount(Q_date_tag_word);

        return 1.0 * docCountByTagAndWordInBody / docCountByTag;
    }

    private double getFutureTagProbabilityByExpertAndYear(String futureTag, Integer eid, int futureYear) {
        double output = 0;
        for (String currentTag : currentYearTags) {
            output += getFutureTagProbabilityByExpertAndCurrentTag(futureTag, currentTag, eid, futureYear - 1)
                    * P_at_e.get(currentTag);
        }
        return output;
    }

    private double getFutureTagProbabilityByExpertAndCurrentTag
            (String futureTag, String currentTag, int eid, int currentYear) {
        double oldTagProbability = getProbabilityChoosingTagFromCurrentYearTags(futureTag, currentTag, eid, currentYear);
        double newTagProbability = getProbabilityChoosingNewTag(futureTag, currentTag, eid, currentYear);

        double output = (conservativeness * oldTagProbability) +
                ((1 - conservativeness) * newTagProbability);
        return output;
    }

    private double getProbabilityChoosingTagFromCurrentYearTags(String futureTag, String currentTag, int eid, int CurrentYear) {
        return (P_at_e.containsKey(futureTag)?P_at_e.get(futureTag):0);
    }

    private double getProbabilityChoosingNewTag(String futureTag, String currentTag, Integer eid, int CurrentYear) {
        // IDEA: check whether this function is independent of the eid????
        //  we can improve this function with considering the eid who change his/her topic

        return ((beta * tagSimilarity(futureTag, currentTag, CurrentYear))
                + ((1 - beta) * tagPopularity(futureTag, CurrentYear)));
    }

    private double tagSimilarity(String futureTag, String currentTag, int currentYear) {
        //TODO
        return 0;
    }

    private double tagPopularity(String futureTag, int currentYear) {
        //TODO
        return 0;
    }

    //---------------------------------


    private HashSet<String> getTopTagsPopular(int futureYear, int topCount) {
        try {

            HashSet<String> popularTags = new HashSet<String>();
            String sCurrentLine;
            BufferedReader br = new BufferedReader(new FileReader("popularity" + futureYear + ".txt"));
            int c = 0;
            while ((sCurrentLine = br.readLine()) != null) {
                c =c + 1;
                if (c > topCount)
                    break;
                popularTags.add(sCurrentLine.split(",")[0]);
            }

            return popularTags;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private HashSet<String> getTopTagsSimilar(int futureYear, int topCount) {
        //TODO
        return null;
    }

    private double getConservativenessProbability(Integer eid, int i) {
        //TODO
        return 0;
    }

    /**
     * Get all the tags which be used in specified year
     * @param year
     * @return
     */
    private HashSet<String> getTags(int year) {
        HashSet<String> Tags = new HashSet<String>();
        Query query = u.SearchCreationDate(year);
        try {
            TopDocs hits = searcher.search(query, Integer.MAX_VALUE);
            ScoreDoc[] ScDocs = hits.scoreDocs;
            for (int i = 0; i < ScDocs.length; ++i) {
                int docId = ScDocs[i].doc;
                Document d = searcher.doc(docId);
                //System.out.println("Id: "+d.get("Id"));
                for (IndexableField tag : d.getFields("Tags")) {
                    if(tag.stringValue().length() != 0 )
                        Tags.add(tag.stringValue());
                }
            }
            return Tags;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get all the tags which be used in specified year by specified expert
     * @param year
     * @param eid   Expert ID
     * @return
     */
    private HashSet<String> getTags(int year,Integer eid) {
        HashSet<String> Tags = new HashSet<String>();
        Query query = u.BooleanQueryAnd(u.SearchCreationDate(year), u.SearchOwnerUserId(eid));
        try {
            TopDocs hits = searcher.search(query, Integer.MAX_VALUE);
            ScoreDoc[] ScDocs = hits.scoreDocs;
            for (int i = 0; i < ScDocs.length; ++i) {
                int docId = ScDocs[i].doc;
                Document d = searcher.doc(docId);
                //System.out.println("Id: "+d.get("Id"));
                for (IndexableField tag : d.getFields("Tags")) {
                    if(tag.stringValue().length() != 0 )
                        Tags.add(tag.stringValue());
                }
            }
            return Tags;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Returns number of documents written by e in year t
     * @param eid   Expert ID
     * @param currentYear
     * @return
     */
    private Integer getN_e_t(Integer eid, int currentYear){
        return u.getDocCount(u.BooleanQueryAnd(u.SearchOwnerUserId(eid), u.SearchCreationDate(currentYear)));
    }
}