package com.robserob.ytgh;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;

interface CommentDownloaderCallback {
    void updateProgress(final int percent, final String state);
    void downloadFinished();
}

public class CommentDownloader {
	private AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
	private String youtubeID;
	private ContestantBag contestantBag;
	private CommentDownloaderCallback commentDownloaderCallback;
	private int totalComments;
	private int commentsDownloaded;
		
	public CommentDownloader(CommentDownloaderCallback commentDownloaderCallback) {
		this.commentDownloaderCallback = commentDownloaderCallback;
	}
	
	public void setYoutubeID(final String ytid) {
		
	}
	
	public void beginDownload(String youtubeID, ContestantBag contestantBag) {
		this.youtubeID = youtubeID;
		this.contestantBag = contestantBag;
		this.totalComments = 0;
		this.commentsDownloaded = 0;
		downloadNextPage("", 1);
	}
	
	// Called to download each page of comments.
	// The pagetoken is given by the GData API to advance to the next page.
	private void downloadNextPage(final String pagetoken, final int currentPage) {
		System.out.println("Downloading page number " + currentPage);
		try {
			String url = "http://gdata.youtube.com/feeds/api/videos/"+youtubeID+"/comments?prettyprint=true&orderby=published&max-results=25&orderby=published";
			if (!pagetoken.isEmpty())
				url = url +"&start-token="+pagetoken;
			System.out.println("Using URL: "+ url);
			asyncHttpClient.prepareGet(url).execute(new AsyncCompletionHandler<Response>(){

			    @Override
			    public Response onCompleted(Response response) throws Exception{
			    	System.out.println("Page " + currentPage + " downloaded!");
			    	String body = response.getResponseBody();
			    	byte[] b = body.getBytes("ISO-8859-1");
			    	body = new String(b, "UTF-8");
			    	Document doc = Jsoup.parse(body);
			    	Elements comments = doc.select("entry");
			    	for (Element comment : comments) {
			    		int messageID = contestantBag.getNumberOfContestants()+1;
			    		System.out.println("Getting comment no "+ messageID);
			    		Element userElement = comment.select("author").get(0);
			    		String profileURL = userElement.select("uri").get(0).html();
			    		String username = userElement.select("name").get(0).html();
			    		// Avoid problems with a wierd comment format glitch where there sometimes are no p element inside the .comment-text div.
			    		Element messageElement = comment.select("content").get(0);
			    		String message = messageElement.html();
			    		Contestant contestant = new Contestant(username, message, messageID, profileURL);
			    		contestantBag.addContestant(contestant);
			    		commentsDownloaded++;
			    		//System.out.println("Added to bag");
			    	}
			    	commentDownloaderCallback.updateProgress(0, "<html><body>Downloading... "+contestantBag.getNumberOfContestants()+" added to the bag</body></html>");
			    	int hasNext = body.indexOf("<link rel='next' type='application/atom+xml'"); 
			    	if (hasNext > -1) {
			    		Element nextUrl = doc.select("link[rel=next]").get(0);
			    		System.out.println(nextUrl.html());
			    		int startIndex = body.indexOf("comments?orderby=published&amp;start-token=");
			    		startIndex += "comments?orderby=published&amp;start-token=".length();
				    	int endIndex = body.indexOf("&amp;max-results=25&amp;orderby=published'/>", startIndex);
				    	System.out.println("Start: " + startIndex + " End: " + endIndex);
			    		System.out.println("Getting next page with pagetoken "+ body.substring(startIndex, endIndex));
			    		downloadNextPage(body.substring(startIndex, endIndex), currentPage+1);
			    	}
			    	else { 
			    		commentDownloaderCallback.downloadFinished();
			    	}
			        return response;
			    }

			    @Override
			    public void onThrowable(Throwable t){
			    	System.out.println("An error occured oh shit!");
			    	System.out.println(t.toString());
			    	t.printStackTrace();
			    	System.out.println("Just trying again...");
			    	downloadNextPage(pagetoken, currentPage);
			        // Something wrong happened.
			    }
			});
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
