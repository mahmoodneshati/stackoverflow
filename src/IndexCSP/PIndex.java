package IndexCSP;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;

import java.io.*;
import java.nio.file.Paths;

/**
 * Created by Zohreh on 10/12/2015.
 */
public class PIndex {
    private final String indexPath;
    Analyzer analyzer;
    private Directory fsDir;
    private Directory ramDir;
    IndexWriter ramWriter, fileWriter;

    int maxInMemoryDoc = 10000;
    int countInMemoryDoc = 0;


    private Document getLuceneDocument(String tag, Integer year, Integer popularity) {
        Document doc = new Document();
        doc.add(new StringField("Tag", tag, Field.Store.YES));
        doc.add(new IntField("Year", year, Field.Store.YES));
        doc.add(new IntField("Popularity", popularity, Field.Store.YES));
        return doc;
    }

    public void index(String inFile) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(inFile));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] temp = line.split(",");
                //System.out.println(line);
                addToIndex(getLuceneDocument(temp[0], Integer.parseInt(temp[1]), Integer.parseInt(temp[2])));
            }
            reader.close();
            closeIndex();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeIndex() throws IOException {
        ramWriter.close();
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
            System.out.println("making index for " + countInMemoryDoc + " Documents.");
            // Merge Ram Memory and create a new ram memory
            ramWriter.addDocument(document);
            ramWriter.close();
            fileWriter.addIndexes(ramDir);
            ramDir.close();
            ramDir = new RAMDirectory();
            ramWriter = new IndexWriter(ramDir, new IndexWriterConfig(analyzer));
            countInMemoryDoc = 0;
        }
    }

    protected void setUp() throws IOException, ParseException {
        analyzer = new StandardAnalyzer();
        ramDir = new RAMDirectory();
        ramWriter = new IndexWriter(ramDir, new IndexWriterConfig(analyzer));
        fsDir = FSDirectory.open(Paths.get(indexPath));
        fileWriter = new IndexWriter(fsDir, new IndexWriterConfig(analyzer));
    }

    public PIndex(String indexPath) {
        this.indexPath = indexPath;
        try {
            setUp();
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException, ParseException {
        PrintStream stdout = System.out;
        try {
            PrintStream out = new PrintStream(new FileOutputStream("test1.txt"));
            System.setOut(out);
        }
        catch(IOException e) {
            System.out.println("\n\n\n\nSorry!\n\n\n\n");
        }

        PIndex m = new PIndex(".\\PIndex");
        m.index("popularity.txt");
    }

}

