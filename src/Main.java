import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * Created by Mahmood on 8/18/2015.
 * Google
 */
public class Main {
    private final String indexPath;
    String Tes;
    Analyzer analyzer;
    private Directory fsDir;
    private Directory ramDir;
    IndexWriter ramWriter, fileWriter;

    int maxInMemoryDoc = 100000;
    int countInMemoryDoc = 0;

    public void index(String xmlFile) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(xmlFile));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().startsWith("<row")) {
                    Post post = new Post(line);
                    addToIndex(post.getLuceneDocument());

                }

            }
            reader.close();
            closeIndex();


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void closeIndex() throws IOException {


        ramWriter.close();
        fsDir = FSDirectory.open(Paths.get(indexPath));
        fileWriter = new IndexWriter(fsDir, new IndexWriterConfig(analyzer));
        fileWriter.addIndexes(ramDir);
        ramDir.close();
        countInMemoryDoc = 0;
        fileWriter.close();
        fsDir.close();
    }

    private void addToIndex(Document document) throws IOException {

        if (countInMemoryDoc < maxInMemoryDoc) {
            // Add to Ram Memory and count up
            ramWriter.addDocument(document);
            countInMemoryDoc++;
        } else {
            System.out.println("making index for " + countInMemoryDoc +" Documents.");
            // Merge Ram Memory and create a new ram memory
            ramWriter.addDocument(document);
            ramWriter.close();

            fsDir = FSDirectory.open(Paths.get(indexPath));
            fileWriter = new IndexWriter(fsDir, new IndexWriterConfig(analyzer));

            fileWriter.addIndexes(ramDir);
            ramDir.close();
            ramDir = new RAMDirectory();
            ramWriter = new IndexWriter(ramDir, new IndexWriterConfig(analyzer));
            countInMemoryDoc = 0;
            fileWriter.close();
            fsDir.close();
        }


    }

    protected void setUp() throws IOException, ParseException {
        analyzer = new StandardAnalyzer();

        ramDir = new RAMDirectory();

        ramWriter = new IndexWriter(ramDir, new IndexWriterConfig(analyzer));

    }

    public Main(String indexPath) {
        this.indexPath = indexPath;
        try {
            setUp();
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException, ParseException {
        Main m = new Main("E:\\research\\stackoverflow\\index");
        m.index("Posts1.xml");
    }
}
