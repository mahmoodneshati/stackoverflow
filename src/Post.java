import org.apache.lucene.document.*;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Mahmood on 8/18/2015.
 * Google
 */
public class Post {
    public Integer Id;
    public Integer PostTypeId;
    public Integer ParentId;
    public Integer AcceptedAnswerId;
    public Date CreationDate;
    public Integer Score;
    public Integer ViewCount;
    public String Body;
    public Integer OwnerUserId;
    public Integer LastEditorUserId;
    public String LastEditorDisplayName;
    public Date LastEditDate;
    public Date LastActivityDate;
    public String Title;
    public ArrayList<String> Tags;
    public Integer AnswerCount;
    public Integer CommentCount;
    public Integer FavoriteCount;
    public Date CommunityOwnedDate;


    public Post(String xmlLine) {
        Elements row = Jsoup.parse(xmlLine).getElementsByTag("row");

        Id = getIntegerValue(row, "Id");
        PostTypeId = getIntegerValue(row, "PostTypeId");
        ParentId = getIntegerValue(row, "ParentId");
        AcceptedAnswerId = getIntegerValue(row, "AcceptedAnswerId");
        CreationDate = getDateValue(row, "CreationDate");
        Score = getIntegerValue(row, "Score");
        ViewCount = getIntegerValue(row, "ViewCount");
        Body = getStringValue(row, "Body");
        OwnerUserId = getIntegerValue(row, "OwnerUserId");
        LastEditorUserId = getIntegerValue(row, "LastEditorUserId");
        LastEditorDisplayName = getStringValue(row, "LastEditorDisplayName");
        LastEditDate = getDateValue(row, "LastEditDate");
        LastActivityDate = getDateValue(row, "LastActivityDate");
        Title = getStringValue(row, "Title");
        Tags = getStringList(row, "Tags");
        AnswerCount = getIntegerValue(row, "AnswerCount");
        CommentCount = getIntegerValue(row, "CommentCount");
        FavoriteCount = getIntegerValue(row, "FavoriteCount");
        CommunityOwnedDate = getDateValue(row, "CommunityOwnedDate");


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

    public Post() {

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

    public static void main(String args[]) {
        Post p = new Post();
        System.out.println(p.getStringValue(null, null));


    }

    public Document getLuceneDocument() {
        Document doc = new Document();
        doc.add(new IntField("Id", Id != null ? Id : -1, Field.Store.YES));
        doc.add(new IntField("PostTypeId", PostTypeId != null ? PostTypeId : -1, Field.Store.YES));
        doc.add(new IntField("ParentId", ParentId != null ? ParentId : -1, Field.Store.YES));
        doc.add(new IntField("AcceptedAnswerId", AcceptedAnswerId != null ? AcceptedAnswerId : -1, Field.Store.YES));
        doc.add(new IntField("AcceptedAnswerId", AcceptedAnswerId != null ? AcceptedAnswerId : -1, Field.Store.YES));
        doc.add(new StringField("CreationDate",
                CreationDate!=null?DateTools.dateToString(CreationDate, DateTools.Resolution.MINUTE):"",
                Field.Store.YES));
        doc.add(new IntField("Score", Score != null ? Score : -1, Field.Store.YES));
        doc.add(new IntField("ViewCount", ViewCount != null ? ViewCount : -1, Field.Store.YES));
        doc.add(new TextField("Body", Body, Field.Store.NO));
        doc.add(new IntField("OwnerUserId", OwnerUserId != null ? OwnerUserId : -1, Field.Store.YES));
        doc.add(new IntField("LastEditorUserId", LastEditorUserId != null ? LastEditorUserId : -1, Field.Store.YES));
        doc.add(new StringField("LastEditorDisplayName", LastEditorDisplayName != null ? LastEditorDisplayName : "",
                Field.Store.YES));
        doc.add(new StringField("LastEditDate",
                LastEditDate!=null?DateTools.dateToString(LastEditDate, DateTools.Resolution.MINUTE):"",
                Field.Store.YES));
        doc.add(new StringField("LastActivityDate",
                LastActivityDate!=null?DateTools.dateToString(LastActivityDate, DateTools.Resolution.MINUTE):"",
                Field.Store.YES));
        doc.add(new TextField("Title", Title, Field.Store.NO));

        for (String tag : Tags) {
            doc.add(new StringField("Tags", tag, Field.Store.YES));
        }
        doc.add(new IntField("AnswerCount", AnswerCount != null ? AnswerCount : -1, Field.Store.YES));
        doc.add(new IntField("CommentCount", CommentCount != null ? CommentCount : -1, Field.Store.YES));
        doc.add(new IntField("FavoriteCount", FavoriteCount != null ? FavoriteCount : -1, Field.Store.YES));
        doc.add(new StringField("CommunityOwnedDate",
                CommunityOwnedDate!=null?DateTools.dateToString(CommunityOwnedDate, DateTools.Resolution.MINUTE):"",
                Field.Store.YES));

        return doc;
    }
}
