package PLM;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
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
        PLM p = new PLM();

        //HashSet<String> TitleTerms = p.getTags(2008);
        //p.getWordProbabilityByTagAndYear(2008, "c#", "datetime");
        // p.getConservativenessProbability();
    }

    public PLM() {
        try {
            IndexDir = "index2";
            reader = DirectoryReader.open(FSDirectory.open(Paths.get(IndexDir)));
            searcher = new IndexSearcher(reader);
            u = new Utility();
            beta = 0.5;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Start from here
    public double getWordProbabilityByExpertAndYear(String word, Integer eid, int futureYear) {

        HashSet<String> tags = getAllTagsByYear(futureYear);
        if (tags == null) {
            System.err.println("No tags found for year = " + futureYear);
            return -1;
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
        HashSet<String> currentYearTags = getAllTagsByYear(futureYear - 1);
        double output = 0;
        for (String currentTag : currentYearTags) {
            output += getFutureTagProbabilityByExpertAndCurrentTag(futureTag, currentTag, eid, futureYear - 1)
                    * getCurrentYearTagProbabilityByExpertMLE(currentTag, eid, futureYear - 1);
        }

        return output;
    }

    private double getCurrentYearTagProbabilityByExpertMLE(String currentTag, Integer eid, int currentYear) {
        // TODO smooth to handle zero probability

        Integer N_e_t = u.getDocCount(u.BooleanQueryAnd(u.SearchOwnerUserId(eid), u.SearchCreationDate(currentYear)));
        Integer N_at_e = u.getDocCount(
                u.BooleanQueryAnd(
                        u.BooleanQueryAnd(
                                u.SearchOwnerUserId(eid), u.SearchCreationDate(currentYear)), u.SearchTag(currentTag)));
        return N_at_e * 1.0 / N_e_t;
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
        if (!futureTag.equalsIgnoreCase(currentTag)) {
            System.err.println("Error current tag should be equal to the future tag");
            return -1;
        }

        Integer N_e_t = u.getDocCount(
                u.BooleanQueryAnd(
                        u.SearchOwnerUserId(eid), u.SearchCreationDate(CurrentYear)));
        Integer N_at1_e_t = u.getDocCount(
                u.BooleanQueryAnd(
                        u.BooleanQueryAnd(
                                u.SearchOwnerUserId(eid), u.SearchCreationDate(CurrentYear)),
                        u.SearchTag(currentTag)));
        //TODO handel the zero cases
        return (1.0 * N_at1_e_t) / N_e_t;
    }

    private double getProbabilityChoosingNewTag(String futureTag, String currentTag, Integer eid, int CurrentYear) {
        // IDEA: check whether this function is independent of the eid????
        //  we can improve this function with considering the eid who change his/her topic
        return ((beta * tagSimilarity(futureTag, currentTag, CurrentYear))
                + ((1 - beta) * tagPopularity(futureTag, CurrentYear)));
    }

    private double tagSimilarity(String futureTag, String currentYear, int CurrentYear) {

        // current tag should not be equal to the pastYear tag
        // for the given past tag, we should have a distribution over all topics
        //TODO this function should prepare the results before the query runTime
        // We can compute the similarity of two tags by number of intersection of people following these two tags

//        Integer a_t_Authors =
//                u.getAuthorsCountByQuery(
//                        u.BooleanQueryAnd(
//                                u.SearchTag(PastYearTag), u.BooleanQueryAnd(
//                                        u.SearchCreationDate(CurrentYear), u.SearchTag(CurrentTag))));
//
//        ArrayList<String> allTags = getAllTagsByYear(CurrentYear);
//
//        double makhraj = 0;
//        for (String tag : allTags) {
//            makhraj += u.getAuthorsCountByQuery(
//                    u.BooleanQueryAnd(
//                            u.SearchTag(PastYearTag), u.BooleanQueryAnd(
//                                    u.SearchCreationDate(CurrentYear), u.SearchTag(tag))));
//        }
//
//
//        return a_t_Authors / makhraj;
        return -1;
    }

    private HashSet<String> getAllTagsByYear(int currentYear) {
        // TODO Implement this function
        return null;
    }

    private double tagPopularity(String futureTag, int currentYear) {
        //TOdO handel zero case
        Integer N_t = u.getDocCount(u.SearchCreationDate(currentYear));
        Integer N_at1_t = u.getDocCount(
                u.BooleanQueryAnd(
                        u.SearchCreationDate(currentYear), u.SearchTag(futureTag)));
        return (1.0 * N_at1_t) / N_t;
    }

    private double getConservativenessProbability(int averageLength, Integer eid, int currentYear) {
        // idea Conservativeness is independent of the tags it only depends to the author
        double output = 0;
        int firstYear = getFirstYearOfAuthor(eid);
        int lastYear = getLastYearOfAuthor(eid);
        int count = 0;
        for (int y = firstYear; y < Math.max(Math.max(averageLength + firstYear, lastYear), currentYear); y++) {
            output += getConservativenessProbabilityByYear(eid, y);
            count++;
        }
        // TODO handle the case of (count==0)!
        return output / count;
    }

    private int getLastYearOfAuthor(Integer eid) {
        // TODO this function returns the maximum year value of activation of Author eid
        return 0;
    }

    private int getFirstYearOfAuthor(Integer eid) {
        // TODO this function returns the minimum year value of activation of Author eid
        return 0;
    }

    private double getConservativenessProbabilityByYear(Integer eid, int year) {
        double output;
        HashSet<String> A_t = getTagsByAuthorAndYear(year, eid);
        HashSet<String> A_t_1 = getTagsByAuthorAndYear(year + 1, eid);
        HashSet<String> IntersectionSet = new HashSet<>();
        IntersectionSet.addAll(A_t);
        IntersectionSet.retainAll(A_t_1);
        A_t_1.addAll(A_t);
        //TODO handel the zero case
        output = (A_t_1.size() == 0 ? 0 : (IntersectionSet.size() * 1.0) / A_t_1.size());
        return output;
    }

    HashSet<String> getTagsByAuthorAndYear(int year, Integer eid) {
        //TODO implement this method
        return null;
    }


    private HashSet<String> getTags(int year) {
        // TODO check the validity of this function
        HashSet<String> Tags = new HashSet<String>();
        String delims = "[<>]";
        BytesRef lowerBR = new BytesRef(String.valueOf(year));
        BytesRef upperBR = new BytesRef(String.valueOf(year + 1));
        Query query = new TermRangeQuery("CreationDate", lowerBR, upperBR, true, true);
        try {
            TopDocs hits = searcher.search(query, Integer.MAX_VALUE);
            ScoreDoc[] ScDocs = hits.scoreDocs;
            for (int i = 0; i < ScDocs.length; ++i) {
                int docId = ScDocs[i].doc;
                Document d = searcher.doc(docId);
                for (String tag : d.get("Tags").split(delims)) {
                    Tags.add(tag);
                }
            }
            return Tags;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
