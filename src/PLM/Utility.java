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
        PIds = u.getPostIDs(u.SearchTag("<c#>"));
        PIds = u.getPostIDs(u.SearchBody("standard"));
        PIds = u.getPostIDs(u.BooleanQueryOr(u.SearchTag("<c#>"), u.SearchBody("standard")));

        PIds = u.getPostIDs(u.SearchTag("<html>"));
        PIds = u.getPostIDs(u.SearchBody("standard"));
        PIds = u.getPostIDs(u.BooleanQueryAnd(u.SearchTag("<html>"), u.SearchBody("standard")));

        PIds = u.getPostIDs(u.SearchTitle("MySQL"));
        PIds = u.getPostIDs(u.SearchTitle("Binary"));
        PIds = u.getPostIDs(u.SearchOwnerUserId(1));

        ArrayList<String> BodyTerms = new ArrayList<String>();
        BodyTerms = u.getTerms(u.SearchPostId(4), "Body");
        ArrayList<String> TitleTerms = new ArrayList<String>();
        TitleTerms = u.getTerms(u.SearchPostId(4), "Title");

        //???
        PIds = u.getPostIDs(u.SearchCreationDateRange("2008-07-31", "2008-08-01"));
        PIds = u.getPostIDs(u.SearchCreationDateRange2("2008-07-31", "2008-08-01"));

        u.getFreqOfWordInBody("return");
        u.getDocCount(u.SearchTag("<c#>"));
        u.getDocCountByWordInBody("standard");
        u.getDocCount(u.SearchBody("standard"));

        //???
        u.getDocCountByWordInTitle("Binary");
        u.getDocCountByWordInTitle("MySQL");
    }

    public Utility() {
        try {
            IndexDir = "index2";
            reader = DirectoryReader.open(FSDirectory.open(Paths.get(IndexDir)));
            searcher = new IndexSearcher(reader);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //1
    public Query SearchTag(String tag){
        System.out.println("Searching for "+tag+" tag ...");
        Query query = new WildcardQuery(new Term("Tags", "*"+tag+"*"));
        return query;
    }

    //2
    public Query SearchBody(String word){
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

    //3
    public Query SearchOwnerUserId(Integer UsersID){
        System.out.println("Searching for "+UsersID+" as OwnerUserID...");
        Query query = NumericRangeQuery.newIntRange("OwnerUserId", UsersID, UsersID,true,true);
        return query;
    }

    //4
    public Query SearchPostId(Integer PostID){
        System.out.println("Searching for "+PostID+" as Post ID...");
        Query query = NumericRangeQuery.newIntRange("Id", PostID, PostID,true,true);
        return query;
    }

    //5
    public Query SearchCreationDateRange(String lower, String upper){
        System.out.println("Searching for CreationDate Range:["+lower+" TO "+upper+"]");
        BytesRef lowerBR = new BytesRef(lower);
        BytesRef upperBR = new BytesRef(upper);
        Query query = new TermRangeQuery("CreationDate", lowerBR, upperBR, true, true);
        return query;
    }

    //5
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

    public Query SearchCreationDate(int year) {
        BytesRef lowerBR = new BytesRef(String.valueOf(year));
        BytesRef upperBR = new BytesRef(String.valueOf(year+1));
        Query query = new TermRangeQuery("CreationDate", lowerBR, upperBR, true, true);
        return query;
    }

    //6
    public BooleanQuery BooleanQueryOr(Query q1, Query q2){
        //System.out.println("OR Operation:");
        BooleanQuery query = new BooleanQuery();
        query.add(q1, BooleanClause.Occur.SHOULD);
        query.add(q2, BooleanClause.Occur.SHOULD);
        return query;
    }

    //6
    public BooleanQuery BooleanQueryAnd(Query q1, Query q2){
        //System.out.println("AND Operation:");
        BooleanQuery query = new BooleanQuery();
        query.add(q1, BooleanClause.Occur.MUST);
        query.add(q2, BooleanClause.Occur.MUST);
        return query;
    }

    //7
    public void getFreqOfWordInBody(String word){
        try {
            //System.out.println(reader.docFreq(new Term("Body", word)));
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

    //7
    public long getFreqOfWordInBody(Integer DocID, String word){
        try {
            Terms terms = reader.getTermVector(DocID, "Body"); //get terms vectors for one document and one field
            if (terms != null && terms.size() > 0) {
                TermsEnum termsEnum = terms.iterator(); // access the terms for this field
                BytesRef term = null;
                while((term = termsEnum.next()) != null) {
                    final String keyword = term.utf8ToString();
                    long termFreq = termsEnum.totalTermFreq();
                    if(keyword.equalsIgnoreCase(word)){
                        System.out.println("term: "+keyword+", termFreq = "+termFreq);
                        return termFreq;
                    }

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    //8
    public Integer getDocCountByWordInBody(String word){
        try{
            System.out.println(word+" occurs in Body of "+reader.docFreq(new Term("Body", word))+" document");
            return reader.docFreq(new Term("Body",word));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    //8
    public Integer getDocCountByWordInTitle(String word) {
        try{
            Analyzer analyzer = new StandardAnalyzer();
            QueryParser parser = new QueryParser("Title", analyzer);
            Query query = parser.parse("Title:" + word);
            TopDocs hits = searcher.search(query, Integer.MAX_VALUE);
            //!!!!!!!!!!!!!!!!!!!!
            System.out.println(word+" occur in Title of "+hits.totalHits+" document");
            System.out.println(word+" occur in Title of "+reader.docFreq(new Term("Title", word))+" document");
            return reader.docFreq(new Term("Title",word));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return -1;
    }

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

    public ArrayList<Integer> getPostIDs(Query q){
        try {
            ArrayList<Integer> PIDs= new ArrayList<Integer>();
            TopDocs hits = searcher.search(q, Integer.MAX_VALUE);
            System.out.println(hits.totalHits+" total matching documents");

            ScoreDoc[] ScDocs = hits.scoreDocs;
            for (int i = 0; i < ScDocs.length; ++i) {
                int docId = ScDocs[i].doc;
                Document d = searcher.doc(docId);
                System.out.println("Post Id:" + d.get("Id"));
                PIDs.add(Integer.parseInt(d.get("Id")));
            }
            return PIDs;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
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

    public int getIntersection(ArrayList<String> list1, ArrayList<String> l2){
        list1.retainAll(l2);
        return list1.size();
    }

    public Integer getAuthorsCountByQuery(BooleanQuery query) {
        //TODO implement this function
        return null;
    }
}
