import java.io.*;

public class Main {
	public static void main(String[] args) {
		//System.out.println("Hello world!");
		String pathToHtmlFile = "/Users/Torcherist/Documents/school/Spring2014/cs172/project/01/web-search-engine/output_directory/www.yale.edu_graduateschool_admissions_";
		File f = new File(pathToHtmlFile);
		Parser p = new Parser();
		if (p.processFile(f)!=null) {
			System.out.println("Success!");
		}
		else {
			System.out.println("Failure!");
		}
		Index i = new Index();
		i.go("/Users/Torcherist/Documents/school/Spring2014/cs172/project/01/web-search-engine/output_directory");
	}
}