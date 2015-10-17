package db;

public class ArticleBean {
	public String pmid;
	public String xmlKeyword;
	public String journalName;
	
	public String getPmid() {
		return pmid;
	}
	public void setPmid(String pmid) {
		this.pmid = pmid;
	}
	public String getXmlKeyword() {
		return xmlKeyword;
	}
	public void setXmlKeyword(String xmlKeyword) {
		this.xmlKeyword = xmlKeyword;
	}
	public String getJournalName() {
		return journalName;
	}
	public void setJournalName(String journalName) {
		this.journalName = journalName;
	}
	
	public ArticleBean(String pmid, String xmlKeyword, String journalName) {
		super();
		this.pmid = pmid;
		this.xmlKeyword = xmlKeyword;
		this.journalName = journalName;
	}
	
}
