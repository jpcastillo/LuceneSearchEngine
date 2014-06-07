import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;


class Search {

    String index;
    final int hitsPerPage = 10;

    public Search(String indexPath) {
        index = indexPath;
    }

    public void find(String queryString) throws Exception {

        String field = "body";
        String queries = null;
        int repeat = 0;
        boolean raw = false;

        IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(index)));
        IndexSearcher searcher = new IndexSearcher(reader);
        // :Post-Release-Update-Version.LUCENE_XY:
        Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_48);

        // :Post-Release-Update-Version.LUCENE_XY:
        QueryParser parser = new QueryParser(Version.LUCENE_48, field, analyzer);
        while (true) {

            queryString = queryString.trim();
            if (queryString.length() == 0) {
                break;
            }

            Query query = parser.parse(queryString);
            //System.out.println("Searching for: " + query.toString(field));

            if (repeat > 0) { // repeat & time as benchmark
                //Date start = new Date();
                for (int i = 0; i < repeat; i++) {
                    searcher.search(query, null, 100);
                }
                //Date end = new Date();
                //System.out.println("Time: "+(end.getTime()-start.getTime())+"ms");
            }

            /*
                Search and output
            */
            TopDocs results = searcher.search(query,hitsPerPage);
            ScoreDoc[] hits = results.scoreDocs;
            int numTotalHits = results.totalHits;
            int end = Math.min(numTotalHits, hitsPerPage);
            for (int i = 0; i < end; i++) {
                if (raw) { // output raw format
                    System.out.println("doc="+hits[i].doc+" score="+hits[i].score);
                    continue;
                }

                Document doc = searcher.doc(hits[i].doc);
                String snippet = doc.get("snippet");
                String title = doc.get("title");
                String url = doc.get("url");
                String hline = null;

                if (title != null && (title.trim()).length()>0) {
                    hline = title;
                    if (url != null && (url.trim()).length()>0) {
                        hline += " > " + url;
                    }
                }
                else {
                    hline = url;
                }

                //String hline = doc.get("title") + " > " + doc.get("url");
                if (hline != null) {
                    System.out.println((i+1) + ". " + hline);
                    if (snippet != null) {
                        System.out.println("     " + snippet);
                    }
                }
                else {
                    System.out.println((i+1)+". "+"No heading for this document");
                }
            }

            if (queryString != null) {
                break;
            }
        }
        reader.close();

    }
}