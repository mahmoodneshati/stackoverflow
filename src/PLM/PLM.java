package PLM;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Mahmood on 8/26/2015.
 * Google
 */
public class PLM {
    IndexReader reader;
    public double getWordProbabilityByExpertAndYear(String word, String eid, int year) {
        ArrayList<String> tags = getTags(year);
        double output = 0;
        for (String tag : tags) {
            output += getWordProbabilityByTagAndYear(year, tag, word) * getTagProbabilityByExpertAndYear(tag, eid, year);

        }
        return output;
    }

    private double getTagProbabilityByExpertAndYear(String tag, String eid, int year) {
        return -1;
    }

    private double getWordProbabilityByTagAndYear(int year, String tag, String word) {

        try {
            reader.docFreq(new Term("Tags",tag))
        } catch (IOException e) {
            e.printStackTrace();
        }

        return -1;
    }

    private ArrayList<String> getTags(int year) {
        return null;
    }

}
