package com.example.sai.ExtractZipLoad;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ExtractZipLoadApplicationTests {

	@Test
	void contextLoads() {
		Pattern p = Pattern.compile("(SQL\\d+.[A-Z]+)(?s)");
		Matcher m = p.matcher("SQL123N89");
		m.find();
		System.out.println(m.group(1));
	}

	@Test
	void contextload(){

	}
}
