package PLM;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import PLM.Utility;

/**
 * Created by Mahmood on 8/26/2015.
 * Google
 */
public class PLM {
    String IndexDir;
    IndexReader reader;
    IndexSearcher searcher;
    Utility u;

    public static void main(String args[]) {
        PLM p = new PLM();

        //HashSet<String> TitleTerms = p.getTags(2008);
        p.getWordProbabilityByTagAndYear(2008, "c#", "datetime");
    }

    public PLM() {
        try {
            IndexDir = "index2";
            reader = DirectoryReader.open(FSDirectory.open(Paths.get(IndexDir)));
            searcher = new IndexSearcher(reader);
            u = new Utility();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public double getWordProbabilityByExpertAndYear(String word, String eid, int year) {
        HashSet<String> tags = getTags(year);
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
        //I don't have any other idea for these variable names
        Integer TotalDocCountWithSpecificTag = u.getDocCount(u.SearchTag(tag));
        //System.out.println("TotalDocCountWithSpecificTag:"+TotalDocCountWithSpecificTag);

        Integer TotalDocCountWithSpecificTagAndWordInBody = u.getDocCount(u.BooleanQueryAnd(u.BooleanQueryAnd(u.SearchBody(word),u.SearchTag(tag)), u.SearchCreationDate(year)));
        //System.out.println("TotalDocCountWithSpecificTagAndWordInBody:"+TotalDocCountWithSpecificTagAndWordInBody);

        return TotalDocCountWithSpecificTagAndWordInBody/TotalDocCountWithSpecificTag;
    }

    private HashSet<String> getTags(int year) {
        HashSet<String> Tags = new HashSet<String>();
        String delims = "[<>]";
        BytesRef lowerBR = new BytesRef(String.valueOf(year));
        BytesRef upperBR = new BytesRef(String.valueOf(year+1));
        Query query = new TermRangeQuery("CreationDate", lowerBR, upperBR, true, true);
        try {
            TopDocs hits = searcher.search(query, Integer.MAX_VALUE);
            ScoreDoc[] ScDocs = hits.scoreDocs;
            for (int i = 0; i < ScDocs.length; ++i) {
                int docId = ScDocs[i].doc;
                Document d = searcher.doc(docId);
                for (String tag:d.get("Tags").split(delims)){
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
