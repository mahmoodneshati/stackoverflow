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
    double beta;

    public static void main(String args[]) {
        PLM p = new PLM();

        //HashSet<String> TitleTerms = p.getTags(2008);
        //p.getWordProbabilityByTagAndYear(2008, "c#", "datetime");
        p.getConservativenessProbability();
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
    public double getWordProbabilityByExpertAndYear(String word, Integer eid, int year) {
        HashSet<String> tags = getTags(year);
        double output = 0;
        for (String tag : tags) {
            output += getWordProbabilityByTagAndYear(year, tag, word) * getTagProbabilityByExpertAndYear(tag, eid, year);
        }
        return output;
    }

    private double getTagProbabilityByExpertAndYear(String tag, Integer eid, int year) {
        HashSet<String> PastYearTags = getTags(year-1);
        double output = 0;
        for (String t : PastYearTags) {
            output += getTagProbabilityByExpertAndPastYearTag(tag, t, eid, year)*getPastYearTagProbabilityByExpert(t, eid, year - 1);
        }

        return output;
    }

    private double getPastYearTagProbabilityByExpert(String tag, Integer eid, int year) {
        Integer N_e_t = u.getDocCount(u.BooleanQueryAnd(u.SearchCreationDate(year), u.SearchOwnerUserId(eid)));
        Integer N_at_e =u.getDocCount(u.BooleanQueryAnd(u.BooleanQueryAnd(u.SearchCreationDate(year),u.SearchOwnerUserId(eid)),u.SearchTag(tag)));
        return N_at_e/N_e_t;
    }

    private double getTagProbabilityByExpertAndPastYearTag(String CurrentTag, String PastYearTag, int eid, int CurrentYear) {
        HashSet<String> PastYearTags = getTags(CurrentYear - 1);
        double output=0;
        output+=(getConservativenessProbability()*getProbabilityChoosingTagFromPastYearTags(CurrentTag,PastYearTag,eid,CurrentYear))+
                    ((1-getConservativenessProbability())*getProbabilityChoosingNewTag(CurrentTag,PastYearTag,eid,CurrentYear));
        return output;
    }

    private double getProbabilityChoosingTagFromPastYearTags(String CurrentTag, String PastYearTag, int eid, int CurrentYear) {
        Integer N_e_t = u.getDocCount(u.BooleanQueryAnd(u.SearchCreationDate(CurrentYear - 1), u.SearchOwnerUserId(eid)));
        Integer N_at1_e_t =u.getDocCount(u.BooleanQueryAnd(u.BooleanQueryAnd(u.SearchCreationDate(CurrentYear-1),u.SearchOwnerUserId(eid)),u.SearchTag(CurrentTag)));
        return N_at1_e_t/N_e_t;
    }

    private double getProbabilityChoosingNewTag(String CurrentTag, String PastYearTag, Integer eid, int CurrentYear) {
        return ((beta*Sim(CurrentTag,PastYearTag,CurrentYear))+((1-beta)*Pop(CurrentTag,CurrentYear)));
    }

    private double Sim(String CurrentTag, String PastYearTag, int CurrentYear) {
        return -1;
    }

    private double Pop(String CurrentTag,int CurrentYear) {
        Integer N_t = u.getDocCount(u.SearchCreationDate(CurrentYear - 1));
        Integer N_at1_t =u.getDocCount(u.BooleanQueryAnd(u.SearchCreationDate(CurrentYear - 1), u.SearchTag(CurrentTag)));
        return N_at1_t/N_t;
    }

    private double getConservativenessProbability(){
        double output = 0;
        for (int year = 2008; year <2016; year++) {
            HashSet<String> A_t = getTags(year);
            HashSet<String> A_t_1 = getTags(year-1);
            HashSet<String> IntersectionSet = new HashSet<String>();
            IntersectionSet.addAll(A_t);
            IntersectionSet.retainAll(A_t_1);
            A_t_1.addAll(A_t);
            output +=IntersectionSet.size()/A_t_1.size();
        }
        return output;
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
