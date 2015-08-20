import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.*;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.search.ScoreDoc;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.apache.lucene.document.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class SearchIndex {

    public static void main(String[] args) {
        long start = new Date().getTime();

        SearchIndex ms = new SearchIndex();

        long end = new Date().getTime();
        System.out.println("Searching took " + (end - start) + " milliseconds");
    }

    public SearchIndex() {
        try {
            String indexDir = "index";
        String q = "";

        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexDir)));
        IndexSearcher searcher = new IndexSearcher(reader);
        Analyzer analyzer = new StandardAnalyzer();
        String field="Id";
        QueryParser parser = new QueryParser(field, analyzer);

        String line;
        int count=0;
        BufferedReader reader2 = new BufferedReader(new FileReader("PostsN.xml"));
        while ((line = reader2.readLine()) != null) {
            if (line.trim().startsWith("<row")) {
                count +=1;
                if (count>1)
                    break;
                System.out.println(line);
                Elements row = Jsoup.parse(line).getElementsByTag("row");

                Integer Id = getIntegerValue(row, "Id");
                Integer PostTypeId = getIntegerValue(row, "PostTypeId");
                Integer ParentId = getIntegerValue(row, "ParentId");
                Integer AcceptedAnswerId = getIntegerValue(row, "AcceptedAnswerId");
                Date CreationDate = getDateValue(row, "CreationDate");
                Integer Score = getIntegerValue(row, "Score");
                Integer ViewCount = getIntegerValue(row, "ViewCount");
                String Body = getStringValue(row, "Body");
                Integer OwnerUserId = getIntegerValue(row, "OwnerUserId");
                Integer LastEditorUserId = getIntegerValue(row, "LastEditorUserId");
                String LastEditorDisplayName = getStringValue(row, "LastEditorDisplayName");
                Date LastEditDate = getDateValue(row, "LastEditDate");
                Date LastActivityDate = getDateValue(row, "LastActivityDate");
                String Title = getStringValue(row, "Title");
                ArrayList<String> Tags = getStringList(row, "Tags");
                Integer AnswerCount = getIntegerValue(row, "AnswerCount");
                Integer CommentCount = getIntegerValue(row, "CommentCount");
                Integer FavoriteCount = getIntegerValue(row, "FavoriteCount");
                Date CommunityOwnedDate = getDateValue(row, "CommunityOwnedDate");

                Query query = parser.parse("4");
                System.out.println("Searching for: " + query.toString(field));

                // Collect enough docs to show 5 pages
                TopDocs results = searcher.search(query, Integer.MAX_VALUE);
                ScoreDoc[] hits = results.scoreDocs;

                int numTotalHits = results.totalHits;
                System.out.println(numTotalHits + " total matching documents");

                /*for (int i = 0; i < hits.length(); i++) {
                    Document doc = hits.doc(i);
                    System.out.println(doc.get("filename"));
                }*/
            }
        }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    private ArrayList<String> getStringList(Elements row, String tag) {
        ArrayList<String> out = new ArrayList<>();
        String[] ss = row.attr(tag).split("&lt;|&gt;");
        for (String s : ss) {
            if (s != null && s.trim().length() > 0)
                out.add(s.trim());
        }
        return out;

    }

    private String getStringValue(Elements row, String tag) {
        try {
            String html = row.attr(tag);
            html = html.replace("&lt;", "<").replace("&gt;", ">");
            return Jsoup.parse(html).text();
        } catch (Exception e) {
            return null;
        }
    }

    private Date getDateValue(Elements row, String tag) {
        Date date;
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S");
            date = formatter.parse(row.attr(tag));


        } catch (Exception e) {
            date = null;
        }
        return date;
    }

    private Integer getIntegerValue(Elements row, String tag) {

        Integer value;
        try {
            value = Integer.parseInt(row.attr(tag));
        } catch (Exception e) {
            value = null;
        }
        return value;

    }
}

