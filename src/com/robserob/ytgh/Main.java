package com.robserob.ytgh;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Font;
import java.io.IOException;
import java.util.Random;

import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;

import javax.swing.JTextArea;
import javax.swing.JProgressBar;

import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class Main implements CommentDownloaderCallback {

	private JFrame frmYoutubeGiveawayHelper;
	private JTextField youtubeURL;
	private JPanel animationFrame;
	private JButton goButton;
	private JCheckBox shouldSkipAnimation;
	private JCheckBox subscribeNeeded;
	private JLabel progress;
	public JProgressBar progressBar;
	private Timer timer;
	private String winnerLog = "";
	private ContestantBag contestantBag = new ContestantBag();
	private CommentDownloader commentDownloader;
	int numberOfComments = 0;
	private JLabel lblAuthor;
	private JLabel lblHttprobserobdk;
	private JTextField channelURL;
	private AsyncHttpClient asyncHttpClient = new AsyncHttpClient();

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Main window = new Main();
					window.frmYoutubeGiveawayHelper.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Main() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		commentDownloader = new CommentDownloader(this);
		
		frmYoutubeGiveawayHelper = new JFrame();
		frmYoutubeGiveawayHelper.setTitle("Youtube Giveaway Helper 2.0");
		frmYoutubeGiveawayHelper.setBounds(100, 100, 550, 425);
		frmYoutubeGiveawayHelper.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmYoutubeGiveawayHelper.getContentPane().setLayout(null);
		
		youtubeURL = new JTextField();
		youtubeURL.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent arg0) {
				goButtonUpdate();
			}
		});
		youtubeURL.setBounds(133, 11, 291, 29);
		frmYoutubeGiveawayHelper.getContentPane().add(youtubeURL);
		youtubeURL.setColumns(10);
		
		JLabel lblNewLabel = new JLabel("Youtube ID/URL:");
		lblNewLabel.setBounds(10, 15, 113, 21);
		frmYoutubeGiveawayHelper.getContentPane().add(lblNewLabel);
		
		final JCheckBox noDoubleComments = new JCheckBox("No double comments");
		noDoubleComments.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				contestantBag.setFilterDoubleComments(noDoubleComments.isSelected());
			}
		});
		noDoubleComments.setBounds(260, 86, 149, 23);
		frmYoutubeGiveawayHelper.getContentPane().add(noDoubleComments);
		
		animationFrame = new JPanel();
		animationFrame.setBounds(10, 120, 514, 230);
		frmYoutubeGiveawayHelper.getContentPane().add(animationFrame);
		
		goButton = new JButton("Go!");
		goButton.setEnabled(false);
		goButton.setFont(new Font("Tahoma", Font.PLAIN, 16));
		goButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (contestantBag.getNumberOfContestants() > 0) {
					Object[] options = {"Yes use existing data",
                    "No redownload the data"};
					int n = JOptionPane.showOptionDialog(frmYoutubeGiveawayHelper,
					    "Would you want to reuse the downloaded data?",
					    "Reuse allready downloaded data?",
					    JOptionPane.YES_NO_OPTION,
					    JOptionPane.QUESTION_MESSAGE,
					    null,     //do not use a custom Icon
					    options,  //the titles of buttons
					    options[0]); //default button title
					if (n == JOptionPane.YES_OPTION) {
						progress.setFont(new Font("Tahoma", Font.PLAIN, 16));
						findWinner();
					} else {
						progress.setFont(new Font("Tahoma", Font.PLAIN, 16));
						contestantBag.emptyBag();
						Go();
					}
				} else {
					Go();
				}
			}
		});
		goButton.setBounds(435, 11, 89, 59);
		frmYoutubeGiveawayHelper.getContentPane().add(goButton);
		animationFrame.setLayout(null);
		
		progress = new JLabel();
		progress.setBounds(0, 0, 514, 230);
		progress.setFont(new Font("Tahoma", Font.PLAIN, 16));
		progress.setHorizontalAlignment(SwingConstants.CENTER);
		progress.setVerticalAlignment(SwingConstants.CENTER);
		progress.setHorizontalTextPosition(SwingConstants.CENTER);
		animationFrame.add(progress);
		
		progressBar = new JProgressBar();
		progressBar.setValue(0);
		progressBar.setVisible(false);
		progressBar.setBounds(10, 194, 494, 25);
		animationFrame.add(progressBar);
		
		JButton btnVisLog = new JButton("Show winner log");
		btnVisLog.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFrame results = new JFrame();
				results.setBounds(100, 100, 400, 300);
				results.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				results.getContentPane().setLayout(new BorderLayout(0, 0));
				
				JTextArea textArea = new JTextArea();
				textArea.setText(winnerLog);
				JScrollPane sp = new JScrollPane(textArea); 
				results.setVisible(true);
				results.getContentPane().add(sp, BorderLayout.CENTER);
			}
		});
		btnVisLog.setBounds(10, 86, 132, 23);
		frmYoutubeGiveawayHelper.getContentPane().add(btnVisLog);
		
		lblAuthor = new JLabel("Author");
		lblAuthor.setBounds(10, 361, 46, 14);
		frmYoutubeGiveawayHelper.getContentPane().add(lblAuthor);
		
		lblHttprobserobdk = new JLabel("http://robserob.dk");
		lblHttprobserobdk.setBounds(415, 361, 109, 14);
		frmYoutubeGiveawayHelper.getContentPane().add(lblHttprobserobdk);
		
		shouldSkipAnimation = new JCheckBox("Skip animation");
		shouldSkipAnimation.setBounds(148, 86, 115, 23);
		frmYoutubeGiveawayHelper.getContentPane().add(shouldSkipAnimation);
		
		subscribeNeeded = new JCheckBox("Only subscribers");
		subscribeNeeded.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				goButtonUpdate();
			}
		});
		subscribeNeeded.setBounds(405, 86, 123, 23);
		frmYoutubeGiveawayHelper.getContentPane().add(subscribeNeeded);
		
		JLabel lblYourChannelUrl = new JLabel("Your channel URL");
		lblYourChannelUrl.setBounds(10, 54, 113, 21);
		frmYoutubeGiveawayHelper.getContentPane().add(lblYourChannelUrl);
		
		channelURL = new JTextField();
		channelURL.setColumns(10);
		channelURL.setBounds(133, 51, 291, 29);
		channelURL.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent arg0) {
				goButtonUpdate();
			}
		});
		frmYoutubeGiveawayHelper.getContentPane().add(channelURL);
	}
	
	private void goButtonUpdate() {
		if (subscribeNeeded.isSelected()) {
			if (getYoutubeID().length() > 0 && getChannelURL().length() > 0)				
				goButton.setEnabled(true);
			else
				goButton.setEnabled(false);
		} else {
			if (getYoutubeID().length() > 0)
				goButton.setEnabled(true);
			else
				goButton.setEnabled(false);
		}
	}
	
	private String getChannelURL() {
		String url = channelURL.getText();
		if (url.indexOf("com/") > 0) {
			int start = url.indexOf("com/") + 4;
			int end = url.length();
			if (url.indexOf("?") > 0) {
				end = url.indexOf("?");
			}
			String str = url.substring(start, end);
			String[] split = str.split("/");
			System.out.println("/" + split[0] + "/" + split[1]);
			return "/" + split[0] + "/" + split[1];
		}
		return "";
	}
	
	private String getYoutubeID() {
		if (youtubeURL.getText().length() == 11) {
			return youtubeURL.getText();
		} else if (youtubeURL.getText().indexOf("?v=") > 0) {
			int start = youtubeURL.getText().indexOf("?v=")+3;
			int end = start+11;
			return youtubeURL.getText().substring(start, end);
		}		
		return "";
	}
	
	public void updateProgress(final int percent, final String state) {
		progressBar.setValue(percent);
		progress.setText(state);
	}
	
	private void Go() {		
		updateProgress(0, "Getting the total number of comments.");
		goButton.setEnabled(false);
		progressBar.setValue(0);
		progressBar.setVisible(true);
		
		commentDownloader.beginDownload(getYoutubeID(), contestantBag);
	}
	
	public void downloadFinished() {
		progressBar.setVisible(false);
		findWinner();
	}
	
	private void findWinner() {
		progress.setText("<html><body>We have "+contestantBag.getNumberOfContestants()+" comments in the bag!</body></html>");
		ActionListener taskPerformer = new ActionListener() {
			int commentCount = 0;
			int fase = 0;
			Random random = new Random();
	        public void actionPerformed(ActionEvent evt) {
	        	if (shouldSkipAnimation.isSelected()) {
	        		goButton.setEnabled(true);
	        		Contestant randomContestant = contestantBag.getRandomContestant();
	        		if (subscribeNeeded.isSelected()) {
	        			progress.setFont(new Font("Tahoma", Font.PLAIN, 16));
	        			progress.setText("<html><body>Drew out "+randomContestant.username+"!<br>Checking subscription status</body></html>");
	        			checkWinner(randomContestant);
	        		} else {
	        			progress.setFont(new Font("Tahoma", Font.PLAIN, 16));
	        			progress.setText("<html><body>Congratulations "+randomContestant.username+"!<br>Profile URL: "+randomContestant.profileURL+")<br>You won with the message number "+randomContestant.messageID+":<br>"+randomContestant.message+"</body></html>");
	        			winnerLog = winnerLog+ randomContestant.username+"\n"+randomContestant.profileURL+"\n\n";
	        		}
	        		timer.stop();
	    		} else {
		        	if (fase == 0) {
		        		progress.setFont(new Font("Tahoma", Font.PLAIN, 13));
		        		if (commentCount > 100) {
		        			fase=1;
		        			timer.setDelay(25);
		        			commentCount = 0;
		        			progress.setFont(new Font("Tahoma", Font.PLAIN, 15));
		        		}
		        	} else if (fase == 1) {
		        		if (commentCount > 75) {
		        			fase=2;
		        			timer.setDelay(50);
		        			commentCount = 0;
		        			progress.setFont(new Font("Tahoma", Font.PLAIN, 17));
		        		}
		        	} else if (fase == 2) {
		        		if (commentCount > 50) {
		        			fase=3;
		        			timer.setDelay(100);
		        			commentCount = 0;
		        			progress.setFont(new Font("Tahoma", Font.PLAIN, 20));
		        		}
		        	} else if (fase == 3) {
		        		if (commentCount > 25) {
		        			fase=4;
		        			timer.setDelay(200);
		        			commentCount = 0;
		        			progress.setFont(new Font("Tahoma", Font.PLAIN, 22));
		        		}
		        	} else if (fase == 4) {
		        		if (commentCount > 10) {
		        			fase=5;
		        			timer.setDelay(500);
		        			commentCount = 0;
		        			progress.setFont(new Font("Tahoma", Font.PLAIN, 25));
		        		}
		        	} else if (fase == 5) {
		        		if (commentCount > random.nextInt(50)) {
		        			fase=6;
		        			timer.setDelay(1000);
		        			commentCount = 0;
		        			progress.setFont(new Font("Tahoma", Font.PLAIN, 29));
							timer.stop();
		        		}
		        	}
		        	
		        	commentCount++;
		        	if (fase == 6) {	        		
		        		goButton.setEnabled(true);
		        		Contestant randomContestant = contestantBag.getRandomContestant();
		        		if (subscribeNeeded.isSelected()) {
		        			progress.setFont(new Font("Tahoma", Font.PLAIN, 16));
		        			progress.setText("<html><body>Drew out "+randomContestant.username+"!<br>Checking subscription status</body></html>");
		        			checkWinner(randomContestant);
		        		} else {
		        			progress.setFont(new Font("Tahoma", Font.PLAIN, 16));
		        			progress.setText("<html><body>Congratulations "+randomContestant.username+"!<br>Profile URL: "+randomContestant.profileURL+")<br>You won with the message number "+randomContestant.messageID+":<br>"+randomContestant.message+"</body></html>");
		        			winnerLog = winnerLog+ randomContestant.username+"\n"+randomContestant.profileURL+"\n\n";
		        		}
		        	} else {
		        		Contestant randomContestant = contestantBag.getRandomContestant();
	        			progress.setText("<html><body>"+randomContestant.username+"</body></html>");
		        	}
	    		}
	        }
        };
        timer = new Timer( 10 , taskPerformer);
        timer.setRepeats(true);
        timer.setInitialDelay(2500);
        timer.start();
	}
	
	private void checkWinner(final Contestant contestant) {
		progress.setFont(new Font("Tahoma", Font.PLAIN, 16));
		progress.setText("<html><body>Drew out "+contestant.username+"!<br>Checking subscription status</body></html>");	
		System.out.println("Checking youtube user: https://www.youtube.com"+contestant.profileURL+"/about");
		try {
			asyncHttpClient.prepareGet("https://www.youtube.com"+contestant.profileURL+"/about").execute(new AsyncCompletionHandler<Response>(){

			    @Override
			    public Response onCompleted(Response response) throws Exception{
			    	System.out.println("Page downloaded!");
			    	String body = response.getResponseBody();
			    	System.out.println("Got body");
			    	byte[] b = body.getBytes("ISO-8859-1");
			    	body = new String(b, "UTF-8");
			    	System.out.println("Parsing body");
			    	Document doc = Jsoup.parse(body);
			    	System.out.println("Body parsed with Jsoup");
			    	Elements comments = doc.select(".about-subscriptions");
			    	if (comments.size() == 0) {
			    		progress.setText("<html><body>Subscription data was hidden...</body></html>");
			    		Object[] options = {"Allow user to win",
	                    "No find new winner"};
			    		int n = JOptionPane.showOptionDialog(frmYoutubeGiveawayHelper,
			    			contestant.username + " has his/her subscriptions hidden. Should the user be allowed to win?",
			    			"Subscriptions hidden",
							JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE,
							null,     //do not use a custom Icon
							options,  //the titles of buttons
							options[0]); //default button title
						if (n == JOptionPane.YES_OPTION) {
							progress.setFont(new Font("Tahoma", Font.PLAIN, 16));
		        			progress.setText("<html><body>Congratulations "+contestant.username+"!<br>Profile URL: "+contestant.profileURL+")<br>You won with the message number "+contestant.messageID+":<br>"+contestant.message+"</body></html>");
		        			winnerLog = winnerLog+ contestant.username+"\n"+contestant.profileURL+"\n\n";
						} else {
							checkWinner(contestantBag.getRandomContestant());
						}
			    	} else {
			    		// Searches for the channel in either the subscriptions or the recommendation box.
			    		Elements channelSearch = doc.select(".channel-summary-list-item a[href="+getChannelURL()+"]");
			    		if (channelSearch.size() == 0) {
			    			// Not a subscriber.. Find a new contestant.
			    			checkWinner(contestantBag.getRandomContestant());
			    		} else {
			    			progress.setFont(new Font("Tahoma", Font.PLAIN, 16));
		        			progress.setText("<html><body>Congratulations "+contestant.username+"!<br>Profile URL: "+contestant.profileURL+")<br>You won with the message number "+contestant.messageID+":<br>"+contestant.message+"</body></html>");
		        			winnerLog = winnerLog+ contestant.username+"\n"+contestant.profileURL+"\n\n";
			    		}
			    	}
			    	return response;
			    }

			    @Override
			    public void onThrowable(Throwable t){
			    	System.out.println("An error occured oh shit!");
			    	System.out.println(t.toString());
			    	t.printStackTrace();
			    	System.out.println("Just trying again...");
			    	checkWinner(contestant);
			        // Something wrong happened.
			    }
			});
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
