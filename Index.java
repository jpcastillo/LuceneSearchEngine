/*
    Note: Much of this code is from Lucene demo example
*/
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Date;

class Index {

    private static Parser jparser = new Parser();
    private static final String iDir = "/Users/Torcherist/Documents/school/Spring2014/cs172/project/02/code/index";
    public Index() {
        //
    }
    public boolean go(String htmldir) {
        return IndexDir(htmldir,iDir);
    }

    /** Index all text files under a directory. */
    public static boolean IndexDir(String htmldir, String indexdir) {
        String indexPath = indexdir;
        String docsPath = htmldir;
        boolean create = true;

        if (htmldir.length()==0 || indexdir.length()==0) {
            return false;
        }

        final File docDir = new File(docsPath);
        if (!docDir.exists() || !docDir.canRead()) {
            return false;
        }

        Date start = new Date();
        try {
            System.out.println("Indexing to directory '" + indexPath + "'...");

            Directory dir = FSDirectory.open(new File(indexPath));
            // :Post-Release-Update-Version.LUCENE_XY:
            Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_48);
            IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_48, analyzer);

            if (create) {
                // Create a new index in the directory, removing any
                // previously indexed documents:
                iwc.setOpenMode(OpenMode.CREATE);
            }
            else {
                // Add new documents to an existing index:
                iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
            }

            // Optional: for better indexing performance, if you
            // are indexing many documents, increase the RAM
            // buffer.  But if you do this, increase the max heap
            // size to the JVM (eg add -Xmx512m or -Xmx1g):
            //
            // iwc.setRAMBufferSizeMB(256.0);

            IndexWriter writer = new IndexWriter(dir, iwc);
            indexDocs(writer, docDir);

            // NOTE: if you want to maximize search performance,
            // you can optionally call forceMerge here.  This can be
            // a terribly costly operation, so generally it's only
            // worth it when your index is relatively static (ie
            // you're done adding documents to it):
            //
            // writer.forceMerge(1);

            writer.close();

            Date end = new Date();
            System.out.println("Indexed in " + (end.getTime() - start.getTime()) + " total milliseconds");
        }
        catch (IOException e) {
            return false;
        }
        return true;
    }

    static void indexDocs(IndexWriter writer, File file) throws IOException {
        // do not try to index files that cannot be read
        if (file.canRead()) {
            if (file.isDirectory()) {
                String[] files = file.list();
                // an IO error could occur
                if (files != null) {
                    for (int i = 0; i < files.length; i++) {
                        indexDocs(writer, new File(file, files[i]));
                    }
                }
            }
            else {
                FileInputStream fis;
                try {
                    fis = new FileInputStream(file);
                }
                catch (FileNotFoundException fnfe) {
                    return;
                }

                try {

                    Document doc = new Document();

                    // Call Parser
                    doc = jparser.processFile(file);
                    if (doc==null) {
                        return;
                    }

                    if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
                        // New index, so we just add the document (no old document can be there):
                        //System.out.println("adding " + file);
                        writer.addDocument(doc);
                    }
                    else {
                        // Existing index (an old copy of this document may have been indexed) so 
                        // we use updateDocument instead to replace the old one matching the exact 
                        // path, if present:
                        //System.out.println("updating " + file);
                        writer.updateDocument(new Term("path", file.getPath()), doc);
                    }
                }
                finally {
                    fis.close();
                }
            }
        }
    }

}