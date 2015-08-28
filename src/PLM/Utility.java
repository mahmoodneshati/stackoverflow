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
        PIds = u.getIntegerResults(u.getPostIDsByTag("<c#>"));
        PIds = u.getIntegerResults(u.getPostIDsByWordOfBody("standard"));
        PIds = u.getIntegerResults(u.BooleanQueryOr(u.getPostIDsByTag("<c#>"), u.getPostIDsByWordOfBody("standard")));

        PIds = u.getIntegerResults(u.getPostIDsByTag("<html>"));
        PIds = u.getIntegerResults(u.getPostIDsByWordOfBody("standard"));
        PIds = u.getIntegerResults(u.BooleanQueryAnd(u.getPostIDsByTag("<html>"), u.getPostIDsByWordOfBody("standard")));

        PIds = u.getIntegerResults(u.getPostIDsByWordOfTitle("MySQL"));
        PIds = u.getIntegerResults(u.getPostIDsByWordOfTitle2("MySQL"));
        PIds = u.getIntegerResults(u.getPostIDsByWordOfTitle("Binary"));
        PIds = u.getIntegerResults(u.getPostIDsByWordOfTitle2("Binary"));

        PIds = u.getIntegerResults(u.getPostIDsByOwnerUserId(1));

        ArrayList<String> BodyTerms = new ArrayList<String>();
        BodyTerms = u.getStringResults(u.getBodyTermsByPostId(4),"Body");
        ArrayList<String> TitleTerms = new ArrayList<String>();
        TitleTerms = u.getStringResults(u.getTiTleTermsByPostId(4), "Title");

        PIds = u.getIntegerResults(u.getPostIDsByCreationDateRange("2008-07-31", "2008-08-01"));
        PIds = u.getIntegerResults(u.getPostIDsByCreationDateRange2("2008-07-31", "2008-08-01"));

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
    public Query getPostIDsByTag(String tag){
        System.out.println("Searching for Post IDs with "+tag+" tag ...");
        Query query = new WildcardQuery(new Term("Tags", "*"+tag+"*"));
        return query;
    }

    //2
    public Query getPostIDsByWordOfBody(String word){
        System.out.println("Searching for Post IDs contain "+word+" word in Body field ...");
        Query query = new WildcardQuery(new Term("Body", "*"+word+"*"));
        return query;
    }

    //2
    public Query getPostIDsByWordOfTitle(String word){
        System.out.println("Searching for Post IDs contain "+word+" word in Title field ...");
        Query query = new WildcardQuery(new Term("Title", "*"+word+"*"));
        return query;
    }

    //2
    public Query getPostIDsByWordOfTitle2(String word){
        try {
            System.out.println("Searching for Post IDs contain "+word+" word in Title field ...");
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
    public Query getPostIDsByOwnerUserId(Integer UsersID){
        System.out.println("Searching for Post IDs with "+UsersID+" as OwnerUserID...");
        Query query = NumericRangeQuery.newIntRange("OwnerUserId", UsersID, UsersID,true,true);
        return query;
    }

    //4
    public Query getBodyTermsByPostId(Integer PostID){
        System.out.println("Searching for Body Terms By "+PostID+" as Post ID...");
        Query query = NumericRangeQuery.newIntRange("Id", PostID, PostID,true,true);
        return query;
    }

    //4
    public Query getTiTleTermsByPostId(Integer PostID){
        System.out.println("Searching for Title Terms By "+PostID+" as Post ID...");
        Query query = NumericRangeQuery.newIntRange("Id", PostID, PostID,true,true);
        return query;
    }

    //5
    public Query getPostIDsByCreationDateRange(String lower, String upper){
        System.out.println("Searching for Post IDs By CreationDate Range:["+lower+" TO "+upper+"]");
        BytesRef lowerBR = new BytesRef(lower);
        BytesRef upperBR = new BytesRef(upper);
        Query query = new TermRangeQuery("CreationDate", lowerBR, upperBR, true, true);
        return query;
    }

    //5
    public Query getPostIDsByCreationDateRange2(String lower, String upper){
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

    //6
    public BooleanQuery BooleanQueryOr(Query q1, Query q2){
        System.out.println("OR Operation of:");
        BooleanQuery query = new BooleanQuery();
        query.add(q1, BooleanClause.Occur.SHOULD);
        query.add(q2, BooleanClause.Occur.SHOULD);
        return query;
    }

    //6
    public BooleanQuery BooleanQueryAnd(Query q1, Query q2){
        System.out.println("AND Operation of:");
        BooleanQuery query = new BooleanQuery();
        query.add(q1, BooleanClause.Occur.MUST);
        query.add(q2, BooleanClause.Occur.MUST);
        return query;
    }

    public ArrayList<Integer> getIntegerResults(Query q){
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

    public ArrayList<String> getStringResults(Query q,String field){
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

}
