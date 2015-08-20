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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;


public class Searcher {

    public static void main(String[] args) throws IOException, ParseException {
        long start = new Date().getTime();

        String indexDir = "index";
        String q = "*:*";
        Searcher s= new Searcher(indexDir, q);

        long end = new Date().getTime();
        System.out.println("Searching took " + (end - start) + " milliseconds");
    }

    public Searcher(String indexDir, String q)  {
        try {
            IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexDir)));
            IndexSearcher searcher = new IndexSearcher(reader);
            Analyzer analyzer = new StandardAnalyzer();

            String queries="";
            String field="Score";

            QueryParser parser = new QueryParser(field, analyzer);

            Query query = parser.parse(q);
            System.out.println("Searching for: " + query.toString(field));

            searcher.search(query, 100);

            // Collect enough docs to show 5 pages
            TopDocs results = searcher.search(query, 10);
            ScoreDoc[] hits = results.scoreDocs;

            int numTotalHits = results.totalHits;
            System.out.println(numTotalHits + " total matching documents");


            int start = 0;
            int end = Math.min(numTotalHits, 10);

            for (int i = start; i < end; i++) {
                System.out.println("doc="+hits[i].doc+" score="+hits[i].score);

                Document doc = searcher.doc(hits[i].doc);
            }

            reader.close();

            IndexReader reader2 = DirectoryReader.open(FSDirectory.open(Paths.get(indexDir)));
            int c=0;
            for (int i=0; i<reader2.maxDoc(); i++) {
                Document doc = reader2.document(i);
                String docId = doc.get("Id");
                String Score = doc.get("Score");
                String Title = doc.get("Title");
                //System.out.println("Id="+docId+" \'nScore="+Score+" Title="+Title);

                c=c+1;
            }
            System.out.println("TotalNum="+c);

            reader2.close();
            //Directory indexDir = FSDirectory.open(".\\index");
            //IndexReader rdr = DirectoryReader.open(FSDirectory.open(new File(indexDir)));
            //IndexSearcher is = new IndexSearcher(rdr);

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    /*Analyzer analyzer = new StandardAnalyzer();

    // Store the index in memory:
    Directory directory = new RAMDirectory();
    // To store an index on disk, use this instead:
    //Directory directory = FSDirectory.open("/tmp/testindex");
    IndexWriterConfig config = new IndexWriterConfig(analyzer);
    IndexWriter iwriter = new IndexWriter(directory, config);
    Document doc = new Document();
    String text = "This is the text to be indexed.";
    doc.add(new Field("fieldname", text, TextField.TYPE_STORED));
    iwriter.addDocument(doc);
    iwriter.close();

    // Now search the index:
    DirectoryReader ireader = DirectoryReader.open(directory);
    IndexSearcher isearcher = new IndexSearcher(ireader);
    // Parse a simple query that searches for "text":
    QueryParser parser = new QueryParser("fieldname", analyzer);
    Query query = parser.parse("text");
    ScoreDoc[] hits = isearcher.search(query, null, 1000).scoreDocs;
    assertEquals(1, hits.length);
    // Iterate through the results:
    for (int i = 0; i < hits.length; i++) {
        Document hitDoc = isearcher.doc(hits[i].doc);
        assertEquals("This is the text to be indexed.", hitDoc.get("fieldname"));
    }
    ireader.close();
    directory.close();*/
}
