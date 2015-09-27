package PLM;

import org.apache.lucene.index.*;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.*;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.QueryBuilder;

import java.nio.file.Paths;
import java.util.*;
import java.io.IOException;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Query;

/**
 * Created by Zohreh on 8/26/2015.
 */
public class Utility {
    String IndexDir;
    IndexReader reader;
    IndexSearcher searcher;
    Analyzer analyzer;


    public static void main(String args[]) {
        Utility u = new Utility("testindex");
    }

    public Utility(String IndexDir) {
        try {
            reader = DirectoryReader.open(FSDirectory.open(Paths.get(IndexDir)));
            searcher = new IndexSearcher(reader);
            analyzer = new StandardAnalyzer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Query SearchTag(String tag) {
        Query query = new TermQuery(new Term("Tags", tag));
        return query;
    }

    public Query SearchBody(String word) {
        QueryBuilder builder = new QueryBuilder(analyzer);
        return builder.createBooleanQuery("Body", word);
    }

    public Query SearchTitle(String word) {
        QueryBuilder builder = new QueryBuilder(analyzer);
        return builder.createBooleanQuery("Title", word);
    }

    public Query SearchOwnerUserId(Integer UsersID) {
        Query query = NumericRangeQuery.newIntRange("OwnerUserId", UsersID, UsersID, true, true);
        return query;
    }

    public Query SearchPostId(Integer PostID) {
        Query query = NumericRangeQuery.newIntRange("Id", PostID, PostID, true, true);
        return query;
    }

    public Query SearchCreationDateRange(Calendar c1, Calendar c2) {
        return NumericRangeQuery.newLongRange("CreationDate", c1.getTimeInMillis(), c2.getTimeInMillis(), true, true);
    }

    public Query SearchCreationDate(int year) {
        Calendar c1 = getFirstDay(year);
        Calendar c2 = getLastDay(year);
        return NumericRangeQuery.newLongRange("CreationDate", c1.getTimeInMillis(), c2.getTimeInMillis(), true, true);
    }

    private Calendar getFirstDay(int year) {
        Calendar c1 = Calendar.getInstance();
        c1.set(year, Calendar.JANUARY, 1, 0, 0, 0);
        c1.clear(Calendar.MINUTE);
        c1.clear(Calendar.HOUR);
        c1.clear(Calendar.SECOND);
        c1.clear(Calendar.MILLISECOND);
        return c1;
    }

    private Calendar getLastDay(int year) {
        Calendar c2 = Calendar.getInstance();
        c2.set(year,  Calendar.DECEMBER, 31, 23, 59);
        c2.clear(Calendar.MINUTE);
        c2.clear(Calendar.HOUR);
        c2.clear(Calendar.SECOND);
        c2.clear(Calendar.MILLISECOND);
        return c2;
    }

    public BooleanQuery BooleanQueryOr(Query q1, Query q2) {
        BooleanQuery query = new BooleanQuery();
        query.add(q1, BooleanClause.Occur.SHOULD);
        query.add(q2, BooleanClause.Occur.SHOULD);
        return query;
    }

    public BooleanQuery BooleanQueryAnd(Query q1, Query q2) {
        BooleanQuery query = new BooleanQuery();
        query.add(q1, BooleanClause.Occur.MUST);
        query.add(q2, BooleanClause.Occur.MUST);
        return query;
    }

    public void getFreqOfWordInBody(String word) {
        try {
            QueryBuilder builder = new QueryBuilder(analyzer);
            Query query = builder.createBooleanQuery("Body", word);
            TopDocs hits = searcher.search(query, Integer.MAX_VALUE);
            System.out.println(hits.totalHits + " total matching documents");
            ScoreDoc[] ScDocs = hits.scoreDocs;
            for (int i = 0; i < ScDocs.length; ++i) {
                int docId = ScDocs[i].doc;
                Document d = searcher.doc(docId);
                Terms terms = reader.getTermVector(docId, "Body"); //get terms vectors for one document and one field
                if (terms != null && terms.size() > 0) {
                    TermsEnum termsEnum = terms.iterator(); // access the terms for this field
                    BytesRef term = null;
                    while ((term = termsEnum.next()) != null) {
                        final String keyword = term.utf8ToString();
                        long termFreq = termsEnum.totalTermFreq();
                        if (keyword.equalsIgnoreCase(word))
                            System.out.println("DocID: " + d.get("Id") + ", term: " + keyword + ", termFreq = " + termFreq);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Integer getDocCountByWordInBody(String word) {
        try {
            return reader.docFreq(new Term("Body", word.toLowerCase()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public Integer getDocCountByWordInTitle(String word) {
        try {
            return reader.docFreq(new Term("Title", word.toLowerCase()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public Integer getDocCount(Query q) {
        try {
            TopDocs hits = searcher.search(q, Integer.MAX_VALUE);
            return hits.totalHits;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Get PostID of query result
     *
     * @param q input query
     * @return list of PostIDs
     */
    public ArrayList<Integer> getPostIDs(Query q) {
        try {
            ArrayList<Integer> PIDs = new ArrayList<Integer>();
            TopDocs hits = searcher.search(q, Integer.MAX_VALUE);
            //System.out.println(hits.totalHits + " total matching documents");

            ScoreDoc[] ScDocs = hits.scoreDocs;
            for (int i = 0; i < ScDocs.length; ++i) {
                int docId = ScDocs[i].doc;
                Document d = searcher.doc(docId);
                //System.out.println(d.toString());
                System.out.println("Post Id:" + d.get("Id"));
                PIDs.add(Integer.parseInt(d.get("Id")));
            }
            return PIDs;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ArrayList<Integer> getActivityYearsByExpertID(Integer eid) {
        ArrayList<Integer> activityYears = new ArrayList<Integer>();
        try {
            for (int year = 2008; year < 2016; year++) {
                Query q = BooleanQueryAnd(SearchOwnerUserId(eid), SearchCreationDate(year));
                TopDocs hits = searcher.search(q, 1);
                //System.out.println(hits.totalHits + " total matching documents");
                if (hits.totalHits > 0) {
                    activityYears.add(year);
                }
            }
            return activityYears;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return activityYears;
    }

    public ArrayList<String> getTerms(Query q, String field) {
        try {
            ArrayList<String> STerms = new ArrayList<String>();
            TopDocs hits = searcher.search(q, Integer.MAX_VALUE);
            System.out.println(hits.totalHits + " total matching documents");

            ScoreDoc[] ScDocs = hits.scoreDocs;
            System.out.println(field + " Terms:");
            for (int i = 0; i < ScDocs.length; ++i) {
                int docId = ScDocs[i].doc;
                Document d = searcher.doc(docId);
                Terms terms = reader.getTermVector(i, field); //get terms vectors for one document and one field
                if (terms != null && terms.size() > 0) {
                    TermsEnum termsEnum = terms.iterator(); // access the terms for this field
                    BytesRef term = null;
                    while ((term = termsEnum.next()) != null) {
                        final String keyword = term.utf8ToString();
                        long termFreq = termsEnum.totalTermFreq();
                        System.out.println("term: " + keyword + ", termFreq = " + termFreq);
                        STerms.add(keyword);
                    }
                }
            }
            return STerms;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public HashSet<Integer> getExpertsBYTagandYear(String Tag, Integer year) {
        Query q = BooleanQueryAnd(SearchCreationDate(year), SearchTag(Tag));
        HashSet<Integer> ExpertIDs = new HashSet<Integer>();
        try {
            TopDocs hits = searcher.search(q, Integer.MAX_VALUE);
            //System.out.println(hits.totalHits+" total matching documents");
            ScoreDoc[] ScDocs = hits.scoreDocs;
            for (int i = 0; i < ScDocs.length; ++i) {
                int docId = ScDocs[i].doc;
                Document d = searcher.doc(docId);
                ExpertIDs.add(Integer.parseInt(d.get("OwnerUserId")));
            }
            return ExpertIDs;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ExpertIDs;
    }

    public long getFreqOfWordInBody(Integer DocID, String word) {
        try {
            PostingsEnum postingsEnum = MultiFields.getTermDocsEnum(reader,
                    MultiFields.getLiveDocs(reader), "Body", new BytesRef(word));
            int doc;
            int output;
            while ((doc = postingsEnum.nextDoc()) != PostingsEnum.NO_MORE_DOCS) {
                if (Integer.parseInt(reader.document(doc).get("Id")) == DocID) {
                    break;
                }
            }
            output =postingsEnum.freq();
            return output;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return 0;
        }
    }

    public int getIntersection(ArrayList<String> list1, ArrayList<String> l2) {
        list1.retainAll(l2);
        return list1.size();
    }

    public Integer getAuthorsCountByQuery(BooleanQuery query) {
        //TODO implement this function
        return null;
    }
}
