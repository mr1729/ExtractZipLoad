package com.example.sai.ezl.batch;

import org.springframework.batch.item.ItemProcessor;

public class ZipItemProcessor implements ItemProcessor<String,String> {
    @Override
    public String process(String item) throws Exception {
        return "";
    }
}
