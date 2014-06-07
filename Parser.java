import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.Scanner;
import java.util.Iterator;

import java.lang.Math;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Element;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import java.util.*;

/*
	JSoup class to parse an HTML file and capture specific details
	of the document like title,headings,body,emphasized text.

	Returns a Lucene Document on success, else null.
*/
class Parser {

	// according to Google, commonly the most common max text message length
	private static final int SNIPPET_LEN = 160;

	// default constructor
	public Parser() {
		;//
	}

	// actual function to call to process html file with JSoup
	public Document processFile(File f) {
		try {
			return getDocument(f);
		}
		catch (FileNotFoundException fne) {
			return null;//System.out.println("FileNotFoundException: " + e.getMessage());
		}
	}

	private static Document getDocument(File f) throws java.io.FileNotFoundException {
		Document ldoc = new Document();
		org.jsoup.nodes.Document html = null;
		try {
			html = Jsoup.parse(f, "UTF-8");
		}
		catch (IOException e) {
			return null;//System.out.println("IOException: " + e.getMessage());
		}

		try {
			String title = html.title();
			String body = html.body().text();
			String snippet = getSnippet(html);
			String h1 = html.select("h1").text();
			String h2 = html.select("h2").text();
			String h3 = html.select("h3").text();
			String em = html.select("em").text();
			String bold = html.select("b").text();
			String italic = html.select("i").text();
			String strong = html.select("strong").text();

			String emph = (em + " " + bold + " " + italic + " " + strong).trim();
			String headings = (h1 + " " + h2 + " " + h3).trim();
			ldoc.add(new Field("docid", f.getName(), Field.Store.YES, Field.Index.NOT_ANALYZED));
			ldoc.add(new Field("url", (f.getName()).replaceAll("_","/"), Field.Store.YES, Field.Index.NOT_ANALYZED));
			ldoc.add(new Field("title", title, Field.Store.YES, Field.Index.NOT_ANALYZED));
			ldoc.add(new Field("snippet", snippet, Field.Store.YES, Field.Index.NOT_ANALYZED));
			ldoc.add(new Field("body", body, Field.Store.NO, Field.Index.ANALYZED));
			ldoc.add(new Field("headings", headings, Field.Store.NO, Field.Index.ANALYZED));
			ldoc.add(new Field("emphasized", emph, Field.Store.NO, Field.Index.ANALYZED));

			//System.out.println(Arrays.toString(ldoc.getFields().toArray()));
			return ldoc;
		}
		catch (NullPointerException npe) {
			return null;//
		}
	}

	private static String cutDownString(String target, int maxChars) {
		if(target == null || target.length() == 0)
			return "";
		return target.substring(0, Math.min(target.length()-1, maxChars-1));
	}
	
	private static String getSnippet(org.jsoup.nodes.Document doc) {
		Element e;
		String str = "";
		if(doc == null) {
			return "";
		}
		e = doc.select("p").first();
		/*
			Check if there is p tag.
			Get the parent tags of the first p tag.
			If the first p tag is not enclosed in a form tag,
			assign it to be the snippet.
			Else use text from body tag.
		*/
		if (e!=null&&(e.parents().forms()).size()==0) {
			str = e.text();
		}
		if(str == "") {
			str = doc.body().text();
		}
		return cutDownString(str, SNIPPET_LEN);
	}

}