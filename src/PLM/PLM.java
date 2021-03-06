package PLM;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by Mahmood on 8/26/2015.
 * Google
 */
public class PLM {
    String IndexDir;
    IndexReader reader;
    IndexSearcher searcher;
    Utility u;
    double beta = 0.5;

    int AVERAGE_LENGTH = 3;

    public static void main(String args[]) {
        PrintStream stdout = System.out;
        try
        {
            PrintStream out = new PrintStream(new FileOutputStream("output.txt"));
            System.setOut(out);
        }
        catch(IOException e)
        {
            System.out.println("\n\n\n\nSorry!\n\n\n\n");
        }
        PLM p = new PLM();

        p.getAllTags2(2008);
    }

    public void getAllTags(int year) {
        HashMap<String,Integer> Tags=new HashMap<String,Integer>();
        System.out.println("Tag,Count");
        Query query = u.SearchCreationDate(year);
        try {
            TopDocs hits = searcher.search(query, Integer.MAX_VALUE);
            ScoreDoc[] ScDocs = hits.scoreDocs;
            for (int i = 0; i < ScDocs.length; ++i) {
                int docId = ScDocs[i].doc;
                Document d = searcher.doc(docId);
                //System.out.println("Id: "+d.get("Id"));
                for (IndexableField tag : d.getFields("Tags")) {
                    if(tag.stringValue().length() != 0 ){
                        String t = tag.stringValue();
                        if(Tags.containsKey(t)){
                            Integer c = Tags.get(t);
                            Tags.put(t, c+1);
                        }
                        else
                            Tags.put(t,1);
                    }
                }
            }
            for (String tag0:Tags.keySet()) {
                System.out.println(tag0+","+Tags.get(tag0));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void getAllTags2(int year) {
        HashMap<String,Integer> Tags=new HashMap<String,Integer>();
        System.out.println("Tag,Count");
        Query query = u.SearchCreationDate(year);
        try {
            TopDocs hits = searcher.search(query, Integer.MAX_VALUE);
            ScoreDoc[] ScDocs = hits.scoreDocs;
            for (int i = 0; i < ScDocs.length; ++i) {
                int docId = ScDocs[i].doc;
                Document d = searcher.doc(docId);
                for (IndexableField tag : d.getFields("Tags")) {
                    System.out.println(tag.stringValue()+","+d.get("PostTypeId"));
                }
            }
            for (String tag0:Tags.keySet()) {
                System.out.println(tag0+","+Tags.get(tag0));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void getExpertIDswithCorrectAnswerCounts(){
        try {
            HashMap<Integer,Integer> ExpertIDs=new HashMap<Integer,Integer>();
            BufferedReader reader = new BufferedReader(new FileReader("D:\\Sharif\\Project\\Lucene Index\\StackOverflow\\java_all.txt"));
            String line;
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                String[] temp = line.split(",");
                if(Integer.parseInt(temp[3])==1){
                    try {
                        if(ExpertIDs.containsKey(Integer.parseInt(temp[10]))) {
                            Integer c=ExpertIDs.get(Integer.parseInt(temp[10]));
                            ExpertIDs.put(Integer.parseInt(temp[10]), c+1);
                        }
                        else
                            ExpertIDs.put(Integer.parseInt(temp[10]),1);
                    } catch (Exception e) {}
                }
            }
            reader.close();
            System.out.println("Count,"+ExpertIDs.size());
            for (Integer i:ExpertIDs.keySet()) {
                System.out.println(i+","+ExpertIDs.get(i));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getExpertIDs(){
        try {
            HashSet<Integer> ExpertIDs=new HashSet<Integer>();
            BufferedReader reader = new BufferedReader(new FileReader("D:\\Sharif\\Project\\Lucene Index\\StackOverflow\\java_all.txt"));
            String line;
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                String[] temp = line.split(",");
                try {
                    ExpertIDs.add(Integer.parseInt(temp[9]));
                    ExpertIDs.add(Integer.parseInt(temp[10]));
                    ExpertIDs.add(Integer.parseInt(temp[11]));
                    ExpertIDs.add(Integer.parseInt(temp[12]));
                } catch (Exception e) {}
            }
            reader.close();
            System.out.println("Count,"+ExpertIDs.size());
            for (Integer i:ExpertIDs) {
                System.out.println(i);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getExpertIDsByYear(){
        HashSet<Integer> ExpertIDs = u.getExpertIDsBYYear(2015);
        for (Integer i:ExpertIDs) {
            System.out.println(i);
        }
    }

    //test getCurrentYearTagProbabilityByExpertMLE(currentTag, eid, futureYear - 1);
    public void test(){
        int year=2008;
        System.out.println("CurrentTag,ExpertID,Year,Probability");
        try {
            BufferedReader reader = new BufferedReader(new FileReader("D:\\Sharif\\Project\\Lucene Index\\StackOverflow\\ExpertIDs08.csv"));
            String line;
            while ((line = reader.readLine()) != null) {
                //System.out.println(line);
                Integer eid = Integer.parseInt(line);
                HashSet<String> currentYearTags = getTags(2008, eid);
                System.out.println(currentYearTags.toString());
                for (String tag:currentYearTags){
                    double output = getCurrentYearTagProbabilityByExpertMLE(tag,eid,2008);
                    System.out.println(tag+","+eid+","+year+","+output);
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public PLM() {
        try {
            IndexDir = "JavaQAIndex";
            reader = DirectoryReader.open(FSDirectory.open(Paths.get(IndexDir)));
            searcher = new IndexSearcher(reader);
            u = new Utility(IndexDir);
            beta = 0.5;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Start from here
    public double getWordProbabilityByExpertAndYear(String word, Integer eid, int futureYear) {

        HashSet<String> tags = getTags(futureYear);
        if (tags == null) {
            System.err.println("No tags found for year = " + futureYear);
            return 0;
        }
        double output = 0;
        for (String tag : tags) {
            double P_W_A = getFutureWordProbabilityByTagAndYearMLE(futureYear, tag, word);
            double P_A_E = getFutureTagProbabilityByExpertAndYear(tag, eid, futureYear);
            output += P_W_A * P_A_E;
        }
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
        HashSet<String> currentYearTags = getTags(futureYear - 1);
        double output = 0;
        for (String currentTag : currentYearTags) {
            output += getFutureTagProbabilityByExpertAndCurrentTag(futureTag, currentTag, eid, futureYear - 1)
                    * getCurrentYearTagProbabilityByExpertMLE(currentTag, eid, futureYear - 1);
        }
        return output;
    }

    private double getCurrentYearTagProbabilityByExpertMLE(String currentTag, Integer eid, int currentYear) {
        Integer N_e_t = u.getDocCount(u.BooleanQueryAnd(u.SearchOwnerUserId(eid), u.SearchCreationDate(currentYear)));
        //N_e_t = count Question and answers by e in year t
        Integer N_at_e = u.getDocCount(
                u.BooleanQueryAnd(
                        u.BooleanQueryAnd(
                                u.SearchOwnerUserId(eid), u.SearchCreationDate(currentYear)), u.SearchTag(currentTag)));
        return N_e_t == 0?0:N_at_e * 1.0 / N_e_t;
    }

    private double getFutureTagProbabilityByExpertAndCurrentTag
            (String futureTag, String currentTag, int eid, int currentYear) {
        // TODO check which parts of formula can be computed offline(i.e. prior to the query)
        double output = 0;
        double conservativeness = getConservativenessProbability(AVERAGE_LENGTH, eid, currentYear);
        double oldTagProbability = getProbabilityChoosingTagFromCurrentYearTags(futureTag, currentTag, eid, currentYear);
        double newTagProbability = getProbabilityChoosingNewTag(futureTag, currentTag, eid, currentYear);
        output += (conservativeness * oldTagProbability) +
                ((1 - conservativeness) * newTagProbability);
        return output;
    }

    private double getProbabilityChoosingTagFromCurrentYearTags(String futureTag, String currentTag, int eid, int CurrentYear) {

        Integer N_e_t = u.getDocCount(
                u.BooleanQueryAnd(
                        u.SearchOwnerUserId(eid), u.SearchCreationDate(CurrentYear)));
        Integer N_at1_e_t = u.getDocCount(
                u.BooleanQueryAnd(
                        u.BooleanQueryAnd(
                                u.SearchOwnerUserId(eid), u.SearchCreationDate(CurrentYear)),
                        u.SearchTag(currentTag)));
        return (1.0 * N_at1_e_t) / N_e_t;
    }

    private double getProbabilityChoosingNewTag(String futureTag, String currentTag, Integer eid, int CurrentYear) {
        // IDEA: check whether this function is independent of the eid????
        //  we can improve this function with considering the eid who change his/her topic
        return ((beta * tagSimilarity(futureTag, currentTag, CurrentYear))
                + ((1 - beta) * tagPopularity(futureTag, CurrentYear)));
    }

    private double tagSimilarity(String futureTag, String currentTag, int CurrentYear) {
        //TODO this function should prepare the results before the query runTime

        int sorat = tagOverlapByAuthors(futureTag, currentTag, CurrentYear);

        /*HashSet<String> allTags = getTags(CurrentYear);
        double makhraj = 0;
        for (String tag : allTags) {
            makhraj += tagSimilarityByAuthors(futureTag, tag, CurrentYear);
        }*/
        int makhraj=tagUnionByAuthors(futureTag, currentTag, CurrentYear);


        return 1.0*sorat / makhraj;
    }

    private int tagUnionByAuthors(String futureTag, String currentTag, int CurrentYear) {
        HashSet<Integer> currentExpertIDs = u.getExpertsBYTagandYear(currentTag, CurrentYear);
        HashSet<Integer> futureExpertIDs = u.getExpertsBYTagandYear(futureTag, CurrentYear);
        currentExpertIDs.addAll(futureExpertIDs);
        return currentExpertIDs.size();
    }

    private int tagOverlapByAuthors(String futureTag, String currentTag, int CurrentYear) {
        HashSet<Integer> currentExpertIDs = u.getExpertsBYTagandYear(currentTag,CurrentYear);
        HashSet<Integer> futureExpertIDs = u.getExpertsBYTagandYear(futureTag,CurrentYear);
        HashSet<Integer> IntersectionSet = new HashSet<Integer>();
        IntersectionSet.addAll(currentExpertIDs);
        IntersectionSet.retainAll(futureExpertIDs);
        return IntersectionSet.size();
    }



    private double tagPopularity(String futureTag, int currentYear) {
        Integer N_t = u.getDocCount(u.SearchCreationDate(currentYear));
        Integer N_at1_t = u.getDocCount(
                u.BooleanQueryAnd(
                        u.SearchCreationDate(currentYear), u.SearchTag(futureTag)));
        return (1.0 * N_at1_t) / N_t;
    }

    private double getConservativenessProbability(int averageLength, Integer eid, int currentYear) {
        // idea Conservativeness is independent of the tags it only depends to the author
        double output = 0;
        ArrayList<Integer> activityYears = u.getActivityYearsByExpertID(eid);
        int count = 0;
        for (int i = 1; i < activityYears.size(); i++) {
            output += getConservativenessProbabilityByYear(eid, activityYears.get(i),activityYears.get(i-1));
            count++;
        }
        output = (count == 0 ? 0.5 : output / count);
        return output;
    }

    private double getConservativenessProbabilityByYear(Integer eid, int year, int lastYear) {
        double output;
        HashSet<String> A_t = getTagsByAuthorAndYear(year, eid);
        HashSet<String> A_t_1 = getTagsByAuthorAndYear(lastYear, eid);
        HashSet<String> IntersectionSet = new HashSet<>();
        IntersectionSet.addAll(A_t);
        IntersectionSet.retainAll(A_t_1);
        A_t_1.addAll(A_t);
        output = (A_t_1.size() == 0 ? 0 : (IntersectionSet.size() * 1.0) / A_t_1.size());
        return output;
    }

    HashSet<String> getTagsByAuthorAndYear(int year, Integer eid) {
        HashSet<String> Tags = new HashSet<String>();
        String delims = "[<>]";
        BytesRef lowerBR = new BytesRef(String.valueOf(year));
        BytesRef upperBR = new BytesRef(String.valueOf(year + 1));
        Query Q_Author_Year = u.BooleanQueryAnd(new TermRangeQuery("CreationDate", lowerBR, upperBR, true, true), u.SearchOwnerUserId(eid));
        try {
            TopDocs hits = searcher.search(Q_Author_Year, Integer.MAX_VALUE);
            ScoreDoc[] ScDocs = hits.scoreDocs;
            for (int i = 0; i < ScDocs.length; ++i) {
                int docId = ScDocs[i].doc;
                Document d = searcher.doc(docId);
                //System.out.println("Id: "+d.get("Id"));
                for (String tag : d.get("Tags").split(delims)) {
                    if(tag.length() != 0 )
                        Tags.add(tag);
                }
            }
            return Tags;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


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
}
