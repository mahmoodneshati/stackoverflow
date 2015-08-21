/**
 * Created by Zohreh on 08/20/2015.
 */
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.document.Document;
import org.apache.lucene.store.FSDirectory;
import java.io.IOException;
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
            Analyzer analyzer = new StandardAnalyzer();

            int c=0;
            for (int i=0; i<reader.maxDoc(); i++) {
                Document doc = reader.document(i);
                String output="";
                System.out.println("Id="+doc.get("Id")+" PostTypeId="+doc.get("PostTypeId")+" ParentId="+doc.get("ParentId")+
                        " AcceptedAnswerId="+doc.get("AcceptedAnswerId")+" CreationDate="+doc.get("CreationDate")+" Score="+
                        doc.get("Score")+" ViewCount="+doc.get("ViewCount")+" Body="+doc.get("Body")+" OwnerUserId="+doc.get("OwnerUserId")+
                                " LastEditorUserId="+doc.get("LastEditorUserId")+" LastEditorDisplayName="+doc.get("LastEditorDisplayName")+
                        " LastEditDate="+doc.get("LastEditDate")+" LastActivityDate="+doc.get("LastActivityDate")+" Title="+doc.get("Title")+
                        " Tags="+doc.get("Tags")+" AnswerCount="+doc.get("AnswerCount")+" CommentCount="+doc.get("CommentCount")+ " FavoriteCount="+
                        doc.get("FavoriteCount")+ " CommunityOwnedDate="+doc.get("CommunityOwnedDate"));
                c=c+1;
            }
            System.out.println("TotalNum="+c);

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
