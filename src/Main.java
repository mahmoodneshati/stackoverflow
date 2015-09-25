import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by Mahmood on 8/18/2015.
 * Google
 */
public class Main {
    private final String indexPath;
    Analyzer analyzer;
    private Directory fsDir;
    private Directory ramDir;
    IndexWriter ramWriter, fileWriter;

    int maxInMemoryDoc = 100000;
    int countInMemoryDoc = 0;

    /*HashSet<Integer> postIDs;
    HashMap<Integer,HashSet<String>> AnswerTags;
    public void getPostIDs(){
        try {
            BufferedReader reader = new BufferedReader(new FileReader("D:\\Sharif\\Project\\Lucene Index\\StackOverflow\\java_all.txt"));
            String line;
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                String[] temp = line.split(",");
                try {
                    postIDs.add(Integer.parseInt(temp[1]));
                    postIDs.add(Integer.parseInt(temp[2]));
                    addToHashMap(Integer.parseInt(temp[2]),temp[14]);
                } catch (Exception e) {}
            }
            reader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addToHashMap(int answerID, String tag) {
        HashSet set=AnswerTags.get(answerID);
        if(set==null){
            set = new HashSet();
            AnswerTags.put(answerID,set);
        }
        set.add(tag);
    }*/

    public void index(String xmlFile) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(xmlFile));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().startsWith("<row")) {
                    Elements row = Jsoup.parse(line).getElementsByTag("row");
                    /*if (row.attr("Tags").toLowerCase().contains("<ios>")) {
                        Post post = new Post(line);
                        addToIndex(post.getLuceneDocument());
                    }*/

                    //if (postIDs.contains(Integer.parseInt(row.attr("Id")))){
                        //Post post = new Post(line,AnswerTags.get(Integer.parseInt(row.attr("Id"))));
                        Post post = new Post(line);
                        addToIndex(post.getLuceneDocument());
                    //}
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

    public Main(String indexPath) {
        this.indexPath = indexPath;
        //postIDs=new HashSet<Integer>();
        //AnswerTags=new HashMap<Integer,HashSet<String>>();
        try {
            setUp();
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException, ParseException {
        Main m = new Main(".\\testindex");
        //m.getPostIDs();

        //System.out.println(m.postIDs.size());
        m.index("Posts1.xml");
        //m.index("D:\\Sharif\\Project\\StackOverflow\\StackOverflow Data\\stackoverflow.com-Posts\\Posts.xml");
    }
}
