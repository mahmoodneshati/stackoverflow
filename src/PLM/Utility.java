package PLM;

import org.apache.lucene.index.*;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.search.ScoreDoc;
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

    public static void main(String args[]) {
        String indexDir = "index2";
        Utility u = new Utility();

        u.getPostIDsBySpecificTag("<c#>", indexDir);
        u.getPostIDsBySpecificWord("standard", indexDir);
    }
    public Utility() {

    }
    public ArrayList<Integer> getPostIDsBySpecificTag(String tag, String IndexDir){
        try {
            ArrayList<Integer> PIDs= new ArrayList<Integer>();
            IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(IndexDir)));
            IndexSearcher searcher = new IndexSearcher(reader);
            Query query = new WildcardQuery(new Term("Tags", "*"+tag+"*"));
            TopDocs hits = searcher.search(query, Integer.MAX_VALUE);
            System.out.println(hits.totalHits+" total matching documents with "+tag+" tag");

            ScoreDoc[] ScDocs = hits.scoreDocs;
            for (int i = 0; i < ScDocs.length; ++i) {
                int docId = ScDocs[i].doc;
                Document d = searcher.doc(docId);
                System.out.println("Post Id:"+d.get("Id"));
                PIDs.add(Integer.parseInt(d.get("Id")));
            }

            return PIDs;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public ArrayList<Integer> getPostIDsBySpecificWord(String word, String IndexDir){
        try {
            ArrayList<Integer> PIDs= new ArrayList<Integer>();
            IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(IndexDir)));
            IndexSearcher searcher = new IndexSearcher(reader);
            Query query = new WildcardQuery(new Term("Body", "*"+word+"*"));
            TopDocs hits = searcher.search(query, Integer.MAX_VALUE);
            System.out.println(hits.totalHits+" total matching documents contain "+word+" word");

            ScoreDoc[] ScDocs = hits.scoreDocs;
            for (int i = 0; i < ScDocs.length; ++i) {
                int docId = ScDocs[i].doc;
                Document d = searcher.doc(docId);
                System.out.println("Post Id:"+d.get("Id"));
                PIDs.add(Integer.parseInt(d.get("Id")));
            }

            return PIDs;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }



}
