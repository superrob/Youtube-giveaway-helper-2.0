package com.robserob.ytgh;

import java.util.ArrayList;
import java.util.Random;

public class ContestantBag {
	private ArrayList<Contestant> contestants = new ArrayList<Contestant>();
	private Boolean filterDoubleComments;
	private Random randomGenerator = new Random();
	
	ContestantBag() {
		filterDoubleComments = false;
	}
	
	public void setFilterDoubleComments(Boolean state) {
		this.filterDoubleComments = state;
	}
	public Boolean willFilterDoubleComments() {
		return this.filterDoubleComments;
	}
	public int getNumberOfContestants() {
		return this.contestants.size();
	}
	// Simply clears the bag.
	public void emptyBag() {
		contestants.clear();
	}	
	// Checks if there is an Contestant with the same URL already in the bag. 
	public Boolean checkIfContestantIsInBag(Contestant contestant) {
		for (Contestant current : contestants) {
			if (current.profileURL.equals(contestant.profileURL)) {
				System.out.println("Found a double entry!");
				return true;
			}
		}
		return false;
	}
	// Adds the Contestant to the bag.
	public Boolean addContestant(Contestant contestant) {
		if (filterDoubleComments) {
			if (checkIfContestantIsInBag(contestant)) return false;
		}
		this.contestants.add(contestant);		
		return true;
	}
	// Gets a random Contestant from the bag.
	public Contestant getRandomContestant() {
		int indexToGet = randomGenerator.nextInt(contestants.size());
		return contestants.get(indexToGet);
	}
}
