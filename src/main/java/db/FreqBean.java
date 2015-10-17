package db;

public class FreqBean {
	public String meshTerm;
	public String journal;
	public String year;
	public int freq;
	
	public String getMeshTerm() {
		return meshTerm;
	}
	public void setMeshTerm(String meshTerm) {
		this.meshTerm = meshTerm;
	}
	public String getJournal() {
		return journal;
	}
	public void setJournal(String journal) {
		this.journal = journal;
	}
	public String getYear() {
		return year;
	}
	public void setYear(String year) {
		this.year = year;
	}
	public int getFreq() {
		return freq;
	}
	public void setFreq(int freq) {
		this.freq = freq;
	}
	public FreqBean(String meshTerm, String journal, String year, int freq) {
		super();
		this.meshTerm = meshTerm;
		this.journal = journal;
		this.year = year;
		this.freq = freq;
	}
}
