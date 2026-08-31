package io.rcrm.api.pojo.albatross;

import java.util.List;

public class CompanyInheritance {

	private String parent_company_slug;
	private List<String> child_company_slugs;

	public CompanyInheritance() {
	}

	public String getParent_company_slug() {
		return parent_company_slug;
	}

	public void setParent_company_slug(String parent_company_slug) {
		this.parent_company_slug = parent_company_slug;
	}

	public List<String> getChild_company_slugs() {
		return child_company_slugs;
	}

	public void setChild_company_slugs(List<String> child_company_slugs) {
		this.child_company_slugs = child_company_slugs;
	}

}