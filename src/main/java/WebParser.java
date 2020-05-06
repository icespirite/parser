import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.io.*;
import java.net.*;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;




public class WebParser {
	final int max_depth = 3;
	int i;
	String protocol;
	private HashSet<String> links;

	public WebParser() {
		links = new HashSet<String>();
	}


	public String connect(String URL) {
		String response= new String();
		String host = new String();
		try {
			URL url = new URL(URL);
		    host = url.getHost();
			protocol = url.getProtocol();
			protocol = protocol.toUpperCase();

			String path = url.getPath();
			if (path.length() == 0)
				path ="/";
			System.out.println("url" + url);
			System.out.println("path: "  + path);
			System.out.println("host: "  + host);
			System.out.println("protokol: "  + protocol);
			response = "GET " + path + " "  +  protocol + "/1.0\n" +
					"Host:" + " " + host + "\n\n";
			System.out.println("response: "  + response);
		}
		catch (MalformedURLException e) {
			System.out.println("Malformed URL: " + e.getMessage());
		}


		String recvreply;
		String Document = new String();

		SSLSocket toServer;

		BufferedReader parserToServerBr;
		BufferedWriter parserToServerBw;
		try {

			toServer = (SSLSocket) ((SSLSocketFactory) SSLSocketFactory.getDefault()).createSocket(InetAddress.getByName(host),443);
			toServer.startHandshake();
			parserToServerBr = new BufferedReader(new InputStreamReader(toServer.getInputStream()));
			parserToServerBw = new BufferedWriter(new OutputStreamWriter(toServer.getOutputStream()));
			parserToServerBw.write(response);
			parserToServerBw.flush();

			while (!((recvreply = parserToServerBr.readLine()) == null)) {
				Document += recvreply;


			}
		}
		catch (IOException e) {
			System.out.println( "Exception");
		}

		return Document;
	}


	public void getPageLinks(String URL,int depth) {

		if (!links.contains(URL) && depth <= max_depth) {



			try {

				if (links.add(URL) ) {
					System.out.println("Depth: " + depth +" " + URL);

				}


				String doc = connect(URL);

				if (doc.indexOf("<!DOCTYPE html>") != -1) {
					i++;
					String file = "Page" + i + ".html";

					BufferedWriter writer = new BufferedWriter(new FileWriter(file));
					doc = doc.substring(doc.indexOf("<!DOCTYPE html>"));

					writer.write(doc);
					writer.close();
				}


					Document d = Jsoup.parse(doc);


					Elements PageLink = d.select("a[href]");




				depth++;

				for (Element page : PageLink) {
					getPageLinks(page.attr("abs:href"),depth);

					}
			} catch (IOException e) {
				System.err.println(URL + "': " + e.getMessage());
			}
		}
	}

	public static void main(String[]args) {



		new WebParser().getPageLinks("http://yandex.ru",0);


	}

}
