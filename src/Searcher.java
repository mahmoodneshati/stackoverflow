/**
 * Created by Zohreh on 08/20/2015.
 */
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.highlight.TokenSources;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Paths;
import java.util.Date;

public class Searcher {
    public static void main(String[] args) throws IOException, ParseException {
        long start = new Date().getTime();

        String indexDir = "index2";
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

                System.out.println("Id=" + doc.get("Id") + " PostTypeId=" + doc.get("PostTypeId") + " ParentId=" + doc.get("ParentId") +
                        " AcceptedAnswerId=" + doc.get("AcceptedAnswerId") + " CreationDate=" + doc.get("CreationDate") + " Score=" +
                        doc.get("Score") + " ViewCount=" + doc.get("ViewCount") + " Body=" + doc.get("Body") + " OwnerUserId=" + doc.get("OwnerUserId") +
                        " OwnerDisplayName=" + doc.get("OwnerDisplayName") + " LastEditorUserId=" + doc.get("LastEditorUserId") + " LastEditorDisplayName=" +
                        doc.get("LastEditorDisplayName") + " LastEditDate=" + doc.get("LastEditDate") + " LastActivityDate=" + doc.get("LastActivityDate")
                        + " ClosedDate=" + doc.get("ClosedDate") + " Title=" + doc.get("Title") + " Tags=" + doc.get("Tags") + " AnswerCount=" + doc.get("AnswerCount") +
                        " CommentCount=" + doc.get("CommentCount") + " FavoriteCount=" + doc.get("FavoriteCount") + " CommunityOwnedDate=" + doc.get("CommunityOwnedDate"));
                c=c+1;

                Terms terms = reader.getTermVector(i, "Body"); //get terms vectors for one document and one field
                System.out.println("Body Terms:");
                if (terms != null && terms.size() > 0) {
                    TermsEnum termsEnum = terms.iterator(); // access the terms for this field
                    BytesRef term = null;
                    while((term = termsEnum.next()) != null) {
                        final String keyword = term.utf8ToString();
                        long termFreq = termsEnum.totalTermFreq();
                        System.out.println("term: "+keyword+", termFreq = "+termFreq);

                    }
                }

                Terms terms2 = reader.getTermVector(i, "Title"); //get terms vectors for one document and one field
                System.out.println("Title Terms:");
                if (terms != null && terms.size() > 0) {
                    TermsEnum termsEnum = terms.iterator(); // access the terms for this field
                    BytesRef term = null;
                    while((term = termsEnum.next()) != null) {
                        final String keyword = term.utf8ToString();
                        long termFreq = termsEnum.totalTermFreq();
                        System.out.println("term: "+keyword+", termFreq = "+termFreq);

                    }
                }
            }
            System.out.println("TotalNum="+c);

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
