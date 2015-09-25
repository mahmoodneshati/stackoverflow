package PLM;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.index.*;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.BytesRefBuilder;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.apache.lucene.document.*;
import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.lucene.queryparser.classic.ParseException;

import java.io.IOException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Query;

/**
 * Created by Zohreh on 8/26/2015.
 */
public class Utility {
    String IndexDir;
    IndexReader reader;
    IndexSearcher searcher;

    public static void main(String args[]) {
        Utility u = new Utility();

        ArrayList<Integer> PIds = new ArrayList<Integer>();
        System.out.println("\ntest:\n");
        PIds = u.getPostIDs(u.SearchTag("html"));//correct
        PIds = u.getPostIDs(u.SearchTag("css3"));//correct

        System.out.println("\ntest:\n");
        PIds = u.getPostIDs(u.SearchBody("standard"));//correct
        PIds = u.getPostIDs(u.SearchBody2("standard"));//correct
        PIds = u.getPostIDs(u.SearchBody("time"));//correct
        PIds = u.getPostIDs(u.SearchBody2("time"));//correct

        //????? wrong
        System.out.println("\ntest:\n");
        PIds = u.getPostIDs(u.SearchTitle("MySQL"));
        PIds = u.getPostIDs(u.SearchTitle2("MySQL"));
        PIds = u.getPostIDs(u.SearchTitle("Binary"));
        PIds = u.getPostIDs(u.SearchTitle2("Binary"));

        System.out.println("\ntest:\n");
        PIds = u.getPostIDs(u.SearchOwnerUserId(2));//correct
        PIds = u.getPostIDs(u.SearchPostId(6));//correct

        System.out.println("\ntest:\n");
        PIds = u.getPostIDs(u.SearchCreationDate(2008));//correct
        PIds = u.getPostIDs(u.SearchCreationDate(2009));//correct
        PIds = u.getPostIDs(u.SearchCreationDate(2010));//correct
        PIds = u.getPostIDs(u.SearchCreationDate(2012));//correct

        System.out.println("\ntest:\n");
        PIds = u.getPostIDs(u.SearchCreationDateRange("20080725", "200807301712"));//correct
        PIds = u.getPostIDs(u.SearchCreationDateRange("20090101", "20091231"));//correct

        System.out.println("\ntest:\n");
        PIds = u.getPostIDs(u.SearchCreationDateRange2("20080725", "200807301712"));//correct
        PIds = u.getPostIDs(u.SearchCreationDateRange2("20090101", "20091231"));//correct

        System.out.println("\ntest:\n");
        System.out.println("OR:");
        PIds = u.getPostIDs(u.SearchTag("c#"));//correct
        PIds = u.getPostIDs(u.SearchBody("standard"));//correct
        PIds = u.getPostIDs(u.BooleanQueryOr(u.SearchTag("c#"), u.SearchBody("standard")));//correct

        System.out.println("\ntest:\n");
        System.out.println("AND:");
        PIds = u.getPostIDs(u.SearchTag("html"));//correct
        PIds = u.getPostIDs(u.SearchBody("standard"));//correct
        PIds = u.getPostIDs(u.BooleanQueryAnd(u.SearchTag("html"), u.SearchBody("standard")));//correct


        System.out.println("\ntest:\n");
        ArrayList<String> BodyTerms = u.getTerms(u.SearchPostId(4), "Body");//correct

        System.out.println("\ntest:\n");
        ArrayList<String> TitleTerms = u.getTerms(u.SearchPostId(4), "Title");//correct

        System.out.println("\ntest:\n");
        u.getFreqOfWordInBody("return");//correct
        u.getFreqOfWordInBody2("return");//correct

        System.out.println("\ntest:\n");
        u.getFreqOfWordInBody("i");//correct
        u.getFreqOfWordInBody2("i");//correct


        System.out.println("\ntest:\n");
        PIds = u.getPostIDs(u.SearchBody("time"));//correct
        u.getDocCountByWordInBody("time");//correct
        u.getDocCount(u.SearchBody("time"));//correct

        System.out.println("\ntest:\n");
        PIds = u.getPostIDs(u.SearchBody("i"));//correct
        u.getDocCountByWordInBody("i");//correct
        u.getDocCount(u.SearchBody("i"));//correct

        System.out.println("\ntest:\n");
        PIds = u.getPostIDs(u.SearchTitle("i"));//correct
        u.getDocCountByWordInTitle("i");//correct
        u.getDocCount(u.SearchTitle("i"));//correct

        //????? wrong
        System.out.println("\ntest:\n");
        PIds = u.getPostIDs(u.SearchTitle("MySQL"));//wrong
        PIds = u.getPostIDs(u.SearchTitle2("MySQL"));//correct
        u.getDocCountByWordInTitle("MySQL");//wrong
        u.getDocCountByWordInTitle2("MySQL");//correct
        u.getDocCount(u.SearchTitle("MySQL"));//wrong
        u.getDocCount(u.SearchTitle2("MySQL"));//correct

        System.out.println("\ntest:\n");
        PIds = u.getPostIDs(u.SearchCreationDate(2009));//correct
        PIds = u.getPostIDs(u.SearchOwnerUserId(9));//correct
        PIds = u.getPostIDs(u.BooleanQueryAnd(u.SearchOwnerUserId(9), u.SearchCreationDate(2009)));//correct

        System.out.println("\ntest:\n");
        ArrayList<Integer> ActivityYears = u.getActivityYearsByExpertID(9);//wrong in search function
        for(Integer year:ActivityYears)
            System.out.println(year);

        System.out.println("\ntest:\n");
        for(Integer year:u.getExpertsBYTagandYear("html",2009))
            System.out.println("ExpertID: "+year);

        System.out.println("\ntest:\n");
        for(Integer year:u.getExpertsBYTagandYear("c#",2010))
            System.out.println("ExpertID: "+year);


        //u.getFreqOfWordInBody(17,"i");//wrong- with error
    }

    public Utility() {
        try {
            IndexDir = "testindex";
            reader = DirectoryReader.open(FSDirectory.open(Paths.get(IndexDir)));
            searcher = new IndexSearcher(reader);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //1 correct
    public Query SearchTag(String tag){
        System.out.println("Searching for "+tag+" tag ...");
        Query query = new TermQuery(new Term("Tags", tag));
        return query;
    }

    //correct
    public Query SearchBody(String word){
        System.out.println("Searching for "+word+" in Body field ...");
        Query query = new TermQuery(new Term("Body", word));
        return query;
    }

    //correct
    public Query SearchBody2(String word){
        try {
            System.out.println("Searching for "+word+" in Body field ...");
            Analyzer analyzer = new StandardAnalyzer();
            QueryParser parser = new QueryParser("Body", analyzer);
            Query query = parser.parse("Body:"+word);
            return query;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    //2
    public Query SearchTitle(String word){
        System.out.println("Searching for "+word+" in Title field ...");
        Query query = new TermQuery(new Term("Title", word));
        return query;
    }

    public Query SearchTitle2(String word){
        try {
            System.out.println("Searching for "+word+" in Title field ...");
            Analyzer analyzer = new StandardAnalyzer();
            QueryParser parser = new QueryParser("Title", analyzer);
            Query query = parser.parse("Title:"+word);
            return query;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    //3 correct
    public Query SearchOwnerUserId(Integer UsersID){
        System.out.println("Searching for "+UsersID+" as OwnerUserID...");
        Query query = NumericRangeQuery.newIntRange("OwnerUserId", UsersID, UsersID,true,true);
        return query;
    }

    //4 correct
    public Query SearchPostId(Integer PostID){
        System.out.println("Searching for "+PostID+" as Post ID...");
        Query query = NumericRangeQuery.newIntRange("Id", PostID, PostID,true,true);
        return query;
    }

    //5 correct
    public Query SearchCreationDateRange(String lower, String upper){
        System.out.println("Searching for CreationDate Range:["+lower+" TO "+upper+"]");
        BytesRef lowerBR = new BytesRef(lower);
        BytesRef upperBR = new BytesRef(upper);
        Query query = new TermRangeQuery("CreationDate", lowerBR, upperBR, true, true);
        return query;
    }

    //5 correct
    public Query SearchCreationDateRange2(String lower, String upper){
        try {
            System.out.println("2*: Searching for Post IDs By CreationDate Range:["+lower+" TO "+upper+"]");
            Analyzer analyzer = new StandardAnalyzer();
            QueryParser parser = new QueryParser("Title", analyzer);
            Query query = parser.parse("CreationDate:["+lower+" TO "+upper+"]");
            return query;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    //correct
    public Query SearchCreationDate(int year) {
        System.out.println("Searching for Creation year: "+year);
        BytesRef lowerBR = new BytesRef(String.valueOf(year));
        BytesRef upperBR = new BytesRef(String.valueOf(year+1));
        Query query = new TermRangeQuery("CreationDate", lowerBR, upperBR, true, true);
        return query;
    }

    //6 correct
    public BooleanQuery BooleanQueryOr(Query q1, Query q2){
        System.out.println("OR Operation:");
        BooleanQuery query = new BooleanQuery();
        query.add(q1, BooleanClause.Occur.SHOULD);
        query.add(q2, BooleanClause.Occur.SHOULD);
        return query;
    }

    //6 correct
    public BooleanQuery BooleanQueryAnd(Query q1, Query q2){
        System.out.println("AND Operation:");
        BooleanQuery query = new BooleanQuery();
        query.add(q1, BooleanClause.Occur.MUST);
        query.add(q2, BooleanClause.Occur.MUST);
        return query;
    }

    // correct
    public void getFreqOfWordInBody(String word){
        try {
            Query query = new TermQuery(new Term("Body", word));
            TopDocs hits = searcher.search(query, Integer.MAX_VALUE);
            System.out.println(hits.totalHits+" total matching documents");
            ScoreDoc[] ScDocs = hits.scoreDocs;
            for (int i = 0; i < ScDocs.length; ++i) {
                int docId = ScDocs[i].doc;
                Document d = searcher.doc(docId);
                //System.out.println(d.toString());
                Terms terms = reader.getTermVector(docId, "Body"); //get terms vectors for one document and one field
                if (terms != null && terms.size() > 0) {
                    TermsEnum termsEnum = terms.iterator(); // access the terms for this field
                    BytesRef term = null;
                    while((term = termsEnum.next()) != null) {
                        final String keyword = term.utf8ToString();
                        long termFreq = termsEnum.totalTermFreq();
                        if(keyword.equalsIgnoreCase(word))
                            System.out.println("DocID: "+d.get("Id")+", term: "+keyword+", termFreq = "+termFreq);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //7 correct
    public void getFreqOfWordInBody2(String word){
        try {
            Analyzer analyzer = new StandardAnalyzer();
            QueryParser parser = new QueryParser("Body", analyzer);
            Query query = parser.parse("Body:" + word);

            TopDocs hits = searcher.search(query, Integer.MAX_VALUE);
            System.out.println(hits.totalHits+" total matching documents");
            ScoreDoc[] ScDocs = hits.scoreDocs;
            for (int i = 0; i < ScDocs.length; ++i) {
                int docId = ScDocs[i].doc;
                Document d = searcher.doc(docId);
                //System.out.println(d.toString());
                Terms terms = reader.getTermVector(docId, "Body"); //get terms vectors for one document and one field
                if (terms != null && terms.size() > 0) {
                    TermsEnum termsEnum = terms.iterator(); // access the terms for this field
                    BytesRef term = null;
                    while((term = termsEnum.next()) != null) {
                        final String keyword = term.utf8ToString();
                        long termFreq = termsEnum.totalTermFreq();
                        if(keyword.equalsIgnoreCase(word))
                        System.out.println("DocID: "+d.get("Id")+", term: "+keyword+", termFreq = "+termFreq);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    //8 correct
    public Integer getDocCountByWordInBody(String word){
        try{
            System.out.println(word+" occurs in Body of "+reader.docFreq(new Term("Body", word))+" document");
            return reader.docFreq(new Term("Body",word));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    //8 wrong
    public Integer getDocCountByWordInTitle(String word) {
        try{
            Integer output = reader.docFreq(new Term("Title", word));
            System.out.println(word+" occur in Title of "+output+" document");
            return output;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    //correct
    public Integer getDocCountByWordInTitle2(String word) {
        try{
            Analyzer analyzer = new StandardAnalyzer();
            QueryParser parser = new QueryParser("Title", analyzer);
            Query query = parser.parse("Title:" + word);
            TopDocs hits = searcher.search(query, Integer.MAX_VALUE);
            System.out.println(word+" occur in Title of "+hits.totalHits+" document");
            return hits.totalHits;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return -1;
    }

    //correct
    public Integer getDocCount(Query q){
        try {
            TopDocs hits = searcher.search(q, Integer.MAX_VALUE);
            System.out.println(hits.totalHits+" total matching documents");
            return hits.totalHits;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Get PostID of query result
     * @param q input query
     * @return list of PostIDs
     */
    public ArrayList<Integer> getPostIDs(Query q){
        try {
            ArrayList<Integer> PIDs= new ArrayList<Integer>();
            TopDocs hits = searcher.search(q, Integer.MAX_VALUE);
            System.out.println(hits.totalHits+" total matching documents");

            ScoreDoc[] ScDocs = hits.scoreDocs;
            for (int i = 0; i < ScDocs.length; ++i) {
                int docId = ScDocs[i].doc;
                Document d = searcher.doc(docId);
                System.out.println(d.toString());
                System.out.println("Post Id:" + d.get("Id"));
                PIDs.add(Integer.parseInt(d.get("Id")));
            }
            return PIDs;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ArrayList<Integer> getActivityYearsByExpertID(Integer eid) {
        ArrayList<Integer> activityYears = new ArrayList<Integer>();
        try {
            for (int year = 2008; year < 2016; year++){
                Query q = BooleanQueryAnd(SearchOwnerUserId(eid), SearchCreationDate(year));
                TopDocs hits = searcher.search(q, 1);//retrieve more than one docs
                System.out.println(hits.totalHits + " total matching documents");
                if (hits.totalHits > 0){
                    activityYears.add(year);
                }
            }
            return activityYears;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return activityYears;
    }

    public ArrayList<String> getTerms(Query q,String field){
        try {
            ArrayList<String> STerms= new ArrayList<String>();
            TopDocs hits = searcher.search(q, Integer.MAX_VALUE);
            System.out.println(hits.totalHits+" total matching documents");

            ScoreDoc[] ScDocs = hits.scoreDocs;
            System.out.println(field+" Terms:");
            for (int i = 0; i < ScDocs.length; ++i) {
                int docId = ScDocs[i].doc;
                Document d = searcher.doc(docId);
                Terms terms = reader.getTermVector(i, field); //get terms vectors for one document and one field
                if (terms != null && terms.size() > 0) {
                    TermsEnum termsEnum = terms.iterator(); // access the terms for this field
                    BytesRef term = null;
                    while((term = termsEnum.next()) != null) {
                        final String keyword = term.utf8ToString();
                        long termFreq = termsEnum.totalTermFreq();
                        System.out.println("term: "+keyword+", termFreq = "+termFreq);
                        STerms.add(keyword);
                    }
                }
            }
            return STerms;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public HashSet<Integer> getExpertsBYTagandYear(String Tag,Integer year){
        Query q = BooleanQueryAnd(SearchCreationDate(year),SearchTag(Tag));
        HashSet<Integer> ExpertIDs= new HashSet<Integer>();
        try {
            TopDocs hits = searcher.search(q, Integer.MAX_VALUE);
            //System.out.println(hits.totalHits+" total matching documents");
            ScoreDoc[] ScDocs = hits.scoreDocs;
            for (int i = 0; i < ScDocs.length; ++i) {
                int docId = ScDocs[i].doc;
                Document d = searcher.doc(docId);
                ExpertIDs.add(Integer.parseInt(d.get("OwnerUserId")));
            }
            return ExpertIDs;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ExpertIDs;
    }

    //***********
    //7 with error
    public long getFreqOfWordInBody(Integer DocID, String word){
        try {
            IndexReader reader2 = DirectoryReader.open(FSDirectory.open(Paths.get(IndexDir)));
            Terms terms = reader2.getTermVector(DocID, "Body"); //get terms vectors for one document and one field
            System.out.println(terms.size());
            if (terms != null && terms.size() > 0) {
                TermsEnum termsEnum = terms.iterator(); // access the terms for this field
                BytesRef term = null;
                while ((term = termsEnum.next()) != null) {
                    final String keyword = term.utf8ToString();
                    long termFreq = termsEnum.totalTermFreq();
                    if (keyword.equalsIgnoreCase(word)) {
                        System.out.println("term: " + keyword + ", termFreq = " + termFreq);
                        return termFreq;
                    }

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int getIntersection(ArrayList<String> list1, ArrayList<String> l2){
        list1.retainAll(l2);
        return list1.size();
    }

    public Integer getAuthorsCountByQuery(BooleanQuery query) {
        //TODO implement this function
        return null;
    }
}
