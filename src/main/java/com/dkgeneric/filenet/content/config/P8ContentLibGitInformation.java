package com.dkgeneric.filenet.content.config;

import org.springframework.stereotype.Component;

import com.dkgeneric.commons.config.CommonsLibGitInformation;

/**
 * Provides access to git commit used to create this library. Maven
 * git-commit-id-plugin is used to populate git data.
 */
@Component("p8contentlibGitInformation")
public class P8ContentLibGitInformation extends CommonsLibGitInformation {
	@Override
	public String getProjectName() {
		return "p8-content-lib";
	}
}
