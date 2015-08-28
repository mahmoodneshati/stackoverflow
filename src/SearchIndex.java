/**
 * Created by Zohreh on 08/20/2015.
 */

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


import java.io.IOException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Query;

public class SearchIndex {
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

    public static void main(String[] args) {
        long start = new Date().getTime();

        SearchIndex ms = new SearchIndex();

        long end = new Date().getTime();
        System.out.println("Searching took " + (end - start) + " milliseconds");
    }

    public SearchIndex() {
        try {
            String indexDir = "index2";
            String q = "";

            IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexDir)));
            IndexSearcher searcher = new IndexSearcher(reader);
            Analyzer analyzer = new StandardAnalyzer();
            String field="Id";
            QueryParser parser = new QueryParser(field, analyzer);

            String line;
            int count=0;
            Date date=new Date();
            Date date1=new Date();
            BufferedReader breader = new BufferedReader(new FileReader("Posts1.xml"));
            while ((line = breader.readLine()) != null) {
                if (line.trim().startsWith("<row")) {
                    count +=1;
                    //if (count>1)
                    //    break;
                    //System.out.println(line);
                    Elements row = Jsoup.parse(line).getElementsByTag("row");

                    Integer PId = getIntegerValue(row, "Id");

                    Query query = NumericRangeQuery.newIntRange("Id", PId, PId,true,true);
                    //System.out.println("Searching for: " + query.toString(field));

                    ScoreDoc[] hits = searcher.search(query,Integer.MAX_VALUE).scoreDocs;
                    //System.out.println("Found " + hits.length + " hits.");
                    for (int i = 0; i < hits.length; ++i) {
                        int docId = hits[i].doc;
                        Document d = searcher.doc(docId);
                        //System.out.println(d.toString());
                        List<IndexableField> fields= d.getFields();
                        //System.out.println(FieldName.name()+" "+FieldName.stringValue());
                        for (IndexableField FieldName : fields)
                            switch (FieldName.name()) {
                                case "PostTypeId":
                                    if (((getIntegerValue(row, "PostTypeId") != null) && (getIntegerValue(row, "PostTypeId") != Integer.parseInt(d.get("PostTypeId")))) || ((getIntegerValue(row, "PostTypeId") == null && Integer.parseInt(d.get("PostTypeId")) != -1)))
                                        System.out.println("Error in PostTypeId Field");
                                    break;
                                case "ParentId":
                                    if ((getIntegerValue(row, "ParentId") != null) && (getIntegerValue(row, "ParentId") != Integer.parseInt(d.get("ParentId"))) || ((getIntegerValue(row, "ParentId") == null && Integer.parseInt(d.get("ParentId")) != -1)))
                                        System.out.println("Error in ParentId Field");
                                    break;
                                case "AcceptedAnswerId":
                                    if ((getIntegerValue(row, "AcceptedAnswerId") != null) && (getIntegerValue(row, "AcceptedAnswerId") != Integer.parseInt(d.get("AcceptedAnswerId"))) || ((getIntegerValue(row, "AcceptedAnswerId") == null && Integer.parseInt(d.get("AcceptedAnswerId")) != -1)))
                                        System.out.println("Error in AcceptedAnswerId Field");
                                    break;
                                case "Score":
                                    if ((getIntegerValue(row, "Score") != null) && (getIntegerValue(row, "Score") != Integer.parseInt(d.get("Score"))) || ((getIntegerValue(row, "Score") == null && Integer.parseInt(d.get("Score")) != -1)))
                                        System.out.println("Error in Score Field");
                                    break;
                                case "ViewCount":
                                    if ((getIntegerValue(row, "ViewCount") != null) && (getIntegerValue(row, "ViewCount") != Integer.parseInt(d.get("ViewCount"))) || ((getIntegerValue(row, "ViewCount") == null && Integer.parseInt(d.get("ViewCount")) != -1)))
                                        System.out.println("Error in ViewCount Field");
                                    break;
                                case "OwnerUserId":
                                    if ((getIntegerValue(row, "OwnerUserId") != null) && (getIntegerValue(row, "OwnerUserId") != Integer.parseInt(d.get("OwnerUserId"))) || ((getIntegerValue(row, "OwnerUserId") == null && Integer.parseInt(d.get("OwnerUserId")) != -1)))
                                        System.out.println("Error in OwnerUserId Field");
                                    break;
                                case "LastEditorUserId":
                                    if ((getIntegerValue(row, "LastEditorUserId") != null) && (getIntegerValue(row, "LastEditorUserId") != Integer.parseInt(d.get("LastEditorUserId"))) || ((getIntegerValue(row, "LastEditorUserId") == null && Integer.parseInt(d.get("LastEditorUserId")) != -1)))
                                        System.out.println("Error in LastEditorUserId Field");
                                    break;
                                case "AnswerCount":
                                    if ((getIntegerValue(row, "AnswerCount") != null) && (getIntegerValue(row, "AnswerCount") != Integer.parseInt(d.get("AnswerCount"))) || ((getIntegerValue(row, "AnswerCount") == null && Integer.parseInt(d.get("AnswerCount")) != -1)))
                                        System.out.println("Error in AnswerCount Field");
                                    break;
                                case "CommentCount":
                                    if ((getIntegerValue(row, "CommentCount") != null) && (getIntegerValue(row, "CommentCount") != Integer.parseInt(d.get("CommentCount"))) || ((getIntegerValue(row, "CommentCount") == null && Integer.parseInt(d.get("CommentCount")) != -1)))
                                        System.out.println("Error in CommentCount Field");
                                    break;
                                case "FavoriteCount":
                                    if ((getIntegerValue(row, "FavoriteCount") != null) && (getIntegerValue(row, "FavoriteCount") != Integer.parseInt(d.get("FavoriteCount"))) || ((getIntegerValue(row, "FavoriteCount") == null && Integer.parseInt(d.get("FavoriteCount")) != -1)))
                                        System.out.println("Error in FavoriteCount Field");
                                    break;
                                case "LastEditorDisplayName":
                                    if (!(getStringValue(row, "LastEditorDisplayName").equalsIgnoreCase(d.get("LastEditorDisplayName"))))
                                        System.out.println("Error in LastEditorDisplayName Field");
                                    break;
                                case "OwnerDisplayName":
                                    if (!(getStringValue(row, "OwnerDisplayName").equalsIgnoreCase(d.get("OwnerDisplayName"))))
                                        System.out.println("Error in OwnerDisplayName Field");
                                    break;
                                case "Tags":
                                    if (!(row.attr("Tags").equalsIgnoreCase(d.get("Tags"))))
                                        System.out.println("Error in Tags Field");
                                    break;
                                case "CreationDate":
                                    if (row.attr("CreationDate")=="")
                                        break;
                                    if(!(row.attr("CreationDate").equalsIgnoreCase(d.get("CreationDate"))))
                                        System.out.println("Error in CreationDate Field");
                                    break;
                                case "LastEditDate":
                                    if (row.attr("LastEditDate")=="")
                                        break;
                                    if(!(row.attr("LastEditDate").equalsIgnoreCase(d.get("LastEditDate"))))
                                        System.out.println("Error in LastEditDate Field");
                                    break;
                                case "LastActivityDate":
                                    if (row.attr("LastActivityDate")=="")
                                        break;
                                    if(!(row.attr("LastActivityDate").equalsIgnoreCase(d.get("LastActivityDate"))))
                                        System.out.println("Error in LastActivityDate Field");
                                    break;
                                case "CommunityOwnedDate":
                                    if (row.attr("CommunityOwnedDate")=="")
                                        break;
                                    if(!(row.attr("CommunityOwnedDate").equalsIgnoreCase(d.get("CommunityOwnedDate"))))
                                        System.out.println("Error in CommunityOwnedDate Field");
                                    break;
                                case "ClosedDate":
                                    if (row.attr("ClosedDate")=="")
                                        break;
                                    if(!(row.attr("ClosedDate").equalsIgnoreCase(d.get("ClosedDate"))))
                                        System.out.println("Error in ClosedDate Field");
                                    break;
                                case "Body":
                                    break;
                                case "Title":
                                    break;
                                case "Id":
                                    break;
                                default:
                                    System.out.println("Another field!: "+FieldName.name());
                                    break;
                            }


                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
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
            date = formatter.parse(row.attr(tag));


        } catch (Exception e) {
            date = null;
        }
        return date;
    }

}
